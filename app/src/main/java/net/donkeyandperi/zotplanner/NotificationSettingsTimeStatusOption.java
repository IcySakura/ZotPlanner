package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.CheckBox;

import java.util.ArrayList;
import java.util.List;

public class NotificationSettingsTimeStatusOption extends AppCompatActivity {

    private MyApp app;
    private Context context = this;
    private CheckBox checkBoxOpen;
    private CheckBox checkBoxWaitl;
    private CheckBox checkBoxNewOnly;
    private CheckBox checkBoxFull;
    private List<String> finalStatusOptions = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_settings_time_status_option);
        setTitle(R.string.notification_settings_time_current_status_options);
        app = (MyApp) getApplication();

        checkBoxOpen = (CheckBox) findViewById(R.id.notification_settings_time_checkbox_open);
        checkBoxWaitl = (CheckBox) findViewById(R.id.notification_settings_time_checkbox_waitl);
        checkBoxNewOnly = (CheckBox) findViewById(R.id.notification_settings_time_checkbox_new_only);
        checkBoxFull = (CheckBox) findViewById(R.id.notification_settings_time_checkbox_full);

        app.readNotificationWhenStatus();
        List<String> savedStatusOptions = app.getNotificationWhenStatus();

        checkBoxOpen.setChecked(savedStatusOptions.contains(CourseStaticData.defaultClassStatusOpen));
        checkBoxWaitl.setChecked(savedStatusOptions.contains(CourseStaticData.defaultClassStatusWL));
        checkBoxNewOnly.setChecked(savedStatusOptions.contains(CourseStaticData.defaultClassStatusNewOnly));
        checkBoxFull.setChecked(savedStatusOptions.contains(CourseStaticData.defaultClassStatusFull));
    }

    @Override
    protected void onPause() {
        super.onPause();
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
