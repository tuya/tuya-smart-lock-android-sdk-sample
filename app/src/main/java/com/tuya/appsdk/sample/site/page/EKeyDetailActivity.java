package com.tuya.appsdk.sample.site.page;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.EKeyBean;
import com.thingclips.smart.lock.bean.EKeyResp;
import com.thingclips.smart.lock.bean.EKeyUpdateParams;
import com.thingclips.smart.lock.bean.LockLivecycle;
import com.thingclips.smart.lock.bean.OnceEKeyCreateParams;
import com.thingclips.smart.lock.bean.PeriodicityEKeyCreateParams;
import com.thingclips.smart.lock.bean.PermanentEKeyCreateParams;
import com.tuya.appsdk.sample.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Objects;

public class EKeyDetailActivity extends AppCompatActivity {

    private long siteId;
    private String deviceId;
    private String ekeyId;
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
    private TextView accountEditText;
    private EditText userNameEditText;

    private HashSet<Integer> grayButtonIndex = new HashSet<>();
    private EKeyBean eKeyBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ekey_detail_activity);

        setTitle(R.string.ekey_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }
        if (intent != null && intent.hasExtra("ekey_id")) {
            ekeyId = intent.getStringExtra("ekey_id");
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


        View saveButton = findViewById(R.id.remove);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removeEKey();
            }
        });
        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateEKey();
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
        ThingOSLock.newLockInstance(deviceId).getEKeyDetail(siteId, ekeyId, new IThingResultCallback<EKeyBean>() {
            @Override
            public void onSuccess(EKeyBean result) {
                eKeyBean = result;
                render(result);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(EKeyDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEKey() {
        if (eKeyBean == null) {
            return;
        }
        EKeyUpdateParams eKeyUpdateParams = new EKeyUpdateParams();
        eKeyUpdateParams.ekeyId = eKeyBean.ekeyId;
        eKeyUpdateParams.groupId = siteId;
        eKeyUpdateParams.deviceId = deviceId;

        String ekeyName = userNameEditText.getText().toString().trim();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        long effectiveTime = 0L;
        long invalidTime = 0L;
        try {
            effectiveTime = dateFormat.parse(effectiveEditText.getText().toString()).getTime();
            invalidTime = dateFormat.parse(invalidEditText.getText().toString()).getTime();
        } catch (ParseException e) {
        }
        String startMinuteFormat = startTimeEditText.getText().toString().trim();
        String endMinuteFormat = endTimeEditText.getText().toString().trim();
        String workingDay = getWorkingDay();

        if (!TextUtils.isEmpty(ekeyName)) {
            eKeyUpdateParams.ekeyName = ekeyName;
        }
        if (effectiveTime != 0) {
            eKeyUpdateParams.effectiveTime = effectiveTime;
        }
        if (invalidTime != 0) {
            eKeyUpdateParams.invalidTime = invalidTime;
        }
        if (!TextUtils.isEmpty(workingDay)) {
            eKeyUpdateParams.workingDay = workingDay;
        }
        if (!TextUtils.isEmpty(startMinuteFormat) && !TextUtils.isEmpty(endMinuteFormat)) {
            String[] startMinute = startMinuteFormat.split(":");
            String[] endMinute = endMinuteFormat.split(":");
            long startMinuteL = Long.parseLong(startMinute[0]) * 60 + Long.parseLong(startMinute[1]);
            long endMinuteL = Long.parseLong(endMinute[0]) * 60 + Long.parseLong(endMinute[1]);
            eKeyUpdateParams.startMinute = startMinuteL;
            eKeyUpdateParams.endMinute = endMinuteL;
        }
        ThingOSLock.newLockInstance(deviceId).updateEKey(eKeyUpdateParams, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(EKeyDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(EKeyDetailActivity.this, R.string.failed + ":" + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

    }


    private void render(EKeyBean result) {
        String livecycleType = result.livecycleType;
        if (livecycleType.equals(LockLivecycle.PERMANENT)) {
            effectiveDateLayout.setVisibility(View.GONE);
            invalidDateLayout.setVisibility(View.GONE);
            loopModeLayout.setVisibility(View.GONE);
            startTimeLayout.setVisibility(View.GONE);
            endTimeLayout.setVisibility(View.GONE);
            loopModeText.setVisibility(View.GONE);
        } else if (livecycleType.equals(LockLivecycle.ONCE)) {
            effectiveDateLayout.setVisibility(View.VISIBLE);
            invalidDateLayout.setVisibility(View.VISIBLE);
            loopModeLayout.setVisibility(View.GONE);
            startTimeLayout.setVisibility(View.GONE);
            endTimeLayout.setVisibility(View.GONE);
            loopModeText.setVisibility(View.GONE);
        } else {
            effectiveDateLayout.setVisibility(View.VISIBLE);
            invalidDateLayout.setVisibility(View.VISIBLE);
            loopModeLayout.setVisibility(View.VISIBLE);
            startTimeLayout.setVisibility(View.VISIBLE);
            endTimeLayout.setVisibility(View.VISIBLE);
            loopModeText.setVisibility(View.VISIBLE);
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        accountEditText.setText(result.account);
        userNameEditText.setText(result.ekeyName);
        effectiveEditText.setText(dateFormat.format(result.liveCycle.effectiveTime));
        invalidEditText.setText(dateFormat.format(result.liveCycle.invalidTime));
        if (!TextUtils.isEmpty(result.liveCycle.workingDay)) {
            String workingDay = result.liveCycle.workingDay;
            for (int i = 0; i < workingDay.length(); i++) {
                char currentStatus = workingDay.charAt(i);
                if (currentStatus == '0') {
                    grayButtonIndex.add(i);
                }
                loopModeLayout.getChildAt(i).setBackgroundResource(currentStatus == '1' ? R.drawable.bg_blue_button : R.drawable.bg_gray_button);
            }
        }

        if (result.liveCycle.endMinute != 0) {
            long startMinute = result.liveCycle.startMinute;
            long endMinute = result.liveCycle.endMinute;
            int startH = (int) (startMinute / 60);
            int startM = (int) (startMinute - startH * 60);
            int endH = (int) (endMinute / 60);
            int endM = (int) (endMinute - endH * 60);
            startTimeEditText.setText(startH + ":" + startM);
            endTimeEditText.setText(endH + ":" + endM);
        }
    }

    private void removeEKey() {
        ThingOSLock.newLockInstance(deviceId).removeEKey(siteId, ekeyId, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(EKeyDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(EKeyDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getWorkingDay() {
        StringBuilder workingDay = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            workingDay.append(grayButtonIndex.contains(i) ? "0" : "1");
        }
        return workingDay.toString();
    }
}
