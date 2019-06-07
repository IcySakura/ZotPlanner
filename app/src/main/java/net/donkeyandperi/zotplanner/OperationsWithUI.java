package net.donkeyandperi.zotplanner;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import net.cachapa.expandablelayout.ExpandableLayout;

import java.util.List;

public class OperationsWithUI {

    private final static String TAG = "OperationsWithUI";

    public static AlertDialog getDialogForNotificationOfCourse(Context context, MyApp app,
                                                               AlertDialog.Builder alertDialogBuilder,
                                                               Course course, Integer whichActivity){
        // whichActivity: 1 means MainActivity, 2 means CourseList

        // Setting up views
        View courseDialogView = LayoutInflater.from(context).inflate(R.layout.notification_dialog, null);
        RecyclerView recyclerView = courseDialogView.findViewById(R.id.notification_dialog_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        NotificationListAdapter notificationListAdapter = new NotificationListAdapter(course, context, app);
        recyclerView.setAdapter(notificationListAdapter);
        alertDialogBuilder.setView(courseDialogView);

        // Setting up alertDialog
        alertDialogBuilder.setTitle(course.getCourseName());
        alertDialogBuilder.setPositiveButton(R.string.understand, (dialog, which) -> {
            if(whichActivity == 1){
                ((MainActivity)context).onResume();
            }
        });
        //alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }

    public static View getViewOfCourseDialogItem(Context context, SingleCourse singleCourse, int forWhat){
        // forWhat is for determining from where to call
        // 1 means it is called to display a singleCourse, so nothing should be clicked, etc.
        // 2 means it is called to display a whole course
        // Setting up views
        // Here the course means singleCourse
        View view = LayoutInflater.from(context).inflate(R.layout.course_dialog_item, null);
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
        RelativeLayout courseMain;
        ExpandableLayout courseSub;
        LinearLayout courseStatusIcon;
        ImageView courseExpandImage;
        TextView courseComments;
        TextView courseFinal;

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
        courseMain = (RelativeLayout) view.findViewById(R.id.course_dialog_item_main);
        courseSub = (ExpandableLayout) view.findViewById(R.id.course_dialog_item_subItem);
        courseStatusIcon = (LinearLayout) view.findViewById(R.id.course_dialog_item_status_icon);
        courseExpandImage = (ImageView) view.findViewById(R.id.course_dialog_item_type_expand_image);
        courseComments = (TextView) view.findViewById(R.id.course_dialog_item_comments_for_singleCourse);
        courseFinal = (TextView) view.findViewById(R.id.course_dialog_item_final);

        List<String> courseElementNameList = singleCourse.getElementNameList();

        courseCode.setText(String.format(context.getString(R.string.course_dialog_code), singleCourse.getCourseCode()));
        courseType.setText(String.format(context.getString(R.string.course_dialog_type), singleCourse.getCourseType()));
        courseSec.setText(String.format(context.getString(R.string.course_dialog_sec), singleCourse.getCourseSection()));
        courseUnits.setText(String.format(context.getString(R.string.course_dialog_units), singleCourse.getCourseUnits()));
        courseInstructor.setText(String.format(context.getString(R.string.course_dialog_instructor), singleCourse.getCourseInstructor()));
        courseTime.setText(String.format(context.getString(R.string.course_dialog_time), singleCourse.getCourseTime()));
        coursePlace.setText(String.format(context.getString(R.string.course_dialog_place), singleCourse.getCoursePlace()));
        courseMax.setText(String.format(context.getString(R.string.course_dialog_max), singleCourse.getCourseMaxPeople()));
        courseEnr.setText(String.format(context.getString(R.string.course_dialog_enr), singleCourse.getCourseEntrance()));
        courseWl.setText(String.format(context.getString(R.string.course_dialog_wl), singleCourse.getCourseWaitlistPeople()));
        courseReq.setText(String.format(context.getString(R.string.course_dialog_req), singleCourse.getCourseRequestPeople()));
        courseNor.setText(String.format(context.getString(R.string.course_dialog_nor), singleCourse.getCourseNormalPeople()));
        courseRstr.setText(String.format(context.getString(R.string.course_dialog_rstr), singleCourse.getCourseRestriction()));
        courseStatus.setText(String.format(context.getString(R.string.course_dialog_status), singleCourse.getCourseStatus()));
        courseFinal.setText(String.format(context.getString(R.string.course_dialog_final), singleCourse.getCourseFinals()));
        int courseStatusColor = context.getResources().getColor(OperationsWithCourse.
                getCourseStatusCorrespondingColor(singleCourse.getCourseStatus(), context));
        courseStatus.setTextColor(courseStatusColor);

        GradientDrawable gradientDrawable = (GradientDrawable) courseStatusIcon.getBackground();
        gradientDrawable.setColor(courseStatusColor);

        if(!singleCourse.getComments().isEmpty()){
            courseComments.setText(String.format(context.getString(R.string.comments), singleCourse.getComments()));
            courseComments.setVisibility(View.VISIBLE);
        }

        if(forWhat == 1){
            addButton.setVisibility(View.GONE);
            courseExpandImage.setVisibility(View.GONE);

            // finish setting up views

            courseSub.setDuration(50);
            view.post(courseSub::expand);
            courseMain.setClickable(false);
            courseSub.setClickable(false);
        }

        return view;
    }

    public static AlertDialog getDialogForSingleCourse(Context context, MyApp app,
                                                       AlertDialog.Builder alertDialogBuilder,
                                                       SingleCourse singleCourse, Integer whichActivity){

        // whichActivity: 1 means MainActivity, 2 means CourseList

        alertDialogBuilder.setView(getViewOfCourseDialogItem(context, singleCourse, 1));

        // Setting up alertDialog
        alertDialogBuilder.setTitle(singleCourse.getCourseName());
        alertDialogBuilder.setPositiveButton(R.string.understand, (dialog, which) -> {

        });

        return alertDialogBuilder.create();
    }

    public static AlertDialog getCourseDialogForCourse(Context context, MyApp app,
                                                       AlertDialog.Builder alertDialogBuilder,
                                                       Course course, Integer whichActivity){

        // whichActivity: 1 means MainActivity, 2 means CourseList

        // Setting up views
        View courseDialogView = LayoutInflater.from(context).inflate(R.layout.course_dialog, null);
        RecyclerView recyclerView = courseDialogView.findViewById(R.id.course_dialog_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(linearLayoutManager);
        CourseDialogAdapter courseDialogAdapter = new CourseDialogAdapter(course, context, app);
        recyclerView.setAdapter(courseDialogAdapter);

        // Setting up comments for the whole course
        if(!course.getComments().isEmpty()){
            RelativeLayout commentsLayout = courseDialogView.findViewById(R.id.course_dialog_course_info_layout);
            TextView textView = courseDialogView.findViewById(R.id.course_dialog_course_info_textView);
            textView.setText(String.format(context.getString(R.string.comments), course.getComments()));
            commentsLayout.setVisibility(View.VISIBLE);
        }

        alertDialogBuilder.setView(courseDialogView);

        // Setting up alertDialog
        alertDialogBuilder.setTitle(course.getCourseName());
        alertDialogBuilder.setPositiveButton(R.string.understand, (dialog, which) -> {
            if(whichActivity == 1){
                ((MainActivity)context).onResume();
            }
        });
        //alertDialogBuilder.setCancelable(false);

        return alertDialogBuilder.create();
    }
}
