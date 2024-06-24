package io.github.chinosk.gakumas.localify.ui.pages

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chinosk.gakumas.localify.MainActivity
import io.github.chinosk.gakumas.localify.R
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import io.github.chinosk.gakumas.localify.ui.theme.GakumasLocalifyTheme


@Composable
fun MainUI(modifier: Modifier = Modifier, context: MainActivity? = null,
           previewData: GakumasConfig? = null) {
    val imagePainter = painterResource(R.drawable.bg_pattern)
    val versionInfo = remember {
        context?.getVersion() ?: listOf("", "Unknown")
    }
    // val config = getConfigState(context, previewData)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDFDFD))
    ) {
        val screenH = imageRepeater(
            painter = imagePainter,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
        )

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(10.dp, 10.dp, 10.dp, 0.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Text(text = "Gakumas Localify ${versionInfo[0]}", fontSize = 18.sp)
            Text(text = "Assets version: ${versionInfo[1]}", fontSize = 13.sp)

            SettingsTabs(modifier, listOf(stringResource(R.string.about), stringResource(R.string.home),
                stringResource(R.string.advanced_settings)),
                context = context, previewData = previewData, screenH = screenH)
        }
    }
}


@Composable
fun imageRepeater(
    painter: Painter,
    modifier: Modifier = Modifier
): Dp {
    val density = LocalDensity.current
    val imageHeightPx = painter.intrinsicSize.height
    val imageHeightDp = with(density) { imageHeightPx.toDp() }
    var retMaxH = 1080.dp
    BoxWithConstraints(modifier = modifier) {
        retMaxH = maxHeight
        val screenHeight = maxHeight
        val repeatCount = (screenHeight / imageHeightDp).toInt() + 1

        Column {
            repeat(repeatCount) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(imageHeightDp)
                )
            }
        }
    }
    return retMaxH
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, widthDp = 380)
@Composable
fun MainUIPreview(modifier: Modifier = Modifier) {
    val previewConfig = GakumasConfig()
    previewConfig.enabled = true

    GakumasLocalifyTheme {
        MainUI(previewData = previewConfig)
    }
}
