package com.tunes.player.interfaces;

import com.tunes.player.model.AlbumModel;
import com.tunes.player.model.ArtistModel;
import com.tunes.player.model.MusicModel;

import java.util.List;

public interface AsyncTaskCallback {

    interface Simple {
        void onTaskComplete(List<MusicModel> list);
    }

    interface UpdateCompletion {
        void onUpdateComplete(List<MusicModel> list, boolean isEdited);
    }

    interface AlbumTask {
        void onTaskComplete(List<AlbumModel> data);
    }

    interface ArtistTask {
        void onTaskComplete(List<ArtistModel> data);
    }
}
