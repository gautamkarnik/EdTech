package com.edtech.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.edtech.Constants;
import com.edtech.EdTech;
import com.edtech.R;
import com.edtech.application.Scores;
import com.edtech.model.Course;
import com.edtech.model.Lesson;
import com.edtech.model.Module;
import com.edtech.model.Syllabus;
import com.edtech.model.Unit;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

/**
 * Created by gautamkarnik on 2015-05-13.
 */
public class LessonManagerTask extends AsyncTask<Object, Integer, String> {

    private final String CLASS_NAME = getClass().getSimpleName();

    private Syllabus mSyllabus;
    private Activity mActivity;
    private Scores mScores;
    private ProgressBar mProgress;
    private int mProgressPercent;
    private static LessonManagerTask mAsyncTaskInstance = null;
    private int hashCode;

    private static final long WAIT_TIME_INTERVAL_MILLIS = 30000; // 30 seconds

    // Define a handler that recieves messages to update the score
    // Handler for incoming messages from clients
    public class LessonHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Intent intent = new Intent(MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT);

            switch(msg.what) {
                case MessagingConstants.MSG_CLEAR_SCORE:
                    mScore = 0;
                    intent.putExtra(MessagingConstants.MSG_DATA_KEY_SCORE, mScore);
                    mActivity.sendBroadcast(intent);
                    break;
                case MessagingConstants.MSG_INCREMENT_SCORE_5:
                    mScore += 5;
                    intent.putExtra(MessagingConstants.MSG_DATA_KEY_SCORE, mScore);
                    mActivity.sendBroadcast(intent);
                    break;
                case MessagingConstants.MSG_INCREMENT_SCORE_10:
                    mScore += 10;
                    intent.putExtra(MessagingConstants.MSG_DATA_KEY_SCORE, mScore);
                    mActivity.sendBroadcast(intent);
                    break;
                case MessagingConstants.MSG_CANCEL_THREAD:
                    String cancelSlateName = msg.getData().getString(MessagingConstants.MSG_DATA_KEY);
                    String cancel = MessagingConstants.TAG_CANCEL;
                    intent.putExtra(MessagingConstants.MSG_DATA_KEY_VIEW, cancel);
                    intent.putExtra(MessagingConstants.MSG_DATA_KEY_CLEAR, cancelSlateName);
                    mActivity.sendBroadcast(intent);
                    break;
                case MessagingConstants.MSG_POKE_THREAD:
                    synchronized (mMonitor) {
                        mMonitor.notifyAll();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final LessonHandler mLessonHandler = new LessonHandler();
    private int mScore = 0;

    public void sendMessage(int messageId) {
        Message msg = mLessonHandler.obtainMessage();
        msg.what = messageId;
        mLessonHandler.sendMessage(msg);
    }

    private void dataMessage(int messageId, String data) {
        if ((data != null) && (!data.equals(""))) {
            Message msg = mLessonHandler.obtainMessage();
            msg.what = messageId;
            Bundle b = new Bundle();
            b.putString(MessagingConstants.MSG_DATA_KEY, data);
            msg.setData(b);
            mLessonHandler.sendMessage(msg);
        }
    }

    // acts as a binary semaphore when 1 max available
    private static final int MAX_AVAILABLE = 1;
    private static final Semaphore mSemaphore = new Semaphore(MAX_AVAILABLE, true);

    // monitor object for wait/notify operations
    private static final Object mMonitor = new Object();

    public LessonManagerTask(Activity activity, Syllabus syllabus, Scores scores,
                             ProgressBar progress) {
        this.mActivity = activity;
        this.mSyllabus = syllabus;
        this.mScores = scores;
        this.mProgress = progress;
    }

    // Source: http://stackoverflow.com/questions/12236899/how-to-check-if-async-task-is-already-running
    public static LessonManagerTask getInstance(Activity activity, Syllabus syllabus, Scores scores,
                                                ProgressBar progress) {
        // if the current async task is already running, return null: no new async task
        // shall be created if an instance is already running
        if ((mAsyncTaskInstance != null) && mAsyncTaskInstance.getStatus() == Status.RUNNING) {
            if (mAsyncTaskInstance.isCancelled()) {
                mAsyncTaskInstance = new LessonManagerTask(activity, syllabus, scores, progress);
            } else {
                // log error and try again later
                Log.d("LessonManagerAyncTask", "Task already running, try later");
                return null;
            }
        }

        // if the current async task is pending, it can be executed return this instance
        if ((mAsyncTaskInstance != null) && mAsyncTaskInstance.getStatus() == Status.PENDING) {
            return mAsyncTaskInstance;
        }

        // if the current async task is finished, it can't be executed another time,
        // so return a new instance
        if ((mAsyncTaskInstance != null) && mAsyncTaskInstance.getStatus() == Status.FINISHED) {
            mAsyncTaskInstance = new LessonManagerTask(activity, syllabus, scores, progress);
        }

        // if the current async task is null, create a new instance
        if ((mAsyncTaskInstance == null)) {
            mAsyncTaskInstance = new LessonManagerTask(activity, syllabus, scores, progress);
        }

        // return the current instance
        return mAsyncTaskInstance;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressPercent = 0;
        mScore = 0;
        mProgress.setProgress(0);
    }

    @Override
    protected String doInBackground(Object... params) {

        String unitMatch = (String) params[0];
        String titleMatch = (String) params[1];
        boolean isTestingMode = (boolean) params[2];
        String result = "";

        // prevents an exercise from being cleared prematurely
        // in the broadcast receiver if the thread is cancelled
        mSemaphore.acquireUninterruptibly();

        Intent flashBarIntent = new Intent(MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT);
        flashBarIntent.putExtra(MessagingConstants.MSG_DATA_KEY_FLASHBAR, MessagingConstants.FLASH_BAR_ON);
        mActivity.sendBroadcast(flashBarIntent);

        sendMessage(MessagingConstants.MSG_CLEAR_SCORE);

        // Obtain the shared Tracker instance.
        // TODO: Consider moving the Google Analytics to pre- and post-execute + onCancelled
        // TODO: Requires shifting around of data, right now all required info is in doInBackground
        Tracker tracker = ((EdTech) mActivity.getApplication()).getDefaultTracker();

        // record Google Analytics start event
        if (isTestingMode) {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_SELECTED)
                    .setAction(Constants.ACTION_TEST_STARTED)
                    .setLabel(unitMatch + ":" + titleMatch)
                    .build());
        } else {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_SELECTED)
                    .setAction(Constants.ACTION_LESSON_STARTED)
                    .setLabel(unitMatch + ":" + titleMatch)
                    .build());
        }


        if (mSyllabus != null) {
            ArrayList<Course> courses = mSyllabus.getSyllabus();
            for (int i = 0; i < courses.size(); i++) {
                ArrayList<Unit> lessons = courses.get(i).getLessons();
                for (int j = 0; j < lessons.size(); j++) {
                    Unit unit = lessons.get(j);
                    if (unitMatch.equals(unit.getUnit()) && titleMatch.equals(unit.getTitle())) {
                        ArrayList<Module> modules = lessons.get(j).getModules();
                        for (int k = 0; k < modules.size(); k++) {
                            ArrayList<Lesson> exercises = modules.get(k).getExercises();
                            String subject = modules.get(k).getSubject();
                            Integer difficulty = Integer.parseInt(modules.get(k).getDifficulty());

                            for (int l = 0; l < exercises.size(); l++) {
                                // extract the lesson content, create the fragment with the exercise
                                String lesson = exercises.get(l).getContent();
                                String phonetic = exercises.get(l).getPhonetic();
                                String slate = subject;

                                Intent intent = new Intent(MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT);
                                intent.putExtra(MessagingConstants.MSG_DATA_KEY_VIEW, slate);
                                intent.putExtra(MessagingConstants.MSG_DATA_KEY_LESSON, lesson);
                                intent.putExtra(MessagingConstants.MSG_DATA_KEY_PHONETIC, phonetic);
                                mActivity.sendBroadcast(intent);

                                // sleep 30 seconds
                                // make this a function of difficulty
                                // duration = 30 sec x difficulty level
                                synchronized (mMonitor) {
                                    try {
                                        //Thread.sleep(30000);
                                        Log.d(CLASS_NAME, "Difficulty: " + difficulty);
                                        long millis = difficulty * WAIT_TIME_INTERVAL_MILLIS;

                                        Intent countdownIntent = new Intent(MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT);
                                        countdownIntent.putExtra(MessagingConstants.MSG_DATA_KEY_COUNTDOWN, millis);
                                        mActivity.sendBroadcast(countdownIntent);

                                        mMonitor.wait(millis);
                                    } catch (InterruptedException ie) {
                                        //Thread.interrupted();
                                        Log.d(CLASS_NAME, "Lesson thread Interrupted.");
                                    }
                                }

                                if (this.isCancelled()) {
                                    // ask the thread to broadcast cancellation back to caller
                                    dataMessage(MessagingConstants.MSG_CANCEL_THREAD, slate);

                                    // Record the Google Analytics cancel event
                                    if (isTestingMode) {
                                        tracker.send(new HitBuilders.EventBuilder()
                                                .setCategory(Constants.CATEGORY_CANCEL)
                                                .setAction(Constants.ACTION_TEST_ABORTED)
                                                .setLabel(unitMatch + ":" + titleMatch)
                                                .build());
                                    } else {
                                        tracker.send(new HitBuilders.EventBuilder()
                                                .setCategory(Constants.CATEGORY_CANCEL)
                                                .setAction(Constants.ACTION_LESSON_ABORTED)
                                                .setLabel(unitMatch + ":" + titleMatch)
                                                .build());
                                    }

                                    // exit the thread and report the slate which was cancelled
                                    result = slate;
                                    return result;
                                }

                                // allows a bit of time for avataar + animations to do their thing
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }

                                // can't calculate total exercises on the fly
                                // using a different method
                                float percentIncrement = 1 / (float) exercises.size() / (float) modules.size() * 100;
                                mProgressPercent += (int) percentIncrement;
                                publishProgress(mProgressPercent);
                            }

                        }
                    }
                }
            }
        }

        // record the score if we are in testing mode otherwise
        // just note that the lesson is finished
        // record the Google Analytics Event
        if (isTestingMode) {
            mScores.setUnitScore(unitMatch, titleMatch, mScore);
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_ACHIEVEMENT)
                    .setAction(Constants.ACTION_TEST_COMPLETED)
                    .setLabel(unitMatch + ":" + titleMatch)
                    .setValue(mScore)
                    .build());
            result = mActivity.getString(R.string.score) + " " + Integer.toString(mScore);
        } else {
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(Constants.CATEGORY_ACHIEVEMENT)
                    .setAction(Constants.ACTION_LESSON_COMPLETED)
                    .setLabel(unitMatch + ":" + titleMatch)
                    .setValue(mScore)
                    .build());
            result = mActivity.getString(R.string.lesson_complete_text);
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        Log.d(CLASS_NAME, "Percent progress: " + progress[0]);
        mProgress.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);

        // move back to the lesson list
        String slate = MessagingConstants.FRAGMENT_TAG_LESSONS_LIST;
        Intent intent = new Intent(MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT);
        intent.putExtra(MessagingConstants.MSG_DATA_KEY_VIEW, slate);
        mActivity.sendBroadcast(intent);

        // Would like to print out the total number of lessons run and correct.
        // To do this need to change the return value of doInBackground result argument
        Toast.makeText(mActivity.getApplicationContext(), result, Toast.LENGTH_SHORT).show();
        Log.d(CLASS_NAME, result);

        Intent flashBarIntent = new Intent(MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT);
        flashBarIntent.putExtra(MessagingConstants.MSG_DATA_KEY_FLASHBAR, MessagingConstants.FLASH_BAR_OFF);
        mActivity.sendBroadcast(flashBarIntent);

        mSemaphore.release();
    }

    @Override
    protected void onCancelled(String result) {
        Log.d(CLASS_NAME, "Cancelled lessons for " + result + ".");

        Intent flashBarIntent = new Intent(MessagingConstants.KEY_INTENT_CHANGE_FRAGMENT);
        flashBarIntent.putExtra(MessagingConstants.MSG_DATA_KEY_FLASHBAR, MessagingConstants.FLASH_BAR_OFF);
        mActivity.sendBroadcast(flashBarIntent);

        mSemaphore.release();
    }

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }
}
