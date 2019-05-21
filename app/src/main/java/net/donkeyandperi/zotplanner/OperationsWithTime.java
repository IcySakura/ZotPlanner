package net.donkeyandperi.zotplanner;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OperationsWithTime {

    private final static String TAG = "OperationsWithTime";

    static String getEngForWeekDay(String num) {
        switch (num) {
            case "1":
                return "Su";
            case "2":
                return "M";
            case "3":
                return "Tu";
            case "4":
                return "W";
            case "5":
                return "Th";
            case "6":
                return "F";
            default:
                return "Sa";
        }
    }

    public static List<List<Integer>> getCourseTimeNumAndWeekNum(String courseTime) {
        //将星期转换为对应的系数  星期日：1，星期一：2，星期二：3，星期三：4，星期四：5，星期五：6，星期六：7
        // The first element in the outer list is the inter list, and the second element is a special list.
        // The elements in the inter list are weekNum.
        // The elements in special list means how many weekNum appears at a single time.
        List<List<Integer>> courseTimeNumAndWeekNum = new ArrayList<>();
        List<Integer> interList = new ArrayList<>();
        List<Integer> specialList = new ArrayList<>();
        String[] courseTimeElements = courseTime.split(" ");
        for (String courseTimeElement : courseTimeElements) {
            int countOfWeekNum = 0;
            if (courseTimeElement.contains("Su")) {
                interList.add(1);
                ++countOfWeekNum;
            }
            if (courseTimeElement.contains("M")) {
                interList.add(2);
                ++countOfWeekNum;
            }
            if (courseTimeElement.contains("Tu")) {
                interList.add(3);
                ++countOfWeekNum;
            }
            if (courseTimeElement.contains("W")) {
                interList.add(4);
                ++countOfWeekNum;
            }
            if (courseTimeElement.contains("Th")) {
                interList.add(5);
                ++countOfWeekNum;
            }
            if (courseTimeElement.contains("F")) {
                interList.add(6);
                ++countOfWeekNum;
            }
            if (courseTimeElement.contains("Sa")) {
                interList.add(7);
                ++countOfWeekNum;
            }
            if (countOfWeekNum != 0) {
                specialList.add(countOfWeekNum);
            }
        }
        courseTimeNumAndWeekNum.add(interList);
        courseTimeNumAndWeekNum.add(specialList);
        return courseTimeNumAndWeekNum;
    }

    public static Integer dayForWeek(Date date) {
        //等到当期时间的周系数。星期日：1，星期一：2，星期二：3，星期三：4，星期四：5，星期五：6，星期六：7
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    public static List<Date> getAllDatesBetweenRange(Date dateFrom, Date dateEnd, String weekDays, Course course, String courseCode) {
        // WeekDays format: 123 , which means Monday, Tuesday and Friday.
        long time;
        long perDayMilSec = 24 * 60 * 60 * 1000;
        long perHalfDayMilSec = 12 * 60 * 60 * 1000;
        long perHourMilSec = 60 * 60 * 1000;
        long perMinuteMilSec = 60 * 1000;
        List<Date> dateList = new ArrayList<Date>();
        List<String> courseTimeListTemp = new ArrayList<>();
        List<String> courseTimeList = new ArrayList<>();
        Collections.addAll(courseTimeListTemp, course.getCourseElement(courseCode, "Time").split(" "));
        int courseTimeListTempSize = courseTimeListTemp.size();
        for(int i = 0; i < courseTimeListTempSize; ++i){
            String[] tempForSubCourseTimeList = courseTimeListTemp.get(i).split("-");
            //Log.i("Notification ", "tempForSubCourseTimeList: " + tempForSubCourseTimeList.toString());
            Collections.addAll(courseTimeList, tempForSubCourseTimeList);
        }
        Integer courseTimeListSize = courseTimeList.size();
        Date tempDateForReadingCourseStart;
        Date tempDateForReadingCourseEnd;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.US);
        Date dateOfCourseEnd;
        Date dateOfCourseStart;
        Calendar calendarForStart = Calendar.getInstance();
        Calendar calendarForEnd = Calendar.getInstance();
        //需要查询的星期系数
        dateFrom = new Date(dateFrom.getTime() - perDayMilSec);
        while (true) {
            time = dateFrom.getTime();
            time = time + perDayMilSec;
            Date date = new Date(time);
            dateFrom = date;
            dateOfCourseStart = new Date(time);
            dateOfCourseEnd = new Date(time);
            if (dateFrom.compareTo(dateEnd) <= 0) {
                //查询的某一时间的星期系数
                Integer weekDay = dayForWeek(date);
                //判断当期日期的星期系数是否是需要查询的
                if (weekDays.contains(weekDay.toString())) {
                    for (int i = 0; i < courseTimeListSize; ++i)
                        if (courseTimeList.get(i).contains(getEngForWeekDay(weekDay.toString()))) {
                            try {
                                //Log.i("Notification ", "courseTimeList: " + courseTimeList.toString());
                                tempDateForReadingCourseStart = sdf.parse(courseTimeList.get(i + 1));
                                tempDateForReadingCourseEnd = sdf.parse(courseTimeList.get(i + 2));
                                /*
                                calendarForStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(courseTimeList.get(i + 1).split(":")[0]));
                                calendarForStart.set(Calendar.MINUTE, Integer.parseInt(courseTimeList.get(i + 1).split(":")[1]));
                                */ // Another solution for setting time from Date to Calendar.
                                calendarForStart.setTime(tempDateForReadingCourseStart);
                                //Log.i("getDatesBetweenRange ", courseTimeList.get(i + 1) + " after tempDateForReadingCourseStart Long: " + tempDateForReadingCourseStart.getTime());
                                //Log.i("getDatesBetweenRange ", sdf.parse(courseTimeList.get(i + 1)) + " is being converted to " + calendarForStart.get(Calendar.HOUR_OF_DAY));
                                calendarForEnd.setTime(tempDateForReadingCourseEnd);
                                //Log.i("getDatesBetweenRange ", tempDateForReadingCourseEnd.toString() + " is being converted to " + calendarForEnd.get(Calendar.HOUR_OF_DAY));
                                if (courseTimeList.get(i + 2).contains("p")) {
                                    if (Integer.parseInt(courseTimeList.get(i + 2).split(":")[0]) < 12) {
                                        if (Integer.parseInt(courseTimeList.get(i + 1).split(":")[0]) < 12) {
                                            //Log.i("Notification ", "dateOfCourseStart: " + dateOfCourseStart + " tempDateForReadingCourseStart: " + tempDateForReadingCourseStart + " ");
                                            //Log.i("Notification ", "part of the new time: " + new Date(dateFrom.getTime() + tempDateForReadingCourseStart.getHours() + perDayMilSec / 2));
                                            // Bet aware here that we have to use Calendar.Hour_of_day since we want 24hrs format but not 12hrs format.
                                            dateOfCourseStart = new Date(dateFrom.getTime() + calendarForStart.get(Calendar.HOUR_OF_DAY) * perHourMilSec + calendarForStart.get(Calendar.MINUTE) * perMinuteMilSec + perHalfDayMilSec);
                                        } else {
                                            dateOfCourseStart = new Date(dateFrom.getTime() + calendarForStart.get(Calendar.HOUR_OF_DAY) * perHourMilSec + calendarForStart.get(Calendar.MINUTE) * perMinuteMilSec);
                                        }
                                        dateOfCourseEnd = new Date(dateFrom.getTime() + calendarForEnd.get(Calendar.HOUR_OF_DAY) * perHourMilSec + calendarForEnd.get(Calendar.MINUTE) * perMinuteMilSec + perHalfDayMilSec);
                                    } else {
                                        dateOfCourseStart = new Date(dateFrom.getTime() + calendarForStart.get(Calendar.HOUR_OF_DAY) * perHourMilSec + calendarForStart.get(Calendar.MINUTE) * perMinuteMilSec);
                                        dateOfCourseEnd = new Date(dateFrom.getTime() + calendarForEnd.get(Calendar.HOUR_OF_DAY) * perHourMilSec + calendarForEnd.get(Calendar.MINUTE) * perMinuteMilSec);
                                    }
                                    //Log.i("Notification ", "We have time for " + courseCode + " on weekday " + weekDay + ":");
                                    //Log.i("Notification ", "The start time is " + dateOfCourseStart);
                                    //Log.i("Notification ", "And the end time is " + dateOfCourseEnd);
                                } else {
                                    dateOfCourseStart = new Date(dateFrom.getTime() + calendarForStart.get(Calendar.HOUR_OF_DAY) * perHourMilSec + calendarForStart.get(Calendar.MINUTE) * perMinuteMilSec);
                                    dateOfCourseEnd = new Date(dateFrom.getTime() + calendarForEnd.get(Calendar.HOUR_OF_DAY) * perHourMilSec + calendarForEnd.get(Calendar.MINUTE) * perMinuteMilSec);
                                }
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    dateList.add(dateOfCourseStart);
                    if(!course.isFirstInstructionDateSet(Integer.parseInt(courseCode))){
                        course.setFirstInstructionDate(courseCode, dateOfCourseStart);
                    }
                    dateList.add(dateOfCourseEnd);
                }
            } else {
                break;
            }
        }
        return dateList;
    }
}
