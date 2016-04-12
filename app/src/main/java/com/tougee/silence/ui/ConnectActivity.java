package com.tougee.silence.ui;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.tougee.silence.R;
import com.tougee.silence.database.SilenceDataBase;
import com.tougee.silence.model.WifiInfo;
import com.tougee.silence.ui.adapter.SimpleRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.tougee.silence.database.SilenceDataBase.TABLE_BIND;
import static com.tougee.silence.provider.SilenceProvider.CONTENT_URI;

public class ConnectActivity extends AppCompatActivity implements View.OnClickListener, SimpleRecyclerAdapter.OnItemClickListener {

    public static final int PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION = 0x01;

    private RecyclerView mWifiListView;
    private View mEmptyView;
    private SimpleRecyclerAdapter mAdapter;
    private WifiManager mWifiManager;
    private List<WifiInfo> mWifiList;
    private List<String> mAddedList;
    private View mContentView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mAddedList = getIntent().getStringArrayListExtra(MainActivity.BIND_LIST);
        mContentView = findViewById(R.id.content);
        mEmptyView = findViewById(R.id.empty_wifi_tips);
        mEmptyView.setOnClickListener(this);
        mWifiListView = (RecyclerView) findViewById(R.id.wifi_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mWifiListView.setLayoutManager(layoutManager);

        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!mWifiManager.isWifiEnabled()) {
            Snackbar.make(mWifiListView, R.string.opening_wifi, Snackbar.LENGTH_SHORT).show();
            boolean success = mWifiManager.setWifiEnabled(true);
            if (!success) {
                Snackbar.make(mWifiListView, R.string.opening_wifi_error, Snackbar.LENGTH_SHORT).show();
                showEmpty();
                return;
            }
        }
        registerReceiver(mWifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method

        } else {
            getWifiList();
        }
    }

    private void getWifiList() {
        mWifiManager.startScan();
        Snackbar.make(mWifiListView, R.string.scanning_wifi, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mWifiReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_CODE_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getWifiList();
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mEmptyView) {
            mWifiManager.startScan();
            Snackbar.make(mEmptyView, R.string.scanning_wifi, Snackbar.LENGTH_SHORT).show();
        }
    }

    private BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> list = mWifiManager.getScanResults();
            if (list != null && list.size() > 0) {
                showList();

                if (mWifiList == null) {
                    mWifiList = new ArrayList<>();
                }
                if (mAdapter == null) {
                    mAdapter = new SimpleRecyclerAdapter(mWifiList, ConnectActivity.this);
                    mWifiListView.setAdapter(mAdapter);
                }

                for (ScanResult result : list) {
                    String name = result.SSID;
                    if (mAddedList != null && mAddedList.contains(name)) {
                        continue;
                    }
                    mWifiList.add(new WifiInfo(name));
                    mAdapter.notifyItemChanged(mWifiList.size() - 1);
                }
            } else {
                showEmpty();
            }
        }
    };

    private void showEmpty() {
        if (mEmptyView.getVisibility() != View.VISIBLE) {
            mEmptyView.setVisibility(View.VISIBLE);
        }
        if (mContentView.getVisibility() != View.GONE) {
            mContentView.setVisibility(View.GONE);
        }
    }

    private void showList() {
        if (mEmptyView.getVisibility() != View.GONE) {
            mEmptyView.setVisibility(View.GONE);
        }
        if (mContentView.getVisibility() != View.VISIBLE) {
            mContentView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(View v) {
        int pos = mWifiListView.getChildAdapterPosition(v);
        String name = mWifiList.get(pos).mName;
        String where = SilenceDataBase.NAME + "=?";
        Cursor c = getContentResolver().query(Uri.withAppendedPath(CONTENT_URI, TABLE_BIND), null, where, new String[]{name}, null);
        if (c != null) {
            if (c.getCount() <= 0) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(SilenceDataBase.NAME, name);
                Uri uri = getContentResolver().insert(Uri.withAppendedPath(CONTENT_URI, TABLE_BIND), contentValues);
                if (uri != null) {
                    mAdapter.removeItem(pos);
                    Snackbar.make(mWifiListView, R.string.add_success, Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(mWifiListView, R.string.add_failed, Snackbar.LENGTH_SHORT).show();
                }
            } else {
                Snackbar.make(mWifiListView, R.string.exits, Snackbar.LENGTH_SHORT).show();
            }
            c.close();
        }else {
            Snackbar.make(mWifiListView, R.string.exits, Snackbar.LENGTH_SHORT).show();
        }
    }
}
