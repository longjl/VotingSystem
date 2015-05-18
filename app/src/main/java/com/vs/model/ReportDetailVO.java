package com.vs.model;

import java.util.List;

/**
 * 报表信息信息
 * Created by longjianlin on 15/3/27.
 */
public class ReportDetailVO {
    public String reportDetailId;//投票子项主键

    public String titleFirst;//区域一标题
    public String detailFirst;//区域一内容

    public String titleSecond;//区域二标题
    public String detailSecond;//区域二内容

    public String titleVote;//投票项标题
    public int elementType;//投票项类型 1、文本 2、单选框 3、复选框  4、带有输入框补充的复选框

    public String extraResult;//当输入项为带有扩展的多选框时，此字段用于保存补充的信息，即手动输入内容

    public String personTitle;//干部信息
    public List<InputTemplateVO> inputTemplateVOList;//投票项内容,只有当elementType=2时使用
}
