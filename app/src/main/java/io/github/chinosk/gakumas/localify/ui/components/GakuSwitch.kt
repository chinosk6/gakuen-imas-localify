package io.github.chinosk.gakumas.localify.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp


@Composable
fun GakuSwitch(modifier: Modifier = Modifier,
               text: String = "",
               checked: Boolean = false,
               leftPart: @Composable (() -> Unit)? = null,
               onCheckedChange: (Boolean) -> Unit = {}) {
    Row(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        if (text.isNotEmpty()) {
            Text(modifier = Modifier, text = text, fontSize = 16.sp)
        }
        leftPart?.invoke()
        Switch(checked = checked,
            onCheckedChange = { value -> onCheckedChange(value) },
            modifier = Modifier,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFFFFFFFF),
                checkedTrackColor = Color(0xFFF89400),

                uncheckedThumbColor = Color(0xFFFFFFFF),
                uncheckedTrackColor = Color(0xFFCFD8DC),
                uncheckedBorderColor = Color(0xFFCFD8DC),
            ))
    }
}


@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_NO)
@Composable
fun GakuSwitchPreview() {
    GakuSwitch(text = "Switch", checked = true)
}
