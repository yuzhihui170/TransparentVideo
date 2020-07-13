package com.forrest.transparentvideo;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.Log;
import android.view.Surface;

import java.io.IOException;

import androidx.annotation.RequiresApi;

class MediaPlayerWrap {

    private static String TAG = "MediaPlayerWrap";
    private MediaPlayer mMediaPlayer;
    private Surface mSurface;
    private String mVideoPath;

    private AssetManager mAssetManager;
    private String mFileName;

    public MediaPlayerWrap() {
        mMediaPlayer = new MediaPlayer();
    }

    /** 给MediaPlayer设置各种监听器 */
    private void setup() {
        /** 设置准好后的监听器 */
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });

        /** 设置视频播放完成的监听器 */
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onCompletion(MediaPlayer mp) {
                play(mAssetManager, mFileName);
            }
        });

        /** 设置出错的监听器 */
        mMediaPlayer.setOnErrorListener(errorListener);
    }

    public void setSurfaceTexture(SurfaceTexture surfaceTexture) {
        this.mSurface = new Surface(surfaceTexture);
    }

    /** 播放制定路径的视频 */
    public void play(String path) {
        mVideoPath = path;
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            setup();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setSurface(mSurface);
            mMediaPlayer.setDataSource(path);
            // mMediaPlayer.prepare();
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void play(AssetManager am, String filename) {
        mAssetManager = am;
        mFileName = filename;
        try {
            if (mMediaPlayer != null) {
                mMediaPlayer.reset();
            }
            setup();
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mMediaPlayer.setSurface(mSurface);
            AssetFileDescriptor afd = am.openFd(filename);
            mMediaPlayer.setDataSource(afd);
            mMediaPlayer.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.pause();
            mMediaPlayer.stop();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /** 出错监听器 */
    private MediaPlayer.OnErrorListener errorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, "Error: what:"+what + " extra:" + extra);
//            switch (what) {
//                case MediaPlayer.MEDIA_ERROR_UNKNOWN:// 未知错误
//                    Log.e(TAG, "Error: MediaPlayer.MEDIA_ERROR_IO");
//                    break;

//                case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
//                    break;

//                case MediaPlayer.MEDIA_ERROR_IO:
//                    Log.e(TAG, "Error: MediaPlayer.MEDIA_ERROR_IO");
//                    break;
//
//                case MediaPlayer.MEDIA_ERROR_MALFORMED:
//                    Log.e(TAG, "Error: MediaPlayer.MEDIA_ERROR_MALFORMED");
//                    break;
//
//                case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
//                    Log.e(TAG, "Error: MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK");
//                    break;
//
//                case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
//                    break;
//
//                case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
//                    break;
//                default:
//                    break;
//        }
            return true;
        }
    };

}
