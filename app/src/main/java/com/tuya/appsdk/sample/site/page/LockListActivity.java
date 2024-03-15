package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.thingclips.sdk.os.ThingOSBLE;
import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.android.ble.builder.BleConnectBuilder;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.DeviceBeanWrapper;
import com.thingclips.smart.lock.bean.SiteDetail;
import com.thingclips.smart.sdk.api.IDevListener;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.LockListAdapter;

import java.util.ArrayList;
import java.util.List;

public class LockListActivity extends AppCompatActivity {

    private List<DeviceBeanWrapper> lockList = new ArrayList<>();
    private LockListAdapter adapter;
    private long siteId;
    private long startId;
    private boolean loading = false;

    private IDevListener devListener = new IDevListener() {
        @Override
        public void onDpUpdate(String devId, String dpStr) {

        }

        @Override
        public void onRemoved(String devId) {

        }

        @Override
        public void onStatusChanged(String devId, boolean online) {
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onNetworkStatusChanged(String devId, boolean status) {

        }

        @Override
        public void onDevInfoUpdate(String devId) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lock_list_activity);

        setTitle(R.string.lock_list);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        adapter = new LockListAdapter(lockList, new LockListAdapter.LockClickListener() {
            @Override
            public void onItemClick(DeviceBeanWrapper lock) {
                Intent intent = new Intent(LockListActivity.this, DoorLockDetailActivity.class);
                intent.putExtra("site_id", siteId);
                intent.putExtra("device_id", lock.deviceRespBean.getDevId());
                startActivity(intent);
            }

            @Override
            public void onUnlockClick(DeviceBeanWrapper lock) {
                ThingOSLock.newLockInstance(lock.deviceRespBean.getDevId()).unlock(siteId, new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(LockListActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(LockListActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRemoveClick(DeviceBeanWrapper lock) {
                ThingOSLock.newSiteInstance(String.valueOf(siteId)).deleteLock(lock.deviceRespBean.getDevId(), new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        lockList.remove(lock);
                        adapter.notifyDataSetChanged();
                        Toast.makeText(LockListActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(LockListActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onConnectClick(DeviceBeanWrapper lock) {
                //connect to this lock
                connectLock(lock);
            }
        });
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Button addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LockListActivity.this, SetupDeviceActivity.class);
                intent.putExtra("site_id", siteId);
                startActivity(intent);
            }
        });

        findViewById(R.id.loadMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMore();
            }
        });
    }

    public void connectLock(DeviceBeanWrapper lockDevice) {
        List<BleConnectBuilder> builderList = new ArrayList<>();
        BleConnectBuilder bleConnectBuilder1 = new BleConnectBuilder();
        bleConnectBuilder1.setDevId(lockDevice.deviceRespBean.getDevId());
        builderList.add(bleConnectBuilder1);
        ThingOSBLE.manager().connectBleDevice(builderList); // 开始连接

        ThingOSDevice.newDeviceInstance(lockDevice.deviceRespBean.getDevId()).registerDevListener(devListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startId = 0;
        loadMore();
    }

    private void loadMore() {
        if (loading) {
            return;
        }
        loading = true;
        ThingOSLock.newSiteInstance(String.valueOf(siteId)).getLockDeviceList(startId, new IThingResultCallback<ArrayList<DeviceBeanWrapper>>() {
            @Override
            public void onSuccess(ArrayList<DeviceBeanWrapper> result) {
                loading = false;
                if (result == null || result.size() <= 0) {
                    return;
                }

                if (startId == 0) {
                    lockList.clear();
                }
                lockList.addAll(result);
                adapter.notifyDataSetChanged();
                startId = result.get(result.size() - 1).gatewayIndexId;
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                loading = false;
                Toast.makeText(LockListActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        devListener = null;
    }
}
