package com.example.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.HomeworkStatus
import com.example.data.local.entities.HomeworkTag
import com.example.data.local.entities.HomeworkTask
import com.example.data.local.entities.PriorityLevel
import com.example.data.local.entities.StudySession
import com.example.data.local.entities.TimetableClass
import com.example.data.local.entities.UserBatch
import com.example.data.ocr.OcrParseResult
import com.example.data.ocr.TimetableOcrParser
import com.example.data.repository.PrepRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class TimerUiState(
    val selectedTask: HomeworkTask? = null,
    val isRunning: Boolean = false,
    val timeRemainingSeconds: Long = 25 * 60L, // 25 min Pomodoro
    val totalTargetSeconds: Long = 25 * 60L,
    val isPomodoroMode: Boolean = true,
    val ambientSoundEnabled: Boolean = false,
    val sessionElapsedSeconds: Long = 0L
)

data class OcrDialogState(
    val isParsing: Boolean = false,
    val parseResult: OcrParseResult? = null,
    val selectedBatchCode: String = "",
    val errorMessage: String? = null,
    val showConfirmationDialog: Boolean = false
)

class PrepViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = PrepRepository(db)
    private val ocrParser = TimetableOcrParser(application)

    // Current selected day in Timetable view (1=Mon .. 7=Sun)
    private val _selectedDay = MutableStateFlow(getTodayDayOfWeekIndex())
    val selectedDay: StateFlow<Int> = _selectedDay.asStateFlow()

    // Flows
    val activeBatch: StateFlow<UserBatch?> = repository.activeBatch
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allClasses: StateFlow<List<TimetableClass>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeTasks: StateFlow<List<HomeworkTask>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backlogTasks: StateFlow<List<HomeworkTask>> = repository.backlogTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val completedTasks: StateFlow<List<HomeworkTask>> = repository.completedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<HomeworkTask>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySession>> = repository.studySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalStudyTimeSeconds: StateFlow<Long?> = repository.totalStudyTimeSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    // OCR State
    private val _ocrState = MutableStateFlow(OcrDialogState())
    val ocrState: StateFlow<OcrDialogState> = _ocrState.asStateFlow()

    // Focus Timer State
    private val _timerState = MutableStateFlow(TimerUiState())
    val timerState: StateFlow<TimerUiState> = _timerState.asStateFlow()
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            repository.runBacklogAutoMigration()
        }
    }

    private fun getTodayDayOfWeekIndex(): Int {
        val cal = Calendar.getInstance()
        val day = cal.get(Calendar.DAY_OF_WEEK)
        // Calendar.MONDAY = 2, SUNDAY = 1
        return when (day) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            Calendar.SUNDAY -> 7
            else -> 1
        }
    }

    fun selectDay(dayIndex: Int) {
        _selectedDay.value = dayIndex
    }

    // OCR & AI Timetable Processing
    fun parseTimetableInput(rawText: String?, bitmap: Bitmap?) {
        viewModelScope.launch {
            _ocrState.value = OcrDialogState(isParsing = true)
            try {
                val result = ocrParser.parseTimetableTextOrImage(rawText, bitmap)
                val defaultBatch = result.detectedBatches.firstOrNull() ?: "JEE-AIR1-2026"
                _ocrState.value = OcrDialogState(
                    isParsing = false,
                    parseResult = result,
                    selectedBatchCode = defaultBatch,
                    showConfirmationDialog = true
                )
            } catch (e: Exception) {
                _ocrState.value = OcrDialogState(
                    isParsing = false,
                    errorMessage = e.localizedMessage ?: "Failed to parse timetable data"
                )
            }
        }
    }

    fun selectOcrBatchCode(batchCode: String) {
        _ocrState.value = _ocrState.value.copy(selectedBatchCode = batchCode)
    }

    fun confirmImportOcrSchedule() {
        val state = _ocrState.value
        val result = state.parseResult ?: return
        viewModelScope.launch {
            val batch = UserBatch(
                batchCode = state.selectedBatchCode,
                coachingName = result.coachingName
            )
            val batchId = repository.insertBatch(batch)
            val updatedClasses = result.extractedClasses.map { it.copy(batchId = batchId) }
            repository.clearSchedule()
            repository.insertClasses(updatedClasses)
            _ocrState.value = OcrDialogState() // Reset
        }
    }

    fun dismissOcrDialog() {
        _ocrState.value = OcrDialogState()
    }

    // Homework Management
    fun createHomeworkTask(
        subject: String,
        title: String,
        notes: String,
        tag: HomeworkTag,
        priority: PriorityLevel,
        imagePath: String?,
        classId: Long? = null
    ) {
        viewModelScope.launch {
            val task = HomeworkTask(
                classId = classId,
                subject = subject,
                title = title,
                notes = notes,
                imagePath = imagePath,
                tag = tag,
                priority = priority,
                status = HomeworkStatus.PENDING,
                dueDate = System.currentTimeMillis() + (24 * 3600 * 1000L) // End of tomorrow
            )
            repository.insertHomework(task)
        }
    }

    fun markHomeworkCompleted(taskId: Long) {
        viewModelScope.launch {
            repository.markTaskCompleted(taskId)
        }
    }

    fun deleteHomework(taskId: Long) {
        viewModelScope.launch {
            repository.deleteHomework(taskId)
        }
    }

    // Focus Timer Operations
    fun selectTaskForTimer(task: HomeworkTask?) {
        _timerState.value = _timerState.value.copy(
            selectedTask = task,
            timeRemainingSeconds = if (_timerState.value.isPomodoroMode) 25 * 60L else 0L,
            sessionElapsedSeconds = 0L
        )
    }

    fun toggleTimerMode(isPomodoro: Boolean) {
        _timerState.value = _timerState.value.copy(
            isPomodoroMode = isPomodoro,
            timeRemainingSeconds = if (isPomodoro) 25 * 60L else 0L,
            totalTargetSeconds = if (isPomodoro) 25 * 60L else 0L,
            sessionElapsedSeconds = 0L
        )
    }

    fun toggleAmbientSound() {
        _timerState.value = _timerState.value.copy(ambientSoundEnabled = !_timerState.value.ambientSoundEnabled)
    }

    fun startTimer() {
        if (_timerState.value.isRunning) return
        _timerState.value = _timerState.value.copy(isRunning = true)

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_timerState.value.isRunning) {
                delay(1000L)
                val current = _timerState.value
                val newElapsed = current.sessionElapsedSeconds + 1

                if (current.isPomodoroMode) {
                    val newRemaining = current.timeRemainingSeconds - 1
                    if (newRemaining <= 0) {
                        // Timer completed
                        _timerState.value = current.copy(
                            isRunning = false,
                            timeRemainingSeconds = 0,
                            sessionElapsedSeconds = newElapsed
                        )
                        saveTimerSession(newElapsed)
                        break
                    } else {
                        _timerState.value = current.copy(
                            timeRemainingSeconds = newRemaining,
                            sessionElapsedSeconds = newElapsed
                        )
                    }
                } else {
                    // Stopwatch mode
                    _timerState.value = current.copy(
                        sessionElapsedSeconds = newElapsed,
                        timeRemainingSeconds = newElapsed
                    )
                }
            }
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _timerState.value = _timerState.value.copy(isRunning = false)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val current = _timerState.value
        _timerState.value = current.copy(
            isRunning = false,
            timeRemainingSeconds = if (current.isPomodoroMode) current.totalTargetSeconds else 0L,
            sessionElapsedSeconds = 0L
        )
    }

    fun finishAndSaveTimer() {
        timerJob?.cancel()
        val elapsed = _timerState.value.sessionElapsedSeconds
        val current = _timerState.value
        _timerState.value = current.copy(isRunning = false)
        if (elapsed > 0) {
            saveTimerSession(elapsed)
        }
        resetTimer()
    }

    private fun saveTimerSession(elapsedSeconds: Long) {
        val task = _timerState.value.selectedTask
        val subject = task?.subject ?: "General Study"
        viewModelScope.launch {
            repository.recordStudySession(
                taskId = task?.id,
                subject = subject,
                durationSeconds = elapsedSeconds
            )
        }
    }

    fun addManualClass(
        subject: String,
        topic: String,
        roomNumber: String,
        teacher: String,
        startTime: String,
        endTime: String,
        dayOfWeek: Int
    ) {
        viewModelScope.launch {
            val newClass = TimetableClass(
                dayOfWeek = dayOfWeek,
                subject = subject,
                topic = topic,
                roomNumber = roomNumber,
                teacher = teacher,
                startTime = startTime,
                endTime = endTime
            )
            repository.addSingleClass(newClass)
        }
    }
}
