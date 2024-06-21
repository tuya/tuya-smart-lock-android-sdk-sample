package com.tuya.appsdk.sample.site.page;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.api.ILockPassword;
import com.thingclips.smart.lock.bean.KeyStatus;
import com.thingclips.smart.lock.bean.PasswordBean;
import com.thingclips.smart.lock.bean.PasswordType;
import com.thingclips.smart.lock.bean.PasswordUpdateParams;
import com.tuya.appsdk.sample.R;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Objects;

public class PasswordDetailActivity extends AppCompatActivity {

    private long siteId;
    private String deviceId;
    private String passwordId;
    private View effectiveDateLayout;
    private TextView effectiveEditText;
    private View invalidDateLayout;
    private TextView invalidEditText;
    private LinearLayout loopModeLayout;
    private View loopModeText;
    private View startTimeLayout;
    private TextView startTimeEditText;
    private View endTimeLayout;
    private TextView endTimeEditText;
    private EditText userNameEditText;

    private PasswordBean passwordBean;

    private HashSet<Integer> grayButtonIndex = new HashSet<>();
    private TextView retryCreateView;
    private TextView removeView;
    private ILockPassword lockPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.password_detail_activity);
        setTitle(R.string.password_detail);

        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("site_id")) {
            siteId = intent.getLongExtra("site_id", 0L);
        }
        if (intent != null && intent.hasExtra("device_id")) {
            deviceId = intent.getStringExtra("device_id");
        }
        if (intent != null && intent.hasExtra("password_id")) {
            passwordId = intent.getStringExtra("password_id");
        }

        lockPassword = ThingOSLock.newPasswordInstance(siteId, deviceId, passwordId);

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

        retryCreateView = findViewById(R.id.retry_create);

        findViewById(R.id.forceRemove).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThingOSLock.getPasswordManager().forceRemoveOnlinePassword(siteId, deviceId, passwordId, new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(PasswordDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(PasswordDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });

            }
        });

        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatePassword();
            }
        });


        removeView = findViewById(R.id.remove);
        removeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePassword();
            }
        });

        lockPassword.getPasswordDetail(new IThingResultCallback<PasswordBean>() {
            @Override
            public void onSuccess(PasswordBean result) {
                passwordBean = result;
                render(result);
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(PasswordDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        retryCreateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lockPassword.retryCreateOnlinePassword(new IThingResultCallback<Boolean>() {
                    @Override
                    public void onSuccess(Boolean result) {
                        Toast.makeText(PasswordDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(String errorCode, String errorMessage) {
                        Toast.makeText(PasswordDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void updatePassword() {
        PasswordUpdateParams params = new PasswordUpdateParams();
        params.deviceId = deviceId;
        params.groupId = siteId;
        params.passwordId = passwordId;
        params.passwordName = userNameEditText.getText().toString();
        ThingOSLock.newPasswordInstance(siteId, deviceId, passwordId).modifyPassword(params, new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(PasswordDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(PasswordDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void render(PasswordBean result) {
        int livecycleType = result.passwordType;
        if (livecycleType == PasswordType.TIME_PERMANENT_ONLINE) {
            //permanent online
            effectiveDateLayout.setVisibility(View.GONE);
            invalidDateLayout.setVisibility(View.GONE);
            loopModeLayout.setVisibility(View.GONE);
            startTimeLayout.setVisibility(View.GONE);
            endTimeLayout.setVisibility(View.GONE);
            loopModeText.setVisibility(View.GONE);
        } else if (livecycleType == PasswordType.TIME_ONCE_OFFLINE) {
            effectiveDateLayout.setVisibility(View.VISIBLE);
            invalidDateLayout.setVisibility(View.VISIBLE);
            loopModeLayout.setVisibility(View.GONE);
            startTimeLayout.setVisibility(View.GONE);
            endTimeLayout.setVisibility(View.GONE);
            loopModeText.setVisibility(View.GONE);
        } else if (livecycleType == PasswordType.TIME_LIMIT_ONLINE) {
            effectiveDateLayout.setVisibility(View.VISIBLE);
            invalidDateLayout.setVisibility(View.VISIBLE);
            loopModeLayout.setVisibility(View.VISIBLE);
            startTimeLayout.setVisibility(View.VISIBLE);
            endTimeLayout.setVisibility(View.VISIBLE);
            loopModeText.setVisibility(View.VISIBLE);
        } else if (livecycleType == PasswordType.TIME_LIMIT_OFFLINE) {
            effectiveDateLayout.setVisibility(View.VISIBLE);
            invalidDateLayout.setVisibility(View.VISIBLE);
            loopModeLayout.setVisibility(View.GONE);
            startTimeLayout.setVisibility(View.GONE);
            endTimeLayout.setVisibility(View.GONE);
            loopModeText.setVisibility(View.GONE);
        }

        retryCreateView.setVisibility(Objects.equals(result.keyStatus, KeyStatus.CREATE_FAIL) ? View.VISIBLE : View.GONE);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        userNameEditText.setText(result.passwordName);
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

    private void removePassword() {
        if (passwordBean == null)
            return;

        IThingResultCallback<Boolean> callback = new IThingResultCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                Toast.makeText(PasswordDetailActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String errorCode, String errorMessage) {
                Toast.makeText(PasswordDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        };
        if (passwordBean.passwordType == PasswordType.TIME_LIMIT_ONLINE || passwordBean.passwordType == PasswordType.TIME_PERMANENT_ONLINE) {
            ThingOSLock.getPasswordManager().removeOnlinePassword(siteId, deviceId, passwordId, passwordBean.lockId, callback);
        } else if (passwordBean.passwordType == PasswordType.TIME_LIMIT_OFFLINE) {
            ThingOSLock.getPasswordManager().removeLimitOfflinePassword(siteId, deviceId, passwordId, new IThingResultCallback<String>() {
                @Override
                public void onSuccess(String result) {
                    if (TextUtils.isEmpty(result)) {
                        return;
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(PasswordDetailActivity.this);
                    builder.setTitle(R.string.success);
                    builder.setMessage(getString(R.string.clear_code) + result); // 设置对话框的消息内容
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            finish();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }

                @Override
                public void onError(String errorCode, String errorMessage) {
                    Toast.makeText(PasswordDetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (passwordBean.passwordType == PasswordType.TIME_ONCE_OFFLINE) {
            ThingOSLock.getPasswordManager().removeOnceOfflinePassword(siteId, deviceId, passwordId, callback);
        }
    }
}
