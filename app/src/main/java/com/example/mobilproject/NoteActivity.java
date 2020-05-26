package com.example.mobilproject;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class NoteActivity extends AppCompatActivity {
    SharedPreferences sharedpreferencesCalendar ;
    public static final String MyPREFERENCESCalendar="Calendar" ;
    private SharedPreferences.Editor editorCalendar;
    private Button noteSaveButton,deleteButton;
    private EditText noteNameText,noteText;
    private Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setThemeSetting();
        setContentView(R.layout.activity_note);


        setComponents();
        setClickListeners();
        setContext(intent);
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
    private void setComponents(){
        noteSaveButton = (Button) findViewById(R.id.noteSaveButton);
        deleteButton = (Button) findViewById(R.id.noteBackButton);
        noteNameText = (EditText) findViewById(R.id.noteNameText);
        noteText = (EditText) findViewById(R.id.noteEditTextNote);
        intent = getIntent();

        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
        }else{
            deleteButton.setBackground(getApplicationContext().getDrawable(R.drawable.shape_red_dark));
            noteSaveButton.setBackground(getApplicationContext().getDrawable(R.drawable.shape_green_dark));
        }
    }
    private void setClickListeners(){
        noteSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(),"Not Kaydedildi",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(NoteActivity.this,SetReminderActivity.class);
                editorCalendar.putString("note", "Yes");
                editorCalendar.putString("title", noteNameText.getText().toString());
                editorCalendar.putString("body", noteText.getText().toString());
                editorCalendar.apply();
                startActivity(intent);
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void setContext(Intent intent){
        if(intent.getStringExtra("title")!= null){
            noteNameText.setText(intent.getStringExtra("title"));
        }
        if(intent.getStringExtra("body")!= null){
            noteText.setText(intent.getStringExtra("body"));
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
