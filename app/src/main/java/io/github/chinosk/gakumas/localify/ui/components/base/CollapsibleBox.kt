package io.github.chinosk.gakumas.localify.ui.components.base

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chinosk.gakumas.localify.models.CollapsibleBoxViewModel

@Composable
fun CollapsibleBox(
    modifier: Modifier = Modifier,
    collapsedHeight: Dp = 28.dp,
    viewModel: CollapsibleBoxViewModel = viewModel(),
    showExpand: Boolean = true,
    expandState: Boolean? = null,
    innerPaddingTopBottom: Dp = 0.dp,
    innerPaddingLeftRight: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val expanded by viewModel::expanded

    // var offsetY by remember { mutableFloatStateOf(0f) }

    val animatedHeight by animateDpAsState(
        targetValue = if (expandState ?: expanded) Dp.Unspecified else collapsedHeight,
        label = "CollapsibleBox$collapsedHeight"
    )

    Box(
        modifier = modifier
            .animateContentSize()/*
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        offsetY += dragAmount
                        if (expanded && offsetY > 0) {
                            viewModel.expanded = false
                        } else if (!expanded && offsetY < 0) {
                            viewModel.expanded = true
                        }
                    },
                    onDragEnd = {
                        offsetY = 0f
                    }
                )
            }*/
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .height(animatedHeight)
                .fillMaxWidth()
                .padding(start = innerPaddingLeftRight, end = innerPaddingLeftRight,
                    top = innerPaddingTopBottom, bottom = innerPaddingTopBottom)
                // .fillMaxSize()
                .clickable {
                    if (!expanded && showExpand) {
                        viewModel.expanded = expandState ?: true
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //item {
                if (expandState ?: expanded) {
                    content()
                }
                else if (showExpand) {
                    Text(text = "Details ↓", color = Color.Gray)
                }
            //}

        }
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun CollapsibleBoxPreview() {
    CollapsibleBox(showExpand = true) {}
}
