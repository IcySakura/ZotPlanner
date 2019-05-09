package net.donkeyandperi.zotplanner;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.Toast;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SelectedCourseListWeekView extends AppCompatActivity implements WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener, NavigationView.OnNavigationItemSelectedListener {
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;
    private MyApp app;
    private static Handler handler;
    private boolean isFirstTimeRunning = true;
    private SubMenu subMenuOfGoTo;
    private Integer newYear = null;
    private Integer newMonth = null;
    private NavigationView navigationView;
    private AVLoadingIndicatorView avi;
    Context context = this;
    ClipData.Item item;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.selected_course_list_calendar_view);
        app = (MyApp) getApplication();
        app.setLanguage(context);
        setTitle(R.string.app_name);

        // Refreshing the list for the activity of SearchCourseOption.
        CourseFunctions.refreshLists(app);

        // Get a reference for the week view in the layout.
        mWeekView = (WeekView) findViewById(R.id.selected_course_list_week_view);


        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        setupDateTimeInterpreter(false);

        Toolbar toolbar = (Toolbar) findViewById(R.id.selected_course_list_week_view_toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.selected_course_list_week_view_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.selected_course_list_week_view_nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        avi = (AVLoadingIndicatorView) findViewById(R.id.selected_course_list_calendar_view_loading_avi);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.selected_course_list_calendar_view_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueToNext();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        Menu menu = (Menu) findViewById(R.id.week_view_menu);
        if(app.getLastCheckedItemInNavView() != null){
            navigationView.setCheckedItem(app.getLastCheckedItemInNavView());
        } else {
            navigationView.setCheckedItem(R.id.calendar_view);
        }
        Log.i("onResume ", "Going to run processWeekView");
        processWeekView();
        if (!isFirstTimeRunning){
            onCreateOptionsMenu(menu);
            if(app.isSelectedCourseListChanged()){
                mWeekView.notifyDatasetChanged();
            }
        } else {
            mWeekView.goToHour(8);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(isFirstTimeRunning){
            getMenuInflater().inflate(R.menu.week_view, menu);
            subMenuOfGoTo = menu.findItem(R.id.week_view_action_go_to_one_event).getSubMenu();
            isFirstTimeRunning = false;
        }
        subMenuOfGoTo.clear();
        subMenuOfGoTo.add(0, R.id.week_view_action_go_to_today, 0, getResources().getString(R.string.selected_course_list_week_view_today));
        for (Course course: app.getSelectedCourseList()){
            for(String courseCode: course.getSelectedCourseCodeList()){
                if(!course.isElementTBA(courseCode, "Time")){
                    subMenuOfGoTo.add(1, Integer.parseInt(courseCode), 1,
                            course.getCourseElement(courseCode, "Type")
                                    + " " + course.getCourseElement(courseCode, "Sec")
                                    + " | " + courseCode);
                }
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        setupDateTimeInterpreter(id == R.id.week_view_action_week_view);
        Log.i("Notification ", "The groupID is: " + item.getGroupId());
        if (item.getGroupId() == 1){
            Course coursePendingGoTo = app.getCourseFromSelectedCourseList(id);
            if(coursePendingGoTo.isFirstInstructionDateSet(id)){
                Log.i("Notification ", "Going to date: " + coursePendingGoTo.getFirstInstructionDate(id));
                mWeekView.goToDate(coursePendingGoTo.getFirstInstructionDate(id));
            }
        } else {
            switch (id){
                case R.id.week_view_action_go_to_today:
                    mWeekView.goToToday();
                    return true;
                case R.id.week_view_action_refresh:
                    app.setIsSelectedCourseListChanged(true);
                    processWeekView();
                    return true;
                case R.id.week_view_action_day_view:
                    if (mWeekViewType != TYPE_DAY_VIEW) {
                        item.setChecked(!item.isChecked());
                        mWeekViewType = TYPE_DAY_VIEW;
                        mWeekView.setNumberOfVisibleDays(1);

                        // Lets change some dimensions to best fit the view.
                        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    }
                    return true;
                case R.id.week_view_action_three_day_view:
                    if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                        item.setChecked(!item.isChecked());
                        mWeekViewType = TYPE_THREE_DAY_VIEW;
                        mWeekView.setNumberOfVisibleDays(3);

                        // Lets change some dimensions to best fit the view.
                        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
                        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12, getResources().getDisplayMetrics()));
                    }
                    return true;
                case R.id.week_view_action_week_view:
                    if (mWeekViewType != TYPE_WEEK_VIEW) {
                        item.setChecked(!item.isChecked());
                        mWeekViewType = TYPE_WEEK_VIEW;
                        mWeekView.setNumberOfVisibleDays(7);

                        // Lets change some dimensions to best fit the view.
                        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
                        mWeekView.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                        mWeekView.setEventTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
                    }
                    return true;
                case R.id.week_view_clear_all_selected_course:
                    if (app.getSelectedCourseList().isEmpty()) {
                        Snackbar.make(getWindow().getDecorView().getRootView(), getString(R.string.empty_clear), Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(getWindow().getDecorView().getRootView(), getString(R.string.success_clear), Snackbar.LENGTH_SHORT).show();
                        app.clearSelectedCourseListData();
                        app.clearNotificationSingleCourseList();
                        app.refreshNotificationService(this);
                        onResume();
                    }
                    return true;
                case R.id.week_view_clear_all_notification_single_course:
                    app.clearNotificationSingleCourseList();
                    app.refreshNotificationService(this);
                    return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     * @param shortDate True if the date values should be short.
     */
    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }

    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE), time.get(Calendar.MONTH)+1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        //Toast.makeText(this, "Clicked " + event.getName(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(SelectedCourseListWeekView.this, CourseDialog.class);
        app.setCurrentSelectedCourseForDialog(app.getCourseByParsedCourseCode(Integer.parseInt(Long.toString(event.getId()))));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        //Toast.makeText(this, "Long pressed event: " + event.getName(), Toast.LENGTH_SHORT).show();
        if(app.getCourseByParsedCourseCode(Integer.parseInt(Long.toString(event.getId()))).getCourseElement(app.getCourseByParsedCourseCode(Integer.parseInt(Long.toString(event.getId()))).courseCodeList.get(0), "Status") == null){
            Snackbar.make(mWeekView, getString(R.string.summer_class_not_available_for_notification), Snackbar.LENGTH_SHORT).show();
        } else {
            app.setCurrentSelectedCourseForNotificationSwitch(app.getCourseByParsedCourseCode(Integer.parseInt(Long.toString(event.getId()))));
            startActivity(new Intent(SelectedCourseListWeekView.this, NotificationList.class));
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        }
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show();
    }

    public WeekView getWeekView() {
        return mWeekView;
    }


    @Override
    public List<WeekViewDisplayable> onMonthChange(final int newYear, final int newMonth) {
        // Populate the week view with some events.
        boolean isAbleToStart = true;
        for(Course iCourse: app.getSelectedCourseList()){
            if(!iCourse.isDateSet()){
                Log.i("onMonthChange ", "Oops, we cannot start onMonthChange since a date is not set yet.");
                isAbleToStart = false;
                break;
            }
        }

        List<WeekViewDisplayable> events = new ArrayList<>();

        if(isAbleToStart) {
            Log.i("onMonthChange ", "We are going to start a month change for: " + newYear + " and month: " + newMonth);
            Random random = new Random();
            Log.i("Notification ", "newYear: " + newYear + " and newMonth: " + newMonth);
            List<Date> dates = new ArrayList<>();
            String courseTime;
            String courseTimeList;
            List<List<Integer>> weekInformation = new ArrayList<>();
            int numOfDates;
            Calendar startTime;
            Calendar endTime;
            int courseColor;
            for (Course iCourse : app.getSelectedCourseList()) {
                for (String selectedCourseCode : iCourse.getSelectedCourseCodeList()) {
                    if (iCourse.isElementTBA(selectedCourseCode, "Time")) {
                        continue;
                    }
                    courseColor = iCourse.getCourseColor(selectedCourseCode);
                    if (courseColor == -1) {
                        courseColor = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                        iCourse.setCourseColor(selectedCourseCode, courseColor);
                    }
                    courseTime = iCourse.getCourseElement(selectedCourseCode, "Time");
                    weekInformation = CourseFunctions.getCourseTimeNumAndWeekNum(courseTime);
                    dates = CourseFunctions.getAllDatesBetweenRange(iCourse.getInstructionBeginDate(), iCourse.getInstructionEndDate(), weekInformation.get(0).toString(), iCourse, selectedCourseCode);
                    numOfDates = dates.size();
                    for (int i = 0; i < numOfDates; ++i) {
                        startTime = Calendar.getInstance();
                        startTime.setTime(dates.get(i));
                        Log.i("Notification ", "Showing startTime: " + startTime);
                        Log.i("Notfication ", "Goign to compar dates " + startTime.get(Calendar.MONTH) + " with " + newMonth + " and " + startTime.get(Calendar.YEAR) + " with " + newYear);
                        if (startTime.get(Calendar.MONTH) + 1 == newMonth && startTime.get(Calendar.YEAR) == newYear) {
                            endTime = Calendar.getInstance();
                            endTime.setTime(dates.get(++i));
                            Log.i("Notification ", "Going to add a course starts on time: " + dates.get(i - 1) + " and ends on time: " + dates.get(i));
                            WeekViewEvent event = new WeekViewEvent(Integer.parseInt(selectedCourseCode), iCourse.getCourseName(), startTime, endTime);
                            event.setColor(courseColor);
                            events.add(event);
                        }
                    }
                }
            }
        }
            /*
            new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean isAbleToStart = false;
                    while (!isAbleToStart){
                        isAbleToStart = true;
                        for(Course iCourse: app.getSelectedCourseList()){
                            isAbleToStart = iCourse.isDateSet();
                            //Log.i("onMonthChange ", iCourse.getCourseName() + " is able to start: " + isAbleToStart);
                            if(!isAbleToStart){
                                break;
                            }
                        }
                    }
                    Log.i("Notification ", "Going to refresh again for year: " + newYear + " and month: " + newMonth);
                    //onMonthChange(newYear, newMonth);
                    mWeekView.notifyDatasetChanged();
                }
            }).run();
            */  // Pending delete if no bugs since this is likely to cause a dead loop on the main thread by preventing OnMonthChange to return.
        return events;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.selected_course_list_week_view_drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        app.setLastCheckedItemInNavView(id);
        Intent pendingIntent = null;

        if (id == R.id.list_view) {
            pendingIntent = new Intent(SelectedCourseListWeekView.this, MainActivity.class);
        } else if (id == R.id.calendar_view) {
            pendingIntent = new Intent(SelectedCourseListWeekView.this, SelectedCourseListWeekView.class);
        } else if (id == R.id.my_eee) {
            Toast.makeText(SelectedCourseListWeekView.this, getString(R.string.under_development_message), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.main_settings) {
            pendingIntent = new Intent(SelectedCourseListWeekView.this, MainSettings.class);
        } else if (id == R.id.donate) {
            Toast.makeText(SelectedCourseListWeekView.this, getString(R.string.under_development_message), Toast.LENGTH_SHORT).show();
        } else if(id == R.id.main_settings_about){
            pendingIntent = new Intent(SelectedCourseListWeekView.this, AboutSettingsMain.class);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.selected_course_list_week_view_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        // Pending added: " | id == R.id.my_eee"
        if(id == R.id.list_view | id == R.id.main_settings | id == R.id.calendar_view | id == R.id.main_settings_about){
            startActivity(pendingIntent);
            if(id != R.id.main_settings){
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }

        return true;
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

    private void endLoadingAnimationQuickSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                avi.smoothToHide();
                mWeekView.notifyDatasetChanged();
            }
        });
    }

    private void processWeekView() {
        List<Course> selectedCourseList = app.getSelectedCourseList();
        Log.i("SelectedCourseListSize ", String.valueOf(selectedCourseList.size()));
        if (!selectedCourseList.isEmpty() && app.isSelectedCourseListChanged() && !app.isInProgressOfRefreshingSelectedCourseList()) {
            final List<CourseFunctions.SendRequest> threads = new ArrayList<>();
            for (final Course course : selectedCourseList) {
                final List<String> selectedCourseCodeList = course.getSelectedCourseCodeList();
                for (final String courseCode : selectedCourseCodeList) {
                    List<String> elementList = new ArrayList<>();
                    elementList.add(course.getSearchOptionYearTerm());
                    elementList.add(CourseStaticData.defaultSearchOptionBreadth);
                    elementList.add(CourseStaticData.defaultSearchOptionDept);
                    elementList.add(CourseStaticData.defaultSearchOptionDivision);
                    elementList.add(courseCode);
                    Log.i("refreshSelected ", "A new handler1 will be made for " + courseCode);
                    handler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            Log.i("handleMessage1 ", "We are getting message!!!");
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
                        }
                    });
                    final CourseFunctions.SendRequest sendRequest = new CourseFunctions.SendRequest(elementList, handler, course.getSearchOptionYearTerm(), app);
                    threads.add(sendRequest);
                    sendRequest.start();
                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    app.setInProgressOfRefreshingSelectedCourseList(true);
                    startLoadingAnimation();
                    boolean endFlag = false;
                    while (!endFlag) {
                        for (CourseFunctions.SendRequest thread : threads) {
                            endFlag = !thread.getRunningFlag();
                            if (!endFlag) {
                                break;
                            }
                        }
                        //wait(1000);
                    }
                    app.setInProgressOfRefreshingSelectedCourseList(false);
                    Log.i("processWeekView ", "Finished refreshing courseElements data");
                    refreshSelectedCourseListForCalendarView(true);
                }
            }).start();
        } else {
            Log.i("processWeekView ", "Not refreshing courseElements data");
            refreshSelectedCourseListForCalendarView(false);
        }
    }

    public void refreshSelectedCourseListForCalendarView(boolean fromWorkerThread){
        Log.i("Notification ", "Going to refresh the dates");
        if(app.isInProgressOfRefreshingSelectedCourseList()){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i("refreshSelected ", "Oops, isInProgressOfRefreshingSelectedCourseList, waiting.......");
                    if(!app.isInProgressOfRefreshingSelectedCourseList()){
                        refreshSelectedCourseListForCalendarView(false);
                    }
                }
            }).run();
        } else{
            final List<CourseFunctions.SendRequestForCalendar> threadsForCalendar = new ArrayList<>();
            for(final Course iCourse: app.getSelectedCourseList()){
                if(iCourse.isDateSet()){
                    continue;
                }
                if(app.isDateInCache(iCourse.getSearchOptionYearTerm())){
                    app.updateCourseWithDate(iCourse.getCourseName(), app.getCachedInstructionBeginAndEndDate(iCourse.getSearchOptionYearTerm()));
                } else {
                    Log.i("refreshSelected ", "A new handler2 will be made for " + iCourse.getCourseName());
                    handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            Log.i("handleMessage2 ", "We are getting message!!!");
                            Bundle bundle = msg.getData();
                            boolean isSuccess = bundle.getBoolean("is_success");
                            Log.i("handleMessage ", "We got the is_success: " + isSuccess);
                            if(isSuccess){
                                String gsonValue = bundle.getString("instruction_begin_and_end_dates");
                                Gson gson = new Gson();
                                Type type = new TypeToken<List<Date>>() {
                                }.getType();
                                List<Date> temp = gson.fromJson(gsonValue, type);
                                Log.i("handleMessage ", "The quarter start and end time is: " + temp);
                                app.updateCourseWithDate(iCourse.getCourseName(), temp);
                                app.addCachedInstructionBeginAndEndDates(iCourse.getSearchOptionYearTerm(), temp);
                            } else{
                                Log.i("Notification ", "Failed in refreshSelectedCourseListForCalendarView, trying again!");
                                app.setInProgressOfRefreshingSelectedCourseList(false);
                                refreshSelectedCourseListForCalendarView(false);
                                return false;
                            }
                            return true;
                        }
                    });
                    final CourseFunctions.SendRequestForCalendar sendRequestForCalendar = new CourseFunctions.SendRequestForCalendar(handler, app, iCourse.getCourseAcademicYearTerm(), iCourse.isSummer(), iCourse.getCourseBeginYear(), iCourse);
                    threadsForCalendar.add(sendRequestForCalendar);
                    sendRequestForCalendar.start();
                }
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    app.setInProgressOfRefreshingSelectedCourseList(true);
                    boolean endFlag = false;
                    while (!endFlag) {
                        if(threadsForCalendar.isEmpty()){
                            break;
                        }
                        for (CourseFunctions.SendRequestForCalendar thread : threadsForCalendar) {
                            endFlag = !thread.getRunningFlag();
                            if (!endFlag) {
                                break;
                            }
                        }
                        //wait(1000);
                    }
                    Log.i("refreshForCalendarView ", "finishing refreshing dates");
                    for(Course iCourse: app.getSelectedCourseList()){
                        Log.i("refreshForCalendarView ", iCourse.getCourseName() + " isDateSet: " + iCourse.isDateSet());
                    }
                    app.setInProgressOfRefreshingSelectedCourseList(false);
                    endLoadingAnimationQuickSuccess();
                }
            }).start();
        }
    }

    private void continueToNext() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SelectedCourseListWeekView.this, SearchCourseOption.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }
}
