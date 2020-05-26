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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class UpdateReminderActivity extends AppCompatActivity {
    SharedPreferences sharedpreferencesCalendar, sharedpreferencesMap;
    public static final String MyPREFERENCESCalendar = "Calendar", MyPREFERENCESMap = "Map";
    private SharedPreferences.Editor editorCalendar, editorMap;
    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private final static String default_notification_channel_id = "default";
    String[] typeSpinnerList, colorSpinnerList, ringToneSpinnerList, frequencySpinnerList;
    Spinner typeSpinner, colorSpinner, ringToneSpinner, frequencySpinner;
    CheckBox checkBoxRemindBefore, checkBoxAlarm;
    LinearLayout layoutRemindBefore, layoutNote, mainLinearLayout;
    Button timeButton, finishDateButton;
    EditText remindBeforeHour, remindBeforeMinute, alarmHour, alarmMinute, noteBody, noteTitle;
    TextView dateText, finishDate;
    int year, month, dayOfMonth;
    NoteClass note;
    Intent intent;
    SQLiteDatabase database;
    AlarmManager alarmManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeSetting();
        setContentView(R.layout.activity_update_reminder);

        database = this.openOrCreateDatabase("Reminders", MODE_PRIVATE, null);
        alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        sharedpreferencesMap = getSharedPreferences(MyPREFERENCESMap, Context.MODE_PRIVATE);
        editorMap = sharedpreferencesMap.edit();
        setComponents();
        setButtonClickers();
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
        intent = getIntent();
        note = new NoteClass();
        dateText = (TextView) findViewById(R.id.UpdateDateText);
        typeSpinner = (Spinner) findViewById(R.id.UpdateSpinnerType);
        colorSpinner = (Spinner) findViewById(R.id.UpdateSpinnerColor);
        frequencySpinner = (Spinner) findViewById(R.id.UpdateSpinnerFrequency);
        ringToneSpinner = (Spinner) findViewById(R.id.UpdateSpinnerRingTone);
        checkBoxRemindBefore = (CheckBox) findViewById(R.id.UpdateCheckboxBefore);
        remindBeforeHour = (EditText) findViewById(R.id.UpdateCheckboxBeforeHour);
        remindBeforeMinute = (EditText) findViewById(R.id.UpdateCheckboxBeforeMinute);
        checkBoxAlarm = (CheckBox) findViewById(R.id.UpdateCheckboxAlarm);
        alarmHour = (EditText) findViewById(R.id.UpdateCheckboxAlarmHour);
        alarmMinute = (EditText) findViewById(R.id.UpdateCheckboxAlarmMinute);
        checkBoxRemindBefore = (CheckBox) findViewById(R.id.UpdateCheckboxBefore);
        layoutRemindBefore = (LinearLayout) findViewById(R.id.UpdateLinearLayoutRemindBefore);
        layoutNote = (LinearLayout) findViewById(R.id.UpdateLayoutNote);
        noteBody = (EditText) findViewById(R.id.UpdateReminderBody);
        noteTitle = (EditText) findViewById(R.id.UpdateReminderTitle);
        timeButton = (Button) findViewById(R.id.UpdateTimeButton);
        finishDateButton = (Button) findViewById(R.id.UpdateFinishDateButton);
        finishDate = (TextView) findViewById(R.id.UpdateFinishDate);
        mainLinearLayout = (LinearLayout) findViewById(R.id.UpdateMainLinearLayout);
        setIntentValues(intent);
        setBackgroundColor(note.getColor());
    }

    private void setIntentValues(Intent intent) {

        getNoteValues(intent);
        setCheckBoxClickers();
        setSpinnerAdapters();
        typeSpinner.setSelection(getIndexOfValue(typeSpinnerList, note.getType()));
        colorSpinner.setSelection(getIndexOfValue(colorSpinnerList, note.getColor()));
        dateText.setText(note.getDate());
        if (note.getRemindbefore().equals("No")) {
            checkBoxRemindBefore.setChecked(false);
        } else {
            checkBoxRemindBefore.setChecked(true);
            remindBeforeHour.setText(note.getRemindbefore().split(":")[0]);
            remindBeforeMinute.setText(note.getRemindbefore().split(":")[1]);
        }
        if ((!note.getBody().equals("")) || (!note.getTitle().equals(""))) {
            layoutNote.setVisibility(View.VISIBLE);
            noteBody.setText(note.getBody());
            noteTitle.setText(note.getTitle());
        } else {
            layoutNote.setVisibility(View.GONE);
        }
        ringToneSpinner.setSelection(getIndexOfValue(ringToneSpinnerList, note.getRingtone()));
        if (note.getAlarm().equals("No")) {
            checkBoxAlarm.setChecked(false);
            layoutRemindBefore.setVisibility(View.GONE);
        } else {
            checkBoxAlarm.setChecked(true);
            layoutRemindBefore.setVisibility(View.VISIBLE);
        }

        frequencySpinner.setSelection(getIndexOfValue(frequencySpinnerList, note.getFrequency()));

        alarmHour.setText(note.getTime().split(":")[0]);
        alarmMinute.setText(note.getTime().split(":")[1]);
        finishDate.setText(note.getFinishdate());
    }

    private void getNoteValues(Intent intent) {
        if (intent.getStringExtra("reminder_id") != null) {
            try {
                int id = Integer.parseInt(intent.getStringExtra("reminder_id"));
                Cursor cursor;
                cursor = database.rawQuery(("SELECT * FROM reminders WHERE id=" + id), null);

                while (cursor.moveToNext()) {
                    note.setId(cursor.getString(cursor.getColumnIndex("id")));
                    note.setType(cursor.getString(cursor.getColumnIndex("type")));
                    note.setColor(cursor.getString(cursor.getColumnIndex("color")));
                    note.setDate(cursor.getString(cursor.getColumnIndex("date")));
                    note.setFrequency(cursor.getString(cursor.getColumnIndex("frequency")));
                    note.setRemindbefore(cursor.getString(cursor.getColumnIndex("remindbefore")));
                    note.setBody(cursor.getString(cursor.getColumnIndex("body")));
                    note.setCompleted(cursor.getString(cursor.getColumnIndex("completed")));
                    note.setRingtone(cursor.getString(cursor.getColumnIndex("ringtone")));
                    note.setTitle(cursor.getString(cursor.getColumnIndex("title")));
                    note.setAlarm(cursor.getString(cursor.getColumnIndex("alarm")));
                    note.setFinishdate(cursor.getString(cursor.getColumnIndex("finishdate")));
                    note.setTime(cursor.getString(cursor.getColumnIndex("time")));
                    note.setLongitude(cursor.getString(cursor.getColumnIndex("longitude")));
                    note.setLatitude(cursor.getString(cursor.getColumnIndex("latitude")));
                }
                cursor.close();

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Hata oluştu", Toast.LENGTH_SHORT).show();
                Intent intentx = new Intent(UpdateReminderActivity.this, MainActivity.class);
                intentx.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentx);
            }
            if(sharedpreferencesMap.getString("map","No" ).equals("Yes")){
                note.setLatitude(sharedpreferencesMap.getString("latitude","0" ));
                note.setLongitude(sharedpreferencesMap.getString("longitude","0" ));
                System.out.println("Konumu güncellemişsin kardeşim haberin olsun");
            }
        }
    }

    private int getIndexOfValue(String[] array, String value) {
        int i = 0;
        for (String str : array) {
            if (str.equals(value)) {
                return i;
            }
            i++;
        }
        return 0;
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

    private void setSpinnerAdapters() {
        typeSpinnerList = getResources().getStringArray(R.array.MainSpinnerTypeValues);
        colorSpinnerList = getResources().getStringArray(R.array.ColorSpinnerString);
        ringToneSpinnerList = getResources().getStringArray(R.array.ringtoneNames);
        frequencySpinnerList = getResources().getStringArray(R.array.FrequencySpinnerString);

        ArrayAdapter<String> typeAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, typeSpinnerList);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(this,
                R.array.ColorSpinnerString, android.R.layout.simple_spinner_item);

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

    private void setButtonClickers() {
        timeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;

                mTimePicker = getTimePickerDialog(hour, minute);
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

    private Notification getNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, default_notification_channel_id);
        if (!title.equals("")) {
            builder.setContentTitle(title + " Anımsatıcısı Bildirimi");
        } else {
            builder.setContentTitle("Anımsatıcı Bildirimi");
        }
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.calendar);
        builder.setAutoCancel(true);
        builder.setChannelId(NOTIFICATION_CHANNEL_ID);
        return builder.build();
    }

    public String getShareBody() {
        String mail = "";
        mail = mail + "\n" + "Tür:" + note.getType();
        mail = mail + "\n" + "Tarih:" + note.getDate() + " - " + note.getFinishdate();
        mail = mail + "\n" + "Saat:" + note.getTime();
        if (!note.getTitle().equals("")) {
            mail = mail + "\n" + "Başlık:" + note.getTitle();
        }
        if (!note.getBody().equals("")) {
            mail = mail + "\n" + "İçerik:" + note.getBody();
        }
        if (!note.getLatitude().equals("No") && !note.getLongitude().equals("No")) {
            mail = mail + "\n" + "Yol Tarifi için:" + "http://maps.google.com/maps?saddr=" + note.getLatitude() + "," + note.getLongitude();
        }
        return mail;
    }

    private Intent getShareIntent(String type, String text) {
        boolean found = false;
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        List<ResolveInfo> resInfo = this.getPackageManager().queryIntentActivities(share, 0);
        System.out.println("resinfo: " + resInfo);
        if (!resInfo.isEmpty()) {
            for (ResolveInfo info : resInfo) {
                if (info.activityInfo.packageName.toLowerCase().contains(type) ||
                        info.activityInfo.name.toLowerCase().contains(type)) {
                    share.putExtra(Intent.EXTRA_TEXT, text);
                    share.setPackage(info.activityInfo.packageName);
                    found = true;
                    break;
                }
            }
            if (!found)
                return null;

            return share;
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.update_menu, menu);

        MenuItem item = menu.getItem(0);
        SpannableString spanString = new SpannableString(menu.getItem(0).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(0, 128, 255)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        item = menu.getItem(1);
        spanString = new SpannableString(menu.getItem(1).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(102, 0, 204)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        item = menu.getItem(2);
        spanString = new SpannableString(menu.getItem(2).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(46, 204, 46)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        item = menu.getItem(3);
        spanString = new SpannableString(menu.getItem(3).getTitle().toString());
        spanString.setSpan(new ForegroundColorSpan(Color.rgb(204, 2, 0)), 0, spanString.length(), 0); //fix the color to white
        item.setTitle(spanString);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.UpdateMenuLocation) {
            locationAction();
        } else if (item.getItemId() == R.id.UpdateMenuShare) {
            shareAction();
        } else if (item.getItemId() == R.id.UpdateMenuSave) {
            saveAction();
        } else if (item.getItemId() == R.id.UpdateMenuDelete) {
            AlertDialog.Builder builder1 = createAlertDialog();
            builder1.setTitle("Etkinliği sil");
            builder1.setMessage("Bu etkinliği silmek istediğinizden emin misiniz ? \n(Bu adımın geri dönüşü olmadığını unutmayınız !)");
            builder1.setNeutralButton(
                    "İptal et",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            builder1.setNegativeButton(
                    "Sil",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            deleteAction();
                            dialog.cancel();
                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveAction() {
        try {
            if (!note.getFinishdate().equals("YOK")) {
                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
                Calendar calendar3 = new GregorianCalendar();
                Calendar calendar4 = new GregorianCalendar();

                try {
                    calendar3.setTime(format.parse((note.getDate().replaceAll("/", "-"))));
                    calendar4.setTime(format.parse((finishDate.getText().toString().replaceAll("/", "-"))));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (Duration.between(calendar3.toInstant(), calendar4.toInstant()).toHours() < 0) {
                    AlertDialog.Builder builder1;
                    if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
                        builder1 = new AlertDialog.Builder(UpdateReminderActivity.this);
                    }else{
                        builder1 = new AlertDialog.Builder(UpdateReminderActivity.this, 4);
                    }
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
                    return;
                }
            }
            ContentValues values = new ContentValues();

            values.put("type", typeSpinner.getSelectedItem().toString());
            values.put("color", colorSpinner.getSelectedItem().toString());
            values.put("date", dateText.getText().toString());
            values.put("body", noteBody.getText().toString());
            values.put("ringtone", ringToneSpinner.getSelectedItem().toString());
            values.put("title", noteTitle.getText().toString());
            values.put("finishdate", finishDate.getText().toString());
            values.put("latitude", note.getLatitude());
            values.put("longitude", note.getLongitude());

            int timeh = Integer.parseInt(alarmHour.getText().toString());
            int timem = Integer.parseInt(alarmMinute.getText().toString());

            if (((timeh >= 0) && (timeh < 24)) && ((timem >= 0) && (timem < 60))) {
                values.put("time", (timeh + ":" + timem));
            } else {
                values.put("time", "00:00");
            }

            int alarmh = 0, alarmm = 0, beforeh = 0, beforem = 0;
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(System.currentTimeMillis());

            Intent myIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
            myIntent.setAction("com.example.mobilproject.MainActivty");

            if (checkBoxAlarm.isChecked() && (!alarmHour.getText().toString().equals("") && !alarmMinute.getText().toString().equals(""))) {
                alarmh = Integer.parseInt(alarmHour.getText().toString());
                alarmm = Integer.parseInt(alarmMinute.getText().toString());

                if (((alarmh >= 0) && (alarmh < 24)) && ((alarmm >= 0) && (alarmm < 60))) {
                    values.put("alarm", (alarmh + ":" + alarmm));
                } else {
                    Toast.makeText(getApplicationContext(), "Alarm geçersiz sayı\n 00:00'a kuruldu", Toast.LENGTH_SHORT).show();
                    values.put("alarm", "00:00");
                }

                calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(note.getDate().split("/")[0]));
                calendar.set(Calendar.MONTH, (Integer.parseInt(note.getDate().split("/")[1]) - 1));
                calendar.set(Calendar.YEAR, Integer.parseInt(note.getDate().split("/")[2]));
                calendar.set(Calendar.HOUR_OF_DAY, alarmh);
                calendar.set(Calendar.MINUTE, alarmm);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (checkBoxRemindBefore.isChecked() && (!remindBeforeHour.getText().toString().equals("") && !remindBeforeMinute.getText().toString().equals(""))) {
                    beforeh = Integer.parseInt(remindBeforeHour.getText().toString());
                    beforem = Integer.parseInt(remindBeforeMinute.getText().toString());
                    if (((beforeh >= 0) && (beforeh < 24)) && ((beforem >= 0) && (beforem < 60))) {
                        values.put("remindbefore", (beforeh + ":" + beforem));
                        calendar.add(Calendar.HOUR, -beforeh);
                        calendar.add(Calendar.MINUTE, -beforem);
                    } else {
                        Toast.makeText(getApplicationContext(), "Önce hatırlatmada geçersiz sayı\n 00:00'a kuruldu", Toast.LENGTH_SHORT).show();
                        values.put("remindbefore", "00:00");
                    }
                } else {
                    values.put("remindbefore", "No");
                }


                values.put("frequency", frequencySpinner.getSelectedItem().toString());
                myIntent.putExtra("type", typeSpinner.getSelectedItem().toString());
                myIntent.putExtra("title", noteTitle.getText().toString());
                myIntent.putExtra("ringtone", ringToneSpinner.getSelectedItem().toString());
                myIntent.putExtra("id", note.getId());
                myIntent.putExtra(AlarmReceiver.NOTIFICATION_ID, Integer.parseInt(note.getId()));
                myIntent.putExtra(AlarmReceiver.NOTIFICATION, getNotification(note.getTitle(), (note.getType() + " türünde anımsatıcı")));

                PendingIntent pendingIntent = PendingIntent.getBroadcast(UpdateReminderActivity.this, Integer.parseInt(note.getId()), myIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                System.out.println(calendar.toString() + " alarmı id:" + note.getId() + " kuruldu"); //TODO sil
                alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

            } else {
                values.put("alarm", "No");
                values.put("remindbefore", "No");
                values.put("frequency", frequencySpinner.getSelectedItem().toString());
                PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), Integer.parseInt(note.getId()),
                        myIntent, PendingIntent.FLAG_NO_CREATE);
                if (pIntent != null) {
                    pIntent.cancel();
                    Toast.makeText(getApplicationContext(), "Alarm kapatıldı", Toast.LENGTH_SHORT).show();
                    System.out.println("IPTAL ETTIM");
                }
            }


            int result = database.update("reminders", values, "id = " + note.getId(), null);
            if (result == 1) {
                Toast.makeText(getApplicationContext(), "Güncelleme Başarılı", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(UpdateReminderActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } else {
                Toast.makeText(getApplicationContext(), "Hata Oluştu !", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Hata oluştu, Tekrar deneyiniz", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteAction() {
        try {
            Intent myIntent = new Intent(getApplicationContext(), AlarmReceiver.class);
            myIntent.setAction("com.example.mobilproject.MainActivty");
            PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), Integer.parseInt(note.getId()),
                    myIntent, PendingIntent.FLAG_NO_CREATE);
            if (pIntent != null) {
                pIntent.cancel();
                System.out.println("Silindiği için iptal");
            }
            int result = database.delete("reminders", "id=?", new String[]{note.getId()});
            if (result == 1) {
                Toast.makeText(getApplicationContext(), "Anımsatıcı Silindi", Toast.LENGTH_SHORT).show();
                Intent intentx = new Intent(UpdateReminderActivity.this, MainActivity.class);
                intentx.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intentx);
            } else {
                Toast.makeText(getApplicationContext(), "Hata oluştu, Tekrar deneyiniz", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Hata oluştu, Tekrar deneyiniz", Toast.LENGTH_SHORT).show();
        }
    }

    private void locationAction() {
        if (note.getLongitude().equals("No") || note.getLatitude().equals("No")) {
            Toast.makeText(getApplicationContext(), "Bu anımsatıcıya ait konum yok!", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(UpdateReminderActivity.this, MapsActivity.class);
            intent.putExtra("map", "Yes");
            intent.putExtra("latitude", note.getLatitude());
            intent.putExtra("longitude", note.getLongitude());
            intent.putExtra("reminder_id",note.getId());
            startActivity(intent);
        }
    }

    private void shareAction() {
        AlertDialog.Builder builder1 = createAlertDialog();
        builder1.setTitle("Paylaşılacak uygulamayı seçiniz")
                .setItems(R.array.share, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: {
                                Intent mailIntent = new Intent(Intent.ACTION_SEND);
                                mailIntent.setType("plain/text");
                                mailIntent.putExtra(Intent.EXTRA_SUBJECT, "Takvim anımsatıcsı");
                                mailIntent.putExtra(Intent.EXTRA_TEXT, getShareBody());
                                startActivity(Intent.createChooser(mailIntent, "Uygulama Seçiniz"));
                                dialog.cancel();
                                break;
                            }
                            case 1: {

                                List<Intent> targetedShareIntents = new ArrayList<Intent>();

                                Intent facebookIntent = getShareIntent("facebook", getShareBody());
                                if (facebookIntent != null)
                                    targetedShareIntents.add(facebookIntent);

                                Intent twitterIntent = getShareIntent("twitter", getShareBody());
                                if (twitterIntent != null)
                                    targetedShareIntents.add(twitterIntent);

                                Intent instagramIntent = getShareIntent("instagram", getShareBody());
                                if (instagramIntent != null)
                                    targetedShareIntents.add(instagramIntent);

                                Intent chooser = Intent.createChooser(targetedShareIntents.remove(0), "Sosyal medya uygulamasını seçiniz:");

                                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, targetedShareIntents.toArray(new Parcelable[]{}));

                                startActivity(chooser);
                                dialog.cancel();
                                break;
                            }
                        }
                    }
                });
        builder1.setNegativeButton(
                "Geri",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    public void setBackgroundColor(String color) {
        if (sharedpreferencesCalendar.getString("theme", "Default").equals("Default")) {
            switch (color) {
                case "Sarı":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_yellow));
                    break;
                case "Kırmızı":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_red));
                    break;
                case "Mor":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_purple));
                    break;
                case "Mavi":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_blue));
                    break;
                case "Yeşil":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_green));
                    break;
                default:
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_white));
            }
        } else {
            timeButton.setBackground(getApplicationContext().getDrawable(R.drawable.shape_blue_dark));
            finishDateButton.setBackground(getApplicationContext().getDrawable(R.drawable.shape_blue_dark));
            switch (color) {
                case "Sarı":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_yellow_dark));
                    break;
                case "Kırmızı":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_red_dark));
                    break;
                case "Mor":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_purple_dark));
                    break;
                case "Mavi":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_blue_dark));
                    break;
                case "Yeşil":
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_green_dark));
                    break;
                default:
                    mainLinearLayout.setBackground(getApplicationContext().getDrawable(R.drawable.setting_white_dark));
            }
        }
    }

    public AlertDialog.Builder createAlertDialog(){
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            return new AlertDialog.Builder(UpdateReminderActivity.this);
        }else{
            return new AlertDialog.Builder(UpdateReminderActivity.this, 4);
        }
    }

    public TimePickerDialog getTimePickerDialog(int hour, int minute){
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")) {
            return new TimePickerDialog(UpdateReminderActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    alarmHour.setText(selectedHour + "");
                    alarmMinute.setText(selectedMinute + "");
                }
            }, hour, minute, true);//Yes 24 hour time
        }else{
            return new TimePickerDialog(UpdateReminderActivity.this,4, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    alarmHour.setText(selectedHour + "");
                    alarmMinute.setText(selectedMinute + "");
                }
            }, hour, minute, true);//Yes 24 hour time
        }
    }

    public DatePickerDialog getDatePickerDialog(Calendar newCalendar){
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){

            return  new DatePickerDialog(UpdateReminderActivity.this, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    finishDate.setText((dayOfMonth+"/"+(monthOfYear+1)+"/"+year));
                }

            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        }else{

            return  new DatePickerDialog(UpdateReminderActivity.this, R.style.myDialogTheme,new DatePickerDialog.OnDateSetListener() {
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
