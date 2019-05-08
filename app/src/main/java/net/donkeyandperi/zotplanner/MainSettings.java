package net.donkeyandperi.zotplanner;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainSettings extends AppCompatActivity {

    private MyApp app;
    TextView currentLanguage;
    TextView currentCheckingInterval;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        app = (MyApp) getApplication();
        app.readCheckingTimeInterval();

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
                Intent intent = new Intent(MainSettings.this, LanguageSettingsDialog.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        timeSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainSettings.this, NotificationSettingsTime.class);
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

    @Override
    protected void onResume() {
        super.onResume();
        currentLanguage.setText(String.format(getString(R.string.current_language_settings), app.getCurrentLanguage()));
        currentCheckingInterval.setText(String.format(getString(R.string.current_notification_settings), String.valueOf(app.getCheckingInterval())));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
