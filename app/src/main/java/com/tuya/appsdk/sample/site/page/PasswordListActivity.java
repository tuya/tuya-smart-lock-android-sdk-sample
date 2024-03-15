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
import com.thingclips.smart.lock.bean.PasswordBean;
import com.thingclips.smart.lock.bean.PasswordListParams;
import com.thingclips.smart.lock.bean.PasswordListResp;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.PasswordListAdapter;

import java.util.ArrayList;
import java.util.List;

public class PasswordListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PasswordListAdapter adapter;
    private long siteId;
    private String deviceId;
    private List<PasswordBean> passwordList = new ArrayList<>();
    private int pageNo = 1;
    private boolean loading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_list_activity);
        setTitle(R.string.password_list);

        Intent intent = getIntent();
        siteId = intent.getLongExtra("site_id", 0);
        deviceId = intent.getStringExtra("device_id");

        recyclerView = findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PasswordListAdapter(this, passwordList, new PasswordListAdapter.EkeyClickListener() {
            @Override
            public void onItemClick(PasswordBean password) {
                Intent intent = new Intent(PasswordListActivity.this, PasswordDetailActivity.class);
                intent.putExtra("site_id", siteId);
                intent.putExtra("device_id", deviceId);
                intent.putExtra("password_id", password.passwordId);
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

        initCreateButton();
    }

    private void initCreateButton() {
        Button createButton = findViewById(R.id.createKeyButton);
        createButton.setOnClickListener(v -> {
            Intent intent = new Intent(PasswordListActivity.this, CreatePasswordActivity.class);
            intent.putExtra("site_id", siteId);
            intent.putExtra("device_id", deviceId);
            startActivity(intent);
        });
    }

    private void loadMore() {
        if (loading) return;
        loading = true;
        PasswordListParams params = new PasswordListParams();
        params.groupId = siteId;
        params.pageNo = pageNo;
        params.pageSize = 20;
        params.deviceId = deviceId;
        ThingOSLock.newLockInstance(deviceId).getPasswordList(params, new IThingResultCallback<PasswordListResp>() {
            @Override
            public void onSuccess(PasswordListResp result) {
                loading = false;
                if (pageNo == 1) {
                    passwordList.clear();
                }
                passwordList.addAll(result.data);
                adapter.notifyDataSetChanged();
                pageNo++;
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                loading = false;
                Toast.makeText(PasswordListActivity.this, "Error: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        pageNo = 1;
        loadMore();
    }
}
