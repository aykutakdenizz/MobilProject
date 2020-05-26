package com.example.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.LinearLayout;

public class CalenderActivity extends AppCompatActivity {

    SharedPreferences sharedpreferences;
    public static final String MyPREFERENCES = "Calendar" ;
    private SharedPreferences.Editor editor;
    CalendarView calendarView;
    Button calenderButton;
    LinearLayout mainLayout;
    String date;
    public int year1=0,month1,dayOfMonth1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeSetting();
        setContentView(R.layout.activity_calender);

        setComponents();
        setListeners();
    }

    private void setThemeSetting() {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();
        if(sharedpreferences.getString("theme","Default").equals("Default")){
            setTheme(R.style.AppTheme);
        }else{
            setTheme(R.style.DarkTheme);
        }
    }

    private void setComponents() {
        calendarView = (CalendarView) findViewById(R.id.CalenderView);
        calenderButton = (Button) findViewById(R.id.CalenderViewButton);
        mainLayout = (LinearLayout) findViewById(R.id.CalenderMainLayout);

        if(!sharedpreferences.getString("theme","Default").equals("Default")){
            calenderButton.setBackground(getApplicationContext().getDrawable(R.drawable.shape_blue_dark));
            mainLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_blue_dark));
            calendarView.setBackground(getApplicationContext().getDrawable(R.drawable.setting_blue_dark));
        }else{
            calendarView.setBackground(getApplicationContext().getDrawable(R.drawable.calendar_item));
        }
    }
    private void setListeners(){
        calenderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CalenderActivity.this, SetReminderActivity.class);
                if(year1!=0){
                    editor.putString("calendar", "Yes");
                    editor.putString("year", year1+"");
                    editor.putString("month", month1+"");
                    editor.putString("dayOfMonth", dayOfMonth1+"");
                    editor.apply();
                }
                startActivity(intent);
            }
        });
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                year1 = year;
                month1 = month;
                dayOfMonth1 = dayOfMonth;
            }
        });
    }
}
