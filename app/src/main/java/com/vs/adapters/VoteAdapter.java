package com.vs.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vs.R;
import com.vs.model.ReportResult;

import java.util.ArrayList;

/**
 * Created by longjianlin on 15/4/3.
 */
public class VoteAdapter extends BaseAdapter {
    private ArrayList<ReportResult> reports;//报表列表
    private LayoutInflater inflater;

    public VoteAdapter(Context context, ArrayList<ReportResult> list) {
        inflater = LayoutInflater.from(context);
        reports = list;
    }

    @Override
    public int getCount() {
        return reports == null ? 0 : reports.size();
    }

    @Override
    public ReportResult getItem(int position) {
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
            convertView = inflater.inflate(R.layout.vote_item, null);
            holder = new ViewHolder();
            holder.tv_detailFirst = (TextView) convertView.findViewById(R.id.tv_detailFirst);
            holder.tv_inputDisplay = (TextView) convertView.findViewById(R.id.tv_inputDisplay);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ReportResult result = getItem(position);
        holder.tv_detailFirst.setText(result.detailFirst);
        holder.tv_inputDisplay.setText(result.inputDisplay);
        return convertView;
    }

    class ViewHolder {
        TextView tv_detailFirst;
        TextView tv_inputDisplay;
    }
}
