package net.donkeyandperi.zotplanner;

import android.app.AlertDialog;
import android.content.Context;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Typeface;
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

public class SelectedCourseListAdapter extends RecyclerView.Adapter<SelectedCourseListAdapter.ViewHolder> {

    private final static String TAG = "SelectedCourseListAdapt";
    private List<Course> mCourseList;
    private MyApp app;

    private RotateAnimation rotateAnimation = null;

    static class ViewHolder extends RecyclerView.ViewHolder{
        View courseView;
        TextView courseName;
        TextView courseCode;
        ExpandableLayout subItem;
        ImageView expandButton;

        ViewHolder(View view){
            super(view);
            courseView = view;
            courseName = (TextView) view.findViewById(R.id.selected_course_item_course_name);
            courseCode = (TextView) view.findViewById(R.id.selected_course_item_course_code);
            subItem = (ExpandableLayout) view.findViewById(R.id.selected_course_item_subItem);
            expandButton = (ImageView) view.findViewById(R.id.selected_course_item_expand_button);
        }

        private void bind(Course course){

        }
    }

    SelectedCourseListAdapter(MyApp app){
        this.app = app;
        mCourseList = this.app.getSelectedCourseList();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.selected_course_list_item, parent, false);
        final ViewHolder holder = new ViewHolder(view);

        holder.subItem.setOnExpansionUpdateListener((expansionFraction, state) -> {
            //Log.d(TAG, "onCreateViewHolder: updating subItem: expansionFraction: " + expansionFraction + ", state: " + state);
            int position = holder.getAdapterPosition();

            // A really stupid method to refresh the selected course list, should be changed later to improve
            // performance of the adapter (also, there is one similar below!)
            //mCourseList = this.app.getSelectedCourseList();

            if(position == -1){
                return;
            }

//            for(Course course: mCourseList){
//                Log.d(TAG, "onCreateViewHolder: showing course(" + course.getCourseName() + "): " +
//                        course.getSelectedCourseCodeList());
//            }
            Log.d(TAG, "onCreateViewHolder: the size of mCourseList is: " + mCourseList.size() +
                    ", and the position is: " + position);

            Course course = mCourseList.get(position);
            switch (state){
                case 0:
                    course.setExpandedOnSelectedCourseList(false);
                    course.setExpandingOnSelectedCourseList(false);
                    // Need to save state of expanded here (or other place which can improve performance if possible)
                    app.updateCourseWithNewExpandedStateInSelectedCourseList(course);
                    break;
                case 1:
                    course.setExpandedOnSelectedCourseList(false);
                    course.setExpandingOnSelectedCourseList(true);
                    break;
                case 2:
                    course.setExpandedOnSelectedCourseList(true);
                    course.setExpandingOnSelectedCourseList(true);
                    // Need to save state of expanded here (or other place which can improve performance if possible)
                    app.updateCourseWithNewExpandedStateInSelectedCourseList(course);
                    break;
                case 3:
                    course.setExpandedOnSelectedCourseList(true);
                    course.setExpandingOnSelectedCourseList(false);
                    break;
            }
        });

        holder.courseView.setOnClickListener(v -> {
            Log.d(TAG, "onCreateViewHolder: expand animation start");
            int position = holder.getAdapterPosition();
            Course course = mCourseList.get(position);
            holder.subItem.toggle();

            int startDegrees = (int)(180 * holder.subItem.getExpansion());
            //Log.d(TAG, "The animation start from degrees: " + startDegrees);
            // Here the animation should be opposite
            if(!course.isExpandedOnSelectedCourseList()){

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
                    if(course.isExpandedOnSelectedCourseList()){
                        Log.d(TAG, "Going to change expandButton Image to state of expanded");
                        holder.expandButton.setImageResource(R.drawable.baseline_expand_less_24);
                    } else {
                        Log.d(TAG, "Going to change expandButton Image to state of collapsed");
                        holder.expandButton.setImageResource(R.drawable.baseline_expand_more_24);
                    }
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            if(course.isExpandingOnSelectedCourseList()){
                holder.expandButton.setAnimation(null);
            }

            holder.expandButton.startAnimation(rotateAnimation);

            //notifyItemChanged(position);
            //Snackbar.make(v, "You clicked view: " + course.getCourseName(), Snackbar.LENGTH_SHORT).show();
            /*
            Intent intent = new Intent(v.getContext(), Abandoned_CourseDialog.class);
            app.setCurrentSelectedCourseForDialog(course);
            v.getContext().startActivity(intent);
            Activity activity = (Activity) v.getContext();
            activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            */
        });

        holder.courseView.setOnLongClickListener(view1 -> {
            int position = holder.getAdapterPosition();
            Course course = null;
            boolean readSuccess = false;
            // Some of the slow devices might got indexOutOfBoundsException when refreshing and clicking happen in the same time
            while (!readSuccess){
                try {
                    course = mCourseList.get(position);
                    readSuccess = true;
                } catch (IndexOutOfBoundsException e){
                    e.printStackTrace();
                }
            }
            if(course == null){
                return false;
            }
            if(course.getCourseElement(course.courseCodeList.get(0), "Status") == null){
                Snackbar.make(view1, view1.getResources().getString(R.string.summer_class_not_available_for_notification), Snackbar.LENGTH_SHORT).show();
            } else {
                OperationsWithUI.getDialogForNotificationOfCourse(view1.getContext(), app,
                        new AlertDialog.Builder(view1.getContext()), course, 1).show();
                /*
                app.setCurrentSelectedCourseForNotificationSwitch(course);
                Intent intent = new Intent(view1.getContext(), NotificationList.class);
                view1.getContext().startActivity(intent);
                Activity activity = (Activity) view1.getContext();
                activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                */
                return true;
            }
            return false;
        });

        return holder;
    }

