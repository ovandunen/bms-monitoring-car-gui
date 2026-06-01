package com.fleet.ecocar.music

import android.Manifest
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import com.bumptech.glide.Glide
import com.fleet.ecocar.EcoCarApplication
import eco_car_gui.composeapp.generated.resources.Res
import eco_car_gui.composeapp.generated.resources.music_buffering
import eco_car_gui.composeapp.generated.resources.music_no_files
import eco_car_gui.composeapp.generated.resources.music_no_stations
import eco_car_gui.composeapp.generated.resources.music_retry
import eco_car_gui.composeapp.generated.resources.music_retry_search
import eco_car_gui.composeapp.generated.resources.music_search_hint
import eco_car_gui.composeapp.generated.resources.music_tab_radio
import eco_car_gui.composeapp.generated.resources.music_tab_usb
import org.jetbrains.compose.resources.stringResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private val MusicBg = Color(0xFF1A1A1A)
private val TabActive = Color(0xFFF5C518)
private val SidebarGreen = Color(0xFF2D6A4F)
private val TextPrimary = Color.White
private val TextSecondary = Color(0xFFAAAAAA)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MusicScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val app = context.applicationContext as EcoCarApplication
    val player = remember(app) { app.ensureMusicExoPlayer() }
    val scope = rememberCoroutineScope()

    var mainTab by remember { mutableIntStateOf(0) }

    var usbTracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var stations by remember { mutableStateOf<List<RadioStation>>(emptyList()) }
    var radioLoading by remember { mutableStateOf(false) }
    var radioError by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    var playbackError by remember { mutableStateOf<String?>(null) }

    var isPlaying by remember { mutableStateOf(player.isPlaying) }
    var positionMs by remember { mutableFloatStateOf(player.currentPosition.toFloat()) }
    var durationMs by remember { mutableFloatStateOf(player.duration.coerceAtLeast(0L).toFloat()) }
    var playbackState by remember { mutableIntStateOf(player.playbackState) }

    val permissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { granted ->
        if (granted.values.all { it }) {
            scope.launch {
                usbTracks = MusicRepository.loadLocalTracks(app)
            }
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(permissions)
    }

    LaunchedEffect(Unit) {
        radioLoading = true
        radioError = null
        runCatching {
            stations = MusicRepository.loadGermanyRadioStations()
        }.onFailure { e ->
            radioError = e.message ?: "Radio laden fehlgeschlagen"
        }
        radioLoading = false
    }

    LaunchedEffect(player) {
        while (isActive) {
            delay(300)
            isPlaying = player.isPlaying
            positionMs = player.currentPosition.toFloat()
            durationMs = player.duration.takeIf { it > 0 }?.toFloat() ?: 0f
            playbackState = player.playbackState
        }
    }

    DisposableEffect(player) {
        val listener = object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                playbackError = error.message ?: "Wiedergabe-Fehler"
            }
        }
        player.addListener(listener)
        onDispose { player.removeListener(listener) }
    }

    val filteredStations = remember(stations, searchQuery) {
        if (searchQuery.isBlank()) stations
        else {
            val q = searchQuery.lowercase()
            stations.filter {
                it.name.lowercase().contains(q) || it.genre.lowercase().contains(q)
            }
        }
    }

    val currentUsbIndex = player.currentMediaItemIndex
    val isUsbMode = app.musicPlaybackSurface == MusicPlaybackSurface.USB

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MusicBg)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        MusicTabRow(
            selectedIndex = mainTab,
            onSelect = { mainTab = it },
        )
        HorizontalDivider(color = TextSecondary.copy(alpha = 0.3f))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (mainTab) {
                0 -> UsbTrackList(
                    tracks = usbTracks,
                    playingIndex = if (isUsbMode) currentUsbIndex else -1,
                    onTrackClick = { index ->
                        playbackError = null
                        app.playUsbTracks(usbTracks, index)
                    },
                )
                else -> RadioStationList(
                    stations = filteredStations,
                    loading = radioLoading,
                    error = radioError,
                    onRetry = {
                        scope.launch {
                            radioLoading = true
                            radioError = null
                            runCatching { stations = MusicRepository.loadGermanyRadioStations() }
                                .onFailure { e -> radioError = e.message }
                            radioLoading = false
                        }
                    },
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    buffering = playbackState == Player.STATE_BUFFERING && app.musicPlaybackSurface == MusicPlaybackSurface.RADIO,
                    onStationClick = { station ->
                        playbackError = null
                        app.playRadioStation(station)
                    },
                )
            }
        }

        playbackError?.let { err ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(err, color = TabActive, fontSize = 13.sp)
                Button(
                    onClick = {
                        playbackError = null
                        player.prepare()
                        player.play()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SidebarGreen, contentColor = Color.White),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(stringResource(Res.string.music_retry))
                }
            }
        }

        SharedPlayerBar(
            player = player,
            app = app,
            isPlaying = isPlaying,
            positionMs = positionMs,
            durationMs = durationMs,
            usbTracks = usbTracks,
            currentUsbIndex = currentUsbIndex,
            isUsbMode = isUsbMode,
        )
    }
}

