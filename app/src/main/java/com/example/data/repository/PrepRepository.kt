package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.HomeworkStatus
import com.example.data.local.entities.HomeworkTag
import com.example.data.local.entities.HomeworkTask
import com.example.data.local.entities.PriorityLevel
import com.example.data.local.entities.StudySession
import com.example.data.local.entities.TimetableClass
import com.example.data.local.entities.UserBatch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.Calendar

class PrepRepository(private val db: AppDatabase) {

    val activeBatch: Flow<UserBatch?> = db.batchDao().getActiveBatch()
    val allBatches: Flow<List<UserBatch>> = db.batchDao().getAllBatches()
    val allClasses: Flow<List<TimetableClass>> = db.timetableDao().getAllClasses()
    val activeTasks: Flow<List<HomeworkTask>> = db.homeworkDao().getActiveTasks()
    val backlogTasks: Flow<List<HomeworkTask>> = db.homeworkDao().getBacklogTasks()
    val completedTasks: Flow<List<HomeworkTask>> = db.homeworkDao().getCompletedTasks()
    val allTasks: Flow<List<HomeworkTask>> = db.homeworkDao().getAllTasks()
    val studySessions: Flow<List<StudySession>> = db.studySessionDao().getAllSessions()
    val totalStudyTimeSeconds: Flow<Long?> = db.studySessionDao().getTotalStudyTime()

    fun getClassesForDay(dayOfWeek: Int): Flow<List<TimetableClass>> {
        return db.timetableDao().getClassesForDay(dayOfWeek)
    }

    suspend fun insertBatch(batch: UserBatch): Long = withContext(Dispatchers.IO) {
        db.batchDao().deactivateAllBatches()
        db.batchDao().insertBatch(batch.copy(isActive = true))
    }

    suspend fun insertClasses(classes: List<TimetableClass>) = withContext(Dispatchers.IO) {
        db.timetableDao().insertClasses(classes)
    }

    suspend fun addSingleClass(timetableClass: TimetableClass) = withContext(Dispatchers.IO) {
        db.timetableDao().insertClass(timetableClass)
    }

    suspend fun deleteClass(id: Long) = withContext(Dispatchers.IO) {
        db.timetableDao().deleteClassById(id)
    }

    suspend fun clearSchedule() = withContext(Dispatchers.IO) {
        db.timetableDao().clearAllClasses()
    }

    suspend fun insertHomework(task: HomeworkTask): Long = withContext(Dispatchers.IO) {
        db.homeworkDao().insertTask(task)
    }

    suspend fun updateHomework(task: HomeworkTask) = withContext(Dispatchers.IO) {
        db.homeworkDao().updateTask(task)
    }

    suspend fun deleteHomework(id: Long) = withContext(Dispatchers.IO) {
        db.homeworkDao().deleteTaskById(id)
    }

    suspend fun markTaskCompleted(id: Long) = withContext(Dispatchers.IO) {
        val task = db.homeworkDao().getTaskById(id)
        if (task != null) {
            db.homeworkDao().updateTask(task.copy(status = HomeworkStatus.COMPLETED, isBacklog = false))
        }
    }

    suspend fun recordStudySession(taskId: Long?, subject: String, durationSeconds: Long) = withContext(Dispatchers.IO) {
        db.studySessionDao().insertSession(
            StudySession(taskId = taskId, subject = subject, durationSeconds = durationSeconds)
        )
        if (taskId != null) {
            db.homeworkDao().addTimeSpent(taskId, durationSeconds)
        }
    }

    suspend fun runBacklogAutoMigration() = withContext(Dispatchers.IO) {
        // Cutoff: End of today (23:59:59) or current time
        val now = System.currentTimeMillis()
        db.homeworkDao().markOverdueAsBacklog(now)
    }

