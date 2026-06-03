package com.ecocar.gui.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.ecocar.gui.i18n.AppLanguage
import com.ecocar.gui.i18n.LanguageRepository
import com.ecocar.gui.i18n.collectLanguageAsState
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.language_de
import eco_car_gui.composeapp.generated.resources.language_en
import eco_car_gui.composeapp.generated.resources.language_fr
import eco_car_gui.composeapp.generated.resources.language_wo
import eco_car_gui.composeapp.generated.resources.settings_language_title
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun LanguageSettingsSection(
    languageRepository: LanguageRepository,
    modifier: Modifier = Modifier,
) {
    val currentLanguage = languageRepository.selectedLanguage.collectLanguageAsState(
        languageRepository.getDefault(),
    )
    val scope = rememberCoroutineScope()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = stringResource(Res.string.settings_language_title),
            style = MaterialTheme.typography.titleMedium,
            color = EcoCarColors.OnDark,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        AppLanguage.entries.forEach { lang ->
            LanguageOptionRow(
                label = languageLabel(lang),
                selected = currentLanguage.value == lang,
                onSelect = { scope.launch { languageRepository.setLanguage(lang) } },
            )
        }
        HorizontalDivider(
            color = EcoCarColors.Divider,
            modifier = Modifier.padding(top = 12.dp),
        )
    }
}

@Composable
private fun languageLabel(lang: AppLanguage): String = stringResource(
    when (lang) {
        AppLanguage.DE -> Res.string.language_de
        AppLanguage.EN -> Res.string.language_en
        AppLanguage.FR -> Res.string.language_fr
        AppLanguage.WO -> Res.string.language_wo
    },
)

@Composable
private fun LanguageOptionRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onSelect,
                role = Role.RadioButton,
            )
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        RadioButton(
            selected = selected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = EcoCarColors.GoldenYellow,
                unselectedColor = EcoCarColors.OnDarkSecondary,
            ),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = EcoCarColors.OnDark,
        )
    }
}
