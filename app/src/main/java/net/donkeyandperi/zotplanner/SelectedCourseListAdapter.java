package net.donkeyandperi.zotplanner;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class SelectedCourseListAdapter extends RecyclerView.Adapter<SelectedCourseListAdapter.ViewHolder> {

    private List<Course> mCourseList;
    private final MyApp app;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View courseView;
        TextView courseName;
        TextView courseCode;


        public ViewHolder(View view){
            super(view);
            courseView = view;
            courseName = (TextView) view.findViewById(R.id.selected_course_item_course_name);
            courseCode = (TextView) view.findViewById(R.id.selected_course_item_course_code);
        }
    }

    public SelectedCourseListAdapter(MyApp app){
        this.app = app;
        mCourseList = this.app.getSelectedCourseList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_course_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.courseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                int position = holder.getAdapterPosition();
                Course course = mCourseList.get(position);
                //Snackbar.make(v, "You clicked view: " + course.getCourseName(), Snackbar.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), CourseDialog.class);
                app.setCurrentSelectedCourseForDialog(course);
                v.getContext().startActivity(intent);
                Activity activity = (Activity) v.getContext();
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        holder.courseView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                int position = holder.getAdapterPosition();
                Course course = mCourseList.get(position);
                if(course.getCourseElement(course.courseCodeList.get(0), "Status") == null){
                    Snackbar.make(view, view.getResources().getString(R.string.summer_class_not_available_for_notification), Snackbar.LENGTH_SHORT).show();
                } else {
                    app.setCurrentSelectedCourseForNotificationSwitch(course);
                    Intent intent = new Intent(view.getContext(), NotificationList.class);
                    view.getContext().startActivity(intent);
                    Activity activity = (Activity) view.getContext();
                    activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    return true;
                }
                return false;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = mCourseList.get(position);
        holder.courseName.setText(course.getCourseName());
        holder.courseCode.setText(course.getSelectedCourseBasicInfo());
    }

    @Override
    public int getItemCount() {
        return mCourseList.size();
    }
}
