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
import java.util.List;

public class OperationsWithStorage {
    
    private final static String TAG = "OperationsWithStorage";

    public static boolean saveCourseListData(Context context, String accountString, int currentProfile, String fileName, List<Course> courseList){
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

    private static String getGsonString(Object object){
        Gson gson = new Gson();
        return gson.toJson(object);
    }

    private static boolean writeStringToDocumentsFolder(Context context, String path, String fileName, String data){
        boolean is_success = true;
        try {
            File dir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
                    + "/" + path);
            dir.mkdirs();
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
