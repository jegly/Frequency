package com.tunes.player.interfaces;

import com.tunes.player.model.ArtistModel;

import java.util.List;

public interface ArtistDataFetchCompletionCallback {

    void onTaskComplete(List<ArtistModel> data);
}
