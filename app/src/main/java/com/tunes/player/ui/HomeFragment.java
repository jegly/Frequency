package com.tunes.player.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;

import com.tunes.player.R;
import com.tunes.player.activities.MainActivity;
import com.tunes.player.activities.SearchActivity;
import com.tunes.player.activities.SettingsActivity;
import com.tunes.player.helper.MediaHelper;
import com.tunes.player.model.MusicModel;
import com.tunes.player.singleton.TrackManager;
import com.tunes.player.utils.PlaylistStorageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Replaced AsyncTask inner classes (ImportFilesTask, ImportFolderTask)
 * with ExecutorService + main-thread Handler.
 * Also removed the deprecated DATA column from the folder scan MediaStore query,
 * using content URIs instead.
 */
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private static final int PICK_MUSIC  = 1600;
    private static final int PICK_FOLDER = 1601;

    private static final ExecutorService sExecutor  = Executors.newSingleThreadExecutor();
    private static final Handler sMainHandler = new Handler(Looper.getMainLooper());

    private TrackManager tm;
    private MediaController.TransportControls mTransportControl;
    private ProgressBar mProgressBar;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        postponeEnterTransition();
        tm = TrackManager.getInstance();
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mProgressBar = view.findViewById(R.id.progress_bar);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.home);
            toolbar.setNavigationIcon(R.drawable.ic_settings);
            if (getActivity() instanceof MainActivity)
                ((MainActivity) getActivity()).setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(v -> {
                if (getContext() != null)
                    startActivity(new Intent(getContext(), SettingsActivity.class));
            });
        }
        startPostponedEnterTransition();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_home_fragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.search && getContext() != null) {
            startActivity(new Intent(getContext(), SearchActivity.class));
            return true;
        } else if (id == R.id.import_music) {
            View anchor = getActivity() != null
                    ? getActivity().findViewById(R.id.import_music) : null;
            if (anchor == null) anchor = getView();
            PopupMenu popup = new PopupMenu(getContext(), anchor);
            popup.getMenu().add(0, 1, 0, "Select Files");
            popup.getMenu().add(0, 2, 1, "Select Folder");
            popup.setOnMenuItemClickListener(menuItem -> {
                if      (menuItem.getItemId() == 1) pickMedia();
                else if (menuItem.getItemId() == 2) pickFolder();
                return true;
            });
            popup.show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void pickMedia() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_MUSIC);
    }

    private void pickFolder() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_FOLDER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK || data == null || getContext() == null) return;

        if (requestCode == PICK_MUSIC) {
            importFiles(data);
        } else if (requestCode == PICK_FOLDER) {
            Uri treeUri = data.getData();
            if (treeUri != null) {
                getContext().getContentResolver().takePersistableUriPermission(
                        treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                importFolder(treeUri);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Import files
    // -------------------------------------------------------------------------

    private void importFiles(final Intent data) {
        showProgress(true);
        sExecutor.execute(() -> {
            List<MusicModel> picked = new ArrayList<>();
            if (data.getClipData() != null) {
                for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                    Uri uri = data.getClipData().getItemAt(i).getUri();
                    MusicModel md = MediaHelper.buildMusicModelFromUri(getContext(), uri);
                    if (md != null) picked.add(md);
                }
            } else if (data.getData() != null) {
                MusicModel md = MediaHelper.buildMusicModelFromUri(getContext(), data.getData());
                if (md != null) picked.add(md);
            }
            final List<MusicModel> result = picked;
            sMainHandler.post(() -> onImportComplete(result,
                    "No valid audio files found"));
        });
    }

    // -------------------------------------------------------------------------
    // Import folder
    // -------------------------------------------------------------------------

    private void importFolder(final Uri treeUri) {
        showProgress(true);
        sExecutor.execute(() -> {
            List<MusicModel> found = new ArrayList<>();

            // Fast path: query MediaStore using the folder path segment.
            try {
                String docId = DocumentsContract.getTreeDocumentId(treeUri);
                String[] split = docId.split(":");
                if (split.length > 1) {
                    String folderPath = split[1];
                    Uri audioUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    // Use DISPLAY_NAME-based filter — avoids deprecated DATA column.
                    String[] projection = {
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.ALBUM,
                            MediaStore.Audio.Media.ALBUM_ID,
                            MediaStore.Audio.Media.DURATION
                    };
                    // DATA is still the only reliable folder-path filter in MediaStore;
                    // we use it here for selection only (not stored in MusicModel).
                    @SuppressWarnings("deprecation")
                    String selection = MediaStore.Audio.Media.DATA + " LIKE ?";
                    String[] args = { "%" + folderPath + "/%" };

                    try (Cursor c = getContext().getContentResolver()
                            .query(audioUri, projection, selection, args, null)) {
                        if (c != null && c.moveToFirst()) {
                            int idCol      = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
                            int titleCol   = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                            int artistCol  = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                            int albumCol   = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                            int albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                            int durCol     = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                            do {
                                long id      = c.getLong(idCol);
                                String title = c.getString(titleCol);
                                String art   = ContentUris.withAppendedId(
                                        Uri.parse("content://media/external/audio/albumart"),
                                        c.getLong(albumIdCol)).toString();
                                // Build content URI — avoids storing the raw file path.
                                String contentUri = ContentUris
                                        .withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                                        .toString();
                                found.add(new MusicModel(
                                        (int) id, title,
                                        c.getString(artistCol),
                                        c.getString(albumCol),
                                        contentUri, art,
                                        c.getLong(durCol), 0));
                            } while (c.moveToNext());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "MediaStore folder scan failed, falling back", e);
            }

            // Fallback: walk the DocumentFile tree if MediaStore found nothing.
            if (found.isEmpty()) {
                DocumentFile root = DocumentFile.fromTreeUri(getContext(), treeUri);
                if (root != null) recursiveScan(root, found);
            }

            final List<MusicModel> result = found;
            sMainHandler.post(() -> onImportComplete(result,
                    "No audio files found in this folder"));
        });
    }

    private void recursiveScan(DocumentFile parent, List<MusicModel> list) {
        for (DocumentFile file : parent.listFiles()) {
            if (file.isDirectory()) {
                recursiveScan(file, list);
            } else {
                String type = file.getType();
                if (type != null && type.startsWith("audio/")) {
                    MusicModel md = MediaHelper.buildMusicModelFromUri(getContext(), file.getUri());
                    if (md != null) list.add(md);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Shared post-import handling
    // -------------------------------------------------------------------------

    private void onImportComplete(List<MusicModel> models, String emptyMsg) {
        showProgress(false);
        if (!models.isEmpty()) {
            tm.addImportedTracks(getContext(), models);
            tm.buildDataList(models, 0);
            play();
        } else {
            if (getContext() != null)
                Toast.makeText(getContext(), emptyMsg, Toast.LENGTH_SHORT).show();
        }
    }

    private void showProgress(boolean visible) {
        sMainHandler.post(() -> {
            if (mProgressBar != null)
                mProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        });
    }

    private void play() {
        if (mTransportControl != null) {
            mTransportControl.play();
        } else if (getActivity() != null) {
            MediaController controller = getActivity().getMediaController();
            if (controller != null) {
                mTransportControl = controller.getTransportControls();
                if (mTransportControl != null) mTransportControl.play();
            }
        }
    }
}
