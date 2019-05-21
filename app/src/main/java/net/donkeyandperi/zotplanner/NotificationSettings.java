package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class NotificationSettings extends AppCompatActivity {

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
        setContentView(R.layout.notification_settings);
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
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                AlertDialog alertDialog = setUpCheckingIntervalAlertDialog(alertDialogBuilder);
                alertDialog.show();
                /*
                Intent intent = new Intent(NotificationSettings.this, Abandoned_NotificationSettingsTimeInterval.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                */
            }
        });

        notifyMeWhenLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                AlertDialog alertDialog = setUpNotifyMeWhenAlertDialog(alertDialogBuilder);
                alertDialog.show();
                /*
                Intent intent = new Intent(NotificationSettings.this, Abandoned_NotificationSettingsTimeStatusOption.class);
                startActivity(intent);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                */
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
            String stringPendingToAdd = "";
            switch (courseStatus){
                case "OPEN":
                    stringPendingToAdd = getString(R.string.notification_settings_time_status_option_open);
                    break;
                case "FULL":
                    stringPendingToAdd = getString(R.string.notification_settings_time_status_option_full);
                    break;
                case "NewOnly":
                    stringPendingToAdd = getString(R.string.notification_settings_time_status_option_NewOnly);
                    break;
                case "Waitl":
                    stringPendingToAdd = getString(R.string.notification_settings_time_status_option_Waitl);
                    break;
            }
            currentNotifyMeWhenString.append(stringPendingToAdd);
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
                keepNotifyMeLayout.callOnClick();
            }
        });
    }

    private AlertDialog setUpNotifyMeWhenAlertDialog(AlertDialog.Builder alertDialogBuilder){

        View notifyMeWhenView = LayoutInflater.from(context).inflate(R.layout.notification_settings_time_status_option, null);

        final CheckBox checkBoxOpen = (CheckBox) notifyMeWhenView.findViewById(R.id.notification_settings_time_checkbox_open);
        final CheckBox checkBoxWaitl = (CheckBox) notifyMeWhenView.findViewById(R.id.notification_settings_time_checkbox_waitl);
        final CheckBox checkBoxNewOnly = (CheckBox) notifyMeWhenView.findViewById(R.id.notification_settings_time_checkbox_new_only);
        final CheckBox checkBoxFull = (CheckBox) notifyMeWhenView.findViewById(R.id.notification_settings_time_checkbox_full);
        final List<String> finalStatusOptions = new ArrayList<>();

        alertDialogBuilder.setView(notifyMeWhenView);

        List<String> savedStatusOptions = app.getNotificationWhenStatus();

        checkBoxOpen.setChecked(savedStatusOptions.contains(CourseStaticData.defaultClassStatusOpen));
        checkBoxWaitl.setChecked(savedStatusOptions.contains(CourseStaticData.defaultClassStatusWL));
        checkBoxNewOnly.setChecked(savedStatusOptions.contains(CourseStaticData.defaultClassStatusNewOnly));
        checkBoxFull.setChecked(savedStatusOptions.contains(CourseStaticData.defaultClassStatusFull));

        alertDialogBuilder.setPositiveButton(R.string.understand, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(checkBoxOpen.isChecked()){
                    finalStatusOptions.add(CourseStaticData.defaultClassStatusOpen);
                }
                if(checkBoxFull.isChecked()){
                    finalStatusOptions.add(CourseStaticData.defaultClassStatusFull);
                }
                if(checkBoxNewOnly.isChecked()){
                    finalStatusOptions.add(CourseStaticData.defaultClassStatusNewOnly);
                }
                if(checkBoxWaitl.isChecked()){
                    finalStatusOptions.add(CourseStaticData.defaultClassStatusWL);
                }
                app.setAndSaveNotificationWhenStatus(finalStatusOptions);
                onResume();
            }
        });
        alertDialogBuilder.setTitle(R.string.notification_settings_time_current_status_options);
        return alertDialogBuilder.create();
    }

    private AlertDialog setUpCheckingIntervalAlertDialog(AlertDialog.Builder alertDialogBuilder){

        View checkingIntervalView = LayoutInflater.from(context).inflate(R.layout.notification_settings_time_interval, null);

        alertDialogBuilder.setView(checkingIntervalView);

        final RadioGroup radioGroup = (RadioGroup) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_radiogroup);
        RadioButton button0 = (RadioButton) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_1_min);
        RadioButton button1 = (RadioButton) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_3_mins);
        RadioButton button2 = (RadioButton) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_5_mins);
        RadioButton button3 = (RadioButton) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_10_mins);
        RadioButton button4 = (RadioButton) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_15_mins);
        RadioButton button5 = (RadioButton) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_30_mins);
        RadioButton button6 = (RadioButton) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_1_hrs);
        RadioButton button7 = (RadioButton) checkingIntervalView.findViewById(R.id.notification_settings_time_interval_2_hrs);

        if(app.getCheckingInterval() == CourseStaticData.checkingTimeInterval3Min){
            button1.setChecked(true);
        } else if(app.getCheckingInterval() == CourseStaticData.checkingTimeInterval5Min){
            button2.setChecked(true);
        } else if(app.getCheckingInterval() == CourseStaticData.checkingTimeInterval10Min){
            button3.setChecked(true);
        } else if(app.getCheckingInterval() == CourseStaticData.checkingTimeInterval15Min){
            button4.setChecked(true);
        } else if(app.getCheckingInterval() == CourseStaticData.checkingTimeInterval30Min){
            button5.setChecked(true);
        } else if(app.getCheckingInterval() == CourseStaticData.checkingTimeInterval60Min){
            button6.setChecked(true);
        } else if(app.getCheckingInterval() == CourseStaticData.checkingTimeInterval120Min){
            button7.setChecked(true);
        } else {
            button0.setChecked(true);
        }

        alertDialogBuilder.setPositiveButton(R.string.understand, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (radioGroup.getCheckedRadioButtonId()){
                    case R.id.notification_settings_time_interval_1_min:
                        app.setAndSaveCheckingInterval(CourseStaticData.checkingTimeInterval1Min);
                        break;
                    case R.id.notification_settings_time_interval_3_mins:
                        app.setAndSaveCheckingInterval(CourseStaticData.checkingTimeInterval3Min);
                        break;
                    case R.id.notification_settings_time_interval_5_mins:
                        app.setAndSaveCheckingInterval(CourseStaticData.checkingTimeInterval5Min);
                        break;
                    case R.id.notification_settings_time_interval_10_mins:
                        app.setAndSaveCheckingInterval(CourseStaticData.checkingTimeInterval10Min);
                        break;
                    case R.id.notification_settings_time_interval_15_mins:
                        app.setAndSaveCheckingInterval(CourseStaticData.checkingTimeInterval15Min);
                        break;
                    case R.id.notification_settings_time_interval_30_mins:
                        app.setAndSaveCheckingInterval(CourseStaticData.checkingTimeInterval30Min);
                        break;
                    case R.id.notification_settings_time_interval_1_hrs:
                        app.setAndSaveCheckingInterval(CourseStaticData.checkingTimeInterval60Min);
                        break;
                    case R.id.notification_settings_time_interval_2_hrs:
                        app.setAndSaveCheckingInterval(CourseStaticData.checkingTimeInterval120Min);
                        break;
                }
                onResume();
            }
        });
        alertDialogBuilder.setTitle(R.string.notification_settings_time_interval_label);
        return alertDialogBuilder.create();
    }

}
