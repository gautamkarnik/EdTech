package com.edtech.audio;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.edtech.Constants;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.musicg.wave.WaveHeader;

/**
 * Created by gautamkarnik on 2016-02-08.
 */
public class MicrophoneAudioPlayer extends AsyncTask<Void, Void, Void> {
    private final String CLASS_NAME = getClass().getName();

    private File file = new File(Constants.STUDENT_AUDIO_FILE_PATH);

    @Override
    protected Void doInBackground(Void... params) {

        int audioSampleRate = 44100;
        try {
            WaveHeader waveHeader = new WaveHeader(new FileInputStream(file));
            Log.d(CLASS_NAME, "Playback WAV Header Valid: " + waveHeader.isValid());
            audioSampleRate = waveHeader.getSampleRate();
            Log.d(CLASS_NAME, "Playback Sample Rate: " + audioSampleRate);
        } catch (IOException e) {
            e.printStackTrace();
        }

        int minBufferSize = AudioTrack.getMinBufferSize(
                audioSampleRate,
                Constants.AUDIO_PLAYER_CHANNEL,
                Constants.AUDIO_RECORDER_ENCODING);

        AudioTrack audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                audioSampleRate,
                Constants.AUDIO_PLAYER_CHANNEL,
                Constants.AUDIO_RECORDER_ENCODING,
                minBufferSize,
                AudioTrack.MODE_STREAM);

        Log.d(CLASS_NAME, "Audio Track State: " + audioTrack.getState());
        Log.d(CLASS_NAME, "Audio Track Buffer Size: " + minBufferSize);

        byte[] audioBuffer = new byte[minBufferSize];

        if ((audioTrack != null) && (audioTrack.getState() != AudioTrack.STATE_UNINITIALIZED)) {

            audioTrack.play();

            try {
                DataInputStream dis = new DataInputStream(
                                           new BufferedInputStream(
                                                new FileInputStream(file)));
                // skip the header
                dis.skip(44);

                while (dis.read(audioBuffer, 0, audioBuffer.length) > 0) {
                    audioTrack.write(audioBuffer, 0, audioBuffer.length);
                }
                dis.close();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            audioTrack.stop();
            audioTrack.release();
        }

        return null;
    }
}
