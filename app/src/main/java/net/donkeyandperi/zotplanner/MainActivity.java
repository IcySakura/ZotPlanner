package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dx.dxloadingbutton.lib.LoadingButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.wang.avi.AVLoadingIndicatorView;
import com.yanzhenjie.recyclerview.swipe.SwipeMenu;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuBridge;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuCreator;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItem;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuItemClickListener;
import com.yanzhenjie.recyclerview.swipe.SwipeMenuRecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private boolean success = false;
    private Intent intent;
    private MyApp app;
    private SwipeMenuRecyclerView recyclerView;
    private AVLoadingIndicatorView avLoadingIndicatorView;
    private Handler handler;
    private LinearLayout welcomeScreen;
    private FloatingActionButton fab;
    private INotificationService iNotificationService;
    private ServiceConnection notificationServiceConnection;
    private SwipeRefreshLayout swipeRefreshLayout;
    private NavigationView navigationView;
    Context context = this;
    SelectedCourseListAdapter selectedCourseListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MyApp) getApplication();
        app.setLanguage(context);
        setTitle(R.string.app_name);

        setContentView(R.layout.activity_main);
        app.readNotificationSingleCourseList();
        app.readSelectedCourseListData();
        app.readCachedInstructionBeginAndEndDates();
        selectedCourseListAdapter = new SelectedCourseListAdapter(app);

        recyclerView = (SwipeMenuRecyclerView) findViewById(R.id.selected_course_list_recycler_view);
        avLoadingIndicatorView = (AVLoadingIndicatorView) findViewById(R.id.selected_course_list_loading_avi);
        welcomeScreen = (LinearLayout) findViewById(R.id.welcome_screen_linearlayout);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.selected_course_list_refresh_layout);

        setUpFloatingButton();
        setUpWelcomeScreen();
        setUpRecyclerView();
        setUpSwipeRefreshLayout();
        CourseFunctions.refreshLists(app);
        //recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        app.refreshNotificationService(context);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.activity_main_drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //overridePendingTransition(0, 0);
        //overridePendingTransition(R.anim.slide_in_left, R.anim.slide_in_left);
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
            pendingIntent = new Intent(MainActivity.this, MainActivity.class);
        } else if (id == R.id.calendar_view) {
            pendingIntent = new Intent(MainActivity.this, SelectedCourseListWeekView.class);
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

        // Pending added: " | id == R.id.my_eee"
        if(id == R.id.list_view | id == R.id.main_settings | id == R.id.calendar_view | id == R.id.main_settings_about){
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
        Log.i("Notification ", "onResume");
        welcomeScreen.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        fab.show();
        Log.i("onResume ", "fab is going to show.");
        if(app.getLastCheckedItemInNavView() != null){
            navigationView.setCheckedItem(app.getLastCheckedItemInNavView());
        } else {
            navigationView.setCheckedItem(R.id.list_view);
        }
        Log.i("FinalIsListChanged ", String.valueOf(app.isSelectedCourseListChanged()));
        if(app.isSelectedCourseListChanged()){
            processorRecyclerView();
        }
        Log.i("FinalSelectedListSize ", String.valueOf(app.getSelectedCourseList().size()));
    }

    private void processorRecyclerView() {
        List<Course> selectedCourseList = app.getSelectedCourseList();
        Log.i("SelectedCourseListSize ", String.valueOf(selectedCourseList.size()));
        if (!selectedCourseList.isEmpty()) {
            if(app.getCourseListRefreshedBySplashActivity()){
                endLoadingAnimationQuickSuccess();
                app.setCourseListRefreshedBySplashActivity(false);
                return;
            }
            app.setIsCurrentlyProcessingSelectedListRecyclerview(true);
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
                    handler = new Handler(new Handler.Callback() {
                        @Override
                        public boolean handleMessage(Message msg) {
                            Bundle bundle = msg.getData();
                            String gsonValue = bundle.getString("course_list");
                            if (gsonValue != null && !gsonValue.isEmpty()) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<List<Course>>() {
                                }.getType();
                                List<Course> temp = gson.fromJson(gsonValue, type);
                                // Temp
                                app.updateCourseToSelectedCourseList(temp.get(0));
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
                    endLoadingAnimationQuickSuccess();
                }
            }).start();
        } else {
            recyclerView.setVisibility(View.GONE);
            fab.hide();
            Log.i("processorRecyclerView ", "fab is going to hide.");
            welcomeScreen.setVisibility(View.VISIBLE);
        }
    }

    private void startLoadingAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("Document ", "Going to prepare");
                //avLoadingIndicatorView.smoothToShow();
                swipeRefreshLayout.setRefreshing(true);
            }
        });
    }

    private void endLoadingAnimationQuickSuccess() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //avLoadingIndicatorView.hide();
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setAdapter(selectedCourseListAdapter);
                app.setIsCurrentlyProcessingSelectedListRecyclerview(false);
                swipeRefreshLayout.setRefreshing(false);
                app.setIsSelectedCourseListChanged(false);
                for(Course course: app.selectedCourseList){
                    Log.i("CourseCode ", course.getSelectedCourseCodeList().toString());
                }
            }
        });
    }

    private void setUpFloatingButton() {
        fab = (FloatingActionButton) findViewById(R.id.selected_course_list_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                continueToNext();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_main_refresh) {
            processorRecyclerView();
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

        return super.onOptionsItemSelected(item);
    }

    public void setUpWelcomeScreen() {
        final LoadingButton lb = (LoadingButton) findViewById(R.id.loading_btn);
        intent = new Intent(MainActivity.this, SearchCourseOption.class);
        lb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lb.startLoading(); //start loading
                goToSearhOptionLB(lb);
            }
        });
        lb.setAnimationEndListener(new LoadingButton.AnimationEndListener() {
            @Override
            public void onAnimationEnd(LoadingButton.AnimationType animationType) {
                startActivity(intent);
                lb.reset();
            }
        });
    }

    private void goToSearhOptionLB(final LoadingButton lb) {
        new Thread(new Runnable() {
            @Override
            public void run() {
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
            }
        }).start();
    }

    private void lbEndAnimation(final LoadingButton lb, final boolean judge) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (judge) {
                    lb.loadingSuccessful();
                    success = true;
                } else {
                    lb.loadingFailed();
                    lb.reset();
                }
            }
        });
    }

    private void continueToNext() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                intent = new Intent(MainActivity.this, SearchCourseOption.class);
                startActivity(intent);
            }
        });
    }

    private void setUpRecyclerView() {
        SwipeMenuCreator swipeMenuCreator = new SwipeMenuCreator() {
            @Override
            public void onCreateMenu(SwipeMenu swipeLeftMenu, SwipeMenu swipeRightMenu, int viewType) {
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
            }
        };
        SwipeMenuItemClickListener swipeMenuItemClickListener = new SwipeMenuItemClickListener() {
            @Override
            public void onItemClick(SwipeMenuBridge menuBridge) {
                menuBridge.closeMenu();
                int direction = menuBridge.getDirection(); // 左侧还是右侧菜单。
                int adapterPosition = menuBridge.getAdapterPosition(); // RecyclerView的Item的position。
                int menuPosition = menuBridge.getPosition(); // 菜单在RecyclerView的Item中的Position。
                Log.i("Direction ", String.valueOf(direction));
                Log.i("AdapterPosistion ", String.valueOf(adapterPosition));
                for(Course course: app.getSelectedCourseList()){
                    Log.i("Selected Course ", course.getCourseName());
                }
                Log.i("MenuPosition ", String.valueOf(menuPosition));
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
                            Log.i("setUpRecyclerView ", "fab is going to hide.");
                        }
                    }
                }
            }
        };
        recyclerView.setSwipeMenuCreator(swipeMenuCreator);
        recyclerView.setSwipeMenuItemClickListener(swipeMenuItemClickListener);
    }

    public void setUpSwipeRefreshLayout(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!app.isCurrentlyProcessingSelectedListRecyclerview()){
                    processorRecyclerView();
                } else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}