    suspend fun seedSampleDataIfEmpty() = withContext(Dispatchers.IO) {
        val currentBatch = activeBatch.firstOrNull()
        if (currentBatch == null) {
            // 1. Create Default Batch
            val batchId = db.batchDao().insertBatch(
                UserBatch(
                    batchCode = "JEE-AIR1-2026",
                    coachingName = "Allen Career Institute / Apex Academy",
                    targetExam = "JEE Advanced 2026"
                )
            )

            // 2. Populate Timetable (Mon..Sat)
            val sampleClasses = listOf(
                // Monday
                TimetableClass(batchId = batchId, dayOfWeek = 1, subject = "Physics", topic = "Rotational Motion - Moment of Inertia", roomNumber = "Hall 302", teacher = "Dr. H.C. Verma", startTime = "08:30", endTime = "10:00"),
                TimetableClass(batchId = batchId, dayOfWeek = 1, subject = "Chemistry", topic = "Thermodynamics & Enthalpy", roomNumber = "Lab 104", teacher = "Prof. O.P. Tandon", startTime = "10:15", endTime = "11:45"),
                TimetableClass(batchId = batchId, dayOfWeek = 1, subject = "Mathematics", topic = "Definite Integration & Areas", roomNumber = "Hall 305", teacher = "Prof. R.D. Sharma", startTime = "12:15", endTime = "13:45"),

                // Tuesday
                TimetableClass(batchId = batchId, dayOfWeek = 2, subject = "Physics", topic = "Angular Momentum & Torque", roomNumber = "Hall 302", teacher = "Dr. H.C. Verma", startTime = "08:30", endTime = "10:00"),
                TimetableClass(batchId = batchId, dayOfWeek = 2, subject = "Chemistry", topic = "Chemical Kinetics & Rate Laws", roomNumber = "Lab 104", teacher = "Prof. O.P. Tandon", startTime = "10:15", endTime = "11:45"),
                TimetableClass(batchId = batchId, dayOfWeek = 2, subject = "Mathematics", topic = "Differential Equations & Degree", roomNumber = "Hall 305", teacher = "Prof. R.D. Sharma", startTime = "12:15", endTime = "13:45"),

                // Wednesday
                TimetableClass(batchId = batchId, dayOfWeek = 3, subject = "Physics", topic = "Simple Harmonic Motion (SHM)", roomNumber = "Hall 302", teacher = "Dr. H.C. Verma", startTime = "08:30", endTime = "10:00"),
                TimetableClass(batchId = batchId, dayOfWeek = 3, subject = "Chemistry", topic = "Electrochemistry & Nernst Equation", roomNumber = "Lab 104", teacher = "Prof. O.P. Tandon", startTime = "10:15", endTime = "11:45"),
                TimetableClass(batchId = batchId, dayOfWeek = 3, subject = "Mathematics", topic = "Vector Algebra & Dot Product", roomNumber = "Hall 305", teacher = "Prof. R.D. Sharma", startTime = "12:15", endTime = "13:45"),

                // Thursday
                TimetableClass(batchId = batchId, dayOfWeek = 4, subject = "Physics", topic = "Wave Motion & Doppler Effect", roomNumber = "Hall 302", teacher = "Dr. H.C. Verma", startTime = "08:30", endTime = "10:00"),
                TimetableClass(batchId = batchId, dayOfWeek = 4, subject = "Chemistry", topic = "Organic Reaction Mechanisms (SN1/SN2)", roomNumber = "Lab 104", teacher = "Prof. O.P. Tandon", startTime = "10:15", endTime = "11:45"),
                TimetableClass(batchId = batchId, dayOfWeek = 4, subject = "Mathematics", topic = "3D Geometry - Planes & Lines", roomNumber = "Hall 305", teacher = "Prof. R.D. Sharma", startTime = "12:15", endTime = "13:45"),

                // Friday
                TimetableClass(batchId = batchId, dayOfWeek = 5, subject = "Physics", topic = "Fluid Mechanics & Bernoulli Theorem", roomNumber = "Hall 302", teacher = "Dr. H.C. Verma", startTime = "08:30", endTime = "10:00"),
                TimetableClass(batchId = batchId, dayOfWeek = 5, subject = "Chemistry", topic = "Aldehydes, Ketones & Carboxylic Acids", roomNumber = "Lab 104", teacher = "Prof. O.P. Tandon", startTime = "10:15", endTime = "11:45"),
                TimetableClass(batchId = batchId, dayOfWeek = 5, subject = "Mathematics", topic = "Probability & Bayes Theorem", roomNumber = "Hall 305", teacher = "Prof. R.D. Sharma", startTime = "12:15", endTime = "13:45"),

                // Saturday
                TimetableClass(batchId = batchId, dayOfWeek = 6, subject = "Physics", topic = "Weekly Doubt Solving & Test Analysis", roomNumber = "Auditorium", teacher = "Panel", startTime = "09:00", endTime = "11:00"),
                TimetableClass(batchId = batchId, dayOfWeek = 6, subject = "Mathematics", topic = "Full JEE Main Mock Paper Discussion", roomNumber = "Auditorium", teacher = "Panel", startTime = "11:30", endTime = "13:30")
            )
            db.timetableDao().insertClasses(sampleClasses)

            // 3. Create Sample Homework & Backlog Tasks
            val now = System.currentTimeMillis()
            val dayMillis = 24 * 3600 * 1000L

            val sampleHomework = listOf(
                HomeworkTask(
                    subject = "Physics",
                    title = "Solve Irodov Problems 1.23 to 1.35 on Torque",
                    notes = "Key formula: τ = Iα, L = Iω. Remember perpendicular axis theorem.",
                    tag = HomeworkTag.DPP,
                    priority = PriorityLevel.HIGH,
                    status = HomeworkStatus.PENDING,
                    dueDate = now + dayMillis,
                    timeSpentSeconds = 1800
                ),
                HomeworkTask(
                    subject = "Chemistry",
                    title = "Complete Module Exercise 2 - Thermodynamics",
                    notes = "Focus on q = ΔU + W and adiabatic processes PV^γ = C.",
                    tag = HomeworkTag.MODULE,
                    priority = PriorityLevel.MEDIUM,
                    status = HomeworkStatus.IN_PROGRESS,
                    dueDate = now + dayMillis,
                    timeSpentSeconds = 2400
                ),
                HomeworkTask(
                    subject = "Mathematics",
                    title = "Solve Last 10 Years PYQs on Definite Integrals",
                    notes = "Properties of integrals: ∫[a,b] f(x)dx = ∫[a,b] f(a+b-x)dx.",
                    tag = HomeworkTag.PYQ,
                    priority = PriorityLevel.HIGH,
                    status = HomeworkStatus.PENDING,
                    dueDate = now + dayMillis,
                    timeSpentSeconds = 900
                ),
                // Overdue tasks -> Backlog
                HomeworkTask(
                    subject = "Physics",
                    title = "Backlog: Work Power Energy Revision Sheet",
                    notes = "Overdue by 3 days. Complete before Sunday Mock Test!",
                    tag = HomeworkTag.REVISION,
                    priority = PriorityLevel.HIGH,
                    status = HomeworkStatus.PENDING,
                    dueDate = now - (3 * dayMillis),
                    isBacklog = true,
                    timeSpentSeconds = 600
                ),
                HomeworkTask(
                    subject = "Chemistry",
                    title = "Backlog: Chemical Equilibrium DPP #4",
                    notes = "Overdue by 5 days. Solve Le Chatelier's principle numericals.",
                    tag = HomeworkTag.DPP,
                    priority = PriorityLevel.MEDIUM,
                    status = HomeworkStatus.PENDING,
                    dueDate = now - (5 * dayMillis),
                    isBacklog = true,
                    timeSpentSeconds = 0
                )
            )

            sampleHomework.forEach { db.homeworkDao().insertTask(it) }

            // 4. Seed initial study sessions
            db.studySessionDao().insertSession(StudySession(subject = "Physics", durationSeconds = 1800, timestamp = now - 3600000))
            db.studySessionDao().insertSession(StudySession(subject = "Chemistry", durationSeconds = 2400, timestamp = now - 7200000))
            db.studySessionDao().insertSession(StudySession(subject = "Mathematics", durationSeconds = 1200, timestamp = now - 10800000))
        }
    }
}
