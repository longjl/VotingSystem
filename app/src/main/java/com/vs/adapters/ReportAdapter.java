package com.vs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vs.R;
import com.vs.model.ReportBaseVO;

import java.util.ArrayList;

/**
 * Created by longjianlin on 15/3/26.
 */
public class ReportAdapter extends BaseAdapter {
    private ArrayList<ReportBaseVO> reports;//报表列表
    private LayoutInflater inflater;
    private Context context;

    public ReportAdapter(Context context, ArrayList<ReportBaseVO> list) {
        inflater = LayoutInflater.from(context);
        reports = list;
        this.context = context;
    }

    @Override
    public int getCount() {
        return reports == null ? 0 : reports.size();
    }

    @Override
    public ReportBaseVO getItem(int position) {
        return reports.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_grid_vs, null);
            holder = new ViewHolder();
            holder.reportName = (TextView) convertView.findViewById(R.id.tv_reportName);
            holder.voteCount = (TextView) convertView.findViewById(R.id.tv_voteCount);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ReportBaseVO report = getItem(position);
        holder.reportName.setText(report.reportName);
        holder.voteCount.setText("完成进度 " + report.progress + "/" + report.voteCount);
        if (report.voteCount == report.progress) {
            holder.voteCount.setTextColor(context.getResources().getColor(R.color.text_color));
        }
        return convertView;
    }

    class ViewHolder {
        TextView reportName;
        TextView voteCount;
    }

}
