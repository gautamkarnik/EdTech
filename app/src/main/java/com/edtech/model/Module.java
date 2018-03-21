package com.edtech.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by gautamkarnik on 2015-05-05.
 * Based on:  https://guides.codepath.com/android/Converting-JSON-to-Models
 */
public class Module implements Serializable {

    private final String CLASS_NAME = getClass().getSimpleName();

    public static final String TAG_SUBTITLE = "subtitle";
    public static final String TAG_SUBJECT = "subject";
    public static final String TAG_DIFFICULTY = "difficulty";
    public static final String TAG_EXERCISES = "exercises";

    private String subTitle;
    private String subject;
    private String difficulty;
    private ArrayList<Lesson> exercises;

    public Module () {

    }

    public String getSubTitle() {
        return this.subTitle;
    }

    public String getSubject() {
        return this.subject;
    }

    public String getDifficulty() {
        return this.difficulty;
    }

    public ArrayList<Lesson> getExercises() {
        return this.exercises;
    }

    // Decodes module json results into data model objects
    public static Module fromJson (JSONObject jsonObject) {
        Module m = new Module();

        try {
            m.subTitle = jsonObject.getString(TAG_SUBTITLE);
            m.subject = jsonObject.getString(TAG_SUBJECT);
            m.difficulty = jsonObject.getString(TAG_DIFFICULTY);
            m.exercises = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) jsonObject.get(TAG_EXERCISES);
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject lessonObj = jsonArray.getJSONObject(i);
                Lesson lesson = Lesson.fromJson(lessonObj);
                m.exercises.add(lesson);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        // return new object
        return m;
    }

}
