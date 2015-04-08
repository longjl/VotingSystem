package com.vs.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vs.R;
import com.vs.model.Person;

import java.util.List;

/**
 * Created by longjianlin on 15/4/5.
 */
public class VotePersonAdapter extends BaseAdapter {

    private List<Person> mPersons;//报表列表
    private LayoutInflater inflater;

    public VotePersonAdapter(Context context, List<Person> persons) {
        inflater = LayoutInflater.from(context);
        mPersons = persons;
    }

    @Override
    public int getCount() {
        return mPersons == null ? 0 : mPersons.size();
    }

    @Override
    public Person getItem(int position) {
        return mPersons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.vote_person_item, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
            holder.imageivew = (ImageView) convertView.findViewById(R.id.imageivew);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Person person = getItem(position);
        holder.tv_name.setText(person.name);
        if (person.progress > 0) {
            holder.imageivew.setVisibility(View.VISIBLE);
        }
        return convertView;
    }

    class ViewHolder {
        TextView tv_name;
        ImageView imageivew;
    }
}