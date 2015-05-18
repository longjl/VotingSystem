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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by longjianlin on 15/3/26.
 */
public class VSApplication extends Application {
    public List<Activity> activities = new ArrayList<Activity>();
    private static final String TAG = "VSApplication";
    public String macAddress;//mac 地址
    public Temp temp = new Temp();
    private static SharedPreferences mUrlPrefs;//保存服务器地址
    public static SharedPreferences mLinshiDenglumaPrefs;//登录码

    public static VSApplication getApplication(Context context) {
        return (VSApplication) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mUrlPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mLinshiDenglumaPrefs = PreferenceManager.getDefaultSharedPreferences(this);
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
            if (file.exists()) {
                file.delete();
            }
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
     * 读取JsonObject
     *
     * @param key
     * @return
     */
    public JSONObject loadJsonObject(String key) {
        JSONObject jsonObject = null;
        String fileName = "/sdcard/vs/" + key + ".json";
        File file = new File(fileName);
        try {
            if (file.exists()) {
                FileInputStream inStream = new FileInputStream(file);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length = -1;
                while ((length = inStream.read(buffer)) != -1) {
                    stream.write(buffer, 0, length);
                }
                stream.close();
                inStream.close();
                jsonObject = new JSONObject(stream.toString());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
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
