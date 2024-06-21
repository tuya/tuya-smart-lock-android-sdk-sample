package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.EKeyResp;
import com.thingclips.smart.lock.bean.LockLivecycle;
import com.thingclips.smart.lock.bean.OnceEKeyCreateParams;
import com.thingclips.smart.lock.bean.PeriodicityEKeyCreateParams;
import com.thingclips.smart.lock.bean.PermanentEKeyCreateParams;
import com.tuya.appsdk.sample.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Objects;

public class CreateKeyActivity extends AppCompatActivity {

    private long siteId;
    private String deviceId;
    private View effectiveDateLayout;
    private EditText effectiveEditText;
    private View invalidDateLayout;
    private EditText invalidEditText;
    private LinearLayout loopModeLayout;
    private View loopModeText;
    private View startTimeLayout;
    private EditText startTimeEditText;
    private View endTimeLayout;
    private EditText endTimeEditText;
    private String liveCycle = LockLivecycle.PERIODICITY;
    private EditText accountEditText;
    private EditText userNameEditText;

    private HashSet<Integer> grayButtonIndex = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_ekey_activity);

        setTitle(R.string.create_ekey);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }

        accountEditText = findViewById(R.id.et_account);
        userNameEditText = findViewById(R.id.et_user_name);

        effectiveDateLayout = findViewById(R.id.startDateLayout);
        effectiveEditText = effectiveDateLayout.findViewById(R.id.etEffectiveTime);

        invalidDateLayout = findViewById(R.id.endDateLayout);
        invalidEditText = invalidDateLayout.findViewById(R.id.etInvalidTime);

        loopModeLayout = findViewById(R.id.repeatModeLayout);
        loopModeText = findViewById(R.id.tv_loop_mode);

        startTimeLayout = findViewById(R.id.ll_start_date);
        startTimeEditText = startTimeLayout.findViewById(R.id.et_start_date);

        endTimeLayout = findViewById(R.id.ll_end_date);
        endTimeEditText = endTimeLayout.findViewById(R.id.et_end_date);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(navListener);

        View saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEKey();
            }
        });

        int childCount = loopModeLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            TextView child = (TextView) loopModeLayout.getChildAt(i);
            int finalI = i;
            child.setOnClickListener(v -> {
                boolean contains = grayButtonIndex.contains(finalI);
                if (contains) {
                    //current state is gray, change to blue
                    grayButtonIndex.remove(finalI);
                    child.setBackgroundResource(R.drawable.bg_blue_button);
                } else {
                    //current state is blue, change to gray
                    grayButtonIndex.add(finalI);
                    child.setBackgroundResource(R.drawable.bg_gray_button);
                }
            });
        }
    }

    private void createEKey() {
        IThingResultCallback<EKeyResp> callback = new IThingResultCallback<EKeyResp>() {
            @Override
            public void onSuccess(EKeyResp result) {
                Toast.makeText(CreateKeyActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(CreateKeyActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        };

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (Objects.equals(liveCycle, LockLivecycle.PERMANENT)) {
            PermanentEKeyCreateParams params = new PermanentEKeyCreateParams();
            params.account = accountEditText.getText().toString().trim();
            params.countryCode = "86";
            params.ekeyName = userNameEditText.getText().toString().trim();
            params.deviceId = deviceId;
            params.siteId = siteId;
            ThingOSLock.getEkeyManager().createPermanentEKey(params, callback);

        } else if (Objects.equals(liveCycle, LockLivecycle.ONCE)) {
            OnceEKeyCreateParams params = new OnceEKeyCreateParams();
            params.account = accountEditText.getText().toString().trim();
            params.countryCode = "86";
            params.ekeyName = userNameEditText.getText().toString().trim();
            params.deviceId = deviceId;
            params.siteId = siteId;
            try {
                params.effectiveTime = dateFormat.parse(effectiveEditText.getText().toString()).getTime();
                params.invalidTime = dateFormat.parse(invalidEditText.getText().toString()).getTime();
            } catch (ParseException e) {
                Toast.makeText(this, R.string.effective_time_or_invalid_time_error, Toast.LENGTH_SHORT).show();
                return;
            }
            ThingOSLock.getEkeyManager().createOnceEKey(params, callback);

        } else if (Objects.equals(liveCycle, LockLivecycle.PERIODICITY)) {
            PeriodicityEKeyCreateParams params = new PeriodicityEKeyCreateParams();
            params.account = accountEditText.getText().toString().trim();
            params.countryCode = "86";
            params.ekeyName = userNameEditText.getText().toString().trim();
            params.deviceId = deviceId;
            params.siteId = siteId;
            try {
                params.effectiveTime = dateFormat.parse(effectiveEditText.getText().toString()).getTime();
                params.invalidTime = dateFormat.parse(invalidEditText.getText().toString()).getTime();
            } catch (ParseException e) {
                Toast.makeText(this, R.string.effective_time_or_invalid_time_error, Toast.LENGTH_SHORT).show();
                return;
            }
            params.startMinuteFormat = startTimeEditText.getText().toString().trim();
            params.endMinuteFormat = endTimeEditText.getText().toString().trim();
            params.workingDay = getWorkingDay();
            ThingOSLock.getEkeyManager().createPeriodicityEKey(params, callback);
        }
    }

    private String getWorkingDay() {
        StringBuilder workingDay = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            workingDay.append(grayButtonIndex.contains(i) ? "0" : "1");
        }
        return workingDay.toString();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.nav_temporary:
                        liveCycle = LockLivecycle.PERIODICITY;
                        effectiveDateLayout.setVisibility(View.VISIBLE);
                        invalidDateLayout.setVisibility(View.VISIBLE);
                        loopModeLayout.setVisibility(View.VISIBLE);
                        startTimeLayout.setVisibility(View.VISIBLE);
                        endTimeLayout.setVisibility(View.VISIBLE);
                        loopModeText.setVisibility(View.VISIBLE);
                        return true;
                    case R.id.nav_permanent:
                        liveCycle = LockLivecycle.PERMANENT;
                        effectiveDateLayout.setVisibility(View.GONE);
                        invalidDateLayout.setVisibility(View.GONE);
                        loopModeLayout.setVisibility(View.GONE);
                        startTimeLayout.setVisibility(View.GONE);
                        endTimeLayout.setVisibility(View.GONE);
                        loopModeText.setVisibility(View.GONE);
                        return true;
                    case R.id.nav_one_time:
                        liveCycle = LockLivecycle.ONCE;
                        effectiveDateLayout.setVisibility(View.VISIBLE);
                        invalidDateLayout.setVisibility(View.VISIBLE);
                        loopModeLayout.setVisibility(View.GONE);
                        startTimeLayout.setVisibility(View.GONE);
                        endTimeLayout.setVisibility(View.GONE);
                        loopModeText.setVisibility(View.GONE);
                        return true;
                }
                return false;
            };

}
