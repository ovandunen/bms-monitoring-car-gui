package com.fleet.ecocar.map

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fleet.ecocar.domain.navigation.Guidance
import com.fleet.ecocar.domain.navigation.NavFormat
import com.fleet.ecocar.domain.navigation.RouteError
import com.fleet.ecocar.domain.navigation.RoutePhase
import com.fleet.ecocar.theme.EcoCarColors
import com.fleet.ecocar.ui.DriverTouch
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.nav_route_bad
import eco_car_gui.composeapp.generated.resources.nav_route_end
import eco_car_gui.composeapp.generated.resources.nav_route_loading
import eco_car_gui.composeapp.generated.resources.nav_route_no_vehicle
import eco_car_gui.composeapp.generated.resources.nav_route_none
import eco_car_gui.composeapp.generated.resources.nav_route_off
import eco_car_gui.composeapp.generated.resources.nav_route_remaining
import eco_car_gui.composeapp.generated.resources.nav_route_start
import eco_car_gui.composeapp.generated.resources.nav_route_timeout
import eco_car_gui.composeapp.generated.resources.nav_route_unreachable
import org.jetbrains.compose.resources.stringResource

@Composable
fun MapNavigationOverlay(
    featureEnabled: Boolean,
    selectedName: String?,
    phase: RoutePhase,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val active = phase as? RoutePhase.Active
    if (active != null) {
        GuidancePanel(guidance = active.guidance, onEnd = onEnd, modifier = modifier)
        return
    }
    if (selectedName == null) return
    val message = when {
        phase is RoutePhase.Loading -> stringResource(Res.string.nav_route_loading)
        phase is RoutePhase.Failed -> routeErrorText(phase.error)
        else -> null
    }
    StationRouteCard(
        name = selectedName,
        showStart = featureEnabled && phase !is RoutePhase.Loading,
        message = message,
        onStart = onStart,
        modifier = modifier,
    )
}

@Composable
private fun StationRouteCard(
    name: String,
    showStart: Boolean,
    message: String?,
    onStart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.then(panelModifier())) {
        Text(
            text = name,
            color = EcoCarColors.OnDark,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (message != null) {
            Text(
                text = message,
                color = EcoCarColors.GoldenYellow,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
        if (showStart) {
            TouchLabel(
                label = stringResource(Res.string.nav_route_start),
                onClick = onStart,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun GuidancePanel(
    guidance: Guidance,
    onEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.then(panelModifier())) {
        if (guidance.offRoute) {
            Text(
                text = stringResource(Res.string.nav_route_off),
                color = EcoCarColors.GoldenYellow,
                style = MaterialTheme.typography.titleMedium,
            )
        }
        Text(
            text = guidance.maneuverText,
            color = EcoCarColors.OnDark,
            style = MaterialTheme.typography.titleMedium,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = NavFormat.formatDistance(guidance.distanceToManeuverMeters),
            color = EcoCarColors.OnDark,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 4.dp),
        )
        Text(
            text = stringResource(
                Res.string.nav_route_remaining,
                NavFormat.formatDistance(guidance.remainingMeters),
                NavFormat.formatDuration(guidance.remainingSeconds),
            ),
            color = EcoCarColors.OnDarkSecondary,
            style = MaterialTheme.typography.bodyMedium,
        )
        TouchLabel(
            label = stringResource(Res.string.nav_route_end),
            onClick = onEnd,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Composable
private fun TouchLabel(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(DriverTouch.MinTarget)
            .clip(RoundedCornerShape(8.dp))
            .background(EcoCarColors.GoldenYellow)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = label, color = EcoCarColors.NearBlack, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
private fun routeErrorText(error: RouteError): String = when (error) {
    RouteError.NoVehiclePosition -> stringResource(Res.string.nav_route_no_vehicle)
    RouteError.NoRoute -> stringResource(Res.string.nav_route_none)
    RouteError.Unreachable -> stringResource(Res.string.nav_route_unreachable)
    RouteError.Timeout -> stringResource(Res.string.nav_route_timeout)
    RouteError.BadAnswer -> stringResource(Res.string.nav_route_bad)
}

private fun panelModifier(): Modifier =
    Modifier
        .widthIn(max = 420.dp)
        .clip(RoundedCornerShape(12.dp))
        .background(EcoCarColors.SurfaceElevated)
        .padding(12.dp)
