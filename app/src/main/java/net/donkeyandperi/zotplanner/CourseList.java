package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class CourseList extends AppCompatActivity {

    private MyApp app;
    private Handler handler;
    CircularProgressIndicator cpiView;
    RecyclerView recyclerView;
    Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.course_list);
        setTitle(R.string.course_list_label);
        app = (MyApp) getApplication();
        recyclerView = (RecyclerView) findViewById(R.id.course_list_recycler_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.course_list_toolbar);
        cpiView = (CircularProgressIndicator) findViewById(R.id.course_list_loading_cpi);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        startLoadingAnimation();

        DividerItemDecoration divider = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this,R.drawable.course_list_rec_line));
        recyclerView.addItemDecoration(divider);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        processorRecyclerView();
    }



    private void processorRecyclerView(){
        List<String> elementList = OperationsWithCourse.getElementListForSearchingCourse(
                app.getElementFromLastSelectedSearchOption(0),
                app.getElementFromLastSelectedSearchOption(1),
                app.getElementFromLastSelectedSearchOption(2),
                app.getElementFromLastSelectedSearchOption(3),
                "",
                CourseStaticData.defaultSearchOptionShowFinals
        );
        handler = new Handler(msg -> {
            Bundle bundle = msg.getData();
            String gsonValue = bundle.getString("course_list");
            if(gsonValue != null && !gsonValue.isEmpty())
            {
                Gson gson = new Gson();
                Type type = new TypeToken<List<Course>>() {}.getType();
                List<Course> temp = gson.fromJson(gsonValue, type);
                app.setCourseList(temp);
                endLoadingAnimationQuickSuccess();
            }else {
                return false;
            }
            return true;
        });
        final OperationsWithCourse.SendRequest sendRequest = new OperationsWithCourse.SendRequest(
                elementList, handler, app.getElementFromLastSelectedSearchOption(0), app);
        new Thread(sendRequest).start();
    }

    private void startLoadingAnimation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Document ", "Going to prepare");
                cpiView.show();
                Log.i("Document ", "Going to prepare");
            }
        });
    }

    private void endLoadingAnimationQuickSuccess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cpiView.hide();
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                CourseListAdapter courseListAdapter = new CourseListAdapter(app.getCourseList(), app);
                recyclerView.setAdapter(courseListAdapter);
            }
        });
    }

    private void endLoadingAnimation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                cpiView.hide();
                Log.i("Document ", "Failed");
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
