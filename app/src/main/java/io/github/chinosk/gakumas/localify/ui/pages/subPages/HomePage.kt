package io.github.chinosk.gakumas.localify.ui.pages.subPages

import io.github.chinosk.gakumas.localify.ui.components.GakuGroupBox
import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.chinosk.gakumas.localify.MainActivity
import io.github.chinosk.gakumas.localify.R
import io.github.chinosk.gakumas.localify.getConfigState
import io.github.chinosk.gakumas.localify.getProgramConfigState
import io.github.chinosk.gakumas.localify.getProgramDownloadAbleState
import io.github.chinosk.gakumas.localify.getProgramDownloadErrorStringState
import io.github.chinosk.gakumas.localify.getProgramDownloadState
import io.github.chinosk.gakumas.localify.getProgramLocalResourceVersionState
import io.github.chinosk.gakumas.localify.hookUtils.FileHotUpdater
import io.github.chinosk.gakumas.localify.mainUtils.FileDownloader
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import io.github.chinosk.gakumas.localify.models.ResourceCollapsibleBoxViewModel
import io.github.chinosk.gakumas.localify.models.ResourceCollapsibleBoxViewModelFactory
import io.github.chinosk.gakumas.localify.ui.components.base.CollapsibleBox
import io.github.chinosk.gakumas.localify.ui.components.GakuButton
import io.github.chinosk.gakumas.localify.ui.components.GakuProgressBar
import io.github.chinosk.gakumas.localify.ui.components.GakuRadio
import io.github.chinosk.gakumas.localify.ui.components.GakuSwitch
import io.github.chinosk.gakumas.localify.ui.components.GakuTextInput
import java.io.File