@Composable
private fun MusicTabRow(selectedIndex: Int, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MusicTabChip(
            label = "🎵 ${stringResource(Res.string.music_tab_usb)}",
            selected = selectedIndex == 0,
            onClick = { onSelect(0) },
            modifier = Modifier.weight(1f),
        )
        MusicTabChip(
            label = "📻 ${stringResource(Res.string.music_tab_radio)}",
            selected = selectedIndex == 1,
            onClick = { onSelect(1) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MusicTabChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) SidebarGreen else MusicBg
    val border = if (selected) TabActive else TextSecondary.copy(alpha = 0.4f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (selected) TabActive else TextPrimary,
            fontSize = 15.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
private fun UsbTrackList(
    tracks: List<Track>,
    playingIndex: Int,
    onTrackClick: (Int) -> Unit,
) {
    if (tracks.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(stringResource(Res.string.music_no_files), color = TextSecondary)
        }
        return
    }
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        itemsIndexed(tracks, key = { _, t -> t.id }) { index, track ->
            val selected = index == playingIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) SidebarGreen.copy(alpha = 0.45f) else Color(0xFF252525))
                    .clickable { onTrackClick(index) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AlbumArtThumb(uri = track.albumArtUri, modifier = Modifier.size(52.dp))
                Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(track.title, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(track.artist, color = TextSecondary, fontSize = 13.sp, maxLines = 1)
                }
                Text(formatDur(track.durationMs), color = TextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun RadioStationList(
    stations: List<RadioStation>,
    loading: Boolean,
    error: String?,
    onRetry: () -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    buffering: Boolean,
    onStationClick: (RadioStation) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Filled.Search, null, tint = TabActive) },
            placeholder = { Text(stringResource(Res.string.music_search_hint), color = TextSecondary) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = TabActive,
                unfocusedBorderColor = TextSecondary,
                cursorColor = TabActive,
                focusedContainerColor = Color(0xFF252525),
                unfocusedContainerColor = Color(0xFF252525),
            ),
        )
        Spacer(Modifier.height(8.dp))
        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TabActive)
            }
            error != null -> Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(error, color = TabActive)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = SidebarGreen)) {
                    Text(stringResource(Res.string.music_retry_search))
                }
            }
            else -> {
                Column(Modifier.fillMaxSize()) {
                    if (buffering) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = TabActive,
                                strokeWidth = 2.dp,
                            )
                            Text(stringResource(Res.string.music_buffering), color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                    if (stations.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(stringResource(Res.string.music_no_stations), color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.weight(1f),
                        ) {
                            itemsIndexed(stations, key = { _, s -> "${s.name}_${s.streamUrl}" }) { _, station ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF252525))
                                        .clickable { onStationClick(station) }
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    FaviconThumb(url = station.favicon, modifier = Modifier.size(44.dp))
                                    Column(Modifier.weight(1f).padding(horizontal = 10.dp)) {
                                        Text(station.name, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(
                                            "${station.country} · ${station.genre}" +
                                                if (station.bitrate > 0) " · ${station.bitrate} kbps" else "",
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            maxLines = 2,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SharedPlayerBar(
    player: Player,
    app: EcoCarApplication,
    isPlaying: Boolean,
    positionMs: Float,
    durationMs: Float,
    usbTracks: List<Track>,
    currentUsbIndex: Int,
    isUsbMode: Boolean,
) {
    val context = LocalContext.current
    val item = player.currentMediaItem
    val meta = item?.mediaMetadata
    val title = meta?.title?.toString().orEmpty().ifBlank { "—" }
    val subtitle = meta?.artist?.toString().orEmpty()

    val currentTrackUri = if (isUsbMode && currentUsbIndex in usbTracks.indices) {
        usbTracks[currentUsbIndex].uri
    } else {
        null
    }

    var embeddedArt by remember(currentTrackUri) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(currentTrackUri) {
        embeddedArt = null
        if (currentTrackUri == null) return@LaunchedEffect
        embeddedArt = withContext(Dispatchers.IO) {
            runCatching {
                val r = MediaMetadataRetriever()
                try {
                    r.setDataSource(context, currentTrackUri)
                    r.embeddedPicture?.let { bytes -> android.graphics.BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
                } finally {
                    r.release()
                }
            }.getOrNull()
        }
    }

    HorizontalDivider(color = TextSecondary.copy(alpha = 0.25f))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(56.dp)) {
            when (app.musicPlaybackSurface) {
                MusicPlaybackSurface.USB -> {
                    if (embeddedArt != null) {
                        Image(
                            bitmap = embeddedArt!!.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(6.dp)),
                            contentScale = ContentScale.Crop,
                        )
                    } else {
                        AlbumArtThumb(
                            uri = usbTracks.getOrNull(currentUsbIndex)?.albumArtUri,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
                MusicPlaybackSurface.RADIO -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.MusicNote, null, tint = TabActive, modifier = Modifier.size(36.dp))
                    }
                }
                null -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(Icons.Filled.MusicNote, null, tint = TabActive, modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                maxLines = 1,
                modifier = Modifier.basicMarquee(),
            )
            Text(
                text = subtitle.ifBlank { " " },
                color = TextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.basicMarquee(),
            )
            val durOk = durationMs > 0f
            if (durOk) {
                Slider(
                    value = (positionMs / durationMs).coerceIn(0f, 1f),
                    onValueChange = { f ->
                        player.seekTo((f * durationMs).toLong())
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = TabActive,
                        activeTrackColor = SidebarGreen,
                        inactiveTrackColor = TextSecondary.copy(alpha = 0.35f),
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                Spacer(Modifier.height(8.dp))
            }
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(formatDurMs(positionMs.toLong()), color = TextSecondary, fontSize = 12.sp)
                Text(
                    if (durOk) formatDurMs(durationMs.toLong()) else "--:--",
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { player.seekToPreviousMediaItem() },
                enabled = isUsbMode && player.mediaItemCount > 1,
            ) {
                Icon(Icons.Filled.SkipPrevious, null, tint = if (isUsbMode) TabActive else TextSecondary)
            }
            IconButton(
                onClick = {
                    if (player.isPlaying) player.pause() else player.play()
                },
            ) {
                Icon(
                    if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    null,
                    tint = TabActive,
                    modifier = Modifier.size(36.dp),
                )
            }
            IconButton(
                onClick = { player.seekToNextMediaItem() },
                enabled = isUsbMode && player.mediaItemCount > 1,
            ) {
                Icon(Icons.Filled.SkipNext, null, tint = if (isUsbMode) TabActive else TextSecondary)
            }
        }
    }
}

@Composable
private fun AlbumArtThumb(uri: Uri?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF333333)),
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.CENTER_CROP } },
            modifier = Modifier.fillMaxSize(),
            update = { iv ->
                if (uri != null) {
                    Glide.with(iv).load(uri).centerCrop().into(iv)
                } else {
                    Glide.with(iv).clear(iv)
                    iv.setImageDrawable(null)
                }
            },
        )
        if (uri == null) {
            Icon(Icons.Filled.MusicNote, null, tint = TabActive, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun FaviconThumb(url: String?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Box(
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF333333)),
        contentAlignment = Alignment.Center,
    ) {
        if (!url.isNullOrBlank()) {
            AndroidView(
                factory = { ImageView(it).apply { scaleType = ImageView.ScaleType.CENTER_INSIDE } },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                update = { iv -> Glide.with(iv).load(url).into(iv) },
            )
        } else {
            Icon(Icons.Filled.MusicNote, null, tint = TabActive, modifier = Modifier.size(24.dp))
        }
    }
}

private fun formatDur(ms: Long): String {
    if (ms <= 0L) return "--:--"
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}

private fun formatDurMs(ms: Long): String {
    if (ms <= 0L) return "0:00"
    val s = ms / 1000
    return "%d:%02d".format(s / 60, s % 60)
}
