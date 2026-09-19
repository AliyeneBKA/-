package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.GeminiQuizGenerator
import com.example.data.model.CurriculumDoc
import com.example.data.model.Question
import com.example.data.model.QuestionReviewItem
import com.example.data.model.QuizAttemptRecord
import com.example.data.model.TopicStats
import com.example.data.repository.SampleData
import com.example.data.repository.SharedCurriculumRepository
import com.example.features.auth.AuthService
import com.example.features.auth.AuthState
import com.example.features.auth.AuthUser
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID

enum class AppTab {
    QUIZ,
    DOCUMENTS,
    ANALYTICS,
    AUTH
}

data class QuizUiState(
    val currentTab: AppTab = AppTab.QUIZ,
    val documents: List<CurriculumDoc> = SampleData.documents,
    val selectedDoc: CurriculumDoc? = SampleData.documents.firstOrNull(),
    val activeQuestions: List<Question> = SampleData.documents.first().questions,
    val currentQuestionIndex: Int = 0,
    val selectedOptionIndex: Int? = null,
    val hasAnsweredCurrent: Boolean = false,
    val totalAttempts: Int = 0,
    val correctCount: Int = 0,
    val isQuizFinished: Boolean = false,
    val isTimeExpired: Boolean = false,
    val timeRemainingSeconds: Int = 30 * 60, // 30 minutes = 1800s
    val totalQuizTimeSeconds: Int = 30 * 60,
    val isTimerRunning: Boolean = false,
    val userAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
    val specializations: List<String> = emptyList(),
    val selectedCategoryFilter: String = "الكل",
    val isGenerating: Boolean = false,
    val generationError: String? = null,
    val attemptRecords: List<QuizAttemptRecord> = emptyList(),
    val authState: AuthState = AuthState.Authenticated(
        AuthUser(
            uid = "student_usr_101",
            email = "student@edu.qcm.app",
            displayName = "طالب جامعي متميز"
        )
    )
) {
    val currentQuestion: Question?
        get() = activeQuestions.getOrNull(currentQuestionIndex)

    val formattedTimeRemaining: String
        get() {
            val minutes = timeRemainingSeconds / 60
            val seconds = timeRemainingSeconds % 60
            return String.format(Locale.US, "%02d:%02d", minutes, seconds)
        }

    val comprehensionPercentage: Int
        get() = if (currentQuestionIndex > 0 || hasAnsweredCurrent) {
            val answeredCount = if (hasAnsweredCurrent) currentQuestionIndex + 1 else currentQuestionIndex
            if (answeredCount > 0) (correctCount * 100) / answeredCount else 0
        } else 0

    // Full Review list of all questions with scoring (+0.25 for correct, -0.25 for incorrect per option weight)
    val questionReviews: List<QuestionReviewItem>
        get() = activeQuestions.mapIndexed { idx, q ->
            val selected = userAnswers[idx]
            val hasAnswer = selected != null
            val isCorrect = selected == q.correctOptionIndex
            val step = 1.0 / q.options.size.coerceAtLeast(1)
            val delta = if (hasAnswer) {
                if (isCorrect) step else -step
            } else 0.0

            QuestionReviewItem(
                questionIndex = idx,
                question = q,
                userSelectedOption = selected,
                isCorrect = isCorrect,
                pointDelta = delta
            )
        }

    val wrongQuestionsReviews: List<QuestionReviewItem>
        get() = questionReviews.filter { it.userSelectedOption != null && !it.isCorrect }

    val correctQuestionsCount: Int
        get() = questionReviews.count { it.userSelectedOption != null && it.isCorrect }

    val wrongQuestionsCount: Int
        get() = questionReviews.count { it.userSelectedOption != null && !it.isCorrect }

    val unansweredQuestionsCount: Int
        get() = questionReviews.count { it.userSelectedOption == null }

    val positivePoints: Double
        get() = questionReviews.filter { it.userSelectedOption != null && it.isCorrect }.sumOf { it.pointDelta }

    val penaltyDeduction: Double
        get() = questionReviews.filter { it.userSelectedOption != null && !it.isCorrect }.sumOf { kotlin.math.abs(it.pointDelta) }

    val rawScore: Double
        get() = questionReviews.sumOf { it.pointDelta }

    val maxPossibleRawScore: Double
        get() = activeQuestions.sumOf { 1.0 / it.options.size.coerceAtLeast(1) }

    // Final score out of 20 with negative marking (+0.25 / -0.25 scaled to 20)
    val scoreOutOf20: Double
        get() {
            val maxRaw = maxPossibleRawScore
            if (maxRaw <= 0.0) return 0.0
            val calculated = (rawScore / maxRaw) * 20.0
            return calculated.coerceIn(0.0, 20.0)
        }

    val formattedScoreOutOf20: String
        get() = String.format(Locale.US, "%.2f", scoreOutOf20)
}

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val authService = AuthService()
    private val sharedRepo = SharedCurriculumRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // Load persistent community documents and specializations
        val initialDocs = sharedRepo.getCommunityDocuments()
        val initialSpecs = sharedRepo.getSpecializations()
        val firstDoc = initialDocs.firstOrNull() ?: SampleData.documents.first()

        _uiState.update {
            it.copy(
                documents = initialDocs,
                selectedDoc = firstDoc,
                activeQuestions = firstDoc.questions,
                specializations = initialSpecs
            )
        }

        // Collect auth state changes
        viewModelScope.launch {
            authService.authState.collect { auth ->
                _uiState.update { it.copy(authState = auth) }
            }
        }

        // Initial historical attempt record
        val initialHistory = listOf(
            QuizAttemptRecord(
                id = "init_att_1",
                documentTitle = "القانون المدني: نظرية العقد والالتزامات",
                timestamp = System.currentTimeMillis() - 86400000L,
                totalQuestions = 5,
                correctAnswers = 4,
                wrongAnswers = 1,
                unansweredCount = 0,
                totalTaps = 6,
                percentage = 80,
                scoreOutOf20 = 15.00,
                rawScore = 0.75,
                positivePoints = 1.00,
                penaltyDeduction = 0.25,
                topicStats = listOf(
                    TopicStats("مصادر القانون والتشريع (المادة 1)", 1, 1),
                    TopicStats("أهلية التعاقد (المادة 14)", 1, 1),
                    TopicStats("القوة الملزمة للعقد (المادة 106)", 1, 1),
                    TopicStats("أركان العقد والرضا (المادة 59)", 1, 1),
                    TopicStats("المسؤولية المدنية والتقصيرية (المادة 124)", 1, 0)
                )
            )
        )
        _uiState.update { it.copy(attemptRecords = initialHistory) }

        // Start 30-minute quiz timer
        startQuizTimer()
    }

    fun setTab(tab: AppTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun startQuizTimer() {
        timerJob?.cancel()
        _uiState.update {
            it.copy(
                timeRemainingSeconds = 30 * 60, // 30 minutes
                isTimerRunning = true,
                isTimeExpired = false
            )
        }
        timerJob = viewModelScope.launch {
            while (isActive) {
                delay(1000)
                val current = _uiState.value
                if (current.isQuizFinished) {
                    break
                }
                if (current.timeRemainingSeconds <= 1) {
                    _uiState.update { it.copy(timeRemainingSeconds = 0, isTimerRunning = false) }
                    finishQuiz(timeExpired = true)
                    break
                } else {
                    _uiState.update { it.copy(timeRemainingSeconds = it.timeRemainingSeconds - 1) }
                }
            }
        }
    }

    fun selectDocument(doc: CurriculumDoc) {
        _uiState.update {
            it.copy(
                selectedDoc = doc,
                activeQuestions = doc.questions,
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                hasAnsweredCurrent = false,
                totalAttempts = 0,
                correctCount = 0,
                userAnswers = emptyMap(),
                isQuizFinished = false,
                isTimeExpired = false,
                currentTab = AppTab.QUIZ
            )
        }
        startQuizTimer()
    }

    fun onOptionSelected(optionIndex: Int) {
        val state = _uiState.value
        val question = state.currentQuestion ?: return

        val isCorrect = optionIndex == question.correctOptionIndex
        val isFirstAnswer = !state.hasAnsweredCurrent

        _uiState.update { current ->
            val newTotalAttempts = current.totalAttempts + 1
            val newCorrectCount = if (isFirstAnswer && isCorrect) current.correctCount + 1 else current.correctCount
            val updatedUserAnswers = current.userAnswers + (current.currentQuestionIndex to optionIndex)
            current.copy(
                selectedOptionIndex = optionIndex,
                hasAnsweredCurrent = true,
                totalAttempts = newTotalAttempts,
                correctCount = newCorrectCount,
                userAnswers = updatedUserAnswers
            )
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        val nextIndex = state.currentQuestionIndex + 1

        if (nextIndex < state.activeQuestions.size) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = nextIndex,
                    selectedOptionIndex = it.userAnswers[nextIndex],
                    hasAnsweredCurrent = it.userAnswers.containsKey(nextIndex)
                )
            }
        } else {
            finishQuiz(timeExpired = false)
        }
    }

    fun previousQuestion() {
        val state = _uiState.value
        val prevIndex = state.currentQuestionIndex - 1

        if (prevIndex >= 0) {
            _uiState.update {
                it.copy(
                    currentQuestionIndex = prevIndex,
                    selectedOptionIndex = it.userAnswers[prevIndex],
                    hasAnsweredCurrent = it.userAnswers.containsKey(prevIndex)
                )
            }
        }
    }

    fun restartCurrentQuiz() {
        _uiState.update {
            it.copy(
                currentQuestionIndex = 0,
                selectedOptionIndex = null,
                hasAnsweredCurrent = false,
                totalAttempts = 0,
                correctCount = 0,
                userAnswers = emptyMap(),
                isQuizFinished = false,
                isTimeExpired = false
            )
        }
        startQuizTimer()
    }

    fun finishQuiz(timeExpired: Boolean = false) {
        timerJob?.cancel()
        val state = _uiState.value
        val totalQ = state.activeQuestions.size
        val correct = state.correctQuestionsCount
        val wrong = state.wrongQuestionsCount
        val unanswered = state.unansweredQuestionsCount
        val percentage = if (totalQ > 0) (correct * 100) / totalQ else 0
        val score20 = state.scoreOutOf20
        val raw = state.rawScore

        // Calculate topic breakdown
        val topicGroups = state.activeQuestions.groupBy { it.topic }
        val topicStats = topicGroups.map { (topic, questions) ->
            val totalInTopic = questions.size
            val correctInTopic = questions.count { q ->
                val idx = state.activeQuestions.indexOf(q)
                state.userAnswers[idx] == q.correctOptionIndex
            }
            TopicStats(
                topicName = topic,
                totalQuestions = totalInTopic,
                correctCount = correctInTopic.coerceIn(0, totalInTopic)
            )
        }

        val record = QuizAttemptRecord(
            id = UUID.randomUUID().toString(),
            documentTitle = state.selectedDoc?.title ?: "اختبار مخصص",
            timestamp = System.currentTimeMillis(),
            totalQuestions = totalQ,
            correctAnswers = correct,
            wrongAnswers = wrong,
            unansweredCount = unanswered,
            totalTaps = state.totalAttempts,
            percentage = percentage,
            scoreOutOf20 = score20,
            rawScore = raw,
            positivePoints = state.positivePoints,
            penaltyDeduction = state.penaltyDeduction,
            topicStats = topicStats
        )

        _uiState.update {
            it.copy(
                isQuizFinished = true,
                isTimerRunning = false,
                isTimeExpired = timeExpired,
                attemptRecords = listOf(record) + it.attemptRecords
            )
        }
    }

    fun addSpecialization(name: String): String {
        val updated = sharedRepo.addSpecialization(name)
        val trimmed = name.trim()
        _uiState.update { it.copy(specializations = updated) }
        return trimmed
    }

    fun setCategoryFilter(category: String) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun generateQuizFromCustomText(
        title: String,
        content: String,
        category: String = "القانون الخاص",
        questionCount: Int = 4
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isGenerating = true, generationError = null) }
            try {
                val questions = GeminiQuizGenerator.generateQuizFromDocument(
                    documentTitle = title,
                    documentText = content,
                    questionCount = questionCount
                )

                val newDoc = CurriculumDoc(
                    id = "custom_${UUID.randomUUID().toString().take(6)}",
                    title = title.ifBlank { "مستند تعليمي جديد" },
                    category = category.ifBlank { "القانون الخاص" },
                    articleCountBadge = "${questions.size} أسئلة QCM",
                    summary = content.take(120) + "...",
                    fullContent = content,
                    questions = questions,
                    isCommunityShared = true,
                    uploaderName = (_uiState.value.authState as? AuthState.Authenticated)?.user?.displayName ?: "طالب مشارك"
                )

                // Save to shared repository so it is available to ALL users
                val updatedDocs = sharedRepo.saveCommunityDocument(newDoc)

                _uiState.update {
                    it.copy(
                        documents = updatedDocs,
                        selectedDoc = newDoc,
                        activeQuestions = questions,
                        currentQuestionIndex = 0,
                        selectedOptionIndex = null,
                        hasAnsweredCurrent = false,
                        totalAttempts = 0,
                        correctCount = 0,
                        userAnswers = emptyMap(),
                        isQuizFinished = false,
                        isTimeExpired = false,
                        isGenerating = false,
                        currentTab = AppTab.QUIZ
                    )
                }
                startQuizTimer()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        generationError = "حدث خطأ أثناء التوليد: ${e.message}"
                    )
                }
            }
        }
    }

    fun signIn(email: String, pass: String) {
        viewModelScope.launch {
            authService.signIn(email, pass)
        }
    }

    fun signUp(displayName: String, email: String, pass: String) {
        viewModelScope.launch {
            authService.signUp(displayName, email, pass)
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            authService.sendPasswordReset(email)
        }
    }

    fun signOut() {
        authService.signOut()
    }
}
