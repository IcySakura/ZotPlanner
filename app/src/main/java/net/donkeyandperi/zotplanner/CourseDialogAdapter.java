package net.donkeyandperi.zotplanner;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

public class CourseDialogAdapter extends RecyclerView.Adapter<CourseDialogAdapter.ViewHolder> {

    private static final String TAG = "CourseDialogAdapter";
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
        RelativeLayout addButton;
        ImageView addButtonImage;
        TextView addButtonText;
        RelativeLayout courseMain;
        ExpandableLayout courseSub;
        LinearLayout courseStatusIcon;

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
            addButton = (RelativeLayout) view.findViewById(R.id.course_dialog_item_addButton);
            addButtonImage = (ImageView) view.findViewById(R.id.course_dialog_item_add_or_remove_imageView);
            addButtonText = (TextView) view.findViewById(R.id.course_dialog_item_add_or_remove_textView);
            courseMain = (RelativeLayout) view.findViewById(R.id.course_dialog_item_main);
            courseSub = (ExpandableLayout) view.findViewById(R.id.course_dialog_item_subItem);
            courseStatusIcon = (LinearLayout) view.findViewById(R.id.course_dialog_status_icon);
        }

        public void changeAddButtonImageAndText(Integer resourceIDForImage, Integer resourceIDForText){
            addButtonImage.setImageResource(resourceIDForImage);
            addButtonText.setText(resourceIDForText);
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

        holder.courseMain.setOnClickListener(v -> holder.courseSub.toggle());

        //holder.courseSub.setOnClickListener(v -> holder.courseSub.toggle());

        // Setting rounded corner for addButton
        LayerDrawable layerDrawable = (LayerDrawable) context.getDrawable(R.drawable.corners_ripple);
        try {
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(1);
            gradientDrawable.setColor(context.getResources().getColor(R.color.selected_single_course_background));
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        holder.addButton.setBackground(layerDrawable);

        holder.courseSub.setOnExpansionUpdateListener((expansionFraction, state) -> {
            SingleCourse singleCourse = mCourseList.get(holder.getAdapterPosition());
            Log.d(TAG, "onExpansionUpdate: expansionFraction is: " + expansionFraction);
            switch (state){
                case 0:
                    singleCourse.setExpanedInDialog(false);
                    break;
                case 1:
                    singleCourse.setExpanedInDialog(false);
                    break;
                case 2:
                    singleCourse.setExpanedInDialog(true);
                    break;
                case 3:
                    singleCourse.setExpanedInDialog(true);
                    break;
            }
        });

        holder.addButton.setOnClickListener(v -> {
            SingleCourse singleCourse = mCourseList.get(holder.getAdapterPosition());
            if(app.checkIfSingleCourseInSelectedCourseList(singleCourse)){
                app.removeSingleCourseFromSelectedCourseList(singleCourse);
                holder.changeAddButtonImageAndText(R.drawable.round_add_circle_outline_black_36, R.string.add_the_course);
            }
            else {
                selectedCourse.addSelectedCourse(singleCourse.getCourseCode());
                app.addCourseToSelectedCourseList(selectedCourse);
                app.saveSelectedCourseListData();
                holder.changeAddButtonImageAndText(R.drawable.round_remove_circle_outline_black_36, R.string.remove_single_course);
            }
            app.setIsSelectedCourseListChanged(true);
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
        int courseStatusColor = context.getResources().getColor(CourseFunctions.
                getCourseStatusCorrespondingColor(singleCourse.getCourseElement(courseElementNameList.get(15)), context));
        holder.courseStatus.setTextColor(courseStatusColor);

        GradientDrawable gradientDrawable = (GradientDrawable) holder.courseStatusIcon.getBackground();
        gradientDrawable.setColor(courseStatusColor);

        // Weird action might happen if not have else (some classes' buttons will be remove where they are not added)
        if(app.checkIfSingleCourseInSelectedCourseList(singleCourse))
        {
            holder.changeAddButtonImageAndText(R.drawable.round_remove_circle_outline_black_36, R.string.remove_single_course);
        } else {
            holder.changeAddButtonImageAndText(R.drawable.round_add_circle_outline_black_36, R.string.add_the_course);
        }

        if(singleCourse.isExpanedInDialog()){
            holder.courseSub.expand(false);
        } else {
            holder.courseSub.collapse(false);
        }


    }

    @Override
    public int getItemCount() {
        return mCourseList.size();
    }
}
