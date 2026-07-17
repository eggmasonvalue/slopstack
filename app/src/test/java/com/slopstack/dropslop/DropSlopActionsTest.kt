package com.slopstack.dropslop

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DropSlopActionsTest {
    @Test
    fun `blank text does not produce a clipboard command`() {
        assertNull(DropSlopActions.copyCommandFor(" \n\t "))
    }

    @Test
    fun `nonblank text produces a plain text clipboard command`() {
        assertEquals(
            ClipboardCommand.Copy("dictated text"),
            DropSlopActions.copyCommandFor("dictated text"),
        )
    }
}
