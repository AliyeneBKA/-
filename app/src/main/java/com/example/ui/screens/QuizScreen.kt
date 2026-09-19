package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Question
import com.example.data.model.QuestionReviewItem
import com.example.ui.theme.QcmBgLight
import com.example.ui.theme.QcmBorder
import com.example.ui.theme.QcmGreen
import com.example.ui.theme.QcmGreenDark
import com.example.ui.theme.QcmGreenLight
import com.example.ui.theme.QcmNavy
import com.example.ui.theme.QcmNavyDark
import com.example.ui.theme.QcmNavyLight
import com.example.ui.theme.QcmRed
import com.example.ui.theme.QcmRedLight
import com.example.ui.theme.QcmTextMuted
import com.example.ui.theme.QcmTextPrimary
import com.example.ui.theme.QcmTextSecondary
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.QuizViewModel
import java.util.Locale

@Composable
fun QuizScreen(
    viewModel: QuizViewModel,
    state: QuizUiState,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val currentQ = state.currentQuestion

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QcmBgLight)
    ) {
        if (state.activeQuestions.isEmpty()) {
            EmptyQuizView(onSelectDoc = { viewModel.setTab(com.example.ui.viewmodel.AppTab.DOCUMENTS) })
        } else if (currentQ != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Header: Active Document Title & Topic & 30-Minute Timer
                DocumentHeader(
                    title = state.selectedDoc?.title ?: "الاختبار التفاعلي",
                    currentIndex = state.currentQuestionIndex + 1,
                    totalCount = state.activeQuestions.size,
                    timeRemainingFormatted = state.formattedTimeRemaining,
                    timeRemainingSeconds = state.timeRemainingSeconds
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Real-time Metrics Bar (المؤقت ⏱️، المحاولات 🔄، النتيجة 🎯، نظام النقطة ⚖️)
                MetricsCounterBar(
                    timeRemainingFormatted = state.formattedTimeRemaining,
                    timeRemainingSeconds = state.timeRemainingSeconds,
                    totalAttempts = state.totalAttempts,
                    score = state.correctCount,
                    totalQuestions = state.activeQuestions.size
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Progress Bar
                val progress = (state.currentQuestionIndex.toFloat() + (if (state.hasAnsweredCurrent) 1f else 0.5f)) /
                        state.activeQuestions.size.toFloat().coerceAtLeast(1f)
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = QcmGreen,
                    trackColor = QcmBorder,
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Question Card with Article Reference Badge
                QuestionCard(question = currentQ)

                Spacer(modifier = Modifier.height(16.dp))

                // Multiple Choice Options List
                OptionsSection(
                    question = currentQ,
                    selectedIndex = state.selectedOptionIndex,
                    hasAnswered = state.hasAnsweredCurrent,
                    onOptionClick = { idx -> viewModel.onOptionSelected(idx) }
                )

                // Instant Feedback & Legal/Pedagogical Explanation
                AnimatedVisibility(
                    visible = state.hasAnsweredCurrent,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        FeedbackAndExplanationCard(
                            isCorrect = state.selectedOptionIndex == currentQ.correctOptionIndex,
                            question = currentQ
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons (Previous / Restart / Next)
                ActionButtonsRow(
                    hasAnswered = state.hasAnsweredCurrent,
                    canGoPrevious = state.currentQuestionIndex > 0,
                    isLast = state.currentQuestionIndex == state.activeQuestions.size - 1,
                    onPrevious = { viewModel.previousQuestion() },
                    onNext = { viewModel.nextQuestion() },
                    onRestart = { viewModel.restartCurrentQuiz() }
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Completion & Review Dialog
        if (state.isQuizFinished) {
            QuizCompletionDialog(
                state = state,
                onRestart = { viewModel.restartCurrentQuiz() },
                onGoToAnalytics = { viewModel.setTab(com.example.ui.viewmodel.AppTab.ANALYTICS) }
            )
        }
    }
}

@Composable
private fun DocumentHeader(
    title: String,
    currentIndex: Int,
    totalCount: Int,
    timeRemainingFormatted: String,
    timeRemainingSeconds: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = QcmNavy),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Countdown Timer (30 minutes)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (timeRemainingSeconds <= 300) Color(0xFFDC2626) else QcmGreenDark
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "مؤقت الاختبار",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "المؤقت: $timeRemainingFormatted",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = QcmNavyDark
                ) {
                    Text(
                        text = "السؤال $currentIndex من $totalCount",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun MetricsCounterBar(
    timeRemainingFormatted: String,
    timeRemainingSeconds: Int,
    totalAttempts: Int,
    score: Int,
    totalQuestions: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Timer Box
        MetricItem(
            modifier = Modifier.weight(1f),
            label = "الوقت ⏱️",
            value = timeRemainingFormatted,
            highlightColor = if (timeRemainingSeconds <= 300) Color(0xFFDC2626) else QcmNavy
        )

        // Attempts Counter 🔄
        MetricItem(
            modifier = Modifier.weight(1f),
            label = "المحاولات 🔄",
            value = "$totalAttempts",
            highlightColor = QcmNavy
        )

        // Current Correct Score 🎯
        MetricItem(
            modifier = Modifier.weight(1f),
            label = "الصحيحة 🎯",
            value = "$score / $totalQuestions",
            highlightColor = QcmGreenDark
        )

        // Grade System Rule
        MetricItem(
            modifier = Modifier.weight(1.1f),
            label = "التنقيط ⚖️",
            value = "+0.25 / -0.25",
            highlightColor = QcmGreenDark
        )
    }
}

@Composable
private fun MetricItem(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    highlightColor: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, QcmBorder),
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = QcmTextSecondary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = highlightColor
            )
        }
    }
}

@Composable
private fun QuestionCard(question: Question) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, QcmBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Topic & Article Reference Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = QcmGreenLight
                ) {
                    Text(
                        text = question.articleReference,
                        color = QcmGreenDark,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = QcmBgLight
                ) {
                    Text(
                        text = question.topic,
                        color = QcmTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Question Text
            Text(
                text = question.text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
private fun OptionsSection(
    question: Question,
    selectedIndex: Int?,
    hasAnswered: Boolean,
    onOptionClick: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        question.options.forEachIndexed { index, optionText ->
            val isSelected = selectedIndex == index
            val isCorrect = index == question.correctOptionIndex

            val (bgColor, borderColor, textColor) = when {
                !hasAnswered -> {
                    if (isSelected) Triple(QcmGreenLight, QcmGreen, Color.Black)
                    else Triple(Color.White, QcmBorder, Color.Black)
                }
                isSelected && isCorrect -> Triple(QcmGreenLight, QcmGreen, QcmGreenDark)
                isSelected && !isCorrect -> Triple(QcmRedLight, QcmRed, QcmRed)
                !isSelected && isCorrect -> Triple(QcmGreenLight.copy(alpha = 0.5f), QcmGreen, QcmGreenDark)
                else -> Triple(Color.White, QcmBorder, Color.Black.copy(alpha = 0.65f))
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !hasAnswered) { onOptionClick(index) }
                    .testTag("option_$index"),
                shape = RoundedCornerShape(12.dp),
                color = bgColor,
                border = BorderStroke(1.5.dp, borderColor),
                shadowElevation = if (isSelected) 2.dp else 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Option letter indicator
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(
                                if (hasAnswered && isCorrect) QcmGreen
                                else if (hasAnswered && isSelected && !isCorrect) QcmRed
                                else QcmBgLight
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = ('أ'.code + index).toChar().toString(),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (hasAnswered && (isCorrect || isSelected)) Color.White else QcmNavy
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = optionText,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = textColor,
                        lineHeight = 20.sp,
                        modifier = Modifier.weight(1f)
                    )

                    if (hasAnswered) {
                        if (isCorrect) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "إجابة صحيحة (+0.25)",
                                tint = QcmGreenDark,
                                modifier = Modifier.size(22.dp)
                            )
                        } else if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "إجابة خاطئة (-0.25)",
                                tint = QcmRed,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeedbackAndExplanationCard(
    isCorrect: Boolean,
    question: Question
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCorrect) QcmGreenLight else QcmRedLight
        ),
        border = BorderStroke(1.dp, if (isCorrect) QcmGreen else QcmRed)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                    contentDescription = null,
                    tint = if (isCorrect) QcmGreenDark else QcmRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isCorrect) "إجابة صحيحة! (+0.25 نقطة)" else "إجابة غير صحيحة (-0.25 حسم)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (isCorrect) QcmGreenDark else QcmRed
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = question.explanation,
                fontSize = 13.sp,
                color = QcmNavy,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = Color.White.copy(alpha = 0.8f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = QcmGreenDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "السند القانوني/التعليمي: ${question.articleReference}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = QcmNavy
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtonsRow(
    hasAnswered: Boolean,
    canGoPrevious: Boolean,
    isLast: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onRestart: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (canGoPrevious) {
            OutlinedButton(
                onClick = onPrevious,
                modifier = Modifier
                    .weight(0.85f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, QcmBorder)
            ) {
                Text(
                    text = "السابق ➡",
                    color = QcmNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        } else {
            OutlinedButton(
                onClick = onRestart,
                modifier = Modifier
                    .weight(0.85f)
                    .height(48.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, QcmBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    tint = QcmNavy,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "إعادة البدء",
                    color = QcmNavy,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        Button(
            onClick = onNext,
            enabled = hasAnswered,
            modifier = Modifier
                .weight(1.3f)
                .height(48.dp)
                .testTag("next_question_button"),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLast) QcmGreenDark else QcmNavy,
                disabledContainerColor = Color(0xFFE2E8F0),
                disabledContentColor = Color(0xFF64748B)
            )
        ) {
            Text(
                text = if (isLast) "إنهاء وحساب العلامة من 20 🏆" else "السؤال التالي ⬅",
                color = if (hasAnswered) Color.White else Color(0xFF64748B),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun EmptyQuizView(onSelectDoc: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = QcmGreenLight,
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                    contentDescription = null,
                    tint = QcmGreenDark,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "لا يوجد منهاج نشط حالياً",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = QcmNavy
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "اختر منهاجاً تعليمياً أو ارفع ملف PDF من بنك الأسئلة والمناهج للبدء بالاختبار الفوري لمدة 30 دقيقة.",
            fontSize = 13.sp,
            color = QcmTextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSelectDoc,
            colors = ButtonDefaults.buttonColors(containerColor = QcmNavy),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("استعراض المناهج والمستندات")
        }
    }
}

@Composable
private fun QuizCompletionDialog(
    state: QuizUiState,
    onRestart: () -> Unit,
    onGoToAnalytics: () -> Unit
) {
    var selectedReviewFilter by remember {
        // If there are wrong answers, default to showing wrong answers for immediate correction!
        mutableStateOf(if (state.wrongQuestionsCount > 0) "WRONG_ONLY" else "ALL")
    }

    val displayedReviews = when (selectedReviewFilter) {
        "WRONG_ONLY" -> state.wrongQuestionsReviews
        "CORRECT_ONLY" -> state.questionReviews.filter { it.userSelectedOption != null && it.isCorrect }
        else -> state.questionReviews
    }

    AlertDialog(
        onDismissRequest = { /* Require action */ },
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (state.isTimeExpired) Icons.Default.HourglassBottom else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (state.isTimeExpired) Color(0xFFDC2626) else QcmGreenDark,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (state.isTimeExpired) "انتهى وقت الاختبار (30 دقيقة) ⏰" else "اكتمل الاختبار بنجاح! 🏆",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = QcmNavy,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Main Score Banner (/20)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = QcmNavy)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "العلامة النهائية المحتسبة",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Large Score out of 20
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = state.formattedScoreOutOf20,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (state.scoreOutOf20 >= 10.0) QcmGreen else Color(0xFFF87171)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "/ 20",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (state.scoreOutOf20 >= 16.0) "تقدير: ممتاز جداً 🌟"
                            else if (state.scoreOutOf20 >= 14.0) "تقدير: جيد جداً 👏"
                            else if (state.scoreOutOf20 >= 10.0) "تقدير: مقبول (فوق المعدل) 👍"
                            else "دون المعدل - يوصى بمراجعة التصحيحات أدناه ⚠️",
                            fontSize = 12.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Points Breakdown Details Card (+0.25 / -0.25 Rule)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, QcmBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "تفصيل احتساب النقاط (نظام حسم الخطأ):",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = QcmNavy
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "✔️ إجابات صحيحة (${state.correctQuestionsCount} × +0.25):",
                                fontSize = 11.sp,
                                color = QcmGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+${String.format(Locale.US, "%.2f", state.positivePoints)} نقطة",
                                fontSize = 11.sp,
                                color = QcmGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "❌ إجابات خاطئة (${state.wrongQuestionsCount} × -0.25):",
                                fontSize = 11.sp,
                                color = QcmRed,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "-${String.format(Locale.US, "%.2f", state.penaltyDeduction)} حسم",
                                fontSize = 11.sp,
                                color = QcmRed,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (state.unansweredQuestionsCount > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "⚠️ أسئلة دون إجابة:",
                                    fontSize = 11.sp,
                                    color = QcmTextSecondary
                                )
                                Text(
                                    text = "${state.unansweredQuestionsCount} (0.00 نقطة)",
                                    fontSize = 11.sp,
                                    color = QcmTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📊 نسبة الاستيعاب:",
                                fontSize = 11.sp,
                                color = QcmNavy,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.comprehensionPercentage}%",
                                fontSize = 11.sp,
                                color = QcmNavy,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Review Section Header & Filter Tabs
                Text(
                    text = "عرض وتصحيح الأسئلة والإجابات 📝",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = QcmNavy
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedReviewFilter == "WRONG_ONLY",
                        onClick = { selectedReviewFilter = "WRONG_ONLY" },
                        label = {
                            Text(
                                text = "الخاطئة (${state.wrongQuestionsCount}) ❌",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QcmRed,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedReviewFilter == "ALL",
                        onClick = { selectedReviewFilter = "ALL" },
                        label = {
                            Text(
                                text = "الكل (${state.activeQuestions.size}) 📋",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QcmNavy,
                            selectedLabelColor = Color.White
                        )
                    )

                    FilterChip(
                        selected = selectedReviewFilter == "CORRECT_ONLY",
                        onClick = { selectedReviewFilter = "CORRECT_ONLY" },
                        label = {
                            Text(
                                text = "الصحيحة (${state.correctQuestionsCount}) ✔️",
                                fontSize = 11.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QcmGreenDark,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (displayedReviews.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = QcmGreenLight
                    ) {
                        Text(
                            text = if (selectedReviewFilter == "WRONG_ONLY") "ممتاز جداً! لا توجد أي إجابات خاطئة في هذا الاختبار 🎉"
                            else "لا توجد عناصر لعرضها بهذا التصنيف.",
                            modifier = Modifier.padding(12.dp),
                            color = QcmGreenDark,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    displayedReviews.forEach { reviewItem ->
                        QuestionCorrectionCard(item = reviewItem)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGoToAnalytics,
                colors = ButtonDefaults.buttonColors(containerColor = QcmNavy),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("لوحة الإحصائيات 📊", fontSize = 12.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onRestart,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("إعادة الاختبار 🔄", fontSize = 12.sp)
            }
        }
    )
}

@Composable
private fun QuestionCorrectionCard(item: QuestionReviewItem) {
    val q = item.question
    val userIdx = item.userSelectedOption
    val isCorrect = item.isCorrect
    val hasAnswered = userIdx != null

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!hasAnswered) Color(0xFFFFFBEB)
            else if (isCorrect) QcmGreenLight.copy(alpha = 0.4f)
            else QcmRedLight.copy(alpha = 0.5f)
        ),
        border = BorderStroke(
            1.dp,
            if (!hasAnswered) Color(0xFFF59E0B)
            else if (isCorrect) QcmGreen
            else QcmRed
        )
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "س ${item.questionIndex + 1}: ${q.articleReference}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = QcmNavy
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = if (!hasAnswered) Color(0xFFFEF3C7)
                    else if (isCorrect) QcmGreen
                    else QcmRed
                ) {
                    Text(
                        text = if (!hasAnswered) "لم تتم الإجابة"
                        else if (isCorrect) "+0.25 صح"
                        else "-0.25 خطأ",
                        color = if (!hasAnswered) Color(0xFFB45309) else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = q.text,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Show User's Selection if wrong
            if (hasAnswered && !isCorrect) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = QcmRedLight,
                    border = BorderStroke(1.dp, QcmRed.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = QcmRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "إجابتك: ${q.options.getOrNull(userIdx) ?: ""}",
                            fontSize = 11.sp,
                            color = QcmRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Always show the Correct Model Answer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                color = QcmGreenLight,
                border = BorderStroke(1.dp, QcmGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = QcmGreenDark,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "التصحيح المعتمد: ${q.options[q.correctOptionIndex]}",
                        fontSize = 11.sp,
                        color = QcmGreenDark,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Explanation / Reference
            Text(
                text = "💡 السند: ${q.explanation}",
                fontSize = 11.sp,
                color = Color.Black.copy(alpha = 0.8f),
                lineHeight = 15.sp
            )
        }
    }
}
