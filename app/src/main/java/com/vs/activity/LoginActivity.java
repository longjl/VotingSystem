package com.vs.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vs.Constant;
import com.vs.R;
import com.vs.dao.ReportDao;
import com.vs.model.Organization;
import com.vs.model.Progress;
import com.vs.model.ReportBaseVO;
import com.vs.model.ReportResult;
import com.vs.network.VSClient;
import com.vs.tasks.DataHandlerAsyncTask;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by longjianlin on 15/3/25.
 */
public class LoginActivity extends BaseActivity implements AdapterView.OnItemSelectedListener, View.OnClickListener,
        DataHandlerAsyncTask.ResultListener {

    private Spinner mSpinner;
    private EditText et_linshiDengluma;//临时登录码
    private Button btn_linshiDengluma;
    private Button btn_start_vote;//开始投票

    private final ArrayList<Organization> mOrg = new ArrayList<Organization>();
    private ArrayAdapter<Organization> mAdapter;
    private List<ReportResult> reportResultList = new ArrayList<ReportResult>();//上传投票数据
    private ReportDao dao;
    private ProgressDialog dialog;
    private ProgressDialog dialogLoading;
    private ProgressDialog progressDialog;
    private DataHandlerAsyncTask.ResultListener resultListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        app.activities.add(this);
        dao = new ReportDao(this);

        resultListener = this;

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

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mobile_queryAllMedicalRegInfo();
        } else {
            Toast.makeText(LoginActivity.this, "SDcard不存在", Toast.LENGTH_SHORT).show();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    ////    第1步  获取全部机构信息
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取全部执业机构
     */
    private void mobile_queryAllMedicalRegInfo() {
        String server_url = app.getServerUrlToPrefs();
        if (server_url == null || server_url.length() == 0) {
            Toast.makeText(this, "请设置服务器地址", Toast.LENGTH_SHORT).show();
            return;
        }
        dialogLoading = ProgressDialog.show(LoginActivity.this, "", "正在获取单位信息. 请稍等...", true, true);
        String url = Constant.BASE_HTTP + server_url + "/tpms/mobile_queryAllMedicalRegInfo.mobile";
        VSClient.get(url, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
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
                    handler.sendEmptyMessage(200);
                } else {
                    Toast.makeText(LoginActivity.this, response.optString(Constant.TIPMESSAGE), Toast.LENGTH_LONG).show();
                    handler.sendEmptyMessage(100);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                  JSONObject errorResponse) {
                Toast.makeText(LoginActivity.this, R.string.exception_prompt, Toast.LENGTH_LONG).show();
                handler.sendEmptyMessage(100);
            }
        });
    }

    //数据处理
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:       //获取单位信息失败
                    hideDialog();
                    //   btn_start_vote.setEnabled(false);
                    et_linshiDengluma.setText(null);
                    break;
                case 200:       //获取单位信息成功
                    hideDialog();
                    mobile_save();
                    mAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    /////////////////////////////////////////////////////////////////////////////////////////
    ////    第2步  将上次投票的数据上传到服务
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 上传数据
     */
    private void mobile_save() {
        if (app.getPreValue("linshiDengluma") == null) return;

        reportResultList = dao.findAllByLinshiDengluma(app.getPreValue("linshiDengluma"));
        if (reportResultList != null && reportResultList.size() > 0) {
            Map<String, String> map = new HashMap<String, String>();
            int index = 0;
            for (ReportResult result : reportResultList) {
                map.put("reportResultList[" + index + "].medicalRegInfoId", result.medicalRegInfoId);
                map.put("reportResultList[" + index + "].reportBaseId", result.reportBaseId);
                map.put("reportResultList[" + index + "].pingjiarenLinshiDengluma", result.pingjiarenLinshiDengluma);
                map.put("reportResultList[" + index + "].reportDetailId", result.reportDetailId);
                map.put("reportResultList[" + index + "].reportResult", result.reportResult);
                map.put("reportResultList[" + index + "].lingdaoGanbuId", result.lingdaoGanbuId);
                map.put("reportResultList[" + index + "].createDate", result.createDate);
                index++;
            }

            RequestParams params = new RequestParams(map);
            params.put("voteMeetingId", reportResultList.get(0).voteMeetingId);
            params.put("medicalRegInfoId", reportResultList.get(0).medicalRegInfoId);
            params.put("macAddress", app.macAddress);

            String url = Constant.BASE_HTTP + app.getServerUrlToPrefs() + "/tpms/mobile_save.mobile";
            VSClient.post(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (response.optInt(Constant.STATUS) == 1) {
                        mobile_voteConfirm(app.getPreValue("linshiDengluma"), reportResultList.get(0).voteMeetingId, reportResultList.get(0).medicalRegInfoId);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                }
            });
        }
    }

    /**
     * 确认提交
     */
    private void mobile_voteConfirm(final String dengluma, final String voteMeetingId, final String medicalRegInfoId) {
        if (app.getLinshiDenglumaToPrefs() == null) return;
        List<Progress> progressList = dao.queryProgressByLinshiDengluma(app.getLinshiDenglumaToPrefs());
        if (progressList == null || progressList.size() == 0) return;

        boolean isConfirm = true;
        for (Progress progress : progressList) {
            if (progress.progress != progress.voteCount || progress.voteCount == 0) {
                isConfirm = false;
                break;
            }
        }
        if (isConfirm) {
            String url = Constant.BASE_HTTP + app.getServerUrlToPrefs() + "/tpms/mobile_voteConfirm.mobile";
            final RequestParams params = new RequestParams();
            params.put("linshiDengluma", dengluma);//临时登陆码
            params.put("voteMeetingId", voteMeetingId);
            params.put("medicalRegInfoId", medicalRegInfoId);
            params.put("macAddress", app.macAddress);
            VSClient.get(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                }
            });
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////
    ////  获取临时登录码
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 获取临时登录码
     * medicalRegInfoId 执业机构Id
     * macAddress 移动端唯一键，用以标示移动设备
     */
    private void mobile_getLinshiDengluma(final String server_url) {
        if (server_url == null || server_url.length() == 0) {
            Toast.makeText(this, "请设置服务器地址", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog();

        String url = Constant.BASE_HTTP + server_url + "/tpms/mobile_getLinshiDengluma.mobile";
        RequestParams params = new RequestParams();
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);
        params.put("macAddress", app.macAddress);

        VSClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                hideProgressDialog();
                if (response.optInt(Constant.STATUS) == 1) {
                    JSONObject object = response.optJSONObject(Constant.VO);
                    if (object == null) return;

                    app.temp.linshiDengluma = object.optString("linshiDengluma");
                    app.temp.voteMeetingId = object.optString("voteMeetingId");
                    et_linshiDengluma.setText(object.optString("linshiDengluma"));
                    if (object.optString("linshiDengluma") != null) {
                        //mobile_showAllReport(server_url);
                        //btn_start_vote.setEnabled(true);
                    }
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
                hideProgressDialog();
                handler.sendEmptyMessage(0);
            }
        });
    }

    /**
     * 查询所以信息
     * 临时登陆码
     * 执业机构主键
     * 投票会议主键
     */
    private void mobile_getAllMessage(final String server_url) {
        String url = Constant.BASE_HTTP + server_url + "/tpms/mobile_getAllMessage.mobile";
        RequestParams params = new RequestParams();
        params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
        params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
        downloadHandler.sendEmptyMessage(1);
        String[] allowedContentTypes = new String[]{"APPLICATION/OCTET-STREAM"};
        VSClient.get(url, params, new BinaryHttpResponseHandler(allowedContentTypes) {

            @Override
            public void onSuccess(int i, Header[] headers, byte[] bytes) {

                if (bytes != null && bytes.length > 0) {
                    DataHandlerAsyncTask.execute(resultListener, bytes, app.temp);
                }
            }

            @Override
            public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                Toast.makeText(LoginActivity.this, "数据加载失败", Toast.LENGTH_SHORT).show();
                downloadHandler.sendEmptyMessage(2);
            }
        });
    }

    private Handler downloadHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                progressDialog = ProgressDialog.show(LoginActivity.this, "", "数据加载中, 请稍等...", true, false);
            } else if (msg.what == 2) {
                if (progressDialog != null) {
                    progressDialog.dismiss();
                }
            }
        }
    };


    /**
     * 投票报表列表
     */
    private void mobile_showAllReport(String server_url) {
        String url = Constant.BASE_HTTP + server_url + "/tpms/mobile_showAllReport.mobile";
        final RequestParams params = new RequestParams();
        params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
        params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
        VSClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response.optInt(Constant.STATUS) == 1) {
                    app.saveJsonObject(response, app.temp.linshiDengluma);//存储报表列表,登录码作为文件的名字

                    JSONArray jsonArray = response.optJSONArray(Constant.VO);
                    if (jsonArray == null || jsonArray.length() == 0) return;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.optJSONObject(i);
                        ReportBaseVO report = new ReportBaseVO();
                        report.reportType = object.optInt("reportType");
                        report.reportBaseId = object.optString("reportBaseId");

                        if (report.reportType == 1 || report.reportType == 4) {//直接投票
                            mobile_toVoteDetailPage(report.reportBaseId);
                        } else if (report.reportType == 2 || report.reportType == 3) {//人员投票
                            mobile_toVoteDetailPage(report.reportBaseId);
                            mobile_queryAllVotePerson(report.reportBaseId);
                        }
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                progressHandler.sendEmptyMessage(0);
            }

            @Override
            public void onFinish() {
                progressHandler.sendEmptyMessageDelayed(0, 12000);
            }
        });
    }


    /**
     * 人员投票
     */
    private void mobile_queryAllVotePerson(final String reportBaseId) {
        String url = Constant.BASE_HTTP + app.getServerUrlToPrefs() + "/tpms/mobile_queryAllVotePerson.mobile";
        RequestParams params = new RequestParams();
        params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
        params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
        params.put("reportBaseId", reportBaseId);//报表编号
        VSClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response.optInt(Constant.STATUS) == 1) {
                    app.saveJsonObject(response, reportBaseId + "_user");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            }
        });
    }

    /**
     * 投票接口
     *
     * @param reportBaseId
     */
    private void mobile_toVoteDetailPage(final String reportBaseId) {
        String url = Constant.BASE_HTTP + app.getServerUrlToPrefs() + "/tpms/mobile_toVoteDetailPage.mobile";
        RequestParams params = new RequestParams();
        params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
        params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
        params.put("reportBaseId", reportBaseId);//报表编号

        VSClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response.optInt(Constant.STATUS) == 1) {
                    app.saveJsonObject(response, reportBaseId);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
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
        final Organization organization = mOrg.get(position);
        if (organization.medicalRegInfoId != null) {
            app.selectionIndex = position;
            new AlertDialog.Builder(LoginActivity.this).setTitle("提示")
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setMessage("请确认您的单位是" + organization.medicalName)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            app.temp.medicalRegInfoId = organization.medicalRegInfoId;
                            et_linshiDengluma.setText("");
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mSpinner.setSelection(0);
                            mAdapter.notifyDataSetChanged();
                        }
                    })
                    .create().show();
        } else {
            app.temp.medicalRegInfoId = null;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btn_linshiDengluma.getId()) {//获取临时登录码
            if (app.temp.medicalRegInfoId == null) {
                Toast.makeText(this, R.string.choose_unit_prompt, Toast.LENGTH_SHORT).show();
                return;
            }
            mobile_getLinshiDengluma(app.getServerUrlToPrefs());
        } else if (v.getId() == btn_start_vote.getId()) {//开始投票
            if (et_linshiDengluma.getText() == null || et_linshiDengluma.getText().toString().length() == 0) {
                Toast.makeText(LoginActivity.this, "请重新获取登录码", Toast.LENGTH_SHORT).show();
            } else {
                mobile_getAllMessage(app.getServerUrlToPrefs());
            }
            /*new AlertDialog.Builder(LoginActivity.this).setTitle("提示")
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setMessage("请确认您的单位是" + mOrg.get(pos).medicalName)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create().show();*/

        }
    }

    private void showProgressDialog() {
        dialog = ProgressDialog.show(LoginActivity.this, "", "正在获取登录码. 请稍等...", true, false);
    }


    private Handler progressHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            hideProgressDialog();
        }
    };

    private void hideProgressDialog() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    private void hideDialog() {
        if (dialogLoading != null) {
            dialogLoading.dismiss();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setDisplayShowTitleEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(false);
    }

    /**
     * 保存zip 回调
     */
    @Override
    public void onLoaded() {
        app.setPreKey("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
        app.setPreKey("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
        app.setPreKey("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }
}
