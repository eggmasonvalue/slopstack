package com.wisprtermandroid.dictate

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DictationPopupTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editor_is_focused_when_popup_opens() {
        setPopup()

        composeRule.onNodeWithTag("dictation_editor").assertIsFocused()
    }

    @Test
    fun copy_writes_text_and_keeps_popup_open() {
        var copied: ClipboardCommand.Copy? = null
        setPopup(onCopy = { copied = it })

        composeRule.onNodeWithTag("dictation_editor").performTextInput("One line")
        composeRule.onNodeWithTag("copy").performClick()

        assertEquals(ClipboardCommand.Copy("One line"), copied)
        composeRule.onNodeWithTag("dictation_popup").assertExists()
    }

    @Test
    fun copy_and_return_writes_text() {
        var copied: ClipboardCommand.Copy? = null
        setPopup(onCopyAndReturn = { copied = it })

        composeRule.onNodeWithTag("dictation_editor").performTextInput("Return this")
        composeRule.onNodeWithTag("copy_and_return").performClick()

        assertEquals(ClipboardCommand.Copy("Return this"), copied)
    }

    @Test
    fun close_discards_without_copying() {
        var dismissed = false
        setPopup(onDismiss = { dismissed = true })

        composeRule.onNodeWithTag("dictation_editor").performTextInput("Discard me")
        composeRule.onNodeWithTag("close").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun text_survives_saved_state_restoration() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { DictationPopup({}, {}, {}) }

        composeRule.onNodeWithTag("dictation_editor").performTextInput("Keep me")
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithTag("dictation_editor").assertTextContains("Keep me")
    }

    private fun setPopup(
        onCopy: (ClipboardCommand.Copy) -> Unit = {},
        onCopyAndReturn: (ClipboardCommand.Copy) -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        composeRule.setContent {
            DictationPopup(
                onCopy = onCopy,
                onCopyAndReturn = onCopyAndReturn,
                onDismiss = onDismiss,
            )
        }
    }
}
