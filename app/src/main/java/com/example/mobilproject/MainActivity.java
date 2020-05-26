package com.example.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedpreferencesMap, sharedpreferencesCalendar;
    public static final String MyPREFERENCESMap = "Map", MyPREFERENCESCalendar = "Calendar";

    private SharedPreferences.Editor editorMap, editorCalendar;
    Spinner dateSpinner, typeSpinner;
    RecyclerView recyclerView;
    String typeKey, dateKey;
    ArrayList<NoteClass> recyclerList;
    SQLiteDatabase database;
    TextView counterText, pageNumberText, listText;
    Button rightButton, leftButton;
    int pageNumber;
    LinearLayout mainAllLayout, mainTopLayout,mainMiddleLeftLayout,mainMiddleRightLayout,mainBottomLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeSetting();
        setContentView(R.layout.activity_main);

        database = this.openOrCreateDatabase("Reminders", MODE_PRIVATE, null);
        database.execSQL(getResources().getString(R.string.SQLDB));

        sharedpreferencesMap = getSharedPreferences(MyPREFERENCESMap, Context.MODE_PRIVATE);
        editorMap = sharedpreferencesMap.edit();

        typeKey = "Tüm";
        dateKey = "Günlük";
        pageNumber = 1;
        setComponents();
        setAdapters();
        setSpinnerSelectedListeners();
        getDatas();
        setButtonListeners();
    }

    private void setThemeSetting() {
        sharedpreferencesCalendar = getSharedPreferences(MyPREFERENCESCalendar, Context.MODE_PRIVATE);
        editorCalendar = sharedpreferencesCalendar.edit();
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            setTheme(R.style.AppTheme);
        }else{
            setTheme(R.style.DarkTheme);
        }
    }

    private void setButtonListeners() {
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pageNumber > 1) {
                    pageNumber--;
                    getDatas();
                }
            }
        });
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pageNumber++;
                getDatas();
            }
        });
    }

    private void setComponents() {
        dateSpinner = (Spinner) findViewById(R.id.MainSpinnerDate);
        typeSpinner = (Spinner) findViewById(R.id.MainSpinnerType);
        recyclerView = (RecyclerView) findViewById(R.id.MainRecyclerView);
        counterText = (TextView) findViewById(R.id.MainCounterText);
        pageNumberText = (TextView) findViewById(R.id.MainPageNumber);
        listText = (TextView) findViewById(R.id.MainListText);
        leftButton = (Button) findViewById(R.id.MainLeftButton);
        rightButton = (Button) findViewById(R.id.MainRightButton);
        recyclerList = new ArrayList<>();

        editorMap.putString("map", "No");
        editorMap.putString("longitude", "No");
        editorMap.putString("latitude", "No");
        editorMap.apply();

        editorCalendar.putString("calendar", "No");
        editorCalendar.putString("note", "No");
        editorCalendar.putString("title", "");
        editorCalendar.putString("body", "");
        editorCalendar.apply();

        setBackgroundColor();
    }

    private void setBackgroundColor() {
        mainTopLayout = (LinearLayout) findViewById(R.id.MainLinearLayoutTop);
        mainMiddleLeftLayout = (LinearLayout) findViewById(R.id.MainLinearLayoutMiddleLeft);
        mainMiddleRightLayout = (LinearLayout) findViewById(R.id.MainLinearLayoutMiddleRight);
        mainBottomLayout = (LinearLayout) findViewById(R.id.MainLinearLayoutBottom);
        mainAllLayout = (LinearLayout) findViewById(R.id.MainAllLayout);
        if(!sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            mainTopLayout.setBackground(getApplicationContext().getDrawable(R.drawable.main_dark_theme));
            mainMiddleLeftLayout.setBackground(getApplicationContext().getDrawable(R.drawable.main_dark_theme));
            mainMiddleRightLayout.setBackground(getApplicationContext().getDrawable(R.drawable.main_dark_theme));
            mainBottomLayout.setBackground(getApplicationContext().getDrawable(R.drawable.main_dark_theme));
            counterText.setTextColor(Color.rgb(170,252,244));
            pageNumberText.setTextColor(Color.rgb(170,252,244));
            listText.setTextColor(Color.rgb(170,252,244));
            rightButton.setBackground(getApplicationContext().getDrawable(R.drawable.right_dark));
            leftButton.setBackground(getApplicationContext().getDrawable(R.drawable.left_dark));
        }else{
            mainAllLayout.setBackground(getApplicationContext().getDrawable(R.drawable.main_background));
        }
    }

    private void setAdapters() {
        ArrayAdapter<CharSequence> dateAdapter = ArrayAdapter.createFromResource(this,
                R.array.MainSpinnerDateStrings, android.R.layout.simple_spinner_item);
        dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dateSpinner.setAdapter(dateAdapter);

        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.MainSpinnerTypeStrings, android.R.layout.simple_spinner_item);

        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(linearLayoutManager);
    }

    private void setSpinnerSelectedListeners() {
        dateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                dateKey = getResources().getStringArray(R.array.MainSpinnerDateStrings)[position];
                pageNumber = 1;
                getDatas();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                typeKey = getResources().getStringArray(R.array.MainSpinnerTypeStrings)[position];
                pageNumber = 1;
                getDatas();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void getDatas() {
        try {
            //TODO System.out.println("--------------------------------------------------------");
            recyclerList.clear();
            Cursor cursor;
            if (typeKey.equals("Tüm")) {
                cursor = database.rawQuery(("SELECT * FROM reminders"), null);
            } else {
                cursor = database.rawQuery(("SELECT * FROM reminders WHERE type='" + typeKey + "'"), null);
            }

            int idx = cursor.getColumnIndex("id");
            int typex = cursor.getColumnIndex("type");
            int bodyx = cursor.getColumnIndex("body");
            int titlex = cursor.getColumnIndex("title");
            int colorx = cursor.getColumnIndex("color");
            int completedx = cursor.getColumnIndex("completed");
            int datex = cursor.getColumnIndex("date");
            int finishdatex = cursor.getColumnIndex("finishdate");
            int frequencyx = cursor.getColumnIndex("frequency");
            int all = 0, checked = 0;

            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            Calendar calendar = new GregorianCalendar();
            Calendar calendar2 = new GregorianCalendar();
            Date trialTime = new Date();
            calendar.setTime(trialTime);
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.AM_PM, 1);
            calendar = setCalendar(calendar);

            while (cursor.moveToNext()) {
                calendar2.setTime(format.parse((cursor.getString(datex).replaceAll("/", "-"))));
                long hours = Duration.between(calendar.toInstant(), calendar2.toInstant()).toHours();
                //TODO System.out.println("----" + hours + "----" + cursor.getString(colorx));
                if (((hours <= ((pageNumber) * getKeyNumberHours())) && checkFinish(calendar, cursor.getString(finishdatex), format, hours, cursor.getString(frequencyx),cursor.getString(datex))) || dateKey.equals("Tüm")) {
                    NoteClass note = new NoteClass();
                    note.setId(cursor.getString(idx));
                    note.setType(cursor.getString(typex));
                    note.setBody(cursor.getString(bodyx));
                    note.setTitle(cursor.getString(titlex));
                    note.setColor(cursor.getString(colorx));
                    note.setCompleted(cursor.getString(completedx));
                    note.setFinishdate(cursor.getString(finishdatex));
                    note.setDate(cursor.getString(datex));
                    note.setFrequency(cursor.getString(frequencyx));
                    all++;
                    if (note.getCompleted().equals("Yes")) {
                        checked++;
                    }
                    recyclerList.add(note);
                    //TODO System.out.println(cursor.getString(cursor.getColumnIndex("date")));
                }
            }

            listText.setText((format.format(calendar.getTime()) + " - " + textViewFinisTime(calendar, format)));
            counterText.setText(("Geçerli kriterde Yapılanlar " + checked + "/" + all));
            pageNumberText.setText(pageNumber + "");

            NoteAdapter noteAdapter = new NoteAdapter(getApplicationContext(), recyclerList);
            noteAdapter.setOnItemClickListener(new NoteAdapter.ClickListener() {
                @Override
                public void onItemClick(int position, View v) {
                    Intent intent = new Intent(MainActivity.this, UpdateReminderActivity.class);
                    intent.putExtra("reminder_id", recyclerList.get(position).getId());
                    startActivity(intent);
                    overridePendingTransition(R.anim.fade_in2, R.anim.slide_left);
                }

            });
            recyclerView.setAdapter(noteAdapter);
            cursor.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String textViewFinisTime(Calendar calendar, SimpleDateFormat format) {
        Calendar cal= new GregorianCalendar();
        cal.setTime(calendar.getTime());
        switch (dateKey) {
            case "Günlük":
                cal.add(Calendar.DAY_OF_MONTH,1);
                return format.format(cal.getTime());
            case "Haftalık":
                cal.add(Calendar.DAY_OF_MONTH,7);
                return format.format(cal.getTime());
            case "Aylık":
                cal.add(Calendar.MONTH,1);
                return format.format(cal.getTime());
            default:
                return "∞";
        }

    }

    private Calendar setCalendar(Calendar calendar) {
        switch (dateKey) {
            case "Günlük":
                calendar.add(Calendar.DAY_OF_MONTH, (pageNumber - 1));
                break;
            case "Haftalık":
                calendar.add(Calendar.DAY_OF_MONTH, ((pageNumber - 1) * 7));
                break;
            case "Aylık":
                calendar.add(Calendar.MONTH, (pageNumber - 1));
                break;
        }
        return calendar;
    }

    private int getKeyNumberHours() {
        switch (dateKey) {
            case "Günlük":
                return 24;
            case "Haftalık":
                return 7 * 24;
            case "Aylık":
                return 30 * 24;
        }
        return 1;
    }

    private boolean checkFinish(Calendar calendar, String finishTime, SimpleDateFormat format, long hour, String frequency,String startDate) {
        if (finishTime.equals("YOK")) {
            switch (frequency) {
                case "Bir Kez":
                    Calendar calendar2 = new GregorianCalendar();
                    try {
                        calendar2.setTime(format.parse((startDate.replaceAll("/", "-"))));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    long hours = Duration.between(calendar.toInstant(), calendar2.toInstant()).toHours();
                    return (hours >= 0 && hours <= 24);
                case "Her Gün":
                    return true;
                case "Haftada Bir":
                    hour = -hour;
                    return ((((hour + getKeyNumberHours()) % (24 * 7)) < getKeyNumberHours()) || (hour < 24 && hour >= 0));
                case "Ayda Bir":
                    hour = -hour;
                    return ((((hour + getKeyNumberHours()) % (24 * 30)) < getKeyNumberHours()) || (hour < 24 && hour >= 0));
                case "Yılda Bir":
                    hour = -hour;
                    return ((((hour + getKeyNumberHours()) % (24 * 365)) < getKeyNumberHours()) || (hour < 24 && hour >= 0));
            }
            return false;
        } else {
            Calendar calendar2 = new GregorianCalendar();
            try {
                calendar2.setTime(format.parse((finishTime.replaceAll("/", "-"))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long hours = Duration.between(calendar.toInstant(), calendar2.toInstant()).toHours();
            return (hours >= 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);

        MenuItem item = menu.getItem(0);
        SpannableString spanString = new SpannableString(menu.getItem(0).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(0, 77, 176)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        item = menu.getItem(1);
        spanString = new SpannableString(menu.getItem(1).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(102, 0, 255)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        item = menu.getItem(2);
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            spanString = new SpannableString(("Dark Temaya Geç"));
        }else{
            spanString = new SpannableString("Ana Temaya Dön");
        }
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(102, 0, 255)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.MainMenuNewReminder) {
            Intent intent = new Intent(MainActivity.this, CalenderActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.MainMenuRepeatReminder) {
            Intent intent = new Intent(MainActivity.this, SetReminderActivity.class);
            intent.putExtra("action", "no");
            startActivity(intent);
        } else if (item.getItemId() == R.id.MainMenuThema) {
            if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
                editorCalendar.putString("theme","Dark" );
                editorCalendar.apply();
            } else {
                editorCalendar.putString("theme","Default" );
                editorCalendar.apply();
            }
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}
