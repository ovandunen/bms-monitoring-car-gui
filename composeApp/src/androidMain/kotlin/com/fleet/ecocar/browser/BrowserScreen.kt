package com.fleet.ecocar.browser

import android.os.Handler
import android.os.Looper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fleet.ecocar.EcoCarApplication
import com.fleet.ecocar.theme.EcoCarColors
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.browser_back
import eco_car_gui.composeapp.generated.resources.browser_forward
import eco_car_gui.composeapp.generated.resources.browser_reload
import org.jetbrains.compose.resources.stringResource
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoView

private val mainHandler = Handler(Looper.getMainLooper())

private fun runOnMainThread(block: () -> Unit) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
        block()
    } else {
        mainHandler.post(block)
    }
}

private fun normalizeUrl(raw: String): String {
    val t = raw.trim()
    if (t.isEmpty()) return EcoCarApplication.BROWSER_DEFAULT_HOME_URL
    if (t.contains("://")) return t
    return "https://$t"
}

/**
 * Gecko requires [GeckoSession.open] **before** [GeckoView.setSession]. In Compose, [AndroidView]
 * `factory` runs during composition, while [androidx.compose.runtime.DisposableEffect] runs after —
 * so opening the session in an effect *after* `factory` leaves the session closed at `setSession`
 * and crashes the process.
 */
@Composable
fun BrowserScreen(modifier: Modifier = Modifier) {
    val session = remember { EcoCarApplication.browserSession() }
    val keyboard = LocalSoftwareKeyboardController.current

    val urlBarState = remember { mutableStateOf(EcoCarApplication.BROWSER_DEFAULT_HOME_URL) }
    val canBackState = remember { mutableStateOf(false) }
    val canFwdState = remember { mutableStateOf(false) }
    var urlBar by urlBarState
    var historyCanGoBack by canBackState
    var historyCanGoForward by canFwdState

    val navigationDelegate = remember {
        object : GeckoSession.NavigationDelegate {
            override fun onLocationChange(
                session: GeckoSession,
                url: String?,
                perms: List<GeckoSession.PermissionDelegate.ContentPermission>,
                hasUserGesture: Boolean,
            ) {
                runOnMainThread { urlBarState.value = url.orEmpty() }
            }

            override fun onCanGoBack(session: GeckoSession, canGoBack: Boolean) {
                runOnMainThread { canBackState.value = canGoBack }
            }

            override fun onCanGoForward(session: GeckoSession, canGoForward: Boolean) {
                runOnMainThread { canFwdState.value = canGoForward }
            }
        }
    }

    BackHandler(enabled = historyCanGoBack) {
        session.goBack()
    }

    Column(modifier = modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = EcoCarColors.SurfaceElevated,
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                IconButton(
                    onClick = { session.goBack() },
                    enabled = historyCanGoBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = EcoCarColors.GoldenYellow,
                        disabledContentColor = EcoCarColors.OnDarkSecondary,
                    ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.browser_back),
                    )
                }
                IconButton(
                    onClick = { session.goForward() },
                    enabled = historyCanGoForward,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = EcoCarColors.GoldenYellow,
                        disabledContentColor = EcoCarColors.OnDarkSecondary,
                    ),
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = stringResource(Res.string.browser_forward),
                    )
                }
                IconButton(
                    onClick = { session.reload() },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = EcoCarColors.GoldenYellow,
                    ),
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = stringResource(Res.string.browser_reload),
                    )
                }
                OutlinedTextField(
                    value = urlBar,
                    onValueChange = { urlBar = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = EcoCarColors.OnDark),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                    keyboardActions = KeyboardActions(
                        onGo = {
                            keyboard?.hide()
                            session.loadUri(normalizeUrl(urlBar))
                        },
                    ),
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
            }
        }
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { ctx ->
                session.navigationDelegate = navigationDelegate
                if (!session.isOpen) {
                    session.open(EcoCarApplication.geckoRuntime())
                }
                GeckoView(ctx).also { view ->
                    view.setSession(session)
                    EcoCarApplication.scheduleInitialBrowserLoadIfNeeded(session)
                }
            },
            update = { view ->
                view.setSession(session)
            },
            onRelease = { view ->
                view.releaseSession()
            },
        )
    }
}
