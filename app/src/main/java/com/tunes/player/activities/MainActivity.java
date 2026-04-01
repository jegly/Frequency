package com.tunes.player.activities;

import android.media.MediaMetadata;
import android.media.browse.MediaBrowser;
import android.media.session.MediaController;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.tunes.player.R;
import com.tunes.player.singleton.TrackManager;
import com.tunes.player.loaders.TrackFetcherFromStorage;
import com.tunes.player.loaders.TrackFetcherFromStorage.Sort;
import com.tunes.player.themes.ThemeManager;
import com.tunes.player.ui.ArtistFragment;
import com.tunes.player.ui.ControlsFragment;
import com.tunes.player.ui.HomeFragment;
import com.tunes.player.ui.LibraryFragment;
import com.tunes.player.ui.PlaylistCardFragment;


public class MainActivity extends MediaSessionActivity {

    public static final String TAG = "MainActivity";
    private static final String HOME = "HomeFragment";
    private static final String LIBRARY = "LibraryFragment";
    private static final String PLAYLIST_CARDS = "PlaylistCardFragment";
    private static final String ARTIST = "ArtistFragment";
    private static final String ACTIVE = "ActiveFragment";

    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment homeFrag = null;
    private Fragment libraryFrag = null;
    private Fragment playlistCardFrag = null;
    private Fragment controlsFrag = null;
    private final MediaController.Callback mCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(@Nullable MediaMetadata metadata) {
            showControlsFragment();
        }
    };

    private MediaController mController;
    private Fragment activeFrag = null;
    private MediaBrowser mMediaBrowser;
    private Fragment artistFrag = null;
    @StyleRes private int mCurrentTheme;
    @StyleRes private int mCurrentAccent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mCurrentTheme = ThemeManager.getThemeToApply();
        mCurrentAccent = ThemeManager.getAccentToApply();

        super.onCreate(null); // Pass null to prevent restoration of fragments on activity recreate

        setContentView(R.layout.activity_main);

        if (TrackManager.getInstance().getMainList().isEmpty()) {
            TrackFetcherFromStorage ml = new TrackFetcherFromStorage(getContentResolver(), list -> {
                new Thread(() -> {
                    TrackManager.getInstance().setMainList(list);
                    runOnUiThread(() -> setUpMainContents(savedInstanceState));
                }).start();
            }, Sort.TITLE_ASC);
            ml.execute();
        } else setUpMainContents(savedInstanceState);
    }

    private void setUpMainContents(Bundle savedInstanceState) {
        if (savedInstanceState == null) switchFragment(homeFrag, HOME);
        else {
            String tag = savedInstanceState.getString(ACTIVE, HOME);
            switch (tag) {
                case LIBRARY: switchFragment(libraryFrag, LIBRARY); break;
                case PLAYLIST_CARDS: switchFragment(playlistCardFrag, PLAYLIST_CARDS); break;
                case ARTIST: switchFragment(artistFrag, ARTIST); break;
                default: switchFragment(homeFrag, HOME); break;
            }
        }
        setUpBottomNavigationView();
    }

    private void setUpBottomNavigationView() {
        BottomNavigationView bottomNavigation = findViewById(R.id.google_bottom_nav);
        bottomNavigation.setOnItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_home) {
                if (activeFrag != homeFrag) {
                    switchFragment(homeFrag, HOME);
                }
            } else if (id == R.id.nav_library) {
                if (activeFrag != libraryFrag) {
                    switchFragment(libraryFrag, LIBRARY);
                }
            } else if (id == R.id.nav_playlist) {
                if (activeFrag != playlistCardFrag) {
                    switchFragment(playlistCardFrag, PLAYLIST_CARDS);
                }
            } else if (id == R.id.nav_artist) {
                if (activeFrag != artistFrag) {
                    switchFragment(artistFrag, ARTIST);
                }
            }
            return true;
        });
    }

    private void switchFragment(Fragment switchTo, String tag) {
        if (null == switchTo) {
            switch (tag) {

                case HOME:
                    homeFrag = new HomeFragment();
                    switchTo = homeFrag;
                    break;

                case LIBRARY:
                    libraryFrag = new LibraryFragment();
                    switchTo = libraryFrag;
                    break;

                case PLAYLIST_CARDS:
                    playlistCardFrag = new PlaylistCardFragment();
                    switchTo = playlistCardFrag;
                    break;

                case ARTIST:
                    artistFrag = new ArtistFragment();
                    switchTo = artistFrag;
                    break;

                default:
                    Log.e(TAG, "SwitchTo fragment is not a member of defined fragments");
            }

            if (switchTo != null && activeFrag != null) {
                fm.beginTransaction()
                        .add(R.id.fragment_container, switchTo, tag)
                        .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                        .hide(activeFrag)
                        .show(switchTo)
                        .commit();
            } else if (switchTo != null) {
                fm.beginTransaction()
                        .add(R.id.fragment_container, switchTo, tag)
                        .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                        .show(switchTo)
                        .commit();
            }
        } else if (switchTo != activeFrag) {
            fm.beginTransaction()
                    .setCustomAnimations(R.anim.fragment_enter, R.anim.fragment_exit)
                    .hide(activeFrag)
                    .show(switchTo)
                    .commit();
        }
        activeFrag = switchTo;
    }

    @Override
    public void onMediaServiceConnected(MediaController controller) {
        mController = controller;
        mMediaBrowser = getMediaBrowser();
        //Register callback to receive metadata changes
        mController.registerCallback(mCallback);

        if (mController.getMetadata() != null)
            showControlsFragment();
    }

    private void showControlsFragment() {
        if (controlsFrag == null) {
            controlsFrag = new ControlsFragment();
            findViewById(R.id.controls_fragment_container).setVisibility(View.VISIBLE);
            fm.beginTransaction()
                    .setCustomAnimations(R.anim.slide_up_enter, R.anim.slide_down_exit)
                    .replace(R.id.controls_fragment_container, controlsFrag)
                    .commit();
        }
    }

    @Override
    protected void onStart() {
        if ((mCurrentTheme != ThemeManager.getThemeToApply()) || (mCurrentAccent != ThemeManager.getAccentToApply())) {
            supportInvalidateOptionsMenu();
            recreate();
        }
        super.onStart();
        if (null != mController && mMediaBrowser != null && mMediaBrowser.isConnected()) {
            mController.registerCallback(mCallback);
            if (null != mController.getMetadata())
                showControlsFragment();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (null != mController)
            mController.unregisterCallback(mCallback);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getMediaController() != null)
            getMediaController().unregisterCallback(mCallback);
        if (mMediaBrowser != null)
            mMediaBrowser.disconnect();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (activeFrag != null) {
            outState.putString(ACTIVE, activeFrag.getTag());
        }
    }
}
