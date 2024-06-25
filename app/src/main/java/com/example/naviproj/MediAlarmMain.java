//알람에 코드를 부여하고 리시버로 넘겨주기 위한 클래스
package com.example.naviproj;

import static android.content.Context.ALARM_SERVICE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MediAlarmMain {

    private Context context;
    private DataBaseHelper dbHelper;
    public String alarmValue;

    private String start = "";
    private String end = "";

    private Integer code[] = new Integer[4]; //코드 저장을 위한 것

    private String setTime[] = new String[4]; //사용자가 설정한 시작시간


    // 생성자를 통한 값 전달
    public MediAlarmMain(Context context, String alarmValue) {
        this.context = context;
        this.alarmValue = alarmValue;
    }

    public void setAlarm() {
        dbHelper = new DataBaseHelper(context);
        getDate(); //시작일과 종료일을 가져오기 위함
        getCode(); //알람생성에 필요한 requestCode를 가져오기 위함

        makeAlarm(); //알람 생성
    }

    public void getDate() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        //Cursor cursor = db.rawQuery("SELECT userMediAlarm.alarmStart, userMediAlarm.alarmEnd From userMediAlarm INNER JOIN MediAlarmSystem ON userMediAlarm.alarmName=MediAlarmSystem.alarmName", null);
        Cursor cursor = db.rawQuery("SELECT alarmStart, alarmEnd FROM userMediAlarm WHERE alarmName = ?", new String[]{alarmValue});
        if (cursor.moveToFirst()) {
            start = cursor.getString(0);
            end = cursor.getString(1);
        }
        cursor.close();

        cursor = db.rawQuery("SELECT MediAlarmSystem.morningTime,MediAlarmSystem.noonTime,MediAlarmSystem.eveningTime,MediAlarmSystem.nightTime From MediAlarmSystem WHERE alarmName = ?", new String[]{alarmValue});
        if (cursor.moveToFirst()) {
            for (int i = 0; i < 4; i++) {
                setTime[i] = cursor.getString(i);
            }
        }
        cursor.close();
        dbHelper.close();
    }

    public void getCode() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT MediAlarmSystem.codeMorning,MediAlarmSystem.codeNoon,MediAlarmSystem.codeEvening,MediAlarmSystem.codeNight FROM MediAlarmSystem WHERE alarmName = ?", new String[]{alarmValue});
        if (cursor.moveToFirst()) {
            for (int i = 0; i < 4; i++) {
                code[i] = Integer.parseInt(cursor.getString(i));
            }
        }
        cursor.close();

        dbHelper.close();
    }


    @SuppressLint("ScheduleExactAlarm")
    public void makeAlarm() { //알람설정, 더 수정해야됨
        // AlarmManager 가져오기
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        // 알람이 울릴 때 실행될 Intent 생성
        Intent intent = new Intent(context, AReceiver.class);

        PendingIntent pendingIntent[] = new PendingIntent[4];

        Calendar calendar[] = new Calendar[4]; //개별 알람 설정을 위한것

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            // 현재 시간 가져오기
            Date currentDate = new Date();
            Date setTimeDate=null;
            for (int i = 0; i < 4; i++) {
                calendar[i] = Calendar.getInstance(); // 각 요소에 Calendar 인스턴스를 할당
                if(!(setTime[i].isEmpty())) {
                    setTimeDate = dateFormat.parse(start + " " + setTime[i]);

                    // 현재 시간과 setTime 비교

                    String[] timeSplit = setTime[i].split(":");
                    int hour = Integer.parseInt(timeSplit[0]);
                    int minute = Integer.parseInt(timeSplit[1]);


                    String[] dateSplit = start.split("-");
                    int year = Integer.parseInt(dateSplit[0]);
                    int month = Integer.parseInt(dateSplit[1]) - 1;//java에서 month는 0부터 시작
                    int date = Integer.parseInt(dateSplit[2]);
                    if (setTimeDate.after(currentDate)) {
                        //calendar[i].set(year+1900,month+60,date+60,hour,minute,0);
                        calendar[i].set(year, month, date, hour, minute, 0); //현재 여기 null오류 발생
                    } else {
                        calendar[i].set(year, month, date, hour, minute, 0);
                        calendar[i].add(Calendar.DAY_OF_MONTH, 1); // 다음날로 설정
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        intent.putExtra("message", alarmValue);
                        intent.putExtra("code", Integer.toString(code[i])); // "message"라는 키로 String 값을 추가
                        intent.putExtra("timeHour", Integer.toString(hour)); //시간을 보내줌
                        intent.putExtra("timeMinute", Integer.toString(minute)); //분을 보내줌
                        pendingIntent[i] = PendingIntent.getBroadcast(context, code[i], intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
                    } else {
                        pendingIntent[i] = PendingIntent.getBroadcast(context, code[i], intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    }


                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar[i].getTimeInMillis(), pendingIntent[i]);//세팅은 하는데 안울림

                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar[i].getTimeInMillis(), pendingIntent[i]);
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar[i].getTimeInMillis(), pendingIntent[i]);
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
