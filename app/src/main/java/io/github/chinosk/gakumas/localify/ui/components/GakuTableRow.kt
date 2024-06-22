package io.github.chinosk.gakumas.localify.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import androidx.compose.material3.Surface
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.*
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GakuTabRow(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    tabs: List<String>,
    onTabSelected: (index: Int) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(pagerState.currentPage) {
        onTabSelected(pagerState.currentPage)
    }

    Box(
        modifier = Modifier
            .shadow(4.dp, RoundedCornerShape(16.dp))
    ) {
        Surface(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .shadow(4.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column {
                TabRow(
                    modifier = modifier.background(Color.Transparent),
                    containerColor = Color.Transparent,
                    selectedTabIndex = pagerState.currentPage,
                    indicator = @Composable { tabPositions ->
                        Box(
                            Modifier
                                .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                .height(4.dp)
                                .background(Color(0xFFFFA500))
                                .padding(horizontal = 4.dp)
                        )
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch {
                                    pagerState.scrollToPage(index)
//                                    pagerState.animateScrollToPage(
//                                        page = index,
//                                        animationSpec = tween(durationMillis = 250)
//                                    )
                                }
                            },
                            text = {
                                Text(
                                    text = title,
                                    color = if (pagerState.currentPage == index) Color(0xFFFFA500) else Color.Black
                                )
                            }
                        )
                    }
                }


            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun GakuTabRowPreview(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    GakuTabRow(modifier, pagerState, listOf("TAB 1", "TAB 2", "TAB 3")) { _ -> }
}