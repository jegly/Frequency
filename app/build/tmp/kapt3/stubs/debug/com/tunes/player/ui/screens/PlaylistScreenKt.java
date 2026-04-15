package com.tunes.player.ui.screens;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00006\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0000\u001aL\u0010\u0000\u001a\u00020\u00012\f\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0018\u0010\b\u001a\u0014\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00040\u0003\u0012\u0004\u0012\u00020\u00010\tH\u0007\u001a&\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u0007H\u0007\u001a4\u0010\u0010\u001a\u00020\u00012\u0006\u0010\u0011\u001a\u00020\u00042\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u00072\u0006\u0010\u0014\u001a\u00020\u0015H\u0007\u00a8\u0006\u0016"}, d2 = {"AddTracksToPlaylistDialog", "", "allTracks", "", "Lcom/tunes/player/model/MusicModel;", "currentTracks", "onDismiss", "Lkotlin/Function0;", "onAddTracks", "Lkotlin/Function1;", "PlaylistScreen", "viewModel", "Lcom/tunes/player/viewmodel/MainViewModel;", "playlistName", "", "onBack", "PlaylistTrackItem", "track", "onTrackClick", "onMenuClick", "canRemove", "", "app_debug"})
public final class PlaylistScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void PlaylistScreen(@org.jetbrains.annotations.NotNull()
    com.tunes.player.viewmodel.MainViewModel viewModel, @org.jetbrains.annotations.NotNull()
    java.lang.String playlistName, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBack) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void PlaylistTrackItem(@org.jetbrains.annotations.NotNull()
    com.tunes.player.model.MusicModel track, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onTrackClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onMenuClick, boolean canRemove) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void AddTracksToPlaylistDialog(@org.jetbrains.annotations.NotNull()
    java.util.List<com.tunes.player.model.MusicModel> allTracks, @org.jetbrains.annotations.NotNull()
    java.util.List<com.tunes.player.model.MusicModel> currentTracks, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.util.List<com.tunes.player.model.MusicModel>, kotlin.Unit> onAddTracks) {
    }
}