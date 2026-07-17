package com.slopstack.dropslop

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

sealed interface ClipboardCommand {
    data class Copy(val text: String) : ClipboardCommand
}

object DropSlopActions {
    fun copyCommandFor(text: String): ClipboardCommand.Copy? =
        text.takeIf { it.isNotBlank() }?.let(ClipboardCommand::Copy)
}

class SystemClipboard(context: Context) {
    private val clipboard = context.getSystemService(ClipboardManager::class.java)

    fun execute(command: ClipboardCommand.Copy) {
        clipboard.setPrimaryClip(ClipData.newPlainText(CLIP_LABEL, command.text))
    }

    private companion object {
        const val CLIP_LABEL = "Drop Slop"
    }
}
