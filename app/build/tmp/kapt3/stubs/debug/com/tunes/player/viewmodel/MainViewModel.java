package com.tunes.player.viewmodel;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000~\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0010\u000e\n\u0002\b\u001b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010/\u001a\u0002002\u0006\u00101\u001a\u00020\n2\u0006\u00102\u001a\u00020\bH\u0086@\u00a2\u0006\u0002\u00103J\b\u00104\u001a\u000200H\u0002J\u0016\u00105\u001a\u00020\n2\u0006\u00106\u001a\u000207H\u0086@\u00a2\u0006\u0002\u00108J\u0016\u00109\u001a\u0002002\u0006\u00101\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010:J\u001c\u0010;\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\u0006\u00101\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010:J\u0006\u0010<\u001a\u000200J\u0006\u0010=\u001a\u000200J\b\u0010>\u001a\u000200H\u0014J\u001c\u0010?\u001a\u0002002\f\u0010@\u001a\b\u0012\u0004\u0012\u00020\b0\u00072\u0006\u0010A\u001a\u00020\u000eJ\u0006\u0010B\u001a\u000200J\u001e\u0010C\u001a\u0002002\u0006\u00101\u001a\u00020\n2\u0006\u0010D\u001a\u00020\nH\u0086@\u00a2\u0006\u0002\u0010EJ\u001e\u0010F\u001a\u0002002\u0006\u00101\u001a\u00020\n2\u0006\u0010G\u001a\u000207H\u0086@\u00a2\u0006\u0002\u0010HJ\u000e\u0010I\u001a\u0002002\u0006\u0010J\u001a\u00020\nJ\r\u0010K\u001a\u0004\u0018\u000100\u00a2\u0006\u0002\u0010LJ\r\u0010M\u001a\u0004\u0018\u000100\u00a2\u0006\u0002\u0010LJ\b\u0010N\u001a\u000200H\u0002J\u0006\u0010O\u001a\u000200J\u0006\u0010P\u001a\u000200J\u0006\u0010Q\u001a\u000200R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\u0010\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\u0013R\u001d\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\u0013R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\n0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0019\u0010\u0013R\u0019\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\b0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0013R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0013R\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\f0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0013R\u0010\u0010 \u001a\u0004\u0018\u00010!X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\"\u001a\u0004\u0018\u00010#X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0017\u0010$\u001a\b\u0012\u0004\u0012\u00020\u000e0\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0013R\u000e\u0010&\u001a\u00020\'X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010(\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020)0\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u0013R\u001d\u0010+\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010\u0013R\u000e\u0010-\u001a\u00020.X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006R"}, d2 = {"Lcom/tunes/player/viewmodel/MainViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "_allTracks", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/tunes/player/model/MusicModel;", "_currentPosition", "", "_isLoading", "", "_playbackState", "", "_recentTracks", "activeQueue", "Lkotlinx/coroutines/flow/StateFlow;", "getActiveQueue", "()Lkotlinx/coroutines/flow/StateFlow;", "allTracks", "getAllTracks", "connectionCallback", "Landroid/media/browse/MediaBrowser$ConnectionCallback;", "currentPosition", "getCurrentPosition", "currentTrack", "getCurrentTrack", "fetcher", "Lcom/tunes/player/loaders/TrackFetcherFromStorage;", "isLoading", "isPlaylistLoading", "mediaBrowser", "Landroid/media/browse/MediaBrowser;", "mediaController", "Landroid/media/session/MediaController;", "playbackState", "getPlaybackState", "playlistManager", "Lcom/tunes/player/manager/PlaylistManager;", "playlists", "Lcom/tunes/player/model/PlaylistModel;", "getPlaylists", "recentTracks", "getRecentTracks", "trackManager", "Lcom/tunes/player/singleton/TrackManager;", "addTrackToPlaylist", "", "playlistId", "track", "(JLcom/tunes/player/model/MusicModel;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "connectToService", "createPlaylist", "name", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deletePlaylist", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getPlaylistTracks", "loadLibrary", "loadPlaylists", "onCleared", "playTrack", "list", "index", "refreshPlaybackState", "removeTrackFromPlaylist", "trackId", "(JJLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "renamePlaylist", "newName", "(JLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "seekTo", "pos", "skipToNext", "()Lkotlin/Unit;", "skipToPrevious", "startPositionUpdater", "togglePlayPause", "toggleRepeat", "toggleShuffle", "app_debug"})
public final class MainViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.tunes.player.singleton.TrackManager trackManager = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tunes.player.loaders.TrackFetcherFromStorage fetcher = null;
    @org.jetbrains.annotations.NotNull()
    private final com.tunes.player.manager.PlaylistManager playlistManager = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.tunes.player.model.MusicModel>> _allTracks = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.MusicModel>> allTracks = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.tunes.player.model.MusicModel>> _recentTracks = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.MusicModel>> recentTracks = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Integer> _playbackState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> playbackState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Long> _currentPosition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Long> currentPosition = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tunes.player.model.MusicModel> currentTrack = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.MusicModel>> activeQueue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.PlaylistModel>> playlists = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPlaylistLoading = null;
    @org.jetbrains.annotations.Nullable()
    private android.media.browse.MediaBrowser mediaBrowser;
    @org.jetbrains.annotations.Nullable()
    private android.media.session.MediaController mediaController;
    @org.jetbrains.annotations.NotNull()
    private final android.media.browse.MediaBrowser.ConnectionCallback connectionCallback = null;
    
    public MainViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.MusicModel>> getAllTracks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.MusicModel>> getRecentTracks() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Integer> getPlaybackState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Long> getCurrentPosition() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tunes.player.model.MusicModel> getCurrentTrack() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.MusicModel>> getActiveQueue() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.PlaylistModel>> getPlaylists() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isPlaylistLoading() {
        return null;
    }
    
    private final void connectToService() {
    }
    
    private final void startPositionUpdater() {
    }
    
    public final void loadLibrary() {
    }
    
    public final void playTrack(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tunes.player.model.MusicModel> list, int index) {
    }
    
    public final void togglePlayPause() {
    }
    
    public final void refreshPlaybackState() {
    }
    
    public final void seekTo(long pos) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.Unit skipToNext() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.Unit skipToPrevious() {
        return null;
    }
    
    public final void toggleShuffle() {
    }
    
    public final void toggleRepeat() {
    }
    
    public final void loadPlaylists() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object createPlaylist(@org.jetbrains.annotations.NotNull()
    java.lang.String name, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object renamePlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    java.lang.String newName, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deletePlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addTrackToPlaylist(long playlistId, @org.jetbrains.annotations.NotNull()
    com.tunes.player.model.MusicModel track, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object removeTrackFromPlaylist(long playlistId, long trackId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getPlaylistTracks(long playlistId, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.tunes.player.model.MusicModel>> $completion) {
        return null;
    }
    
    @java.lang.Override()
    protected void onCleared() {
    }
}