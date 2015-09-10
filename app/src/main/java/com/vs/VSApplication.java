package com.vs;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.util.Log;

import com.vs.model.Temp;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by longjianlin on 15/3/26.
 */
public class VSApplication extends Application {
    public List<Activity> activities = new ArrayList<Activity>();
    private static final String TAG = "VSApplication";
    public String macAddress;//mac 地址
    public Temp temp = new Temp();
    public static SharedPreferences mUrlPrefs;//保存服务器地址
    public static SharedPreferences mLinshiDenglumaPrefs;//登录码
    public static SharedPreferences sp;//存储临时信息

    public    int selectionIndex = 0;

    public static VSApplication getApplication(Context context) {
        return (VSApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mUrlPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mLinshiDenglumaPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        getMacAddress();
    }

    /**
     * 获取Mac地址
     */
    public void getMacAddress() {
        if (macAddress == null) {
            // 获取wifi管理器
            WifiManager wifiMng = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMng.getConnectionInfo();
            macAddress = wifiInfo.getMacAddress();
            Log.i(TAG, "--- DVB Mac Address : " + macAddress);
        }
    }

    /**
     * 退出
     */
    public void exit() {
        if (activities != null && activities.size() > 0) {
            for (Activity activity : activities) activity.finish();
        }
        System.exit(0);
    }

    public void setPreKey(String key, String value) {
        if (sp != null) {
            sp.edit().putString(key, value).commit();
        }
    }

    public String getPreValue(String key) {
        if (sp != null) {
            return sp.getString(key, null);
        }
        return null;
    }


    /**
     * 服务器URL
     *
     * @param server_url
     */
    public void setServerUrlToPrefs(final String server_url) {
        if (null != mUrlPrefs) {
            mUrlPrefs.edit().putString("server_url", server_url).commit();
        }
    }

    /**
     * 服务器URL
     */
    public String getServerUrlToPrefs() {
        if (null != mUrlPrefs) {
            return mUrlPrefs.getString("server_url", null);
        }
        return null;
    }


    /**
     * 登录码
     *
     * @param linshiDengluma
     */
    public void setLinshiDenglumaToPrefs(final String linshiDengluma) {
        if (null != mLinshiDenglumaPrefs) {
            mLinshiDenglumaPrefs.edit().putString("linshiDengluma", linshiDengluma).commit();
        }
    }

    /**
     * 获取
     * linshiDengluma
     */
    public String getLinshiDenglumaToPrefs() {
        if (null != mLinshiDenglumaPrefs) {
            return mLinshiDenglumaPrefs.getString("linshiDengluma", null);
        }
        return null;
    }


    /**
     * @param jsonObject
     * @param key
     */
    public void saveJsonObject(JSONObject jsonObject, String key) {
        try {
            String fileName = "/sdcard/vs/" + key + ".json";//使用登录码作为文件的名字
            File file = new File(fileName);
            if (!file.exists()) {
                FileOutputStream outStream = new FileOutputStream(file);
                outStream.write(jsonObject.toString().getBytes());
                outStream.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 判断网络是否存在
     *
     * @param context
     * @return
     */
    public boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }
}
