package com.tougee.silence.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tougee.silence.R;
import com.tougee.silence.database.SilenceDataBase;
import com.tougee.silence.model.WifiInfo;
import com.tougee.silence.receiver.WifiWatcher;
import com.tougee.silence.ui.adapter.SimpleRecyclerAdapter;

import java.util.ArrayList;
import java.util.List;

import static com.tougee.silence.database.SilenceDataBase.TABLE_BIND;
import static com.tougee.silence.provider.SilenceProvider.CONTENT_URI;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SimpleRecyclerAdapter.OnItemClickListener {

    public static final String BIND_LIST = "bind_list";

    private SimpleRecyclerAdapter mAdapter;
    private RecyclerView mBindRecyclerView;
    private View mEmptyView;
    private View mDataView;
    private ArrayList<String> mNameList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showConnectActivity();
            }
        });

        mDataView = findViewById(R.id.data_view);
        mEmptyView = findViewById(R.id.empty_tips);
        mEmptyView.setOnClickListener(this);
        mBindRecyclerView = (RecyclerView) findViewById(R.id.bind_list);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mBindRecyclerView.setLayoutManager(layoutManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBindRecyclerView.post(mQueryBindListRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Runnable mQueryBindListRunnable = new Runnable() {
        @Override
        public void run() {
            Cursor c = getContentResolver().query(Uri.withAppendedPath(CONTENT_URI, TABLE_BIND), null, null, null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    List<WifiInfo> list = new ArrayList<>();
                    mNameList = new ArrayList<>();
                    while (c.moveToNext()) {
                        WifiInfo bindInfo = new WifiInfo(c);
                        list.add(bindInfo);
                        mNameList.add(bindInfo.mName);
                    }
                    if (mAdapter == null) {
                        mAdapter = new SimpleRecyclerAdapter(list, MainActivity.this);
                        mBindRecyclerView.setAdapter(mAdapter);
                    } else {
                        mAdapter.setBindList(list);
                        mAdapter.notifyDataSetChanged();
                    }
                    showList();
                } else {
                    showEmpty();
                }
                c.close();

            } else {
                showEmpty();
            }

            mBindRecyclerView.removeCallbacks(mQueryBindListRunnable);
        }
    };

    @Override
    public void onClick(View v) {
        if (v == mEmptyView) {
            showConnectActivity();
        }
    }

    private void showConnectActivity() {
        Intent intent = new Intent(this, ConnectActivity.class);
        if (mNameList != null && mNameList.size() > 0) {
            intent.putStringArrayListExtra(BIND_LIST, mNameList);
        }
        startActivity(intent);
    }

    @Override
    public void onItemClick(View v) {
        int pos = mBindRecyclerView.getChildAdapterPosition(v);
        showForgetDialog(pos);
    }

    private void showEmpty() {
        mEmptyView.setVisibility(View.VISIBLE);
        mDataView.setVisibility(View.GONE);
    }

    private void showList() {
        mEmptyView.setVisibility(View.GONE);
        mDataView.setVisibility(View.VISIBLE);
    }

    private void showForgetDialog(final int pos) {
        final String currName = mAdapter.getWifiList().get(pos).mName;
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle(getString(R.string.forget_wifi));
        builder.setMessage(currName);
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                String where = SilenceDataBase.NAME + "=?";
                int count = getContentResolver().delete(Uri.withAppendedPath(CONTENT_URI, TABLE_BIND), where, new String[]{currName});
                if (count > 0) {
                    mAdapter.removeItem(pos);
                    mNameList.remove(pos);
                    Snackbar.make(mBindRecyclerView, R.string.delete_success, Snackbar.LENGTH_SHORT).show();
                    if (mAdapter.getItemCount() == 0) {
                        showEmpty();
                    }
                } else {
                    Snackbar.make(mBindRecyclerView, R.string.delete_failed, Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        builder.show();
    }
}
