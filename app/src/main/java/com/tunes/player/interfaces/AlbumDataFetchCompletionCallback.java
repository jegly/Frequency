package com.tunes.player.interfaces;


import com.tunes.player.model.AlbumModel;

import java.util.List;

public interface AlbumDataFetchCompletionCallback {

    void onTaskComplete(List<AlbumModel> data);
}
