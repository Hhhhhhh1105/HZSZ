package com.zju.hzsz.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.zju.hzsz.R;
import com.zju.hzsz.model.Note;

import java.util.Collections;
import java.util.List;

/**
 * Created by Wangli on 2017/3/17.
 */

public class MyAdapter extends BaseAdapter {

    private Context context;
    private List<Note> notes;

    public MyAdapter(Context context, List<Note> notes) {
        this.context = context;
        //实现倒序排列
        Collections.reverse(notes);
        this.notes = notes;
    }

    public void removeAllItem() {
        notes.clear();
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        notes.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return notes.size();
    }

    @Override
    public Note getItem(int i) {
        return notes.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_note, null);
            viewHolder = new ViewHolder();
            viewHolder.tvTitle = (TextView) convertView.findViewById(R.id.note_title);
            viewHolder.tvContent = (TextView) convertView.findViewById(R.id.note_content);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.note_time);


        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.setId(notes.get(position).getId());
        viewHolder.tvTitle.setText(notes.get(position).getTitle());
        viewHolder.tvContent.setText(notes.get(position).getContent());
        viewHolder.tvTime.setText(notes.get(position).getTime());

        convertView.setTag(viewHolder);
        return convertView;
    }

    public static class ViewHolder {
        public int Id;
        public TextView tvTitle;
        public TextView tvContent;
        public TextView tvTime;

        public void setId(int id) {
            Id = id;
        }
    }
}