@Composable
fun HomePage(modifier: Modifier = Modifier,
             context: MainActivity? = null,
             previewData: GakumasConfig? = null,
             bottomSpacerHeight: Dp = 120.dp,
             screenH: Dp = 1080.dp) {
    val config = getConfigState(context, previewData)
    val programConfig = getProgramConfigState(context)

    val downloadProgress by getProgramDownloadState(context)
    val downloadAble by getProgramDownloadAbleState(context)
    val localResourceVersion by getProgramLocalResourceVersionState(context)
    val downloadErrorString by getProgramDownloadErrorStringState(context)

    // val scrollState = rememberScrollState()
    val keyboardOptionsNumber = remember {
        KeyboardOptions(keyboardType = KeyboardType.Number)
    }
    val keyBoardOptionsDecimal = remember {
        KeyboardOptions(keyboardType = KeyboardType.Decimal)
    }

    val resourceSettingsViewModel: ResourceCollapsibleBoxViewModel =
        viewModel(factory = ResourceCollapsibleBoxViewModelFactory(initiallyExpanded = false))

    fun onClickDownload() {
        context?.mainPageAssetsViewDataUpdate(
            downloadAbleState = false,
            errorString = "",
            downloadProgressState = -1f
        )
        val (_, newUrl) = FileDownloader.checkAndChangeDownloadURL(programConfig.value.transRemoteZipUrl)
        context?.onPTransRemoteZipUrlChanged(newUrl, 0, 0, 0)
        FileDownloader.downloadFile(
            newUrl,
            checkContentTypes = listOf("application/zip", "application/octet-stream"),
            onDownload = { progress, _, _ ->
                context?.mainPageAssetsViewDataUpdate(downloadProgressState = progress)
            },

            onSuccess = { byteArray ->
                context?.mainPageAssetsViewDataUpdate(
                    downloadAbleState = true,
                    errorString = "",
                    downloadProgressState = -1f
                )
                val file = File(context?.filesDir, "update_trans.zip")
                file.writeBytes(byteArray)
                val newFileVersion = FileHotUpdater.getZipResourceVersion(file.absolutePath)
                if (newFileVersion != null) {
                    context?.mainPageAssetsViewDataUpdate(
                        localResourceVersionState = newFileVersion
                    )
                }
                else {
                    context?.mainPageAssetsViewDataUpdate(
                        localResourceVersionState = context.getString(
                            R.string.invalid_zip_file
                        ),
                        errorString = context.getString(R.string.invalid_zip_file_warn)
                    )
                }
            },

            onFailed = { code, reason ->
                context?.mainPageAssetsViewDataUpdate(
                    downloadAbleState = true,
                    errorString = reason,
                )
            })

    }


    LazyColumn(modifier = modifier
        .sizeIn(maxHeight = screenH)
        // .fillMaxHeight()
        // .verticalScroll(scrollState)
        // .width(IntrinsicSize.Max)
        .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            GakuGroupBox(modifier = modifier, stringResource(R.string.basic_settings)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    GakuSwitch(modifier, stringResource(R.string.enable_plugin), checked = config.value.enabled) {
                            v -> context?.onEnabledChanged(v)
                    }

                    GakuSwitch(modifier, stringResource(R.string.replace_font), checked = config.value.replaceFont) {
                            v -> context?.onReplaceFontChanged(v)
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }

        item {
            GakuGroupBox(modifier, stringResource(R.string.resource_settings),
                contentPadding = 0.dp,
                onHeadClick = {
                    resourceSettingsViewModel.expanded = !resourceSettingsViewModel.expanded
                }) {
                CollapsibleBox(modifier = modifier,
                    viewModel = resourceSettingsViewModel
                ) {
                    LazyColumn(modifier = modifier
                        // .padding(8.dp)
                        .sizeIn(maxHeight = screenH),
                        // verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            GakuSwitch(modifier = modifier.padding(start = 8.dp, end = 8.dp, top = 8.dp),
                                checked = programConfig.value.checkBuiltInAssets,
                                text = stringResource(id = R.string.check_built_in_resource)
                            ) { v -> context?.onPCheckBuiltInAssetsChanged(v) }
                        }
                        item {
                            GakuSwitch(modifier = modifier.padding(start = 8.dp, end = 8.dp),
                                checked = programConfig.value.cleanLocalAssets,
                                text = stringResource(id = R.string.delete_plugin_resource)
                            ) { v -> context?.onPCleanLocalAssetsChanged(v) }
                        }

                        item {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                            )
                        }

                        item {
                            GakuSwitch(modifier = modifier.padding(start = 8.dp, end = 8.dp),
                                checked = programConfig.value.useRemoteAssets,
                                text = stringResource(id = R.string.use_remote_zip_resource)
                            ) { v -> context?.onPUseRemoteAssetsChanged(v) }

                            CollapsibleBox(modifier = modifier.graphicsLayer(clip = false),
                                expandState = programConfig.value.useRemoteAssets,
                                collapsedHeight = 0.dp,
                                innerPaddingLeftRight = 8.dp,
                                showExpand = false
                            ) {
                                GakuSwitch(modifier = modifier,
                                    checked = programConfig.value.delRemoteAfterUpdate,
                                    text = stringResource(id = R.string.del_remote_after_update)
                                ) { v -> context?.onPDelRemoteAfterUpdateChanged(v) }

                                LazyColumn(modifier = modifier
                                    // .padding(8.dp)
                                    .sizeIn(maxHeight = screenH),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    item {
                                        Row(modifier = modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            verticalAlignment = Alignment.CenterVertically) {

                                            GakuTextInput(modifier = modifier
                                                .height(45.dp)
                                                .padding(end = 8.dp)
                                                .fillMaxWidth()
                                                .weight(1f),
                                                fontSize = 14f,
                                                value = programConfig.value.transRemoteZipUrl,
                                                onValueChange = { c -> context?.onPTransRemoteZipUrlChanged(c, 0, 0, 0)},
                                                label = { Text(stringResource(id = R.string.resource_url)) },
                                                keyboardOptions = keyboardOptionsNumber)

                                            if (downloadAble) {
                                                GakuButton(modifier = modifier
                                                    .height(40.dp)
                                                    .sizeIn(minWidth = 80.dp),
                                                    text = stringResource(id = R.string.download),
                                                    onClick = { onClickDownload() })
                                            }
                                            else {
                                                GakuButton(modifier = modifier
                                                    .height(40.dp)
                                                    .sizeIn(minWidth = 80.dp),
                                                    text = stringResource(id = R.string.cancel), onClick = {
                                                        FileDownloader.cancel()
                                                    })
                                            }

                                        }
                                    }

                                    if (downloadProgress >= 0) {
                                        item {
                                            GakuProgressBar(progress = downloadProgress, isError = downloadErrorString.isNotEmpty())
                                        }
                                    }

                                    if (downloadErrorString.isNotEmpty()) {
                                        item {
                                            Text(text = downloadErrorString, color = Color(0xFFE2041B))
                                        }
                                    }

                                    item {
                                        Text(modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val file =
                                                    File(context?.filesDir, "update_trans.zip")
                                                context?.mainPageAssetsViewDataUpdate(
                                                    localResourceVersionState = FileHotUpdater
                                                        .getZipResourceVersion(file.absolutePath)
                                                        .toString()
                                                )
                                            }, text = "${stringResource(R.string.downloaded_resource_version)}: $localResourceVersion")
                                    }

                                    item {
                                        Spacer(Modifier.height(0.dp))
                                    }

                                }

                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
        }

        item {
            GakuGroupBox(modifier = modifier, contentPadding = 0.dp, title = stringResource(R.string.graphic_settings)) {
                LazyColumn(modifier = Modifier
                    .sizeIn(maxHeight = screenH),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        GakuTextInput(modifier = modifier
                            .padding(start = 4.dp, end = 4.dp)
                            .height(45.dp)
                            .fillMaxWidth(),
                            fontSize = 14f,
                            value = config.value.targetFrameRate.toString(),
                            onValueChange = { c -> context?.onTargetFpsChanged(c, 0, 0, 0)},
                            label = { Text(stringResource(R.string.setFpsTitle)) },
                            keyboardOptions = keyboardOptionsNumber)
                    }

                    item {
                        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(stringResource(R.string.orientation_lock))
                            Row(modifier = modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val radioModifier = remember {
                                    modifier
                                        .height(40.dp)
                                        .weight(1f)
                                }

                                GakuRadio(modifier = radioModifier,
                                    text = stringResource(R.string.orientation_orig), selected = config.value.gameOrientation == 0,
                                    onClick = { context?.onGameOrientationChanged(0) })

                                GakuRadio(modifier = radioModifier,
                                    text = stringResource(R.string.orientation_portrait), selected = config.value.gameOrientation == 1,
                                    onClick = { context?.onGameOrientationChanged(1) })

                                GakuRadio(modifier = radioModifier,
                                    text = stringResource(R.string.orientation_landscape), selected = config.value.gameOrientation == 2,
                                    onClick = { context?.onGameOrientationChanged(2) })
                            }
                        }
                    }

                    item {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                        )
                    }

                    item {
                        GakuSwitch(modifier.padding(start = 8.dp, end = 8.dp),
                            stringResource(R.string.useCustomeGraphicSettings),
                            checked = config.value.useCustomeGraphicSettings) {
                                v -> context?.onUseCustomeGraphicSettingsChanged(v)
                        }

                        CollapsibleBox(modifier = modifier,
                            expandState = config.value.useCustomeGraphicSettings,
                            collapsedHeight = 0.dp,
                            showExpand = false
                        ) {
                            LazyColumn(modifier = modifier
                                .padding(8.dp)
                                .sizeIn(maxHeight = screenH)
                                .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                item {
                                    Row(modifier = modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val buttonModifier = remember {
                                            modifier
                                                .height(40.dp)
                                                .weight(1f)
                                        }

                                        GakuButton(modifier = buttonModifier,
                                            text = stringResource(R.string.max_high), onClick = { context?.onChangePresetQuality(4) })

                                        GakuButton(modifier = buttonModifier,
                                            text = stringResource(R.string.very_high), onClick = { context?.onChangePresetQuality(3) })

                                        GakuButton(modifier = buttonModifier,
                                            text = stringResource(R.string.hign), onClick = { context?.onChangePresetQuality(2) })

                                        GakuButton(modifier = buttonModifier,
                                            text = stringResource(R.string.middle), onClick = { context?.onChangePresetQuality(1) })

                                        GakuButton(modifier = buttonModifier,
                                            text = stringResource(R.string.low), onClick = { context?.onChangePresetQuality(0) })
                                    }
                                }

                                item {
                                    Row(modifier = modifier,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val textInputModifier = remember {
                                            modifier
                                                .height(45.dp)
                                                .weight(1f)
                                        }

                                        GakuTextInput(modifier = textInputModifier,
                                            fontSize = 14f,
                                            value = config.value.renderScale.toString(),
                                            onValueChange = { c -> context?.onRenderScaleChanged(c, 0, 0, 0)},
                                            label = { Text(stringResource(R.string.renderscale)) },
                                            keyboardOptions = keyBoardOptionsDecimal)

                                        GakuTextInput(modifier = textInputModifier,
                                            fontSize = 14f,
                                            value = config.value.qualitySettingsLevel.toString(),
                                            onValueChange = { c -> context?.onQualitySettingsLevelChanged(c, 0, 0, 0)},
                                            label = { Text("QualityLevel (1/1/2/3/5)") },
                                            keyboardOptions = keyboardOptionsNumber)
                                    }
                                }

                                item {
                                    Row(modifier = modifier,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val textInputModifier = remember {
                                            modifier
                                                .height(45.dp)
                                                .weight(1f)
                                        }

                                        GakuTextInput(modifier = textInputModifier,
                                            fontSize = 14f,
                                            value = config.value.volumeIndex.toString(),
                                            onValueChange = { c -> context?.onVolumeIndexChanged(c, 0, 0, 0)},
                                            label = { Text("VolumeIndex (0/1/2/3/4)") },
                                            keyboardOptions = keyboardOptionsNumber)

                                        GakuTextInput(modifier = textInputModifier,
                                            fontSize = 14f,
                                            value = config.value.maxBufferPixel.toString(),
                                            onValueChange = { c -> context?.onMaxBufferPixelChanged(c, 0, 0, 0)},
                                            label = { Text("MaxBufferPixel (1024/1440/2538/3384/8190)", fontSize = 10.sp) },
                                            keyboardOptions = keyboardOptionsNumber)
                                    }
                                }

                                item {
                                    Row(modifier = modifier,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        val textInputModifier = remember {
                                            modifier
                                                .height(45.dp)
                                                .weight(1f)
                                        }

                                        GakuTextInput(modifier = textInputModifier,
                                            fontSize = 14f,
                                            value = config.value.reflectionQualityLevel.toString(),
                                            onValueChange = { c -> context?.onReflectionQualityLevelChanged(c, 0, 0, 0)},
                                            label = { Text( text = "ReflectionLevel (0~5)") },
                                            keyboardOptions = keyboardOptionsNumber)

                                        GakuTextInput(modifier = textInputModifier,
                                            fontSize = 14f,
                                            value = config.value.lodQualityLevel.toString(),
                                            onValueChange = { c -> context?.onLodQualityLevelChanged(c, 0, 0, 0)},
                                            label = { Text("LOD Level (0~5)") },
                                            keyboardOptions = keyboardOptionsNumber)
                                    }
                                }
                            }
                        }

                    }

                }

            }
        }

        item {
            Spacer(modifier = modifier.height(bottomSpacerHeight))
        }
    }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun HomePagePreview(modifier: Modifier = Modifier, data: GakumasConfig = GakumasConfig()) {
    HomePage(modifier, previewData = data)
}
