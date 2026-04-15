package com.tunes.player.playback;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0007\u001a\u00020\bH\u0002J\u0018\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0016\u0010\u000f\u001a\u00020\b2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u0006\u0010\u0010\u001a\u00020\bR\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/tunes/player/playback/MediaNotificationManager;", "", "service", "Lcom/tunes/player/TMS;", "(Lcom/tunes/player/TMS;)V", "notificationManager", "Landroid/app/NotificationManager;", "createChannel", "", "createNotification", "Landroid/app/Notification;", "session", "Landroid/media/session/MediaSession;", "isPlaying", "", "startNotification", "stopNotification", "Companion", "app_release"})
public final class MediaNotificationManager {
    @org.jetbrains.annotations.NotNull()
    private final com.tunes.player.TMS service = null;
    @org.jetbrains.annotations.NotNull()
    private final android.app.NotificationManager notificationManager = null;
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String CHANNEL_ID = "tunes_playback_channel";
    private static final int NOTIFICATION_ID = 412;
    @org.jetbrains.annotations.NotNull()
    public static final com.tunes.player.playback.MediaNotificationManager.Companion Companion = null;
    
    public MediaNotificationManager(@org.jetbrains.annotations.NotNull()
    com.tunes.player.TMS service) {
        super();
    }
    
    private final void createChannel() {
    }
    
    public final void startNotification(@org.jetbrains.annotations.NotNull()
    android.media.session.MediaSession session, boolean isPlaying) {
    }
    
    public final void stopNotification() {
    }
    
    private final android.app.Notification createNotification(android.media.session.MediaSession session, boolean isPlaying) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Lcom/tunes/player/playback/MediaNotificationManager$Companion;", "", "()V", "CHANNEL_ID", "", "NOTIFICATION_ID", "", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}