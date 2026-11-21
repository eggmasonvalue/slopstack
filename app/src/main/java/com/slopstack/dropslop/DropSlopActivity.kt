package com.slopstack.dropslop

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonShapes
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

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
        labelMedium = base.labelMedium.copy(fontFamily = GoogleSansFlex),
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
    var viewportSize by remember { mutableStateOf(IntSize.Zero) }
    var popupSize by remember { mutableStateOf(IntSize.Zero) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val command = DropSlopActions.copyCommandFor(text)
    val copyInteractionSource = remember { MutableInteractionSource() }
    val copyAndReturnInteractionSource = remember { MutableInteractionSource() }

    fun clampOffsetX(raw: Float): Float {
        if (viewportSize == IntSize.Zero || popupSize == IntSize.Zero) return raw
        val maxAbsX = ((viewportSize.width - popupSize.width) / 2f).coerceAtLeast(0f)
        return raw.coerceIn(-maxAbsX, maxAbsX)
    }

    fun clampOffsetY(raw: Float): Float {
        if (viewportSize == IntSize.Zero || popupSize == IntSize.Zero) return raw
        val maxY = (viewportSize.height - popupSize.height).coerceAtLeast(0).toFloat()
        return raw.coerceIn(0f, maxY)
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }
    BackHandler(onBack = onDismiss)

    val handleWidth by animateDpAsState(
        targetValue = if (isDragging) 48.dp else 36.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "drag_handle_width",
    )
    val handleColor by animateColorAsState(
        targetValue = if (isDragging) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
        },
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "drag_handle_color",
    )

    val actionHeight = ButtonDefaults.ExtraSmallContainerHeight
    val actionContentPadding = ButtonDefaults.contentPaddingFor(actionHeight)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeColor.Transparent)
            .windowInsetsPadding(WindowInsets.systemBars)
            .imePadding()
            .padding(8.dp)
            .onSizeChanged { viewportSize = it }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Surface(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = clampOffsetX(offsetX).roundToInt(),
                        y = clampOffsetY(offsetY).roundToInt(),
                    )
                }
                .onSizeChanged { popupSize = it }
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                )
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { isDragging = true },
                        onDragEnd = { isDragging = false },
                        onDragCancel = { isDragging = false },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            offsetX = clampOffsetX(offsetX + dragAmount.x)
                            offsetY = clampOffsetY(offsetY + dragAmount.y)
                        },
                    )
                }
                .testTag("drop_slop_popup"),
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.97f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            shadowElevation = 14.dp,
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 10.dp,
                    end = 10.dp,
                    top = 6.dp,
                    bottom = 8.dp,
                ),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .testTag("drop_slop_drag_handle"),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier
                            .width(handleWidth)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(handleColor),
                    )
                }
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
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = ComposeColor.Transparent,
                    ),
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    FilledTonalIconButton(
                        modifier = Modifier
                            .size(IconButtonDefaults.extraSmallContainerSize())
                            .testTag("close"),
                        shapes = IconButtonDefaults.shapes(),
                        onClick = onDismiss,
                    ) {
                        Text("×", fontSize = 18.sp)
                    }
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        when {
                            copied -> Text(
                                text = "Copied",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                            )
                            lastCopiedText != null && text.isBlank() -> OutlinedButton(
                                modifier = Modifier
                                    .heightIn(min = actionHeight)
                                    .testTag("restore_last_copy"),
                                shapes = ButtonDefaults.shapes(),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                onClick = { text = lastCopiedText },
                            ) {
                                Text("Restore", maxLines = 1)
                            }
                        }
                    }
                    ButtonGroup(
                        overflowIndicator = { menuState ->
                            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
                        },
                        horizontalArrangement = Arrangement.spacedBy(
                            ButtonGroupDefaults.ConnectedSpaceBetween,
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        customItem(
                            buttonGroupContent = {
                                FilledTonalButton(
                                    modifier = Modifier
                                        .animateWidth(copyInteractionSource)
                                        .heightIn(min = actionHeight)
                                        .testTag("copy"),
                                    enabled = command != null,
                                    shapes = ButtonShapes(
                                        shape = ButtonGroupDefaults.connectedLeadingButtonShape,
                                        pressedShape = ButtonGroupDefaults.connectedLeadingButtonPressShape,
                                    ),
                                    contentPadding = actionContentPadding,
                                    interactionSource = copyInteractionSource,
                                    onClick = {
                                        command?.let(onCopy)
                                        copied = true
                                    },
                                ) {
                                    Text(
                                        text = "Copy",
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Visible,
                                    )
                                }
                            },
                            menuContent = { menuState ->
                                DropdownMenuItem(
                                    text = { Text("Copy") },
                                    enabled = command != null,
                                    onClick = {
                                        menuState.dismiss()
                                        command?.let(onCopy)
                                        copied = true
                                    },
                                )
                            },
                        )
                        customItem(
                            buttonGroupContent = {
                                Button(
                                    modifier = Modifier
                                        .animateWidth(copyAndReturnInteractionSource)
                                        .heightIn(min = actionHeight)
                                        .testTag("copy_and_return"),
                                    enabled = command != null,
                                    shapes = ButtonShapes(
                                        shape = ButtonGroupDefaults.connectedTrailingButtonShape,
                                        pressedShape = ButtonGroupDefaults.connectedTrailingButtonPressShape,
                                    ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
                                    ),
                                    contentPadding = actionContentPadding,
                                    interactionSource = copyAndReturnInteractionSource,
                                    onClick = {
                                        command?.let(onCopyAndReturn)
                                        copied = true
                                    },
                                ) {
                                    Text(
                                        text = "Copy & return →",
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Visible,
                                    )
                                }
                            },
                            menuContent = { menuState ->
                                DropdownMenuItem(
                                    text = { Text("Copy & return →") },
                                    enabled = command != null,
                                    onClick = {
                                        menuState.dismiss()
                                        command?.let(onCopyAndReturn)
                                        copied = true
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
