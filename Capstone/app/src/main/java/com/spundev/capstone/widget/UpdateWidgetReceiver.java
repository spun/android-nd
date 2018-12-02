package com.spundev.capstone.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.spundev.capstone.ui.TTSActivity;

public class UpdateWidgetReceiver extends BroadcastReceiver {

    public static final String ACTION_CLICK_FAVORITE = "com.spundev.capstone.action.favorite_click";
    public static final String EXTRA_FAVORITE_ID = "com.spundev.capstone.extra.favorite_id";
    public static final String EXTRA_FAVORITE_TEXT = "com.spundev.capstone.extra.favorite_text";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Called when a widget checkbox changes
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_CLICK_FAVORITE.equals(action)) {
                long favoriteId = intent.getLongExtra(EXTRA_FAVORITE_ID, -1);
                String favoriteText = intent.getStringExtra(EXTRA_FAVORITE_TEXT);

                Intent ttsIntent = new Intent(context, TTSActivity.class);
                ttsIntent.putExtra(TTSActivity.TTS_TEXT_EXTRA, favoriteText);
                ttsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(ttsIntent);
            }
        }
    }

}
