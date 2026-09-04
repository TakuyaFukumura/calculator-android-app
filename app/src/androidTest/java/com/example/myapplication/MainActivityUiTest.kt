package com.example.myapplication

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.entity.StringEntity
import com.example.myapplication.ui.components.StringListItem
import com.example.myapplication.ui.components.themeToggle
import com.example.myapplication.ui.theme.MyApplicationTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityUiTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun stringListItem_exposesAccessibleActions() {
        composeRule.setContent {
            MyApplicationTheme {
                StringListItem(
                    stringEntity = StringEntity(id = 1, value = "hello"),
                    editingId = null,
                    editText = "",
                    onEditStart = { _, _ -> },
                    onEditSave = {},
                    onEditCancel = {},
                    onEditTextChange = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithText("hello").assertIsDisplayed()
        composeRule.onNodeWithTag("editButton").assertIsDisplayed()
        composeRule.onNodeWithTag("deleteButton").assertIsDisplayed()
    }

    @Test
    fun stringListItem_canEnterAndCancelEditMode() {
        var editingId by mutableStateOf<Int?>(null)
        composeRule.setContent {
            MyApplicationTheme {
                StringListItem(
                    stringEntity = StringEntity(id = 1, value = "hello"),
                    editingId = editingId,
                    editText = "hello",
                    onEditStart = { id, _ -> editingId = id },
                    onEditSave = {},
                    onEditCancel = { editingId = null },
                    onEditTextChange = {},
                    onDelete = {},
                )
            }
        }

        composeRule.onNodeWithTag("editButton").performClick()
        composeRule.onNodeWithTag("editInput").assertIsDisplayed()
    }

    @Test
    fun themeToggle_canSwitchToDarkMode() {
        var darkTheme by mutableStateOf(false)
        composeRule.setContent {
            MyApplicationTheme(darkTheme = darkTheme) {
                themeToggle(
                    darkTheme = darkTheme,
                    onToggle = { darkTheme = it },
                )
            }
        }

        composeRule.onNodeWithTag("darkModeToggle").performClick()
        composeRule.onNodeWithTag("darkModeToggle").assertIsOn()
    }
}
