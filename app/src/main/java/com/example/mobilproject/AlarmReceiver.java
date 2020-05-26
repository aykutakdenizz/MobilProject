package com.example.mobilproject;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;

import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.content.Context.MODE_PRIVATE;
import static com.example.mobilproject.SetReminderActivity.NOTIFICATION_CHANNEL_ID;
import static java.lang.Thread.sleep;


public class AlarmReceiver extends BroadcastReceiver {
    private SQLiteDatabase database;
    MediaPlayer mediaPlayer;
    NoteClass note;
    AlarmManager alarmManager;
    Calendar calendar;
    Context thisContext;
    Intent thisIntent;
    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public static final String NOTIFICATION_CHANNEL_ID = "10001" ;
    private final static String default_notification_channel_id = "default" ;


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("com.example.mobilproject.MainActivty")) {
            showNotification(context, intent);

            thisContext=context;
            thisIntent=intent;
            thread.start();

            Toast.makeText(context, "Alarm çalıyor, bildirimlerinizi kontrol ediniz", Toast.LENGTH_SHORT).show();
            System.out.println("---ALARM GELDI");//TODO sil
            database = context.openOrCreateDatabase("Reminders", MODE_PRIVATE, null);
            getNoteValues(context, intent);
            setRepeatedAlarm(context);
            System.out.println("set bitti");
        }
    }

    private MediaPlayer findRingTone(Context context, String tone) {
        switch (tone) {
            case "alarm":
                return MediaPlayer.create(context, R.raw.alarm);
            case "juntos":
                return MediaPlayer.create(context, R.raw.juntos);
            case "serious-strike":
                return MediaPlayer.create(context, R.raw.ringtone_3);
            case "ring":
                return MediaPlayer.create(context, R.raw.ring);
            case "slow":
                return MediaPlayer.create(context, R.raw.slow);
            default:
                return MediaPlayer.create(context, R.raw.alarm);
        }
    }

    private void showNotification(Context context, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME", importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        assert notificationManager != null;
        notificationManager.notify(id, notification);
    }

    private void setRepeatedAlarm(Context context) {
        Intent myIntent = new Intent(context, AlarmReceiver.class);
        myIntent.setAction("com.example.mobilproject.MainActivty");
        calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Date trialTime = new Date();
        calendar.setTime(trialTime);
        setCalendarUpdate(note.getFrequency());


        Calendar calendar2 = new GregorianCalendar();
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        boolean check = false;

        if(note.getFinishdate().equals("YOK")){
            check= true;
        }else{
            try {
                calendar2.setTime(format.parse((note.getFinishdate().replaceAll("/", "-"))));
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if(Duration.between(calendar.toInstant(), calendar2.toInstant()).toHours()<=0){
                check= false;
            }else{
                check = true;
            }
        }

        if(!note.getFrequency().equals("Bir Kez") && check){
            myIntent.putExtra("type", note.getType());
            myIntent.putExtra("title",  note.getTitle());
            myIntent.putExtra("ringtone", note.getRingtone());
            myIntent.putExtra("id", note.getId());

            myIntent.putExtra(AlarmReceiver. NOTIFICATION_ID , Integer.parseInt(note.getId()) ) ;
            myIntent.putExtra(AlarmReceiver. NOTIFICATION , getNotification( note.getTitle(),"Tür:"+note.getType() ,context)) ;

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(note.getId()), myIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            System.out.println(calendar.toString()+" alarmı id:"+note.getId()+" kuruldu");
            alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
       }else{
            ContentValues values = new ContentValues();
            values.put("alarm","No");
            database.update("reminders", values,"id = "+ note.getId(), null);
        }
    }

    private void setCalendarUpdate(String inc) {
        calendar.set(Calendar.AM_PM, 1);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        switch (inc){
            case "Her Gün":
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                break;
            case "Haftada Bir":
                calendar.add(Calendar.DAY_OF_MONTH, 7);
                break;
            case "Ayda Bir":
                calendar.add(Calendar.MONTH, 1);
                break;
            case "Yılda Bir":
                calendar.add(Calendar.YEAR, 1);
                break;
        }
    }

    private void getNoteValues(Context context, Intent intent) {
        if(intent.getStringExtra("id")!=null){
            try{
                int id = Integer.parseInt(intent.getStringExtra("id"));
                note = new NoteClass();
                Cursor cursor;
                cursor = database.rawQuery(("SELECT * FROM reminders WHERE id="+id),null);

                while(cursor.moveToNext()){
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
                }
                cursor.close();

            }catch (Exception e){
                e.printStackTrace();
                Toast.makeText(context,"Hata oluştu,Alarm tekrarı oluşturulamadı!",Toast.LENGTH_SHORT).show();
            }
            System.out.println("Not okuma bitti");
        }
    }

    private Notification getNotification (String title, String content, Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( context, default_notification_channel_id ) ;
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

    public Thread thread = new Thread() {
        @Override
        public void run() {
            try {
                mediaPlayer = findRingTone(thisContext, thisIntent.getStringExtra("ringtone"));
                mediaPlayer.start();
                try {
                    sleep(3200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mediaPlayer.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
}
