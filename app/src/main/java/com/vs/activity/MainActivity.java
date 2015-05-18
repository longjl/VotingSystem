package com.vs.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vs.Constant;
import com.vs.R;
import com.vs.adapters.ReportAdapter;
import com.vs.dao.ReportDao;
import com.vs.model.Progress;
import com.vs.model.ReportBaseVO;
import com.vs.network.VSClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends BaseActivity implements AdapterView.OnItemClickListener, AdapterView.OnClickListener {
    private GridView gridView;
    private ReportAdapter reportAdapter;
    private ArrayList<ReportBaseVO> reports = new ArrayList<ReportBaseVO>();
    private ReportDao dao;
    private Button btn_sure_vote;
    private static final int REQUEST_CODE = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.grid);
        gridView.setOnItemClickListener(this);

        btn_sure_vote = (Button) findViewById(R.id.btn_sure_vote);
        btn_sure_vote.setOnClickListener(this);
        dao = new ReportDao(this);
        reportAdapter = new ReportAdapter(this, reports);
        gridView.setAdapter(reportAdapter);

        app.setLinshiDenglumaToPrefs(app.temp.linshiDengluma);
        mobile_showAllReport_local();
    }

    /**
     * 读取本地数据
     */
    private void mobile_showAllReport_local() {
        JSONObject response = app.loadJsonObject(app.temp.linshiDengluma);
        if (response == null) {
            Toast.makeText(MainActivity.this, "数据加载失败,请重新加载数据", Toast.LENGTH_SHORT).show();
            return;
        }

        if (response.optInt(Constant.STATUS) == 1) {
            reports.clear();
            JSONArray jsonArray = response.optJSONArray(Constant.VO);
            if (jsonArray == null || jsonArray.length() == 0) return;

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject object = jsonArray.optJSONObject(i);
                ReportBaseVO report = new ReportBaseVO();
                report.reportType = object.optInt("reportType");
                report.reportSequence = object.optInt("reportSequence");
                report.reportBaseId = object.optString("reportBaseId");
                report.reportName = object.optString("reportName");
                report.voteCount = object.optInt("voteCount");

                Progress progress = dao.queryProgress(app.temp.medicalRegInfoId, app.temp.linshiDengluma, object.optString("reportBaseId"), app.temp.voteMeetingId);
                if (null != progress && null != progress.reportBaseId) {
                    report.progress = progress.progress;
                } else {
                    dao.saveProgress(app.temp.medicalRegInfoId, app.temp.linshiDengluma, object.optString("reportBaseId"), app.temp.voteMeetingId, object.optInt("voteCount"), 0);
                }
                reports.add(report);
            }
        } else {
            Toast.makeText(MainActivity.this, response.optString(Constant.TIPMESSAGE), Toast.LENGTH_SHORT).show();
        }
        handler.sendEmptyMessage(1);
    }

    /**
     * 投票报表列表
     */
    private void mobile_showAllReport() {
        String url = Constant.BASE_HTTP + "/tpms/mobile_showAllReport.mobile";
        final RequestParams params = new RequestParams();
        params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
        params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
        VSClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Log.e("mobile_showAllReport======", response.toString());
                if (response.optInt(Constant.STATUS) == 1) {
                    reports.clear();
                    JSONArray jsonArray = response.optJSONArray(Constant.VO);
                    if (jsonArray == null || jsonArray.length() == 0) return;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.optJSONObject(i);
                        ReportBaseVO report = new ReportBaseVO();
                        report.reportType = object.optInt("reportType");
                        report.reportSequence = object.optInt("reportSequence");
                        report.reportBaseId = object.optString("reportBaseId");
                        report.reportName = object.optString("reportName");
                        report.voteCount = object.optInt("voteCount");

                        Progress progress = dao.queryProgress(app.temp.medicalRegInfoId, app.temp.linshiDengluma, object.optString("reportBaseId"), app.temp.voteMeetingId);
                        if (null != progress && null != progress.reportBaseId) {
                            report.progress = progress.progress;
                        } else {
                            dao.saveProgress(app.temp.medicalRegInfoId, app.temp.linshiDengluma, object.optString("reportBaseId"), app.temp.voteMeetingId, object.optInt("voteCount"), 0);
                        }
                        reports.add(report);
                    }
                } else {
                    Toast.makeText(MainActivity.this, response.optString(Constant.TIPMESSAGE), Toast.LENGTH_SHORT).show();
                }
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(MainActivity.this, R.string.exception_prompt, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    reportAdapter.notifyDataSetChanged();
                    break;
                case 200:
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("提示")
                            .setIconAttribute(android.R.attr.alertDialogIcon)
                            .setMessage("确认投票吗?")
                            .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mobile_voteConfirm();
                                }
                            })
                            .setNegativeButton("取消", null)
                            .create().show();

                    /*for (ReportBaseVO r : reports) {
                        int count = dao.voteComplete(app.temp.medicalRegInfoId, app.temp.linshiDengluma, r.reportBaseId, app.temp.voteMeetingId);
                    }*/
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ReportBaseVO report = reportAdapter.getItem(position);
        Intent intent = null;
        switch (report.reportType) {
            case 1://直接投票
                intent = new Intent(MainActivity.this, VoteActivity.class);
                break;
            case 2:
                intent = new Intent(MainActivity.this, PersonActivity.class);
                intent.putExtra("reportType", report.reportType);
                break;
            case 3:
                intent = new Intent(MainActivity.this, PersonActivity.class);
                intent.putExtra("reportType", report.reportType);
                break;
            case 4:
                //intent = new Intent(MainActivity.this, VoteActivity.class);
                break;
            default:
                break;
        }
        if (intent != null) {
            intent.putExtra("reportBaseId", report.reportBaseId);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == REQUEST_CODE) {
            mobile_showAllReport_local();
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == btn_sure_vote.getId()) {
            handler.sendEmptyMessage(200);
        }
    }


    /**
     * 确认提交
     */
    private void mobile_voteConfirm() {
        List<Progress> progressList = dao.queryProgressByLinshiDengluma(app.temp.linshiDengluma);
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
        /*Log.e("**********", url
                        + "?linshiDengluma=" + app.temp.linshiDengluma
                        + "&medicalRegInfoId=" + app.temp.medicalRegInfoId
                        + "&voteMeetingId=" + app.temp.voteMeetingId
                        + "&macAddress=" + app.macAddress
        );*/
            final RequestParams params = new RequestParams();
            params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
            params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
            params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
            params.put("macAddress", app.macAddress);
            VSClient.get(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (response.optInt(Constant.STATUS) == 1) {
                        Toast.makeText(MainActivity.this, "确认成功", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                }

                @Override
                public void onFinish() {
                    toLogin();
                }
            });
        } else {
           // toLogin();
            Toast.makeText(MainActivity.this, "投票未完成,请先完成投票再确认", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 跳转
     */
    private void toLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // 监控返回键
            new AlertDialog.Builder(MainActivity.this).setTitle("提示")
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .setMessage("确定要退出投票吗?")
                    .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setNegativeButton("取消", null)
                    .create().show();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
