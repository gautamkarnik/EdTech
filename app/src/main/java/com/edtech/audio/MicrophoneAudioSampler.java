package com.edtech.audio;

import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.edtech.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * Created by gautamkarnik on 2015-08-18.
 */
public class MicrophoneAudioSampler extends AsyncTask<Void, Void, Void> {

    private final String CLASS_NAME = getClass().getName();
    private final static int MAX_BUFFER_SIZE = 5 * 1024 * 1024; // 5 megabytes

    public AudioRecord audioRecorder;
    private int bufferSize;
    private int framePeriod;

    public enum State {INITIALIZING, READY, CAPTURING, RECORDING, ERROR, STOPPED};
    public State state;

    private final static int[] sampleRates = {44100, 22050, 11025, 8000};
    private int audioSampleRate = sampleRates[0];

    private PipedOutputStream pipedOutputStream;
    private PipedInputStream pipedInputStream;
    private ByteArrayInputStream byteArrayInputStream;
    private ByteArrayOutputStream byteArrayOutputStream;
    private int frameNumber = 0;

    byte[] buffer;
    byte[] eightBit;
    short[] sixteenBit;

    private boolean isRunning = false;
    private boolean isRecording = false;

    private VisualizerView visualizerView;
    private PolarView polarView;

    private static MicrophoneAudioSampler mAsyncTaskInstance;

    public static synchronized MicrophoneAudioSampler getInstance () {

        // if the current async task is already running, return null: no new async task
        // shall be created if an instance is already running
        if ((mAsyncTaskInstance != null) && mAsyncTaskInstance.getStatus() == Status.RUNNING) {
            if (mAsyncTaskInstance.isCancelled()) {
                mAsyncTaskInstance = new MicrophoneAudioSampler();
            } else {
                // log error and re-instantiate the task if needed otherwise try again later and return null
                // execute the task right away for better performance
                Log.d("MicrophoneAyncTask", "Task already running. State: " + mAsyncTaskInstance.state);
                Log.d("MicrophoneAycnTask", "Task already running. Running: " + mAsyncTaskInstance.isRunning);
                Log.d("MicrophoneAycnTask", "Task already running. Recording: " + mAsyncTaskInstance.isRecording);
                if ((mAsyncTaskInstance.state == State.CAPTURING)
                     && (mAsyncTaskInstance.isRunning == false)
                     && (mAsyncTaskInstance.isRecording == false)) {
                    mAsyncTaskInstance = new MicrophoneAudioSampler();
                    if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.HONEYCOMB)
                        mAsyncTaskInstance.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    else
                        mAsyncTaskInstance.execute();
                } else {
                    return null;
                }
            }
        }

        // if the current async task is pending, it can be executed return this instance
        if ((mAsyncTaskInstance != null) && mAsyncTaskInstance.getStatus() == Status.PENDING) {
            return mAsyncTaskInstance;
        }

        // if the current async task is finished, it can't be executed another time,
        // so return a new instance
        if ((mAsyncTaskInstance != null) && mAsyncTaskInstance.getStatus() == Status.FINISHED) {
            mAsyncTaskInstance = new MicrophoneAudioSampler();
        }

        // if the current async task is null, create a new instance
        if ((mAsyncTaskInstance == null)) {
            mAsyncTaskInstance = new MicrophoneAudioSampler();
        }

