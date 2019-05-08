package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.jaredrummler.materialspinner.MaterialSpinner;
import com.wang.avi.AVLoadingIndicatorView;

import java.util.ArrayList;
import java.util.List;

public class SearchCourseOption extends AppCompatActivity {
    private MyApp app;
    AVLoadingIndicatorView avLoadingIndicatorView;
    private int currentDeptChoice = 0;
    private int currentQuarterChoice = 0;
    private int currentGeChoice = 0;
    private int currentLevelChoice = 0;
    private List<String> deptList;
    private List<String> quarterList;
    private List<String> geList;
    private List<String> levelList;
    private FloatingActionButton fab;
    private LinearLayout searchOptionLinearLayout;
    private AVLoadingIndicatorView avi;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_course_option);
        setTitle(R.string.start_choose_course_label);
        Toolbar toolbar = (Toolbar) findViewById(R.id.content_main_toolbar);
        app = (MyApp) getApplication();
        searchOptionLinearLayout = (LinearLayout) findViewById(R.id.search_option_linearlayout);
        avi = (AVLoadingIndicatorView) findViewById(R.id.search_option_loading_avi);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        setUpFloatingButton();
        setUpEverything();
    }

    private void setUpEverything() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                startLoadingAnimation();
                while (true){
                    if(app.checkSearchOptionFromMap(CourseStaticData.lastSearchOptionForCheck)){
                        break;
                    }
                }
                endLoadingAnimation();
            }
        }).start();
    }

    private void setUpSpinner() {
        MaterialSpinner quarterSpinner = (MaterialSpinner) findViewById(R.id.quarterSpinner);
        MaterialSpinner geSpinner = (MaterialSpinner) findViewById(R.id.geSpinner);
        MaterialSpinner deptSpinner = (MaterialSpinner) findViewById(R.id.deptSpinner);
        MaterialSpinner levelSpinner = (MaterialSpinner) findViewById(R.id.levelSpinner);
        //refreshLists(deptSpinner);
        processSpinner(quarterSpinner, app.getSearchOptionFromMap("quarter_list"), 1000, getString(R.string.select_hint_name_quarter));
        quarterSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                currentQuarterChoice = position;
            }
        });
        processSpinner(geSpinner, app.getSearchOptionFromMap("ge_list"), 1000, getString(R.string.select_hint_name_ge));
        geSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                currentGeChoice = position;
            }
        });
        processSpinner(deptSpinner, app.getSearchOptionFromMap("dept_list"), 1000, getString(R.string.select_hint_name_dept));
        deptSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                currentDeptChoice = position;
            }
        });
        processSpinner(levelSpinner, app.getSearchOptionFromMap("level_list"), 1000, getString(R.string.select_hint_name_level));
        levelSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                currentLevelChoice = position;
            }
        });

        deptList = app.getSearchOptionFromMap("dept_value_list");
        quarterList = app.getSearchOptionFromMap("quarter_value_list");
        geList = app.getSearchOptionFromMap("ge_value_list");
        levelList = app.getSearchOptionFromMap("level_value_list");
    }

    private void processSpinner(final MaterialSpinner spinner, final List<String> list, final int maxHeight, final String hintName){
        //Log.i("processAdapter", "Processing");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                spinner.setDropdownMaxHeight(maxHeight);
                spinner.setItems(list);
                spinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {

                    @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                        Snackbar.make(view, hintName + getString(R.string.select_hint_name_prefix) + item, Snackbar.LENGTH_SHORT).show();
                    }
                });
                Log.i("Notification", "should be changed with size: " + list.size());
            }
        });
    }

    private void setUpFloatingButton(){
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Integer> tempChoiceListForChecking = new ArrayList<>();
                tempChoiceListForChecking.add(currentGeChoice);
                tempChoiceListForChecking.add(currentDeptChoice);
                tempChoiceListForChecking.add(currentLevelChoice);
                boolean validForNext = false;
                for(Integer choice: tempChoiceListForChecking){
                    if(choice != 0){
                        validForNext = true;
                        break;
                    }
                }
                if(validForNext){
                    goToNextActivity();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(R.drawable.warning);
                    builder.setTitle(getString(R.string.sorry));
                    builder.setMessage(getString(R.string.have_to_choose_at_least_two_or_more_options));
                    builder.setPositiveButton(getString(R.string.understand), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });
                    builder.create().show();
                }
            }
        });
    }

    private void goToNextActivity(){
        Log.i("Document ", "Going to prepare");
        List<String> selectedSearchOption = new ArrayList<>();
        selectedSearchOption.add(quarterList.get(currentQuarterChoice));
        selectedSearchOption.add(geList.get(currentGeChoice));
        selectedSearchOption.add(deptList.get(currentDeptChoice));
        selectedSearchOption.add(levelList.get(currentLevelChoice));
        app.setLastSelectedSearchOption(selectedSearchOption);
        Intent intent = new Intent(SearchCourseOption.this, CourseList.class);
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
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
            // Need work
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startLoadingAnimation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                fab.hide();
                searchOptionLinearLayout.setVisibility(View.GONE);
                avi.smoothToShow();
            }
        });
    }

    private void endLoadingAnimation(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                avi.smoothToHide();
                fab.show();
                searchOptionLinearLayout.setVisibility(View.VISIBLE);
                setUpSpinner();
            }
        });
    }

}
