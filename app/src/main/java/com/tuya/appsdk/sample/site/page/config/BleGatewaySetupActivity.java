package com.tuya.appsdk.sample.site.page.config;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thingclips.smart.android.ble.api.ScanDeviceBean;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.sdk.api.IBleActivatorListener;
import com.thingclips.smart.sdk.api.IThingActivator;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.tuya.appsdk.sample.R;

public class BleGatewaySetupActivity extends AppCompatActivity {

    private static final String TAG = "GatewayEzSetupActivity";
    private EditText etWifiName, etWifiPassword;
    private Button btnStartSetup;
    private ProgressBar progressBar;
    private long site_id;
    private IThingActivator mTuyaActivator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ez_config_activity);
        setTitle(R.string.ez_config_title);

        // 获取从前一个Activity传递过来的 site_id 参数
        Intent intent = getIntent();
        site_id = intent.getLongExtra("site_id", 0);

        etWifiName = findViewById(R.id.et_wifi_name);
        etWifiPassword = findViewById(R.id.et_wifi_password);
        btnStartSetup = findViewById(R.id.btn_start_setup);
        progressBar = findViewById(R.id.progress_loading);

        btnStartSetup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetup();
            }
        });
    }

    private void startSetup() {
        String wifiName = etWifiName.getText().toString().trim();
        String wifiPassword = etWifiPassword.getText().toString().trim();

        if (wifiName.isEmpty() || wifiPassword.isEmpty()) {
            Toast.makeText(this, "请填写WIFI名称和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        realActive(wifiName, wifiPassword);
        // 显示加载状态
        progressBar.setVisibility(View.VISIBLE);
    }

    private void realActive(String wifiName, String wifiPassword) {
        ThingOSLock.getBleGatewayActivator().startScan(60 * 1000, new IThingResultCallback<ScanDeviceBean>() {
            @Override
            public void onSuccess(ScanDeviceBean result) {
                ThingOSLock.getBleGatewayActivator().startActivator(site_id, result, wifiName, wifiPassword, new IBleActivatorListener() {
                    @Override
                    public void onSuccess(DeviceBean deviceBean) {
                        Toast.makeText(BleGatewaySetupActivity.this, "Config Success:" + deviceBean.getName(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        finish();
                    }

                    @Override
                    public void onFailure(int code, String msg, Object handle) {
                        progressBar.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(BleGatewaySetupActivity.this, "Config Failed:" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTuyaActivator != null) {
            //停止配网
            mTuyaActivator.stop();
            //退出页面销毁一些缓存和监听
            mTuyaActivator.onDestroy();
        }
    }
}
