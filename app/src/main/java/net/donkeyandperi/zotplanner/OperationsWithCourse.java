package net.donkeyandperi.zotplanner;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OperationsWithCourse {

    private final static String TAG = "OperationsWithCourse";

    public static Document getDocument(String url, List<String> elementList) {
        Document document = null;
        try {

            Log.d(TAG, "getDocument: The elementList is: " + elementList);
            document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
                    .maxBodySize(0)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .method(Connection.Method.POST)
                    .timeout(0)
                    .data("YearTerm", elementList.get(0))
                    .data("Breadth", elementList.get(1))
                    .data("Dept", elementList.get(2))
                    .data("Division", elementList.get(3))
                    .data("ClassType", "ALL")
                    .data("CourseCodes", elementList.get(4))
                    .data("ShowFinals", elementList.get(5))
                    .data("ShowComments", "1")
                    .data("CancelledCourses", "Exclude")
                    .execute().parse();
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
                    //Log.i("Elementlist ", elementList.toString());
                    org.jsoup.nodes.Document document = getDocument("https://www.reg.uci.edu/perl/WebSoc", elementList);
                    //Log.i("Document ", document.toString());
                    /*
                    Log.i(TAG, "SendRequest: (Original): " + document.toString().substring(
                            document.toString().indexOf("Total Classes Displayed"), document.toString().indexOf("Class web site links (listed in ")));
                    */
                    //app.writeLogToFile(document.toString(), new Date(System.currentTimeMillis()).toString(), "documentGotFromInternet/");

                    String[] tempSearchTerm = elementList.get(0).split("-");

                    courseList = parseHtmlForCourseList(document, searchOptionYearTerm, tempSearchTerm);
                    //Log.i(TAG, "SendRequest: (Parsed length): " + singleCourseInfoList.size());
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
        Log.i(TAG, "courseElementLength: " + courseElementLength);
        Log.i(TAG, "should be the last courseElementTitle: " + elementsForCourseElementTitles.get(courseElementLength - 1).text());
        for(int i = 0; i < courseElementLength; ++i){
            courseElementToBeAdded.add(elementsForCourseElementTitles.get(i).text());
        }
        Elements subElementsTitle;
        //System.out.println("Hey there: " + elements);
        Course newCourse = null;
        List<Course> courseList = new ArrayList<>();
        String currentCourseNum = null;
        boolean isAbleToContinue = true;
        Elements schoolCommentList = elements.select("tr > div[class=college-comment]");
        Log.d(TAG, "School comments size: " + schoolCommentList.size());
        if(schoolCommentList.size() > 0){
            String schoolComments = schoolCommentList.get(0).text();
            Log.d(TAG, "parseHtmlForCourseList: school comments: " + schoolComments);
        }
        for (int i = 0; i < elements.size(); ++i) {
            subElementsTitle = elements.get(i).select("td");
            //Log.i("Notification ", "Size of subElementsTitle is: " + subElementsTitle.size());
            if(i == 157){
                Log.d(TAG, "parseHtmlForCourseList: (elements : " + i + "): " + elements.get(i));
                Log.d(TAG, "parseHtmlForCourseList: The class attribute is: " + elements.get(i).attr("class"));
            }
            if(i == 159){
                Log.d(TAG, "parseHtmlForCourseList: (elements : " + i + "): " + elements.get(i));
                Log.d(TAG, "parseHtmlForCourseList: The class attribute is: " + elements.get(i).attr("class"));
            }
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
                    /*
                    if(i == 157){
                        Log.d(TAG, "parseHtmlForCourseList: (subI: " + subI + "): " + subElementsTitle.get(subI).text());
                    }
                    if(i == 159){
                        Log.d(TAG, "parseHtmlForCourseList: (subI: " + subI + "): " + subElementsTitle.get(subI).text());
                    }
                    */
                    //Log.i("Notification ", " indexOfElement (After Check): " + indexOfElement + " with SubI: " + subI + " and check if equal: " + sizeOfElementList);
                    if (isAbleToContinue) {
                        /*
                        if(i > 156 && i < 160){
                            Log.d(TAG, "parseHtmlForCourseList: (elements : " + i + "): " +
                                    "Checking should create a new course: " + subElementsTitle);
                            Log.d(TAG, "parseHtmlForCourseList: The answer is: " + subElementsTitle.get(subI).text().isEmpty());
                            Log.d(TAG, "parseHtmlForCourseList: is the problem solved?: " + subElementsTitle.attr("class").equals("Pct100"));
                        }
                        */
                        if(subElementsTitle.attr("class").equals("Pct100")){
                            // This if case is for dealing with case where there is an additional message for the course
                            // such as "same as ..." or "  To enroll in COMPSCI 199, obtain an authorization code from the instructor." and so on...
                            /*
                            Log.d(TAG, "parseHtmlForCourseList: the comments we get here is: " +
                                    subElementsTitle.select("td > table > tbody > tr > td[class=Comments]").text());
                                    */
                            String comments = subElementsTitle.select("td > table > tbody > tr > td[class=Comments]").text();
                            if(newCourse.getNumberOfSingleCourses() == 0){
//                                Log.d(TAG, "parseHtmlForCourseList: Setting up comments for " +
//                                        "the whole course : " + newCourse.getCourseName() + ", with comments: " + comments);
                                newCourse.setComments(comments);
                            } else {
//                                Log.d(TAG, "parseHtmlForCourseList: Setting up comments for " +
//                                        "the singleCourse of course : " + newCourse.getCourseName() +
//                                        ", with courseCode: " + currentCourseNum + ", with comments: " + comments);
                                newCourse.setCommentsForSingleCourse(currentCourseNum, comments);
                            }
                            continue;
                        }
                        if (subElementsTitle.get(subI).text().isEmpty()) {
                            newCourse = null;
                            continue;
                        }
                        currentCourseNum = subElementsTitle.get(subI).text();
                        /*
                        if(i > 156 && i < 160){
                            Log.d(TAG, "parseHtmlForCourseList: (elements : " + i + "): " + "Setting up new singleCourse as :" + currentCourseNum);
                        }
                        */
                        newCourse.setUpNewSingleCourse(currentCourseNum);
                        isAbleToContinue = false;
                    } else if (currentCourseNum != null) {
                        //Log.d(TAG, "Setting up single course element(" + currentCourseNum + "): " + courseElementToBeAdded.get(subI) + " with " + subElementsTitle.get(subI).text());
                        newCourse.setUpSingleCourseElement(currentCourseNum, courseElementToBeAdded.get(subI), subElementsTitle.get(subI).text());
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
        List<String> elementList = getElementListForSearchingCourse(
                singleCourse.getSearchOptionYearTerm(),
                CourseStaticData.defaultSearchOptionBreadth,
                CourseStaticData.defaultSearchOptionDept,
                CourseStaticData.defaultSearchOptionDivision,
                singleCourse.getCourseCode(),
                CourseStaticData.defaultSearchOptionShowFinals
        );
        final OperationsWithCourse.SendRequest sendRequest = new OperationsWithCourse.SendRequest(
                elementList, handler, singleCourse.getSearchOptionYearTerm(), app);
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


    public static Integer getCourseStatusCorrespondingColor(String status, Context context){
        Log.d(TAG, "getCourseStatusCorrespondingColor: the status is: " + status);
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

    public static List<String> getElementListForSearchingCourse(String yearTerm, String breadth, String dept,
                                                         String division, String courseCodes, String showFinals){
        // Leave the one empty by giving ""
        // showFinals: 0 -> yes, 1 -> no
        List<String> elementList = new ArrayList<>();
        elementList.add(yearTerm);
        elementList.add(breadth);
        elementList.add(dept);
        elementList.add(division);
        elementList.add(courseCodes);
        elementList.add(showFinals);
        return elementList;
    }

}