    private LinearLayout getAllSingleCoursesView(Context context, List<SingleCourse> singleCourseList, int position){
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        linearLayout.setLayoutParams(layoutParams);

        int lengthOfList = singleCourseList.size();
        for(int i = 0; i < lengthOfList; ++i){
            linearLayout.addView(getSingleCourseView(context, singleCourseList.get(i), position));
        }

        // Setting up edit button
        RelativeLayout relativeLayout = new RelativeLayout(context);
        RelativeLayout.LayoutParams editButtonLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        editButtonLayoutParams.bottomMargin = 15;
        relativeLayout.setLayoutParams(editButtonLayoutParams);
        LinearLayout innerLayout = new LinearLayout(context);
        RelativeLayout.LayoutParams innerLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        innerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        innerLayoutParams.topMargin = 20;
        innerLayoutParams.bottomMargin = 20;
        innerLayoutParams.rightMargin = 30;
        innerLayoutParams.leftMargin = 30;
        innerLayout.setLayoutParams(innerLayoutParams);
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
        ImageView editIcon = new ImageView(context);
        editIcon.setImageResource(R.drawable.round_edit_black_48);
        editIcon.setLayoutParams(new LinearLayout.LayoutParams(60, 60));
        TextView textView = new TextView(context);
        textView.setText(R.string.edit);
        textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        innerLayout.addView(editIcon);
        innerLayout.addView(textView);
        relativeLayout.addView(innerLayout);

        // Setting rounded corner
        LayerDrawable layerDrawable = (LayerDrawable) context.getDrawable(R.drawable.corners_ripple);
        try {
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(1);
            gradientDrawable.setColor(context.getResources().getColor(R.color.edit_button_color));
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        relativeLayout.setBackground(layerDrawable);

        // Setting edit button onClickListener
        relativeLayout.setOnClickListener(v -> {
            OperationsWithUI.getCourseDialogForCourse(context, app, new AlertDialog.Builder(context),
                    app.getCourseFromSelectedCourseList(Integer.valueOf(singleCourseList.get(0).getCourseCode())), 1).show();
            /*
            app.setCurrentSelectedCourseForDialog(app.getCourseFromSelectedCourseList(
                    Integer.valueOf(singleCourseList.get(0).getCourseCode())));
            context.startActivity(new Intent(context, Abandoned_CourseDialog.class));
            */
        });

        linearLayout.addView(relativeLayout);

        return linearLayout;
    }

    private RelativeLayout getSingleCourseView(Context context, SingleCourse singleCourse, int position){
        RelativeLayout result = new RelativeLayout(context);

        // Setting rounded corner
        LayerDrawable layerDrawable = (LayerDrawable) context.getDrawable(R.drawable.corners_ripple);
        try {
            GradientDrawable gradientDrawable = (GradientDrawable) layerDrawable.getDrawable(1);
            gradientDrawable.setColor(context.getResources().getColor(R.color.selected_single_course_background));
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        result.setBackground(layerDrawable);

        RelativeLayout.LayoutParams resultLayoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        resultLayoutParams.bottomMargin = 15;
        result.setLayoutParams(resultLayoutParams);
        ImageView imageView = new ImageView(context);
        if(app.getNotificationCourseCodeList().contains(singleCourse.getCourseCode())){
            imageView.setImageResource(R.drawable.baseline_notifications_active_white_48);
        } else {
            imageView.setImageResource(R.drawable.baseline_notifications_off_white_48);
        }
        imageView.setOnClickListener(v -> {
            if(app.getNotificationCourseCodeList().contains(singleCourse.getCourseCode())){
                app.removeSingleCourseFromNotificationSingleCourseList(singleCourse);
            } else {
                app.addNotificationSingleCourseList(singleCourse);
                app.refreshNotificationService(context);
            }
            notifyItemChanged(position);
        });
        RelativeLayout.LayoutParams layoutParamsForImageViewNotification = new RelativeLayout.LayoutParams(110, 110);
        layoutParamsForImageViewNotification.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        layoutParamsForImageViewNotification.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        layoutParamsForImageViewNotification.rightMargin = 20;
        imageView.setLayoutParams(layoutParamsForImageViewNotification);

        // For showing singleCourse
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(20, 20, 20, 20);
        linearLayout.setLayoutParams(layoutParams);

        TextView courseCode = new TextView(context);
        courseCode.setText(String.format(context.getString(R.string.course_dialog_code), singleCourse.getCourseCode()));
        courseCode.setTypeface(courseCode.getTypeface(), Typeface.BOLD);
        linearLayout.addView(courseCode);

        TextView courseType = new TextView(context);
        courseType.setText(String.format(context.getString(R.string.course_dialog_type), singleCourse.getCourseType()));
        linearLayout.addView(courseType);

        /*
        TextView courseSection = new TextView(context);
        courseSection.setText(String.format(context.getString(R.string.course_dialog_sec), singleCourse.getCourseSection()));
        linearLayout.addView(courseSection);

        TextView courseCredit = new TextView(context);
        courseCredit.setText(String.format(context.getString(R.string.course_dialog_units), singleCourse.getCourseUnits()));
        linearLayout.addView(courseCredit);

        TextView courseProfessor = new TextView(context);
        courseProfessor.setText(String.format(context.getString(R.string.course_dialog_instructor), singleCourse.getCourseInstructor()));
        linearLayout.addView(courseProfessor);

        TextView courseTime = new TextView(context);
        courseTime.setText(String.format(context.getString(R.string.course_dialog_time), singleCourse.getCourseTime()));
        linearLayout.addView(courseTime);
        */

        TextView courseStatus = new TextView(context);
        courseStatus.setText(String.format(context.getString(R.string.course_dialog_status), singleCourse.getCourseStatus()));
        courseStatus.setTextColor(context.getResources().getColor(OperationsWithCourse.getCourseStatusCorrespondingColor(singleCourse.getCourseStatus(), context)));
        linearLayout.addView(courseStatus);

        result.addView(linearLayout);
        result.addView(imageView);

        result.setOnClickListener(v -> {
            OperationsWithUI.getDialogForSingleCourse(context, app, new AlertDialog.Builder(context),
                    singleCourse, 1).show();
        });

        return result;
    }

    private void prepareRotateAnimation(int fromDegrees, int toDegrees){
        // Prepare expand animation
        rotateAnimation = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setDuration(200);
        rotateAnimation.setInterpolator(new LinearInterpolator());
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: start");

        // A stupid method to refresh the selected course list, should be changed later to improve
        // performance of the adapter
//        mCourseList = this.app.getSelectedCourseList();

        Course course = mCourseList.get(position);
        holder.courseName.setText(course.getCourseName());

        holder.courseCode.setText(course.getSelectedCourseBasicInfo());
        //holder.bind(course);

        holder.subItem.removeAllViews();
        holder.subItem.addView(getAllSingleCoursesView(holder.courseView.getContext(), course.getSelectedSingleCourseList(), position));
        holder.subItem.post(() -> {
            if(course.isExpandedOnSelectedCourseList()){
                Log.d(TAG, "onBindViewHolder: going to expand course: " + course.getCourseName());
                holder.subItem.expand();
                holder.expandButton.setImageResource(R.drawable.baseline_expand_less_24);
            } else {
                Log.d(TAG, "onBindViewHolder: going to collapse course: " + course.getCourseName());
                holder.subItem.collapse();
                holder.expandButton.setImageResource(R.drawable.baseline_expand_more_24);
            }
        });
        Log.d(TAG, "onBindViewHolder: end");
    }
    @Override
    public int getItemCount() {
        return mCourseList.size();
    }
}
