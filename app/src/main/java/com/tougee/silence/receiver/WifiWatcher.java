package com.tougee.silence.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import static com.tougee.silence.database.SilenceDataBase.TABLE_BIND;
import static com.tougee.silence.provider.SilenceProvider.CONTENT_URI;

public class WifiWatcher extends BroadcastReceiver {

    private AudioManager mAudioManager;
    private WifiManager mWifiManager;
    private List<String> mBindList;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        }
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String name = wifiInfo.getSSID();
        if (name.charAt(0) == '\"' && name.charAt(name.length() - 1) == '\"') {
            name = name.substring(1, name.length() - 1);
        }
        final String realName = name;
        Runnable mQueryBindListRunnable = new Runnable() {
            @Override
            public void run() {
                Cursor c = context.getContentResolver().query(Uri.withAppendedPath(CONTENT_URI, TABLE_BIND), null, null, null, null);
                if (c != null) {
                    if (c.getCount() > 0) {
                        if (mBindList == null) {
                            mBindList = new ArrayList<>();
                        }
                        while (c.moveToNext()) {
                            com.tougee.silence.model.WifiInfo bindInfo = new com.tougee.silence.model.WifiInfo(c);
                            String itemName = bindInfo.mName;
                            if (!mBindList.contains(itemName)) {
                                mBindList.add(bindInfo.mName);
                            }
                        }

                        if (mBindList != null && mBindList.contains(realName)) {
                            if (mAudioManager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                                Toast.makeText(context, "Change to mode silent, " + realName + " connected ", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            if (mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_SILENT) {
                                mAudioManager.setRingerMode(AudioManager.MODE_NORMAL);
                                mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                                Toast.makeText(context, "Change to mode normal ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    c.close();
                }
            }
        };
        mQueryBindListRunnable.run();
    }
}