        // return the current instance
        return mAsyncTaskInstance;

    }

    protected AudioRecord.OnRecordPositionUpdateListener updateListener = new AudioRecord.OnRecordPositionUpdateListener() {
        public void onPeriodicNotification(AudioRecord recorder) {
            try {
                if ((recorder != null)) {
                    if (recorder.read(buffer, 0, buffer.length) > 0) {
                        pipedOutputStream.write(buffer, 0, buffer.length);
                        // down sample the 16 bit PCM to 8 bit
                        if ((visualizerView != null) && (polarView != null) && (buffer != null)) {
                            for (int i = 0; i < buffer.length / 2; i++) {
                                sixteenBit[i] = (short) ((buffer[i * 2] & 0xff) | (buffer[i * 2 + 1] << 8));
                                eightBit[i] = (byte) ((sixteenBit[i] / 256) + 128);
                            }

                            visualizerView.updateVisualizer(eightBit);
                            polarView.updateVisualizer(eightBit);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onMarkerReached(AudioRecord recorder) {
            // NOT USED
        }
    };

    private MicrophoneAudioSampler() {
        try {
            int audioSource = MediaRecorder.AudioSource.MIC;
            //int sampleRate = Constants.AUDIO_RECORDER_SAMPLE_RATE;
            int audioFormat = Constants.AUDIO_RECORDER_ENCODING;
            int channelConfig = Constants.AUDIO_RECORDER_CHANNEL;

            int bSamples = Constants.AUDIO_RECORDER_BPS;
            int nChannels = Constants.AUDIO_RECORDER_NUM_CHANNELS;

            int i=0;
            do
            {
                framePeriod = sampleRates[i] * Constants.AUDIO_RECORDER_TIMER_INTERVAL / 1000;
                bufferSize = framePeriod * bSamples * nChannels / 8;
                // Check to make sure buffer size is not smaller than the smallest allowed one
                if (bufferSize < AudioRecord.getMinBufferSize(sampleRates[i], channelConfig, audioFormat)) {
                    // Set frame period and timer interval accordingly
                    bufferSize = AudioRecord.getMinBufferSize(sampleRates[i], channelConfig, audioFormat);
                    framePeriod = bufferSize / ( bSamples * nChannels / 8 );
                    Log.d(CLASS_NAME, "Increasing buffer size to " + Integer.toString(bufferSize));
                }

                audioSampleRate = sampleRates[i];

                Log.d(CLASS_NAME, "Attempting AudioRecord Initialization: ");
                Log.d(CLASS_NAME, "Sample Rate: " + sampleRates[i]);
                Log.d(CLASS_NAME, "Buffer Size: " + bufferSize);
                Log.d(CLASS_NAME, "Frame Period: " + framePeriod);
                audioRecorder = new AudioRecord(audioSource, sampleRates[i], channelConfig, audioFormat, bufferSize);

            } while((++i<sampleRates.length) && (audioRecorder.getState() == AudioRecord.STATE_UNINITIALIZED));

            if (audioRecorder.getState() != AudioRecord.STATE_INITIALIZED) {
                throw new Exception("AudioRecord Initialization failed");
            }

            // TODO: make a more sophisticated buffer size based on LCM or number of FRAME BLOCKS
            pipedInputStream = new PipedInputStream(bufferSize * 10);
            pipedOutputStream = new PipedOutputStream(pipedInputStream);

            Log.d(CLASS_NAME, "Audio Record Sample Rate: " + audioSampleRate);
            Log.d(CLASS_NAME, "Audio Record Buffer Size: " + bufferSize);
            Log.d(CLASS_NAME, "Audio Record Frame Period: " + framePeriod);
            Log.d(CLASS_NAME, "PipedInputStream buffer length: " + bufferSize * 10);

            audioRecorder.setRecordPositionUpdateListener(updateListener);
            audioRecorder.setPositionNotificationPeriod(framePeriod);

            buffer = new byte[bufferSize];
            eightBit = new byte[buffer.length / 2];
            sixteenBit = new short[buffer.length / 2];

            state = State.INITIALIZING;
        } catch (Exception e) {
            if (e.getMessage() != null)
            {
                Log.e(CLASS_NAME, e.getMessage());
            }
            else
            {
                Log.e(CLASS_NAME, "Unknown error occured while initializing recording");
            }
            state = State.ERROR;
        }
    }

    @Override
    protected void onPreExecute() {
        Log.d(CLASS_NAME, "onPreExecute");
        super.onPreExecute();
        state = State.READY;
        frameNumber = 0;
    }

    public void setRunning(boolean running) {
        this.isRunning = running;
    }

    public void setVisualizers(VisualizerView visualizerView, PolarView polarView) {
        this.visualizerView = visualizerView;
        this.polarView = polarView;
    }

    public void setRecording(boolean record) {

        try {
            if (record) {
                state = State.RECORDING;
                byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.reset();
                frameNumber = 0;
            } else {
                state = State.CAPTURING;
                byteArrayOutputStream.close();
                createWavFileFromByteStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.isRecording = record;

    }

    @Override
    protected Void doInBackground(Void... params) {

        state = State.CAPTURING;
        byte[] temp = new byte[bufferSize];
        byte[] frameBuffer = new byte[bufferSize];
        isRunning = true;

        Log.d(CLASS_NAME, "doInBackground");

        try {

            audioRecorder.startRecording();
            audioRecorder.read(temp, 0, temp.length);

            while (isRunning) {
                Thread.sleep(Constants.AUDIO_RECORDER_TIMER_INTERVAL);
                if ((pipedInputStream.read(frameBuffer, 0, frameBuffer.length)) > -1) {
                    if(isRecording) {
                        try {
                            // write frame to raw buffer and cap it at 5 MB
                            if ((byteArrayOutputStream != null) &&
                                 byteArrayOutputStream.size() <= MAX_BUFFER_SIZE) {
                                byteArrayOutputStream.write(frameBuffer, 0, frameBuffer.length);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            isRecording = false;
                            state = State.CAPTURING;
                        }
                        Log.d(CLASS_NAME, "Frame number: " + frameNumber);
                        frameNumber++;
                    }
                }
            }

            audioRecorder.setRecordPositionUpdateListener(null);
            audioRecorder.stop();
            audioRecorder.release();
            pipedInputStream.close();
            pipedOutputStream.close();
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
                isRecording = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        Log.d(CLASS_NAME, "onPostExecute");
        super.onPostExecute(aVoid);
        state = State.STOPPED;
    }

    // based on source: http://stackoverflow.com/questions/26770993/comparing-sound-file-and-realtime-voice-with-musicg
    private void createWavFileFromByteStream(){
        Log.d(CLASS_NAME, "createWavFileFromByteStream");
        FileOutputStream out = null;
        int totalAudioLen = 0;
        int totalDataLen = 0;

        byte[] data = new byte[bufferSize];

        try {
            byteArrayInputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
            out = new FileOutputStream(Constants.STUDENT_AUDIO_FILE_PATH, false);

            // max file size is 5 MB, so should be okay to cast
            totalAudioLen = byteArrayInputStream.available();
            totalDataLen = totalAudioLen + 44;
            Log.d(CLASS_NAME, "File size: " + totalDataLen);

            //short format, short numChannels, int sampleRate, short bitsPerSample, int numBytes
            // should be okay to cast to short since using MONO and 16 BPS
            WaveHeader waveHeader = new WaveHeader(WaveHeader.FORMAT_PCM,
                    (short) Constants.AUDIO_RECORDER_NUM_CHANNELS,
                    audioSampleRate,
                    (short) Constants.AUDIO_RECORDER_BPS,
                    totalAudioLen);
            waveHeader.write(out);

            while(byteArrayInputStream.read(data) != -1){
                out.write(data, 0, data.length);
            }

            byteArrayInputStream.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

