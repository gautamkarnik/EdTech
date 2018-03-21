package com.edtech.activities;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.edtech.AppPermissions;
import com.edtech.Constants;
import com.edtech.EdTech;
import com.edtech.R;
import com.edtech.application.Settings;
import com.edtech.bluetooth.BluetoothChatFragment;
import com.edtech.helpers.Camera;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class ProfileActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener {

    private final String CLASS_NAME = getClass().getSimpleName();
    private Settings mSettings;
    private RadioGroup radioLanguages;
    private RadioGroup radioMode;
    private Switch onLineSwitch;
    private ProfileNameEditText mProfileName;
    private ImageButton mProfilePic;
    private Camera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mSettings = ((EdTech) getApplication()).getSettings();
        mSettings.setDefaultsIfNotSet();

        radioLanguages = (RadioGroup) findViewById(R.id.languages);
        radioLanguages.setOnCheckedChangeListener(this);
        boolean isEnglishOn = mSettings.isEnglishOn();
        int langId = isEnglishOn ? R.id.radioEnglish : R.id.radioSwahili;
        radioLanguages.check(langId);

        radioMode = (RadioGroup) findViewById(R.id.modes);
        radioMode.setOnCheckedChangeListener(this);
        boolean isLearningMode = mSettings.isLearningMode();
        int modeId = isLearningMode ? R.id.radioLearningMode : R.id.radioTestingMode;
        radioMode.check(modeId);

        onLineSwitch = (Switch) findViewById(R.id.onLineSwitch);
        onLineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSettings.setUseOnlineLessons(true);
                } else {
                    mSettings.setUseOnlineLessons(false);
                }
            }
        });
        onLineSwitch.setChecked(mSettings.isUseOnlineLessons());


        // Get local Bluetooth adapter
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            BluetoothChatFragment fragment = new BluetoothChatFragment();
            transaction.replace(R.id.bluetooth_fragment, fragment);
            transaction.commit();
        }

        mProfileName = (ProfileNameEditText) findViewById(R.id.profileName);
        mProfileName.setSettings(mSettings);

        String name = mSettings.getProfileName();
        if (name != null) {
            mProfileName.setText(name);
        }

        mProfilePic = (ImageButton) findViewById(R.id.profilePic);
        Bitmap bitmap = mSettings.getProfilePic();
        if (bitmap != null) {
            mProfilePic.setImageBitmap(bitmap);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case android.R.id.home:
                gotoHome();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        gotoHome();
    }

    void gotoHome() {
        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(mainIntent);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        Log.d(CLASS_NAME, "onCheckedChanged called");

        switch(checkedId) {
            case R.id.radioEnglish:
                mSettings.setEnglishOn(true);
                mSettings.setSwahiliOn(false);
                break;
            case R.id.radioSwahili:
                mSettings.setEnglishOn(false);
                mSettings.setSwahiliOn(true);
                break;
            case R.id.radioLearningMode:
                mSettings.setLearningMode(true);
                mSettings.setTestingMode(false);
                break;
            case R.id.radioTestingMode:
                mSettings.setLearningMode(false);
                mSettings.setTestingMode(true);
                break;
            default:
                break;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(CLASS_NAME, "onStart");
        mCamera = new Camera(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Obtain the shared Tracker instance.
        Tracker tracker = ((EdTech) getApplication()).getDefaultTracker();
        tracker.setScreenName(Constants.VIEW_SCREEN + getClass().getSimpleName());
        tracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(CLASS_NAME, "onStop");

        //ideally want to pull off the interface using:
        //int selected = radioLanguages.getCheckedRadioButtonId();

        mSettings.setEnglishOn(mSettings.isEnglishOn());
        mSettings.setSwahiliOn(mSettings.isSwahiliOn());
        mSettings.setLearningMode(mSettings.isLearningMode());
        mSettings.setTestingMode(mSettings.isTestingMode());
        mSettings.setUseOnlineLessons(mSettings.isUseOnlineLessons());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(CLASS_NAME, "onDestroy");
        mCamera = null;
    }


    public void takePhoto(View view) {
        Log.d(CLASS_NAME, "takePhoto");

        AppPermissions appPermissions = new AppPermissions(this);
        if (appPermissions.checkPermissionForCamera()) {
            if(mCamera.hasCamera() && mCamera.hasCameraApplication()) {
                mCamera.takePhoto();
            }
        } else {
            Toast.makeText(this, this.getString(R.string.missing_permissions_needed), Toast.LENGTH_LONG).show();
        }
    }

    public void displayPhoto(Intent intent) {
        Log.d(CLASS_NAME, "displayPhoto");
        mCamera.displayPhoto(mProfilePic, intent);
    }

    public Bitmap getPhoto() {
        Log.d(CLASS_NAME, "getPhoto");
        BitmapDrawable drawable = (BitmapDrawable) mProfilePic.getDrawable();
        Bitmap bitmap = null;
        if (drawable != null) {
            bitmap = drawable.getBitmap();
        }
        return bitmap;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(CLASS_NAME, "onActivityResult");
        switch(requestCode) {
            case Camera.PHOTO_TAKEN:
                if (resultCode == RESULT_OK) {
                    displayPhoto(intent);
                    mSettings.setProfilePic(getPhoto());
                }
                break;
            default:
                Log.d(CLASS_NAME, "Unknown request code " + Integer.toString(requestCode));
                break;
        }
    }

}
