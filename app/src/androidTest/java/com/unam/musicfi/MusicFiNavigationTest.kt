package com.unam.musicfi

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MusicFiNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun welcomeScreenShowsBrandAndNavigationActions() {
        composeRule.onNodeWithText("musicFI").assertIsDisplayed()
        composeRule.onNodeWithText("Empezar").assertIsDisplayed()
        composeRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
    }

    @Test
    fun startButtonOpensMusicLibrary() {
        composeRule.onNodeWithText("Empezar").performClick()
        composeRule.onNodeWithText("Tu música, en un solo lugar").assertIsDisplayed()
    }

    @Test
    fun loginScreenIsReachable() {
        composeRule.onNodeWithText("Iniciar sesión").performClick()
        composeRule.onNodeWithText("Nombre de usuario").assertIsDisplayed()
        composeRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }

    @Test
    fun loginRejectsEmptyFields() {
        composeRule.onNodeWithText("Iniciar sesión").performClick()
        composeRule.onNodeWithText("Entrar").performClick()
        composeRule.onNodeWithText("Completa ambos campos para continuar.").assertIsDisplayed()
    }
}
