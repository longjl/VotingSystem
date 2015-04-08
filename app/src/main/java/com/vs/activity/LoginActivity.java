package com.vs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vs.Constant;
import com.vs.R;
import com.vs.model.Organization;
import com.vs.model.Temp;
import com.vs.network.VSClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by longjianlin on 15/3/25.
 */
public class LoginActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener {
    private Spinner mSpinner;
    private EditText et_linshiDengluma;//临时登录码
    private Button btn_linshiDengluma;
    private Button btn_start_vote;//开始投票

    private final ArrayList<Organization> mOrg = new ArrayList<Organization>();
    private ArrayAdapter<Organization> mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getActionBar().setDisplayShowTitleEnabled(false);

        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(this);
        mAdapter = new ArrayAdapter<Organization>(this, R.layout.layout_spinner_item, mOrg);

        et_linshiDengluma = (EditText) findViewById(R.id.et_linshiDengluma);

        btn_linshiDengluma = (Button) findViewById(R.id.btn_linshiDengluma);
        btn_linshiDengluma.setOnClickListener(this);

        btn_start_vote = (Button) findViewById(R.id.btn_start_vote);
        btn_start_vote.setOnClickListener(this);

        Organization organization = new Organization();
        organization.medicalRegInfoId = null;
        organization.medicalName = "请选择您的单位";
        mOrg.add(organization);

        mSpinner.setAdapter(mAdapter);
        mobile_queryAllMedicalRegInfo();
    }

    /**
     * 获取全部执业机构
     */
    private void mobile_queryAllMedicalRegInfo() {
        String url = Constant.BASE_HTTP + "/tpms/mobile_queryAllMedicalRegInfo.mobile";
        VSClient.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Log.e("response----", response.toString());
                if (response.optInt(Constant.STATUS) == 1) {
                    JSONArray vo_arr = response.optJSONArray(Constant.VO);

                    if (vo_arr == null || vo_arr.length() <= 0) return;

                    for (int i = 0; i < vo_arr.length(); i++) {
                        JSONObject object = vo_arr.optJSONObject(i);
                        Organization organization = new Organization();
                        organization.medicalName = object.optString("medicalName");
                        organization.medicalRegInfoId = object.optString("medicalRegInfoId");
                        mOrg.add(organization);
                    }
                    handler.sendEmptyMessage(1);
                } else {
                    Toast.makeText(LoginActivity.this, response.optString(Constant.TIPMESSAGE), Toast.LENGTH_SHORT).show();
                    handler.sendEmptyMessage(0);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Toast.makeText(LoginActivity.this, R.string.exception_prompt, Toast.LENGTH_SHORT).show();
                handler.sendEmptyMessage(0);
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    et_linshiDengluma.setText("");
                    break;
                case 1://请求成功
                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }

        }
    };


    /**
     * 获取临时登录码
     * medicalRegInfoId 执业机构Id
     * macAddress 移动端唯一键，用以标示移动设备
     */
    private void mobile_getLinshiDengluma() {
        String url = Constant.BASE_HTTP + "/tpms/mobile_getLinshiDengluma.mobile";
        RequestParams params = new RequestParams();
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);
        params.put("macAddress", app.macAddress);

        VSClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                 Log.e("JSONObject----", response.toString());
                if (response.optInt(Constant.STATUS) == 1) {
                    JSONObject object = response.optJSONObject(Constant.VO);
                    if (object == null) return;
                    app.temp.linshiDengluma = object.optString("linshiDengluma");
                    app.temp.voteMeetingId = object.optString("voteMeetingId");
                    et_linshiDengluma.setText(object.optString("linshiDengluma"));
                } else {
                    Toast.makeText(LoginActivity.this, response.optString(Constant.TIPMESSAGE), Toast.LENGTH_SHORT).show();
                    app.temp.linshiDengluma = null;
                    app.temp.voteMeetingId = null;
                    handler.sendEmptyMessage(0);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(LoginActivity.this, R.string.exception_prompt, Toast.LENGTH_SHORT).show();
                handler.sendEmptyMessage(0);
            }
        });
    }

    /**
     * 选择机构
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Organization organization = mOrg.get(position);
        if (organization.medicalRegInfoId != null) {
            app.temp.medicalRegInfoId = organization.medicalRegInfoId;
        } else {
            app.temp.medicalRegInfoId = null;
        }
        et_linshiDengluma.setText("");
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btn_linshiDengluma.getId()) {
            if (app.temp.medicalRegInfoId == null) {
                Toast.makeText(this, R.string.choose_unit_prompt, Toast.LENGTH_SHORT).show();
                return;
            }
            mobile_getLinshiDengluma();
        } else if (v.getId() == btn_start_vote.getId()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }



    /*
        RequestParams params = new RequestParams();
        params.put(Constant.FROM_PARAM_KEY, Constant.FROM_PARAM_VALUE);
        params.put(Constant.PAGE_NUM_KEY, pageNum);
        params.put(Constant.TOKEN_KEY, LeapApplication.getInstance().getToken());
        LeapRestClient.get(URLs.LEAP_COURSE_URL, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                loadingGone();
                stopAnim();
                completeHandler.sendEmptyMessage(0);

                if (response.optInt(Constant.CODE_KEY) != 200) return;

                JSONObject data_json = response.optJSONObject(Constant.DATA_KEY);
                if (data_json == null) return;

                JSONArray records_json = data_json.optJSONArray(Constant.RECORDS_KEY);
                if (records_json == null || records_json.length() == 0) {
                    isRequest = false;//已经没有数据了
                    return;
                }
                Message message = new Message();
                message.obj = records_json;
                mHandler.sendMessage(message);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                stopAnim();
                completeHandler.sendEmptyMessage(0);
                showToast(R.string.network_exception);
            }
        });
     */

}
