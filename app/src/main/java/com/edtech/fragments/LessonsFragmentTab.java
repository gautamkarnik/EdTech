package com.edtech.fragments;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.edtech.R;
import com.edtech.application.Scores;
import com.edtech.application.Settings;
import com.edtech.utilities.LessonParser;
import com.edtech.model.Syllabus;
import com.edtech.model.Unit;
import com.edtech.utilities.ToolTipGenerator;

import java.util.ArrayList;
import java.util.HashMap;

import it.sephiroth.android.library.tooltip.Tooltip;

public class LessonsFragmentTab extends Fragment implements AdapterView.OnItemClickListener {

    private final String CLASS_NAME = getClass().getSimpleName();
    private static final String SYLLABUS_KEY = "syllabus_key";

    OnUnitSelectedListener mCallback;
    // Container Activity must implement this interface
    public interface OnUnitSelectedListener extends BaseFragmentTab.SlateInteractions {
        public void onUnitSelected(String unit, String title);
    }

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> unitList;

    private ListView lv;
    private Syllabus syllabus;
    private Settings settings;
    private Scores scores;

    // lesson running status
    private boolean[] status;

    private boolean toolTipGuard = true;

    public LessonsFragmentTab() {

    }

    public static LessonsFragmentTab newInstance(Syllabus syllabus) {
        LessonsFragmentTab fragment = new LessonsFragmentTab();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SYLLABUS_KEY, syllabus);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // retrieve the syllabus object using the get/setArguments() interface
        Bundle arguments = getArguments();
        if (arguments != null) {
            syllabus = (Syllabus) arguments.getSerializable(SYLLABUS_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_lessons, container, false);

        unitList = new ArrayList<HashMap<String, String>>();
        lv = (ListView) rootView.findViewById(R.id.lessonsView);
        lv.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        lv.setOnItemClickListener(this);

        updateList(rootView.getContext());

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCallback.getSettings().isFirstTimeLaunch() && toolTipGuard) {
            if (lv.getChildCount() > 0) {
                ToolTipGenerator.showTooltip(this.getContext(), lv.getChildAt(0), getString(R.string.click_on_a_lesson), Tooltip.Gravity.TOP);
                toolTipGuard = false;
            }
        }
    }

    public void refresh() {
        updateList(this.getActivity().getApplicationContext());
    }

    public void updateList(Context context) {
        //extract unit List for list view
        // update list based on what language is selected
        settings = mCallback.getSettings();
        scores = mCallback.getScores();
        unitList = LessonParser.extractUnits(syllabus, settings, scores);

        SimpleAdapter adapter = new SimpleAdapter(context,
                unitList,
                R.layout.list_item,
                new String[] {Unit.TAG_UNIT, Unit.TAG_TITLE, Scores.TAG_SCORE},
                new int[] {R.id.unit, R.id.title, R.id.score});

        lv.setAdapter(adapter);

        for (int i=0; i < unitList.size(); i++) {
            lv.setItemChecked(i, false);

            String unit = unitList.get(i).get(Unit.TAG_UNIT);
            String title = unitList.get(i).get(Unit.TAG_TITLE);
            String key = unit + ":" + title;
            //Log.d(CLASS_NAME, "Search Key: " + key);
            int hashCode = key.hashCode();
            if(mCallback.isLessonRunning()) {
                if (mCallback.getLessonThread().getHashCode() == hashCode) {
                    Log.d(CLASS_NAME, "Got it: [i] " + i + " " + unit + ":" + title);
                    lv.setItemChecked(i, true);
                    lv.setSelection(i); // might not be needed, but just setting selection anyways
                }
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // get values from selected List Item
        String unit = ((TextView) view.findViewById(R.id.unit)).getText().toString();
        String title = ((TextView) view.findViewById(R.id.title)).getText().toString();

        // Send the event to the host activity
        mCallback.onUnitSelected(unit, title);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface.  if not, it throws an exception.
        try {
            mCallback = (OnUnitSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException((activity.toString() +
                    " must implement OnUnitSelectedListener"));
        }
    }

}
