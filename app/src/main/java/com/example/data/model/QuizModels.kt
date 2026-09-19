package com.example.data.model

data class Question(
    val id: String,
    val text: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String,
    val articleReference: String,
    val topic: String
)

data class TopicStats(
    val topicName: String,
    val totalQuestions: Int,
    val correctCount: Int
) {
    val percentage: Int
        get() = if (totalQuestions > 0) (correctCount * 100) / totalQuestions else 0

    val isStrength: Boolean
        get() = percentage >= 70
}

data class QuizAttemptRecord(
    val id: String,
    val documentTitle: String,
    val timestamp: Long,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val totalTaps: Int,
    val percentage: Int,
    val topicStats: List<TopicStats>,
    val wrongAnswers: Int = 0,
    val unansweredCount: Int = 0,
    val scoreOutOf20: Double = 0.0,
    val rawScore: Double = 0.0,
    val positivePoints: Double = 0.0,
    val penaltyDeduction: Double = 0.0
)

data class CurriculumDoc(
    val id: String,
    val title: String,
    val category: String,
    val articleCountBadge: String,
    val summary: String,
    val fullContent: String,
    val questions: List<Question>,
    val isCommunityShared: Boolean = true,
    val uploaderName: String = "مجتمع الطلاب"
)

data class QuestionReviewItem(
    val questionIndex: Int,
    val question: Question,
    val userSelectedOption: Int?,
    val isCorrect: Boolean,
    val pointDelta: Double
)

