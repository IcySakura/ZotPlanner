package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private MyApp app;
    private Context context = this;
    private Handler handler;
    private AVLoadingIndicatorView avi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApp) getApplication();
        app.setLanguage(context);
        setContentView(R.layout.activity_splash);

        avi = (AVLoadingIndicatorView) findViewById(R.id.splash_activity_loading_avi);

        List<Course> selectedCourseList = app.getSelectedCourseList();
        Log.i("SelectedCourseListSize ", String.valueOf(selectedCourseList.size()));
        if (!selectedCourseList.isEmpty()) {
            startLoadingAnimation();
            app.setIsCurrentlyProcessingSelectedListRecyclerview(true);
            final List<OperationsWithCourse.SendRequest> threads = new ArrayList<>();
            for (final Course course : selectedCourseList) {
                final List<String> selectedCourseCodeList = course.getSelectedCourseCodeList();
                for (final String courseCode : selectedCourseCodeList) {
                    List<String> elementList = OperationsWithCourse.getElementListForSearchingCourse(
                            course.getSearchOptionYearTerm(),
                            CourseStaticData.defaultSearchOptionBreadth,
                            CourseStaticData.defaultSearchOptionDept,
                            CourseStaticData.defaultSearchOptionDivision,
                            courseCode,
                            CourseStaticData.defaultSearchOptionShowFinals
                    );
                    handler = new Handler(msg -> {
                        Bundle bundle = msg.getData();
                        String gsonValue = bundle.getString("course_list");
                        if (gsonValue != null && !gsonValue.isEmpty()) {
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<Course>>() {
                            }.getType();
                            List<Course> temp = gson.fromJson(gsonValue, type);
                            // Temp
                            app.updateCourseToSelectedCourseList(temp.get(0));
                            app.setIsSelectedCourseListChanged(false);
                            Log.i("Notification ", "Changing isSelectedCourseListChanged to false.");
                        } else {
                            return false;
                        }
                        return true;
                    });
                    final OperationsWithCourse.SendRequest sendRequest = new OperationsWithCourse.SendRequest(
                            elementList, handler, course.getSearchOptionYearTerm(), app);
                    threads.add(sendRequest);
                    sendRequest.start();
                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean endFlag = false;
                    while (!endFlag) {
                        for (OperationsWithCourse.SendRequest thread : threads) {
                            endFlag = !thread.getRunningFlag();
                            if (!endFlag) {
                                break;
                            }
                        }
                        //wait(1000);
                    }
                    endLoadingAnimationQuickSuccess();
                }
            }).start();
        } else {
            endLoadingAnimationQuickSuccess();
        }

    }

    private void startLoadingAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Document ", "Going to prepare");
                avi.smoothToShow();
            }
        });
    }

    private void endLoadingAnimationQuickSuccess(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //avi.smoothToHide();
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                app.setIsCurrentlyProcessingSelectedListRecyclerview(true);
                app.setIsCourseListRefreshedBySplashActivity(true);
                startActivity(intent);
                //finish();
            }
        });
    }
}
