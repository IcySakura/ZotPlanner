package net.donkeyandperi.zotplanner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class NotificationSettingsTime extends AppCompatActivity {

    private MyApp app;
    TextView currentTimeIntervalTextView;
    TextView currentNotifyMeWhen;
    LinearLayout keepNotifyMeLayout;
    CheckBox keepNotifyMeCheckbox;
    TextView keepNotifyMeCurrentTextview;
    Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.notification_settings_time_label);
        setContentView(R.layout.notification_settings_time);
        app = (MyApp) getApplication();

        app.readKeepNotifyMe();

        LinearLayout timeIntervalLayout = (LinearLayout) findViewById(R.id.notification_settings_time_interval_layout);
        LinearLayout currentCheckingListLayout = (LinearLayout) findViewById(R.id.notification_settings_time_current_checking_list_layout);
        LinearLayout notifyMeWhenLayout = (LinearLayout) findViewById(R.id.notification_settings_status_options_layout);
        keepNotifyMeLayout = (LinearLayout) findViewById(R.id.notification_settings_time_keep_notify_me_layout);

        currentTimeIntervalTextView = (TextView) findViewById(R.id.notification_settings_time_current_status);
        TextView currentCheckingListTextView = (TextView) findViewById(R.id.notification_settings_time_current_checking_list);
        currentNotifyMeWhen = (TextView) findViewById(R.id.notification_settings_current_status_options);
        keepNotifyMeCheckbox = (CheckBox) findViewById(R.id.notification_settings_time_keep_notify_me_checkbox);
        keepNotifyMeCurrentTextview = (TextView) findViewById(R.id.notification_settings_time_current_keep_notify_me);

        Toolbar toolbar = (Toolbar) findViewById(R.id.notification_settings_toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        timeIntervalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NotificationSettingsTime.this, NotificationSettingsTimeInterval.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        notifyMeWhenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NotificationSettingsTime.this, NotificationSettingsTimeStatusOption.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        StringBuilder currentCheckingListString = new StringBuilder();
        for(SingleCourse singleCourse: app.getNotificationSingleCourseList()){
            currentCheckingListString.append(singleCourse.getCourseCode());
            currentCheckingListString.append(" ");
            currentCheckingListString.append(singleCourse.getCourseName());
            currentCheckingListString.append("\n");
        }
        currentCheckingListTextView.setText(currentCheckingListString.toString());

        setUpKeepNotifyMe();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentTimeIntervalTextView.setText(String.format(getString(R.string.current_notification_settings), String.valueOf(app.getCheckingInterval())));

        if(app.getNotificationWhenStatus().isEmpty()){
            app.readNotificationWhenStatus();
        }
        StringBuilder currentNotifyMeWhenString = new StringBuilder();
        for(String courseStatus: app.getNotificationWhenStatus()){
            currentNotifyMeWhenString.append(courseStatus);
            currentNotifyMeWhenString.append(" | ");
        }
        currentNotifyMeWhenString.deleteCharAt(currentNotifyMeWhenString.lastIndexOf("|"));
        currentNotifyMeWhen.setText(currentNotifyMeWhenString.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void refreshKeepNotifyMe(){
        if(app.isKeepNotifyMe()){
            keepNotifyMeCurrentTextview.setText(R.string.notification_settings_time_keep_notify_me_open);
            keepNotifyMeCheckbox.setChecked(true);
        } else {
            keepNotifyMeCurrentTextview.setText(R.string.notification_settings_time_keep_notify_me_close);
            keepNotifyMeCheckbox.setChecked(false);
        }
    }

    public void setUpKeepNotifyMe(){
        refreshKeepNotifyMe();
        keepNotifyMeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(app.isKeepNotifyMe()){
                    app.setAndSaveKeepNotifyMe(false);
                } else {
                    app.setAndSaveKeepNotifyMe(true);
                }
                refreshKeepNotifyMe();
                app.refreshNotificationService(context);
            }
        });
        keepNotifyMeCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(app.isKeepNotifyMe()){
                    app.setAndSaveKeepNotifyMe(false);
                } else {
                    app.setAndSaveKeepNotifyMe(true);
                }
                refreshKeepNotifyMe();
                app.refreshNotificationService(context);
            }
        });
    }

}
