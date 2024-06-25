//이곳에서 알람 재설정 및 알람이 울릴 때 수행할 작업을 정의
package com.example.naviproj;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class AReceiver extends BroadcastReceiver {
    private static final String TAG = "AReceiver";
    private static final String CHANNEL_ID = "alarm_channel";
    private DataBaseHelper dbHelper;
    //private Context context;


    private String code[]=new String[4];

    private Intent i=null;

    //private PendingIntent pendingIntent[]=new PendingIntent[4];
    private PendingIntent pendingIntent;

    public NotificationManagerCompat notificationManager;

    private String targetName="";

    private String testCode="";

    private String reHour=""; //재설정 할 시간
    private String reMinute="";//재설정 할 분

    private String legacyCode="";//기존 코드를 담아두기 위하(안써도 될듯, 나중에 지우기)
    private String newerCode="";//다음날 사용할 요청코드

    private String legacyName="";//기존 알람의 시간대를 저장하기 위함(아침 점심 저녁 밤 중)
    private String legacyTimeCode="";//기존 알람의 시간대를 저장하기 위함(아침 점심 저녁 밤 중)



    //public int code = 0;
    @Override
    public void onReceive(Context context, Intent intent) {
        //A 클래스를 보여주는 로직을 구현

        targetName = intent.getStringExtra("message");
        testCode = intent.getStringExtra("code");
        reHour=intent.getStringExtra("timeHour");
        reMinute=intent.getStringExtra("timeMinute");


        i = new Intent(context, AActivity.class);

        dbHelper = new DataBaseHelper(context);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        PendingIntent pendingIntent2;

        boolean del=deleteOrNot(targetName);
        if(del==false){ //이곳에 오면 알람이 계속 울림(종료일이 아직 아님)
            makeAlarm(context);
        }
        else{//이곳에 오면 안울리고 db에서도 삭제(종료일 도달)
            deleteAlarm(dbHelper,context, targetName);
        }


    }

    private void getCode(){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MediAlarmSystem.codeMorning,MediAlarmSystem.codeNoon,MediAlarmSystem.codeEvening,MediAlarmSystem.codeNight FROM MediAlarmSystem WHERE alarmName = ?", new String[] { targetName });

        int index = 0; // 배열 인덱스
        while (cursor.moveToNext()) {
            // 각 행에서 첫 번째 열의 값을 가져와 배열에 넣음
            code[index] = cursor.getString(0);
            index++;
        }
        cursor.close();
        dbHelper.close();
    }

    private void makeAlarm(Context context) {
        PendingIntent pendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(testCode), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        } else {
            pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(testCode), i, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Alarm Channel", NotificationManager.IMPORTANCE_HIGH);
                    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_alarm_pill)
                        .setContentTitle("약먹을 시간이에요!")
                        .setContentText("설정하신 약을 먹을 시간이에요!")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setFullScreenIntent(pendingIntent, true)
                        .setContentIntent(pendingIntent);

                NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                notificationManager.notify(Integer.parseInt(testCode), builder.build());

                setNewerCode(testCode);
                resetAlarm(context);
            }


    @SuppressLint("ScheduleExactAlarm")
    private void resetAlarm(Context context) {
        // 재설정 작업을 여기에 작성
        // Calendar를 사용하여 다음 날 같은 시간을 계산
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, 1); // 다음 날로 이동
        int hour = Integer.parseInt(reHour); // 다음 날 같은 시간
        int minute = Integer.parseInt(reMinute); // 다음 날 같은 분
        int second = 0; // 초는 0 고정
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);


        // 다음 알람 설정
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // 요청 코드를 변경하여 새로운 알람을 구별

        Intent alarmIntent = new Intent(context, AReceiver.class);
        alarmIntent.putExtra("message", targetName);
        alarmIntent.putExtra("code", newerCode); // "message"라는 키로 String 값을 추가
        alarmIntent.putExtra("timeHour",Integer.toString(hour)); //시간을 보내줌
        alarmIntent.putExtra("timeMinute",Integer.toString(minute)); //분을 보내줌
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(newerCode), alarmIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
    }

    private boolean deleteOrNot(String targetName){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT alarmEnd FROM userMediAlarm WHERE alarmName = ?", new String[] { targetName });
        if(cursor.moveToFirst()){
            String alarmEndString = cursor.getString(0); // alarmEnd 값을 가져옴
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            try {
                Date alarmEndDate = sdf.parse(alarmEndString); // 문자열을 Date로 변환
                Calendar calEnd = Calendar.getInstance();
                calEnd.setTime(alarmEndDate); //사용자가 설정한 종료일
                calEnd.set(Calendar.HOUR_OF_DAY, 0);
                calEnd.set(Calendar.MINUTE, 0);
                calEnd.set(Calendar.SECOND, 0);
                calEnd.set(Calendar.MILLISECOND, 0);

                Date currentDate = new Date(); // 현재 날짜 가져오기
                Calendar calCurrent = Calendar.getInstance();
                calCurrent.setTime(currentDate);
                calCurrent.set(Calendar.HOUR_OF_DAY, 0);
                calCurrent.set(Calendar.MINUTE, 0);
                calCurrent.set(Calendar.SECOND, 0);
                calCurrent.set(Calendar.MILLISECOND, 0);


                // 현재 날짜가 종료일 이전이거나 종료일과 같은 경우
                if (alarmEndDate != null && (calCurrent.before(calEnd) || calCurrent.equals(calEnd))) {
                    return false; // 삭제하지 않음 (알람 울림)
                }else if (alarmEndDate != null && calCurrent.after(calEnd)) {
                    return true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return true; // 삭제함 (알람 울리지 않음)
    }

    public void deleteAlarm(DataBaseHelper dbHelper, Context context,String targetName){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT codeMorning, codeNoon, codeEvening, codeNight FROM MediAlarmSystem WHERE alarmName = ?", new String[] { targetName });
        if(cursor.moveToFirst()) {
            String code[]=new String[4];
            for(int j=0;j<4;j++){
               code[j] = cursor.getString(j); // alarmEnd 값을 가져옴
                if(code[j]!=""){
                    Intent i = new Intent(context, AReceiver.class); // YourActivity는 알람을 받을 액티비티
                    PendingIntent pendingIntent;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(code[j]), i, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                    } else {
                        pendingIntent = PendingIntent.getActivity(context, Integer.parseInt(code[j]), i, PendingIntent.FLAG_UPDATE_CURRENT);
                    }

                    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    if (alarmManager != null) {
                        alarmManager.cancel(pendingIntent);
                    }

                    // PendingIntent 취소
                    pendingIntent.cancel();

                }

            }
            SQLiteDatabase db2 = dbHelper.getWritableDatabase();
            db2.execSQL("DELETE FROM MediAlarmSystem WHERE alarmName=?", new String[]{targetName});

            SQLiteDatabase db3 = dbHelper.getWritableDatabase();
            db3.execSQL("DELETE FROM userMediAlarm WHERE alarmName=?", new String[]{targetName});
            db.close();

        }
    }

    private void setNewerCode(String oldCode){
        //재설정 하면서 바뀐 요청 코드를 이곳에 저장
        int count = 0;
        Random random = new Random();
        SQLiteDatabase db = dbHelper.getReadableDatabase();


        newerCode = Integer.toString(Integer.parseInt(oldCode) + 1); //일단은 1을 더해보기
        String[] selectionArgs2 = {newerCode, newerCode, newerCode, newerCode}; // newerCode를 4번 넣어줌.
        while (true) {
            Cursor cursor2 = db.rawQuery("SELECT COUNT(*) FROM MediAlarmSystem WHERE codeMorning=? OR codeNoon=? OR codeEvening=? OR codeNight=?", selectionArgs2);
            if (cursor2.moveToFirst()) {
                count = cursor2.getInt(0); // 첫 번째 열의 값을 가져옴
                cursor2.close();
                if (count == 0) {
                    // 중복된 코드가 없으면 바로 반환
                    dbHelper.close();
                    break;

                } else {
                    // 중복된 코드가 있으면 새로운 코드를 생성하여 다시 확인
                    int newCode = random.nextInt(1000031) + 31; //중복이 없을 때까지 31부터 1,000,031까지의 랜덤 숫자를 더해 요청코드가 겹치는 상황을 원천차단. 31부터인 이유는 1을 더할 경우 다음날 요청 코드에 영향을 주기 때문
                    newerCode = Integer.toString(Integer.parseInt(oldCode) + newCode);
                    selectionArgs2 = new String[]{newerCode, newerCode, newerCode, newerCode}; // 새로운 코드를 selectionArgs에 업데이트
                }
            } else {
                cursor2.close();
                dbHelper.close();
                break;
            }
        }

        SQLiteDatabase db2 = dbHelper.getWritableDatabase();
        String sql = "UPDATE MediAlarmSystem SET " +
                "codeMorning = CASE WHEN codeMorning = " + oldCode + " THEN " + newerCode + " ELSE codeMorning END, " +
                "codeNoon = CASE WHEN codeNoon = " + oldCode + " THEN " + newerCode + " ELSE codeNoon END, " +
                "codeEvening = CASE WHEN codeEvening = " + oldCode + " THEN " + newerCode + " ELSE codeEvening END, " +
                "codeNight = CASE WHEN codeNight = " + oldCode + " THEN " + newerCode + " ELSE codeNight END " +
                "WHERE codeMorning = " + oldCode + " OR codeNoon = " + oldCode + " OR codeEvening = " + oldCode + " OR codeNight = " + oldCode;
        db2.execSQL(sql);
        db.close();

    }
    }
