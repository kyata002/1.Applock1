package com.mtg.applock.util.audio;

import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class AudioFocusManager implements AudioManager.OnAudioFocusChangeListener, Player.EventListener {
    private static final String TAG = AudioFocusManager.class.getSimpleName();
    private static final float MEDIA_VOLUME_DEFAULT = 1.0f;
    private static final float MEDIA_VOLUME_DUCK = 0.2f;

    private final AudioManager mAudioManager;
    private final AudioAttributes mAudioAttributes;
    private final SimpleExoPlayer mPlayer;

    private AudioFocusRequest mAudioFocusRequest;
    private boolean mShouldPlayWhenGainFocus = false;

    public AudioFocusManager(@NonNull AudioManager audioManager, AudioAttributes audioAttributes, @NonNull SimpleExoPlayer player) {
        mAudioManager = audioManager;
        mAudioAttributes = audioAttributes;
        mPlayer = player;
        mPlayer.addListener(this);
    }

    /**
     * media player app should call this function when onPlay() get called
     *
     * @return boolean indicate that whether request success or not
     */
    public boolean requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return requestAudioFocusOreo();
        } else {
            return requestAudioFocusDefault();
        }
    }

    /**
     * media player app should call this function when onPause() get called
     */
    public void abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            abandonAudioFocusOreo();
        } else {
            abandonAudioFocusDefault();
        }
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN: {
                if (mShouldPlayWhenGainFocus && !isPlaying()) {
                    // request audio focus and play
                    //if (requestAudioFocus()) {
                    onPlay();
                    //}
                } else if (isPlaying()) {
                    // restore volume
                    setVolume(MEDIA_VOLUME_DEFAULT);
                }
                mShouldPlayWhenGainFocus = false;
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                // lower volume
                setVolume(MEDIA_VOLUME_DUCK);
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                // pause media
                if (isPlaying()) {
                    mShouldPlayWhenGainFocus = true;
                    //abandonAudioFocus();
                    onPause();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS: {
                // abandon audio focus
                abandonAudioFocus();
                mShouldPlayWhenGainFocus = false;
                onPause();
                break;
            }
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if (playbackState == Player.STATE_ENDED) {
            abandonAudioFocus();
        }
    }

    private void setVolume(float volume) {
        if (volume < 0 || volume > 1) return;
        mPlayer.setVolume(volume);
    }

    private void onPlay() {
        mPlayer.setPlayWhenReady(true);
    }

    private void onPause() {
        mPlayer.setPlayWhenReady(false);
    }

    private boolean isPlaying() {
        return mPlayer.getPlayWhenReady();
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private boolean requestAudioFocusOreo() {
        if (mAudioFocusRequest == null) {
            mAudioFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                    .setAudioAttributes(mAudioAttributes)
                    .setOnAudioFocusChangeListener(this)
                    .build();
        }
        int result = mAudioManager.requestAudioFocus(mAudioFocusRequest);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    private boolean requestAudioFocusDefault() {
        final int result = mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void abandonAudioFocusOreo() {
        if (mAudioFocusRequest != null) {
            mAudioManager.abandonAudioFocusRequest(mAudioFocusRequest);
        }
    }

    private void abandonAudioFocusDefault() {
        mAudioManager.abandonAudioFocus(this);
    }
}
