package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thingclips.sdk.os.ThingOSUser;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.OfflinePasswordCreateParams;
import com.thingclips.smart.lock.bean.OncePasswordCreateParams;
import com.thingclips.smart.lock.bean.PasswordResp;
import com.thingclips.smart.lock.bean.PasswordType;
import com.tuya.appsdk.sample.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Objects;

public class CreatePasswordActivity extends AppCompatActivity {

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
    private int passwordType = PasswordType.TIME_LIMIT_ONLINE;
    private EditText userNameEditText;

    private HashSet<Integer> grayButtonIndex = new HashSet<>();
    private BottomNavigationView subNavigationView;
    private View userNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_password_activity);

        setTitle(R.string.create_password);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }

        userNameEditText = findViewById(R.id.et_user_name);
        userNameTextView = findViewById(R.id.editTextName);

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

        subNavigationView = findViewById(R.id.bottom_navigation_sub);
        subNavigationView.setOnNavigationItemSelectedListener(navListener);

        View saveButton = findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createPassword();
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

    private void createPassword() {
        IThingResultCallback<PasswordResp> callback = new IThingResultCallback<PasswordResp>() {
            @Override
            public void onSuccess(PasswordResp result) {
                Toast.makeText(CreatePasswordActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(CreatePasswordActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        };

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (Objects.equals(passwordType, PasswordType.TIME_LIMIT_ONLINE)) {
            //limit online
            long effectiveTime = 0L;
            long invalidTime = 0L;
            try {
                effectiveTime = dateFormat.parse(effectiveEditText.getText().toString()).getTime();
                invalidTime = dateFormat.parse(invalidEditText.getText().toString()).getTime();
            } catch (ParseException e) {
                Toast.makeText(CreatePasswordActivity.this, R.string.effective_time_or_invalid_time_error, Toast.LENGTH_SHORT).show();
                return;
            }
            ThingOSLock.newLockInstance(deviceId).createLimitOnlinePassword(siteId, userNameEditText.getText().toString().trim(),
                    effectiveTime, invalidTime, getWorkingDay(),
                    startTimeEditText.getText().toString().trim(),
                    endTimeEditText.getText().toString().trim(), callback);

        } else if (Objects.equals(passwordType, PasswordType.TIME_LIMIT_OFFLINE)) {
            //limit offline
            long effectiveTime = 0L;
            long invalidTime = 0L;
            try {
                effectiveTime = dateFormat.parse(effectiveEditText.getText().toString()).getTime();
                invalidTime = dateFormat.parse(invalidEditText.getText().toString()).getTime();
            } catch (ParseException e) {
                Toast.makeText(CreatePasswordActivity.this, R.string.effective_time_or_invalid_time_error, Toast.LENGTH_SHORT).show();
                return;
            }

            OfflinePasswordCreateParams params = new OfflinePasswordCreateParams();
            params.groupId = siteId;
            params.deviceId = deviceId;
            params.passwordName = userNameEditText.getText().toString().trim();
            params.effectiveTime = effectiveTime;
            params.invalidTime = invalidTime;
            params.passwordType = PasswordType.TIME_LIMIT_OFFLINE;
            params.timeZoneId = ThingOSUser.getUserInstance().getUser().getTimezoneId();

            ThingOSLock.newLockInstance(deviceId).createOfflinePassword(params, callback);
        } else if (Objects.equals(passwordType, PasswordType.TIME_PERMANENT_ONLINE)) {
            //permanent online
            ThingOSLock.newLockInstance(deviceId).createPermanentPassword(siteId, userNameEditText.getText().toString().trim(), callback);
        } else if (Objects.equals(passwordType, PasswordType.TIME_ONCE_OFFLINE)) {
            //once offline
            OncePasswordCreateParams oncePasswordCreateParams = new OncePasswordCreateParams();
            oncePasswordCreateParams.passwordName = userNameEditText.getText().toString().trim();
            oncePasswordCreateParams.groupId = siteId;
            oncePasswordCreateParams.deviceId = deviceId;

            ThingOSLock.newLockInstance(deviceId).createOncePassword(oncePasswordCreateParams, callback);
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
                        effectiveDateLayout.setVisibility(View.VISIBLE);
                        invalidDateLayout.setVisibility(View.VISIBLE);
                        loopModeLayout.setVisibility(View.VISIBLE);
                        startTimeLayout.setVisibility(View.VISIBLE);
                        endTimeLayout.setVisibility(View.VISIBLE);
                        loopModeText.setVisibility(View.VISIBLE);
                        subNavigationView.setVisibility(View.VISIBLE);
                        passwordType = PasswordType.TIME_LIMIT_ONLINE;
                        Menu menu = subNavigationView.getMenu();
                        menu.getItem(0).setChecked(true);
                        return true;
                    case R.id.nav_permanent:
                        effectiveDateLayout.setVisibility(View.GONE);
                        invalidDateLayout.setVisibility(View.GONE);
                        loopModeLayout.setVisibility(View.GONE);
                        startTimeLayout.setVisibility(View.GONE);
                        endTimeLayout.setVisibility(View.GONE);
                        loopModeText.setVisibility(View.GONE);
                        subNavigationView.setVisibility(View.GONE);

                        passwordType = PasswordType.TIME_PERMANENT_ONLINE;
                        return true;
                    case R.id.nav_one_time:
                        effectiveDateLayout.setVisibility(View.GONE);
                        invalidDateLayout.setVisibility(View.GONE);
                        loopModeLayout.setVisibility(View.GONE);
                        startTimeLayout.setVisibility(View.GONE);
                        endTimeLayout.setVisibility(View.GONE);
                        loopModeText.setVisibility(View.GONE);
                        subNavigationView.setVisibility(View.GONE);
                        passwordType = PasswordType.TIME_ONCE_OFFLINE;
                        return true;
                    case R.id.nav_temporary_online:
                        passwordType = PasswordType.TIME_LIMIT_ONLINE;
                        loopModeLayout.setVisibility(View.VISIBLE);
                        startTimeLayout.setVisibility(View.VISIBLE);
                        endTimeLayout.setVisibility(View.VISIBLE);
                        loopModeText.setVisibility(View.VISIBLE);
                        item.setChecked(true);
                        break;
                    case R.id.nav_temporary_offline:
                        passwordType = PasswordType.TIME_LIMIT_OFFLINE;
                        loopModeLayout.setVisibility(View.GONE);
                        startTimeLayout.setVisibility(View.GONE);
                        endTimeLayout.setVisibility(View.GONE);
                        loopModeText.setVisibility(View.GONE);
                        item.setChecked(true);
                        break;
                }
                return false;
            };

}
