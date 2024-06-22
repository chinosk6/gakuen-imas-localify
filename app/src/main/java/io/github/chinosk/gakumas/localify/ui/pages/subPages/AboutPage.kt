package io.github.chinosk.gakumas.localify.ui.pages.subPages

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import coil.size.Size
import io.github.chinosk.gakumas.localify.MainActivity
import io.github.chinosk.gakumas.localify.R
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import io.github.chinosk.gakumas.localify.ui.components.GakuButton


@Composable
fun AboutPage(modifier: Modifier = Modifier,
             context: MainActivity? = null,
             previewData: GakumasConfig? = null,
             bottomSpacerHeight: Dp = 120.dp,
             screenH: Dp = 1080.dp) {
    // val config = getConfigState(context, previewData)

    LazyColumn(modifier = modifier
        .sizeIn(maxHeight = screenH)
        .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }

        item {
            Text(stringResource(R.string.about_warn_title), fontSize = 24.sp, color = MaterialTheme.colorScheme.error)
            Text(stringResource(R.string.about_warn_p1))
            Text(stringResource(R.string.about_warn_p2))
        }

        item {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }

        item {
            Text(stringResource(R.string.about_about_title), fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Text(stringResource(R.string.about_about_p1))
            Text(stringResource(R.string.about_about_p2))
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 8.dp, end = 8.dp, top = 8.dp, bottom = 0.dp
                )) {
                GakuButton(text = "Github", modifier = modifier.weight(1f), onClick = {
                    context?.openUrl("https://github.com/chinosk6/gakuen-imas-localify")
                })
            }
        }

        item {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }

        item {
            LazyColumn(modifier = modifier
                .sizeIn(maxHeight = screenH)
                .fillMaxWidth()) {
                item {
                    Text(stringResource(R.string.project_contribution), fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
                item {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp, 8.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("chinosk（${stringResource(R.string.plugin_code)}）", fontSize = 16.sp)
                        GakuButton(text = "Github", modifier = modifier.height(40.dp),
                            onClick = {
                            context?.openUrl("https://github.com/chinosk6")
                        })
                        GakuButton(text = "Bilibili", modifier = modifier.height(40.dp),
                            onClick = {
                                context?.openUrl("https://space.bilibili.com/287061163")
                            })
                    }
                }
                item {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp, 8.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("DarwinTree（${stringResource(R.string.translation_workflow)}）", fontSize = 16.sp)
                        GakuButton(text = "Github", modifier = modifier.height(40.dp),
                            onClick = {
                                context?.openUrl("https://github.com/darwintree")
                            })
                        GakuButton(text = "Bilibili", modifier = modifier.height(40.dp),
                            onClick = {
                                context?.openUrl("https://space.bilibili.com/6069705")
                            })
                    }
                }
                item {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 8.dp, 8.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(R.string.and_other_translators), fontSize = 16.sp)
                        GakuButton(text = "Github", modifier = modifier.height(40.dp),
                            onClick = {
                                context?.openUrl("https://github.com/chinosk6/GakumasTranslationData/graphs/contributors")
                            })
                    }
                }
            }
        }

        item {
            Text(stringResource(R.string.contributors), fontSize = 24.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)

            Text(stringResource(R.string.plugin_code), fontSize = 16.sp)
            NetworkSvgImage(
                url = "https://contrib.rocks/image?repo=chinosk6/gakuen-imas-localify",
                contentDescription = "plugin-contrib"
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(stringResource(R.string.translation_repository), fontSize = 16.sp)
            NetworkSvgImage(
                url = "https://contrib.rocks/image?repo=chinosk6/GakumasTranslationData",
                contentDescription = "translation-contrib"
            )
        }

        item {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
        }

        item {
            Spacer(modifier = modifier.height(bottomSpacerHeight))
        }
    }
}


@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val painter = rememberAsyncImagePainter(model = ImageRequest.Builder(LocalContext.current)
        .data(url)
        .crossfade(true)
        .size(Size.ORIGINAL)
        .build())

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
    )
}

@Composable
fun NetworkSvgImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val imageLoader = ImageLoader.Builder(LocalContext.current)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .size(Size.ORIGINAL)
            .build(),
        imageLoader = imageLoader
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier
    )
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun AboutPagePreview(modifier: Modifier = Modifier, data: GakumasConfig = GakumasConfig()) {
    AboutPage(modifier, previewData = data)
}