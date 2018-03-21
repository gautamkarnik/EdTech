package com.edtech.audio;

import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Environment;
import android.util.Log;

import java.io.IOException;

/* Based on: http://developer.android.com/guide/topics/media/audio-capture.html
 * The application needs to have the permission to write to external storage
 * if the output file is written to the external storage, and also the
 * permission to record audio. These permissions must be set in the
 * application's AndroidManifest.xml file, with something like:
 *
 * <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
 * <uses-permission android:name="android.permission.RECORD_AUDIO" />
 *
*/
public class AudioPlayer {

    private final String CLASS_NAME = getClass().getSimpleName();
    private static final String AUDIO_FILE_PATH = "/Documents/EdTech/audioRecord.wav";

    private String filename = null;
    private ExtAudioRecorder mRecorder = null;
    private boolean compressed = false;
    private MediaPlayer mPlayer = null;
    private Visualizer mVisualizer;
    private VisualizerView mVisualizerView;

    public AudioPlayer(VisualizerView visualizerView) {
        filename = Environment.getExternalStorageDirectory().getAbsolutePath();
        filename += AUDIO_FILE_PATH;
        mVisualizerView = visualizerView;
        mVisualizer = null;
    }

    public void startPlaying() {
        mPlayer = new MediaPlayer();
        Log.d(CLASS_NAME, "MediaPlayer audio session ID: " + mPlayer.getAudioSessionId());

        // Create the Visualizer object and attach it to our media player.
        mVisualizer = new Visualizer(mPlayer.getAudioSessionId());
        mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
        mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
            public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                              int samplingRate) {
                //Log.d(CLASS_NAME, "onWaveFormCapture");

                mVisualizerView.updateVisualizer(bytes);
            }

            public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int samplingRate) {
                // TODO: just for learning
                //Log.d(CLASS_NAME, "onFFTDataCapture");
                float[] fft = new float[bytes.length/2];
                for (int i = 0; i < fft.length; i++) {
                    //Log.d(CLASS_NAME, "Data: R= " + (int) bytes[i*2] + " I= " + (int) bytes[(i*2)+1]);
                    float real = (bytes[(i*2)+0])/128.0f;
                    float imag = (bytes[(i*2)+1])/128.0f;
                    fft[i] = ((real*real) + (imag*imag));
                    //Log.d(CLASS_NAME, "Data: R= " + real + " I= " + imag + " FFT: " + fft[i]);
                    float mag = (float) Math.sqrt((double) fft[i]);
                    float phase = (float) Math.atan2((double) imag, (double) real);
                    //Log.d(CLASS_NAME, "Mag: " + mag + " Phase: " + phase);
                    // Reconstruction
                    float real_mod = (float) mag * (float) Math.cos(phase);
                    float imag_mod = (float) mag * (float) Math.sin(phase);
                    //Log.d(CLASS_NAME, "Mag Mod: " + real_mod + " Phase Mod: " + imag_mod);
                }

            }
        }, Visualizer.getMaxCaptureRate() / 2, true, true);
        mVisualizer.setEnabled(true);

        try {
            mPlayer.setDataSource(filename);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e(CLASS_NAME, "prepare() failed");
        }
    }

    public void stopPlaying() {
        if(mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
        }

        if(mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }

    }

    public void startRecording() {
        //mRecorder = new MediaRecorder();
        mRecorder = ExtAudioRecorder.getInstance(compressed);

        try {

            //mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mRecorder.setOutputFile(filename);
            //mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mRecorder.prepare();


        } catch (Exception e) {
            Log.e(CLASS_NAME, "prepare() failed");
        }

        mRecorder.start();
    }

    public void stopRecording() {
        if(mRecorder != null) {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
        }
    }

    public void release() {
        if (mRecorder != null) {
            mRecorder.release();
            mRecorder = null;
        }

        if (mPlayer != null) {
            mPlayer.release();
            mPlayer = null;
        }

        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }

}
