package com.vs.util;

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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by longjianlin on 15/6/2.
 */
public class RWData {
    private static final String BASE_PATH = "/sdcard/vs/";

    /**
     * 保存zip文件
     *
     * @param bytes
     */
    public static void saveZip(byte[] bytes, Temp temp) {
        String fileName = temp.linshiDengluma + "_" + temp.voteMeetingId + "_" + temp.medicalRegInfoId + ".zip";
        File file = new File(BASE_PATH + temp.linshiDengluma + "/" + fileName);//文件存储目录
        File dir = new File(BASE_PATH + temp.linshiDengluma);
        try {
            if (!dir.isDirectory()) {
                dir.mkdirs();
            }
            if (file.exists()) {//如果已经存在改zip文件，那么就先删除
                file.delete();
            }
            FileOutputStream outStream = new FileOutputStream(file);
            outStream.write(bytes);
            outStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        unzip(temp);
    }

    /**
     * 解压zip文件
     */
    public static void unzip(Temp temp) {
        String fileName = temp.linshiDengluma + "_" + temp.voteMeetingId + "_" + temp.medicalRegInfoId + ".zip";
        File file = new File(BASE_PATH + temp.linshiDengluma + "/" + fileName);//文件存储目录
        String zipFile = BASE_PATH + temp.linshiDengluma + "/" + fileName;
        String location = BASE_PATH + temp.linshiDengluma + "/";//原始目录
        try {
            if (!file.exists()) {//如果zip文件不存在就直接返回
                return;
            }
            FileInputStream fin = new FileInputStream(zipFile);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());
                if (!ze.isDirectory()) {
                    FileOutputStream fout = new FileOutputStream(location + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }
                    zin.closeEntry();
                    fout.close();
                }
            }
            zin.close();
        } catch (Exception e) {
            Log.e("Decompress", "unzip", e);
        }
        if (file.exists()) {//如果zip文件存在就直接删除
            file.delete();
        }
    }



    /**
     * 读取JsonObject
     *
     * @param filePath
     * @return
     */
    public static JSONObject loadJsonObject(String filePath) {
        JSONObject jsonObject = null;
        File file = new File(filePath);
        if (!file.exists()) {
            return jsonObject;
        }
        try {
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }
}
