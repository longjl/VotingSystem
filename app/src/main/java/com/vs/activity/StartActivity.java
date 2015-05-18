package com.vs.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vs.Constant;
import com.vs.R;
import com.vs.dao.ReportDao;
import com.vs.model.Progress;
import com.vs.model.ReportResult;
import com.vs.network.VSClient;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by longjianlin on 15/4/15.
 */
public class StartActivity extends BaseActivity implements View.OnClickListener {
    private LinearLayout layout_setting;//设置
    private LinearLayout layout_start_vote;//开始投票
    private LinearLayout layout_update;//系统更新
    private LinearLayout layout_upload;//上传
    private ReportDao dao;
    private List<ReportResult> reportResultList = new ArrayList<ReportResult>();//上传投票数据
    private long waitTime = 2000;  //sign out wait time
    private long touchTime = 0;    //sign out touch time

    private ProgressDialog dialogLoading;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        dao = new ReportDao(this);

        layout_setting = (LinearLayout) findViewById(R.id.layout_setting);
        layout_setting.setOnClickListener(this);

        layout_start_vote = (LinearLayout) findViewById(R.id.layout_start_vote);
        layout_start_vote.setOnClickListener(this);

        layout_update = (LinearLayout) findViewById(R.id.layout_update);
        layout_update.setOnClickListener(this);

        layout_upload = (LinearLayout) findViewById(R.id.layout_upload);
        layout_upload.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = null;
        if (v.getId() == layout_setting.getId()) {
            intent = new Intent(StartActivity.this, SettingActivity.class);
            startActivityForResult(intent, 100);
        } else if (v.getId() == layout_start_vote.getId()) {
            intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
        } else if (v.getId() == layout_update.getId()) {
            Toast.makeText(StartActivity.this, "已经是最新版本", Toast.LENGTH_LONG).show();
        } else if (v.getId() == layout_upload.getId()) {//上传投票记录
            if (app.isNetworkConnected(this)) {
                mobile_save();
            } else {
                Toast.makeText(StartActivity.this, "网络连接不可用,请检查你的网络连接", Toast.LENGTH_LONG).show();
            }
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////
    ////将上次投票的数据上传到服务
    /////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 上传数据
     */
    private void mobile_save() {
        if (app.getLinshiDenglumaToPrefs() == null) return;

        dialogHandler.sendEmptyMessage(1);
        reportResultList = dao.findAllByLinshiDengluma(app.getLinshiDenglumaToPrefs());
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
                index++;
            }
            RequestParams params = new RequestParams(map);
            params.put("voteMeetingId", app.temp.voteMeetingId);
            params.put("medicalRegInfoId", app.temp.medicalRegInfoId);
            params.put("macAddress", app.macAddress);

            String url = Constant.BASE_HTTP + app.getServerUrlToPrefs() + "/tpms/mobile_save.mobile";
            VSClient.post(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (response.optInt(Constant.STATUS) == 1) {
                        mobile_voteConfirm();
                    } else {
                        Toast.makeText(StartActivity.this, response.optString("tipMessage"), Toast.LENGTH_LONG).show();
                        dialogHandler.sendEmptyMessage(0);
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                    Toast.makeText(StartActivity.this, "投票记录上传失败", Toast.LENGTH_LONG).show();
                    dialogHandler.sendEmptyMessage(0);
                }
            });
        }
    }

    /**
     * 确认提交
     */
    private void mobile_voteConfirm() {
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
            params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
            params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
            params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
            params.put("macAddress", app.macAddress);
            VSClient.get(url, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    if (response.optInt(Constant.STATUS) == 1) {
                        Toast.makeText(StartActivity.this, "投票记录上传成功", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(StartActivity.this, response.optString("tipMessage"), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {

                }

                @Override
                public void onFinish() {
                    dialogHandler.sendEmptyMessage(0);
                }
            });
        } else {
            dialogHandler.sendEmptyMessageDelayed(0, 2000);
        }
    }


    //dialogHandler
    private Handler dialogHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (dialogLoading != null) {
                        dialogLoading.dismiss();
                    }
                    break;
                case 1:
                    dialogLoading = ProgressDialog.show(StartActivity.this, "", "投票记录正在上传中,请稍等...", true, false);
                    break;
                default:
                    break;
            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == 100) {
            Toast.makeText(StartActivity.this, "服务器地址设置成功", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) { // 监控返回键
            long currentTime = System.currentTimeMillis();
            if ((currentTime - touchTime) >= waitTime) {
                Toast.makeText(StartActivity.this, "再按一次退出系统", Toast.LENGTH_SHORT).show();
                touchTime = currentTime;
            } else {
                app.exit();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }
}
