package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.thingclips.sdk.os.ThingOSUser;
import com.thingclips.smart.android.user.api.ILogoutCallback;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.SiteDetail;
import com.tuya.appsdk.sample.R;
import com.tuya.appsdk.sample.adapter.SiteAdapter;
import com.tuya.appsdk.sample.resource.SiteModel;
import com.tuya.appsdk.sample.user.main.UserFuncActivity;

import java.util.ArrayList;
import java.util.List;

public class SiteListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SiteAdapter siteAdapter;
    private List<SiteDetail> siteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.site_list_activity);

        setTitle(R.string.site_list);

        recyclerView = findViewById(R.id.recyclerView);
        Button btnCreateSite = findViewById(R.id.btnCreateSite);

        siteList = new ArrayList<>();
        siteAdapter = new SiteAdapter(siteList, new SiteAdapter.SiteClickListener() {
            @Override
            public void onSiteClick(SiteDetail siteDetail) {
                SiteModel.INSTANCE.setCurrentSite(SiteListActivity.this, siteDetail.gid);
                Intent intent = new Intent(SiteListActivity.this, SiteDetailActivity.class);
                intent.putExtra("site_info", JSON.toJSONString(siteDetail));
                startActivity(intent);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(siteAdapter);

        btnCreateSite.setOnClickListener(v -> {
            startActivity(new Intent(SiteListActivity.this, CreateSiteActivity.class));
        });

        findViewById(R.id.logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThingOSUser.getUserInstance().logout(new ILogoutCallback() {
                    @Override
                    public void onSuccess() {
                        SiteModel.INSTANCE.clear(SiteListActivity.this);

                        Intent intent = new Intent(SiteListActivity.this, UserFuncActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(String code, String error) {

                    }
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //query site list
        ThingOSLock.getSiteManager().getSiteList(new IThingResultCallback<ArrayList<SiteDetail>>() {
            @Override
            public void onSuccess(ArrayList<SiteDetail> result) {
                siteList.clear();
                siteList.addAll(result);
                siteAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(SiteListActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
