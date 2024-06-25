package com.example.naviproj;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StepCountingService extends Service implements SensorEventListener {
    private static final String CHANNEL_ID = "step_counting_service";
    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private int stepCount;
    private DataBaseHelper dbHelper;
    private SharedPreferences prefs;
    private static final String PREF_NAME = "StepCountPrefs";
    private static final String PREF_STEP_COUNT = "stepCount";

    private static int realCount=0;

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        dbHelper = new DataBaseHelper(this);
        realCount=getStepCountForToday();
        startForegroundService();
        scheduleStepResetTask();
    }

    private void startForegroundService() {
        String channelName = "Step Counting Service";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setSound(null, null);
            channel.setVibrationPattern(new long[]{0});
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

        //바로 밑에 빨간줄은 무시해도 됨
        Notification.Builder builder = new Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("만보기가 정상적으로 작동중이에요")
                .setSmallIcon(R.mipmap.ic_icons)
                .setColor(getColor(R.color.mainColor))
                .setVibrate(null);

        Notification notification = builder.build();
        startForeground(1, notification);
    }

    private void scheduleStepResetTask() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, StepResetReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, 1);

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                pendingIntent
        );
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            //stepCount++;
            realCount++;
            if (realCount % 100 == 0) {
                insertDataToUserWalkTable(realCount);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void insertDataToUserWalkTable(int userWalk) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String userDate = formatter.format(calendar.getTime());
        values.put("Date", userDate);
        values.put("Walk", userWalk);
        int rowsAffected = db.update("userWalk", values, "Date=?", new String[]{userDate});
        if (rowsAffected == 0) {
            db.insert("userWalk", null, values);
        }
        db.close();
    }

    public static class StepResetReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            realCount=0;
        }
    }

    private int getStepCountForToday() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = formatter.format(calendar.getTime());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int stepCount = 0;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT Walk FROM userWalk WHERE Date = ?", new String[]{todayDate});
            if (cursor != null && cursor.moveToFirst()) {
                int walkColumnIndex = Integer.parseInt(cursor.getString(0));
                if (walkColumnIndex != -1) {
                    stepCount = walkColumnIndex;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return stepCount;
    }
}