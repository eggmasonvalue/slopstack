package com.slopstack.dropslop

import androidx.activity.ComponentActivity
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

class DropSlopPopupTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editor_is_focused_when_popup_opens() {
        setPopup()

        composeRule.onNodeWithTag("drop_slop_editor").assertIsFocused()
    }

    @Test
    fun copy_writes_text_and_keeps_popup_open() {
        var copied: ClipboardCommand.Copy? = null
        setPopup(onCopy = { copied = it })

        composeRule.onNodeWithTag("drop_slop_editor").performTextInput("One line")
        composeRule.onNodeWithTag("copy").performClick()

        assertEquals(ClipboardCommand.Copy("One line"), copied)
        composeRule.onNodeWithTag("drop_slop_popup").assertExists()
    }

    @Test
    fun copy_and_return_writes_text() {
        var copied: ClipboardCommand.Copy? = null
        setPopup(onCopyAndReturn = { copied = it })

        composeRule.onNodeWithTag("drop_slop_editor").performTextInput("Return this")
        composeRule.onNodeWithTag("copy_and_return").performClick()

        assertEquals(ClipboardCommand.Copy("Return this"), copied)
    }

    @Test
    fun close_discards_without_copying() {
        var dismissed = false
        setPopup(onDismiss = { dismissed = true })

        composeRule.onNodeWithTag("drop_slop_editor").performTextInput("Discard me")
        composeRule.onNodeWithTag("close").performClick()

        assertTrue(dismissed)
    }

    @Test
    fun restore_last_copy_loads_text_without_copying() {
        var copied: ClipboardCommand.Copy? = null
        setPopup(lastCopiedText = "Saved drop", onCopy = { copied = it })

        composeRule.onNodeWithTag("restore_last_copy").performClick()

        composeRule.onNodeWithTag("drop_slop_editor").assertTextContains("Saved drop")
        composeRule.onNodeWithTag("restore_last_copy").assertDoesNotExist()
        assertEquals(null, copied)
    }

    @Test
    fun restore_last_copy_is_hidden_after_typing() {
        setPopup(lastCopiedText = "Saved drop")

        composeRule.onNodeWithTag("drop_slop_editor").performTextInput("New drop")

        composeRule.onNodeWithTag("restore_last_copy").assertDoesNotExist()
    }

    @Test
    fun restore_last_copy_is_hidden_without_a_saved_drop() {
        setPopup()

        composeRule.onNodeWithTag("restore_last_copy").assertDoesNotExist()
    }

    @Test
    fun text_survives_saved_state_restoration() {
        val restorationTester = StateRestorationTester(composeRule)
        restorationTester.setContent { DropSlopPopup(onCopy = {}, onCopyAndReturn = {}, onDismiss = {}) }

        composeRule.onNodeWithTag("drop_slop_editor").performTextInput("Keep me")
        restorationTester.emulateSavedInstanceStateRestore()

        composeRule.onNodeWithTag("drop_slop_editor").assertTextContains("Keep me")
    }

    private fun setPopup(
        lastCopiedText: String? = null,
        onCopy: (ClipboardCommand.Copy) -> Unit = {},
        onCopyAndReturn: (ClipboardCommand.Copy) -> Unit = {},
        onDismiss: () -> Unit = {},
    ) {
        composeRule.setContent {
            DropSlopPopup(
                lastCopiedText = lastCopiedText,
                onCopy = onCopy,
                onCopyAndReturn = onCopyAndReturn,
                onDismiss = onDismiss,
            )
        }
    }
}
