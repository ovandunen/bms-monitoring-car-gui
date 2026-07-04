package com.fleet.ecocar.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.dialog_close
import eco_car_gui.composeapp.generated.resources.dialog_optimal_swap_body
import eco_car_gui.composeapp.generated.resources.dialog_optimal_swap_cta
import eco_car_gui.composeapp.generated.resources.dialog_optimal_swap_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun OptimalSwapDialog(
    stationLabel: String,
    confidencePercent: Int,
    onDismiss: () -> Unit,
    onNavigateToStation: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EcoCarColors.SurfaceElevated,
        titleContentColor = EcoCarColors.OnDark,
        textContentColor = EcoCarColors.OnDarkSecondary,
        title = {
            Text(
                text = stringResource(Res.string.dialog_optimal_swap_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.dialog_optimal_swap_body, stationLabel, confidencePercent),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onNavigateToStation,
                colors = ButtonDefaults.buttonColors(containerColor = EcoCarColors.GoldenYellow),
            ) {
                Text(
                    text = stringResource(Res.string.dialog_optimal_swap_cta),
                    color = EcoCarColors.NearBlack,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.dialog_close))
            }
        },
    )
}
