package com.edtech.utilities;

import android.util.Log;

import com.edtech.application.Scores;
import com.edtech.application.Settings;
import com.edtech.model.Course;
import com.edtech.model.Syllabus;
import com.edtech.model.Unit;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by gautamkarnik on 2015-05-06.
 */
public class LessonParser {

    private static final String CLASS_NAME = LessonParser.class.getSimpleName();

    /* Parses through a JSON string and loads the listView data structures */
    public static Syllabus parseLessonModel(String jsonStr) {

        Syllabus s = null;

        if (jsonStr != null) {
            try {
                s = new Syllabus();
                JSONObject jsonSyllabus = new JSONObject(jsonStr);
                s = Syllabus.fromJson(jsonSyllabus);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.e(CLASS_NAME, "Could not load JSON Lessons.");
        }

        return s;
    }

    public static ArrayList<HashMap<String, String>> extractUnits(Syllabus syllabus, Settings settings, Scores scores) {

        // Hashmap unitList
        ArrayList<HashMap<String, String>> unitList;
        unitList = new ArrayList<HashMap<String, String>>();

        if ((syllabus!= null) && (settings != null) && (scores != null)) {
            ArrayList<Course> courses = syllabus.getSyllabus();
            for (int i = 0; i < courses.size(); i++) {
                ArrayList<Unit> lessons = courses.get(i).getLessons();
                String language = courses.get(i).getLanguage();
                String selectedLanguage = Course.TAG_ENGLISH;

                if (settings.isSwahiliOn()) selectedLanguage = Course.TAG_SWAHILI;

                if (language.equals(selectedLanguage)) {
                    for (int j = 0; j < lessons.size(); j++) {
                        Unit unit = lessons.get(j);
                        // put each unit into the list data structure
                        HashMap<String, String> unitItem = new HashMap<String, String>();
                        unitItem.put(Unit.TAG_UNIT, unit.getUnit());
                        unitItem.put(Unit.TAG_TITLE, unit.getTitle());
                        unitItem.put(Scores.TAG_SCORE, Scores.TAG_SCORE + ": " +
                                Integer.toString(scores.getUnitScore(
                                        unit.getUnit(),
                                        unit.getTitle())));
                        unitList.add(unitItem);
                    }
                }
            }
        }

        return unitList;
    }
}
