package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.OpenRecordDetail;
import com.thingclips.smart.lock.bean.OpenRecordsResp;
import com.thingclips.smart.lock.bean.OperateRecordDetail;
import com.thingclips.smart.lock.bean.OperateRecordsResp;
import com.thingclips.smart.lock.bean.RecordsParams;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.OpenRecordsAdapter;
import com.tuya.appsdk.sample.adapter.OperationRecordsAdapter;

import java.util.ArrayList;
import java.util.List;

public class OpenRecordsListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OpenRecordsAdapter adapter;
    private long siteId;
    private String deviceId;
    private List<OpenRecordDetail> entityList = new ArrayList<>();

    private String sortValues = "";
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_list_activity);
        setTitle(R.string.unlock_records);

        Intent intent = getIntent();
        siteId = intent.getLongExtra("site_id", 0);
        deviceId = intent.getStringExtra("device_id");

        recyclerView = findViewById(R.id.keyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OpenRecordsAdapter(this, entityList);
        recyclerView.setAdapter(adapter);
        findViewById(R.id.loadMore).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadMore();
            }
        });
    }

    private void loadMore() {
        if (loading) {
            return;
        }
        loading = true;
        RecordsParams recordsParams = new RecordsParams();
        recordsParams.deviceId = deviceId;
        recordsParams.groupId = siteId;
        recordsParams.pageSize = 20;
        recordsParams.sortValues = sortValues;
        ThingOSLock.newLockInstance(deviceId).getOpenRecords(recordsParams, new IThingResultCallback<OpenRecordsResp>() {
            @Override
            public void onSuccess(OpenRecordsResp result) {
                loading = false;
                if (TextUtils.isEmpty(sortValues)) {
                    entityList.clear();
                }
                entityList.addAll(result.data);
                sortValues = result.sortValues;
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                loading = false;
                Toast.makeText(OpenRecordsListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        sortValues = "";
        loadMore();
    }
}
