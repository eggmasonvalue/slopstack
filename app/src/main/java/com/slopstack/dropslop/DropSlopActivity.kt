package com.slopstack.dropslop

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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class DropSlopActivity : ComponentActivity() {
    private lateinit var clipboard: SystemClipboard
    private lateinit var lastCopiedTextStore: LastCopiedTextStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        clipboard = SystemClipboard(this)
        lastCopiedTextStore = LastCopiedTextStore(this)
        setContent {
            DropSlopTheme {
                DropSlopPopup(
                    lastCopiedText = lastCopiedTextStore.read(),
                    onCopy = ::copy,
                    onCopyAndReturn = { command ->
                        copy(command)
                        window.decorView.postDelayed({ finishAfterTransition() }, EXIT_CONFIRMATION_MS)
                    },
                    onDismiss = ::finishAfterTransition,
                )
            }
        }
    }

    private fun copy(command: ClipboardCommand.Copy) {
        clipboard.execute(command)
        lastCopiedTextStore.save(command.text)
    }

    private companion object {
        const val EXIT_CONFIRMATION_MS = 120L
    }
}

private val GoogleSansFlex = FontFamily(Font(R.font.google_sans_flex))

private val DropSlopTypography = Typography().let { base ->
    base.copy(
        titleMedium = base.titleMedium.copy(fontFamily = GoogleSansFlex),
        bodyLarge = base.bodyLarge.copy(fontFamily = GoogleSansFlex),
        bodyMedium = base.bodyMedium.copy(fontFamily = GoogleSansFlex),
        labelLarge = base.labelLarge.copy(fontFamily = GoogleSansFlex),
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DropSlopTheme(content: @Composable () -> Unit) {
    // minSdk = 37, so wallpaper-derived dynamic color (Android 12+) is always available;
    // no pre-S fallback branch is needed.
    val colorScheme = dynamicDarkColorScheme(LocalContext.current)
    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = DropSlopTypography,
        motionScheme = MotionScheme.expressive(),
        content = content,
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DropSlopPopup(
    lastCopiedText: String? = null,
    onCopy: (ClipboardCommand.Copy) -> Unit,
    onCopyAndReturn: (ClipboardCommand.Copy) -> Unit,
    onDismiss: () -> Unit,
) {
    var text by rememberSaveable { mutableStateOf("") }
    var copied by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val command = DropSlopActions.copyCommandFor(text)

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    BackHandler(onBack = onDismiss)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.Transparent)
            .imePadding()
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(horizontal = 20.dp)
            .padding(top = 72.dp, bottom = 16.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.TopCenter,
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
                .testTag("drop_slop_popup"),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.97f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 8.dp,
            shadowElevation = 16.dp,
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Drop Slop", fontWeight = FontWeight.Medium)
                    IconButton(
                        modifier = Modifier.testTag("close"),
                        shapes = IconButtonDefaults.shapes(),
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
                        .testTag("drop_slop_editor"),
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
                    when {
                        copied -> Text(
                            text = "Copied",
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                        )
                        lastCopiedText != null && text.isBlank() -> TextButton(
                            modifier = Modifier
                                .weight(1f)
                                .testTag("restore_last_copy"),
                            shapes = ButtonDefaults.shapes(),
                            contentPadding = PaddingValues(0.dp),
                            onClick = { text = lastCopiedText },
                        ) {
                            Text("Restore", maxLines = 1)
                        }
                        else -> Spacer(Modifier.weight(1f))
                    }
                    FilledTonalButton(
                        modifier = Modifier.testTag("copy"),
                        enabled = command != null,
                        shapes = ButtonDefaults.shapes(),
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
                        shapes = ButtonDefaults.shapes(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        onClick = {
                            command?.let(onCopyAndReturn)
                            copied = true
                        },
                    ) {
                        Text("Copy & return  →")
                    }
                }
            }
        }
    }
}
