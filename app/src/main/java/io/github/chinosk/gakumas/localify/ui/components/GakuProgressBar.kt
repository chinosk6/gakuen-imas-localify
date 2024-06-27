package io.github.chinosk.gakumas.localify.ui.components


import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun GakuProgressBar(modifier: Modifier = Modifier, progress: Float, isError: Boolean = false) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progressAnime")

    Row(
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        modifier = modifier
    ) {
        if (progress <= 0f) {
            LinearProgressIndicator(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .height(8.dp),
                color = if (isError) Color(0xFFE2041B) else Color(0xFFF9C114),
            )
        }
        else {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(4.dp))
                    .height(8.dp),
                color = if (isError) Color(0xFFE2041B) else Color(0xFFF9C114),
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(if (progress > 0f) "${(progress * 100).toInt()}%" else if (isError) "Failed" else "Downloading")
    }
}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun GakuProgressBarPreview() {
    GakuProgressBar(progress = 0.25f)
}
