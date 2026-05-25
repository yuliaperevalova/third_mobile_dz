package com.example.third_dz.navigation

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.third_dz.MainActivity
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavGraphIntegrationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun app_startsAndShowsContent() {
        // Ждём загрузки приложения (до 15 секунд)
        composeRule.waitUntil(15_000) { true }
    }

    @Test
    fun navigateToFavouritesAndBack() {
        // Ждём загрузки приложения
        composeRule.waitUntil(15_000) { true }

        // Кликаем по кнопке "Favourites" (по contentDescription)
        composeRule.onNodeWithContentDescription("Favourites").performClick()

        // Ждём перехода
        composeRule.waitUntil(3_000) { true }

        // Проверяем, что мы на экране Favourites
        composeRule.onNodeWithText("Favourites").assertExists()
        
        // Кликаем по кнопке "Back"
        composeRule.onNodeWithContentDescription("Back").performClick()
        
        // Ждём возврата
        composeRule.waitUntil(3_000) { true }
    }

    @Test
    fun navigateToCollectionsScreen() {
        // Ждём загрузки приложения
        composeRule.waitUntil(15_000) { true }

        // Кликаем по кнопке "Collections" (по contentDescription)
        composeRule.onNodeWithContentDescription("Collections").performClick()

        // Ждём перехода
        composeRule.waitUntil(3_000) { true }

        // Проверяем, что мы на экране Collections
        composeRule.onNodeWithText("Collections").assertExists()
    }
}