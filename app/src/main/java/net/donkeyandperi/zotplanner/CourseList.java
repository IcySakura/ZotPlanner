package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CourseList extends AppCompatActivity {

    private MyApp app;
    private Handler handler;
    AVLoadingIndicatorView avLoadingIndicatorView;
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
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.course_list_loading_avi);
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
        List<String> elementList = new ArrayList<>();
        elementList.add(app.getElementFromLastSelectedSearchOption(0));
        elementList.add(app.getElementFromLastSelectedSearchOption(1));
        elementList.add(app.getElementFromLastSelectedSearchOption(2));
        elementList.add(app.getElementFromLastSelectedSearchOption(3));
        elementList.add("");
        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
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
            }
        });
        final CourseFunctions.SendRequest sendRequest = new CourseFunctions.SendRequest(elementList, handler, app.getElementFromLastSelectedSearchOption(0), app);
        new Thread(sendRequest).start();
    }

    private void startLoadingAnimation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Document ", "Going to prepare");
                avLoadingIndicatorView.smoothToShow();
                Log.i("Document ", "Going to prepare");
            }
        });
    }

    private void endLoadingAnimationQuickSuccess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                avLoadingIndicatorView.hide();
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
                avLoadingIndicatorView.smoothToHide();
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
