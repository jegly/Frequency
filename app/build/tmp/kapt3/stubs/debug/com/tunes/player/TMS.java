package com.tunes.player;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000~\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u0000 32\u00020\u00012\u00020\u0002:\u00013B\u0005\u00a2\u0006\u0002\u0010\u0003J\b\u0010\u0010\u001a\u00020\u0011H\u0002J\b\u0010\u0012\u001a\u00020\u0011H\u0016J\b\u0010\u0013\u001a\u00020\u0011H\u0016J$\u0010\u0014\u001a\u0004\u0018\u00010\u00152\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00192\b\u0010\u001a\u001a\u0004\u0018\u00010\u001bH\u0016J(\u0010\u001c\u001a\u00020\u00112\u0006\u0010\u001d\u001a\u00020\u00172\u0016\u0010\u001e\u001a\u0012\u0012\n\u0012\b\u0012\u0004\u0012\u00020!0 0\u001fR\u00020\u0001H\u0016J\u0010\u0010\"\u001a\u00020\u00112\u0006\u0010#\u001a\u00020$H\u0016J\b\u0010%\u001a\u00020\u0011H\u0016J\u0010\u0010&\u001a\u00020\u00112\u0006\u0010\'\u001a\u00020(H\u0016J\b\u0010)\u001a\u00020\u0011H\u0016J\"\u0010*\u001a\u00020\u00192\b\u0010+\u001a\u0004\u0018\u00010,2\u0006\u0010-\u001a\u00020\u00192\u0006\u0010.\u001a\u00020\u0019H\u0016J\b\u0010/\u001a\u00020\u0011H\u0016J\b\u00100\u001a\u00020\u0011H\u0016J\u0012\u00101\u001a\u00020\u00112\b\u00102\u001a\u0004\u0018\u00010,H\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0006\u001a\u0004\u0018\u00010\u0007X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u00064"}, d2 = {"Lcom/tunes/player/TMS;", "Landroid/service/media/MediaBrowserService;", "Lcom/tunes/player/playback/PlaybackManager$PlaybackServiceCallback;", "()V", "isServiceRunning", "", "mMediaSession", "Landroid/media/session/MediaSession;", "mNotificationManager", "Lcom/tunes/player/playback/MediaNotificationManager;", "mPlaybackManager", "Lcom/tunes/player/playback/PlaybackManager;", "mServiceThread", "Landroid/os/HandlerThread;", "mWorkerHandler", "Landroid/os/Handler;", "createStartupChannel", "", "onCreate", "onDestroy", "onGetRoot", "Landroid/service/media/MediaBrowserService$BrowserRoot;", "clientPackageName", "", "clientUid", "", "rootHints", "Landroid/os/Bundle;", "onLoadChildren", "parentId", "result", "Landroid/service/media/MediaBrowserService$Result;", "", "Landroid/media/browse/MediaBrowser$MediaItem;", "onMetaDataChanged", "newMetaData", "Landroid/media/MediaMetadata;", "onPlaybackStart", "onPlaybackStateChanged", "newState", "Landroid/media/session/PlaybackState;", "onPlaybackStopped", "onStartCommand", "intent", "Landroid/content/Intent;", "flags", "startId", "onStartNotification", "onStopNotification", "onTaskRemoved", "rootIntent", "Companion", "app_debug"})
public final class TMS extends android.service.media.MediaBrowserService implements com.tunes.player.playback.PlaybackManager.PlaybackServiceCallback {
    @org.jetbrains.annotations.Nullable()
    private android.media.session.MediaSession mMediaSession;
    @org.jetbrains.annotations.Nullable()
    private com.tunes.player.playback.MediaNotificationManager mNotificationManager;
    @org.jetbrains.annotations.Nullable()
    private com.tunes.player.playback.PlaybackManager mPlaybackManager;
    private boolean isServiceRunning = false;
    @org.jetbrains.annotations.Nullable()
    private android.os.HandlerThread mServiceThread;
    @org.jetbrains.annotations.Nullable()
    private android.os.Handler mWorkerHandler;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String TAG = "TMS";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "tunes_service_channel";
    private static final int NOTIFICATION_ID = 412;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_PLAY = "com.tunes.player.ACTION_PLAY";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_PAUSE = "com.tunes.player.ACTION_PAUSE";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_STOP = "com.tunes.player.ACTION_STOP";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_NEXT = "com.tunes.player.ACTION_NEXT";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String ACTION_PREV = "com.tunes.player.ACTION_PREV";
    @org.jetbrains.annotations.NotNull()
    public static final com.tunes.player.TMS.Companion Companion = null;
    
    public TMS() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.Nullable()
    public android.service.media.MediaBrowserService.BrowserRoot onGetRoot(@org.jetbrains.annotations.NotNull()
    java.lang.String clientPackageName, int clientUid, @org.jetbrains.annotations.Nullable()
    android.os.Bundle rootHints) {
        return null;
    }
    
    @java.lang.Override()
    public void onLoadChildren(@org.jetbrains.annotations.NotNull()
    java.lang.String parentId, @org.jetbrains.annotations.NotNull()
    android.service.media.MediaBrowserService.Result<java.util.List<android.media.browse.MediaBrowser.MediaItem>> result) {
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    @java.lang.Override()
    public int onStartCommand(@org.jetbrains.annotations.Nullable()
    android.content.Intent intent, int flags, int startId) {
        return 0;
    }
    
    private final void createStartupChannel() {
    }
    
    @java.lang.Override()
    public void onPlaybackStart() {
    }
    
    @java.lang.Override()
    public void onPlaybackStopped() {
    }
    
    @java.lang.Override()
    public void onStartNotification() {
    }
    
    @java.lang.Override()
    public void onStopNotification() {
    }
    
    @java.lang.Override()
    public void onPlaybackStateChanged(@org.jetbrains.annotations.NotNull()
    android.media.session.PlaybackState newState) {
    }
    
    @java.lang.Override()
    public void onMetaDataChanged(@org.jetbrains.annotations.NotNull()
    android.media.MediaMetadata newMetaData) {
    }
    
    @java.lang.Override()
    public void onTaskRemoved(@org.jetbrains.annotations.Nullable()
    android.content.Intent rootIntent) {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/tunes/player/TMS$Companion;", "", "()V", "ACTION_NEXT", "", "ACTION_PAUSE", "ACTION_PLAY", "ACTION_PREV", "ACTION_STOP", "CHANNEL_ID", "NOTIFICATION_ID", "", "TAG", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}