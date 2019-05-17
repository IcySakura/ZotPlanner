package net.donkeyandperi.zotplanner;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotificationList extends AppCompatActivity {

    private MyApp app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_dialog);
        setTitle(R.string.notification_list_label);
        app = (MyApp) getApplication();
        app.readNotificationSingleCourseList();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.notification_dialog_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        NotificationListAdapter notificationListAdapter = new NotificationListAdapter(app.getCurrentSelectedCourseForNotificationSwitch(), this, app);
        recyclerView.setAdapter(notificationListAdapter);
        //CourseDialogAdapter courseDialogAdapter = new CourseDialogAdapter(app.getCurrentSelectedCourseForDialog(), this, app);
        //recyclerView.setAdapter(courseDialogAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
