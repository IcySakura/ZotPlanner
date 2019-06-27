package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class OperationsWithStorage {
    
    private final static String TAG = "OperationsWithStorage";

    public static boolean saveCourseListData(Context context, String accountString,
                                             int currentProfile, String fileName, List<Course> courseList){
        Gson gson = new Gson();
        String courseListGsonData = gson.toJson(courseList);
        return writeStringToDocumentsFolder(context, accountString + "/" + currentProfile, fileName, courseListGsonData);
    }

    public static List<Course> getCourseListData(Context context, String accountString, int currentProfile, String fileName){
        // Return an empty list if there is no data found in the file
        List<Course> courseList = new ArrayList<>();
        String courseListGsonData = tryReadStringFromFileInDocumentsFolder(context, accountString + "/" + currentProfile, fileName);
        if(!courseListGsonData.isEmpty()){
            Gson gson = new Gson();
            courseList = gson.fromJson(courseListGsonData, new TypeToken<List<Course>>() {}.getType());
        }
        return courseList;
    }

    public static boolean saveUserLanguagePreference(Context context, String accountString, String fileName, String languageData){
        return writeStringToDocumentsFolder(context, accountString, fileName, getGsonString(languageData));
    }

    public static String getUserLanguagePreference(Context context, String accountString, String fileName){
        // Return "null" if nothing found in file
        String result = "null";
        String languageGsonData = tryReadStringFromFileInDocumentsFolder(context, accountString, fileName);
        if(!languageGsonData.isEmpty()){
            Gson gson = new Gson();
            result = gson.fromJson(languageGsonData, new TypeToken<String>(){}.getType());
        }
        return result;
    }

    public static boolean saveNotificationSingleCourseList(Context context, String accountString, String fileName,
                                                           List<SingleCourse> notificationSingleCourseList){
        return writeStringToDocumentsFolder(context, accountString, fileName, getGsonString(notificationSingleCourseList));
    }

    public static List<SingleCourse> getNotificationSingleCourseList(Context context, String accountString, String fileName){
        // Return an empty list if there is no data found in the file
        List<SingleCourse> resultList = new ArrayList<>();
        String notificationSingleCourseListStringData = tryReadStringFromFileInDocumentsFolder(context, accountString, fileName);
        if(!notificationSingleCourseListStringData.isEmpty()){
            Gson gson = new Gson();
            resultList.addAll(gson.fromJson(notificationSingleCourseListStringData, new TypeToken<List<SingleCourse>>(){}.getType()));
        }
        return resultList;
    }

    public static boolean saveCheckingTimeInterval(Context context, String accountString, String fileName, int checkingInterval){
        return writeStringToDocumentsFolder(context, accountString, fileName, getGsonString(checkingInterval));
    }

    public static int getCheckingTimeInterval(Context context, String accountString, String fileName){
        // Return CourseStaticData.checkingTimeInterval5Min if there is no data found in the file
        String resultCheckingIntervalStringData = tryReadStringFromFileInDocumentsFolder(context, accountString, fileName);
        if(!resultCheckingIntervalStringData.isEmpty()) {
            Gson gson = new Gson();
            return gson.fromJson(resultCheckingIntervalStringData, new TypeToken<Integer>(){}.getType());
        }
        return CourseStaticData.checkingTimeInterval5Min;
    }

    public static boolean saveCachedInstructionBeginAndEndDates(Context context, String accountString,
                                                                String fileName, HashMap<String, List<Date>> cachedInstructionBeginAndEndDates){
        return writeStringToDocumentsFolder(context, accountString, fileName, getGsonString(cachedInstructionBeginAndEndDates));
    }

    public static HashMap<String, List<Date>> getCachedInstructionBeginAndEndDates(Context context, String accountString, String fileName){
        // Return empty HashMap if there is no data found in the file
        String resultCachedHashMapStringData = tryReadStringFromFileInDocumentsFolder(context, accountString, fileName);
        if(!resultCachedHashMapStringData.isEmpty()){
            Gson gson = new Gson();
            return gson.fromJson(resultCachedHashMapStringData, new TypeToken<HashMap<String, List<Date>>>(){}.getType());
        }
        return new HashMap<>();
    }

    public static boolean saveNotificationWhenStatus(Context context, String accountString,
                                                     String fileName, List<String> notificationWhenStatus){
        return writeStringToDocumentsFolder(context, accountString, fileName, getGsonString(notificationWhenStatus));
    }

    public static List<String> getNotificationWhenStatus(Context context, String accountString, String fileName){
        // Return empty list if there is no data found in the file
        List<String> resultList = new ArrayList<>();
        String resultListStringData = tryReadStringFromFileInDocumentsFolder(context, accountString, fileName);
        if(!resultListStringData.isEmpty()){
            Gson gson = new Gson();
            resultList.addAll(gson.fromJson(resultListStringData, new TypeToken<List<String>>(){}.getType()));
        }
        return resultList;
    }

    public static boolean saveKeepNotifyMe(Context context, String accountString, String fileName, boolean knm){
        return writeStringToDocumentsFolder(context, accountString, fileName, getGsonString(knm));
    }

    public static boolean getKeepNotifyMe(Context context, String accountString, String fileName){
        // Return false if there is no data found in the file
        boolean resultOfKNM = false;
        String resultOfKNMStringData = tryReadStringFromFileInDocumentsFolder(context, accountString, fileName);
        if(!resultOfKNMStringData.isEmpty()){
            Gson gson = new Gson();
            resultOfKNM = gson.fromJson(resultOfKNMStringData, new TypeToken<Boolean>(){}.getType());
        }
        return resultOfKNM;
    }

    public static boolean saveCurrentProfile(Context context, String accountString, String fileName, int currentProfile){
        return writeStringToDocumentsFolder(context, accountString, fileName, String.valueOf(currentProfile));
    }

    public static int getCurrentProfile(Context context, String accountString, String fileName){
        // Return 0 if there is no data found in the file
        int resultOfCurrentProfile = 0;
        String resultOfCurrentProfileStringData = tryReadStringFromFileInDocumentsFolder(context, accountString, fileName);
        if(!resultOfCurrentProfileStringData.isEmpty()){
            Gson gson = new Gson();
            resultOfCurrentProfile = gson.fromJson(resultOfCurrentProfileStringData, new TypeToken<Integer>(){}.getType());
        }
        return resultOfCurrentProfile;
    }

    private static String getGsonString(Object object){
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    private static boolean writeStringToDocumentsFolder(Context context, String path, String fileName, String data){
        boolean is_success = true;
        try {
            Log.d(TAG, "writeStringToDocumentsFolder: Trying to create path: " + context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    + "/" + path);
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    + "/" + path);
            dir.mkdirs();
            Log.d(TAG, "writeStringToDocumentsFolder: Successfully created: " + context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    + "/" + path);
            File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    + "/" + path, fileName); //this works
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(new FileOutputStream(tempFile));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            is_success = false;
            Log.d(TAG, "writeStringToDocumentsFolder: File write failed: " + e.toString());
        }
        return is_success;
    }

    private static String tryReadStringFromFileInDocumentsFolder(Context context, String path, String fileName){
        // Return the data if there is something; otherwise, return empty string
        StringBuilder data = new StringBuilder();
        data.append("");
        try {
            File tempFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    + "/" + path, fileName); //this works
            FileInputStream fileInputStream = new FileInputStream(tempFile);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            while ((line = bufferedReader.readLine()) != null){
                data.append(line);
            }
            bufferedReader.close();
            fileInputStream.close();
        }catch (IOException e){
            Log.d(TAG, "tryReadStringFromFileInDocumentsFolder: File read failed: " + e.toString());
        }
        return data.toString();
    }
    
}
