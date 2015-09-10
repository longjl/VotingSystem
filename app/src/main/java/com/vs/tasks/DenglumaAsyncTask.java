package com.vs.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.vs.VSApplication;
import com.vs.dao.ReportDao;

import java.util.Set;


/**
 * Created by longjianlin on 15/6/2.
 */
public class DenglumaAsyncTask extends AsyncTask<Void, Void, Boolean> {


    private Context mContext;
    private String mDengluma;
    private ReportDao dao;

    public static void execute(Context context, String dengluma) {
        new DenglumaAsyncTask(context, dengluma).execute();
    }

    private DenglumaAsyncTask(Context context, String dengluma) {
        mContext = context;
        mDengluma = dengluma;
        dao = new ReportDao(context);
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (mDengluma != null && mDengluma.length() > 0) {
            dao.deleteDengluma(mDengluma);
        }
        return true;
    }

    @Override
    protected void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);

    }

}