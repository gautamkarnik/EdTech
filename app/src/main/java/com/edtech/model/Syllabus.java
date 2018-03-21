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
public class Syllabus implements Serializable {
    private final String CLASS_NAME = getClass().getSimpleName();

    public static final String TAG_APPLICATION = "application";
    public static final String TAG_VERSION = "lesson_model_version";
    public static final String TAG_SYLLABUS = "syllabus";

    private String applicationName;
    private String version;
    private ArrayList<Course> syllabus;

    public Syllabus () {

    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public String getAppVersion() {
        return this.version;
    }

    public ArrayList<Course> getSyllabus() {
        return this.syllabus;
    }

    // Decodes module json results into data model objects
    public static Syllabus fromJson (JSONObject jsonObject) {
        Syllabus s = new Syllabus();

        try {
            s.applicationName = jsonObject.getString(TAG_APPLICATION);
            s.version = jsonObject.getString(TAG_VERSION);
            s.syllabus = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) jsonObject.get(TAG_SYLLABUS);
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject courseObj = jsonArray.getJSONObject(i);
                Course course = Course.fromJson(courseObj);
                s.syllabus.add(course);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return s;
    }

}
