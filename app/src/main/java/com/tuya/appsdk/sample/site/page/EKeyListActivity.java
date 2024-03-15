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
import com.thingclips.smart.lock.bean.EKeyBean;
import com.thingclips.smart.lock.bean.EKeyListParams;
import com.thingclips.smart.lock.bean.EKeyListResp;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.EKeyAdapter;

import java.util.ArrayList;
import java.util.List;

public class EKeyListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private EKeyAdapter adapter;
    private long siteId;
    private String deviceId;
    private List<EKeyBean> eKeyBeanList = new ArrayList<>();
    private int currentPageNo = 1;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ekey_list_activity);
        setTitle(R.string.ekey_list);

        Intent intent = getIntent();
        siteId = intent.getLongExtra("site_id", 0);
        deviceId = intent.getStringExtra("device_id");

        recyclerView = findViewById(R.id.keyList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EKeyAdapter(this, eKeyBeanList, new EKeyAdapter.EkeyClickListener() {
            @Override
            public void onItemClick(EKeyBean eKeyBean) {
                Intent intent = new Intent(EKeyListActivity.this, EKeyDetailActivity.class);
                intent.putExtra("site_id", siteId);
                intent.putExtra("device_id", deviceId);
                intent.putExtra("ekey_id", eKeyBean.ekeyId);
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

        initCreateKeyButton();
    }

    private void loadMore() {
        if (loading) {
            return;
        }
        loading = true;
        EKeyListParams params = new EKeyListParams();
        params.groupId = siteId;
        params.pageNo = currentPageNo;
        params.pageSize = 20;
        params.deviceId = deviceId;
        ThingOSLock.newLockInstance(deviceId).getEKeyList(params, new IThingResultCallback<EKeyListResp>() {
            @Override
            public void onSuccess(EKeyListResp result) {
                if (currentPageNo == 1) {
                    eKeyBeanList.clear();
                }
                eKeyBeanList.addAll(result.data);
                adapter.notifyDataSetChanged();
                loading = false;
                currentPageNo++;
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                loading = false;
                Toast.makeText(EKeyListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initCreateKeyButton() {
        Button createKeyButton = findViewById(R.id.createKeyButton);
        createKeyButton.setOnClickListener(v -> {
            Intent intent = new Intent(EKeyListActivity.this, CreateKeyActivity.class);
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
