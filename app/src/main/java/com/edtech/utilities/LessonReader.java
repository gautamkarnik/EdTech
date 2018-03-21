package com.edtech.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;

import com.edtech.Constants;
import com.edtech.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

/**
 * Created by gautamkarnik on 2015-05-06.
 */
public class LessonReader {

    private static final String CLASS_NAME = LessonReader.class.getSimpleName();

    public static String readDataModel(Context context) {
        String jString = null;

        File file = new File(Constants.LESSONS_FILE_PATH);

        if(file.exists()) {
            Log.d(CLASS_NAME, "Reading Data Model from SD Card");
            jString = readDataModelfromSDCard();
        } else {
            Log.d(CLASS_NAME, "Reading Data Model from Resource Library");
            jString = readDataModelFromLibrary(context);
        }

        return jString;
    }

    // grab the raw JSON data from disk
    public static String readDataModelfromSDCard() {

        // JSON string
        String jString = null;
        File file;

        if (isExternalStorageReadable()) {
            try {
                file = new File(Constants.LESSONS_FILE_PATH);
                FileInputStream stream = new FileInputStream(file);
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                jString = Charset.defaultCharset().decode(bb).toString();
                stream.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        return jString;
    }

    // grab the raw JSON data from raw resource library
    public static String readDataModelFromLibrary(Context context) {

        String jString = null;

        Resources res = context.getResources();
        InputStream in_s = res.openRawResource(R.raw.lessons);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in_s));
        String line;
        StringBuilder text = new StringBuilder();

        try {
            while((line = reader.readLine()) != null) {
                text.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        jString = text.toString();

        return jString;

    }

    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
}
