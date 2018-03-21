package com.edtech.activities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.ImageView;

import com.edtech.Constants;
import com.edtech.R;

import java.io.File;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by gautamkarnik on 16-05-18.
 */
public class MainActivityHelper {

    // functions for file manipulations
    public static void deleteFileIfExists(String filename) {
        try {
            File file = new File(filename);
            if (file.exists()) file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean checkInternetConnection(Context context) {
        ConnectivityManager con_manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if ((con_manager.getActiveNetworkInfo() != null)
                && (con_manager.getActiveNetworkInfo().isAvailable())
                && (con_manager.getActiveNetworkInfo().isConnected())) {
            return true;
        } else {
            return false;
        }

    }


    // module to with support functions for Main Activity
    public static void speak(TextToSpeech tts, String text) {
        if (tts != null) {
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public static void synthesizeToFile(TextToSpeech tts, String text) {
        if (tts != null) {
            String filename = Constants.TTS_AUDIO_FILE_PATH;

            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP) {
                tts.synthesizeToFile(text, null, new File(filename), TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID);
            } else {
                HashMap<String, String> myHashRender = new HashMap<String, String>();
                myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, text);
                tts.synthesizeToFile(text, myHashRender, filename);
            }
        }
    }
    public static void setAvatar(ImageView stickerView) {
        Random rn = new Random();
        int image = rn.nextInt(4);

        switch (image) {
            case Constants.AVATAR_LION:
                stickerView.setBackgroundResource(R.drawable.animation_list_lion);
                break;
            case Constants.AVATAR_ELEPHANT:
                stickerView.setBackgroundResource(R.drawable.animation_list_elephant);
                break;
            case Constants.AVATAR_HORSE:
                stickerView.setBackgroundResource(R.drawable.animation_list_horse);
                break;
            case Constants.AVATAR_GOAT:
                stickerView.setBackgroundResource(R.drawable.animation_list_goat);
                break;
            default:
                stickerView.setBackgroundResource(R.drawable.animation_list_lion);
                break;
        }
    }

    public static void setImage(ImageView stickerView) {
        Random rn = new Random();
        int image = rn.nextInt(3);

        switch (image) {
            case Constants.IMAGE_GREEN_FIREWORK:
                stickerView.setBackgroundResource(R.drawable.animation_list_green_firework);
                break;
            case Constants.IMAGE_RED_FIREWORK:
                stickerView.setBackgroundResource(R.drawable.animation_list_red_firework);
                break;
            case Constants.IMAGE_BLUE_FIREWORK:
                stickerView.setBackgroundResource(R.drawable.animation_list_blue_firework);
                break;
            default:
                stickerView.setBackgroundResource(R.drawable.animation_list_green_firework);
                break;
        }
    }
}
