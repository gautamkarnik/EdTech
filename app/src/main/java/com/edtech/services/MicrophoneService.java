package com.edtech.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.edtech.Constants;
import com.edtech.R;
import com.edtech.activities.MessagingConstants;
import com.edtech.audio.MicrophoneAudioSampler;
import com.edtech.audio.PolarView;
import com.edtech.audio.VisualizerView;
import com.musicg.dsp.Resampler;
import com.musicg.fingerprint.FingerprintSimilarity;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveFileManager;
import com.musicg.wave.WaveHeader;

import java.io.File;
import java.util.concurrent.Semaphore;

/**
 * Created by gautamkarnik on 2015-10-13.
 */
public class MicrophoneService extends Service {

    private final String CLASS_NAME = getClass().getName();
    private MicrophoneAudioSampler microphoneAudioSampler;
    private boolean running;
    private final IBinder binder;
    private static final Semaphore semaphore = new Semaphore(1, true);

    public MicrophoneService() {
        binder = new MicrophoneBinder();
    }

    @Override
    public void onCreate() {
        Log.d(CLASS_NAME, "OnCreate");
        running = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(CLASS_NAME, "onStartCommand");
        return START_STICKY;  // always run in the background
    }

    @Override
    public void onDestroy() {
        Log.d(CLASS_NAME, "onDestroy");
        running = false;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public class MicrophoneBinder extends Binder {
        public MicrophoneService getService() {
            return MicrophoneService.this;
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void startMicrophoneAudio(VisualizerView audioView, PolarView polarView) {
        Log.d(CLASS_NAME, "startMicrophoneAudio");


        try {
            semaphore.acquire();
            microphoneAudioSampler = MicrophoneAudioSampler.getInstance();
            if (microphoneAudioSampler != null) {
                microphoneAudioSampler.setVisualizers(audioView, polarView);
                // make sure thread is not running already
                if (microphoneAudioSampler.getStatus() != AsyncTask.Status.RUNNING) {
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                        microphoneAudioSampler.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        microphoneAudioSampler.execute();
                }
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopMicrophoneAudio() {
        Log.d(CLASS_NAME, "stopMicrophoneAudio");
        if (microphoneAudioSampler != null) {
            microphoneAudioSampler.setRunning(false);
            microphoneAudioSampler = null;
        }
        semaphore.release();
    }

    public void startRecording() {
        if (microphoneAudioSampler != null) {
            Log.d(CLASS_NAME, "startRecording");
            microphoneAudioSampler.setRecording(true);
        }
    }

    public void stopRecording() {
        if (microphoneAudioSampler != null) {
            Log.d(CLASS_NAME, "stopRecording");
            microphoneAudioSampler.setRecording(false);
        }
    }

    public float compareVoiceFiles() {
        // compare student to tts because sampling for tts varies per device
        Log.d(CLASS_NAME, "compareVoiceFiles");

        String f1 = Constants.TTS_AUDIO_FILE_PATH;
        String f2 = Constants.STUDENT_AUDIO_FILE_PATH;

        try {
            // check if files exist
            File check1 = new File(f1);
            File check2 = new File(f2);

            if (!check1.exists() || !check2.exists()) {
                return 0;
            }

            //resample to 8000 Hz sampling rate for musicg engine
            Wave w1 = resample(f1).getWave();
            Wave w2 = resample(f2).getWave();
            if ((w1 != null) && (w2 != null)) {
                // compare student to tts because sampling for tts varies per device
                FingerprintSimilarity fps = w2.getFingerprintSimilarity(w1);
                float sim = fps.getSimilarity();
                Log.d(CLASS_NAME, "Similarity: " + String.format("%3.10f", sim));
                return sim;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    // Source: http://www.programcreek.com/java-api-examples/index.php?api=com.musicg.wave.Wave
    private WaveFileManager resample(String src) {
        //down sample to 8 kHz for best results
        Wave wave = new Wave(src);
        Resampler resampler = new Resampler();
        int sourceRate = wave.getWaveHeader().getSampleRate();
        int targetRate = Constants.AUDIO_RESAMPLE_RATE;

        // resample the source
        byte[] resampledWaveData = resampler.reSample(wave.getBytes(),
                wave.getWaveHeader().getBitsPerSample(),
                sourceRate,
                targetRate);
        WaveHeader resampledWaveHeader = wave.getWaveHeader();
        resampledWaveHeader.setSampleRate(targetRate);

        Wave resampledWave = new Wave(resampledWaveHeader, resampledWaveData);
        WaveFileManager wfm = new WaveFileManager(resampledWave);
        return wfm;
    }
}
