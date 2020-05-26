package com.example.mobilproject;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import com.example.mobilproject.R.drawable;

import static android.content.Context.MODE_PRIVATE;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.MyViewHolder> {
    ArrayList<NoteClass> noteList;
    LayoutInflater inflater;
    Context context;
    SQLiteDatabase database;
    private static ClickListener clickListener;
    SharedPreferences sharedpreferencesCalendar;
    public static final String MyPREFERENCESCalendar = "Calendar";
    private SharedPreferences.Editor editorCalendar;

    public void setOnItemClickListener(ClickListener clickListener) {
        NoteAdapter.clickListener = clickListener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public NoteAdapter(Context context, ArrayList<NoteClass> notes) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.noteList = notes;
        sharedpreferencesCalendar = context.getSharedPreferences(MyPREFERENCESCalendar, Context.MODE_PRIVATE);
        editorCalendar = sharedpreferencesCalendar.edit();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_for_note_list, parent, false);
        if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
            view.getContext().setTheme(R.style.AppTheme);
        }else{
            view.getContext().setTheme(R.style.DarkTheme);
        }

        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        NoteClass selectedNote = noteList.get(position);
        holder.setData(selectedNote, position);
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView noteTitle, noteDate, noteType;
        String title, type, date;
        LinearLayout linearLayout;
        String id;
        View thisView ;
        CheckBox completeCheck ;
        SQLiteDatabase database;
        SharedPreferences sharedpreferencesCalendar;
        public static final String MyPREFERENCESCalendar = "Calendar";
        private SharedPreferences.Editor editorCalendar;

        public MyViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            thisView = itemView;
            noteTitle = (TextView) itemView.findViewById(R.id.item_note_name);
            noteDate = (TextView) itemView.findViewById(R.id.item_date);
            noteType = (TextView) itemView.findViewById(R.id.item_note_type);
            completeCheck = (CheckBox) itemView.findViewById(R.id.item_checkBox);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.item_linearLayout);
        }

        public void setData(NoteClass selectedNote, int position) {
            id = selectedNote.getId();
            title = selectedNote.getTitle();
            type = selectedNote.getType();
            date = selectedNote.getDate()+ " - "+ selectedNote.getFinishdate();
            setBackgroundColor(selectedNote.getColor());
            setCheckBoxClicker();

            //this.noteFrequency.setText(selectedNote.getFrequency());
            this.noteType.setText(type);

            String titleText;
            if(title.length() < 1){
                titleText = "Yok";
            }else if (title.length() < 20) {
                titleText = title;
            } else {
                titleText = title.substring(0, 20);
                titleText = titleText + "...";
            }
            this.noteTitle.setText(titleText);
            if(selectedNote.getCompleted().equals("Yes")){
                this.completeCheck.setChecked(true);
            }
            this.noteDate.setText(date);
        }

        public void setBackgroundColor(String color) {
            sharedpreferencesCalendar = thisView.getContext().getSharedPreferences(MyPREFERENCESCalendar, Context.MODE_PRIVATE);
            editorCalendar = sharedpreferencesCalendar.edit();
            if(sharedpreferencesCalendar.getString("theme","Default").equals("Default")){
                switch (color) {
                    case "Sarı":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.item_card_yellow));
                        break;
                    case "Kırmızı":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.item_card_red));
                        break;
                    case "Mor":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.item_card_purple));
                        break;
                    case "Mavi":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.item_card_blue));
                        break;
                    case "Yeşil":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.item_card_green));
                        break;
                    default:
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.item_card_white));
                }
            }else{
                switch (color) {
                    case "Sarı":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.setting_yellow_dark));
                        break;
                    case "Kırmızı":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.setting_red_dark));
                        break;
                    case "Mor":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.setting_purple_dark));
                        break;
                    case "Mavi":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.setting_blue_dark));
                        break;
                    case "Yeşil":
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.setting_green_dark));
                        break;
                    default:
                        linearLayout.setBackground(thisView.getContext().getDrawable(drawable.setting_white_dark));
                }
            }

        }

        public void setCheckBoxClicker(){
            completeCheck.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (completeCheck.isChecked()) {
                        try{
                            database = v.getContext().openOrCreateDatabase("Reminders", MODE_PRIVATE, null);
                            SQLiteStatement updateStatement = database.compileStatement("UPDATE reminders SET completed = 'Yes', alarm='No' WHERE id="+Integer.parseInt(id));
                            if(updateStatement.executeUpdateDelete()==1){
                                Toast.makeText(v.getContext(),"Hatırlatıcı yapıldı işaretlendi, Alarmı varsa kapatıldı",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(v.getContext(),"Hata oluştu! Tekrar deneyiniz",Toast.LENGTH_SHORT).show();
                            }
                            Intent myIntent = new Intent(v.getContext(), AlarmReceiver.class);
                            myIntent.setAction("com.example.mobilproject.MainActivty");
                            PendingIntent pIntent = PendingIntent.getBroadcast(v.getContext(), Integer.parseInt(id),
                                    myIntent, PendingIntent.FLAG_NO_CREATE);
                            if (pIntent != null) {
                                pIntent.cancel();
                                System.out.println("Görev tamamlandı işaretlendiği için alarm iptal");
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                            Toast.makeText(v.getContext(),"Hata oluştu! Tekrar deneyiniz",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try{
                            database = v.getContext().openOrCreateDatabase("Reminders", MODE_PRIVATE, null);
                            SQLiteStatement updateStatement = database.compileStatement("UPDATE reminders SET completed = 'No' WHERE id="+Integer.parseInt(id));
                            if(updateStatement.executeUpdateDelete()==1){
                                Toast.makeText(v.getContext(),"Hatırlatıcı yapılmadı işaretlendi",Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(v.getContext(),"Hata oluştu! Tekrar deneyiniz",Toast.LENGTH_SHORT).show();
                            }
                        }catch(Exception e){
                            e.printStackTrace();
                            Toast.makeText(v.getContext(),"Hata oluştu! Tekrar deneyiniz",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getAdapterPosition(), v);
        }
    }
}
