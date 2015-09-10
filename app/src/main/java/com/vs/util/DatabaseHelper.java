package com.vs.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLiteOpenHelper是一个辅助类，用来管理数据库的创建和版本他，它提供两个方面的功能
 * 第一，getReadableDatabase()、getWritableDatabase()可以获得SQLiteDatabase对象，通过该对象可以对数据库进行操作
 * 第二，提供了onCreate()、onUpgrade()两个回调函数，允许我们再创建和升级数据库时，进行自己的操作
 * Created by longjianlin on 15/3/31.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "/sdcard/vs/voting_system_db";//数据库名称

    private static DatabaseHelper helper;

    /**
     * 在SQLiteOpenHelper的子类当中，必须有该构造函数
     *
     * @param context 上下文对象
     * @param name    数据库名称
     * @param factory
     * @param version 当前数据库的版本，值必须是整数并且是递增的状态
     */
    public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                          int version) {
        //必须通过super调用父类当中的构造函数
        super(context, name, factory, version);
    }

    public static SQLiteDatabase getInstance(Context context) {
        if (helper == null) {
            // 指定数据库名为student，需修改时在此修改；此处使用默认工厂；指定版本为1
            helper = new DatabaseHelper(context, DATABASE_NAME, null, VERSION);
        }
        return helper.getWritableDatabase();
    }

    //该函数是在第一次创建的时候执行，实际上是第一次得到SQLiteDatabase对象的时候才会调用这个方法
    @Override
    public void onCreate(SQLiteDatabase db) {
        //execSQL用于执行SQL语句
        StringBuffer buffer = new StringBuffer("create table if not exists report_result(");
        buffer.append("_id INTEGER PRIMARY KEY,");//主键
        buffer.append("medicalRegInfoId text,");//执业机构主键
        buffer.append("pingjiarenLinshiDengluma text,");//评价人临时登录码
        buffer.append("lingdaoGanbuId text,");//人员主键，若reportType为1或4时为空
        buffer.append("extraResult text,");//当投票元素为4（带有输入框扩展的多选框）时，此为输入框内容
        buffer.append("reportDetailId text,");//投票子项主键
        buffer.append("reportBaseId text,");//投票报表主键
        buffer.append("voteMeetingId text,");//投票会议主键
        buffer.append("reportResult text,");//(投票值)(用于上传值服务器)
        buffer.append("radioId int,");//投票选项编号
        buffer.append("inputDisplay text,");//投票显示的值
        buffer.append("detailFirst text,");//区域1内容
        buffer.append("createDate text)");//创建时间
        db.execSQL(buffer.toString());

        //报表投票进度表
        StringBuffer vote_progress = new StringBuffer("create table if not exists vote_progress(");
        vote_progress.append("_id INTEGER PRIMARY KEY,");//主键
        vote_progress.append("medicalRegInfoId text,");//执业机构主键
        vote_progress.append("pingjiarenLinshiDengluma text,");//评价人临时登录码
        vote_progress.append("reportBaseId text,");//投票报表主键
        vote_progress.append("voteMeetingId text,");//投票会议主键
        vote_progress.append("voteCount int,");//投票数
        vote_progress.append("progress int)");//完成进度
        db.execSQL(vote_progress.toString());

        //人员投票进度表
        StringBuffer person_progress = new StringBuffer("create table if not exists person_progress(");
        person_progress.append("_id INTEGER PRIMARY KEY,");//主键
        person_progress.append("medicalRegInfoId text,");//执业机构主键
        person_progress.append("pingjiarenLinshiDengluma text,");//评价人临时登录码
        person_progress.append("reportBaseId text,");//投票报表主键
        person_progress.append("voteMeetingId text,");//投票会议主键
        person_progress.append("lingdaoGanbuId text)");//人员主键，若reportType为1或4时为空
        db.execSQL(person_progress.toString());

        //报表表
        /*StringBuffer report = new StringBuffer("create table if not exists report(");
        report.append("_id INTEGER PRIMARY KEY,");//主键
        report.append("reportType int,");//投票报表类型 1、针对组织机构评价  2、针对领导干部评价  3、针对新选拔干部评价 4、第四类报表
        report.append("reportBaseId text,");//报表id
        report.append("reportName text,");//报表名字
        report.append("voteCount int)");//报表选项数量
        db.execSQL(report.toString());*/

        StringBuffer dengluma_sql = new StringBuffer("create table if not exists dengluma(");
        dengluma_sql.append("_id INTEGER PRIMARY KEY,");//主键
        dengluma_sql.append("pingjiarenLinshiDengluma text)");//评价人临时登录码
        db.execSQL(dengluma_sql.toString());
    }

    @Override
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
        // TODO Auto-generated method stub
        System.out.println("upgrade a database");
    }
}
