package com.tunes.player.singleton;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000b\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010!\n\u0000\n\u0002\u0010#\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\n\n\u0002\b\u0010\u0018\u0000 A2\u00020\u0001:\u0001AB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001e\u0010%\u001a\u00020&2\u0006\u0010\'\u001a\u00020(2\u000e\u0010)\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005J\u000e\u0010*\u001a\u00020&2\u0006\u0010+\u001a\u00020\u0006J\u000e\u0010,\u001a\u00020&2\u0006\u0010\'\u001a\u00020(J\u001c\u0010-\u001a\u00020&2\f\u0010.\u001a\b\u0012\u0004\u0012\u00020\u00060\u00052\u0006\u0010/\u001a\u00020\u0017J\u000e\u00100\u001a\u00020\u000f2\u0006\u00101\u001a\u000202J\u0006\u00103\u001a\u00020\u0017J\b\u00104\u001a\u0004\u0018\u00010\u0006J\f\u00105\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005J\u000e\u00106\u001a\u00020&2\u0006\u0010+\u001a\u00020\u0006J\u000e\u00107\u001a\u00020&2\u0006\u00108\u001a\u00020\u0017J\u0006\u00109\u001a\u00020&J\u0016\u0010:\u001a\u00020&2\u000e\u0010;\u001a\n\u0012\u0004\u0012\u00020\u0006\u0018\u00010\u0005J\u0016\u0010<\u001a\u00020&2\u0006\u0010=\u001a\u00020\u00172\u0006\u0010>\u001a\u00020\u0017J\b\u0010?\u001a\u00020&H\u0002J\b\u0010@\u001a\u00020&H\u0002R\u001a\u0010\u0003\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0007\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00060\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001d\u0010\b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00060\u00050\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0019\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00060\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000bR\u001a\u0010\u000e\u001a\u00020\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u0010\"\u0004\b\u0011\u0010\u0012R$\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0013\u001a\u00020\u000f@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0010\"\u0004\b\u0015\u0010\u0012R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u00060\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\u001e0\u001dX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00060\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010 \u001a\u00020\u000fX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b!\u0010\u0010\"\u0004\b\"\u0010\u0012R\u000e\u0010#\u001a\u00020$X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006B"}, d2 = {"Lcom/tunes/player/singleton/TrackManager;", "", "()V", "_activeList", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "Lcom/tunes/player/model/MusicModel;", "_currentTrack", "activeList", "Lkotlinx/coroutines/flow/StateFlow;", "getActiveList", "()Lkotlinx/coroutines/flow/StateFlow;", "currentTrack", "getCurrentTrack", "isInfiniteLoopEnabled", "", "()Z", "setInfiniteLoopEnabled", "(Z)V", "value", "isShuffleEnabled", "setShuffleEnabled", "mDeletedQueueIndex", "", "mDeletedQueueItem", "mIndex", "mMainList", "", "mMainListPaths", "", "", "mOriginalList", "repeatCurrentTrack", "getRepeatCurrentTrack", "setRepeatCurrentTrack", "scope", "Lkotlinx/coroutines/CoroutineScope;", "addImportedTracks", "", "context", "Landroid/content/Context;", "tracks", "addToActiveQueue", "md", "addToHistory", "buildDataList", "newList", "index", "canSkipTrack", "direction", "", "getActiveIndex", "getActiveQueueItem", "getMainList", "playNext", "removeItemFromActiveQueue", "position", "restoreItem", "setMainList", "mainList", "updateActiveQueue", "from", "to", "updateCurrentTrack", "updateShuffle", "Companion", "app_release"})
public final class TrackManager {
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<com.tunes.player.model.MusicModel>> _activeList = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.MusicModel>> activeList = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.tunes.player.model.MusicModel> _currentTrack = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.tunes.player.model.MusicModel> currentTrack = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tunes.player.model.MusicModel> mMainList = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Set<java.lang.String> mMainListPaths = null;
    private int mIndex = -1;
    @org.jetbrains.annotations.Nullable()
    private com.tunes.player.model.MusicModel mDeletedQueueItem;
    private int mDeletedQueueIndex = -1;
    private boolean repeatCurrentTrack = false;
    private boolean isInfiniteLoopEnabled = false;
    private boolean isShuffleEnabled = false;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.tunes.player.model.MusicModel> mOriginalList = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    @org.jetbrains.annotations.NotNull()
    private static final kotlin.Lazy<?> instance$delegate = null;
    public static final short ACTION_PLAY_NEXT = (short)1;
    public static final short ACTION_PLAY_PREV = (short)2;
    @org.jetbrains.annotations.NotNull()
    public static final com.tunes.player.singleton.TrackManager.Companion Companion = null;
    
    private TrackManager() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.tunes.player.model.MusicModel>> getActiveList() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.tunes.player.model.MusicModel> getCurrentTrack() {
        return null;
    }
    
    public final boolean getRepeatCurrentTrack() {
        return false;
    }
    
    public final void setRepeatCurrentTrack(boolean p0) {
    }
    
    public final boolean isInfiniteLoopEnabled() {
        return false;
    }
    
    public final void setInfiniteLoopEnabled(boolean p0) {
    }
    
    public final boolean isShuffleEnabled() {
        return false;
    }
    
    public final void setShuffleEnabled(boolean value) {
    }
    
    @kotlin.jvm.Synchronized()
    public final synchronized void setMainList(@org.jetbrains.annotations.Nullable()
    java.util.List<com.tunes.player.model.MusicModel> mainList) {
    }
    
    @kotlin.jvm.Synchronized()
    public final synchronized void addImportedTracks(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    java.util.List<com.tunes.player.model.MusicModel> tracks) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.tunes.player.model.MusicModel> getMainList() {
        return null;
    }
    
    public final void buildDataList(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tunes.player.model.MusicModel> newList, int index) {
    }
    
    private final void updateCurrentTrack() {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.tunes.player.model.MusicModel getActiveQueueItem() {
        return null;
    }
    
    public final int getActiveIndex() {
        return 0;
    }
    
    private final void updateShuffle() {
    }
    
    public final boolean canSkipTrack(short direction) {
        return false;
    }
    
    public final void playNext(@org.jetbrains.annotations.NotNull()
    com.tunes.player.model.MusicModel md) {
    }
    
    public final void addToActiveQueue(@org.jetbrains.annotations.NotNull()
    com.tunes.player.model.MusicModel md) {
    }
    
    public final void removeItemFromActiveQueue(int position) {
    }
    
    public final void restoreItem() {
    }
    
    public final void updateActiveQueue(int from, int to) {
    }
    
    public final void addToHistory(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\n\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0006\u001a\u00020\u00078FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\n\u0010\u000b\u001a\u0004\b\b\u0010\t\u00a8\u0006\f"}, d2 = {"Lcom/tunes/player/singleton/TrackManager$Companion;", "", "()V", "ACTION_PLAY_NEXT", "", "ACTION_PLAY_PREV", "instance", "Lcom/tunes/player/singleton/TrackManager;", "getInstance", "()Lcom/tunes/player/singleton/TrackManager;", "instance$delegate", "Lkotlin/Lazy;", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.tunes.player.singleton.TrackManager getInstance() {
            return null;
        }
    }
}