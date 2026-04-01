package com.tunes.player.loaders;

import android.os.Handler;
import android.os.Looper;

import com.tunes.player.interfaces.AsyncTaskCallback;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Replaced deprecated AsyncTask with ExecutorService + main-thread Handler. */
public class ShuffledListProvider {

    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private final AsyncTaskCallback.Simple mCallback;

    public ShuffledListProvider(AsyncTaskCallback.Simple callback) {
        mCallback = callback;
    }

    public void execute() {
        sExecutor.execute(() -> {
            List<MusicModel> result = suggested();
            sMainHandler.post(() -> {
                if (mCallback != null) mCallback.onTaskComplete(result);
            });
        });
    }

    private List<MusicModel> suggested() {
        List<MusicModel> source = TrackManager.getInstance().getMainList();
        List<MusicModel> out = new ArrayList<>();
        if (source == null || source.isEmpty()) return out;

        int size = source.size();
        Random rn = new Random();
        int attempts = 0;
        while (out.size() < 15 && attempts < size * 3) {
            MusicModel candidate = source.get(rn.nextInt(size));
            if (!out.contains(candidate)) out.add(candidate);
            attempts++;
        }
        return out;
    }
}
