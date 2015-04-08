package com.vs.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vs.Constant;
import com.vs.R;
import com.vs.adapters.VotePersonAdapter;
import com.vs.dao.ReportDao;
import com.vs.model.Person;
import com.vs.network.VSClient;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longjianlin on 15/4/5.
 */
public class PersonActivity extends BaseActivity implements AdapterView.OnItemClickListener, View.OnClickListener {
    private GridView grid_person;
    private List<Person> persons = new ArrayList<Person>();
    private VotePersonAdapter adapter;
    private Button btn_cancel;

    private int reportType = 0;//报表类型
    private String reportBaseId;//报表编号

    private ReportDao dao;
    private static final int REQUEST_CODE = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(true);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        dao = new ReportDao(this);

        grid_person = (GridView) findViewById(R.id.grid_person);
        grid_person.setOnItemClickListener(this);

        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(this);

        adapter = new VotePersonAdapter(this, persons);
        grid_person.setAdapter(adapter);
        reportType = getIntent().getIntExtra("reportType", 0);
        reportBaseId = getIntent().getStringExtra("reportBaseId");
        mobile_queryAllVotePerson(reportBaseId);
    }


    /**
     * 投票人员
     */
    private void mobile_queryAllVotePerson(final String reportBaseId) {
        String url = Constant.BASE_HTTP + "/tpms/mobile_queryAllVotePerson.mobile";
        RequestParams params = new RequestParams();
        params.put("linshiDengluma", app.temp.linshiDengluma);//临时登陆码
        params.put("medicalRegInfoId", app.temp.medicalRegInfoId);//执业机构主键
        params.put("voteMeetingId", app.temp.voteMeetingId);//投票会议主键
        params.put("reportBaseId", reportBaseId);//报表编号
        VSClient.get(url, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("mobile_queryAllVotePerson - >", response.toString());
                if (response.optInt(Constant.STATUS) == 1) {
                    persons.clear();
                    JSONArray jsonArray = response.optJSONArray(Constant.VO);
                    if (null != jsonArray && jsonArray.length() > 0) {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.optJSONObject(i);
                            Person person = new Person();
                            person.lingdaoGanbuId = object.optString("lingdaoGanbuId");
                            person.name = object.optString("name");
                            person.birthday = object.optString("birthday");
                            person.sex = object.optString("sex");
                            person.xianrenzhiwu = object.optString("xianrenzhiwu");
                            person.yuanrenZhiwu = object.optString("yuanrenZhiwu");
                            person.renzhishijian = object.optString("renzhishijian");

                            person.progress = dao.queryPersonProgress(app.temp.medicalRegInfoId, app.temp.linshiDengluma, reportBaseId, app.temp.voteMeetingId, person.lingdaoGanbuId);
                            persons.add(person);
                        }
                    }
                } else {
                    Toast.makeText(PersonActivity.this, response.optString(Constant.TIPMESSAGE), Toast.LENGTH_SHORT).show();
                }
                handler.sendEmptyMessage(1);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Toast.makeText(PersonActivity.this, R.string.exception_prompt, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    adapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Person person = adapter.getItem(position);
        if (reportType > 0 && reportBaseId != null) {
            Intent intent = new Intent(PersonActivity.this, VotePersonActivity.class);
            intent.putExtra("reportBaseId", reportBaseId);
            intent.putExtra("reportType", reportType);

            intent.putExtra("lingdaoGanbuId", person.lingdaoGanbuId);
            intent.putExtra("renzhishijian", person.renzhishijian);
            intent.putExtra("birthday", person.birthday);
            intent.putExtra("sex", person.sex);
            intent.putExtra("yuanrenZhiwu", person.yuanrenZhiwu);
            intent.putExtra("name", person.name);
            intent.putExtra("xianrenzhiwu", person.xianrenzhiwu);
            startActivityForResult(intent, REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == REQUEST_CODE) {
            dao.updateProgress(app.temp.medicalRegInfoId, app.temp.linshiDengluma, reportBaseId, app.temp.voteMeetingId);
            mobile_queryAllVotePerson(reportBaseId);
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_cancel == v.getId()) {
            Intent intent = new Intent(PersonActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
