package io.github.chinosk.gakumas.localify.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.ui.tooling.preview.Preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp


@Composable
fun GakuButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(50.dp), // 用于实现左右两边的半圆角
    shadowElevation: Dp = 8.dp, // 阴影的高度
    borderWidth: Dp = 1.dp, // 描边的宽度
    borderColor: Color = Color.Transparent // 描边的颜色
) {
    var buttonSize by remember { mutableStateOf(IntSize.Zero) }

    val gradient = remember(buttonSize) {
        Brush.linearGradient(
            colors = listOf(Color(0xFFFF5F19), Color(0xFFFFA028)),
            start = Offset(0f, 0f),
            end = Offset(buttonSize.width.toFloat(), buttonSize.height.toFloat()) // 动态终点
        )
    }

    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        modifier = modifier
            .onGloballyPositioned { layoutCoordinates ->
                buttonSize = layoutCoordinates.size
            }
            .shadow(elevation = shadowElevation, shape = shape)
            .clip(shape)
            .background(gradient)
            .border(borderWidth, borderColor, shape),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(text = text)
    }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun GakuButtonPreview() {
    GakuButton(modifier = Modifier.width(80.dp).height(40.dp), text = "Button", onClick = {})
}
