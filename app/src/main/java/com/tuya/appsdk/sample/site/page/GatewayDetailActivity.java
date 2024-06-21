package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.sdk.os.ThingOSDevice;
import com.thingclips.smart.android.common.utils.L;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.api.ILockGateway;
import com.thingclips.smart.lock.bean.SubDeviceBean;
import com.thingclips.smart.lock.bean.SubDeviceListResp;
import com.thingclips.smart.sdk.api.ISubDevListener;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.BindedDeviceAdapter;

import java.util.ArrayList;
import java.util.List;

public class GatewayDetailActivity extends AppCompatActivity {

    private static final String TAG = "GatewayDetailActivity";
    private long siteId;
    private String deviceId;
    private RecyclerView bindedrecyclerView;
    private RecyclerView unbindrecyclerView;
    private ILockGateway gatewayInstance;
    private List<SubDeviceBean> bindedDeviceBeanList = new ArrayList<>();
    private List<SubDeviceBean> unbindDeviceBeanList = new ArrayList<>();
    private BindedDeviceAdapter bindedAdapter = new BindedDeviceAdapter(bindedDeviceBeanList, 1, new BindedDeviceAdapter.BindDeviceClickListener() {
        @Override
        public void unbindClick(SubDeviceBean item) {
            ArrayList<String> devIds = new ArrayList<>();
            devIds.add(item.deviceId);
            progressBar.setVisibility(View.VISIBLE);
            gatewayInstance.unbindSubDevices(devIds, new IThingResultCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    Toast.makeText(GatewayDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void bindClick(SubDeviceBean item) {
            //do nothing
        }
    });

    private BindedDeviceAdapter unbindAdapter = new BindedDeviceAdapter(unbindDeviceBeanList, 0, new BindedDeviceAdapter.BindDeviceClickListener() {
        @Override
        public void unbindClick(SubDeviceBean item) {
            //do nothing
        }

        @Override
        public void bindClick(SubDeviceBean item) {
            ArrayList<String> devIds = new ArrayList<>();
            devIds.add(item.deviceId);
            progressBar.setVisibility(View.VISIBLE);
            gatewayInstance.bindSubDevices(devIds, new IThingResultCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean result) {
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    Toast.makeText(GatewayDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        }
    });
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gateway_detail_activity);

        setTitle(R.string.lock_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }

        TextView deviceIdView = findViewById(R.id.tvDeviceIdValue);
        TextView copyIdView = findViewById(R.id.copy_id);
        TextView deviceNameView = findViewById(R.id.textDeviceNameValue);
        progressBar = findViewById(R.id.progressBar);
        deviceIdView.setText(deviceId);
        deviceNameView.setText(ThingOSDevice.getDeviceBean(deviceId).getName());

        copyIdView.setOnClickListener(view -> {
            android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            android.content.ClipData clipData = android.content.ClipData.newPlainText("DeviceId", deviceId);
            clipboardManager.setPrimaryClip(clipData);
            Toast.makeText(GatewayDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
        });

        View textDesc2 = findViewById(R.id.textDesc2);

        bindedrecyclerView = findViewById(R.id.recyclerView);
        bindedrecyclerView.setLayoutManager(new LinearLayoutManager(this));
        bindedrecyclerView.setAdapter(bindedAdapter);

        unbindrecyclerView = findViewById(R.id.recyclerView2);
        unbindrecyclerView.setLayoutManager(new LinearLayoutManager(this));
        unbindrecyclerView.setAdapter(unbindAdapter);

        gatewayInstance = ThingOSLock.newGatewayInstance(siteId, deviceId);
        gatewayInstance.registerSubDevListener(new ISubDevListener() {
            @Override
            public void onSubDevDpUpdate(String nodeId, String dps) {

            }

            @Override
            public void onSubDevRemoved(String devId) {
                L.i(TAG, "onSubDevRemoved:" + devId);
            }

            @Override
            public void onSubDevAdded(String devId) {
                L.i(TAG, "onSubDevAdded:" + devId);
            }

            @Override
            public void onSubDevInfoUpdate(String devId) {
                L.i(TAG, "onSubDevInfoUpdate:" + devId);
                progressBar.setVisibility(View.GONE);
                refresh();
            }

            @Override
            public void onSubDevStatusChanged(List<String> onlineDeviceIds, List<String> offlineDeviceIds) {

            }
        });

        boolean sigMeshWifi = ThingOSDevice.getDeviceBean(deviceId).isSigMeshWifi();
        bindedAdapter.setBleWifi(sigMeshWifi);
        unbindAdapter.setBleWifi(sigMeshWifi);

        if (!sigMeshWifi) {
            unbindrecyclerView.setVisibility(View.GONE);
            textDesc2.setVisibility(View.GONE);
        }

        refresh();
    }

    private void refresh() {
        gatewayInstance.getSubDeviceList(20, 0, new IThingResultCallback<SubDeviceListResp>() {
            @Override
            public void onSuccess(SubDeviceListResp result) {
                bindedDeviceBeanList.clear();
                bindedDeviceBeanList.addAll(result.data);
                bindedAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(GatewayDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        ThingOSLock.newSiteInstance(siteId).getUnbinedDeviceList(0, new IThingResultCallback<SubDeviceListResp>() {
            @Override
            public void onSuccess(SubDeviceListResp result) {
                unbindDeviceBeanList.clear();
                unbindDeviceBeanList.addAll(result.data);
                unbindAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(GatewayDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gatewayInstance.unregisterSubDevListener();
    }
}
