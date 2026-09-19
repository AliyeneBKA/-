package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.DocumentsScreen
import com.example.ui.screens.QuizScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.QcmBgLight
import com.example.ui.theme.QcmBorder
import com.example.ui.theme.QcmGreen
import com.example.ui.theme.QcmGreenDark
import com.example.ui.theme.QcmGreenLight
import com.example.ui.theme.QcmNavy
import com.example.ui.theme.QcmNavyDark
import com.example.ui.theme.QcmTextPrimary
import com.example.ui.theme.QcmTextSecondary
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Support full Arabic RTL by default
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    QcmMainApp()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QcmMainApp(viewModel: QuizViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = QcmBgLight,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = QcmGreen
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = null,
                                tint = QcmNavyDark,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "منصة الاختبارات والمناهج QCM",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = QcmNavy
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = state.currentTab == AppTab.QUIZ,
                    onClick = { viewModel.setTab(AppTab.QUIZ) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Quiz,
                            contentDescription = "الاختبار التفاعلي"
                        )
                    },
                    label = {
                        Text(
                            text = "الاختبار",
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == AppTab.QUIZ) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = QcmNavyDark,
                        selectedTextColor = QcmNavyDark,
                        indicatorColor = QcmGreen,
                        unselectedIconColor = QcmTextSecondary,
                        unselectedTextColor = QcmTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = state.currentTab == AppTab.DOCUMENTS,
                    onClick = { viewModel.setTab(AppTab.DOCUMENTS) },
                    icon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "المستندات"
                        )
                    },
                    label = {
                        Text(
                            text = "المستندات",
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == AppTab.DOCUMENTS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = QcmNavyDark,
                        selectedTextColor = QcmNavyDark,
                        indicatorColor = QcmGreen,
                        unselectedIconColor = QcmTextSecondary,
                        unselectedTextColor = QcmTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = state.currentTab == AppTab.ANALYTICS,
                    onClick = { viewModel.setTab(AppTab.ANALYTICS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "التحليلات"
                        )
                    },
                    label = {
                        Text(
                            text = "التحليلات",
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == AppTab.ANALYTICS) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = QcmNavyDark,
                        selectedTextColor = QcmNavyDark,
                        indicatorColor = QcmGreen,
                        unselectedIconColor = QcmTextSecondary,
                        unselectedTextColor = QcmTextSecondary
                    )
                )

                NavigationBarItem(
                    selected = state.currentTab == AppTab.AUTH,
                    onClick = { viewModel.setTab(AppTab.AUTH) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "الحساب والمصادقة"
                        )
                    },
                    label = {
                        Text(
                            text = "الحساب",
                            fontSize = 11.sp,
                            fontWeight = if (state.currentTab == AppTab.AUTH) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = QcmNavyDark,
                        selectedTextColor = QcmNavyDark,
                        indicatorColor = QcmGreen,
                        unselectedIconColor = QcmTextSecondary,
                        unselectedTextColor = QcmTextSecondary
                    )
                )
            }
        }
    ) { innerPadding ->
        when (state.currentTab) {
            AppTab.QUIZ -> QuizScreen(
                viewModel = viewModel,
                state = state,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.DOCUMENTS -> DocumentsScreen(
                viewModel = viewModel,
                state = state,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.ANALYTICS -> AnalyticsScreen(
                viewModel = viewModel,
                state = state,
                modifier = Modifier.padding(innerPadding)
            )
            AppTab.AUTH -> AuthScreen(
                viewModel = viewModel,
                state = state,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
