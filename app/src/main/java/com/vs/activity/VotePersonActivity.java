package com.vs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vs.Constant;
import com.vs.R;
import com.vs.adapters.VoteAdapter;
import com.vs.dao.ReportDao;
import com.vs.model.InputTemplateVO;
import com.vs.model.Person;
import com.vs.model.ReportBaseVO;
import com.vs.model.ReportDetailVO;
import com.vs.model.ReportResult;
import com.vs.network.VSClient;
import com.vs.views.MyListView;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by longjianlin on 15/4/5.
 */
public class VotePersonActivity extends BaseActivity implements AdapterView.OnItemClickListener, RadioGroup.OnCheckedChangeListener, View.OnClickListener {
    private TextView tv_reportName;//报表名字
    private TextView tv_name;//干部名字
    private TextView tv_xianrenzhiwu;
    private TextView tv_sex;
    private TextView tv_birthday;
    private TextView tv_renzhishijian;
    private TextView tv_yuanrenZhiwu;

    private TextView tv_titleFirst;
    private TextView tv_detailFirst;

    private TextView tv_titleSecond;
    private TextView tv_detailSecond;
    private TextView tv_titleVote;

    private EditText et_extraResult;//输入框

    private MyListView lv_vote;
    private RadioGroup rg_vote;
    private RadioButton rb_a;   //选项A
    private RadioButton rb_b;   //选项B
    private RadioButton rb_c;   //选项C
    private RadioButton rb_d;   //选项D
    private RadioButton rb_e;   //选项E

    private LinearLayout layout_elementtype_1;//文本 elementType = 1  意见和建议
    private LinearLayout layout_elementtype_2;//单选框 elementType = 2
    private LinearLayout layout_reportType_3;//第3类报表
    private LinearLayout layout_first;//区域1
    private LinearLayout layout_second; //区域2

    private Button btn_next;//保存 或 下一步

    private Person person;
    private int reportType;
    private String reportBaseId;

    private RadioButton[] radios;

    private ReportDao dao;
    private ReportBaseVO reportBaseVO;
    private ReportResult reportResult;
    private int mCurrentPosition = 0;//当前显示的位置

    private ArrayList<ReportResult> list = new ArrayList<ReportResult>();
    private List<ReportResult> reportResultList = new ArrayList<ReportResult>();//上传投票数据

