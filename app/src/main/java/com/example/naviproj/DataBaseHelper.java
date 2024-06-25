package com.example.naviproj;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Info.db";
    private static final int DATABASE_VERSION = 1;

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS userWeight");
        db.execSQL("DROP TABLE IF EXISTS userPress");
        db.execSQL("DROP TABLE IF EXISTS userSugar"); //혈당

        createTables(db);
    }

    private void createTables(SQLiteDatabase db) {
        // 테이블 생성 쿼리
        db.execSQL("CREATE TABLE IF NOT EXISTS userInfo (Name TEXT NOT NULL, Birth TEXT NOT NULL, Height REAL NOT NULL, Kg REAL NOT NULL, Sex TEXT NOT NULL,Blood TEXT,phoneNum TEXT );"); //사용자 기본 정보 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS userWeight (Name TEXT NOT NULL, Date TEXT NOT NULL, Kg REAL NOT NULL);"); //체중 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS userMediAlarm (alarmName TEXT NOT NULL, alarmStart TEXT NOT NULL, alarmEnd TEXT NOT NULL, alarmMorning TEXT, alarmNoon TEXT, alarmEvening TEXT, alarmNight TEXT);"); //약 알람 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS MediAlarmSystem (alarmName TEXT NOT NULL, codeMorning TEXT,codeNoon TEXT,codeEvening TEXT,codeNight TEXT, morningTime TEXT, noonTime TEXT, eveningTime TEXT, nightTime TEXT);"); //약 알람 테이블(시스템 설정용)
        db.execSQL("CREATE TABLE IF NOT EXISTS userPress (Name TEXT NOT NULL, Date TEXT NOT NULL, Time TEXT NOT NULL, Side TEXT NOT NULL, Systolic INT NOT NULL, Diastolic INT NOT NULL, Pulse REAL NOT NULL);"); //혈압 테이블
        db.execSQL("CREATE TABLE IF NOT EXISTS userSugar (Name TEXT NOT NULL, Date TEXT NOT NULL, Time TEXT NOT NULL, BloodSugar INT NOT NULL);"); //혈당 테이블
    }

}