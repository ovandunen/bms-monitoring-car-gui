package com.fleet.ecocar.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.browser.BrowserHomeStore
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.settings_browser_home_hint
import eco_car_gui.composeapp.generated.resources.settings_browser_home_save
import eco_car_gui.composeapp.generated.resources.settings_browser_home_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun BrowserHomeSettingsSection(
    store: BrowserHomeStore,
    modifier: Modifier = Modifier,
) {
    val saved by store.homeUrl.collectAsState()
    var draft by remember(saved) { mutableStateOf(saved) }
    val scope = rememberCoroutineScope()
    val save: () -> Unit = {
        scope.launch { store.setHomeUrl(draft) }
    }

    Column(modifier = modifier.fillMaxWidth().padding(top = 16.dp)) {
        Text(
            text = stringResource(Res.string.settings_browser_home_title),
            style = MaterialTheme.typography.titleMedium,
            color = EcoCarColors.OnDark,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = EcoCarColors.OnDark),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { save() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = EcoCarColors.OnDark,
                unfocusedTextColor = EcoCarColors.OnDark,
                focusedBorderColor = EcoCarColors.GoldenYellow,
                unfocusedBorderColor = EcoCarColors.Divider,
                cursorColor = EcoCarColors.GoldenYellow,
                focusedContainerColor = EcoCarColors.NearBlack,
                unfocusedContainerColor = EcoCarColors.NearBlack,
            ),
        )
        Text(
            text = stringResource(Res.string.settings_browser_home_hint),
            style = MaterialTheme.typography.bodySmall,
            color = EcoCarColors.OnDarkSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        Button(
            onClick = save,
            colors = ButtonDefaults.buttonColors(
                containerColor = EcoCarColors.GoldenYellow,
                contentColor = EcoCarColors.NearBlack,
            ),
        ) {
            Text(
                text = stringResource(Res.string.settings_browser_home_save),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}
