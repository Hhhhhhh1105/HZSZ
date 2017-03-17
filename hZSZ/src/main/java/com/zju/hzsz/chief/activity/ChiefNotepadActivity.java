package com.zju.hzsz.chief.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.sin.android.sinlibs.adapter.SimpleViewInitor;
import com.zju.hzsz.R;
import com.zju.hzsz.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangli on 2017/3/8.
 */

public class ChiefNotepadActivity extends BaseActivity {

    private FloatingActionButton addBtn;
    private ListView listView;
    private TextView emptyListTextView;

    private SimpleViewInitor simpleViewInitor = null;
    private List<Note> notes = new ArrayList<Note>();



    //listView的适配器
    private SimpleViewInitor noteInitor = new SimpleViewInitor() {
        @Override
        public View initView(Context context, int position, View convertView, ViewGroup parent, Object data) {
            if (convertView == null){
                convertView = RelativeLayout.inflate(context, R.layout.item_note, null);
            }

            Note note = (Note) data;

            ((TextView)convertView.findViewById(R.id.note_id)).setText(note.noteId);
            ((TextView)convertView.findViewById(R.id.note_title)).setText(note.title);
            ((TextView)convertView.findViewById(R.id.note_content)).setText(note.content);
            ((TextView)convertView.findViewById(R.id.note_time)).setText(note.time);

            convertView.setTag(note);

            //还需绑定跳转监听器，点击具体条目

            return convertView;

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_notepad);
        setTitle("备忘录");
        initHead(R.drawable.ic_head_back, 0);

        listView = (ListView) findViewById(R.id.list);
        addBtn = (FloatingActionButton) findViewById(R.id.add);
        emptyListTextView = (TextView) findViewById(R.id.empty);
        addBtn.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add:{

                break;
            }
        }
    }
}
