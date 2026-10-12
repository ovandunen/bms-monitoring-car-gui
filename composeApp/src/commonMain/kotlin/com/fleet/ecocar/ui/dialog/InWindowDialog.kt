package com.fleet.ecocar.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors

class InWindowDialogSlot {
    var content = mutableStateOf<(@Composable () -> Unit)?>(null)
}

val LocalInWindowDialogSlot = staticCompositionLocalOf { InWindowDialogSlot() }

@Composable
fun InWindowDialog(
    onDismissRequest: () -> Unit,
    title: @Composable () -> Unit,
    text: @Composable () -> Unit,
    buttons: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismissRequest),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 560.dp)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            color = EcoCarColors.SurfaceElevated,
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                title()
                Box(modifier = Modifier.padding(top = 12.dp)) {
                    text()
                }
                Box(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    buttons()
                }
            }
        }
    }
}