    private VoteAdapter adapter;
    private SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static final int RESULT_CODE = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote_person);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(false);

        initView();
        getIntentData();
    }

    /**
     * 初始化组件
     */
    private void initView() {
        tv_reportName = (TextView) findViewById(R.id.tv_reportName);
        et_extraResult = (EditText) findViewById(R.id.et_extraResult);

        tv_name = (TextView) findViewById(R.id.tv_name);
        tv_xianrenzhiwu = (TextView) findViewById(R.id.tv_xianrenzhiwu);
        tv_sex = (TextView) findViewById(R.id.tv_sex);
        tv_birthday = (TextView) findViewById(R.id.tv_birthday);
        tv_renzhishijian = (TextView) findViewById(R.id.tv_renzhishijian);
        tv_yuanrenZhiwu = (TextView) findViewById(R.id.tv_yuanrenZhiwu);
        tv_titleFirst = (TextView) findViewById(R.id.tv_titleFirst);
        tv_detailFirst = (TextView) findViewById(R.id.tv_detailFirst);
        lv_vote = (MyListView) findViewById(R.id.lv_vote);
        tv_titleSecond = (TextView) findViewById(R.id.tv_titleSecond);
        tv_detailSecond = (TextView) findViewById(R.id.tv_detailSecond);
        tv_titleVote = (TextView) findViewById(R.id.tv_titleVote);

        rg_vote = (RadioGroup) findViewById(R.id.rg_vote);

        rb_a = (RadioButton) findViewById(R.id.rb_a);
        rb_b = (RadioButton) findViewById(R.id.rb_b);
        rb_c = (RadioButton) findViewById(R.id.rb_c);
        rb_d = (RadioButton) findViewById(R.id.rb_d);
        rb_e = (RadioButton) findViewById(R.id.rb_e);

        layout_elementtype_1 = (LinearLayout) findViewById(R.id.layout_elementtype_1);
        layout_elementtype_2 = (LinearLayout) findViewById(R.id.layout_elementtype_2);
        layout_reportType_3 = (LinearLayout) findViewById(R.id.layout_reportType_3);
        layout_first = (LinearLayout) findViewById(R.id.layout_first);
        layout_second = (LinearLayout) findViewById(R.id.layout_second);

        btn_next = (Button) findViewById(R.id.btn_next);
    }

    /**
     * 获取参数信息
     */
    private void getIntentData() {
        dao = new ReportDao(this);
        person = new Person();
        person.lingdaoGanbuId = getIntent().getStringExtra("lingdaoGanbuId");
        person.renzhishijian = getIntent().getStringExtra("renzhishijian");
        person.birthday = getIntent().getStringExtra("birthday");
        person.sex = getIntent().getStringExtra("sex");
        person.yuanrenZhiwu = getIntent().getStringExtra("yuanrenZhiwu");
        person.name = getIntent().getStringExtra("name");
        person.xianrenzhiwu = getIntent().getStringExtra("xianrenzhiwu");

        reportType = getIntent().getIntExtra("reportType", 0);
        reportBaseId = getIntent().getStringExtra("reportBaseId");

        reportResult = new ReportResult();
        reportResult.reportBaseId = reportBaseId;
        reportResult.pingjiarenLinshiDengluma = app.temp.linshiDengluma;
        reportResult.voteMeetingId = app.temp.voteMeetingId;
        reportResult.medicalRegInfoId = app.temp.medicalRegInfoId;
        reportResult.lingdaoGanbuId = person.lingdaoGanbuId;

        radios = new RadioButton[]{rb_a, rb_b, rb_c, rb_d, rb_e};

        rg_vote.setOnCheckedChangeListener(this);
        lv_vote.setOnItemClickListener(this);
        btn_next.setOnClickListener(this);

        adapter = new VoteAdapter(this, list);
        lv_vote.setAdapter(adapter);

        mobile_toVoteDetailPage(reportBaseId);
    }

    /**
     * *************************************************************************
     * 投票接口 (获取投票信息)
     * *************************************************************************
     *
     * @param reportBaseId
     */
    private void mobile_toVoteDetailPage(final String reportBaseId) {
        String url = Constant.BASE_HTTP + "/tpms/mobile_toVoteDetailPage.mobile";
        RequestParams params = new RequestParams();
        params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
        params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
        params.put("reportBaseId", reportBaseId);//报表编号

        VSClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                //Log.e("mobile_toVoteDetailPage======", response.toString());
                if (response.optInt(Constant.STATUS) == 1) {
                    if (null == reportBaseVO) reportBaseVO = new ReportBaseVO();
                    JSONObject object = response.optJSONObject(Constant.VO);
                    reportBaseVO.reportType = object.optInt("reportType");
                    reportBaseVO.reportSequence = object.optInt("reportSequence");
                    reportBaseVO.reportBaseId = object.optString("reportBaseId");
                    reportBaseVO.reportName = object.optString("reportName");
                    reportBaseVO.voteCount = object.optInt("voteCount");

                    JSONArray jsonArray = object.optJSONArray("reportDetailVOList");
                    if (jsonArray == null || jsonArray.length() == 0) return;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.optJSONObject(i);
                        ReportDetailVO reportDetailVO = new ReportDetailVO();
                        reportDetailVO.reportDetailId = obj.optString("reportDetailId");

                        reportDetailVO.titleFirst = obj.optString("titleFirst");
                        reportDetailVO.detailFirst = obj.optString("detailFirst");

                        reportDetailVO.titleSecond = obj.optString("titleSecond");
                        reportDetailVO.detailSecond = obj.optString("detailSecond");

                        reportDetailVO.elementType = obj.optInt("elementType");
                        reportDetailVO.extraResult = obj.optString("extraResult");
                        reportDetailVO.titleVote = obj.optString("titleVote");

                        if (reportDetailVO.elementType == 2) {
                            List<InputTemplateVO> inputTemplateVOList = new ArrayList<InputTemplateVO>();
                            JSONArray input_json_arr = obj.optJSONArray("inputTemplateVOList");
                            if (input_json_arr != null && input_json_arr.length() > 0) {
                                for (int k = 0; k < input_json_arr.length(); k++) {
                                    JSONObject input_obj = input_json_arr.optJSONObject(k);
                                    InputTemplateVO inputTemplateVO = new InputTemplateVO();
                                    inputTemplateVO.inputCode = input_obj.optString("inputCode");
                                    inputTemplateVO.inputDisplay = input_obj.optString("inputDisplay");
                                    inputTemplateVO.inputSequence = input_obj.optInt("inputSequence");
                                    inputTemplateVOList.add(inputTemplateVO);
                                }
                                reportDetailVO.inputTemplateVOList = inputTemplateVOList;
                            }
                        }
                        reportBaseVO.reportDetailVOList.add(reportDetailVO);
                    }
                    handler.sendEmptyMessage(1);
                } else {
                    Toast.makeText(VotePersonActivity.this, response.optString(Constant.TIPMESSAGE), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(VotePersonActivity.this, R.string.exception_prompt, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 处理 UI显示  数据显示
     */
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (null == reportBaseVO) return;
                    showReportDetail(mCurrentPosition);
                    break;
                default:
                    break;

            }
        }
    };

    /**
     * UI显示  数据显示
     *
     * @param currentPosition 当前位置
     */
    private void showReportDetail(final int currentPosition) {
        ReportDetailVO reportDetailVO = reportBaseVO.reportDetailVOList.get(currentPosition);
        switch (reportType) {
            case 2://第2类报表
                layout_second.setVisibility(View.VISIBLE);
                layout_first.setVisibility(View.VISIBLE);
                break;
            case 3://第3类报表
                layout_reportType_3.setVisibility(View.VISIBLE);
                layout_second.setVisibility(View.GONE);
                layout_first.setVisibility(View.GONE);

                tv_renzhishijian.setText(person.renzhishijian);
                tv_birthday.setText(person.birthday);
                tv_sex.setText(person.sex);
                tv_yuanrenZhiwu.setText(person.yuanrenZhiwu);
                break;
            default:
                break;
        }
        tv_name.setText(person.name);
        tv_xianrenzhiwu.setText(person.xianrenzhiwu);

        tv_reportName.setText(reportBaseVO.reportName);
        tv_titleFirst.setText(reportDetailVO.titleFirst);
        tv_detailFirst.setText(reportDetailVO.detailFirst);
        tv_titleVote.setText(reportDetailVO.titleVote);

        switch (reportDetailVO.elementType) {
            case 1://文本
                layout_elementtype_1.setVisibility(View.VISIBLE);
                layout_elementtype_2.setVisibility(View.GONE);
                break;
            case 2://单选框
                layout_elementtype_1.setVisibility(View.GONE);
                layout_elementtype_2.setVisibility(View.VISIBLE);
                tv_titleSecond.setText(reportDetailVO.titleSecond);
                tv_detailSecond.setText(reportDetailVO.detailSecond);

                generateRadioButton(reportDetailVO.inputTemplateVOList);
                isCheckChoose(reportDetailVO);
                break;
        }

        if (reportBaseVO.reportDetailVOList.size() == (currentPosition + 1)) { //处理 SHOW_ALL
            btn_next.setText("保存");
            if (reportDetailVO.detailFirst.equals("SHOW_ALL")) {
                layout_second.setVisibility(View.GONE);
                tv_detailFirst.setVisibility(View.GONE);
                lv_vote.setVisibility(View.VISIBLE);

                list.clear();
                list.addAll(dao.findReportResults(reportResult, reportBaseVO.reportDetailVOList.size() - 1));
                adapter.notifyDataSetChanged();
            } else {
                tv_detailFirst.setVisibility(View.VISIBLE);
                lv_vote.setVisibility(View.GONE);
            }
        }
    }


    /**
     * 动态生成 RadioButton
     *
     * @param inputTemplateVOList
     */
    private void generateRadioButton(List<InputTemplateVO> inputTemplateVOList) {
        showRadio(inputTemplateVOList);
    }

    /**
     * 显示 radio
     *
     * @param inputTemplateVOList
     */
    private void showRadio(List<InputTemplateVO> inputTemplateVOList) {
        if (inputTemplateVOList.size() > 5) return;

        for (int k = 0; k < 5; k++) {
            radios[k].setVisibility(View.GONE);
        }
        for (int i = 0; i < inputTemplateVOList.size(); i++) {
            radios[i].setText(inputTemplateVOList.get(i).inputDisplay);
            radios[i].setVisibility(View.VISIBLE);
        }
        rg_vote.clearCheck();
    }

    /**
     * 检测是否已经选择
     */
    private void isCheckChoose(ReportDetailVO reportDetailVO) {
        reportResult.reportDetailId = reportDetailVO.reportDetailId;
        reportResult.lingdaoGanbuId = person.lingdaoGanbuId;
        ReportResult result = dao.findReportResult(reportResult);
        if (null == result.reportResult) return;

        switch (result.radioId) {
            case R.id.rb_a:
                rb_a.setChecked(true);
                break;
            case R.id.rb_b:
                rb_b.setChecked(true);
                break;
            case R.id.rb_c:
                rb_c.setChecked(true);
                break;
            case R.id.rb_d:
                rb_d.setChecked(true);
                break;
            case R.id.rb_e:
                rb_e.setChecked(true);
                break;
            default:
                break;
        }
    }

    /**
     * item 点击事件
     *
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        lv_vote.setVisibility(View.GONE);
        tv_detailFirst.setVisibility(View.VISIBLE);
        btn_next.setText("下一步");
        mCurrentPosition = position;
        showReportDetail(mCurrentPosition);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        isChecked = true;
    }

    /**
     * 更新或保存Radio
     */
    private void updateOrSaveRadio(int id) {
        ReportDetailVO reportDetailVO = reportBaseVO.reportDetailVOList.get(mCurrentPosition);
        InputTemplateVO inputTemplateVO = null;
        int radioId = 0;
        switch (id) {
            case R.id.rb_a:
                radioId = R.id.rb_a;
                inputTemplateVO = reportDetailVO.inputTemplateVOList.get(0);
                break;
            case R.id.rb_b:
                radioId = R.id.rb_b;
                inputTemplateVO = reportDetailVO.inputTemplateVOList.get(1);
                break;
            case R.id.rb_c:
                radioId = R.id.rb_c;
                inputTemplateVO = reportDetailVO.inputTemplateVOList.get(2);
                break;
            case R.id.rb_d:
                radioId = R.id.rb_d;
                inputTemplateVO = reportDetailVO.inputTemplateVOList.get(3);
                break;
            case R.id.rb_e:
                radioId = R.id.rb_e;
                inputTemplateVO = reportDetailVO.inputTemplateVOList.get(4);
                break;
            default:
                break;
        }

        if (null == inputTemplateVO) return;

        reportResult.radioId = radioId;
        reportResult.reportResult = inputTemplateVO.inputCode;
        reportResult.inputDisplay = inputTemplateVO.inputDisplay;
        reportResult.detailFirst = reportDetailVO.detailFirst;
        reportResult.lingdaoGanbuId = person.lingdaoGanbuId;
        reportResult.createDate = format.format(new Date());

        dao.updateOrSaveReportResult(reportResult);
        isChecked = false;
    }


    @Override
    public void onClick(View v) {
        showNext(mCurrentPosition);
    }

    private boolean isChecked = false;//是否已经选择

    /**
     * 显示下一题
     *
     * @param currentPosition
     */
    private void showNext(final int currentPosition) {
        if (isChecked) {//判断是否已经投票(只有投票了才能下一题)
            //Toast.makeText(VotePersonActivity.this, rg_vote.getCheckedRadioButtonId() + "", Toast.LENGTH_SHORT).show();
            int count = reportBaseVO.reportDetailVOList.size();
            if (count > (currentPosition + 1)) {
                updateOrSaveRadio(rg_vote.getCheckedRadioButtonId());//下一步之前保存上一次的选择记录
                mCurrentPosition += 1;
                showReportDetail(mCurrentPosition);
            } else {
                updateOrSaveRadio(rg_vote.getCheckedRadioButtonId());//下一步之前保存上一次的选择记录
                mobile_save();
            }
        } else {
            Toast.makeText(VotePersonActivity.this, "你还未投票", Toast.LENGTH_SHORT).show();
            return;
        }
    }

    /**
     * 保存数据
     */
    private void mobile_save() {
        reportResultList = dao.findAll(reportResult);
        if (reportResultList == null || reportResultList.size() == 0) {
            Toast.makeText(this, "您还未完成投票", Toast.LENGTH_SHORT).show();
            return;
        }
        Map<String, String> map = new HashMap<String, String>();
        int index = 0;
        for (ReportResult result : reportResultList) {
            map.put("reportResultList[" + index + "].medicalRegInfoId", result.medicalRegInfoId);
            map.put("reportResultList[" + index + "].reportBaseId", result.reportBaseId);
            map.put("reportResultList[" + index + "].pingjiarenLinshiDengluma", result.pingjiarenLinshiDengluma);
            map.put("reportResultList[" + index + "].reportDetailId", result.reportDetailId);
            map.put("reportResultList[" + index + "].reportResult", result.reportResult);
            map.put("reportResultList[" + index + "].lingdaoGanbuId", person.lingdaoGanbuId);
            map.put("reportResultList[" + index + "].extraResult", et_extraResult.getText().toString());

            index++;
        }
        RequestParams params = new RequestParams(map);
        params.put("voteMeetingId", app.temp.voteMeetingId);
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);

        String url = Constant.BASE_HTTP + "/tpms/mobile_save.mobile";
        VSClient.post(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                if (response.optInt(Constant.STATUS) == 1) {
                    if (dao.queryPersonProgress(app.temp.medicalRegInfoId, app.temp.linshiDengluma, reportResult.reportBaseId, app.temp.voteMeetingId, person.lingdaoGanbuId) == 0) {
                        dao.savePersonProgress(app.temp.medicalRegInfoId, app.temp.linshiDengluma, reportResult.reportBaseId, app.temp.voteMeetingId, person.lingdaoGanbuId);
                    }
                    Toast.makeText(VotePersonActivity.this, "投票保存成功", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_CODE);
                    finish();
                } else {
                    Toast.makeText(VotePersonActivity.this, response.optString(Constant.TIPMESSAGE), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(VotePersonActivity.this, R.string.exception_prompt, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
