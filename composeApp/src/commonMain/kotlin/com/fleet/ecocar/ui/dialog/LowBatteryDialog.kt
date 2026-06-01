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
import eco_car_gui.composeapp.generated.resources.dialog_low_battery_charge_cta
import eco_car_gui.composeapp.generated.resources.dialog_close
import eco_car_gui.composeapp.generated.resources.dialog_low_battery_action
import eco_car_gui.composeapp.generated.resources.dialog_low_battery_body
import eco_car_gui.composeapp.generated.resources.dialog_low_battery_title
import eco_car_gui.composeapp.generated.resources.dialog_technical
import org.jetbrains.compose.resources.stringResource

@Composable
fun LowBatteryDialog(
    onDismiss: () -> Unit,
    onNavigateToCharging: () -> Unit,
    onTechnicalIssues: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = EcoCarColors.SurfaceElevated,
        titleContentColor = EcoCarColors.OnDark,
        textContentColor = EcoCarColors.OnDarkSecondary,
        title = {
            Text(
                text = stringResource(Res.string.dialog_low_battery_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(Res.string.dialog_low_battery_body),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = stringResource(Res.string.dialog_low_battery_action),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    onClick = {
                        onNavigateToCharging()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EcoCarColors.GoldenYellow,
                        contentColor = EcoCarColors.NearBlack,
                    ),
                ) {
                    Text(stringResource(Res.string.dialog_low_battery_charge_cta))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextButton(
                        onClick = {
                            onTechnicalIssues()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(stringResource(Res.string.dialog_technical), color = EcoCarColors.GoldenYellow)
                    }
                    TextButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                        Text(stringResource(Res.string.dialog_close), color = EcoCarColors.OnDarkSecondary)
                    }
                }
            }
        },
    )
}
