package io.github.chinosk.gakumas.localify.ui.pages.subPages

import GakuGroupBox
import android.content.res.Configuration.UI_MODE_NIGHT_NO
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.chinosk.gakumas.localify.MainActivity
import io.github.chinosk.gakumas.localify.R
import io.github.chinosk.gakumas.localify.getConfigState
import io.github.chinosk.gakumas.localify.models.GakumasConfig
import io.github.chinosk.gakumas.localify.ui.components.base.CollapsibleBox
import io.github.chinosk.gakumas.localify.ui.components.GakuButton
import io.github.chinosk.gakumas.localify.ui.components.GakuRadio
import io.github.chinosk.gakumas.localify.ui.components.GakuSwitch
import io.github.chinosk.gakumas.localify.ui.components.GakuTextInput


@Composable
fun HomePage(modifier: Modifier = Modifier,
             context: MainActivity? = null,
             previewData: GakumasConfig? = null,
             bottomSpacerHeight: Dp = 120.dp,
             screenH: Dp = 1080.dp) {
    val config = getConfigState(context, previewData)
    // val scrollState = rememberScrollState()
    val keyboardOptionsNumber = remember {
        KeyboardOptions(keyboardType = KeyboardType.Number)
    }
    val keyBoardOptionsDecimal = remember {
        KeyboardOptions(keyboardType = KeyboardType.Decimal)
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


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO, widthDp = 880)
@Composable
fun HomePagePreview(modifier: Modifier = Modifier, data: GakumasConfig = GakumasConfig()) {
    HomePage(modifier, previewData = data)
}
