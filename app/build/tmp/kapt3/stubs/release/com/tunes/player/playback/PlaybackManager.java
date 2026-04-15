package com.tunes.player.playback;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 !2\u00020\u0001:\u0002!\"B%\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u00a2\u0006\u0002\u0010\nJ\b\u0010\u0011\u001a\u00020\u0012H\u0002J\b\u0010\u0013\u001a\u00020\u0012H\u0002J\b\u0010\u0014\u001a\u00020\u0012H\u0002J\b\u0010\u0015\u001a\u00020\u0012H\u0002J\b\u0010\u0016\u001a\u00020\u0012H\u0002J\u0010\u0010\u0017\u001a\u00020\u00122\u0006\u0010\u0018\u001a\u00020\u0019H\u0016J\b\u0010\u001a\u001a\u00020\u0012H\u0016J\u0010\u0010\u001b\u001a\u00020\u00122\u0006\u0010\u001c\u001a\u00020\u001dH\u0016J\u0010\u0010\u001e\u001a\u00020\u00122\u0006\u0010\u001f\u001a\u00020 H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\r\u001a\u00020\u000e8F\u00a2\u0006\u0006\u001a\u0004\b\u000f\u0010\u0010R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/tunes/player/playback/PlaybackManager;", "Lcom/tunes/player/playback/Playback$Callback;", "context", "Landroid/content/Context;", "playback", "Lcom/tunes/player/playback/Playback;", "trackManager", "Lcom/tunes/player/singleton/TrackManager;", "serviceCallback", "Lcom/tunes/player/playback/PlaybackManager$PlaybackServiceCallback;", "(Landroid/content/Context;Lcom/tunes/player/playback/Playback;Lcom/tunes/player/singleton/TrackManager;Lcom/tunes/player/playback/PlaybackManager$PlaybackServiceCallback;)V", "lastKnownPath", "", "sessionCallbacks", "Landroid/media/session/MediaSession$Callback;", "getSessionCallbacks", "()Landroid/media/session/MediaSession$Callback;", "handlePause", "", "handlePlay", "handleSkipToNext", "handleSkipToPrev", "handleStop", "onFocusChanged", "resumePlayback", "", "onPlaybackCompletion", "onPlaybackStateChanged", "state", "", "publishMetadata", "md", "Lcom/tunes/player/model/MusicModel;", "Companion", "PlaybackServiceCallback", "app_release"})
public final class PlaybackManager implements com.tunes.player.playback.Playback.Callback {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tunes.player.playback.Playback playback = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tunes.player.singleton.TrackManager trackManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tunes.player.playback.PlaybackManager.PlaybackServiceCallback serviceCallback = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METADATA_TITLE_KEY = "android.media.metadata.TITLE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METADATA_ARTIST_KEY = "android.media.metadata.ARTIST";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METADATA_ALBUM_KEY = "android.media.metadata.ALBUM";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METADATA_ALBUM_ART = "android.media.metadata.ALBUM_ART";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String METADATA_DURATION_KEY = "android.media.metadata.DURATION";
    public static final short ACTION_PLAY_NEXT = (short)1;
    public static final short ACTION_PLAY_PREV = (short)-1;
    @org.jetbrains.annotations.NotNull()
    private java.lang.String lastKnownPath = "";
    @org.jetbrains.annotations.NotNull()
    public static final com.tunes.player.playback.PlaybackManager.Companion Companion = null;
    
    public PlaybackManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.tunes.player.playback.Playback playback, @org.jetbrains.annotations.NotNull()
    com.tunes.player.singleton.TrackManager trackManager, @org.jetbrains.annotations.NotNull()
    com.tunes.player.playback.PlaybackManager.PlaybackServiceCallback serviceCallback) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final android.media.session.MediaSession.Callback getSessionCallbacks() {
        return null;
    }
    
    private final void handlePlay() {
    }
    
    private final void handlePause() {
    }
    
    private final void handleStop() {
    }
    
    private final void handleSkipToNext() {
    }
    
    private final void handleSkipToPrev() {
    }
    
    private final void publishMetadata(com.tunes.player.model.MusicModel md) {
    }
    
    @java.lang.Override()
    public void onPlaybackCompletion() {
    }
    
    @java.lang.Override()
    public void onPlaybackStateChanged(int state) {
    }
    
    @java.lang.Override()
    public void onFocusChanged(boolean resumePlayback) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\n\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/tunes/player/playback/PlaybackManager$Companion;", "", "()V", "ACTION_PLAY_NEXT", "", "ACTION_PLAY_PREV", "METADATA_ALBUM_ART", "", "METADATA_ALBUM_KEY", "METADATA_ARTIST_KEY", "METADATA_DURATION_KEY", "METADATA_TITLE_KEY", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0003H&J\u0010\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\u0003H&J\b\u0010\u000b\u001a\u00020\u0003H&J\b\u0010\f\u001a\u00020\u0003H&\u00a8\u0006\r"}, d2 = {"Lcom/tunes/player/playback/PlaybackManager$PlaybackServiceCallback;", "", "onMetaDataChanged", "", "newMetaData", "Landroid/media/MediaMetadata;", "onPlaybackStart", "onPlaybackStateChanged", "newState", "Landroid/media/session/PlaybackState;", "onPlaybackStopped", "onStartNotification", "onStopNotification", "app_release"})
    public static abstract interface PlaybackServiceCallback {
        
        public abstract void onPlaybackStart();
        
        public abstract void onPlaybackStopped();
        
        public abstract void onStartNotification();
        
        public abstract void onStopNotification();
        
        public abstract void onPlaybackStateChanged(@org.jetbrains.annotations.NotNull()
        android.media.session.PlaybackState newState);
        
        public abstract void onMetaDataChanged(@org.jetbrains.annotations.NotNull()
        android.media.MediaMetadata newMetaData);
    }
}