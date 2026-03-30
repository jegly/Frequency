package com.tunes.player.model;

import java.io.Serializable;

public class MusicModel implements Serializable {

    private static final long serialVersionUID = 1L;

    private int mId;
    private String mSongName;
    private String mArtist;
    private String mAlbum;
    private String mSongPath;
    private String mAlbumArtUrl;
    private long mDuration;
    private long mDateAdded;

    public MusicModel(int id, String songName, String artist, String album,
                      String songPath, String albumArtUrl, long duration, long dateAdded) {
        this.mId = id;
        this.mSongName = songName;
        this.mArtist = artist;
        this.mAlbum = album;
        this.mSongPath = songPath;
        this.mAlbumArtUrl = albumArtUrl;
        this.mDuration = duration;
        this.mDateAdded = dateAdded;
    }

    public int getId() {
        return mId;
    }

    public String getSongName() {
        return mSongName;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public String getSongPath() {
        return mSongPath;
    }

    public void setSongPath(String mSongPath) {
        this.mSongPath = mSongPath;
    }

    public String getAlbumArtUrl() {
        return mAlbumArtUrl;
    }

    public void setAlbumArtUrl(String mAlbumArtUrl) {
        this.mAlbumArtUrl = mAlbumArtUrl;
    }

    public long getDuration() {
        return mDuration;
    }

    public long getDateAdded() {
        return mDateAdded;
    }
}
