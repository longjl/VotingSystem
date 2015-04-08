package com.vs.model;

import java.util.ArrayList;
import java.util.List;

/*
 * 报表
 *
 描述:此接口获取的数据用于显示所有可以进行投票的报表。
  当点击投票报表时，需要根据reportType进行跳转判断，
  当reportType为1或4时，点击报表时直接跳转到投票页面，
  即直接调用投票接口（接口4）；当reportType为2或3时，
  需要跳转到投票人员接口（接口5）。
  voteCount用以记录该投票报表共有多少个子项，
  当reportType为1或4时恒为1，
  当reportType为2或3时，
  则为需要进行被投票的人员总数。

 * Created by longjianlin on 15/3/25.
 */
public class ReportBaseVO {
    public int reportType;//投票报表类型 1、针对组织机构评价  2、针对领导干部评价  3、针对新选拔干部评价 4、第四类报表
    public int reportSequence;//排序字段，数据都是按照此字段进行排序
    public String reportBaseId;//投票报表Id
    public String reportName;//报表名称
    public int voteCount;//投票项总数
    public int progress;//投票完成度
    public List<ReportDetailVO> reportDetailVOList = new ArrayList<ReportDetailVO>();//评价项详细信息
}
