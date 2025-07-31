package com.mono.music.presentation.player

object PlayerSourceInfo {
    private var sourceId: Long? = null
    private var sourceType: String? = null
    private var sourceName: String? = null
    private var lastTrackId: Long? = null

    fun getSourceId(): Long? = sourceId
    fun getSourceType(): String? = sourceType
    fun getSourceName(): String? = sourceName

    fun setSourceInfo(id: Long?, type: String?, name: String?, trackId: Long?) {
        if (id != null && type != null && name != null && trackId != null) {
            sourceId = id
            sourceType = type
            sourceName = name
            lastTrackId = trackId
        } else {
            clear()
        }
    }

    fun checkAndClearIfNeeded(currentTrackId: Long?): Boolean {
        // If track IDs don't match, source info is stale
        if (currentTrackId != null && currentTrackId != lastTrackId) {
            clear()
            return true
        }
        return false
    }

    // Optional: Add a clear method
    fun clear() {
        sourceId = null
        sourceType = null
        sourceName = null
        lastTrackId = null
    }
}