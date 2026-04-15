package com.tunes.player.playback;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001:\u0001\u0016J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0003H&J\b\u0010\u0005\u001a\u00020\u0006H&J\b\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\nH&J\u001a\u0010\u000b\u001a\u00020\n2\b\u0010\f\u001a\u0004\u0018\u00010\r2\u0006\u0010\u000e\u001a\u00020\bH&J\u0010\u0010\u000f\u001a\u00020\n2\u0006\u0010\u0010\u001a\u00020\u0003H&J\u0010\u0010\u0011\u001a\u00020\n2\u0006\u0010\u0012\u001a\u00020\bH&J\u0010\u0010\u0013\u001a\u00020\n2\u0006\u0010\u0014\u001a\u00020\u0015H&\u00a8\u0006\u0017"}, d2 = {"Lcom/tunes/player/playback/Playback;", "", "getActiveMediaId", "", "getCurrentStreamingPosition", "getPlaybackState", "", "isPlaying", "", "onPause", "", "onPlay", "md", "Lcom/tunes/player/model/MusicModel;", "mediaHasChanged", "onSeekTo", "position", "onStop", "abandonAudioFocus", "setCallback", "callback", "Lcom/tunes/player/playback/Playback$Callback;", "Callback", "app_debug"})
public abstract interface Playback {
    
    public abstract void onPlay(@org.jetbrains.annotations.Nullable()
    com.tunes.player.model.MusicModel md, boolean mediaHasChanged);
    
    public abstract void onPause();
    
    public abstract void onSeekTo(long position);
    
    public abstract void onStop(boolean abandonAudioFocus);
    
    public abstract long getActiveMediaId();
    
    public abstract boolean isPlaying();
    
    public abstract long getCurrentStreamingPosition();
    
    public abstract int getPlaybackState();
    
    public abstract void setCallback(@org.jetbrains.annotations.NotNull()
    com.tunes.player.playback.Playback.Callback callback);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001e\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\bf\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0003H&J\u0010\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH&\u00a8\u0006\n"}, d2 = {"Lcom/tunes/player/playback/Playback$Callback;", "", "onFocusChanged", "", "resumePlayback", "", "onPlaybackCompletion", "onPlaybackStateChanged", "state", "", "app_debug"})
    public static abstract interface Callback {
        
        public abstract void onPlaybackCompletion();
        
        public abstract void onPlaybackStateChanged(int state);
        
        public abstract void onFocusChanged(boolean resumePlayback);
    }
}