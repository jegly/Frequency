package com.tunes.player.loaders;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000$\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001:\u0001\u000bB\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u001e\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u00062\b\b\u0002\u0010\b\u001a\u00020\tH\u0086@\u00a2\u0006\u0002\u0010\nR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/tunes/player/loaders/AlbumFetcher;", "", "contentResolver", "Landroid/content/ContentResolver;", "(Landroid/content/ContentResolver;)V", "fetchAlbums", "", "Lcom/tunes/player/model/AlbumModel;", "sort", "Lcom/tunes/player/loaders/AlbumFetcher$Sort;", "(Lcom/tunes/player/loaders/AlbumFetcher$Sort;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Sort", "app_debug"})
public final class AlbumFetcher {
    @org.jetbrains.annotations.NotNull()
    private final android.content.ContentResolver contentResolver = null;
    
    public AlbumFetcher(@org.jetbrains.annotations.NotNull()
    android.content.ContentResolver contentResolver) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object fetchAlbums(@org.jetbrains.annotations.NotNull()
    com.tunes.player.loaders.AlbumFetcher.Sort sort, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.tunes.player.model.AlbumModel>> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004\u00a8\u0006\u0005"}, d2 = {"Lcom/tunes/player/loaders/AlbumFetcher$Sort;", "", "(Ljava/lang/String;I)V", "TITLE_ASC", "DATE_ASC", "app_debug"})
    public static enum Sort {
        /*public static final*/ TITLE_ASC /* = new TITLE_ASC() */,
        /*public static final*/ DATE_ASC /* = new DATE_ASC() */;
        
        Sort() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.tunes.player.loaders.AlbumFetcher.Sort> getEntries() {
            return null;
        }
    }
}