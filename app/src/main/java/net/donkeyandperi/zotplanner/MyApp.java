package net.donkeyandperi.zotplanner;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimerTask;

public class MyApp extends Application {
    private static final String TAG = "MyApp";
    Document regNormalPage = null;
    List<Course> courseList = null;
    Course currentSelectedCourseForDialog = null;
    List<Course> selectedCourseList = new ArrayList<>();
    List<String> lastSelectedSearchOption = null;
    HashMap<String, List<String>> searchOptions = new HashMap<>();
    String currentLanguage = "default";
    Course currentSelectedCourseForNotificationSwitch = null;
    List<SingleCourse> notificationSingleCourseList = new ArrayList<>();
    boolean isSelectedCourseListChanged = true;
    private INotificationService iNotificationService;
    private ServiceConnection notificationServiceConnection;
    private List<String> notificationServiceOnGoingList = new ArrayList<>();
    private HashMap<String, TimerTask> currentSingleCourseCheckingList = new HashMap<>();
    Context context = this;
    private int checkingInterval = 30;
    private boolean inProgressOfRefreshingSelectedCourseList = false;
    private HashMap<String, List<Date>> cachedInstructionBeginAndEndDates = new HashMap<>();
    private List<String> notificationWhenStatus = new ArrayList<>();
    private boolean keepNotifyMeSwitch = false;
    private boolean isCurrentlyProcessingSelectedListRecyclerview = false;
    private Integer lastCheckedItemInNavView = null;
    private Boolean isCourseListRefreshedBySplashActivity = false;
    private Boolean isLanguageJustChanged = false;

    public void setRegNormalPage(Document regNormalPage){
        this.regNormalPage = regNormalPage;
    }

    public Document getRegNormalPage() {
        return regNormalPage;
    }

    public Course getcoursefromselectedcourselist(String courseName){
        for(Course iCourse: selectedCourseList){
            if(iCourse.getCourseName().equals(courseName)){
                return iCourse;
            }
        }
        return null;
    }

    public Course getCourseFromSelectedCourseList(int courseCode){
        for(Course iCourse: selectedCourseList){
            if(iCourse.haveCourseCode(courseCode)){
                return iCourse;
            }
        }
        throw new NullPointerException("We cannot find the course according to the courseCode: " + courseCode);
    }

