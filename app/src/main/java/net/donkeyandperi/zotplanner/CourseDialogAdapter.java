package net.donkeyandperi.zotplanner;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CourseDialogAdapter extends RecyclerView.Adapter<CourseDialogAdapter.ViewHolder> {

    private List<SingleCourse> mCourseList;
    private Context context;
    private MyApp app;
    private Course selectedCourse;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View courseView;
        TextView courseCode;
        TextView courseType;
        TextView courseSec;
        TextView courseUnits;
        TextView courseInstructor;
        TextView courseTime;
        TextView coursePlace;
        TextView courseMax;
        TextView courseEnr;
        TextView courseWl;
        TextView courseReq;
        TextView courseNor;
        TextView courseRstr;
        TextView courseStatus;
        Button addButton;


        public ViewHolder(View view){
            super(view);
            courseView = view;
            courseCode = (TextView) view.findViewById(R.id.course_dialog_item_code);
            courseType = (TextView) view.findViewById(R.id.course_dialog_item_type);
            courseSec = (TextView) view.findViewById(R.id.course_dialog_item_sec);
            courseUnits = (TextView) view.findViewById(R.id.course_dialog_item_units);
            courseInstructor = (TextView) view.findViewById(R.id.course_dialog_item_instructor);
            courseTime = (TextView) view.findViewById(R.id.course_dialog_item_time);
            coursePlace = (TextView) view.findViewById(R.id.course_dialog_item_place);
            courseMax = (TextView) view.findViewById(R.id.course_dialog_item_max);
            courseEnr = (TextView) view.findViewById(R.id.course_dialog_item_enr);
            courseWl = (TextView) view.findViewById(R.id.course_dialog_item_wl);
            courseReq = (TextView) view.findViewById(R.id.course_dialog_item_req);
            courseNor = (TextView) view.findViewById(R.id.course_dialog_item_nor);
            courseRstr = (TextView) view.findViewById(R.id.course_dialog_item_rstr);
            courseStatus = (TextView) view.findViewById(R.id.course_dialog_item_status);
            addButton = (Button) view.findViewById(R.id.course_dialog_item_addButton);
        }
    }

    public CourseDialogAdapter(Course course, Context context, MyApp app){
        mCourseList = course.getSingleCourseList();
        this.context = context;
        this.app = app;
        this.selectedCourse = course;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.course_dialog_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SingleCourse singleCourse = mCourseList.get(holder.getAdapterPosition());
                if(app.checkIfSingleCourseInSelectedCourseList(singleCourse)){
                    app.removeSingleCourseFromSelectedCourseList(singleCourse);
                    holder.addButton.setText(context.getString(R.string.add_the_course));
                }
                else {
                    selectedCourse.addSelectedCourse(singleCourse.getCourseCode());
                    Toast.makeText(v.getContext(), "You have added class: " + singleCourse.getCourseName() + " : " + singleCourse.getCourseCode(), Toast.LENGTH_SHORT).show();
                    app.addCourseToSelectedCourseList(selectedCourse);
                    app.saveSelectedCourseListData();
                    holder.addButton.setText(context.getString(R.string.remove_single_course));
                }
                app.setIsSelectedCourseListChanged(true);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SingleCourse singleCourse = mCourseList.get(position);
        List<String> courseElementNameList = singleCourse.getElementNameList();
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
        holder.courseStatus.setTextColor(CourseFunctions.getCourseStatusCorrespondingColor(singleCourse.getCourseElement(courseElementNameList.get(15)), context));
        /*
        if(singleCourse.getCourseElement(courseElementNameList.get(15)) == null){
            holder.courseStatus.setTextColor(context.getResources().getColor(R.color.course_status_null_color));
        } else if(singleCourse.getCourseElement(courseElementNameList.get(15)).equals(CourseStaticData.defaultClassStatusWL))
        {
            holder.courseStatus.setTextColor(context.getResources().getColor(R.color.course_status_wait_list_color));
        } else if (singleCourse.getCourseElement(courseElementNameList.get(15)).equals(CourseStaticData.defaultClassStatusFull))
        {
            holder.courseStatus.setTextColor(context.getResources().getColor(R.color.course_status_full_color));
        } else if (singleCourse.getCourseElement(courseElementNameList.get(15)).equals(CourseStaticData.defaultClassStatusNewOnly))
        {
            holder.courseStatus.setTextColor(context.getResources().getColor(R.color.course_status_new_only_color));
        } else if(singleCourse.getCourseElement(courseElementNameList.get(15)).equals(CourseStaticData.defaultClassStatusOpen))
        {
            holder.courseStatus.setTextColor(context.getResources().getColor(R.color.course_status_open_color));
        }
        */
        if(app.checkIfSingleCourseInSelectedCourseList(singleCourse))
        {
            holder.addButton.setText(context.getString(R.string.remove_single_course));
        }
    }

    @Override
    public int getItemCount() {
        return mCourseList.size();
    }
}
