package com.zju.hzsz.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.zju.hzsz.model.Note;

import java.util.List;

/**
 * Created by Wangli on 2017/3/17.
 */

public class DBManger {
    private Context context;
    private NoteOpenHelper databaseOpenHelper;
    private SQLiteDatabase dbReader;
    private SQLiteDatabase dbWriter;
    private static DBManger instance;

    public DBManger(Context context){
        this.context = context;
        databaseOpenHelper = new NoteOpenHelper(context);
        //创建、打开一个数据库
        dbReader = databaseOpenHelper.getReadableDatabase();
        dbWriter = databaseOpenHelper.getWritableDatabase();
    }

    /**
     * getInstance单例
     * @param context
     * @return
     */
    public static synchronized DBManger getInstance(Context context){
        if(instance == null){
            instance = new DBManger(context);
        }

        return instance;
    }

    /**
     * 添加纪录至数据库
     * @param title
     * @param content
     * @param time
     */
    public void addToDB(String title, String content, String time){
        //组装数据
        ContentValues cv = new ContentValues();
        cv.put(NoteOpenHelper.TITLE, title);
        cv.put(NoteOpenHelper.CONTENT, content);
        cv.put(NoteOpenHelper.TIME, time);
        dbWriter.insert(NoteOpenHelper.TABLE_NAME, null, cv);
    }

    /**
     * 读取数据
     * @param noteList
     */
    public void readFromDB(List<Note> noteList){
        Cursor cursor = dbReader.query(NoteOpenHelper.TABLE_NAME, null, null, null, null, null, null);
        try{
            while(cursor.moveToNext()){
                Note note = new Note();
                note.setId(cursor.getInt(cursor.getColumnIndex(NoteOpenHelper.ID)));
                note.setTitle(cursor.getString(cursor.getColumnIndex(NoteOpenHelper.TITLE)));
                note.setContent(cursor.getString(cursor.getColumnIndex(NoteOpenHelper.CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(NoteOpenHelper.TIME)));
                noteList.add(note);
            }
            cursor.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 更新数据
     * @param noteID
     * @param title
     * @param content
     * @param time
     */
    public void updateNote(int noteID, String title, String content, String time) {
        ContentValues cv = new ContentValues();
        cv.put(NoteOpenHelper.ID, noteID);
        cv.put(NoteOpenHelper.TITLE, title);
        cv.put(NoteOpenHelper.CONTENT, content);
        cv.put(NoteOpenHelper.TIME, time);
        dbWriter.update(NoteOpenHelper.TABLE_NAME, cv, "_id = ?", new String[]{noteID + ""});
    }

    /**
     * 删除数据
     * @param noteID
     */
    public void deleteNote(int noteID) {
        dbWriter.delete(NoteOpenHelper.TABLE_NAME, "_id = ?", new String[]{noteID + ""});
    }

    /**
     * 根据ID查询数据
     * @param noteID
     * @return
     */
    public Note readData(int noteID) {
        Cursor cursor = dbReader.rawQuery("SELECT * FROM note WHERE _id = ?", new String[]{noteID + ""});
        Note note = new Note();
        if(cursor != null && cursor.moveToFirst()){
            note.setId(cursor.getInt(cursor.getColumnIndex(NoteOpenHelper.ID)));
            note.setTitle(cursor.getString(cursor.getColumnIndex(NoteOpenHelper.TITLE)));
            note.setContent(cursor.getString(cursor.getColumnIndex(NoteOpenHelper.CONTENT)));
        }

        return note;
    }
}
