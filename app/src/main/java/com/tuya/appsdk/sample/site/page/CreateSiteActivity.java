package com.tuya.appsdk.sample.site.page;// CreateSiteActivity.java

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.thingclips.smart.home.sdk.callback.IThingResultCallback;
import com.thingclips.smart.lock.ThingOSLock;
import com.thingclips.smart.lock.bean.SiteDetail;
import com.tuya.appsdk.sample.R;

public class CreateSiteActivity extends AppCompatActivity {

    private EditText editTextSiteName, editTextLocation;
    private Button btnCreate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.site_create_layout);
        setTitle(R.string.create_site);

        editTextSiteName = findViewById(R.id.editTextSiteName);
        editTextLocation = findViewById(R.id.editTextLocation);
        btnCreate = findViewById(R.id.btnCreate);

        editTextSiteName.addTextChangedListener(textWatcher);
        editTextLocation.addTextChangedListener(textWatcher);

        updateButtonState();

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ThingOSLock.getSiteManager().createSite(editTextSiteName.getText().toString().trim(), editTextLocation.getText().toString().trim(), 0L, 0L,
                        new IThingResultCallback<SiteDetail>() {
                            @Override
                            public void onSuccess(SiteDetail result) {
                                Toast.makeText(CreateSiteActivity.this, R.string.success, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void onError(String errorCode, String errorMessage) {
                                Toast.makeText(CreateSiteActivity.this, R.string.failed, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            updateButtonState();
        }
    };

    private void updateButtonState() {
        String siteName = editTextSiteName.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        btnCreate.setEnabled(!siteName.isEmpty() && !location.isEmpty());
    }
}
