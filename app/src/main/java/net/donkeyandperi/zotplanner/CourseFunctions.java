package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CourseFunctions {

    public static Document getDocument(String url, List<String> elementList) {
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .data("YearTerm", elementList.get(0))
                    .data("Breadth", elementList.get(1))
                    .data("Dept", elementList.get(2))
                    .data("Division", elementList.get(3))
                    .data("ClassType", "ALL")
                    .data("CourseCodes", elementList.get(4))
                    .data("CancelledCourses", "Exclude")
                    .data("Submit", "Display Web Results")
                    .timeout(10000).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    public static class SendRequest extends Thread {
        private volatile List<Course> courseList = null;
        final List<String> elementList;
        private Handler handler;
        private final String searchOptionYearTerm;
        private boolean runningFlag = false;
        final MyApp app;

        public SendRequest(final List<String> elementList, Handler handler, String searchOptionYearTerm, MyApp app) {
            this.elementList = elementList;
            this.handler = handler;
            this.searchOptionYearTerm = searchOptionYearTerm;
            this.app = app;
        }

        @Override
        public void run() {
            super.run();
            runningFlag = true;
            int failTime = 0;
            Message message = new Message();
            Bundle data = new Bundle();
            for (failTime = 0; failTime < 10; ++failTime) {
                try {
                    Log.i("Elementlist ", elementList.toString());
                    org.jsoup.nodes.Document document = getDocument("https://www.reg.uci.edu/perl/WebSoc", elementList);
                    Log.i("Document ", document.toString());

                    String[] tempSearchTerm = elementList.get(0).split("-");

                    courseList = parseHtmlForCourseList(document, searchOptionYearTerm, tempSearchTerm);
                    Gson gson = new Gson();
                    data.putBoolean("is_success", true);
                    data.putString("course_list", gson.toJson(courseList));
                    message.setData(data);
                    break;
                } catch (Exception e) {
                    Log.i("Notification ", "Hey! We have some problems!");
                    e.printStackTrace();
                }
            }
            if (failTime == 10) {
                if (app.checkNotificationServiceOnGoingList(elementList.get(4))) {
                    app.removeNotificationServiceOnGoingList(elementList.get(4));
                    Log.i("Notification ", "After removing notification(fail): " + app.checkNotificationServiceOnGoingList(elementList.get(4)));
                    data.putBoolean("is_success", false);
                }
            }
            handler.sendMessage(message);
            runningFlag = false;
        }

        public boolean getRunningFlag() {
            return runningFlag;
        }

        public List<Course> getCourseList() {
            return courseList;
        }
    }

    private static List<Course> parseHtmlForCourseList(Document document, String searchOptionYearTerm, String[] searchTerm) {
        //Elements elements = document.getElementsByClass("CourseTitle");

        boolean isSummer = searchTerm[1].contains("25") | searchTerm[1].contains("39") | searchTerm[1].contains("51") | searchTerm[1].contains("76");

        Elements elements = document.select("div[class=course-list] > table > tbody > tr");
        Elements elementsForCourseElementTitles = document.select("div[class=course-list] > table > tbody > tr[bgcolor=#E7E7E7]").get(0).select("th");
        List<String> courseElementToBeAdded = new ArrayList<>();
        int courseElementLength = elementsForCourseElementTitles.size();
        Log.i("Notification ", "courseElementLength: " + courseElementLength);
        Log.i("Notification ", "should be the last courseElementTitle: " + elementsForCourseElementTitles.get(courseElementLength - 1).text());
        for(int i = 0; i < courseElementLength; ++i){
            courseElementToBeAdded.add(elementsForCourseElementTitles.get(i).text());
        }
        Elements subElementsTitle;
        //System.out.println("Hey there: " + elements);
        Course newCourse = null;
        List<Course> courseList = new ArrayList<>();
        String currentCourseNum = null;
        boolean isAbleToContinue = true;
        for (int i = 0; i < elements.size(); ++i) {
            subElementsTitle = elements.get(i).select("td");
            //Log.i("Notification ", "Size of subElementsTitle is: " + subElementsTitle.size());
            if (elements.get(i).attr("class").equals("college-title")) {
                newCourse = null;
                continue;
            }
            for (int subI = 0; subI < subElementsTitle.size(); ++subI) {
                //Log.i("Notification ", "We are getting Num (" + subI + "): " + subElementsTitle.get(subI).text());
                if (newCourse == null && subElementsTitle.get(subI).attr("class").equals("CourseTitle")) {
                    //Log.i("Notification ", "Getting class: " + subElementsTitle.get(subI).attr("class") + " with value: " + subElementsTitle.get(subI).text());
                    newCourse = new Course();
                    courseList.add(newCourse);
                    newCourse.setIsSummer(isSummer);
                    newCourse.setCourseBeginYear(searchTerm[0]);
                    newCourse.setCourseName(subElementsTitle.get(subI).text());
                    newCourse.setSearchOptionYearTerm(searchOptionYearTerm);
                    newCourse.setCourseAcademicYearTerm();
                    //Log.i("New course ", newCourse.getCourseName());
                } else if (newCourse != null) {
                    //Log.i("Notification ", " indexOfElement (After Check): " + indexOfElement + " with SubI: " + subI + " and check if equal: " + sizeOfElementList);
                    if (isAbleToContinue) {
                        if (subElementsTitle.get(subI).text().isEmpty()) {
                            newCourse = null;
                            continue;
                        }
                        currentCourseNum = subElementsTitle.get(subI).text();
                        newCourse.setUpNewCourse(currentCourseNum);
                        isAbleToContinue = false;
                    } else if (currentCourseNum != null) {
                        newCourse.setUpCourseElement(currentCourseNum, courseElementToBeAdded.get(subI), subElementsTitle.get(subI).text());
                        if(subI == courseElementLength - 1){
                            isAbleToContinue = true;
                        }
                    }
                }
            }
        }
        return courseList;
    }

    static public SendRequest SendNotificationIfSingleCourseMatchStatus(final SingleCourse singleCourse, final MyApp app, final Context context, final Handler handler) {
        List<String> elementList = new ArrayList<>();
        elementList.add(singleCourse.getSearchOptionYearTerm());
        elementList.add(CourseStaticData.defaultSearchOptionBreadth);
        elementList.add(CourseStaticData.defaultSearchOptionDept);
        elementList.add(CourseStaticData.defaultSearchOptionDivision);
        elementList.add(singleCourse.getCourseCode());
        final CourseFunctions.SendRequest sendRequest = new CourseFunctions.SendRequest(elementList, handler, singleCourse.getSearchOptionYearTerm(), app);
        sendRequest.start();
        return sendRequest;
    }

    public static Document getCalendarDocument(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url)
                    .timeout(10000).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    public static class SendRequestForCalendar extends Thread {
        private Handler handler;
        private boolean runningFlag = false;
        final MyApp app;
        private String academicYearTerm;
        private boolean isSummer;
        private String year;
        private Course iCourse;

        public SendRequestForCalendar(Handler handler, MyApp app, String academicYearTerm, boolean isSummer, String year, Course iCourse) {
            this.handler = handler;
            this.app = app;
            this.academicYearTerm = academicYearTerm;
            this.isSummer = isSummer;
            this.year = year;
            this.iCourse = iCourse;
        }

        @Override
        public void run() {
            super.run();
            runningFlag = true;
            int failTime = 0;
            Message message = new Message();
            Bundle data = new Bundle();
            for (failTime = 0; failTime < 10; ++failTime) {
                try {
                    StringBuilder url = new StringBuilder();
                    url.append("https://www.reg.uci.edu/calendars/quarterly/");
                    if (academicYearTerm.contains("Fall")) {
                        url.append(year);
                        url.append("-");
                        url.append(String.valueOf(Integer.parseInt(year) + 1));
                    } else {
                        url.append(String.valueOf(Integer.parseInt(year) - 1));
                        url.append("-");
                        url.append(year);
                    }
                    url.append("/quarterly");
                    if (academicYearTerm.contains("Fall")) {
                        url.append(year.substring(2, 4));
                        url.append("-");
                        url.append(String.valueOf(Integer.parseInt(year) + 1).substring(2, 4));
                    } else {
                        url.append(String.valueOf(Integer.parseInt(year) - 1).substring(2, 4));
                        url.append("-");
                        url.append(year.substring(2, 4));
                    }
                    url.append(".html");
                    Log.i("Notification ", "Url build complete, which is: " + url.toString());

                    org.jsoup.nodes.Document document = getCalendarDocument(url.toString());
                    List<Date> instructionBeginAndEndDates = parseHtmlForBeginAndEndDate(document, academicYearTerm, isSummer, year);
                    if (instructionBeginAndEndDates.isEmpty()) {
                        throw new NullPointerException("Nothing is provided in instructionBeginAndEndDates");
                    }
                    Gson gson = new Gson();
                    data.putBoolean("is_success", true);
                    data.putString("instruction_begin_and_end_dates", gson.toJson(instructionBeginAndEndDates));
                    message.setData(data);
                    Log.i("SendRequestForCalendar ", "is_success: true");
                    //app.updateCourseWithDate(iCourse.getCourseName(), instructionBeginAndEndDates);
                    //app.addCachedInstructionBeginAndEndDates(iCourse.getSearchOptionYearTerm(), instructionBeginAndEndDates);
                    break;
                } catch (Exception e) {
                    Log.i("SendRequestForCalendar ", "Hey! We have some problems!");
                    e.printStackTrace();
                }
            }
            if (failTime == 10) {
                data.putBoolean("is_success", false);
            }
            Log.i("SendRequestForCalendar ", "Going to send message back to the handler.");
            handler.sendMessage(message);
            runningFlag = false;
        }

        public boolean getRunningFlag() {
            return runningFlag;
        }
    }

    private static List<Date> parseHtmlForBeginAndEndDate(Document document, String academicYearTerm, boolean isSummer, String year) {
        Elements elements = document.select("table[class=calendartable]");
        Elements quarterActivity;
        Elements instructionEnd;
        if (!isSummer) {
            quarterActivity = elements.get(2).select("tr");
            instructionEnd = quarterActivity.get(16).select("td");
        } else {
            quarterActivity = elements.get(4).select("tr");
            instructionEnd = quarterActivity.get(5).select("td");
        }
        Elements instructionBegin = quarterActivity.get(2).select("td");
        int num;
        if (academicYearTerm.equals("Fall") | academicYearTerm.equals("Summer Session I")) {
            num = 1;
        } else if (academicYearTerm.equals("Winter") | academicYearTerm.equals("Summer Session 10WK")) {
            num = 2;
        } else {
            num = 3;
        }
        StringBuilder instructionBeginString = new StringBuilder();
        instructionBeginString.append(instructionBegin.get(num).text());
        instructionBeginString.append(", ");
        instructionBeginString.append(year);
        Log.i("Notification ", "instructionBeginString should be: " + instructionBeginString.toString());
        StringBuilder instructionEndString = new StringBuilder();
        instructionEndString.append(instructionEnd.get(num).text());
        instructionEndString.append(", ");
        instructionEndString.append(year);
        Log.i("Notification ", "instructionEndString should be: " + instructionEndString.toString());
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.US);
        List<Date> result = new ArrayList<>();
        try {
            Date instructionBeginDate = sdf.parse(instructionBeginString.toString());
            Date instructionEndDate = sdf.parse(instructionEndString.toString());
            Log.i("Notification ", "instructionBeginDate should be: " + instructionBeginDate.toString());
            Log.i("Notification ", "instructionEndDate should be: " + instructionEndDate.toString());
            result.add(instructionBeginDate);
            result.add(instructionEndDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
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
            Log.i("Notification ", "tempForSubCourseTimeList: " + tempForSubCourseTimeList.toString());
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
                                Log.i("Notification ", "courseTimeList: " + courseTimeList.toString());
                                tempDateForReadingCourseStart = sdf.parse(courseTimeList.get(i + 1));
                                tempDateForReadingCourseEnd = sdf.parse(courseTimeList.get(i + 2));
                                /*
                                calendarForStart.set(Calendar.HOUR_OF_DAY, Integer.parseInt(courseTimeList.get(i + 1).split(":")[0]));
                                calendarForStart.set(Calendar.MINUTE, Integer.parseInt(courseTimeList.get(i + 1).split(":")[1]));
                                */ // Another solution for setting time from Date to Calendar.
                                calendarForStart.setTime(tempDateForReadingCourseStart);
                                Log.i("getDatesBetweenRange ", courseTimeList.get(i + 1) + " after tempDateForReadingCourseStart Long: " + tempDateForReadingCourseStart.getTime());
                                Log.i("getDatesBetweenRange ", sdf.parse(courseTimeList.get(i + 1)) + " is being converted to " + calendarForStart.get(Calendar.HOUR_OF_DAY));
                                calendarForEnd.setTime(tempDateForReadingCourseEnd);
                                Log.i("getDatesBetweenRange ", tempDateForReadingCourseEnd.toString() + " is being converted to " + calendarForEnd.get(Calendar.HOUR_OF_DAY));
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
                                    Log.i("Notification ", "We have time for " + courseCode + " on weekday " + weekDay + ":");
                                    Log.i("Notification ", "The start time is " + dateOfCourseStart);
                                    Log.i("Notification ", "And the end time is " + dateOfCourseEnd);
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

    //等到当期时间的周系数。星期日：1，星期一：2，星期二：3，星期三：4，星期四：5，星期五：6，星期六：7
    public static Integer dayForWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_WEEK);
    }

    //将星期转换为对应的系数  星期日：1，星期一：2，星期二：3，星期三：4，星期四：5，星期五：6，星期六：7
    public static List<List<Integer>> getCourseTimeNumAndWeekNum(String courseTime) {
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

    private static String getEngForWeekDay(String num) {
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

    public static Integer getCourseStatusCorrespondingColor(String status, Context context){
        if(status == null){
            return R.color.course_status_null_color;
        } else if(status.equals(CourseStaticData.defaultClassStatusWL))
        {
            return R.color.course_status_wait_list_color;
        } else if (status.equals(CourseStaticData.defaultClassStatusFull))
        {
            return R.color.course_status_full_color;
        } else if (status.equals(CourseStaticData.defaultClassStatusNewOnly))
        {
            return R.color.course_status_new_only_color;
        } else if(status.equals(CourseStaticData.defaultClassStatusOpen))
        {
            return R.color.course_status_open_color;
        }
        return R.color.course_status_null_color;
    }

    public static String getCorrespondingCourseStatusString(String courseStatus, Context context){
        if(courseStatus.equals(CourseStaticData.defaultClassStatusFull)){
            return context.getString(R.string.notification_settings_time_status_option_full);
        } else if(courseStatus.equals(CourseStaticData.defaultClassStatusNewOnly)){
            return context.getString(R.string.notification_settings_time_status_option_NewOnly);
        } else if(courseStatus.equals(CourseStaticData.defaultClassStatusOpen)){
            return context.getString(R.string.notification_settings_time_status_option_open);
        } else {
            return context.getString(R.string.notification_settings_time_status_option_Waitl);
        }
    }

    public static void refreshLists(final MyApp app) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int failTime = 0; failTime < 10; ++failTime) {
                    try {
                        org.jsoup.nodes.Document document = Jsoup.connect("https://www.reg.uci.edu/perl/WebSoc").timeout(10000).get();
                        app.addSearchOptionToMap("dept_list", new ArrayList<String>(parseHtmlForText(document, "Dept")));
                        app.addSearchOptionToMap("quarter_list", new ArrayList<String>(parseHtmlForText(document, "YearTerm")));
                        app.addSearchOptionToMap("ge_list", new ArrayList<String>(parseHtmlForText(document, "Breadth")));
                        app.addSearchOptionToMap("level_list", new ArrayList<String>(parseHtmlForText(document, "Division")));
                        app.addSearchOptionToMap("dept_value_list", new ArrayList<String>(parseHtmlForValue(document, "Dept")));
                        app.addSearchOptionToMap("quarter_value_list", new ArrayList<String>(parseHtmlForValue(document, "YearTerm")));
                        app.addSearchOptionToMap("ge_value_list", new ArrayList<String>(parseHtmlForValue(document, "Breadth")));
                        app.addSearchOptionToMap("level_value_list", new ArrayList<String>(parseHtmlForValue(document, "Division")));
                        app.setRegNormalPage(document);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static List<String> parseHtmlForText(Document document, String name) {
        Elements elements = document.select("select[name=" + name + "]").select("option");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < elements.size(); ++i) {
            //Log.i("size: ", String.valueOf(elements.size()));
            //list.add(element.select("option").attr("value"));
            list.add(elements.get(i).text());
        }
        return list;
    }

    private static List<String> parseHtmlForValue(Document document, String name) {
        Elements elements = document.select("select[name=" + name + "]").select("option");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < elements.size(); ++i) {
            //Log.i("size: ", String.valueOf(elements.size()));
            //list.add(element.select("option").attr("value"));
            list.add(elements.get(i).attr("value"));
        }
        return list;
    }

}
