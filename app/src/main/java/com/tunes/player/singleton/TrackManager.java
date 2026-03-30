package com.tunes.player.singleton;

import android.content.Context;

import com.tunes.player.model.MusicModel;
import com.tunes.player.playback.PlaybackManager;
import com.tunes.player.utils.PlaylistStorageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrackManager {

    private static final TrackManager ourInstance = new TrackManager();
    private List<MusicModel> mActiveList = new ArrayList<>();
    private List<MusicModel> mMainList;
    private int mIndex = -1;
    private int mActiveListSize = -1;
    private MusicModel mDeletedQueueItem;
    private int mDeletedQueueIndex;
    private boolean mRepeatCurrentTrack = false;
    private boolean mShuffle = false;
    private List<MusicModel> mOriginalList = new ArrayList<>();

    private TrackManager() {
    }

    public static TrackManager getInstance() {
        return ourInstance;
    }

    public void setMainList(final List<MusicModel> mainList) {
        mMainList = mainList;
    }

    public List<MusicModel> getMainList() {
        return mMainList;
    }

    public void buildDataList(List<MusicModel> newList, int index) {
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
                setActiveIndex(0);
            }
        } else {
            setActiveIndex(index);
        }
    }

    private void setActiveIndex(int index) {
        mIndex = index;
    }

    public int getActiveIndex() {
        return mIndex;
    }

    public List<MusicModel> getActiveQueue() {
        return mActiveList;
    }

    public MusicModel getActiveQueueItem() {
        if (mIndex >= 0 && mIndex < mActiveList.size()) {
            return mActiveList.get(mIndex);
        }
        return null;
    }

    public void updateActiveQueue(int from, int to) {
        Collections.swap(mActiveList, from, to);
    }

    public void repeatCurrentTrack(boolean b) {
        mRepeatCurrentTrack = b;
    }

    public boolean isCurrentTrackInRepeatMode() {
        return mRepeatCurrentTrack;
    }

    public void setShuffle(boolean shuffle) {
        if (this.mShuffle == shuffle) return;
        this.mShuffle = shuffle;
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
                        if (mActiveList.get(i).getId() == current.getId()) {
                            mIndex = i;
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean isShuffleEnabled() {
        return mShuffle;
    }

    public boolean canSkipTrack(short direction) {
        if (mRepeatCurrentTrack) {
            mRepeatCurrentTrack = false;
            return true;
        }
        if (direction == PlaybackManager.ACTION_PLAY_NEXT) {
            if (mIndex < mActiveListSize - 1) {
                setActiveIndex(++mIndex);
                return true;
            } else if (mShuffle) { // In shuffle mode, loop back to start and reshuffle
                MusicModel current = getActiveQueueItem();
                if (current != null) {
                    mActiveList.remove(mIndex);
                    Collections.shuffle(mActiveList);
                    mActiveList.add(0, current);
                    mIndex = 0;
                    if (mActiveListSize > 1) {
                        mIndex = 1;
                        return true;
                    }
                }
            }
        } else if (direction == PlaybackManager.ACTION_PLAY_PREV && mIndex > 0) {
            setActiveIndex(--mIndex);
            return true;
        }
        return false;
    }

    public void playNext(MusicModel md) {
        if (mIndex + 1 < mActiveListSize) {
            if (mActiveList.get(mIndex + 1).getId() != md.getId()) {
                mActiveList.add(mIndex + 1, md);
                mActiveListSize = mActiveList.size();
            }
        } else {
            mActiveList.add(mIndex + 1, md);
            mActiveListSize = mActiveList.size();
        }
    }

    public void addToActiveQueue(MusicModel md) {
        mActiveList.add(md);
        mActiveListSize = mActiveList.size();
    }

    public boolean canRemoveItem(int position) {
        return position > -1 && position < mActiveListSize;
    }

    public void removeItemFromActiveQueue(int position) {
        if (position >= 0 && position < mActiveList.size()) {
            mDeletedQueueIndex = position;
            mDeletedQueueItem = mActiveList.remove(position);
            mActiveListSize = mActiveList.size();
            if (position < mIndex) {
                mIndex--;
            }
        }
    }

    public void restoreItem() {
        if (mDeletedQueueItem != null) {
            mActiveList.add(mDeletedQueueIndex, mDeletedQueueItem);
            mActiveListSize = mActiveList.size();
        }
    }

    public void addToHistory(Context context) {
        MusicModel current = getActiveQueueItem();
        if (current != null) {
            PlaylistStorageManager.addToRecentTracks(context, current);
        }
    }
}
