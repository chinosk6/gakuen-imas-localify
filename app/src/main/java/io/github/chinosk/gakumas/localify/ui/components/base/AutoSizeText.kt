package io.github.chinosk.gakumas.localify.ui.components.base

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp


@Composable
fun AutoSizeText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = TextUnit.Unspecified,
    textStyle: TextStyle? = null,
    minSize: TextUnit = 8.sp
) {
    var scaledTextStyle by remember { mutableStateOf(textStyle ?: TextStyle(color = color, fontSize = fontSize)) }
    var readyToDraw by remember { mutableStateOf(false) }

    if (LocalInspectionMode.current) {
        Text(
            text,
            modifier,
            style = scaledTextStyle
        )
        return
    }

    Text(
        text,
        modifier.drawWithContent {
            if (readyToDraw) {
                drawContent()
            }
        },
        style = scaledTextStyle,
        softWrap = false,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.didOverflowWidth) {
                val newSize = (scaledTextStyle.fontSize.value - 1.sp.value).sp
                if (minSize <= newSize) {
                    scaledTextStyle = scaledTextStyle.copy(fontSize = newSize)
                }
                else {
                    readyToDraw = true
                }
            } else {
                readyToDraw = true
            }
        }
    )
}
