package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuizAttemptRecord
import com.example.data.model.TopicStats
import com.example.ui.theme.QcmBgLight
import com.example.ui.theme.QcmBorder
import com.example.ui.theme.QcmGreen
import com.example.ui.theme.QcmGreenDark
import com.example.ui.theme.QcmGreenLight
import com.example.ui.theme.QcmNavy
import com.example.ui.theme.QcmNavyDark
import com.example.ui.theme.QcmRed
import com.example.ui.theme.QcmRedLight
import com.example.ui.theme.QcmTextMuted
import com.example.ui.theme.QcmTextPrimary
import com.example.ui.theme.QcmTextSecondary
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.QuizViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AnalyticsScreen(
    viewModel: QuizViewModel,
    state: QuizUiState,
    modifier: Modifier = Modifier
) {
    val totalAttempts = state.attemptRecords.size
    val averageScore = if (totalAttempts > 0) {
        state.attemptRecords.sumOf { it.percentage } / totalAttempts
    } else 0

    // Collect all topics
    val allTopicStats = state.attemptRecords.flatMap { it.topicStats }
    val groupedTopics = allTopicStats.groupBy { it.topicName }.map { (name, statsList) ->
        val totalQ = statsList.sumOf { it.totalQuestions }
        val correctQ = statsList.sumOf { it.correctCount }
        TopicStats(name, totalQ, correctQ)
    }

    val strengths = groupedTopics.filter { it.isStrength }
    val weaknesses = groupedTopics.filter { !it.isStrength }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(QcmBgLight)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            // Analytics Header Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = QcmNavy)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "لوحة تحليل الأداء والاستيعاب",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تتبع نقاط القوة والضعف حسب المواد والأبواب",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = QcmGreen
                        ) {
                            Text(
                                text = "$averageScore%",
                                color = QcmNavyDark,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        AnalyticsStatChip(
                            modifier = Modifier.weight(1f),
                            label = "إجمالي الاختبارات",
                            value = "$totalAttempts"
                        )
                        AnalyticsStatChip(
                            modifier = Modifier.weight(1f),
                            label = "متوسط الدرجة",
                            value = "$averageScore%"
                        )
                        AnalyticsStatChip(
                            modifier = Modifier.weight(1f),
                            label = "حالة الاستيعاب",
                            value = if (averageScore >= 75) "متقدم 🌟" else "متوسط 📈"
                        )
                    }
                }
            }
        }

        // Section: Strengths (النقاط القوية)
        item {
            Text(
                text = "النقاط القوية والمواد المتقنة 🎯",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = QcmGreenDark,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        if (strengths.isEmpty()) {
            item {
                Text(
                    text = "أكمل مزيداً من الاختبارات لإظهار المواد التي حققت فيها أكثر من 70% استيعاب.",
                    fontSize = 12.sp,
                    color = QcmTextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else {
            items(strengths) { stat ->
                TopicMasteryCard(stat = stat, isPositive = true)
            }
        }

        // Section: Weaknesses / Review Needed (النقاط التي تحتاج مراجعة)
        item {
            Text(
                text = "مواد تحتاج إلى مراجعة وتركيز 💡",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = QcmNavy,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (weaknesses.isEmpty()) {
            item {
                Text(
                    text = "رائع! لا توجد مواد ضعيفة حالياً. استمر في الحفاظ على هذا المستوى.",
                    fontSize = 12.sp,
                    color = QcmTextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        } else {
            items(weaknesses) { stat ->
                TopicMasteryCard(stat = stat, isPositive = false)
            }
        }

        // Section: Attempt History
        item {
            Text(
                text = "سجل المحاولات السابقة للطلاب 🕒",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = QcmNavy,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        if (state.attemptRecords.isEmpty()) {
            item {
                Text(
                    text = "لم تسجل أي محاولة حتى الآن.",
                    fontSize = 12.sp,
                    color = QcmTextSecondary
                )
            }
        } else {
            items(state.attemptRecords) { record ->
                AttemptHistoryCard(record = record)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AnalyticsStatChip(
    modifier: Modifier = Modifier,
    label: String,
    value: String
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = Color.White.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun TopicMasteryCard(
    stat: TopicStats,
    isPositive: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, if (isPositive) QcmGreen.copy(alpha = 0.4f) else Color(0xFFFDBA74).copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(if (isPositive) QcmGreenLight else Color(0xFFFFF7ED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.ThumbUp else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (isPositive) QcmGreenDark else Color(0xFFEA580C),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = stat.topicName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = QcmNavy
                    )
                }

                Text(
                    text = "${stat.percentage}%",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isPositive) QcmGreenDark else Color(0xFFEA580C)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (stat.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isPositive) QcmGreen else Color(0xFFF97316),
                trackColor = QcmBorder
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${stat.correctCount} إجابات صحيحة من ${stat.totalQuestions}",
                    fontSize = 11.sp,
                    color = QcmTextSecondary
                )
                Text(
                    text = if (isPositive) "إتقان ممتاز" else "يوصى بإعادة مراجعة نصوص المواد",
                    fontSize = 11.sp,
                    color = if (isPositive) QcmGreenDark else Color(0xFFEA580C),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AttemptHistoryCard(record: QuizAttemptRecord) {
    val dateStr = SimpleDateFormat("yyyy/MM/dd - HH:mm", Locale.getDefault()).format(Date(record.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, QcmBorder)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (record.percentage >= 70) QcmGreenLight else Color(0xFFFFF7ED)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = String.format(Locale.US, "%.1f", record.scoreOutOf20),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (record.percentage >= 70) QcmGreenDark else Color(0xFFEA580C)
                    )
                    Text(
                        text = "/20",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = QcmNavy
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.documentTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = QcmNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$dateStr | ${record.correctAnswers} صح (+0.25) • ${record.wrongAnswers} خطأ (-0.25)",
                        fontSize = 10.sp,
                        color = QcmTextMuted
                    )
                }
            }
        }
    }
}
