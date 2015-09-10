package com.vs.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.vs.model.Person;
import com.vs.model.Progress;
import com.vs.model.ReportResult;
import com.vs.model.Temp;
import com.vs.util.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by longjianlin on 15/3/31.
 */
public class ReportDao {
    private SQLiteDatabase db;

    public ReportDao(Context context) {
        db = DatabaseHelper.getInstance(context);
    }

    /*
     buffer.append("_id int primary key,");//主键
        buffer.append("medicalRegInfoId text,");//执业机构主键
        buffer.append("pingjiarenLinshiDengluma text,");//评价人临时登录码
        buffer.append("lingdaoGanbuId text,");//人员主键，若reportType为1或4时为空
        buffer.append("extraResult text,");//当投票元素为4（带有输入框扩展的多选框）时，此为输入框内容
        buffer.append("reportDetailId text,");//投票子项主键
        buffer.append("reportBaseId text,");//投票报表主键
        buffer.append("voteMeetingId text,");//投票会议主键
        buffer.append("reportResult text,");//(投票值)
        buffer.append("radioId int,");//投票选项编号
        buffer.append("createDate text)");//创建时间
     */

    /**
     * 添加数据
     */
    public void insert(ReportResult reportResult) {
        db.execSQL("insert into report_result values(null,?,?,?,?,?,?,?,?,?,?,?,?)",
                new String[]{
                        reportResult.medicalRegInfoId,//执业机构主键
                        reportResult.pingjiarenLinshiDengluma,//评价人临时登录码
                        reportResult.lingdaoGanbuId,//人员主键，若reportType为1或4时为空
                        reportResult.extraResult,//当投票元素为4（带有输入框扩展的多选框）时，此为输入框内容
                        reportResult.reportDetailId,//投票子项主键
                        reportResult.reportBaseId,//投票报表主键
                        reportResult.voteMeetingId,//投票会议主键
                        reportResult.reportResult,//(投票值)
                        reportResult.radioId + "",//投票选项编号
                        reportResult.inputDisplay,//投票显示的值
                        reportResult.detailFirst,//区域1显示的内容
                        reportResult.createDate,//创建时间
                });
    }

    /**
     * 查询 选项结果
     *
     * @return
     */
    public ReportResult findReportResult(ReportResult reportResult) {
        StringBuffer buffer = new StringBuffer("select r.reportResult,r.radioId from report_result  r where ");
        buffer.append(" r.medicalRegInfoId=? ");
        buffer.append(" and r.pingjiarenLinshiDengluma=? ");
        buffer.append(" and r.reportBaseId=? ");
        buffer.append(" and r.reportDetailId=? ");
        buffer.append(" and r.voteMeetingId=? ");
        if (reportResult.lingdaoGanbuId != null) {
            buffer.append(" and r.lingdaoGanbuId= '" + reportResult.lingdaoGanbuId + "'");
        }
        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{reportResult.medicalRegInfoId,
                        reportResult.pingjiarenLinshiDengluma,
                        reportResult.reportBaseId,
                        reportResult.reportDetailId,
                        reportResult.voteMeetingId});

