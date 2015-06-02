package com.vs.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.vs.R;

/**
 * Created by longjianlin on 15/4/18.
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private EditText et_url;
    private Button btn_ok;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        et_url = (EditText) findViewById(R.id.et_url);
        btn_ok = (Button) findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(this);

        initDate();
    }


    private void initDate() {
        if (app.getServerUrlToPrefs() != null && app.getServerUrlToPrefs().length() > 0) {
            et_url.setText(app.getServerUrlToPrefs());
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btn_ok.getId()) {
            if (et_url.getText() != null && et_url.getText().toString().length() > 0) {
                app.setServerUrlToPrefs(et_url.getText().toString());
                setResult(100);
                finish();
            } else {
                Toast.makeText(SettingActivity.this, "服务器地址不能为空", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
    }
}
