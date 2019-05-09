package net.donkeyandperi.zotplanner;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotificationService extends Service {

    static final String NOTIFICATION_CHANNEL_ID = "7728463";

    private MyApp app;
    private Timer timer;
    final Context context = this;
    private List<SingleCourse> previousSelectedSingleCourseList = new ArrayList<>();
    private int previousCheckingInterval = -1;

    public NotificationService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return new INotificationService.Stub(){
            @Override
            public String getMessage() throws RemoteException{
                return "Remote Service is being successfully called!";
            }
            @Override
            public void setCheckingInterval(int ci) throws RemoteException{
                //Not using, should be better than current solution.
            }
        };
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Notification ", "Starting notification service...");
        app = (MyApp) getApplication();
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        timer = new Timer(true);
        setUpNotificationChannel();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Notification ", "notification service onStartCommand executed...");
        //return super.onStartCommand(intent, flags, startId);
        Log.i("Notification ", "Start Printing (NotificationService)");
        app.readSelectedCourseListData();
        app.readNotificationWhenStatus();
        app.readKeepNotifyMe();
        Log.i("Notification ", "SelectedCourseListSize: " + String.valueOf(app.getSelectedCourseList().size()));
        setCheckingTimeInterval();
        Log.i("Notification ", "Going to call removeAllDeadTimerTask in onStartCommand.");
        removeAllDeadTimerTask();
        Log.i("Notification ", "Finished to call removeAllDeadTimerTask in onStartCommand.");
        for(final SingleCourse singleCourse: app.getNotificationSingleCourseList()){
            if(!app.checkKeyCurrentSingleCourseCheckingList(singleCourse.getCourseCode()))
            {
                Log.i("After change ", singleCourse.getCourseCode());
                final Handler handler = new Handler(new Handler.Callback() {
                    @Override
                    public boolean handleMessage(Message msg) {
                        Log.i("Notification ", "The notification is configuring course: " + singleCourse.getCourseCode());
                        Bundle bundle = msg.getData();
                        if(bundle.getBoolean("is_success")){
                            String gsonValue = bundle.getString("course_list");
                            if (gsonValue != null && !gsonValue.isEmpty()) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<List<Course>>() {
                                }.getType();
                                List<Course> temp = gson.fromJson(gsonValue, type);
                                NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                                for(String courseStatus: app.getNotificationWhenStatus()){
                                    if(temp.get(0).getCourseElement(temp.get(0).getCourseCodeList().get(0), temp.get(0).getElementNameFromList(15)).equals(courseStatus)){
                                        Log.i("Notification ", "The notification is sending course: " + singleCourse.getCourseCode());
                                        Intent intent = new Intent(context, MainActivity.class);
                                        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                                        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, NotificationService.NOTIFICATION_CHANNEL_ID)
                                                .setContentTitle(String.format(context.getString(R.string.notification_title_for_open_class), CourseFunctions.getCorrespondingCourseStatusString(courseStatus, context)))
                                                .setContentText(String.format(context.getString(R.string.notification_context_for_open_class), singleCourse.getCourseElement("Type"), singleCourse.getCourseElement("Sec"), singleCourse.getCourseCode(), CourseFunctions.getCorrespondingCourseStatusString(courseStatus, context)))
                                                .setSmallIcon(R.drawable.anteater_icon)
                                                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.anteater_icon))
                                                .setContentIntent(pendingIntent)
                                                .setAutoCancel(true)
                                                .setStyle(new NotificationCompat.BigTextStyle().bigText(String.format(context.getString(R.string.notification_big_context_for_open_class), singleCourse.getCourseName(), singleCourse.getCourseCode(), singleCourse.getCourseElement("Type"), singleCourse.getCourseElement("Sec"), singleCourse.getCourseElement("Instructor"), singleCourse.getCourseElement("Place"), singleCourse.getCourseElement("Time"))))
                                                .setDefaults(NotificationCompat.DEFAULT_ALL);
                                        try {
                                            notificationManager.notify(Integer.parseInt(singleCourse.getCourseCode()), notification.build());
                                            if(!app.isKeepNotifyMe()){
                                                Log.i("Notification ", "Entering not keep notify me mode, killing courseCode: " + singleCourse.getCourseCode());
                                                app.removeSingleCourseFromNotificationSingleCourseList(singleCourse);
                                                removeAllDeadTimerTask();
                                                app.refreshNotificationService(context);
                                            }
                                            break;
                                        } catch (NullPointerException e){
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                        if(app.checkNotificationServiceOnGoingList(singleCourse.getCourseCode())){
                            app.removeNotificationServiceOnGoingList(singleCourse.getCourseCode());
                        }
                        Log.i("Notification ", "After removing notification: " + app.checkNotificationServiceOnGoingList(singleCourse.getCourseCode()));
                        return true;
                    }
                });
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        Log.i("Notification ", "Start checking whether " + singleCourse.getCourseCode() + " is being checked: " + app.checkNotificationServiceOnGoingList(singleCourse.getCourseCode()));
                        if(!app.checkNotificationServiceOnGoingList(singleCourse.getCourseCode())){
                            app.addNotificationServiceOnGoingList(singleCourse.getCourseCode());
                            CourseFunctions.SendRequest sendRequest = CourseFunctions.SendNotificationIfSingleCourseMatchStatus(singleCourse, app, context, handler);
                            Log.i("Notification ","The timer task is going to end.");
                        }
                    }
                };
                app.addCurrentSingleCourseCheckingList(singleCourse.getCourseCode(), task);
                Log.i("Notification ", "Scheduling course: " + singleCourse.getCourseCode() + " to be checked every " + app.getCheckingInterval() + " minutes.");
                timer.schedule(task, 1000,app.getCheckingInterval()*60*1000);
            }
        }
        Log.i("Notification ", "End Printing (NotificationService)");
        return START_STICKY;
    }

    private void setUpNotificationChannel(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.notification_channel_name);
            String description = context.getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(NotificationService.NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            try {
                notificationManager.createNotificationChannel(channel);
            }catch (NullPointerException e){
                e.printStackTrace();
            }
        }
    }

    private void removeAllDeadTimerTask(){
        app.readNotificationSingleCourseList();
        if(!previousSelectedSingleCourseList.isEmpty()){
            for(SingleCourse singleCourse: previousSelectedSingleCourseList){
                if(!app.checkSingleCourseInNotificationSingleCourseList(singleCourse)){
                    Log.i("Notification ", "Going to remove courseCode: " + singleCourse.getCourseCode() + " in removeAllDeadTimerTask");
                    app.removeAndStopCurrentSingleCourseCheckingList(singleCourse.getCourseCode());
                }
            }
        }
        previousSelectedSingleCourseList = new ArrayList<>(app.getNotificationSingleCourseList());
        Log.i("Notification ", "Final Size: " + String.valueOf(app.getNotificationSingleCourseList().size()) + " should be equal to: " + previousSelectedSingleCourseList.size());
    }

    private void removeAllTimerTask(){
        for(SingleCourse singleCourse: app.getNotificationSingleCourseList()){
            app.removeAndStopCurrentSingleCourseCheckingList(singleCourse.getCourseCode());
        }
        previousSelectedSingleCourseList.clear();
    }

    private void setCheckingTimeInterval(){
        app.readCheckingTimeInterval();
        if(previousCheckingInterval != app.getCheckingInterval()){
            removeAllTimerTask();
        }
        previousCheckingInterval = app.getCheckingInterval();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeAllDeadTimerTask();
        Log.i("Notification ", "Ending notification service...");
    }
}
