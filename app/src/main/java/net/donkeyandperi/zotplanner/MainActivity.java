package net.donkeyandperi.zotplanner;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Handler;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewDisplayable;
import com.alamkanak.weekview.WeekViewEvent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.SubMenu;
import android.view.View;
import com.google.android.material.navigation.NavigationView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yanzhenjie.recyclerview.OnItemMenuClickListener;
import com.yanzhenjie.recyclerview.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.SwipeMenuItem;
import com.yanzhenjie.recyclerview.SwipeRecyclerView;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        WeekView.EventClickListener, MonthLoader.MonthChangeListener,
        WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener{

    private static final String TAG = "MainActivity";
    private Intent intent;
    private MyApp app;
    private SwipeRecyclerView recyclerView;
    private LinearLayout welcomeScreen;
    private FloatingActionButton fab;
    private INotificationService iNotificationService;
    private ServiceConnection notificationServiceConnection;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NavigationView navigationView;
    Context context = this;
    SelectedCourseListAdapter selectedCourseListAdapter;

    private Toolbar toolbar_list_view;
    private Toolbar toolbar_calendar_view;

    // Following variables are for calendar view
    private RelativeLayout calendarViewLayout;
    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 2;
    private static final int TYPE_WEEK_VIEW = 3;
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;
    private WeekView mWeekView;
    private static Handler handler;
    private boolean isFirstTimeRunning = true;
    private SubMenu subMenuOfGoTo;

    //For nav header popup profile menu
    RelativeLayout navHeaderPopupProfileMenu;
    TextView navHeaderPopupProfileTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApp) getApplication();
        app.setLanguage(context);
        setTitle(R.string.app_name);

        setContentView(R.layout.activity_main);
        app.readCachedInstructionBeginAndEndDates();
        selectedCourseListAdapter = new SelectedCourseListAdapter(app);

        recyclerView = (SwipeRecyclerView) findViewById(R.id.selected_course_list_recycler_view);
        welcomeScreen = (LinearLayout) findViewById(R.id.welcome_screen_linearlayout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.selected_course_list_refresh_layout);

        setUpFloatingButton();
        setUpWelcomeScreen();
        setUpRecyclerView();
        setUpSwipeRefreshLayout();
        OperationsWithCourse.refreshLists(app); // Refreshing the list for the activity of SearchCourseOption.
        app.refreshNotificationService(context);

        toolbar_list_view = (Toolbar) findViewById(R.id.toolbar_list_view);
        toolbar_calendar_view = (Toolbar) findViewById(R.id.toolbar_calendar_view);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(selectedCourseListAdapter);

        setUpCalendarView();

        setUpNavHeaderPopupProfileMenu();

        //overridePendingTransition(0, 0);
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_left);
    }

    private void setUpNavHeaderPopupProfileMenu(){
        View navHeaderView = navigationView.getHeaderView(0);
        navHeaderPopupProfileMenu = (RelativeLayout) navHeaderView.findViewById(R.id.nav_header_main_popup_layout);
        navHeaderPopupProfileTextview = (TextView) navHeaderView.findViewById(R.id.nav_header_main_popup_current_profile);
        refreshNavHeaderPopupProfileRelatedViews();
        navHeaderPopupProfileMenu.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.nav_header_popup_profile_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()){
                    case R.id.nav_header_popup_profile_menu_1:
                        app.setCurrentProfileAndNotifyProfileChange(0);
                        refreshNavHeaderPopupProfileRelatedViews();
                        return true;
                    case R.id.nav_header_popup_profile_menu_2:
                        app.setCurrentProfileAndNotifyProfileChange(1);
                        refreshNavHeaderPopupProfileRelatedViews();
                        return true;
                    case R.id.nav_header_popup_profile_menu_3:
                        app.setCurrentProfileAndNotifyProfileChange(2);
                        refreshNavHeaderPopupProfileRelatedViews();
                        return true;
                    case R.id.nav_header_popup_profile_menu_4:
                        app.setCurrentProfileAndNotifyProfileChange(3);
                        refreshNavHeaderPopupProfileRelatedViews();
                        return true;
                    default:
                        return false;

                }
            });
            popupMenu.show();
        });
    }

    private void refreshNavHeaderPopupProfileRelatedViews(){
        switch (app.getCurrentProfile()){
            case 0:
                navHeaderPopupProfileTextview.setText(R.string.profile_1);
                break;
            case 1:
                navHeaderPopupProfileTextview.setText(R.string.profile_2);
                break;
            case 2:
                navHeaderPopupProfileTextview.setText(R.string.profile_3);
                break;
            case 3:
                navHeaderPopupProfileTextview.setText(R.string.profile_4);
                break;
            default:
                navHeaderPopupProfileTextview.setText(R.string.profile_1);
                break;
        }
        onResume();
    }

    private void setUpToolbar(Toolbar toolbar){
        setSupportActionBar(toolbar);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    private void setUpCalendarView(){
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

        // Setting up the main layout to set visibility
        calendarViewLayout = (RelativeLayout) findViewById(R.id.selected_course_calendar_view_layout);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
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
            //pendingIntent = new Intent(MainActivity.this, MainActivity.class);
            app.setCurrentModeInMainActivity(0);
            onResume();
        } else if (id == R.id.calendar_view) {
            //pendingIntent = new Intent(MainActivity.this, Abandoned_SelectedCourseListCalendarView.class);
            app.setCurrentModeInMainActivity(1);
            onResume();
        } else if (id == R.id.my_eee) {
            Toast.makeText(MainActivity.this, getString(R.string.under_development_message), Toast.LENGTH_SHORT).show();
        } else if (id == R.id.main_settings) {
            pendingIntent = new Intent(MainActivity.this, MainSettings.class);
        } else if (id == R.id.donate) {
            Toast.makeText(MainActivity.this, getString(R.string.under_development_message), Toast.LENGTH_SHORT).show();
        } else if(id == R.id.main_settings_about){
            pendingIntent = new Intent(MainActivity.this, AboutSettingsMain.class);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        if(pendingIntent != null){
            startActivity(pendingIntent);
            if(id != R.id.main_settings){
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        }

        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        // The following three lines are for fixing the bug where the real selectedCourseList
        // got changed but it is not updated in the adapter. The fix is a little bit stupid, so
        // fix it if possible!
//        app.readNotificationSingleCourseList();
//        app.readSelectedCourseListData();
//        app.readCachedInstructionBeginAndEndDates();

        Long timeStampForStartOfOnResume = System.currentTimeMillis();

        // Setting up toolbar according to currentMode
        if(app.getCurrentModeInMainActivity() == 0){
            Log.d(TAG, "onResume: Setting up toolbar as list view");
            setUpToolbar(toolbar_list_view);
            toolbar_list_view.setVisibility(View.VISIBLE);
            toolbar_calendar_view.setVisibility(View.GONE);
        } else {
            Log.d(TAG, "onResume: Setting up toolbar as calendar view");
            setUpToolbar(toolbar_calendar_view);
            toolbar_list_view.setVisibility(View.GONE);
            toolbar_calendar_view.setVisibility(View.VISIBLE);
        }

        // Setting up which view should be visible
        if(app.getCurrentModeInMainActivity() == 0){
            calendarViewLayout.setVisibility(View.GONE);
            welcomeScreen.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            calendarViewLayout.setVisibility(View.VISIBLE);
            welcomeScreen.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
        }

        // Setting up different views
        if(app.getCurrentModeInMainActivity() == 0){
            Log.i(TAG, "onResume(0): going to set visibility to gone and visible.");
            welcomeScreen.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

            fab.show();
            if(app.getLastCheckedItemInNavView() != null){
                navigationView.setCheckedItem(app.getLastCheckedItemInNavView());
            } else {
                navigationView.setCheckedItem(R.id.list_view);
            }
            Log.i("FinalIsListChanged ", String.valueOf(app.isSelectedCourseListChanged()));
            processRecyclerView();
            Log.i("FinalSelectedListSize ", String.valueOf(app.getSelectedCourseList().size()));
        } else {
            Menu menu = (Menu) findViewById(R.id.week_view_menu);
            if(app.getLastCheckedItemInNavView() != null){
                navigationView.setCheckedItem(app.getLastCheckedItemInNavView());
            } else {
                navigationView.setCheckedItem(R.id.calendar_view);
            }
            Log.i("onResume ", "Going to run processCalendarView");
            processCalendarView();
            if (!isFirstTimeRunning){
                onCreateOptionsMenu(menu);
                if(app.isSelectedCourseListChanged()){
                    mWeekView.notifyDatasetChanged();
                }
            } else {
                mWeekView.goToHour(8);
            }
        }

        Log.d(TAG, "onResume: the time for onResume to run is(millsec): " + (System.currentTimeMillis() - timeStampForStartOfOnResume));

    }

    private void processRecyclerView() {
        List<Course> selectedCourseList = app.getSelectedCourseList();
        Log.i(TAG, "processRecyclerView: SelectedCourseListSize " + String.valueOf(selectedCourseList.size()));
        if (!selectedCourseList.isEmpty()) {
            swipeRefreshLayout.setEnabled(true);
            if(app.isCourseListRefreshedBySplashActivity()){
                endLoadingAnimationQuickSuccess();
                app.setIsCourseListRefreshedBySplashActivity(false);
                return;
            }
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
                    // Temp
                    Handler handler = new Handler(msg -> {
                        Bundle bundle = msg.getData();
                        if (!bundle.getBoolean("is_success")) {
                            Toast.makeText(context, R.string.error_refreshing_singleCourse, Toast.LENGTH_SHORT).show();
                            return false;
                        }
                        String gsonValue = bundle.getString("course_list");
                        if (gsonValue != null && !gsonValue.isEmpty()) {
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<Course>>() {
                            }.getType();
                            List<Course> newCourses = gson.fromJson(gsonValue, type);
                            //Log.d(TAG, "processRecyclerView: updating " + newCourses.get(0).getCourseName());
                            // Temp
                            app.updateCourseToSelectedCourseList(newCourses.get(0));
                            //Log.i("Notification ", "Changing isSelectedCourseListChanged to false.");
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
            new Thread(() -> {
                startLoadingAnimation();
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
            }).start();
        } else {

            Log.i(TAG, "processRecyclerView: going to set visibility to gone and visible.");
            recyclerView.setVisibility(View.GONE);
            fab.hide();
            //Log.i("processRecyclerView ", "fab is going to hide.");
            welcomeScreen.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setEnabled(false);
        }
    }

    private void startLoadingAnimation() {
        runOnUiThread(() -> {
            if(app.getCurrentModeInMainActivity() == 0){
                //Log.i("Document ", "Going to prepare");
                //avLoadingIndicatorView.smoothToShow();
                swipeRefreshLayout.setRefreshing(true);
            } else {
                //Log.i("Document ", "Going to prepare");
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void endLoadingAnimationQuickSuccess() {
        runOnUiThread(() -> {
            if(app.getCurrentModeInMainActivity() == 0){
                //avLoadingIndicatorView.hide();
                Log.d(TAG, "Before notifying data changed: " + app.getSelectedCourseCodeList());
                List<Course> tempCourseList = app.getSelectedCourseList();
                for (Course course: tempCourseList){
                    Log.d(TAG, "Before notifying data changed(" + course.getCourseName() +
                            "): " + app.getSelectedCourseCodeList());
                }
                selectedCourseListAdapter.notifyDataSetChanged();
                Log.d(TAG, "endLoadingAnimationQuickSuccess: notifyDataSetChanged has been successfully called.");
                app.setIsCurrentlyProcessingSelectedListRecyclerview(false);
                swipeRefreshLayout.setRefreshing(false);
                app.setIsSelectedCourseListChanged(false);
                /*
                for(Course course: app.selectedCourseList){
                    Log.i("CourseCode ", course.getSelectedCourseCodeList().toString());
                }
                */
            } else {
                swipeRefreshLayout.setRefreshing(false);
                mWeekView.notifyDatasetChanged();
            }
        });
    }

    private void setUpFloatingButton() {
        fab = (FloatingActionButton) findViewById(R.id.selected_course_list_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToSearchPage();
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // The reason we do not inflate menu in onCreateOptionsMenu is
        // onCreateOptionsMenu only get called once and will make menu null for the next duplicated call (for unknown reason)
        // duplicated call means mode change in a pattern of 0 -> 1 -> 0 -> 1   (where only mode 1 will get that nullPointerException)
        menu.clear();   // clear the menu every time so no item will be duplicated
        if(app.getCurrentModeInMainActivity() == 0){
            getMenuInflater().inflate(R.menu.list_view_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.calendar_view_menu, menu);
            subMenuOfGoTo = menu.findItem(R.id.week_view_action_go_to_one_event).getSubMenu();
            if(isFirstTimeRunning){
                isFirstTimeRunning = false;
            }
            subMenuOfGoTo.clear();
            subMenuOfGoTo.add(0, R.id.week_view_action_go_to_today, 0,
                    getResources().getString(R.string.selected_course_list_week_view_today));
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
            int numberOfVisableDays = mWeekView.getNumberOfVisibleDays();
            switch (numberOfVisableDays){
                case 1:
                    menu.findItem(R.id.week_view_action_day_view).setChecked(true);
                    break;
                case 3:
                    menu.findItem(R.id.week_view_action_three_day_view).setChecked(true);
                    break;
                case 7:
                    menu.findItem(R.id.week_view_action_week_view).setChecked(true);
                    break;
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if(app.getCurrentModeInMainActivity() == 0){
            if (id == R.id.menu_main_refresh) {
                processRecyclerView();
                return true;
            } else if (id == R.id.clear_all_selected_course) {
                if (app.getSelectedCourseList().isEmpty()) {
                    Snackbar.make(recyclerView, getString(R.string.empty_clear), Snackbar.LENGTH_SHORT).show();
                } else {
                    Snackbar.make(recyclerView, getString(R.string.success_clear), Snackbar.LENGTH_SHORT).show();
                    app.clearSelectedCourseListData();
                    app.clearNotificationSingleCourseList();
                    app.refreshNotificationService(this);
                    onResume();
                }
            } else if (id == R.id.clear_all_notification_single_course) {
                app.clearNotificationSingleCourseList();
                app.refreshNotificationService(this);
            }
        } else {
            setupDateTimeInterpreter(id == R.id.week_view_action_week_view);
            //Log.i("Notification ", "The groupID is: " + item.getGroupId());
            if (item.getGroupId() == 1){
                Course coursePendingGoTo = app.getCourseFromSelectedCourseList(id);
                if(coursePendingGoTo.isFirstInstructionDateSet(id)){
                    //Log.i("Notification ", "Going to date: " + coursePendingGoTo.getFirstInstructionDate(id));
                    mWeekView.goToDate(coursePendingGoTo.getFirstInstructionDate(id));
                }
            } else {
                switch (id){
                    case R.id.week_view_action_go_to_today:
                        mWeekView.goToToday();
                        return true;
                    case R.id.week_view_action_refresh:
                        if(!app.isCurrentlyProcessingSelectedListCalendarview()){
                            app.setIsSelectedCourseListChanged(true);
                            processCalendarView();
                        }
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
        }

        return super.onOptionsItemSelected(item);
    }

    public void setUpWelcomeScreen() {
        final LoadingButton lb = (LoadingButton) findViewById(R.id.loading_btn);
        intent = new Intent(MainActivity.this, SearchCourseOption.class);
        lb.setOnClickListener(view -> {
            lb.startLoading(); //start loading
            goToSearhOptionLB(lb);
        });
        lb.setAnimationEndListener(animationType -> {
            startActivity(intent);
            lb.reset();
        });
    }

    private void goToSearhOptionLB(final LoadingButton lb) {
        // When loading finish, either go to search page or fail
        new Thread(() -> {
            try {
                while (true) {
                    if (app.checkSearchOptionFromMap(CourseStaticData.lastSearchOptionForCheck)) {
                        break;
                    }
                }
                lbEndAnimation(lb, true);
            } catch (Exception e) {
                e.printStackTrace();
                lbEndAnimation(lb, false);
            }
        }).start();
    }

    private void lbEndAnimation(final LoadingButton lb, final boolean judge) {
        // success in loading search page, showing animation
        runOnUiThread(() -> {
            if (judge) {
                lb.loadingSuccessful();
            } else {
                lb.loadingFailed();
                lb.reset();
            }
        });
    }

    private void goToSearchPage() {
        // Going to search page
        runOnUiThread(() -> {
            intent = new Intent(MainActivity.this, SearchCourseOption.class);
            startActivity(intent);
        });
    }

    private void setUpRecyclerView() {
        // For setting up special items in SwipeRecyclerView
        SwipeMenuCreator swipeMenuCreator = (swipeLeftMenu, swipeRightMenu, viewType) -> {
            LayerDrawable layerDrawable = (LayerDrawable) getDrawable(R.drawable.corners_ripple);
            try {
                GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(1);
                gradientDrawable.setColor(getResources().getColor(R.color.primaryBlue));
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            SwipeMenuItem notificationControllerOpener = new SwipeMenuItem(getApplicationContext())
                    .setImage(R.drawable.baseline_notifications_active_24)
                    .setHeight(LinearLayout.LayoutParams.MATCH_PARENT)
                    .setWidth(200)
                    .setBackground(layerDrawable);
            swipeRightMenu.addMenuItem(notificationControllerOpener);
            layerDrawable = (LayerDrawable) getDrawable(R.drawable.corners_ripple);
            try {
                GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(1);
                gradientDrawable.setColor(getResources().getColor(R.color.primaryRed));
            } catch (NullPointerException e){
                e.printStackTrace();
            }
            SwipeMenuItem deleteItem = new SwipeMenuItem(getApplicationContext())
                    .setImage(R.drawable.baseline_delete_24)
                    .setHeight(LinearLayout.LayoutParams.MATCH_PARENT)
                    .setWidth(200)
                    .setBackground(layerDrawable);
            swipeRightMenu.addMenuItem(deleteItem);
        };
        OnItemMenuClickListener swipeMenuItemClickListener = (menuBridge, adapterPosition) -> {
            menuBridge.closeMenu();
            int direction = menuBridge.getDirection(); // Left menu or right menu
            int menuPosition = menuBridge.getPosition(); // The position of the item in recyclerView
            //Log.i("Direction ", String.valueOf(direction));
            //Log.i("AdapterPosistion ", String.valueOf(adapterPosition));
            for(Course course: app.getSelectedCourseList()){
                //Log.i("Selected Course ", course.getCourseName());
            }
            //Log.i("MenuPosition ", String.valueOf(menuPosition));
            if(direction == -1){
                if(menuPosition == 0){
                    if(app.getSelectedCourseList().get(adapterPosition).getCourseElement(app.getSelectedCourseList().get(adapterPosition).courseCodeList.get(0), "Status") == null){
                        Snackbar.make(recyclerView, getString(R.string.summer_class_not_available_for_notification), Snackbar.LENGTH_SHORT).show();
                    } else {
                        app.setCurrentSelectedCourseForNotificationSwitch(app.getSelectedCourseList().get(adapterPosition));
                        startActivity(new Intent(MainActivity.this, NotificationList.class));
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }
                }else if(menuPosition == 1){
                    app.removeCourseFromNotificationSingleCourseList(app.getSelectedCourseList().get(adapterPosition));
                    app.deleteCourseFromSelectedList(adapterPosition);
                    selectedCourseListAdapter.notifyItemRemoved(adapterPosition);
                    //endLoadingAnimationQuickSuccess();  // 临时方法去刷新RecyclerView
                    if (app.getSelectedCourseList().isEmpty()){
                        welcomeScreen.setVisibility(View.VISIBLE);
                        fab.hide();
                        //Log.i("setUpRecyclerView ", "fab is going to hide.");
                    }
                }
            }
        };
        recyclerView.setSwipeMenuCreator(swipeMenuCreator);
        recyclerView.setOnItemMenuClickListener(swipeMenuItemClickListener);
    }

    public void setUpSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if(app.getCurrentModeInMainActivity() == 0){
                if(!app.isCurrentlyProcessingSelectedListRecyclerview()){
                    processRecyclerView();
                }
            } else {
                if(!app.isCurrentlyProcessingSelectedListCalendarview()){
                    app.setIsSelectedCourseListChanged(true);
                    processCalendarView();
                }
            }
        });
        swipeRefreshLayout.setOnChildScrollUpCallback((parent, child) -> {
            if(app.getCurrentModeInMainActivity() == 0){
                return false;
            } else {
                return mWeekView.getFirstVisibleHour() != 0.0;
            }
        });
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

    @SuppressLint("DefaultLocale")
    protected String getEventTitle(Calendar time) {
        return String.format("Event of %02d:%02d %s/%d", time.get(Calendar.HOUR_OF_DAY),
                time.get(Calendar.MINUTE), time.get(Calendar.MONTH)+1, time.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        OperationsWithUI.getDialogForSingleCourse(context, app, new AlertDialog.Builder(context),
                app.getCourseByParsedCourseCode(Integer.parseInt(Long.toString(event.getId()))).getSingleCourse(Long.toString(event.getId())),
                1).show();
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {
        if(app.getCourseByParsedCourseCode(Integer.parseInt(Long.toString(event.getId()))).
                getCourseElement(app.getCourseByParsedCourseCode(Integer.parseInt(Long.toString(event.getId()))).
                        courseCodeList.get(0), "Status") == null){
            Snackbar.make(mWeekView, getString(R.string.summer_class_not_available_for_notification), Snackbar.LENGTH_SHORT).show();
        } else {
            OperationsWithUI.getDialogForNotificationOfCourse(context, app, new AlertDialog.Builder(context),
                    app.getCourseByParsedCourseCode(Integer.parseInt(Long.toString(event.getId()))), 1).show();
        }
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {
        //Toast.makeText(this, "Empty view long pressed: " + getEventTitle(time), Toast.LENGTH_SHORT).show();
    }

    @Override
    public List<WeekViewDisplayable> onMonthChange(final int newYear, final int newMonth) {
        // Populate the week view with some events.

        long timeStampForStartOfOnMonthChange = System.currentTimeMillis();

        boolean isAbleToStart = true;
        for(Course iCourse: app.getSelectedCourseList()){
            if(!iCourse.isDateSet()){
                //Log.i("onMonthChange ", "Oops, we cannot start onMonthChange since a date is not set yet.");
                isAbleToStart = false;
                break;
            }
        }

        List<WeekViewDisplayable> events = new ArrayList<>();

        if(isAbleToStart) {
            //Log.i("onMonthChange ", "We are going to start a month change for: " + newYear + " and month: " + newMonth);
            Random random = new Random();
            //Log.i("Notification ", "newYear: " + newYear + " and newMonth: " + newMonth);
            List<Date> dates;
            String courseTime;
            List<List<Integer>> weekInformation;
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
                    weekInformation = OperationsWithTime.getCourseTimeNumAndWeekNum(courseTime);
                    dates = OperationsWithTime.getAllDatesBetweenRange(
                            iCourse.getInstructionBeginDate(), iCourse.getInstructionEndDate(),
                            weekInformation.get(0).toString(), iCourse, selectedCourseCode);
                    numOfDates = dates.size();
                    for (int i = 0; i < numOfDates; ++i) {
                        startTime = Calendar.getInstance();
                        startTime.setTime(dates.get(i));
                        //Log.i("Notification ", "Showing startTime: " + startTime);
                        //Log.i("Notfication ", "Goign to compar dates " + startTime.get(Calendar.MONTH) + " with " + newMonth + " and " + startTime.get(Calendar.YEAR) + " with " + newYear);
                        if (startTime.get(Calendar.MONTH) + 1 == newMonth && startTime.get(Calendar.YEAR) == newYear) {
                            endTime = Calendar.getInstance();
                            endTime.setTime(dates.get(++i));
                            //Log.i("Notification ", "Going to add a course starts on time: " + dates.get(i - 1) + " and ends on time: " + dates.get(i));
                            WeekViewEvent event = new WeekViewEvent(Integer.parseInt(selectedCourseCode)
                                    , iCourse.getCourseName(), startTime, endTime);
                            event.setColor(courseColor);
                            events.add(event);
                        }
                    }
                    //Log.d(TAG, "onMonthChange: half way there (" + selectedCourseCode + "): " +
                    // (System.currentTimeMillis() - timeStampForStartOfOnMonthChange));
                }
            }
        }
        /*
        Log.d(TAG, "onMonthChange: The time that onMonthChange took to execute is(year=" + newYear +
                ", month=" + newMonth + "): " +
                (System.currentTimeMillis() - timeStampForStartOfOnMonthChange));
                */
        return events;
    }

    private void processCalendarView() {
        List<Course> selectedCourseList = app.getSelectedCourseList();
        Log.i(TAG, "processCalendarView: SelectedCourseListSize: " + String.valueOf(selectedCourseList.size()));
        if (!selectedCourseList.isEmpty() && app.isSelectedCourseListChanged() && !app.isInProgressOfRefreshingSelectedCourseList()) {
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
            new Thread(() -> {
                app.setInProgressOfRefreshingSelectedCourseList(true);
                startLoadingAnimation();
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
                app.setInProgressOfRefreshingSelectedCourseList(false);
                //Log.i("processCalendarView ", "Finished refreshing courseElements data");
                refreshSelectedCourseListForCalendarView();
            }).start();
        } else {
            //Log.i("processCalendarView ", "Not refreshing courseElements data");
            refreshSelectedCourseListForCalendarView();
        }
    }

    public void refreshSelectedCourseListForCalendarView(){
        Log.i(TAG, "refreshSelectedCourseListForCalendarView: Going to refresh the dates");
        if(app.isInProgressOfRefreshingSelectedCourseList()){
            new Thread(() -> {
                //Log.i("refreshSelected ", "Oops, isInProgressOfRefreshingSelectedCourseList, waiting.......");
                if (!app.isInProgressOfRefreshingSelectedCourseList()) {
                    refreshSelectedCourseListForCalendarView();
                }
            }).start();
            return;
        } else{
            final List<OperationsWithCourse.SendRequestForCalendar> threadsForCalendar = new ArrayList<>();
            for(final Course iCourse: app.getSelectedCourseList()){
                if(iCourse.isDateSet()){
                    continue;
                }
                if(app.isDateInCache(iCourse.getSearchOptionYearTerm())){
                    app.updateCourseWithDate(iCourse.getCourseName(), app.getCachedInstructionBeginAndEndDate(iCourse.getSearchOptionYearTerm()));
                } else {
                    //Log.i("refreshSelected ", "A new handler2 will be made for " + iCourse.getCourseName());
                    handler = new Handler(Looper.getMainLooper(), msg -> {
                        //Log.i("handleMessage2 ", "We are getting message!!!");
                        Bundle bundle = msg.getData();
                        boolean isSuccess = bundle.getBoolean("is_success");
                        //Log.i("handleMessage ", "We got the is_success: " + isSuccess);
                        if(isSuccess){
                            String gsonValue = bundle.getString("instruction_begin_and_end_dates");
                            Gson gson = new Gson();
                            Type type = new TypeToken<List<Date>>() {
                            }.getType();
                            List<Date> temp = gson.fromJson(gsonValue, type);
                            //Log.i("handleMessage ", "The quarter start and end time is: " + temp);
                            app.updateCourseWithDate(iCourse.getCourseName(), temp);
                            app.addCachedInstructionBeginAndEndDates(iCourse.getSearchOptionYearTerm(), temp);
                        } else{
                            //Log.i("Notification ", "Failed in refreshSelectedCourseListForCalendarView, trying again!");
                            app.setInProgressOfRefreshingSelectedCourseList(false);
                            refreshSelectedCourseListForCalendarView();
                            return false;
                        }
                        return true;
                    });
                    final OperationsWithCourse.SendRequestForCalendar sendRequestForCalendar = new OperationsWithCourse.SendRequestForCalendar(handler, app, iCourse.getCourseAcademicYearTerm(), iCourse.isSummer(), iCourse.getCourseBeginYear(), iCourse);
                    threadsForCalendar.add(sendRequestForCalendar);
                    sendRequestForCalendar.start();
                }
            }
            new Thread(() -> {
                app.setInProgressOfRefreshingSelectedCourseList(true);
                boolean endFlag = false;
                while (!endFlag) {
                    if(threadsForCalendar.isEmpty()){
                        break;
                    }
                    for (OperationsWithCourse.SendRequestForCalendar thread : threadsForCalendar) {
                        endFlag = !thread.getRunningFlag();
                        if (!endFlag) {
                            break;
                        }
                    }
                    //wait(1000);
                }
                Log.i("refreshForCalendarView ", "finishing refreshing dates");
                app.setInProgressOfRefreshingSelectedCourseList(false);
                endLoadingAnimationQuickSuccess();
            }).start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
