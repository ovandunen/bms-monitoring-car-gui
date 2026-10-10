package com.fleet.ecocar.volume

/** Desktop has no AudioManager. The bar still moves inside 0..15. */
class DesktopMusicVolume : MusicVolumeControl {
    private var index: Int = 8

    override fun read(): MusicVolumeLevel = MusicVolumeLevel(index, MAX_INDEX)

    override fun setIndex(index: Int) {
        this.index = index.coerceIn(0, MAX_INDEX)
    }

    override fun observe(onChange: (MusicVolumeLevel) -> Unit): MusicVolumeSubscription =
        MusicVolumeSubscription {}

    private companion object {
        const val MAX_INDEX = 15
    }
}
