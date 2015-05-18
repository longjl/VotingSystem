package com.vs.activity;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

import com.vs.R;
import com.vs.VSApplication;
import com.vs.adapters.ReportAdapter;

/**
 * Created by longjianlin on 15/3/26.
 */
public class BaseActivity extends Activity {
    public VSApplication app;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = VSApplication.getApplication(this);
        app.activities.add(this);
    }

}
