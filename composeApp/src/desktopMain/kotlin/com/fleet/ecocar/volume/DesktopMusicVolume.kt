package com.fleet.ecocar.volume

/** Desktop has no AudioManager. The bar still moves inside 0..15. */
class DesktopMusicVolume : MusicVolumeControl {
    private var media = MediaVolume(index = 8, maxIndex = MAX_INDEX, muted = false, restoreIndex = 8)

    override fun read(): MusicVolumeLevel = media.toLevel()

    override fun setIndex(index: Int) {
        media = media.withChosenIndex(index)
    }

    override fun toggleMute() {
        media = media.toggleMute()
    }

    override fun observe(onChange: (MusicVolumeLevel) -> Unit): MusicVolumeSubscription =
        MusicVolumeSubscription {}

    private companion object {
        const val MAX_INDEX = 15
    }
}
