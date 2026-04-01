package com.tunes.player.singleton;

import android.content.Context;
import com.tunes.player.model.MusicModel;
import com.tunes.player.playback.PlaybackManager;
import com.tunes.player.utils.PlaylistStorageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TrackManager {

    private static final TrackManager ourInstance = new TrackManager();
    private final List<MusicModel> mActiveList = new ArrayList<>();
    private final List<MusicModel> mMainList = new ArrayList<>();
    private final Set<String> mMainListPaths = new HashSet<>();
    
    private int mIndex = -1;
    private int mActiveListSize = -1;
    private MusicModel mDeletedQueueItem;
    private int mDeletedQueueIndex;
    private boolean mRepeatCurrentTrack = false;
    private boolean mInfiniteLoop = false;
    private boolean mShuffle = false;
    private final List<MusicModel> mOriginalList = new ArrayList<>();

    private TrackManager() {
    }

    public static TrackManager getInstance() {
        return ourInstance;
    }

    public synchronized void setMainList(List<MusicModel> mainList) {
        if (mainList == null) return;
        mMainList.clear();
        mMainListPaths.clear();
        for (MusicModel m : mainList) {
            if (m.getSongPath() != null && mMainListPaths.add(m.getSongPath())) {
                mMainList.add(m);
            }
        }
    }

    public synchronized void addImportedTracks(Context context, List<MusicModel> tracks) {
        if (tracks == null || tracks.isEmpty()) return;
        List<MusicModel> actuallyAdded = new ArrayList<>();
        for (MusicModel m : tracks) {
            if (m.getSongPath() != null && mMainListPaths.add(m.getSongPath())) {
                mMainList.add(m);
                actuallyAdded.add(m);
            }
        }
        if (!actuallyAdded.isEmpty()) {
            PlaylistStorageManager.saveImportedTracks(context, actuallyAdded);
        }
    }

    public synchronized List<MusicModel> getMainList() {
        return new ArrayList<>(mMainList);
    }

    public void buildDataList(List<MusicModel> newList, int index) {
        synchronized (mActiveList) {
            mActiveList.clear();
            mActiveList.addAll(newList);
            mActiveListSize = mActiveList.size();
            
            if (mShuffle) {
                mOriginalList.clear();
                mOriginalList.addAll(mActiveList);
                if (index >= 0 && index < mActiveList.size()) {
                    MusicModel current = mActiveList.get(index);
                    mActiveList.remove(index);
                    Collections.shuffle(mActiveList);
                    mActiveList.add(0, current);
                    mIndex = 0;
                }
            } else {
                mIndex = index;
            }
        }
    }

    public MusicModel getActiveQueueItem() {
        synchronized (mActiveList) {
            if (mIndex >= 0 && mIndex < mActiveList.size()) {
                return mActiveList.get(mIndex);
            }
        }
        return null;
    }

    public int getActiveIndex() {
        return mIndex;
    }

    public boolean isShuffleEnabled() {
        return mShuffle;
    }

    public void setShuffle(boolean shuffle) {
        if (this.mShuffle == shuffle) return;
        this.mShuffle = shuffle;
        synchronized (mActiveList) {
            if (mShuffle) {
                if (mActiveList.size() > 0) {
                    mOriginalList.clear();
                    mOriginalList.addAll(mActiveList);
                    MusicModel current = getActiveQueueItem();
                    if (current != null) {
                        mActiveList.remove(mIndex);
                        Collections.shuffle(mActiveList);
                        mActiveList.add(0, current);
                        mIndex = 0;
                    }
                }
            } else {
                if (!mOriginalList.isEmpty()) {
                    MusicModel current = getActiveQueueItem();
                    mActiveList.clear();
                    mActiveList.addAll(mOriginalList);
                    mActiveListSize = mActiveList.size();
                    if (current != null) {
                        for (int i = 0; i < mActiveList.size(); i++) {
                            if (mActiveList.get(i).getSongPath().equals(current.getSongPath())) {
                                mIndex = i;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public void repeatCurrentTrack(boolean b) {
        mRepeatCurrentTrack = b;
    }

    public boolean isCurrentTrackInRepeatMode() {
        return mRepeatCurrentTrack;
    }

    public void setInfiniteLoop(boolean b) {
        mInfiniteLoop = b;
    }

    public boolean isInfiniteLoopEnabled() {
        return mInfiniteLoop;
    }

    public boolean canSkipTrack(short direction) {
        if (mInfiniteLoop) {
            return true;
        }
        if (mRepeatCurrentTrack) {
            mRepeatCurrentTrack = false;
            return true;
        }
        synchronized (mActiveList) {
            if (direction == PlaybackManager.ACTION_PLAY_NEXT) {
                if (mIndex < mActiveListSize - 1) {
                    mIndex++;
                    return true;
                }
            } else if (direction == PlaybackManager.ACTION_PLAY_PREV && mIndex > 0) {
                mIndex--;
                return true;
            }
        }
        return false;
    }

    public void playNext(MusicModel md) {
        synchronized (mActiveList) {
            if (mIndex + 1 < mActiveList.size()) {
                mActiveList.add(mIndex + 1, md);
            } else {
                mActiveList.add(md);
            }
            mActiveListSize = mActiveList.size();
        }
    }

    public void addToActiveQueue(MusicModel md) {
        synchronized (mActiveList) {
            mActiveList.add(md);
            mActiveListSize = mActiveList.size();
        }
    }

    public boolean canRemoveItem(int position) {
        return position >= 0 && position < mActiveListSize;
    }

    public void removeItemFromActiveQueue(int position) {
        synchronized (mActiveList) {
            if (position >= 0 && position < mActiveList.size()) {
                mDeletedQueueIndex = position;
                mDeletedQueueItem = mActiveList.remove(position);
                mActiveListSize = mActiveList.size();
                if (position < mIndex) {
                    mIndex--;
                }
            }
        }
    }

    public void restoreItem() {
        synchronized (mActiveList) {
            if (mDeletedQueueItem != null) {
                mActiveList.add(mDeletedQueueIndex, mDeletedQueueItem);
                mActiveListSize = mActiveList.size();
            }
        }
    }

    public void updateActiveQueue(int from, int to) {
        synchronized (mActiveList) {
            if (from >= 0 && from < mActiveList.size() && to >= 0 && to < mActiveList.size()) {
                Collections.swap(mActiveList, from, to);
                if (mIndex == from) mIndex = to;
                else if (mIndex == to) mIndex = from;
            }
        }
    }

    public void addToHistory(Context context) {
        MusicModel current = getActiveQueueItem();
        if (current != null) {
            PlaylistStorageManager.addToRecentTracks(context, current);
        }
    }

    public List<MusicModel> getActiveQueue() {
        synchronized (mActiveList) {
            return new ArrayList<>(mActiveList);
        }
    }
}
