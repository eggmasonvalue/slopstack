package com.wisprtermandroid.dictate

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DictationActivity : ComponentActivity() {
    private lateinit var clipboard: SystemClipboard

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        clipboard = SystemClipboard(this)
        setContent {
            DictateTheme {
                DictationPopup(
                    onCopy = { command -> clipboard.execute(command) },
                    onCopyAndReturn = { command ->
                        clipboard.execute(command)
                        window.decorView.postDelayed({ finishAfterTransition() }, EXIT_CONFIRMATION_MS)
                    },
                    onDismiss = ::finishAfterTransition,
                )
            }
        }
    }

    private companion object {
        const val EXIT_CONFIRMATION_MS = 120L
    }
}

@Composable
private fun DictateTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = dictateColors(), content = content)
}

private fun dictateColors(): ColorScheme = darkColorScheme(
    primary = ComposeColor(0xFFB8A9FF),
    onPrimary = ComposeColor(0xFF211B37),
    secondary = ComposeColor(0xFFCBC2EA),
    surface = ComposeColor(0xFF121216),
    onSurface = ComposeColor(0xFFE8E1EA),
    onSurfaceVariant = ComposeColor(0xFFCAC3CF),
)

@Composable
fun DictationPopup(
    onCopy: (ClipboardCommand.Copy) -> Unit,
    onCopyAndReturn: (ClipboardCommand.Copy) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    var copied by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val command = DictationActions.copyCommandFor(text)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.Transparent)
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .testTag("dictation_popup"),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Dictate", fontWeight = FontWeight.Medium)
                    IconButton(
                        modifier = Modifier.testTag("close"),
                        onClick = onDismiss,
                    ) {
                        Text("×", fontSize = 26.sp)
                    }
                }
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = text,
                    onValueChange = {
                        text = it
                        copied = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .focusRequester(focusRequester)
                        .testTag("dictation_editor"),
                    placeholder = { Text("Speak or type") },
                    minLines = 4,
                    maxLines = 8,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = ComposeColor(0xFF1A191F),
                        unfocusedContainerColor = ComposeColor(0xFF1A191F),
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = ComposeColor.Transparent,
                    ),
                )
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (copied) {
                        Text(
                            text = "Copied",
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                    FilledTonalButton(
                        modifier = Modifier.testTag("copy"),
                        enabled = command != null,
                        onClick = {
                            command?.let(onCopy)
                            copied = true
                        },
                    ) {
                        Text("Copy")
                    }
                    Button(
                        modifier = Modifier.testTag("copy_and_return"),
                        enabled = command != null,
                        onClick = {
                            command?.let(onCopyAndReturn)
                            copied = true
                        },
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                    ) {
                        Text("Copy & return  →")
                    }
                }
            }
        }
    }
}
