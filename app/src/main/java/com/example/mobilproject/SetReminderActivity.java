package com.example.mobilproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class SetReminderActivity extends AppCompatActivity {
    SharedPreferences sharedpreferences, sharedpreferencesCalendar ;
    public static final String MyPREFERENCES = "Map",MyPREFERENCESCalendar="Calendar" ;
    private SharedPreferences.Editor editor, editorCalendar;
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;
    String[] typeSpinnerList, colorSpinnerList, ringToneSpinnerList, frequencySpinnerList;
    Spinner typeSpinner, colorSpinner, ringToneSpinner,frequencySpinner;
    CheckBox checkBoxRemindBefore, checkBoxAlarm;
    LinearLayout layoutFrequency, layoutRemindBefore,layoutMain;
    Button  timeButton, finishDateButton;
    EditText remindBeforeHour, remindBeforeMinute, alarmHour, alarmMinute;
    TextView dateText, finishDate;
    int year, month, dayOfMonth;
    Intent intent;
    SQLiteDatabase database;
    AlarmManager alarmManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeSetting();
        setContentView(R.layout.activity_set_reminder);

        database = this.openOrCreateDatabase("Reminders", MODE_PRIVATE, null);
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        editor = sharedpreferences.edit();

        setComponents();
        setIntentValues(intent);
        setSpinnerAdapters();
        setCheckBoxClickers();
        setButtonClickers();
        dateText.setText((dayOfMonth + "/" + (month+1) + "/" + year));
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


    private void setComponents() {
        dateText = (TextView) findViewById(R.id.SelectDateText);
        typeSpinner = (Spinner) findViewById(R.id.SelectSpinnerType);
        colorSpinner = (Spinner) findViewById(R.id.SelectSpinnerColor);
        ringToneSpinner = (Spinner) findViewById(R.id.SelectSpinnerRingTone);
        frequencySpinner = (Spinner) findViewById(R.id.SelectSpinnerFrequency);
        checkBoxRemindBefore = (CheckBox) findViewById(R.id.SelectCheckboxBefore);
        remindBeforeHour = (EditText) findViewById(R.id.SelectCheckboxBeforeHour);
        remindBeforeMinute = (EditText) findViewById(R.id.SelectCheckboxBeforeMinute);
        checkBoxAlarm = (CheckBox) findViewById(R.id.SelectCheckboxAlarm);
        alarmHour = (EditText) findViewById(R.id.SelectCheckboxAlarmHour);
        alarmMinute = (EditText) findViewById(R.id.SelectCheckboxAlarmMinute);
        checkBoxRemindBefore = (CheckBox) findViewById(R.id.SelectCheckboxBefore);
        layoutFrequency = (LinearLayout) findViewById(R.id.SelectLinearLayoutFrequency);
        layoutRemindBefore = (LinearLayout) findViewById(R.id.SelectLinearLayoutRemindBefore);
        timeButton = (Button) findViewById(R.id.SelectTimeButton);
        finishDateButton = (Button) findViewById(R.id.SelectFinishDateButton);
        finishDate = (TextView) findViewById(R.id.SelectFinishDate);
        layoutMain = (LinearLayout) findViewById(R.id.SetMainLayout);

        remindBeforeHour.setVisibility(View.GONE);
        remindBeforeMinute.setVisibility(View.GONE);
        layoutRemindBefore.setVisibility(View.GONE);
        checkBoxAlarm.setChecked(false);
        checkBoxRemindBefore.setChecked(false);
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            layoutMain.setBackground(getApplicationContext().getDrawable(R.drawable.setting_white));
        }else{
            layoutMain.setBackground(getApplicationContext().getDrawable(R.drawable.setting_white_dark));
            timeButton.setBackground(getApplicationContext().getDrawable(R.drawable.shape_blue_dark));
            finishDateButton.setBackground(getApplicationContext().getDrawable(R.drawable.shape_blue_dark));
        }
        intent = getIntent();
    }

    private void setSpinnerAdapters() {
        typeSpinnerList = getResources().getStringArray(R.array.MainSpinnerTypeValues);
        ringToneSpinnerList = getResources().getStringArray(R.array.ringtoneNames);
        colorSpinnerList = getResources().getStringArray(R.array.ColorSpinnerString);
        frequencySpinnerList = getResources().getStringArray(R.array.FrequencySpinnerString);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, typeSpinnerList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, colorSpinnerList);

        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);

        ArrayAdapter<String> ringToneAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, ringToneSpinnerList);
        ringToneAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ringToneSpinner.setAdapter(ringToneAdapter);

        ArrayAdapter<String> frequencyAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, frequencySpinnerList);

        frequencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        frequencySpinner.setAdapter(frequencyAdapter);
    }


    private void setIntentValues(Intent intent) {
        Calendar calendar = new GregorianCalendar();
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        if(sharedpreferencesCalendar.getString("calendar","No" ).equals("Yes")){
            year = Integer.parseInt(sharedpreferencesCalendar.getString("year","0" ));
            month = Integer.parseInt(sharedpreferencesCalendar.getString("month","0" ));
            dayOfMonth = Integer.parseInt(sharedpreferencesCalendar.getString("dayOfMonth","0" ));
        }else{
            Calendar calendar1 = new GregorianCalendar();
            Date trialTime1 = new Date();
            calendar1.setTime(trialTime1);
            dayOfMonth = calendar1.get(Calendar.DAY_OF_MONTH);
            month = calendar1.get(Calendar.MONTH);
            year = calendar1.get(Calendar.YEAR);
        }
    }

    private void setCheckBoxClickers() {
        checkBoxAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxAlarm.isChecked()) {
                    layoutRemindBefore.setVisibility(View.VISIBLE);
                } else {
                    layoutRemindBefore.setVisibility(View.GONE);
                }
            }
        });
        checkBoxRemindBefore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBoxRemindBefore.isChecked()) {
                    remindBeforeHour.setVisibility(View.VISIBLE);
                    remindBeforeMinute.setVisibility(View.VISIBLE);
                } else {
                    remindBeforeHour.setVisibility(View.GONE);
                    remindBeforeMinute.setVisibility(View.GONE);
                    remindBeforeHour.setHint("HH");
                    remindBeforeMinute.setHint("MM");
                }
            }
        });
    }

    private void setButtonClickers() {
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = getTimePickerDialog(hour,minute);
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });

        finishDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar newCalendar = Calendar.getInstance();
                DatePickerDialog StartTime = getDatePickerDialog(newCalendar);
                StartTime.show();
            }
        });
    }

    private void setReminderValues(NoteClass note) {
        note.setType(typeSpinner.getSelectedItem().toString());
        note.setColor(colorSpinner.getSelectedItem().toString());
        note.setDate(dayOfMonth + "/" + (month+1) + "/" + year);

        note.setFrequency(frequencySpinner.getSelectedItem().toString());

        if (checkBoxAlarm.isChecked() && checkBoxRemindBefore.isChecked() && (!remindBeforeHour.getText().toString().equals("")) && (!remindBeforeMinute.getText().toString().equals(""))) {
            int h = Integer.parseInt(remindBeforeHour.getText().toString()), m = Integer.parseInt(remindBeforeMinute.getText().toString());
            if ((h >= 0 && h < 24) && (m >= 0 && m < 60)) {
                note.setRemindbefore(h + ":" + m);
            } else {
                note.setRemindbefore("No");
            }
        } else {
            note.setRemindbefore("No");
        }

        if(sharedpreferencesCalendar.getString("note","No" ).equals("Yes")){
            note.setTitle(sharedpreferencesCalendar.getString("title","" ));
            note.setBody(sharedpreferencesCalendar.getString("body","" ));
        }else{
            note.setTitle("");
            note.setBody("");
        }

        note.setCompleted("No");
        note.setRingtone(ringToneSpinner.getSelectedItem().toString());

        if (checkBoxAlarm.isChecked() && !alarmHour.getText().toString().equals("") && !alarmMinute.getText().toString().equals("")) {
            int h = Integer.parseInt(alarmHour.getText().toString()), m = Integer.parseInt(alarmMinute.getText().toString());
            if ((h >= 0 && h < 24) && (m >= 0 && m < 60)) {
                note.setAlarm(h + ":" + m);
            } else {
                note.setAlarm("No");
            }
        } else {
            note.setAlarm("No");
        }
        if(finishDate.getText().toString().trim().length() == 0){
            Calendar calendar = new GregorianCalendar();
            Date trialTime = new Date();
            calendar.setTime(trialTime);
            note.setFinishdate("YOK");
            //note.setFinishdate(calendar.get(Calendar.DAY_OF_MONTH)+"/"+(calendar.get(Calendar.MONTH)+1)+"/"+calendar.get(Calendar.YEAR));
        }else{
            //note.setFinishdate(finishDate.getText().toString().split("/")[0]+"/"+(Integer.parseInt(finishDate.getText().toString().split("/")[1])-1)+"/"+finishDate.getText().toString().split("/")[2]);
            note.setFinishdate(finishDate.getText().toString());
            // TODO System.out.println("BITIS:"+note.getFinishdate());
        }

        if(alarmHour.getText().toString().trim().length() == 0 || alarmHour.getText().toString().trim().length() == 0 ){
            note.setTime("00:00");
        }else{
            int h = Integer.parseInt(alarmHour.getText().toString()), m = Integer.parseInt(alarmMinute.getText().toString());
            if ((h >= 0 && h < 24) && (m >= 0 && m < 60)) {
                note.setTime(h + ":" + m);
            } else {
                note.setTime("00:00");
            }
        }

        if(sharedpreferences.getString("map","No" ).equals("Yes")){
            note.setLatitude(sharedpreferences.getString("latitude","0" ));
            note.setLongitude(sharedpreferences.getString("longitude","0" ));
        }else{
            note.setLatitude("No");
            note.setLongitude("No");
        }
    }

    private Notification getNotification (String title,String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( this, default_notification_channel_id ) ;
        if(!title.equals("")){
            builder.setContentTitle( title + " Anımsatıcısı Bildirimi" ) ;
        }else{
            builder.setContentTitle( "Anımsatıcı Bildirimi" ) ;
        }
        builder.setContentText(content) ;
        builder.setSmallIcon(R.drawable.calendar) ;
        builder.setAutoCancel( true ) ;
        builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
        return builder.build() ;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.set_menu,menu);

        MenuItem item = menu.getItem(0);
        SpannableString spanString = new SpannableString(menu.getItem(0).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(0,128,255)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        item = menu.getItem(1);
        spanString = new SpannableString(menu.getItem(1).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(102,0,204)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        item = menu.getItem(2);
        spanString = new SpannableString(menu.getItem(2).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(46,204,46)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.SetMenuNote){
            noteAction();
        }else if(item.getItemId() == R.id.SetMenuLocation){
            locationAction();
        }else if (item.getItemId() == R.id.SetMenuSave){
            saveAction();
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveAction() {
        NoteClass note = new NoteClass();
        setReminderValues(note);
        Cursor cursor=null;
        if(!note.getFinishdate().equals("YOK")){
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            Calendar calendar = new GregorianCalendar();
            Calendar calendar2 = new GregorianCalendar();

            try {
                calendar.setTime(format.parse((note.getDate().replaceAll("/", "-"))));
                calendar2.setTime(format.parse((note.getFinishdate().replaceAll("/", "-"))));

            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(Duration.between(calendar.toInstant(), calendar2.toInstant()).toHours()<0){
                AlertDialog.Builder builder1 = createAlertDialog();
                builder1.setTitle("Geçersiz Tarih");
                builder1.setMessage("Etkinliğin bitiş tarihi, başlangıç tarihinden önceki bir tarihi girdiniz. Lütfen bitiş tarihini kontrol ediniz.");
                builder1.setNegativeButton(
                        "Kapat",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert11 = builder1.create();
                alert11.show();
                return ;
            }
        }
        try {
            String sqlString = getResources().getString(R.string.SQLInsert);
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1, note.getType());
            sqLiteStatement.bindString(2, note.getColor());
            sqLiteStatement.bindString(3, note.getDate());
            sqLiteStatement.bindString(4, note.getFrequency());
            sqLiteStatement.bindString(5, note.getRemindbefore());
            sqLiteStatement.bindString(6, note.getBody());
            sqLiteStatement.bindString(7, note.getCompleted());
            sqLiteStatement.bindString(8, note.getRingtone());
            sqLiteStatement.bindString(9, note.getTitle());
            sqLiteStatement.bindString(10, note.getAlarm());
            sqLiteStatement.bindString(11, note.getFinishdate());
            sqLiteStatement.bindString(12, note.getTime());
            sqLiteStatement.bindString(13, note.getLatitude());
            sqLiteStatement.bindString(14, note.getLongitude());

            sqLiteStatement.execute();

            cursor = database.rawQuery(("SELECT * FROM reminders "), null);
            int idx = cursor.getColumnIndex("id");
            while (cursor.moveToNext()) {
                note.setId(cursor.getString(idx));
            }
            int hour = 0, minute = 0, day = 0;

            if (!note.getAlarm().equals("No")) {
                hour = Integer.parseInt(note.getAlarm().split(":")[0]);
                minute = Integer.parseInt(note.getAlarm().split(":")[1]);
                Intent myIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
                myIntent.setAction("com.example.mobilproject.MainActivty");
                myIntent.putExtra("type", note.getType());
                myIntent.putExtra("title", note.getTitle());
                myIntent.putExtra("ringtone", note.getRingtone());
                myIntent.putExtra("id", note.getId());

                myIntent.putExtra(AlarmReceiver. NOTIFICATION_ID , Integer.parseInt(note.getId()) ) ;
                myIntent.putExtra(AlarmReceiver. NOTIFICATION , getNotification( note.getTitle(),(note.getType()+ " türünde anımsatıcı") )) ;

                PendingIntent pendingIntent = PendingIntent.getBroadcast(SetReminderActivity.this, Integer.parseInt(note.getId()), myIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);


                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());

                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(note.getDate().split("/")[0]));
                calendar.set(Calendar.MONTH, (Integer.parseInt(note.getDate().split("/")[1]) - 1));
                calendar.set(Calendar.YEAR, Integer.parseInt(note.getDate().split("/")[2]));
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, minute);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (!note.getRemindbefore().equals("No")) {
                    calendar.add(Calendar.HOUR, -Integer.parseInt(note.getRemindbefore().split(":")[0]));
                    calendar.add(Calendar.MINUTE, -Integer.parseInt(note.getRemindbefore().split(":")[1]));
                }
                System.out.println(calendar.toString()+" alarmı id:"+note.getId()+" kuruldu");
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
            }
            Toast.makeText(getApplicationContext(), "Kayıt Başarılı", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SetReminderActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Hata oluştu, Tekrar deneyiniz", Toast.LENGTH_SHORT).show();
        }finally {
            if(cursor !=null){
                cursor.close();
            }
        }
    }

    private void locationAction() {
        Intent intent = new Intent(SetReminderActivity.this, MapsActivity.class);
        startActivity(intent);
    }

    private void noteAction() {
        Intent intent = new Intent(SetReminderActivity.this, NoteActivity.class);
        startActivity(intent);
    }

    public AlertDialog.Builder createAlertDialog(){
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            return new AlertDialog.Builder(SetReminderActivity.this);
        }else{
            return new AlertDialog.Builder(SetReminderActivity.this, 4);
        }
    }

    public TimePickerDialog getTimePickerDialog(int hour, int minute){
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            return new TimePickerDialog(SetReminderActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    alarmHour.setText(selectedHour+"");
                    alarmMinute.setText(selectedMinute+"");
                }
            }, hour, minute, true);//Yes 24 hour time
        }else{
            return new TimePickerDialog(SetReminderActivity.this, 4, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    alarmHour.setText(selectedHour+"");
                    alarmMinute.setText(selectedMinute+"");
                }
            }, hour, minute, true);//Yes 24 hour time
        }
    }

    public DatePickerDialog getDatePickerDialog(Calendar newCalendar){
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){

            return  new DatePickerDialog(SetReminderActivity.this, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    finishDate.setText((dayOfMonth+"/"+(monthOfYear+1)+"/"+year));
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        }else{

            return  new DatePickerDialog(SetReminderActivity.this, R.style.myDialogTheme,new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    finishDate.setText((dayOfMonth+"/"+(monthOfYear+1)+"/"+year));
                }
            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
    }
}
