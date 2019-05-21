package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

public class Abandoned_NotificationSettingsTimeInterval extends AppCompatActivity {

    private MyApp app;
    private Context context = this;
    private boolean isBeingCheckedByDefault = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_settings_time_interval);
        setTitle(R.string.notification_settings_time_interval_label);
        app = (MyApp) getApplication();
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.notification_settings_time_interval_radiogroup);
        RadioButton button0 = (RadioButton) findViewById(R.id.notification_settings_time_interval_1_min);
        RadioButton button1 = (RadioButton) findViewById(R.id.notification_settings_time_interval_3_mins);
        RadioButton button2 = (RadioButton) findViewById(R.id.notification_settings_time_interval_5_mins);
        RadioButton button3 = (RadioButton) findViewById(R.id.notification_settings_time_interval_10_mins);
        RadioButton button4 = (RadioButton) findViewById(R.id.notification_settings_time_interval_15_mins);
        RadioButton button5 = (RadioButton) findViewById(R.id.notification_settings_time_interval_30_mins);
        RadioButton button6 = (RadioButton) findViewById(R.id.notification_settings_time_interval_1_hrs);
        RadioButton button7 = (RadioButton) findViewById(R.id.notification_settings_time_interval_2_hrs);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
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
                Toast.makeText(context, app.getNotificationSingleCourseList().size() + " course(s) will be checked in every " + String.valueOf(app.getCheckingInterval()) + " minutes", Snackbar.LENGTH_LONG).show();
                if(!isBeingCheckedByDefault){
                    finish();
                }
            }
        });
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
        isBeingCheckedByDefault = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
