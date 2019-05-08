package net.donkeyandperi.zotplanner;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CourseListAdapter extends RecyclerView.Adapter<CourseListAdapter.ViewHolder> {

    private List<Course> mCourseList;
    private final MyApp app;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View courseView;
        TextView courseName;
        TextView courseCode;


        public ViewHolder(View view){
            super(view);
            courseView = view;
            courseName = (TextView) view.findViewById(R.id.course_item_course_name);
            courseCode = (TextView) view.findViewById(R.id.course_item_course_code);
        }
    }

    public CourseListAdapter(List<Course> courseList, MyApp app){
        mCourseList = courseList;
        this.app = app;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.courseView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                int position = holder.getAdapterPosition();
                Course course = mCourseList.get(position);
                Snackbar.make(v, "You clicked view: " + course.getCourseName(), Snackbar.LENGTH_SHORT).show();
                Intent intent = new Intent(v.getContext(), CourseDialog.class);
                app.setCurrentSelectedCourseForDialog(course);
                v.getContext().startActivity(intent);
                Activity activity = (Activity) v.getContext();
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Course course = mCourseList.get(position);
        holder.courseName.setText(course.getCourseName());
        holder.courseCode.setText(course.getCourseBasicInfo());
    }

    @Override
    public int getItemCount() {
        return mCourseList.size();
    }
}
