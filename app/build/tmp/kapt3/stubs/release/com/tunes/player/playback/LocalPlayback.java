package com.tunes.player.playback;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\r\n\u0002\u0018\u0002\n\u0002\b\f\u0018\u00002\u00020\u00012\u00020\u00022\u00020\u00032\u00020\u0004B!\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\u0002\u0010\u000bJ\b\u0010\"\u001a\u00020#H\u0002J\b\u0010$\u001a\u00020#H\u0002J\b\u0010%\u001a\u00020\u001aH\u0016J\b\u0010&\u001a\u00020\u001aH\u0016J\b\u0010\'\u001a\u00020\u0013H\u0016J\b\u0010(\u001a\u00020#H\u0002J\b\u0010)\u001a\u00020\u0015H\u0016J\u0010\u0010*\u001a\u00020#2\u0006\u0010+\u001a\u00020\u0013H\u0016J\u0010\u0010,\u001a\u00020#2\u0006\u0010-\u001a\u00020\u001cH\u0016J\b\u0010.\u001a\u00020#H\u0016J\u001a\u0010/\u001a\u00020#2\b\u00100\u001a\u0004\u0018\u0001012\u0006\u00102\u001a\u00020\u0015H\u0016J\u0010\u00103\u001a\u00020#2\u0006\u0010-\u001a\u00020\u001cH\u0016J\u0010\u00104\u001a\u00020#2\u0006\u00105\u001a\u00020\u001aH\u0016J\u0010\u00106\u001a\u00020#2\u0006\u0010\"\u001a\u00020\u0015H\u0016J\b\u00107\u001a\u00020#H\u0002J\b\u00108\u001a\u00020#H\u0002J\u0006\u00109\u001a\u00020#J\u0010\u0010:\u001a\u00020#2\u0006\u0010;\u001a\u00020\u001eH\u0016J\b\u0010<\u001a\u00020\u0015H\u0002R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\t\u001a\u0004\u0018\u00010\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0015X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u001aX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001b\u001a\u0004\u0018\u00010\u001cX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001d\u001a\u0004\u0018\u00010\u001eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001f\u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010 \u001a\u00020\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010!\u001a\u00020\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006="}, d2 = {"Lcom/tunes/player/playback/LocalPlayback;", "Lcom/tunes/player/playback/Playback;", "Landroid/media/MediaPlayer$OnCompletionListener;", "Landroid/media/MediaPlayer$OnPreparedListener;", "Landroid/media/AudioManager$OnAudioFocusChangeListener;", "context", "Landroid/content/Context;", "trackManager", "Lcom/tunes/player/singleton/TrackManager;", "handler", "Landroid/os/Handler;", "(Landroid/content/Context;Lcom/tunes/player/singleton/TrackManager;Landroid/os/Handler;)V", "audioFocusRequest", "Landroid/media/AudioFocusRequest;", "audioManager", "Landroid/media/AudioManager;", "becomingNoisyReceiver", "Landroid/content/BroadcastReceiver;", "currentState", "", "delayedPlayback", "", "filter", "Landroid/content/IntentFilter;", "isBecomingNoisyReceiverRegistered", "mediaId", "", "mediaPlayer", "Landroid/media/MediaPlayer;", "playbackCallback", "Lcom/tunes/player/playback/Playback$Callback;", "playbackState", "resumePosition", "workerHandler", "abandonAudioFocus", "", "configurePlayerState", "getActiveMediaId", "getCurrentStreamingPosition", "getPlaybackState", "initMediaPlayer", "isPlaying", "onAudioFocusChange", "focusChange", "onCompletion", "mp", "onPause", "onPlay", "md", "Lcom/tunes/player/model/MusicModel;", "mediaHasChanged", "onPrepared", "onSeekTo", "position", "onStop", "play", "registerBecomingNoisyReceiver", "resetPlaybackState", "setCallback", "callback", "tryGetAudioFocus", "app_release"})
public final class LocalPlayback implements com.tunes.player.playback.Playback, android.media.MediaPlayer.OnCompletionListener, android.media.MediaPlayer.OnPreparedListener, android.media.AudioManager.OnAudioFocusChangeListener {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tunes.player.singleton.TrackManager trackManager = null;
    @org.jetbrains.annotations.Nullable()
    private final android.os.Handler handler = null;
    @org.jetbrains.annotations.NotNull()
    private final android.content.IntentFilter filter = null;
    @org.jetbrains.annotations.NotNull()
    private final android.media.AudioManager audioManager = null;
    @org.jetbrains.annotations.Nullable()
    private com.tunes.player.playback.Playback.Callback playbackCallback;
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaPlayer mediaPlayer;
    private int resumePosition = -1;
    private boolean isBecomingNoisyReceiverRegistered = false;
    private int currentState = 0;
    @org.jetbrains.annotations.NotNull()
    private final android.content.BroadcastReceiver becomingNoisyReceiver = null;
    private long mediaId = -99L;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler workerHandler = null;
    private boolean delayedPlayback = false;
    private int playbackState = android.media.session.PlaybackState.STATE_NONE;
    @org.jetbrains.annotations.Nullable()
    private android.media.AudioFocusRequest audioFocusRequest;
    
    public LocalPlayback(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.tunes.player.singleton.TrackManager trackManager, @org.jetbrains.annotations.Nullable()
    android.os.Handler handler) {
        super();
    }
    
    @java.lang.Override()
    public void setCallback(@org.jetbrains.annotations.NotNull()
    com.tunes.player.playback.Playback.Callback callback) {
    }
    
    private final void initMediaPlayer() {
    }
    
    @java.lang.Override()
    public void onPlay(@org.jetbrains.annotations.Nullable()
    com.tunes.player.model.MusicModel md, boolean mediaHasChanged) {
    }
    
    private final void play() {
    }
    
    @java.lang.Override()
    public void onPrepared(@org.jetbrains.annotations.NotNull()
    android.media.MediaPlayer mp) {
    }
    
    @java.lang.Override()
    public void onCompletion(@org.jetbrains.annotations.NotNull()
    android.media.MediaPlayer mp) {
    }
    
    @java.lang.Override()
    public void onPause() {
    }
    
    @java.lang.Override()
    public void onSeekTo(long position) {
    }
    
    @java.lang.Override()
    public void onStop(boolean abandonAudioFocus) {
    }
    
    public final void resetPlaybackState() {
    }
    
    @java.lang.Override()
    public long getActiveMediaId() {
        return 0L;
    }
    
    @java.lang.Override()
    public boolean isPlaying() {
        return false;
    }
    
    @java.lang.Override()
    public long getCurrentStreamingPosition() {
        return 0L;
    }
    
    @java.lang.Override()
    public int getPlaybackState() {
        return 0;
    }
    
    private final boolean tryGetAudioFocus() {
        return false;
    }
    
    private final void abandonAudioFocus() {
    }
    
    @java.lang.Override()
    public void onAudioFocusChange(int focusChange) {
    }
    
    private final void configurePlayerState() {
    }
    
    private final void registerBecomingNoisyReceiver() {
    }
}