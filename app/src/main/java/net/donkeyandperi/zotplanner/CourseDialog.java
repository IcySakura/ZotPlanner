package net.donkeyandperi.zotplanner;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class CourseDialog extends AppCompatActivity {

    private MyApp app;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_dialog);
        app = (MyApp) getApplication();
        setTitle(app.getCurrentSelectedCourseForDialog().getCourseName());
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.course_dialog_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        CourseDialogAdapter courseDialogAdapter = new CourseDialogAdapter(app.getCurrentSelectedCourseForDialog(), this, app);
        recyclerView.setAdapter(courseDialogAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