        ReportResult result = new ReportResult();
        if (null != cursor) {
            while (cursor.moveToNext()) {
                result.reportResult = cursor.getString(cursor.getColumnIndex("reportResult"));
                result.radioId = cursor.getInt(cursor.getColumnIndex("radioId"));
                break;
            }
        }
        return result;
    }

    /**
     * 查询 某一个周报所对应的选项
     *
     * @param reportResult
     * @return
     */
    public List<ReportResult> findReportResults(ReportResult reportResult, int topCount) {
        List<ReportResult> reportResults = new ArrayList<ReportResult>();
        StringBuffer buffer = new StringBuffer("select r.reportResult,r.radioId,r.inputDisplay,r.detailFirst from report_result  r where ");
        buffer.append(" r.medicalRegInfoId=? ");
        buffer.append(" and r.pingjiarenLinshiDengluma=? ");
        buffer.append(" and r.reportBaseId=? ");
        buffer.append(" and r.voteMeetingId=?  ");
        if (reportResult.lingdaoGanbuId != null) {
            buffer.append(" and r.lingdaoGanbuId= '" + reportResult.lingdaoGanbuId + "'");
        }
        buffer.append(" limit " + topCount);

        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{reportResult.medicalRegInfoId,
                        reportResult.pingjiarenLinshiDengluma,
                        reportResult.reportBaseId,
                        reportResult.voteMeetingId});
        if (null != cursor) {
            while (cursor.moveToNext()) {
                ReportResult result = new ReportResult();
                result.reportResult = cursor.getString(cursor.getColumnIndex("reportResult"));
                result.radioId = cursor.getInt(cursor.getColumnIndex("radioId"));
                result.detailFirst = cursor.getString(cursor.getColumnIndex("detailFirst"));
                result.inputDisplay = cursor.getString(cursor.getColumnIndex("inputDisplay"));
                reportResults.add(result);
            }
        }
        return reportResults;
    }

    /**
     * 修改数据
     *
     * @param reportResult
     */
    public void updateOrSaveReportResult(ReportResult reportResult) {
        ReportResult result = findReportResult(reportResult);
        if (null == result.reportResult) {
            insert(reportResult);
        } else {
            update(reportResult);
        }
    }

    /**
     * 更新数据
     *
     * @param reportResult
     */
    public void update(ReportResult reportResult) {
        StringBuffer sql = new StringBuffer("update report_result  set reportResult=?,radioId=?,inputDisplay=? ");

//        if (reportResult.extraResult != null) {
//            sql.append(" ,extraResult ='" + reportResult.extraResult + "'");
//        }
        sql.append(" where medicalRegInfoId=? ");
        sql.append(" and pingjiarenLinshiDengluma=? and reportBaseId=? and reportDetailId=? ");
        sql.append(" and voteMeetingId=? ");
        if (reportResult.lingdaoGanbuId != null) {
            sql.append(" and lingdaoGanbuId='" + reportResult.lingdaoGanbuId + "' ");
        }

        db.execSQL(sql.toString(),
                new Object[]{
                        reportResult.reportResult,
                        reportResult.radioId,
                        reportResult.inputDisplay,
                        reportResult.medicalRegInfoId,
                        reportResult.pingjiarenLinshiDengluma,
                        reportResult.reportBaseId,
                        reportResult.reportDetailId,
                        reportResult.voteMeetingId
                });
    }

    /**
     * 查询投票数据 （用于上传至服务器）
     *
     * @param reportResult
     * @return Cursor
     */
    public List<ReportResult> findAll(ReportResult reportResult) {
        StringBuffer buffer = new StringBuffer("select voteMeetingId,medicalRegInfoId,reportBaseId,pingjiarenLinshiDengluma,reportDetailId,reportResult,lingdaoGanbuId,createDate ");
        buffer.append(" from report_result where ");
        buffer.append(" medicalRegInfoId=? ");
        buffer.append(" and pingjiarenLinshiDengluma=? ");
        buffer.append(" and reportBaseId=? ");
        buffer.append(" and voteMeetingId=? ");
        if (reportResult.lingdaoGanbuId != null) {
            buffer.append(" and lingdaoGanbuId='" + reportResult.lingdaoGanbuId + "' ");
        }
        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{
                        reportResult.medicalRegInfoId,
                        reportResult.pingjiarenLinshiDengluma,
                        reportResult.reportBaseId,
                        reportResult.voteMeetingId
                });

        if (null == cursor) return null;

        List<ReportResult> reportResultList = new ArrayList<ReportResult>();
        while (cursor.moveToNext()) {
            ReportResult result = new ReportResult();
            result.voteMeetingId = cursor.getString(cursor.getColumnIndex("voteMeetingId"));
            result.medicalRegInfoId = cursor.getString(cursor.getColumnIndex("medicalRegInfoId"));
            result.reportBaseId = cursor.getString(cursor.getColumnIndex("reportBaseId"));
            result.pingjiarenLinshiDengluma = cursor.getString(cursor.getColumnIndex("pingjiarenLinshiDengluma"));
            result.reportDetailId = cursor.getString(cursor.getColumnIndex("reportDetailId"));
            result.lingdaoGanbuId = cursor.getString(cursor.getColumnIndex("lingdaoGanbuId"));
            result.reportResult = cursor.getString(cursor.getColumnIndex("reportResult"));
            result.createDate = cursor.getString(cursor.getColumnIndex("createDate"));
            reportResultList.add(result);
        }
        return reportResultList;
    }

    public List<ReportResult> findAll(Temp temp) {
        StringBuffer buffer = new StringBuffer("select voteMeetingId,medicalRegInfoId,reportBaseId,pingjiarenLinshiDengluma,reportDetailId,reportResult,lingdaoGanbuId ");
        buffer.append(" from report_result where ");
        buffer.append(" medicalRegInfoId=? ");
        buffer.append(" and pingjiarenLinshiDengluma=? ");
        buffer.append(" and voteMeetingId=? ");
        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{
                        temp.medicalRegInfoId,
                        temp.linshiDengluma,
                        temp.voteMeetingId
                });

        if (null == cursor) return null;

        List<ReportResult> reportResultList = new ArrayList<ReportResult>();
        while (cursor.moveToNext()) {
            ReportResult result = new ReportResult();
            result.voteMeetingId = cursor.getString(cursor.getColumnIndex("voteMeetingId"));
            result.medicalRegInfoId = cursor.getString(cursor.getColumnIndex("medicalRegInfoId"));
            result.reportBaseId = cursor.getString(cursor.getColumnIndex("reportBaseId"));
            result.pingjiarenLinshiDengluma = cursor.getString(cursor.getColumnIndex("pingjiarenLinshiDengluma"));
            result.reportDetailId = cursor.getString(cursor.getColumnIndex("reportDetailId"));
            result.lingdaoGanbuId = cursor.getString(cursor.getColumnIndex("lingdaoGanbuId"));
            result.reportResult = cursor.getString(cursor.getColumnIndex("reportResult"));
            reportResultList.add(result);
        }
        return reportResultList;
    }

    /**
     * 根据临时登录码查询
     *
     * @param linshiDengluma
     * @return
     */
    public List<ReportResult> findAllByLinshiDengluma(String linshiDengluma) {
        StringBuffer buffer = new StringBuffer("select voteMeetingId,medicalRegInfoId,reportBaseId,pingjiarenLinshiDengluma,reportDetailId,reportResult,lingdaoGanbuId,createDate ");
        buffer.append(" from report_result where pingjiarenLinshiDengluma=?");
        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{linshiDengluma});

        if (null == cursor) return null;

        List<ReportResult> reportResultList = new ArrayList<ReportResult>();
        while (cursor.moveToNext()) {
            ReportResult result = new ReportResult();
            result.voteMeetingId = cursor.getString(cursor.getColumnIndex("voteMeetingId"));
            result.medicalRegInfoId = cursor.getString(cursor.getColumnIndex("medicalRegInfoId"));
            result.reportBaseId = cursor.getString(cursor.getColumnIndex("reportBaseId"));
            result.pingjiarenLinshiDengluma = cursor.getString(cursor.getColumnIndex("pingjiarenLinshiDengluma"));
            result.reportDetailId = cursor.getString(cursor.getColumnIndex("reportDetailId"));
            result.lingdaoGanbuId = cursor.getString(cursor.getColumnIndex("lingdaoGanbuId"));
            result.reportResult = cursor.getString(cursor.getColumnIndex("reportResult"));
            result.createDate = cursor.getString(cursor.getColumnIndex("createDate"));
            reportResultList.add(result);
        }
        return reportResultList;
    }



    /* ------------------更改投票状态------------------*/

    /**
     * 保存进度
     *
     * @param medicalRegInfoId
     * @param pingjiarenLinshiDengluma
     * @param reportBaseId
     * @param voteMeetingId
     * @param voteCount
     * @param progress
     */
    public void saveProgress(String medicalRegInfoId, String pingjiarenLinshiDengluma, String reportBaseId, String voteMeetingId, int voteCount, int progress) {
        db.execSQL("insert into vote_progress values(null,?,?,?,?,?,?)",
                new Object[]{
                        medicalRegInfoId,//执业机构主键
                        pingjiarenLinshiDengluma,//评价人临时登录码
                        reportBaseId,//投票报表主键
                        voteMeetingId,//投票会议主键
                        voteCount,//投票子项主键
                        progress//完成进度
                });
    }

    /**
     * 更新进度
     *
     * @param medicalRegInfoId
     * @param pingjiarenLinshiDengluma
     * @param reportBaseId
     * @param voteMeetingId
     */
    public void updateProgress(String medicalRegInfoId, String pingjiarenLinshiDengluma, String reportBaseId, String voteMeetingId) {
        db.execSQL("update vote_progress set progress = progress + 1 where medicalRegInfoId=? " +
                        " and pingjiarenLinshiDengluma=? and reportBaseId=? and voteMeetingId=? " +
                        " and (voteCount > progress)",
                new Object[]{
                        medicalRegInfoId,
                        pingjiarenLinshiDengluma,
                        reportBaseId,
                        voteMeetingId
                });
    }

    /**
     * 查看进度
     *
     * @param medicalRegInfoId
     * @param pingjiarenLinshiDengluma
     * @param reportBaseId
     * @param voteMeetingId
     */
    public Progress queryProgress(String medicalRegInfoId, String pingjiarenLinshiDengluma, String reportBaseId, String voteMeetingId) {
        StringBuffer buffer = new StringBuffer("select progress,reportBaseId from vote_progress ");
        buffer.append(" where medicalRegInfoId=?");
        buffer.append(" and pingjiarenLinshiDengluma=?");
        buffer.append(" and reportBaseId=?");
        buffer.append(" and voteMeetingId=?");

        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{
                        medicalRegInfoId,
                        pingjiarenLinshiDengluma,
                        reportBaseId,
                        voteMeetingId
                });

        Progress progress = new Progress();
        if (null != cursor) {
            while (cursor.moveToNext()) {
                progress.progress = cursor.getInt(cursor.getColumnIndex("progress"));
                progress.reportBaseId = cursor.getString(cursor.getColumnIndex("reportBaseId"));
                break;
            }
        } else {
            progress.progress = 0;
            progress.reportBaseId = null;
        }
        return progress;
    }

    /**
     * 根据登录码查询进度
     *
     * @param linshiDengluma
     * @return
     */
    public List<Progress> queryProgressByLinshiDengluma(String linshiDengluma) {
        StringBuffer buffer = new StringBuffer("select progress,voteCount from vote_progress ");
        buffer.append(" where pingjiarenLinshiDengluma=?");

        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{
                        linshiDengluma
                });
        List<Progress> progressList = new ArrayList<Progress>();
        if (null != cursor) {
            while (cursor.moveToNext()) {
                Progress progress = new Progress();
                progress.progress = cursor.getInt(cursor.getColumnIndex("progress"));
                progress.voteCount = cursor.getInt(cursor.getColumnIndex("voteCount"));
                progressList.add(progress);
            }
        }
        return progressList;
    }


    /**
     * 判断投票是否完成
     *
     * @param medicalRegInfoId
     * @param pingjiarenLinshiDengluma
     * @param reportBaseId
     * @param voteMeetingId
     * @return
     */
    public int voteComplete(String medicalRegInfoId, String pingjiarenLinshiDengluma, String reportBaseId, String voteMeetingId) {
        StringBuffer buffer = new StringBuffer("select progress,voteCount from vote_progress ");
        buffer.append(" where medicalRegInfoId=?");
        buffer.append(" and pingjiarenLinshiDengluma=?");
        buffer.append(" and reportBaseId=?");
        buffer.append(" and voteMeetingId=? and (progress = voteCount)");
        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{
                        medicalRegInfoId,
                        pingjiarenLinshiDengluma,
                        reportBaseId,
                        voteMeetingId
                });

        return cursor.getCount();
    }


    /**
     * *******************************************************
     * person_progress  人员进度
     * *******************************************************
     */
    /**
     * 保存进度
     *
     * @param medicalRegInfoId
     * @param pingjiarenLinshiDengluma
     * @param reportBaseId
     * @param voteMeetingId
     * @param lingdaoGanbuId
     */
    public void savePersonProgress(String medicalRegInfoId, String pingjiarenLinshiDengluma, String reportBaseId, String voteMeetingId, String lingdaoGanbuId) {
        db.execSQL("insert into person_progress values(null,?,?,?,?,?)",
                new Object[]{
                        medicalRegInfoId,//执业机构主键
                        pingjiarenLinshiDengluma,//评价人临时登录码
                        reportBaseId,//投票报表主键
                        voteMeetingId,//投票会议主键
                        lingdaoGanbuId//人员主键
                });
    }


    /**
     * 查询人员投票进度
     *
     * @param medicalRegInfoId
     * @param pingjiarenLinshiDengluma
     * @param reportBaseId
     * @param voteMeetingId
     */
    public int queryPersonProgress(String medicalRegInfoId, String pingjiarenLinshiDengluma, String reportBaseId, String voteMeetingId, String lingdaoGanbuId) {
        StringBuffer buffer = new StringBuffer("select voteMeetingId from person_progress ");
        buffer.append(" where medicalRegInfoId=?");
        buffer.append(" and pingjiarenLinshiDengluma=?");
        buffer.append(" and reportBaseId=?");
        buffer.append(" and voteMeetingId=?");
        buffer.append(" and lingdaoGanbuId=?");
        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{
                        medicalRegInfoId,
                        pingjiarenLinshiDengluma,
                        reportBaseId,
                        voteMeetingId,
                        lingdaoGanbuId
                });
        return cursor.getCount();
    }

    //**********************dengluma 表**********************

    /**
     * 根据登录码查询数量
     */
    public int findDenglumaCount(final String pingjiarenLinshiDengluma) {
        StringBuffer buffer = new StringBuffer("select pingjiarenLinshiDengluma from dengluma ");
        buffer.append(" where pingjiarenLinshiDengluma=?");

        Cursor cursor = db.rawQuery(buffer.toString(),
                new String[]{
                        pingjiarenLinshiDengluma
                });
        return cursor.getCount();
    }

    /**
     * 将登录码存入数据库中
     *
     * @param pingjiarenLinshiDengluma
     */
    public void saveDengluma(final String pingjiarenLinshiDengluma) {
        if (findDenglumaCount(pingjiarenLinshiDengluma) <= 0) {
            db.execSQL("insert into dengluma values(null,?)", new String[]{pingjiarenLinshiDengluma});
        }
    }

    /**
     * 查询所有的登录码
     */
    public List<String> findDenglumas() {
        Cursor cursor = db.rawQuery("select pingjiarenLinshiDengluma from dengluma", null);
        List<String> list = null;
        if (null != cursor) {
            list = new ArrayList<String>();
            while (cursor.moveToNext()) {
                list.add(cursor.getString(cursor.getColumnIndex("pingjiarenLinshiDengluma")));
            }
        }
        return list;
    }

    /**
     * 删除登录码
     *
     * @param pingjiarenLinshiDengluma
     */
    public void deleteDengluma(final String pingjiarenLinshiDengluma) {
        db.delete("dengluma", "pingjiarenLinshiDengluma=?", new String[]{pingjiarenLinshiDengluma});
    }
}
