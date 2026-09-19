package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CurriculumDoc
import com.example.ui.theme.QcmBgLight
import com.example.ui.theme.QcmBorder
import com.example.ui.theme.QcmGreen
import com.example.ui.theme.QcmGreenDark
import com.example.ui.theme.QcmGreenLight
import com.example.ui.theme.QcmNavy
import com.example.ui.theme.QcmNavyDark
import com.example.ui.theme.QcmRed
import com.example.ui.theme.QcmTextMuted
import com.example.ui.theme.QcmTextPrimary
import com.example.ui.theme.QcmTextSecondary
import com.example.ui.viewmodel.QuizUiState
import com.example.ui.viewmodel.QuizViewModel
import com.example.util.PdfExtractorUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: QuizViewModel,
    state: QuizUiState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var showCustomUploadDialog by remember { mutableStateOf(false) }
    var customTitle by remember { mutableStateOf("") }
    var customContent by remember { mutableStateOf("") }
    var questionCount by remember { mutableIntStateOf(4) }
    var isReadingPdf by remember { mutableStateOf(false) }
    var uploadedPdfName by remember { mutableStateOf<String?>(null) }
    var uploadedPdfInfo by remember { mutableStateOf<String?>(null) }

    // Specialization state for upload
    var selectedUploadCategory by remember {
        mutableStateOf(state.specializations.firstOrNull() ?: "القانون الخاص")
    }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryInput by remember { mutableStateOf("") }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            coroutineScope.launch {
                isReadingPdf = true
                showCustomUploadDialog = true
                val result = PdfExtractorUtil.extractFromUri(context, it)
                uploadedPdfName = result.fileName
                uploadedPdfInfo = "الحجم: ${result.fileSizeFormatted} • الصفحات: ${result.pageCount}"
                customTitle = result.fileName.replace(".pdf", "", ignoreCase = true)
                customContent = result.extractedText
                isReadingPdf = false
            }
        }
    }

    // Filter documents based on selected category
    val filteredDocuments = if (state.selectedCategoryFilter == "الكل") {
        state.documents
    } else {
        state.documents.filter { it.category == state.selectedCategoryFilter }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(QcmBgLight)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
        item {
            // Header Banner: Community Bank & Document Processor
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = QcmNavy)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = QcmGreen
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = QcmNavyDark,
                                    modifier = Modifier
                                        .padding(6.dp)
                                        .size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "بنك الأسئلة والمناهج المشترك",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "متاح لجميع المستخدمين حسب التخصصات الأكاديمية",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QcmNavyDark
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = null,
                                    tint = QcmGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "عام للجميع",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "تصفح المناهج المعتمدة بحسب تخصصك الأكاديمي، أو اضغط على الأيقونة العائمة 📁 أسفل الشاشة لرفع ملف PDF واستخراج الأسئلة ومشاركتها مع جميع الطلبة والمستخدمين.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }





        // Specializations Filter Bar
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تصنيف المناهج حسب التخصصات",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = QcmNavy
                    )

                    Text(
                        text = "${filteredDocuments.size} مستند متاح",
                        fontSize = 12.sp,
                        color = QcmTextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // All Chip
                    FilterChip(
                        selected = state.selectedCategoryFilter == "الكل",
                        onClick = { viewModel.setCategoryFilter("الكل") },
                        label = { Text("الكل (${state.documents.size})", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = QcmNavy,
                            selectedLabelColor = Color.White
                        )
                    )

                    // Specializations Chips
                    state.specializations.forEach { spec ->
                        val count = state.documents.count { it.category == spec }
                        FilterChip(
                            selected = state.selectedCategoryFilter == spec,
                            onClick = { viewModel.setCategoryFilter(spec) },
                            label = { Text("$spec ($count)", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QcmGreenDark,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    // Add new specialization button
                    Surface(
                        modifier = Modifier
                            .clickable { showNewCategoryDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, QcmGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = QcmGreenDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "+ إضافة تخصص",
                                fontSize = 12.sp,
                                color = QcmGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Documents List
        if (filteredDocuments.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, QcmBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = QcmTextMuted,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "لا توجد مستندات في تخصص \"${state.selectedCategoryFilter}\"",
                            color = QcmNavy,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "كن أول من يرفع ملف PDF أو ينشئ أسئلة في هذا التخصص!",
                            color = QcmTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        } else {
            items(filteredDocuments) { doc ->
                CurriculumDocumentCard(
                    doc = doc,
                    isSelected = state.selectedDoc?.id == doc.id,
                    onStartQuiz = { viewModel.selectDocument(doc) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Floating Action Button for uploading files / creating custom quiz
    ExtendedFloatingActionButton(
        onClick = { showCustomUploadDialog = true },
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(16.dp)
            .testTag("upload_fab"),
        containerColor = QcmGreenDark,
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
        icon = {
            Icon(
                imageVector = Icons.Default.CloudUpload,
                contentDescription = "رفع ملف",
                modifier = Modifier.size(24.dp)
            )
        },
        text = {
            Text(
                text = "رفع ملف PDF / أسئلة",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    )
}

    // BottomSheet for Document & PDF Upload
    if (showCustomUploadDialog) {
        ModalBottomSheet(
            onDismissRequest = { showCustomUploadDialog = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header with title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = QcmGreenLight,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = QcmGreenDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "رفع مستند أو إضافة أسئلة",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = QcmNavy
                            )
                            Text(
                                text = "متاح فوراً ومشارك لجميع المستخدمين 🌐",
                                fontSize = 11.sp,
                                color = QcmGreenDark
                            )
                        }
                    }

                    IconButton(onClick = { showCustomUploadDialog = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = QcmTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Specialization Selector for Upload
                Text(
                    text = "اختر التخصص الأكاديمي للملف المرفوع:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = QcmNavy
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    state.specializations.forEach { spec ->
                        FilterChip(
                            selected = selectedUploadCategory == spec,
                            onClick = { selectedUploadCategory = spec },
                            label = { Text(spec, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QcmNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .clickable { showNewCategoryDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        color = QcmGreenLight,
                        border = BorderStroke(1.dp, QcmGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = QcmGreenDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "تخصص جديد",
                                fontSize = 11.sp,
                                color = QcmGreenDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PDF Upload Button (Document Picker from device storage)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { pdfPickerLauncher.launch("application/pdf") },
                    shape = RoundedCornerShape(12.dp),
                    color = QcmGreenLight,
                    border = BorderStroke(1.dp, QcmGreen)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QcmGreenDark
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (uploadedPdfName != null) uploadedPdfName!! else "اختيار ورفع ملف PDF من هاتفك",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = QcmNavy
                            )
                            Text(
                                text = if (uploadedPdfInfo != null) uploadedPdfInfo!! else "استخراج نصوص المنهج وتوليد اختبار QCM مدته 30 دقيقة",
                                fontSize = 11.sp,
                                color = if (uploadedPdfName != null) QcmGreenDark else QcmTextSecondary
                            )
                        }

                        if (isReadingPdf) {
                            CircularProgressIndicator(
                                color = QcmGreenDark,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "رفع PDF",
                                tint = QcmGreenDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                if (uploadedPdfName != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = QcmGreenDark,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تم استخراج محتوى الـ PDF وجاهز للتحليل والتصنيف ضمن [$selectedUploadCategory]",
                            color = QcmGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("عنوان المادة أو المستند (مثال: القانون الإداري)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = customContent,
                    onValueChange = { customContent = it },
                    label = { Text("نص المحاضرة أو المواد القانونية/التعليمية...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp),
                    placeholder = {
                        Text(
                            "المادة 1: ...\nأو الصق مقتطفات من المنهج والمستندات التعليمية",
                            fontSize = 12.sp
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Question Count Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "عدد الأسئلة المطلوب:",
                        fontSize = 12.sp,
                        color = QcmTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    listOf(3, 4, 6).forEach { count ->
                        FilterChip(
                            selected = questionCount == count,
                            onClick = { questionCount = count },
                            label = { Text("$count") },
                            modifier = Modifier.padding(horizontal = 3.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = QcmNavy,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Sample quick fill button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "تعبئة نموذج تجريبي جاهز",
                        color = QcmGreenDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clickable {
                                customTitle = "القانون الجنائي: أركان الجريمة والمحاولة"
                                customContent = """
                                    المادة 30: كل محاولة لارتكاب جناية تبدأ بالشروع في التنفيذ لا توقف أو لا تخيب إلا لظروف مستقلة عن إرادة الفاعل، تعتبر كالجناية نفسها.
                                    المادة 39: لا جريمة ولا عقوبة بغير نص قانوني سابق وقت ارتكاب الفعل.
                                    المادة 42: الدفاع الشرعي يبيح العمل متى كان ضرورياً لدرء اعتداء حال غير مشروع على النفس أو المال.
                                """.trimIndent()
                            }
                            .padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (state.isGenerating) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            color = QcmGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "جاري تحليل المستند وتوليد أسئلة QCM للمجتمع العام...",
                            fontSize = 13.sp,
                            color = QcmNavy
                        )
                    }
                } else {
                    Button(
                        onClick = {
                            if (customContent.isNotBlank()) {
                                viewModel.generateQuizFromCustomText(
                                    title = customTitle.ifBlank { "مستند تعليمي جديد" },
                                    content = customContent,
                                    category = selectedUploadCategory,
                                    questionCount = questionCount
                                )
                                showCustomUploadDialog = false
                            }
                        },
                        enabled = customContent.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("generate_quiz_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = QcmGreenDark),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "توليد الاختبار ومشاركته لجميع المستخدمين 🌐",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }

                if (state.generationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.generationError,
                        color = QcmRed,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }

    // Dialog for Creating New Specialization
    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showNewCategoryDialog = false
                newCategoryInput = ""
            },
            title = {
                Text(
                    text = "إنشاء تخصص أكاديمي جديد 🎓",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = QcmNavy
                )
            },
            text = {
                Column {
                    Text(
                        text = "أدخل اسم التخصص الأكاديمي لإضافته وتصنيف ملفات الـ PDF والمناهج وفقه:",
                        fontSize = 13.sp,
                        color = QcmTextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = newCategoryInput,
                        onValueChange = { newCategoryInput = it },
                        label = { Text("اسم التخصص الجديد (مثال: الهندسة المدنية)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryInput.isNotBlank()) {
                            val created = viewModel.addSpecialization(newCategoryInput)
                            selectedUploadCategory = created
                            showNewCategoryDialog = false
                            newCategoryInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = QcmNavy),
                    enabled = newCategoryInput.isNotBlank()
                ) {
                    Text("إضافة التخصص")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showNewCategoryDialog = false
                    newCategoryInput = ""
                }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun CurriculumDocumentCard(
    doc: CurriculumDoc,
    isSelected: Boolean,
    onStartQuiz: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) QcmGreen else QcmBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = QcmGreenLight
                    ) {
                        Text(
                            text = doc.category,
                            color = QcmGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Public Community Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = QcmNavyDark
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = QcmGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "متاح للجميع",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = QcmBgLight
                ) {
                    Text(
                        text = doc.articleCountBadge,
                        color = QcmNavy,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = doc.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = QcmNavy
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = doc.summary,
                fontSize = 12.sp,
                color = QcmTextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = QcmTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${doc.questions.size} أسئلة • مؤقت 30د",
                        fontSize = 11.sp,
                        color = QcmTextMuted
                    )
                }

                Button(
                    onClick = onStartQuiz,
                    colors = ButtonDefaults.buttonColors(containerColor = QcmNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "بدء الاختبار (30د) ⏱️",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
