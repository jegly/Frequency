package com.jegly.frequency.singleton

import android.content.Context
import com.jegly.frequency.model.MusicModel
import com.jegly.frequency.utils.PlaylistStorageManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*

class TrackManager private constructor() {

    private val _activeList = MutableStateFlow<List<MusicModel>>(emptyList())
    val activeList: StateFlow<List<MusicModel>> = _activeList.asStateFlow()

    private val _currentTrack = MutableStateFlow<MusicModel?>(null)
    val currentTrack: StateFlow<MusicModel?> = _currentTrack.asStateFlow()

    private val mMainList = mutableListOf<MusicModel>()
    private val mMainListPaths = mutableSetOf<String>()

    private var mIndex = -1
    private var mDeletedQueueItem: MusicModel? = null
    private var mDeletedQueueIndex: Int = -1
    
    var repeatCurrentTrack = false
    var isInfiniteLoopEnabled = false
    var isShuffleEnabled = false
        set(value) {
            if (field == value) return
            field = value
            updateShuffle()
        }

    private val mOriginalList = mutableListOf<MusicModel>()
    private val scope = CoroutineScope(Dispatchers.IO)

    companion object {
        val instance: TrackManager by lazy { TrackManager() }
        const val ACTION_PLAY_NEXT: Short = 1
        const val ACTION_PLAY_PREV: Short = 2
    }

    @Synchronized
    fun setMainList(mainList: List<MusicModel>?) {
        if (mainList == null) return
        mMainList.clear()
        mMainListPaths.clear()
        mainList.forEach { m ->
            if (m.songPath != null && mMainListPaths.add(m.songPath)) {
                mMainList.add(m)
            }
        }
    }

    @Synchronized
    fun addImportedTracks(context: Context, tracks: List<MusicModel>?) {
        if (tracks.isNullOrEmpty()) return
        val actuallyAdded = mutableListOf<MusicModel>()
        tracks.forEach { m ->
            if (m.songPath != null && mMainListPaths.add(m.songPath)) {
                mMainList.add(m)
                actuallyAdded.add(m)
            }
        }
        if (actuallyAdded.isNotEmpty()) {
            scope.launch {
                PlaylistStorageManager.saveImportedTracks(context, actuallyAdded)
            }
        }
    }

    fun getMainList(): List<MusicModel> = mMainList.toList()

    fun buildDataList(newList: List<MusicModel>, index: Int) {
        val list = newList.toMutableList()
        if (isShuffleEnabled) {
            mOriginalList.clear()
            mOriginalList.addAll(list)
            if (index in list.indices) {
                val current = list.removeAt(index)
                list.shuffle()
                list.add(0, current)
                mIndex = 0
            }
        } else {
            mIndex = index
        }
        _activeList.value = list
        updateCurrentTrack()
    }

    private fun updateCurrentTrack() {
        val list = _activeList.value
        _currentTrack.value = if (mIndex in list.indices) list[mIndex] else null
    }

    fun getActiveQueueItem(): MusicModel? = _currentTrack.value

    fun getActiveIndex(): Int = mIndex

    private fun updateShuffle() {
        val currentList = _activeList.value.toMutableList()
        if (isShuffleEnabled) {
            if (currentList.isNotEmpty()) {
                mOriginalList.clear()
                mOriginalList.addAll(currentList)
                val current = getActiveQueueItem()
                if (current != null) {
                    currentList.removeAt(mIndex)
                    currentList.shuffle()
                    currentList.add(0, current)
                    mIndex = 0
                }
            }
        } else {
            if (mOriginalList.isNotEmpty()) {
                val current = getActiveQueueItem()
                currentList.clear()
                currentList.addAll(mOriginalList)
                if (current != null) {
                    mIndex = currentList.indexOfFirst { it.songPath == current.songPath }
                }
            }
        }
        _activeList.value = currentList
        updateCurrentTrack()
    }

    fun canSkipTrack(direction: Short): Boolean {
        if (isInfiniteLoopEnabled) return true
        if (repeatCurrentTrack) {
            repeatCurrentTrack = false
            return true
        }
        val list = _activeList.value
        if (direction == ACTION_PLAY_NEXT) {
            if (mIndex < list.size - 1) {
                mIndex++
                updateCurrentTrack()
                return true
            }
        } else if (direction == ACTION_PLAY_PREV) {
            if (mIndex > 0) {
                mIndex--
            } else if (isShuffleEnabled && list.isNotEmpty()) {
                mIndex = list.size - 1
            } else {
                return false
            }
            updateCurrentTrack()
            return true
        }
        return false
    }

    fun playNext(md: MusicModel) {
        val list = _activeList.value.toMutableList()
        if (mIndex + 1 < list.size) {
            list.add(mIndex + 1, md)
        } else {
            list.add(md)
        }
        _activeList.value = list
    }

    fun addToActiveQueue(md: MusicModel) {
        val list = _activeList.value.toMutableList()
        list.add(md)
        _activeList.value = list
    }

    fun removeItemFromActiveQueue(position: Int) {
        val list = _activeList.value.toMutableList()
        if (position in list.indices) {
            mDeletedQueueIndex = position
            mDeletedQueueItem = list.removeAt(position)
            if (position < mIndex) {
                mIndex--
            }
            _activeList.value = list
            updateCurrentTrack()
        }
    }

    fun restoreItem() {
        mDeletedQueueItem?.let { item ->
            val list = _activeList.value.toMutableList()
            list.add(mDeletedQueueIndex.coerceIn(0, list.size), item)
            _activeList.value = list
            updateCurrentTrack()
        }
    }

    fun updateActiveQueue(from: Int, to: Int) {
        val list = _activeList.value.toMutableList()
        if (from in list.indices && to in list.indices) {
            Collections.swap(list, from, to)
            if (mIndex == from) mIndex = to
            else if (mIndex == to) mIndex = from
            _activeList.value = list
            updateCurrentTrack()
        }
    }

    fun addToHistory(context: Context) {
        getActiveQueueItem()?.let {
            scope.launch {
                PlaylistStorageManager.addToRecentTracks(context, it)
            }
        }
    }
}
