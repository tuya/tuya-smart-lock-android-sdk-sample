package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.FingerprintBean;
import com.thingclips.smart.lock.bean.FingerprintListResp;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.FingerprintAdapter;

import java.util.ArrayList;
import java.util.List;

public class FingerprintListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FingerprintAdapter adapter;
    private long siteId;
    private String deviceId;
    private List<FingerprintBean> fingerprintList = new ArrayList<>();
    private int currentPageNo = 1;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fingerprint_list_activity);
        setTitle(R.string.fingerprint_list);

        Intent intent = getIntent();
        siteId = intent.getLongExtra("site_id", 0);
        deviceId = intent.getStringExtra("device_id");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FingerprintAdapter(this, fingerprintList, new FingerprintAdapter.ItemClickListener() {
            @Override
            public void onItemClick(FingerprintBean bean) {
                Intent intent = new Intent(FingerprintListActivity.this, FingerprintDetailActivity.class);
                intent.putExtra("site_id", siteId);
                intent.putExtra("device_id", deviceId);
                intent.putExtra("fingerprint_id", bean.fingerprintId);
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        findViewById(R.id.loadMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMore();
            }
        });

        findViewById(R.id.clearButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThingOSLock.getFingerprintManager().clearAllFingerprint(siteId, deviceId, new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        onResume();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(FingerprintListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        initCreateButton();
    }

    private void loadMore() {
        if (loading) {
            return;
        }
        loading = true;

        ThingOSLock.getFingerprintManager().getFingerprintList(siteId, deviceId, currentPageNo, new IThingResultCallback<FingerprintListResp>() {
            @Override
            public void onSuccess(FingerprintListResp result) {
                if (currentPageNo == 1) {
                    fingerprintList.clear();
                }
                fingerprintList.addAll(result.data);
                adapter.notifyDataSetChanged();
                loading = false;
                currentPageNo++;
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                loading = false;
                Toast.makeText(FingerprintListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCreateButton() {
        Button button = findViewById(R.id.createButton);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(FingerprintListActivity.this, CreateFingerprintActivity.class);
            intent.putExtra("site_id", siteId);
            intent.putExtra("device_id", deviceId);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentPageNo = 1;
        loadMore();
    }
}
