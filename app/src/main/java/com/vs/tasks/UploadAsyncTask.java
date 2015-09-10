package com.vs.tasks;


import android.content.Context;
import android.os.AsyncTask;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.vs.Constant;
import com.vs.dao.ReportDao;
import com.vs.model.ReportResult;
import com.vs.network.VSClient;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by longjianlin on 15/8/11.
 */
public class UploadAsyncTask extends AsyncTask<Void, Void, Void> {

    private ReportDao dao;

    public static void execute(Context context) {
        new UploadAsyncTask(context).execute();
    }

    private UploadAsyncTask(Context context) {
        dao = new ReportDao(context);
    }

    @Override
    protected Void doInBackground(Void... params) {
        //获取所有的登录码
        List<String> dengluma_list = dao.findDenglumas();
        if (dengluma_list != null && dengluma_list.size() > 0) {

        }


        return null;
    }



    @Override
    protected void onPostExecute(Void bool) {
        super.onPostExecute(bool);

    }
}
