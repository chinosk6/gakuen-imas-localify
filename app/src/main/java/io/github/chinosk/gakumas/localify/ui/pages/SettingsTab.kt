package io.github.chinosk.gakumas.localify.ui.pages

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.chinosk.gakumas.localify.MainActivity
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import io.github.chinosk.gakumas.localify.ui.components.GakuTabRow
import io.github.chinosk.gakumas.localify.ui.pages.subPages.AboutPage
import io.github.chinosk.gakumas.localify.ui.pages.subPages.AdvanceSettingsPage
import io.github.chinosk.gakumas.localify.ui.pages.subPages.HomePage


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SettingsTabs(modifier: Modifier = Modifier,
                 titles: List<String>,
                 context: MainActivity? = null,
                 previewData: GakumasConfig? = null,
                 screenH: Dp = 1080.dp
) {

    val pagerState = rememberPagerState(initialPage = 1, pageCount = { titles.size })

    Box {
        HorizontalPager(
            state = pagerState,
            modifier = modifier.fillMaxSize(),
            pageSpacing = 10.dp
        ) { page ->
            Column(modifier = modifier
                .padding(5.dp)
                .fillMaxHeight(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally) {
                when (page) {
                    0 -> AboutPage(modifier, context = context, previewData = previewData, screenH = screenH)
                    1 -> HomePage(modifier, context = context, previewData = previewData, screenH = screenH)
                    2 -> AdvanceSettingsPage(modifier, context = context, previewData = previewData, screenH = screenH)
                }
            }
        }

        Box(
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 6.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                FloatingActionButton(
                    onClick = { context?.onClickStartGame() },
                    modifier = Modifier.align(Alignment.End),
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "StartGame")
                }

                GakuTabRow(modifier, pagerState, titles) { }
            }
        }
    }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, heightDp = 760)
@Composable
fun SettingTabsPreview(modifier: Modifier = Modifier) {
    SettingsTabs(titles = listOf("TAB 1", "TAB 2", "TAB 3"), previewData = GakumasConfig())
}