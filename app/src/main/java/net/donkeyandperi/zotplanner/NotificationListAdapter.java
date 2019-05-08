package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class NotificationListAdapter extends RecyclerView.Adapter<NotificationListAdapter.ViewHolder>  {

    private List<SingleCourse> mCourseList;
    private Context context;
    private MyApp app;
    private Course selectedCourse;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View courseView;
        Switch notificationSwitch;


        public ViewHolder(View view){
            super(view);
            courseView = view;
            notificationSwitch = (Switch) courseView.findViewById(R.id.notification_list_item_switch);
        }
    }

    public NotificationListAdapter(Course course, Context context, MyApp app){
        mCourseList = course.getSingleCourseList();
        this.context = context;
        this.app = app;
        this.selectedCourse = course;
    }

    @NonNull
    @Override
    public NotificationListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_list_item, parent, false);
        final NotificationListAdapter.ViewHolder holder = new NotificationListAdapter.ViewHolder(view);
        holder.notificationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(holder.notificationSwitch.isChecked()){
                    SingleCourse singleCourse = mCourseList.get(holder.getAdapterPosition());
                    Log.i("Notification ", "You have opened " + singleCourse.getCourseCode() + " switch");
                    app.addNotificationSingleCourseList(singleCourse);
                }
                else {
                    SingleCourse singleCourse = mCourseList.get(holder.getAdapterPosition());
                    Log.i("Notification ", "You have closed " + singleCourse.getCourseCode() + " switch");
                    app.removeSingleCourseFromNotificationSingleCourseList(singleCourse);

                }
                Log.i("Notification ", "Start Printing");
                for(SingleCourse singleCourse1: app.getNotificationSingleCourseList()){
                    Log.i("After change ", singleCourse1.getCourseCode());
                }
                Log.i("Notification ", "End Printing");
                app.refreshNotificationService(v.getContext());
            }
        });
        /*
        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleCourse singleCourse = mCourseList.get(holder.getAdapterPosition());
                Log.i("SizeOfList ", String.valueOf(selectedCourse.courseList.size()));
                Log.i("SizeOfList2 ", String.valueOf(selectedCourse.courseList.get(singleCourse.getCourseCode()).size()));
                selectedCourse.addSelectedCourse(singleCourse.getCourseCode());
                Toast.makeText(v.getContext(), "You have added class: " + singleCourse.getCourseName() + " : " + singleCourse.getCourseCode(), Toast.LENGTH_SHORT).show();
                app.addCourseToSelectedCourseList(selectedCourse);
                app.saveSelectedCourseListData();
                holder.addButton.setClickable(false);
                holder.addButton.setText(context.getString(R.string.cannot_add_the_course));
            }
        });
        */
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationListAdapter.ViewHolder holder, int position) {
        SingleCourse singleCourse = mCourseList.get(position);
        List<String> courseElementNameList = singleCourse.getElementNameList();
        String temp = singleCourse.getCourseElement(courseElementNameList.get(0))
                + "  "
                + singleCourse.getCourseElement(courseElementNameList.get(1))
                + "  "
                + singleCourse.getCourseElement(courseElementNameList.get(5))
                + "  "
                + singleCourse.getCourseElement(courseElementNameList.get(15));
        holder.notificationSwitch.setText(temp);
        if(app.checkSingleCourseInNotificationSingleCourseList(singleCourse)){
            holder.notificationSwitch.setChecked(true);
        }
        /*
        holder.courseCode.setText(String.format(context.getString(R.string.course_dialog_code), singleCourse.getCourseElement(courseElementNameList.get(0))));
        holder.courseType.setText(String.format(context.getString(R.string.course_dialog_type), singleCourse.getCourseElement(courseElementNameList.get(1))));
        holder.courseSec.setText(String.format(context.getString(R.string.course_dialog_sec), singleCourse.getCourseElement(courseElementNameList.get(2))));
        holder.courseUnits.setText(String.format(context.getString(R.string.course_dialog_units), singleCourse.getCourseElement(courseElementNameList.get(3))));
        holder.courseInstructor.setText(String.format(context.getString(R.string.course_dialog_instructor), singleCourse.getCourseElement(courseElementNameList.get(4))));
        holder.courseTime.setText(String.format(context.getString(R.string.course_dialog_time), singleCourse.getCourseElement(courseElementNameList.get(5))));
        holder.coursePlace.setText(String.format(context.getString(R.string.course_dialog_place), singleCourse.getCourseElement(courseElementNameList.get(6))));
        holder.courseMax.setText(String.format(context.getString(R.string.course_dialog_max), singleCourse.getCourseElement(courseElementNameList.get(7))));
        holder.courseEnr.setText(String.format(context.getString(R.string.course_dialog_enr), singleCourse.getCourseElement(courseElementNameList.get(8))));
        holder.courseWl.setText(String.format(context.getString(R.string.course_dialog_wl), singleCourse.getCourseElement(courseElementNameList.get(9))));
        holder.courseReq.setText(String.format(context.getString(R.string.course_dialog_req), singleCourse.getCourseElement(courseElementNameList.get(10))));
        holder.courseNor.setText(String.format(context.getString(R.string.course_dialog_nor), singleCourse.getCourseElement(courseElementNameList.get(11))));
        holder.courseRstr.setText(String.format(context.getString(R.string.course_dialog_rstr), singleCourse.getCourseElement(courseElementNameList.get(12))));
        holder.courseStatus.setText(String.format(context.getString(R.string.course_dialog_status), singleCourse.getCourseElement(courseElementNameList.get(15))));
        if(singleCourse.getCourseElement(courseElementNameList.get(15)).equals(CourseStaticData.defaultClassStatusWL))
        {
            holder.courseStatus.setTextColor(Color.parseColor("#ff0000"));
        } else if (singleCourse.getCourseElement(courseElementNameList.get(15)).equals(CourseStaticData.defaultClassStatusFull))
        {
            holder.courseStatus.setTextColor(Color.parseColor("#000000"));
        } else if (singleCourse.getCourseElement(courseElementNameList.get(15)).equals(CourseStaticData.defaultClassStatusNewOnly))
        {
            holder.courseStatus.setTextColor(Color.parseColor("#198cff"));
        } else if(singleCourse.getCourseElement(courseElementNameList.get(15)).equals(CourseStaticData.defaultClassStatusOpen))
        {
            holder.courseStatus.setTextColor(Color.parseColor("#44ff6a"));
        }
        if(app.checkIfSingleCourseInSelectedCourseList(singleCourse))
        {
            holder.addButton.setClickable(false);
            holder.addButton.setText(context.getString(R.string.cannot_add_the_course));
        }
        */
    }

    @Override
    public int getItemCount() {
        return mCourseList.size();
    }

}
