package com.tuya.appsdk.sample.site.page.config;// AddDeviceActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.DeviceAdapter;
import com.tuya.appsdk.sample.resource.SiteModel;

import java.util.ArrayList;
import java.util.List;

public class GwConfigSubDeviceActivity extends AppCompatActivity {

    private RecyclerView deviceRecyclerView;
    private Button startButton;
    private DeviceAdapter deviceAdapter;
    private List<String> deviceList = new ArrayList<>();

    private boolean adding = false;
    private String gwId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_gw_add_sub_device);

        Intent intent = getIntent();
        gwId = intent.getStringExtra("gw_id");

        deviceRecyclerView = findViewById(R.id.deviceRecyclerView);
        startButton = findViewById(R.id.startButton);

        // 初始化RecyclerView和Adapter
        deviceAdapter = new DeviceAdapter(deviceList);
        deviceRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceRecyclerView.setAdapter(deviceAdapter);

        // 点击开始配网按钮
        startButton.setOnClickListener(v -> {
            if (adding) {
                ThingOSLock.getGwSubDeviceActivator().stopActivator();
                adding = false;
                startButton.setText(R.string.start_config_device);
            } else {
                adding = true;
                startButton.setText(R.string.searching_sub_device);
                // 处理开始配网逻辑
                ThingOSLock.getGwSubDeviceActivator().startActivator(SiteModel.getCurrentSite(this), gwId, 60, new IThingSmartActivatorListener() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Toast.makeText(GwConfigSubDeviceActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onActiveSuccess(DeviceBean devResp) {
                        deviceList.add(devResp.getName());
                        deviceAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onStep(String step, Object data) {

                    }
                });
            }
        });
    }
}
