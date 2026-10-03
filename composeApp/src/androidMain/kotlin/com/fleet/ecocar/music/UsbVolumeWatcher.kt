package com.fleet.ecocar.music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

/**
 * Signals when a USB volume is mounted or removed, or when MediaStore audio changes.
 * Events are collapsed to one update after [DEBOUNCE_MS] of quiet.
 */
@OptIn(FlowPreview::class)
class UsbVolumeWatcher(
    private val context: Context,
    private val scope: CoroutineScope,
    private val debounceMs: Long = DEBOUNCE_MS,
) {
    private val _changes = MutableStateFlow(0L)
    val changes: StateFlow<Long> = _changes.asStateFlow()

    private val events = MutableSharedFlow<Unit>(extraBufferCapacity = 64)
    private var collectJob: Job? = null

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            events.tryEmit(Unit)
        }
    }

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            events.tryEmit(Unit)
        }

        override fun onChange(selfChange: Boolean, uri: Uri?) {
            events.tryEmit(Unit)
        }
    }

    fun start() {
        if (collectJob != null) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_MEDIA_MOUNTED)
            addAction(Intent.ACTION_MEDIA_UNMOUNTED)
            addAction(Intent.ACTION_MEDIA_EJECT)
            addAction(Intent.ACTION_MEDIA_REMOVED)
            addAction(Intent.ACTION_MEDIA_BAD_REMOVAL)
            addDataScheme("file")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            observer,
        )
        collectJob = scope.launch {
            events.debounce(debounceMs).collect {
                _changes.value = _changes.value + 1
            }
        }
    }

    fun stop() {
        collectJob?.cancel()
        collectJob = null
        runCatching { context.unregisterReceiver(receiver) }
        runCatching { context.contentResolver.unregisterContentObserver(observer) }
    }

    companion object {
        const val DEBOUNCE_MS = 2_000L
    }
}
