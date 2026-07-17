package com.wisprtermandroid.dictate

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DictationActionsTest {
    @Test
    fun `blank text does not produce a clipboard command`() {
        assertNull(DictationActions.copyCommandFor(" \n\t "))
    }

    @Test
    fun `nonblank text produces a plain text clipboard command`() {
        assertEquals(
            ClipboardCommand.Copy("dictated text"),
            DictationActions.copyCommandFor("dictated text"),
        )
    }
}