    public void setCourseList(List<Course> courseList){
        this.courseList = courseList;
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    public void setCurrentSelectedCourseForDialog(Course currentSelectedCourseForDialog) {
        int checkValue = checkIfCourseInCourseList(selectedCourseList, currentSelectedCourseForDialog);
        if(checkValue == -1){
            this.currentSelectedCourseForDialog = currentSelectedCourseForDialog;
        } else {
            this.currentSelectedCourseForDialog = selectedCourseList.get(checkValue);
        }
    }

    public Course getCurrentSelectedCourseForDialog() {
        return currentSelectedCourseForDialog;
    }

    public void setLastSelectedSearchOption(List<String> selectedCourseList) {
        this.lastSelectedSearchOption = selectedCourseList;
    }

    public String getElementFromLastSelectedSearchOption(int index) {
        return lastSelectedSearchOption.get(index);
    }

    public int checkIfCourseInCourseList(List<Course> courseList, Course course){
        for(int i = 0; i < courseList.size(); ++i){
            if(courseList.get(i).getCourseName().equals(course.getCourseName())){
                return i;
            }
        }
        return -1;
    }

    public void addCourseToSelectedCourseList(Course course)
    {
        int checkValue = checkIfCourseInCourseList(selectedCourseList, course);
        Log.i("Notification ", "The checkValue is: " + checkValue);
        if(checkValue == -1)
        {
            isSelectedCourseListChanged = true;
            Log.i("Notification ", "Changing isSelectedCourseListChanged to true.(addCourseToSelectedCourseList)");
            selectedCourseList.add(course);
            saveSelectedCourseListData();
        } else if(selectedCourseList.get(checkValue).getSelectedCourseCodeList() != course.getSelectedCourseCodeList()){
            isSelectedCourseListChanged = true;
            Log.i("Notification ", "Changing isSelectedCourseListChanged to true.(addCourseToSelectedCourseList) Reason: selectedCourseCodeList changed");
            selectedCourseList.get(checkValue).setSelectedCourseCodeList(course.getSelectedCourseCodeList());
            saveSelectedCourseListData();
        }
    }

    public void updateCourseToSelectedCourseList(Course course)
    {
        isSelectedCourseListChanged = true;
        Log.i("Notification ", "Changing isSelectedCourseListChanged to true.(updateCourseToSelectedCourseList)");
        for (Course course_old: selectedCourseList){
            if(course_old.getCourseName().equals(course.getCourseName())){
                Log.i("CourseName ", course.getCourseName());
                Log.i("SizeOfList ", String.valueOf(course.courseList.size()));
                Log.i("SizeOfCourse ", String.valueOf(course.courseCodeList.size()));
                course_old.replaceCourseElementHashMap(course.getCourseCodeList().get(0), course.getCourseCodeHashMap(course.getCourseCodeList().get(0)));
                break;
            }
        }
        //selectedCourseList.add(course);   // if the add of index work, this line is useless
        saveSelectedCourseListData();
    }

    public List<Course> getSelectedCourseList() {
        return selectedCourseList;
    }

    public boolean checkIfSingleCourseInSelectedCourseList(SingleCourse singleCourse){
        for(Course course: selectedCourseList){
            if(course.getSelectedCourseCodeList().contains(singleCourse.getCourseCode()))
            {
                Log.d(TAG, "checkIfSingleCourseInSelectedCourseList: start checking for: " + singleCourse.getCourseCode());
                Log.d(TAG, "checkIfSingleCourseInSelectedCourseList: The list is: " + course.getSelectedCourseCodeList());
                Log.d(TAG, "checkIfSingleCourseInSelectedCourseList: returning True...");
                return true;
            }
        }
        return false;
    }

    public void removeSingleCourseFromSelectedCourseList(SingleCourse singleCourse){
        isSelectedCourseListChanged = true;
        Log.i("Notification ", "Changing isSelectedCourseListChanged to true.(removeSingleCourseFromSelectedCourseList)");
        Log.i("Notification ", "Before Remove (Count of all selectedSingleCourse): " + getSelectedSingleCourseCount());
        for(int i = 0; i < selectedCourseList.size(); ++i){
            if(selectedCourseList.get(i).getSelectedCourseCodeList().contains(singleCourse.getCourseCode())){
                selectedCourseList.get(i).removeSelectedCourse(singleCourse.getCourseCode());
                if(selectedCourseList.get(i).getSelectedCourseCodeList().isEmpty()){
                    selectedCourseList.remove(i);
                }
            }
        }
        saveSelectedCourseListData();
        Log.i("Notification ", "After Remove (Count of all selectedSingleCourse): " + getSelectedSingleCourseCount());
    }

    public int getSelectedSingleCourseCount(){
        int count = 0;
        for(Course iCourse: selectedCourseList){
            count += iCourse.getSelectedCourseCodeList().size();
        }
        return count;
    }

    public void saveData(String mainTag, String subTag, Object object){
        Gson gson = new Gson();
        String dataToBeSaved = gson.toJson(object);
        SharedPreferences.Editor editor = getSharedPreferences(mainTag, MODE_MULTI_PROCESS).edit();
        editor.clear();
        editor.putString(subTag, dataToBeSaved);
        editor.commit();
    }

    public Object readData(String mainTag, String subTag, Type objectType){
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = getSharedPreferences(mainTag, MODE_MULTI_PROCESS);
        String dataToBeRead = sharedPreferences.getString(subTag, "");
        if(!dataToBeRead.isEmpty()){
            return gson.fromJson(dataToBeRead, objectType);
        }
        return null;
    }

    public void saveSelectedCourseListData() {
        saveData("course_related_data", "selected_course_list_data", selectedCourseList);
    }

    public void clearSelectedCourseListData() {
        isSelectedCourseListChanged = true;
        selectedCourseList = new ArrayList<>();
        saveSelectedCourseListData();
    }

    @SuppressWarnings("unchecked")
    public void readSelectedCourseListData(){
        List<Course> tempSelectedCourseList = (List<Course>) readData("course_related_data", "selected_course_list_data", new TypeToken<List<Course>>() {}.getType());
        if(tempSelectedCourseList != null){
            isSelectedCourseListChanged = true;
            selectedCourseList = tempSelectedCourseList;
        }
    }

    public void addSearchOptionToMap(String optionName, List<String> options){
        searchOptions.put(optionName, options);
    }

    public List<String> getSearchOptionFromMap(String optionName){
        return searchOptions.get(optionName);
    }

    public boolean checkSearchOptionFromMap(String optionName){
        return searchOptions.containsKey(optionName);
    }

    public void saveLanguage(String targetLanguage){
        Log.d("Myles", "saveLanguage: The language is going to be saved as: " + targetLanguage);
        saveData("language_settings", "app_language", targetLanguage);
    }

    public String getSavedLanguage(){
        String result = (String)readData("language_settings", "app_language", new TypeToken<String>() {}.getType());
        if(result != null){
            return result;
        }
        return "null";
    }

    public void setLanguage(Context context){
        String targetLanguage = getSavedLanguage();
        Log.d("Myles", "setLanguage: The language is going to be set as: " + targetLanguage);
        if(targetLanguage != null){
            currentLanguage = targetLanguage;
            changeLanguage(targetLanguage, context);
        }
    }

    public void changeLanguage(String targetLanguage, Context context){
        Resources resources = context.getResources();  // 获得res资源对象
        Configuration config = resources.getConfiguration();  // 获得设置对象
        DisplayMetrics dm = resources.getDisplayMetrics();  // 获得屏幕参数：主要是分辨率，像素等。
        Log.d("Myles", "changeLanguage: The language is going to be: " + targetLanguage);
        switch (targetLanguage){
            case "default":
                config.setLocale(Locale.getDefault());
                break;
            case "zh-cn":
                config.setLocale(Locale.SIMPLIFIED_CHINESE);
                break;
            case "en-us":
                config.setLocale(Locale.ENGLISH);
                break;
            case "ja-rJP":
                config.setLocale(Locale.JAPAN);
                break;
            default:
                config.locale = Locale.getDefault();
                break;
        }
        resources.updateConfiguration(config, dm);
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public void deleteCourseFromSelectedList(int position){
        isSelectedCourseListChanged = true;
        Log.i("Notification ", "Changing isSelectedCourseListChanged to true.(deleteCourseFromSelectedList)");
        selectedCourseList.remove(position);
        saveSelectedCourseListData();
    }

    public void setCurrentSelectedCourseForNotificationSwitch(Course courseForNotificationSwitch)
    {
        currentSelectedCourseForNotificationSwitch = courseForNotificationSwitch;
    }

    public Course getCurrentSelectedCourseForNotificationSwitch() {
        return currentSelectedCourseForNotificationSwitch;
    }

    public void addNotificationSingleCourseList(SingleCourse singleCourse){
        for(SingleCourse sc: notificationSingleCourseList){
            if(sc.getCourseCode().equals(singleCourse.getCourseCode())){
                return;
            }
        }
        notificationSingleCourseList.add(singleCourse);
        saveNotificationSingleCourseList();
    }

    public List<SingleCourse> getNotificationSingleCourseList(){
        return notificationSingleCourseList;
    }

    public List<String> getNotificationCourseCodeList(){
        List<String> result = new ArrayList<>();
        for(SingleCourse singleCourse: notificationSingleCourseList){
            result.add(singleCourse.getCourseCode());
        }
        return result;
    }

    public void removeSingleCourseFromNotificationSingleCourseList(SingleCourse singleCourse){
        for(SingleCourse sc: notificationSingleCourseList){
            if(sc.getCourseCode().equals(singleCourse.getCourseCode())){
                notificationSingleCourseList.remove(sc);
                break;
            }
        }
        Log.i("Notfication ", "After removing from NotificationSingleCourseList: " + notificationSingleCourseList.size());
        saveNotificationSingleCourseList();
    }

    public boolean checkSingleCourseInNotificationSingleCourseList(SingleCourse singleCourse){
        for(SingleCourse sc: notificationSingleCourseList){
            if(sc.getCourseCode().equals(singleCourse.getCourseCode())){
                return true;
            }
        }
        return false;
    }

    public void clearNotificationSingleCourseList(){
        if(!notificationSingleCourseList.isEmpty()){
            notificationSingleCourseList.clear();
            saveNotificationSingleCourseList();
        }
    }

    public void removeCourseFromNotificationSingleCourseList(Course course){
        for(SingleCourse singleCourse: course.getSingleCourseList()){
            removeSingleCourseFromNotificationSingleCourseList(singleCourse);
        }
    }

    public void saveNotificationSingleCourseList(){
        saveData("notification_single_course_list", "notification_single_course_list", notificationSingleCourseList);
    }

    @SuppressWarnings("unchecked")
    public void readNotificationSingleCourseList(){
        List<SingleCourse> tempNotificationSingleCourseList = (List<SingleCourse>) readData("notification_single_course_list", "notification_single_course_list", new TypeToken<List<SingleCourse>>() {}.getType());
        if(tempNotificationSingleCourseList != null){
            notificationSingleCourseList = tempNotificationSingleCourseList;
        }
    }

    public void setIsSelectedCourseListChanged(boolean result){
        isSelectedCourseListChanged = result;
    }

    public boolean isSelectedCourseListChanged() {
        return isSelectedCourseListChanged;
    }

    public void refreshNotificationService(Context context){
        Intent serviceStatus = new Intent(this, NotificationService.class);
        Log.i("Notification ", "refreshNotificationService checking whether empty " + notificationSingleCourseList.isEmpty());
        if(notificationSingleCourseList.isEmpty())
        {
            if(notificationServiceConnection != null){
                try{
                    unbindService(notificationServiceConnection);
                } catch (IllegalArgumentException e){
                    e.printStackTrace();
                }
                //....UP....
            }
            stopService(serviceStatus);
        } else {
            //startService(serviceStatus);
            bindNotificationService();
        }
    }

    public void restartNotificationService(){
        Intent serviceStatus = new Intent(this, NotificationService.class);
        if(notificationServiceConnection != null){
            //....Temp solution, should be replaced by a better one....
            try{
                unbindService(notificationServiceConnection);
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }
            //....UP....
        }
        stopService(serviceStatus);
        refreshNotificationService(context);
    }

    private void bindNotificationService(){
        Intent intentService = new Intent(this, NotificationService.class);
        notificationServiceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                iNotificationService = INotificationService.Stub.asInterface(service);
                try {
                    Log.i("Notification ", "We got message: " + iNotificationService.getMessage());
                }catch (RemoteException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.i("Notification ", "Service has been disconnected!");
            }
        };
        startService(intentService);
        bindService(intentService, notificationServiceConnection, Context.BIND_AUTO_CREATE);
    }

    public void addNotificationServiceOnGoingList(String courseCode){
        // For NotificationService use only
        notificationServiceOnGoingList.add(courseCode);
    }

    public void removeNotificationServiceOnGoingList(String courseCode){
        notificationServiceOnGoingList.remove(courseCode);
    }

    public boolean checkNotificationServiceOnGoingList(String courseCode){
        return notificationServiceOnGoingList.contains(courseCode);
    }

    public boolean checkKeyCurrentSingleCourseCheckingList(String courseCode){
        return currentSingleCourseCheckingList.containsKey(courseCode);
    }

    public void addCurrentSingleCourseCheckingList(String courseCode, TimerTask task){
        currentSingleCourseCheckingList.put(courseCode, task);
        Log.i("After adding ", String.valueOf(currentSingleCourseCheckingList.size()));
        Log.i("Notification ", "Adding " + courseCode + " to CurrentSingleCourseCheckingList: " + currentSingleCourseCheckingList.get(courseCode));
    }

    public void removeAndStopCurrentSingleCourseCheckingList(String courseCode){
        Log.i("Before Removing ", String.valueOf(currentSingleCourseCheckingList.size()));
        Log.i("Notification ", "Removing " + courseCode + " from CurrentSingleCourseCheckingList: " + currentSingleCourseCheckingList.get(courseCode));
        Log.i("Notification ", "Before Truly Removing list: " + currentSingleCourseCheckingList.toString() + " and the courseCode to be removed " + courseCode);
        if(currentSingleCourseCheckingList.containsKey(courseCode)){
            Log.i("Notification ", "Going to truly remove " + courseCode);
            currentSingleCourseCheckingList.get(courseCode).cancel();
            currentSingleCourseCheckingList.remove(courseCode);
        }
    }

    public INotificationService getiNotificationService(){
        return iNotificationService;
    }

    public void setAndSaveCheckingInterval(int checkingInterval){
        this.checkingInterval = checkingInterval;
        saveCheckingTimeInterval();
        restartNotificationService();
        /*
        try {
            iNotificationService.setCheckingInterval(this.checkingInterval);    // Not using, should be better than current solution.
        } catch (RemoteException e){
            e.printStackTrace();
        }
        */
    }

    public int getCheckingInterval(){
        return checkingInterval;
    }

    public void saveCheckingTimeInterval(){
        saveData("time_settings", "time_interval", checkingInterval);
    }

    public void readCheckingTimeInterval(){
        Integer tempCheckingInterval = (Integer)readData("time_settings", "time_interval", new TypeToken<Integer>() {}.getType());
        if(tempCheckingInterval == null){
            checkingInterval = CourseStaticData.checkingTimeInterval5Min;
        } else {
            checkingInterval = tempCheckingInterval;
        }
        Log.i("Notification ", "After reading checkingTimeInterval: " + checkingInterval);
    }

    public void setInProgressOfRefreshingSelectedCourseList(boolean isInProgress){
        inProgressOfRefreshingSelectedCourseList = isInProgress;
    }

    public boolean isInProgressOfRefreshingSelectedCourseList(){
        return inProgressOfRefreshingSelectedCourseList;
    }

    public void addCachedInstructionBeginAndEndDates(String searchOptionterm, List<Date> data){
        cachedInstructionBeginAndEndDates.put(searchOptionterm, data);
        saveCachedInstructionBeginAndEndDates();
    }

    public List<Date> getCachedInstructionBeginAndEndDate(String searchOptionterm){
        return cachedInstructionBeginAndEndDates.get(searchOptionterm);
    }

    public boolean isDateInCache(String searchOptionterm){
        return cachedInstructionBeginAndEndDates.containsKey(searchOptionterm);
    }

    public void updateCourseWithDate(String courseName, List<Date> date){
        Course iCourse = getcoursefromselectedcourselist(courseName);
        iCourse.setInstructionBeginDate(date.get(0));
        iCourse.setInstructionEndDate(date.get(1));
        Log.i("updateCourseWithDate ", "Setting " + courseName + " isDateSet to true");
        iCourse.setIsDateSet(true);
    }

    public void saveCachedInstructionBeginAndEndDates(){
        saveData("instruction_begin_and_end_dates", "cached_instruction_begin_and_end_dates", cachedInstructionBeginAndEndDates);
    }

    @SuppressWarnings("unchecked")
    public void readCachedInstructionBeginAndEndDates(){
        HashMap<String, List<Date>> tempCachedInstructionBeginAndEndDates = (HashMap<String, List<Date>>) readData("instruction_begin_and_end_dates", "cached_instruction_begin_and_end_dates", new TypeToken<HashMap<String, List<Date>>>() {}.getType());
        if(tempCachedInstructionBeginAndEndDates != null){
            cachedInstructionBeginAndEndDates = tempCachedInstructionBeginAndEndDates;
        }
    }

    public void setAndSaveNotificationWhenStatus(List<String> nws){
        if(nws.isEmpty()){
            nws.add(CourseStaticData.defaultClassStatusOpen);
            Toast.makeText(context, getText(R.string.notification_settings_time_status_option_cannot_be_empty), Toast.LENGTH_SHORT).show();
        }
        notificationWhenStatus = nws;
        saveNotificationWhenStatus();
        restartNotificationService();
    }

    private void saveNotificationWhenStatus(){
        saveData("notification_status_settings", "notify_me_when", notificationWhenStatus);
    }

    @SuppressWarnings("unchecked")
    public void readNotificationWhenStatus(){
        List<String> tempNotificationWhenStatus = (List<String>)readData("notification_status_settings", "notify_me_when", new TypeToken<List<String>>() {}.getType());
        if(tempNotificationWhenStatus != null){
            notificationWhenStatus.clear();
            notificationWhenStatus = tempNotificationWhenStatus;
        } else {
            notificationWhenStatus.add(CourseStaticData.defaultClassStatusOpen);
        }
    }

    public List<String> getNotificationWhenStatus() {
        readNotificationWhenStatus();
        return notificationWhenStatus;
    }

    public void setAndSaveKeepNotifyMe(boolean knm){
        keepNotifyMeSwitch = knm;
        saveData("keep_notify_me_settings", "keep_notify_me", keepNotifyMeSwitch);
    }

    public void readKeepNotifyMe(){
        Boolean tempKeepNotifyMeSwitch = (Boolean) readData("keep_notify_me_settings", "keep_notify_me", new TypeToken<Boolean>() {}.getType());
        if(tempKeepNotifyMeSwitch != null) {
            keepNotifyMeSwitch = tempKeepNotifyMeSwitch;
        }
    }

    public Boolean isKeepNotifyMe(){
        return keepNotifyMeSwitch;
    }

    public List<String> getSelectedCourseCodeList(){
        List<String> temp = new ArrayList<>();
        for(Course iCourse: selectedCourseList){
            temp.addAll(iCourse.getSelectedCourseCodeList());
        }
        return temp;
    }

    public Course getCourseByParsedCourseCode(Integer parsedCourseCode){
        // 01234 after parsed will be 1234, so we need this method.
        // Will return null if no course found.
        for(Course iCourse: selectedCourseList){
            for(String courseCode: iCourse.getSelectedCourseCodeList()){
                if(Integer.parseInt(courseCode) == parsedCourseCode){
                    return iCourse;
                }
            }
        }
        return null;
    }

    public void setIsCurrentlyProcessingSelectedListRecyclerview(Boolean icpslr){
        isCurrentlyProcessingSelectedListRecyclerview = icpslr;
    }

    public boolean isCurrentlyProcessingSelectedListRecyclerview(){
        return isCurrentlyProcessingSelectedListRecyclerview;
    }

    public void setLastCheckedItemInNavView(Integer lciinv){
        lastCheckedItemInNavView = lciinv;
    }

    public Integer getLastCheckedItemInNavView(){
        return lastCheckedItemInNavView;
    }

    public void setIsCourseListRefreshedBySplashActivity(Boolean clrbsa){
        isCourseListRefreshedBySplashActivity = clrbsa;
    }

    public Boolean isCourseListRefreshedBySplashActivity(){
        return isCourseListRefreshedBySplashActivity;
    }

    public void setIsLanguageJustChanged(Boolean isLanguageJustChanged){
        this.isLanguageJustChanged = isSelectedCourseListChanged;
    }

    public Boolean isLanguageJustChanged(){
        return isLanguageJustChanged;
    }

    public void writeLogToFile(String log, String fileName, String path){
        try {
            File dir = new File(getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/debug_log/"
                    + path);
            dir.mkdirs();
            File tempFile = new File(dir, fileName); //this works
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(tempFile));
            outputStreamWriter.write(log);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e(TAG, "writeLogToFile: File write failed: " + e.toString());
        }
    }

}
