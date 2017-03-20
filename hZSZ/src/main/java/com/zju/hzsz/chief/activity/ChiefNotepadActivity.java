package com.zju.hzsz.chief.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.zju.hzsz.R;
import com.zju.hzsz.adapter.MyAdapter;
import com.zju.hzsz.db.DBManger;
import com.zju.hzsz.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Wangli on 2017/3/8.
 */

public class ChiefNotepadActivity extends BaseActivity {

    private FloatingActionButton addBtn;
    private TextView emptyListTextView;
    private DBManger dm;
    private List<Note> noteDataList = new ArrayList<>();
    private MyAdapter adapter;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_notepad);
        setTitle("备忘录");
        initHead(R.drawable.ic_head_back, 0);

        initNotePad();

    }

    private void initNotePad() {
        dm = new DBManger(this);
        dm.readFromDB(noteDataList);
        listView = (ListView) findViewById(R.id.list);
        addBtn = (FloatingActionButton) findViewById(R.id.add);
        emptyListTextView = (TextView) findViewById(R.id.empty);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ChiefNotepadActivity.this, ChiefEditNoteActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
            }
        });
        adapter = new MyAdapter(this, noteDataList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new NoteClickListener());
        listView.setOnItemLongClickListener(new NoteLongClickListener());
        updateView();
    }
    

    //listView单击事件
    private class NoteClickListener implements AdapterView.OnItemClickListener{

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            MyAdapter.ViewHolder viewHolder = (MyAdapter.ViewHolder) view.getTag();
            int noteId = viewHolder.Id;
            Intent intent = new Intent(ChiefNotepadActivity.this, ChiefEditNoteActivity.class);
            intent.putExtra("id", noteId);
            startActivity(intent);
        }
    }



    //listView长按事件
    private class NoteLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, final int i, long l) {
            final Note note = ((MyAdapter) adapterView.getAdapter()).getItem(i);
            if (note == null) {
                return true;
            }
            final int id = note.getId();
            new MaterialDialog.Builder(ChiefNotepadActivity.this)
                    .content("确定删除此条笔记？")
                    .positiveText("确定")
                    .negativeText("取消")
                    .callback(new MaterialDialog.ButtonCallback(){
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            DBManger.getInstance(ChiefNotepadActivity.this).deleteNote(id);
                            adapter.removeItem(i);
                            updateView();
                        }
                    }).show();

            return true;
        }
    }

    //数据更新
    private void updateView() {

        if (noteDataList.isEmpty()) {
            listView.setVisibility(View.GONE);
            emptyListTextView.setVisibility(View.VISIBLE);
        } else {
            listView.setVisibility(View.VISIBLE);
            emptyListTextView.setVisibility(View.GONE);
        }

    }


}
