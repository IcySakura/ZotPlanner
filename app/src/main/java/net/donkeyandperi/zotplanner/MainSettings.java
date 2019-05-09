package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class MainSettings extends AppCompatActivity {

    private final static String TAG = "MainSettings";
    private MyApp app;
    TextView currentLanguage;
    TextView currentCheckingInterval;
    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (MyApp) getApplication();
        app.readCheckingTimeInterval();
        app.setLanguage(context);

        setContentView(R.layout.main_settings);
        setTitle(R.string.main_settings_label);

        LinearLayout languageSettings = (LinearLayout) findViewById(R.id.main_settings_language_textview);
        LinearLayout timeSettings = (LinearLayout) findViewById(R.id.main_settings_notification_textview);
        final LinearLayout aboutSettings = (LinearLayout) findViewById(R.id.main_settings_about_layout);

        currentLanguage = (TextView) findViewById(R.id.main_settings_current_language);
        currentCheckingInterval = (TextView) findViewById(R.id.main_settings_current_notification_mode);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_settings_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        languageSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                AlertDialog alertDialog = setAlertDialogForLangaugeSettings(alertDialogBuilder);
                /*
                Intent intent = new Intent(MainSettings.this, LanguageSettingsDialog_Abandoned.class);
                startActivity(intent);
                */
                alertDialog.show();
                // overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        timeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainSettings.this, NotificationSettings.class);
                startActivity(intent);
            }
        });

        aboutSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainSettings.this, AboutSettingsMain.class);
                startActivity(intent);
            }
        });

    }

    private AlertDialog setAlertDialogForLangaugeSettings(AlertDialog.Builder alertDialogBuilder){
        View language_settings_view = LayoutInflater.from(context).inflate(R.layout.language_settings, null);
        alertDialogBuilder.setView(language_settings_view);
        RadioButton englishUSbtn = (RadioButton) language_settings_view.findViewById(R.id.language_settings_english_us);
        RadioButton chineseSimplifiedbtn = (RadioButton) language_settings_view.findViewById(R.id.language_settings_chinese_simplified);
        RadioButton japanesebtn = (RadioButton) language_settings_view.findViewById(R.id.language_settings_japanese);
        RadioButton defaultBtn = (RadioButton) language_settings_view.findViewById(R.id.language_settings_default);
        final RadioGroup radioGroup = (RadioGroup) language_settings_view.findViewById(R.id.language_settings_radiogroup);
        switch (app.getSavedLanguage()){
            case "zh-cn":
                chineseSimplifiedbtn.setChecked(true);
                break;
            case "en-us":
                englishUSbtn.setChecked(true);
                break;
            case "ja-rJP":
                japanesebtn.setChecked(true);
                break;
            default:
                defaultBtn.setChecked(true);
                break;
        }
        alertDialogBuilder.setPositiveButton(R.string.understand, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String notification = "";
                switch(radioGroup.getCheckedRadioButtonId()){
                    case R.id.language_settings_default:
                        app.saveLanguage(CourseStaticData.defaultLanguage);
                        notification = getString(R.string.change_language_to_default);
                        break;
                    case R.id.language_settings_english_us:
                        app.saveLanguage(CourseStaticData.unitedStatesEnglish);
                        notification = "App Language will be changed to English (US) after relaunch";
                        break;
                    case R.id.language_settings_chinese_simplified:
                        app.saveLanguage(CourseStaticData.simplifiedChinese);
                        notification = "应用语言将会在下次打开时变为中文（简体）";
                        break;
                    case R.id.language_settings_japanese:
                        app.saveLanguage(CourseStaticData.japaneseJapan);
                        notification = "アプリの言語は再起動後に変更します";
                        break;
                }
                if(!notification.isEmpty()){
                    Toast.makeText(context, notification, Toast.LENGTH_LONG).show();
                }
                app.setIsLanguageJustChanged(true);
            }
        });
        alertDialogBuilder.setTitle(R.string.language_settings_dialog_label);
        return alertDialogBuilder.create();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume is being called.");
        currentLanguage.setText(String.format(getString(R.string.current_language_settings), app.getSavedLanguage()));
        currentCheckingInterval.setText(String.format(getString(R.string.current_notification_settings), String.valueOf(app.getCheckingInterval())));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
