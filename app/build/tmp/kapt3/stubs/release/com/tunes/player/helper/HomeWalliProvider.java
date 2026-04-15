package com.tunes.player.helper;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u0005B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0006\u0010\u0003\u001a\u00020\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/tunes/player/helper/HomeWalliProvider;", "", "()V", "getTimeOfDay", "Lcom/tunes/player/helper/HomeWalliProvider$Day;", "Day", "app_release"})
public final class HomeWalliProvider {
    @org.jetbrains.annotations.NotNull()
    public static final com.tunes.player.helper.HomeWalliProvider INSTANCE = null;
    
    private HomeWalliProvider() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.tunes.player.helper.HomeWalliProvider.Day getTimeOfDay() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/tunes/player/helper/HomeWalliProvider$Day;", "", "(Ljava/lang/String;I)V", "MORNING", "AFTERNOON", "EVENING", "NIGHT", "app_release"})
    public static enum Day {
        /*public static final*/ MORNING /* = new MORNING() */,
        /*public static final*/ AFTERNOON /* = new AFTERNOON() */,
        /*public static final*/ EVENING /* = new EVENING() */,
        /*public static final*/ NIGHT /* = new NIGHT() */;
        
        Day() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.tunes.player.helper.HomeWalliProvider.Day> getEntries() {
            return null;
        }
    }
}