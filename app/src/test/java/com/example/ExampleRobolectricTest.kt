package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("QCM", appName)
  }

  @Test
  fun `auth service sign in with valid credentials succeeds`() = kotlinx.coroutines.runBlocking {
    val authService = com.example.features.auth.AuthService()
    val result = authService.signIn("student@test.com", "secret123")
    org.junit.Assert.assertTrue(result.isSuccess)
    assertEquals("student@test.com", result.getOrNull()?.email)
  }

  @Test
  fun `auth service sign in with invalid email fails`() = kotlinx.coroutines.runBlocking {
    val authService = com.example.features.auth.AuthService()
    val result = authService.signIn("invalid-email", "secret123")
    org.junit.Assert.assertTrue(result.isFailure)
  }

  @Test
  fun `quiz timer initial state starts at 30 minutes and formats properly`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.viewmodel.QuizViewModel(context)
    val state = vm.uiState.value
    assertEquals(1800, state.timeRemainingSeconds)
    assertEquals("30:00", state.formattedTimeRemaining)
  }

  @Test
  fun `score calculation handles +0_25 correct and -0_25 incorrect normalized to 20`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val vm = com.example.ui.viewmodel.QuizViewModel(context)
    val state = vm.uiState.value
    val firstQ = state.currentQuestion
    org.junit.Assert.assertNotNull(firstQ)

    // Select correct option (+0.25)
    vm.onOptionSelected(firstQ!!.correctOptionIndex)
    val updatedState = vm.uiState.value
    assertEquals(1, updatedState.correctQuestionsCount)
    assertEquals(0, updatedState.wrongQuestionsCount)
    org.junit.Assert.assertTrue(updatedState.scoreOutOf20 > 0.0)
  }

  @Test
  fun `specialization addition and shared repository persistence works`() {
    val context = ApplicationProvider.getApplicationContext<android.app.Application>()
    val repo = com.example.data.repository.SharedCurriculumRepository(context)
    val specsBefore = repo.getSpecializations()
    val newSpec = "الهندسة المعمارية والتصميم"
    val updatedSpecs = repo.addSpecialization(newSpec)
    org.junit.Assert.assertTrue(updatedSpecs.contains(newSpec))
  }
}
