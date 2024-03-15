package com.tuya.appsdk.sample.site.page;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thingclips.smart.android.ble.api.ScanDeviceBean;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.sdk.api.IBleActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.tuya.appsdk.sample.R;

import java.util.HashMap;

public class SetupDeviceActivity extends AppCompatActivity {
    private long siteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_device_activity);
        setTitle(R.string.add_lock);

        siteId = getIntent().getLongExtra("site_id", 0L);
        HashMap<String, ScanDeviceBean> map = new HashMap<>();

        ThingOSLock.getBleLockActivator().startScan(60 * 1000, new IThingResultCallback<ScanDeviceBean>() {
            @Override
            public void onSuccess(ScanDeviceBean result) {
                if (map.get(result.getUuid()) != null) {
                    return;
                }

                map.put(result.getUuid(), result);

                ThingOSLock.getBleLockActivator().startActivator(siteId, result, new IBleActivatorListener() {
                    @Override
                    public void onSuccess(DeviceBean deviceBean) {
                        Toast.makeText(SetupDeviceActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onFailure(int code, String msg, Object handle) {
                        Toast.makeText(SetupDeviceActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(SetupDeviceActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
