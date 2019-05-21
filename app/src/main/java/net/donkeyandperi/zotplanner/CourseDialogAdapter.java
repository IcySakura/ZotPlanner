package net.donkeyandperi.zotplanner;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

public class CourseDialogAdapter extends RecyclerView.Adapter<CourseDialogAdapter.ViewHolder> {
    // In this class, most courses means singleCourse

    private static final String TAG = "CourseDialogAdapter";
    private List<SingleCourse> mCourseList;
    private Context context;
    private MyApp app;
    private Course selectedCourse;

    private RotateAnimation rotateAnimation = null;

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
        ImageView courseExpandImage;
        TextView courseComments;
        TextView courseFinal;

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
            courseStatusIcon = (LinearLayout) view.findViewById(R.id.course_dialog_item_status_icon);
            courseExpandImage = (ImageView) view.findViewById(R.id.course_dialog_item_type_expand_image);
            courseComments = (TextView) view.findViewById(R.id.course_dialog_item_comments_for_singleCourse);
            courseFinal = (TextView) view.findViewById(R.id.course_dialog_item_final);
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

        holder.courseMain.setOnClickListener(v -> {
            SingleCourse singleCourse = mCourseList.get(holder.getAdapterPosition());

            int startDegrees = (int)(180 * holder.courseSub.getExpansion());
            if(!singleCourse.isExpanedInDialog()){
                Log.d(TAG, "Going to prepare animation: from: " + startDegrees + ", to: " + 180);
                prepareRotateAnimation(startDegrees, 180);
            } else {
                Log.d(TAG, "Going to prepare animation: from: " + (startDegrees - 180) + ", to: " + -180);
                prepareRotateAnimation(startDegrees - 180, -180);
            }
            rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    if(singleCourse.isExpanedInDialog()){
                        holder.courseExpandImage.setImageResource(R.drawable.baseline_expand_less_24);
                    } else {
                        holder.courseExpandImage.setImageResource(R.drawable.baseline_expand_more_24);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            if(singleCourse.isExpanedInDialog()){
                holder.courseExpandImage.setAnimation(null);
            }

            holder.courseExpandImage.startAnimation(rotateAnimation);

            holder.courseSub.toggle();
        });

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
        holder.courseCode.setText(String.format(context.getString(R.string.course_dialog_code), singleCourse.getCourseCode()));
        holder.courseType.setText(String.format(context.getString(R.string.course_dialog_type), singleCourse.getCourseType()));
        holder.courseSec.setText(String.format(context.getString(R.string.course_dialog_sec), singleCourse.getCourseSection()));
        holder.courseUnits.setText(String.format(context.getString(R.string.course_dialog_units), singleCourse.getCourseUnits()));
        holder.courseInstructor.setText(String.format(context.getString(R.string.course_dialog_instructor), singleCourse.getCourseInstructor()));
        holder.courseTime.setText(String.format(context.getString(R.string.course_dialog_time), singleCourse.getCourseTime()));
        holder.coursePlace.setText(String.format(context.getString(R.string.course_dialog_place), singleCourse.getCoursePlace()));
        holder.courseMax.setText(String.format(context.getString(R.string.course_dialog_max), singleCourse.getCourseMaxPeople()));
        holder.courseEnr.setText(String.format(context.getString(R.string.course_dialog_enr), singleCourse.getCourseEntrance()));
        holder.courseWl.setText(String.format(context.getString(R.string.course_dialog_wl), singleCourse.getCourseWaitlistPeople()));
        holder.courseReq.setText(String.format(context.getString(R.string.course_dialog_req), singleCourse.getCourseRequestPeople()));
        holder.courseNor.setText(String.format(context.getString(R.string.course_dialog_nor), singleCourse.getCourseNormalPeople()));
        holder.courseRstr.setText(String.format(context.getString(R.string.course_dialog_rstr), singleCourse.getCourseRestriction()));
        holder.courseStatus.setText(String.format(context.getString(R.string.course_dialog_status), singleCourse.getCourseStatus()));
        holder.courseFinal.setText(String.format(context.getString(R.string.course_dialog_final), singleCourse.getCourseFinals()));
        if(!singleCourse.getComments().isEmpty()){
            holder.courseComments.setText(String.format(context.getString(R.string.comments), singleCourse.getComments()));
            holder.courseComments.setVisibility(View.VISIBLE);
        }
        int courseStatusColor = context.getResources().getColor(OperationsWithCourse.
                getCourseStatusCorrespondingColor(singleCourse.getCourseStatus(), context));
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

    private void prepareRotateAnimation(int fromDegrees, int toDegrees){
        // Prepare expand animation
        rotateAnimation = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setInterpolator(new LinearInterpolator());
    }

    @Override
    public int getItemCount() {
        return mCourseList.size();
    }
}
