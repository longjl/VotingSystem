package com.vs.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.vs.model.Temp;
import com.vs.util.RWData;


/**
 * Created by longjianlin on 15/6/2.
 */
public class DataHandlerAsyncTask extends AsyncTask<Void, Void, Boolean> {

    public interface ResultListener {
        public void onLoaded();
    }


    private ResultListener mListener;
    private byte[] mByes;
    private Temp mTemp;

    public static void execute(ResultListener listener, byte[] bytes, Temp temp) {
        new DataHandlerAsyncTask(listener, bytes, temp).execute();
    }

    private DataHandlerAsyncTask(ResultListener listener, byte[] bytes, Temp temp) {
        mListener = listener;
        mByes = bytes;
        mTemp = temp;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        RWData.saveZip(mByes, mTemp);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean bool) {
        super.onPostExecute(bool);
        if (null != mListener) {
            mListener.onLoaded();
        }
    }


}