package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.SoundFeedback
import com.example.ui.theme.LocalCalculatorKeyColors

enum class KeyType {
    NUMBER,
    OPERATOR,
    FUNCTION,
    EQUALS,
    CLEAR
}

@Composable
fun CalcKey(
    label: String,
    type: KeyType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    soundFeedback: SoundFeedback? = null,
    hapticEnabled: Boolean = true,
    soundEnabled: Boolean = false,
    fontSize: TextUnit = 24.sp,
    subLabel: String? = null,
    cornerRadius: Dp = 20.dp,
    testTag: String = "key_$label"
) {
    val keyColors = LocalCalculatorKeyColors.current
    val view = LocalView.current

    val (bgColor, textColor) = when (type) {
        KeyType.NUMBER -> Pair(keyColors.numKeyBg, keyColors.numText)
        KeyType.OPERATOR -> Pair(keyColors.opKeyBg, keyColors.opText)
        KeyType.FUNCTION -> Pair(keyColors.fnKeyBg, keyColors.fnText)
        KeyType.EQUALS -> Pair(keyColors.equalsBg, keyColors.equalsText)
        KeyType.CLEAR -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.onErrorContainer)
    }

    Surface(
        shape = RoundedCornerShape(cornerRadius),
        color = bgColor,
        tonalElevation = 2.dp,
        shadowElevation = 1.dp,
        modifier = modifier
            .padding(4.dp)
            .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true, color = textColor.copy(alpha = 0.3f))
                ) {
                    soundFeedback?.triggerFeedback(view, hapticEnabled, soundEnabled)
                    onClick()
                },
            contentAlignment = Alignment.Center
        ) {
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    fontSize = 10.sp,
                    color = textColor.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 4.dp, end = 8.dp)
                )
            }
            Text(
                text = label,
                fontSize = fontSize,
                fontWeight = if (type == KeyType.OPERATOR || type == KeyType.EQUALS) FontWeight.Bold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
