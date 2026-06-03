package com.fleet.ecocar.ui.top

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.theme.EcoCarColors

data class TopBarMusicState(
    val title: String = "—",
    val duration: String = "0:00 / --:--",
    val source: String = "",
    val clock: String = "—",
)

@Composable
fun EcoTopBar(
    music: TopBarMusicState,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = EcoCarColors.SurfaceElevated,
        tonalElevation = 0.dp,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = music.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = EcoCarColors.OnDark,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(top = 4.dp),
                    ) {
                        Text(
                            text = music.duration,
                            style = MaterialTheme.typography.bodySmall,
                            color = EcoCarColors.OnDarkSecondary,
                        )
                        Text(
                            text = music.source,
                            style = MaterialTheme.typography.bodySmall,
                            color = EcoCarColors.GoldenYellow,
                        )
                    }
                }
                Text(
                    text = music.clock,
                    style = MaterialTheme.typography.headlineSmall,
                    color = EcoCarColors.GoldenYellow,
                )
            }
            HorizontalDivider(color = EcoCarColors.Divider, thickness = 1.dp)
        }
    }
}
