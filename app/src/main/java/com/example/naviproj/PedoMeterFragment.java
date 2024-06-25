package com.example.naviproj;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.SENSOR_SERVICE;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class PedoMeterFragment extends Fragment implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor stepDetectorSensor;
    private Sensor stepSensor;
    private TextView stepCountView;
    private Button resetButton;
    private boolean isCountingSteps;
    private SharedPreferences prefs;
    private SharedPreferences prefs2;
    private SharedPreferences prefs3;
    private DataBaseHelper dbHelper;
    private static final String PREF_NAME = "StepCountPrefs";
    private static final String PREF_STEP_COUNT = "stepCount";
    private static final String PREF_NAME_BEFORE = "StepCountPrefsBefore";
    private static final String PREF_STEP_BEFORE = "stepBefore";
    private static final String PREF_NAME_DATE = "StepCountPrefsDate";
    private static final String PREF_STEP_DATE = "stepDate"; //날짜 계산

    private TextView earth, seoul;

    private int stepCounting = 0;
    private int savedStepCount = 0;
    private int updatedWalkCount = 0;
    private String todayDates = "";
    private int realCount = 0; //센서값 말고 실제로 사용자에게 보여지고 db에 저장될 값
    private SharedPreferences.Editor editor;

    private List<ShowPressItem> ShowPressList;

    private ShowPressAdapter showAdapter;

    private int totalCount=0;

    private String totalEarth="", totalSeoul="";

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.pedometer_test, container, false);
        stepCountView = view.findViewById(R.id.stepCountView);
        resetButton = view.findViewById(R.id.resetButton);
        seoul=view.findViewById(R.id.seoul);
        earth=view.findViewById(R.id.earth);

        dbHelper = new DataBaseHelper(requireContext());

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatters = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        todayDates = formatters.format(calendar.getTime());

        prefs = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        sensorManager = (SensorManager) getActivity().getSystemService(SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        resetButton.setOnClickListener(v -> resetStepCount());

        realCount = getStepCountForToday();
        totalCount=getStepCountForTotal();

        stepCountView.setText(Integer.toString(realCount));

        totalSeoul=calSeoul(realCount,totalCount);
        totalEarth=calEarth(realCount,totalCount);

        seoul.setText(totalSeoul);
        earth.setText(totalEarth);


        Intent serviceIntent = new Intent(getActivity(), StepCountingService.class);
        getActivity().startService(serviceIntent);

        RecyclerView showRecyclerView = view.findViewById(R.id.walkListRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        showRecyclerView.setLayoutManager(layoutManager);

        showAdapter = new ShowPressAdapter(getShowPress());
        showRecyclerView.setAdapter(showAdapter);



        return view;
    }
    private List<ShowPressItem> getShowPress() {
        // 가상의 데이터를 생성하거나 실제 데이터를 가져오는 로직을 추가
        ShowPressList = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM userWalk ORDER BY Date ASC", null);
        int i = 0;

        if (cursor.moveToFirst()) {
            do {
                String date = cursor.getString(0);
                BigDecimal kg = new BigDecimal(cursor.getString(1));

                if (cursor.moveToPrevious()) {
                    BigDecimal previousKg = new BigDecimal(cursor.getString(1));
                    BigDecimal weightDifference = kg.subtract(previousKg);

                    if (weightDifference.compareTo(BigDecimal.ZERO) > 0) {
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "▲" + formattedDifference));
                    } else if (weightDifference.compareTo(BigDecimal.ZERO) < 0) {
                        // DecimalFormat을 사용하여 형식을 지정
                        BigDecimal absoluteDifference = weightDifference.abs();
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(absoluteDifference);

                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "▼" + formattedDifference));
                    } else {
                        // DecimalFormat을 사용하여 형식을 지정
                        DecimalFormat decimalFormat = new DecimalFormat("#00");
                        String formattedDifference = decimalFormat.format(weightDifference);
                        ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "±" + formattedDifference));
                    }
                    cursor.moveToNext();
                } else {
                    // 첫 번째 아이템은 차이가 없으므로 0으로 설정
                    ShowPressList.add(new ShowPressItem(date, Integer.parseInt(String.valueOf(kg)), "±00"));
                    cursor.moveToNext();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return ShowPressList;
    }

    @Override
    public void onResume() {
        super.onResume();
        first(); //앱이 처음일 때와 아닐때를 구분
        dayChange(); //자정 검사

        isCountingSteps = true;
        sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onPause() {
        super.onPause();
        isCountingSteps = false;
        sensorManager.unregisterListener(this);

        int currentStepCount = Integer.parseInt(stepCountView.getText().toString());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isCountingSteps) {
            realCount++;

            int finalCurrentStepCount = realCount;
            requireActivity().runOnUiThread(() -> stepCountView.setText(String.valueOf(finalCurrentStepCount)));

            if (realCount % 10 == 0) {
                insertDataToUserWalkTable(realCount);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do nothing
    }

    private void resetStepCount() {
        stepCountView.setText("0");
        stepCounting = 0;
        prefs.edit().putInt(PREF_STEP_COUNT, 0).apply();
    }

    private boolean checkFirst() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT 1 FROM userWalk LIMIT 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    private boolean checkZero() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String todayDate = formatter.format(calendar.getTime());
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT Walk FROM userWalk WHERE Date = ?", new String[]{todayDate});
            if (cursor != null && cursor.moveToFirst()) {
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
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

    private int getStepCountForTotal() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        int totalStepCount = 0;

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT SUM(Walk) FROM userWalk", null);
            if (cursor != null && cursor.moveToFirst()) {
                totalStepCount = cursor.getInt(0); // SUM(Walk)의 결과는 첫 번째 컬럼에 위치
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return totalStepCount;
    }
    private void insertDataToUserWalkTable(int userWalk) {
        Log.d("저장할 걸음수", String.valueOf(userWalk));
        dbHelper = new DataBaseHelper(getActivity());
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

    private String getMostRecentDate() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT Date FROM userWalk ORDER BY Date DESC LIMIT 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
    }

    private void first() {
        boolean chkFirst = checkFirst();
        if (chkFirst) {
            stepCountView.setText("0");

            prefs = getActivity().getSharedPreferences(PREF_NAME, MODE_PRIVATE);
            editor = prefs.edit();
            editor.putString(PREF_NAME, PREF_STEP_COUNT);
            editor.putInt(PREF_STEP_COUNT, 0).apply();

            prefs2 = getActivity().getSharedPreferences(PREF_NAME_BEFORE, MODE_PRIVATE);
            editor = prefs2.edit();
            editor.putString(PREF_NAME_BEFORE, PREF_STEP_BEFORE);
            editor.putInt(PREF_STEP_BEFORE, 0).apply();

            prefs3 = getActivity().getSharedPreferences(PREF_NAME_DATE, MODE_PRIVATE);
            editor = prefs3.edit();
            editor.putString(PREF_NAME_DATE, PREF_STEP_DATE);
            editor.putString(PREF_STEP_BEFORE, todayDates).apply();
        }
    }

    private void dayChange() {
        String compareDate = getMostRecentDate();
        if (!todayDates.equals(compareDate)) {
            int chkTodayVal = getStepCountForToday();
            if (chkTodayVal == 0) {
                editor = prefs.edit();
                editor.putInt(PREF_STEP_COUNT, 0).apply();
                insertDataToUserWalkTable(0);
                stepCountView.setText("0");
            } else {
                realCount = chkTodayVal;
                editor = prefs.edit();
                editor.putInt(PREF_STEP_COUNT, realCount).apply();
            }
        }
    }

    private String calSeoul(int today, int total) {
        // 걸음 수를 거리로 변환
        float todayWalk = (float) (today * 0.78) / 860000;
        float totalWalk = (float) (total * 0.78) / 860000;

        // 소수점 다섯째 자리에서 반올림하여 넷째 자리까지 표현
        String todayWalkFormatted = String.format("%.4f", todayWalk);
        String totalWalkFormatted = String.format("%.4f", totalWalk);

        // 결과 문자열 생성
        String result = "오늘 걸음을 기준으로 왕복 " + todayWalkFormatted + " 회\n누적 걸음을 기준으로 왕복 " + totalWalkFormatted+" 회";
        return result;
    }

    private String calEarth(int today, int total) {
        // 걸음 수를 거리로 변환
        float todayWalk = (float) (today * 0.78) / 40075000;
        float totalWalk = (float) (total * 0.78) / 40075000;

        // 소수점 다섯째 자리에서 반올림하여 넷째 자리까지 표현
        String todayWalkFormatted = String.format("%.4f", todayWalk);
        String totalWalkFormatted = String.format("%.4f", totalWalk);

        // 결과 문자열 생성
        String result = "오늘 걸음을 기준으로 " + todayWalkFormatted +"바퀴\n누적 걸음을 기준으로 " + totalWalkFormatted+" 바퀴";
        return result;
    }
}

