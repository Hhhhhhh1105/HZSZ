package com.zju.hzsz.chief.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Selection;
import android.text.Spannable;
import android.view.View;
import android.widget.EditText;

import com.getbase.floatingactionbutton.FloatingActionButton;
import com.zju.hzsz.R;
import com.zju.hzsz.db.DBManger;
import com.zju.hzsz.model.Note;

import java.util.Date;

/**
 * Created by Wangli on 2017/3/8.
 */

public class ChiefEditNoteActivity extends BaseActivity {
    private EditText titleEt;
    private EditText contentEt;
    private FloatingActionButton saveBtn;
    private int noteID = -1;
    private DBManger dbManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chief_editnote);
        setTitle("编辑备忘");
        initHead(R.drawable.ic_head_back, 0);

        initEditNotePad();
    }

    private void initEditNotePad() {
        dbManger = new DBManger(this);
        titleEt = (EditText) findViewById(R.id.note_title);
        contentEt = (EditText) findViewById(R.id.note_content);
        saveBtn = (FloatingActionButton) findViewById(R.id.save);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = titleEt.getText().toString();
                String content = contentEt.getText().toString();
                String time = getTime();

                //将最新修改的放最前面
                if ( noteID != -1) {
                    dbManger.deleteNote(noteID);
                }

                dbManger.addToDB(title, content, time);

                Intent i = new Intent(ChiefEditNoteActivity.this, ChiefNotepadActivity.class);
                startActivity(i);
                finish();
            }
        });

        noteID = getIntent().getIntExtra("id", -1);
        if (noteID != -1) {
            showNoteData(noteID);
        }
    }

    private void showNoteData(int noteID) {
        Note note = dbManger.readData(noteID);
        titleEt.setText(note.getTitle());
        contentEt.setText(note.getContent());
        //控制光标
        Spannable spannable = titleEt.getText();
        Selection.setSelection(spannable, titleEt.getText().length());
    }


    private String getTime() {

        java.text.SimpleDateFormat format = new java.text.SimpleDateFormat("MM-dd HH:mm E");
        Date curDate = new Date();
        String str = format.format(curDate);
        return str;

    }
}
