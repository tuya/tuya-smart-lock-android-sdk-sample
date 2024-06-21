package com.tuya.appsdk.sample.site.page.config;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thingclips.smart.android.ble.api.ScanDeviceBean;
import com.thingclips.smart.android.hardware.bean.HgwBean;
import com.thingclips.smart.home.sdk.api.IGwSearchListener;
import com.thingclips.smart.home.sdk.bean.ConfigProductInfoBean;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.sdk.api.IThingDataCallback;
import com.thingclips.smart.sdk.api.IThingSmartActivatorListener;
import com.thingclips.smart.sdk.bean.DeviceBean;
import com.tuya.appsdk.sample.R;

import java.util.HashMap;

public class ZigbeeWiredGatewayConfigActivity extends AppCompatActivity {
    private long siteId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.config_zigbee_wired_gateway_activity);
        setTitle(R.string.add_zigbee_gateway);

        siteId = getIntent().getLongExtra("site_id", 0L);
        HashMap<String, ScanDeviceBean> map = new HashMap<>();

        ThingOSLock.getZigbeeWiredGatewayActivator().startSearch(new IGwSearchListener() {
            @Override
            public void onDevFind(HgwBean gw) {
                ThingOSLock.getZigbeeWiredGatewayActivator().startActivator(ZigbeeWiredGatewayConfigActivity.this, siteId, 60, gw, new IThingSmartActivatorListener() {
                    @Override
                    public void onError(String errorCode, String errorMsg) {
                        Toast.makeText(ZigbeeWiredGatewayConfigActivity.this, R.string.failed + ":" + errorMsg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onActiveSuccess(DeviceBean devResp) {
                        Toast.makeText(ZigbeeWiredGatewayConfigActivity.this, getString(R.string.activator_success) + devResp.getName(), Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onStep(String step, Object data) {

                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ThingOSLock.getZigbeeWiredGatewayActivator().stopActivator();
    }
}
