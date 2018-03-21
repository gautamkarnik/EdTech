package com.edtech.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by gautamkarnik on 2015-05-05.
 * Based on:  https://guides.codepath.com/android/Converting-JSON-to-Models
 */
public class Lesson implements Serializable {

    private final String CLASS_NAME = getClass().getSimpleName();

    public static final String TAG_LESSON = "lesson";
    public static final String TAG_PHONETIC ="phonetic";

    private String content;
    private String phonetic;

    public Lesson () {

    }

    public String getContent() {
        return this.content;
    }

    public String getPhonetic() {
        return this.phonetic;
    }


    // Decodes module json results into data model objects
    public static Lesson fromJson (JSONObject jsonObject) {
        Lesson l = new Lesson();

        try {
            l.content = jsonObject.getString(TAG_LESSON);
            l.phonetic = jsonObject.getString(TAG_PHONETIC);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        // return new object
        return l;
    }
}
