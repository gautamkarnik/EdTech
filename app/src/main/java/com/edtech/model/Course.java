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
public class Course implements Serializable {

    private final String CLASS_NAME = getClass().getSimpleName();

    public static final String TAG_LANGUAGE = "language";
    public static final String TAG_LESSONS = "lessons";

    public static final String TAG_ENGLISH = "English";
    public static final String TAG_SWAHILI = "Swahili";

    private String language;
    private ArrayList<Unit> lessons;

    public Course () {

    }

    public String getLanguage() {
        return this.language;
    }

    public ArrayList<Unit> getLessons() {
        return this.lessons;
    }

    // Decodes module json results into data model objects
    public static Course fromJson (JSONObject jsonObject) {
        Course c = new Course();

        try {
            c.language = jsonObject.getString(TAG_LANGUAGE);
            c.lessons = new ArrayList<>();
            JSONArray jsonArray = (JSONArray) jsonObject.get(TAG_LESSONS);
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject unitObj = jsonArray.getJSONObject(i);
                Unit unit = Unit.fromJson(unitObj);
                c.lessons.add(unit);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        return c;
    }
}
