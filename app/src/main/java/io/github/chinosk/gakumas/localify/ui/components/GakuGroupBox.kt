import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chinosk.gakumas.localify.R

@Composable
fun GakuGroupBox(
    modifier: Modifier = Modifier,
    title: String = "Title",
    maxWidth: Dp = 500.dp,
    contentPadding: Dp = 8.dp,
    rightHead: @Composable (() -> Unit)? = null,
    onHeadClick: () -> Unit = {},
    content: @Composable () -> Unit
) {

    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(
                bottomStart = 16.dp,
                bottomEnd = 8.dp,
                topEnd = 16.dp,
                topStart = 0.dp
            ))
            // .background(Color.White, RoundedCornerShape(8.dp))
    ) {
        Column(modifier = modifier.widthIn(max = maxWidth)) {
            // Header
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .height(23.dp)
                    .clickable {
                        onHeadClick()
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.bg_h1),
                    contentDescription = null,
                    // modifier = Modifier.fillMaxSize(),
                    modifier = Modifier.fillMaxWidth(),
                    contentScale = ContentScale.FillWidth
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    modifier = modifier
                        .align(Alignment.CenterStart)
                        .padding(start = (maxWidth.value * 0.043f).dp)
                )
                if (rightHead != null) {
                    Box(modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = (maxWidth.value * 0.1f).dp)) {
                        rightHead()
                    }
                }
            }

            // Content
            Box(
                modifier = modifier
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(
                            bottomStart = 16.dp,
                            bottomEnd = 8.dp
                        )
                    )
                    .padding(contentPadding)
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }


}

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun PreviewGakuGroupBox() {
    GakuGroupBox(
        title = "GroupBox Title"
    ) {
        Column {
            Text("This is the content of the GroupBox.")
            Text("This is the content of the GroupBox.")
        }
    }
}
