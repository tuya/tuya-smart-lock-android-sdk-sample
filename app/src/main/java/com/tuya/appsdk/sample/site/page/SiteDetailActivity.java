package com.tuya.appsdk.sample.site.page;// SiteDetailActivity.java

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.SiteDetail;
import com.tuya.appsdk.sample.R;

public class SiteDetailActivity extends AppCompatActivity {

    private TextView textSiteName, textLocation;
    private Button btnLockList, btnGatewayList, btnDeleteSite;
    private SiteDetail siteDetail;
    private boolean isSiteNameChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.site_info_activity);
        setTitle(R.string.site_detail);

        textSiteName = findViewById(R.id.textSiteName);
        textLocation = findViewById(R.id.textLocation);
        btnLockList = findViewById(R.id.btnLockList);
        btnGatewayList = findViewById(R.id.btnGatewayList);
        btnDeleteSite = findViewById(R.id.btnDeleteSite);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_info")) {
            String json = intent.getStringExtra("site_info");
            siteDetail = JSON.parseObject(json, SiteDetail.class);
            if (siteDetail != null) {
                textSiteName.setText(siteDetail.name);
                textLocation.setText(siteDetail.geoName);
            }
        }

        btnLockList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lockListIntent = new Intent(SiteDetailActivity.this, DeviceListActivity.class);
                lockListIntent.putExtra("site_id", siteDetail.gid);
                lockListIntent.putExtra("category", "lock");
                startActivity(lockListIntent);
            }
        });
        btnGatewayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent lockListIntent = new Intent(SiteDetailActivity.this, DeviceListActivity.class);
                lockListIntent.putExtra("site_id", siteDetail.gid);
                lockListIntent.putExtra("category", "gateway");
                startActivity(lockListIntent);
            }
        });

        btnDeleteSite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThingOSLock.getSiteManager().removeSite(siteDetail.gid, new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(SiteDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(SiteDetailActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
