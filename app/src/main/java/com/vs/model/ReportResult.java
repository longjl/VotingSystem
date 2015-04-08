package com.vs.model;

/**
 * Created by longjianlin on 15/3/27.
 */
public class ReportResult {
    public int _id;//主键
    public String medicalRegInfoId;//执业机构主键
    public String pingjiarenLinshiDengluma;//评价人临时登录码
    public String lingdaoGanbuId;//人员主键，若reportType为1或4时为空
    public String extraResult;//当投票元素为4（带有输入框扩展的多选框）时，此为输入框内容
    public String reportDetailId;//投票子项主键
    public String reportBaseId;//投票报表主键
    public String voteMeetingId;//投票会议主键
    public String reportResult;//投票结果(选项值)
    public int radioId;//选项id
    public String inputDisplay;//投票显示的值
    public String detailFirst;//区域1内容
    public String createDate;//投票时间
}
