package net.donkeyandperi.zotplanner;

import android.os.Build;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class Course {
    private static final String TAG = "Course";
    private String courseName;
    private HashMap<String, SingleCourse> singleCourseHashMap = new HashMap<>();
    private List<String> elementNameList =  CourseStaticData.presetElementNameList;
    List<String> courseCodeList = new ArrayList<>();
    private List<String> selectedCourseCodeList = new ArrayList<>();
    private String currentSelectedCourseCode = null;
    private String searchOptionYearTerm;
    private boolean isSummer = false;
    private List<String> courseQuarterBeginDate = new ArrayList<>();
    private List<String> courseQuarterEndDate = new ArrayList<>();
    private String courseAcademicYearTerm;
    private Date instructionBeginDate;
    private Date instructionEndDate;
    private boolean isDateSet = false;
    private HashMap<String, List<Date>> courseStartDates = new HashMap<>();
    private HashMap<String, Integer> courseColors = new HashMap<>();
    private HashMap<Integer, Calendar> firstInstructionDates = new HashMap<>();
    private boolean isExpandedOnSelectedCourseList = false;
    private boolean isExpandingOnSelectedCourseList = false;
    private boolean isReadyWithViewsOnSelectedCourseListForExpand = false;
    private String comments = "";   // This is the comments for the whole Course
    private HashMap<String, String> commentsForSingleCourses = new HashMap<>();// This is the comments for every singleCourse

    public Course(){
    }

    public void setCourseName(String courseName)
    {
        this.courseName = courseName;
    }

    public String getCourseName() {
        return courseName;
    }

    public void addElementNameToList(String courseElement)
    {
        elementNameList.add(courseElement);
    }

    public List<String> getElementNameList()
    {
        return elementNameList;
    }

    public void setCourseElementNameList(List<String> courseElementNameList){
        this.elementNameList = courseElementNameList;
    }

    public String getElementNameFromList(int index)
    {
        return elementNameList.get(index);
    }

    public void setUpNewSingleCourse(String courseNum)
    {
        Log.v(TAG, "setUpNewSingleCourse: setting up a new course: " + courseNum);

        courseCodeList.add(courseNum);

        singleCourseHashMap.put(courseNum, new SingleCourse());
        setUpSingleCourseElement(courseNum, elementNameList.get(0), courseNum);
        singleCourseHashMap.get(courseNum).setCourseName(courseName);
    }

    public void setUpSingleCourseElement(String courseNum, String courseElement, String elementValue)
    {
        singleCourseHashMap.get(courseNum).setUpCourseElement(courseElement, elementValue);
    }

    public void setUpSingleCourseLocationLink(String courseNum, String locationLink)
    {
        singleCourseHashMap.get(courseNum).setCourseLocationWebsiteLink(locationLink);
    }

    public void setUpSingleCourseLocationBuildingName(String courseNum, String buildingName){
        singleCourseHashMap.get(courseNum).setCourseLocationBuildingName(buildingName);
    }

    public String getSingleCourseLocationBuildingName(String courseNum){
        return singleCourseHashMap.get(courseNum).getCourseLocationBuildingName();
    }

    public String getCourseElement(String courseNum, String courseElement)
    {
//        Log.v(TAG, "getCourseElement: Here is the CourseList: " + singleCourseHashMap);
//        Log.v(TAG, "getCourseElement: Do we have a course here for " + courseNum +
//                ": " + singleCourseHashMap.get(courseNum));
        return singleCourseHashMap.get(courseNum).getCourseElement(courseElement);
    }

    public String getCourseBasicInfo()
    {
        StringBuilder sb = new StringBuilder();
        for(String courseCode: courseCodeList)
        {
            sb.append(courseCode);
            sb.append(": ");
            sb.append(getCourseElement(courseCode, "Type"));
            sb.append("   ");
            sb.append(getCourseElement(courseCode, "Time"));
            sb.append("   ");
            sb.append(getCourseElement(courseCode, "Place"));
            sb.append("   ");
            sb.append(getCourseElement(courseCode, "Status"));
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<SingleCourse> getSingleCourseList()
    {
        List<SingleCourse> result = new ArrayList<>();
        for(String courseCode: courseCodeList)
        {
            result.add(getSingleCourse(courseCode));
        }
        return result;
    }

    public List<SingleCourse> getSelectedSingleCourseList(){
        List<SingleCourse> result = new ArrayList<>();
        // Sort it first to make the singleCourses being shown in the correct order
        Collections.sort(selectedCourseCodeList);
        for(String courseCode: selectedCourseCodeList){
            result.add(getSingleCourse(courseCode));
        }
        return result;
    }

    public void addSelectedCourse(String courseCode) {
        if(!selectedCourseCodeList.contains(courseCode))
        {
            this.selectedCourseCodeList.add(courseCode);
        }
    }

    public void removeSelectedCourse(String courseCode){
        selectedCourseCodeList.remove(courseCode);
    }

    public void setSelectedCourseCodeList(List<String> selectedCourseCodeList){
        this.selectedCourseCodeList = selectedCourseCodeList;
    }

    public List<String> getSelectedCourseCodeList() {
        return selectedCourseCodeList;
    }

    public SingleCourse getSingleCourse(String courseCode){
        // Return null on no found
        if (!singleCourseHashMap.containsKey(courseCode)) {
            return null;
        }
        return singleCourseHashMap.get(courseCode);
    }

    public void setSearchOptionYearTerm(String searchOptionYearTerm) {
        this.searchOptionYearTerm = searchOptionYearTerm;
    }

    public String getSearchOptionYearTerm() {
        return searchOptionYearTerm;
    }

    public void setCurrentSelectedCourseCode(String currentSelectedCourseCode) {
        this.currentSelectedCourseCode = currentSelectedCourseCode;
    }

    public String getCurrentSelectedCourseCode(){
        return this.currentSelectedCourseCode;
    }

    public String getSelectedCourseBasicInfo(){
        StringBuilder sb = new StringBuilder();
        for(String courseCode: selectedCourseCodeList)
        {
            sb.append(courseCode);
            sb.append(": ");
            sb.append(getCourseElement(courseCode, "Type"));
            sb.append("   ");
            sb.append(getCourseElement(courseCode, "Time"));
            sb.append("   ");
            sb.append(getCourseElement(courseCode, "Place"));
            sb.append("   ");
            sb.append(getCourseElement(courseCode, "Status"));
            sb.append("\n");
        }
        return sb.toString();
    }

    public List<String> getCourseCodeList(){
        return courseCodeList;
    }

    public void setIsSummer(boolean is){
        isSummer = is;
    }

    public boolean isSummer(){
        return isSummer;
    }

    public void setCourseBeginYear(String year){
        courseQuarterBeginDate.add(year);
    }

    public String getCourseBeginYear(){
        return courseQuarterBeginDate.get(0);
    }

    public void setCourseBeginMonthAndDay(String monthAndDay){
        String[] iMonthAndDay = monthAndDay.split(" ");
        courseQuarterBeginDate.add(iMonthAndDay[0]);
        courseQuarterBeginDate.add(iMonthAndDay[1]);
    }

    public String getCourseBeginMonth(){
        return courseQuarterBeginDate.get(1);
    }

    public String getCourseBeginDay(){
        return courseQuarterBeginDate.get(2);
    }

    public void setCourseEndYear(String year){
        courseQuarterEndDate.set(0, year);
    }

    public String getCourseEndYear(){
        return courseQuarterEndDate.get(0);
    }

    public void setCourseEndMonthAndDay(String monthAndDay){
        String[] iMonthAndDay = monthAndDay.split(" ");
        courseQuarterEndDate.set(1, iMonthAndDay[0]);
        courseQuarterEndDate.set(2, iMonthAndDay[1]);
    }

    public String getCourseEndMonth(){
        return courseQuarterEndDate.get(1);
    }

    public String getCourseEndDay(){
        return courseQuarterEndDate.get(2);
    }

    public void setCourseAcademicYearTerm(){
        String[] searchItems = searchOptionYearTerm.split("-");
        if(isSummer){
            if(searchItems[1].equals(CourseStaticData.summerSession1Code)){
                courseAcademicYearTerm = "Summer Session I";
            } else if(searchItems[1].equals(CourseStaticData.summerSession2Code)){
                courseAcademicYearTerm = "Summer Session II";
            } else if(searchItems[1].equals(CourseStaticData.summerSessionQuarterCode)){
                courseAcademicYearTerm = "Summer Session 10WK";
            } else{
                courseAcademicYearTerm = "Summer Session 10WK";
            }
        } else{
            StringBuilder yearTerm = new StringBuilder();
            if(searchItems[1].equals(CourseStaticData.fallQuarterCode)){
                yearTerm.append("Fall");
            } else if(searchItems[1].equals(CourseStaticData.winterQuarterCode)){
                yearTerm.append("Winter");
            } else if(searchItems[1].equals(CourseStaticData.springQuarterCode)){
                yearTerm.append("Spring");
            }
            courseAcademicYearTerm = yearTerm.toString();
        }
    }

    public String getCourseAcademicYearTerm(){
        return courseAcademicYearTerm;
    }

    public void setInstructionBeginDate(Date date){
        instructionBeginDate = date;
    }

    public Date getInstructionBeginDate(){
        return instructionBeginDate;
    }

    public void setInstructionEndDate(Date date){
        instructionEndDate = date;
    }

    public Date getInstructionEndDate(){
        return instructionEndDate;
    }

    public void setIsDateSet(boolean isDateSet){
        // Setting whether instruction begin and end dates are set.
        this.isDateSet = isDateSet;
    }

    public boolean isDateSet(){
        return isDateSet;
    }

    public void setCourseColor(String courseCode, Integer color){
        courseColors.put(courseCode, color);
    }

    public Integer getCourseColor(String courseCode){
        // Return -1 if find nothing.
        int result = -1;
        if(courseColors.containsKey(courseCode)){
            result = courseColors.get(courseCode);
        }
        return result;
    }

    public void setFirstInstructionDate(String courseCode, Date date){
        Calendar temp = Calendar.getInstance();
        temp.setTime(date);
        firstInstructionDates.put(Integer.parseInt(courseCode), temp);
    }

    public boolean isFirstInstructionDateSet(Integer courseCode){
        return firstInstructionDates.containsKey(courseCode);
    }

    public Calendar getFirstInstructionDate(Integer courseCode){
        return firstInstructionDates.get(courseCode);
    }

    public boolean haveCourseCode(int courseCode){
        Log.i("Notification ", "(haveCourseCode) checking whether " + courseCode + " in " + courseCodeList.toString());
        for (String iCourseCode: courseCodeList)
        {
            if(Integer.parseInt(iCourseCode) == courseCode){
                return true;
            }
        }
        return false;
    }

    public boolean isElementTBA(String courseCode, String elementTitle){
        return singleCourseHashMap.get(courseCode).getCourseElement(elementTitle).equals("TBA");
    }

    public boolean isExpandedOnSelectedCourseList() {
        return isExpandedOnSelectedCourseList;
    }

    public void setExpandedOnSelectedCourseList(boolean expandedOnSelectedCourseList) {
        isExpandedOnSelectedCourseList = expandedOnSelectedCourseList;
    }

    public boolean isExpandingOnSelectedCourseList() {
        return isExpandingOnSelectedCourseList;
    }

    public void setExpandingOnSelectedCourseList(boolean expandingOnSelectedCourseList) {
        isExpandingOnSelectedCourseList = expandingOnSelectedCourseList;
    }

    public boolean isReadyWithViewsOnSelectedCourseListForExpand() {
        return isReadyWithViewsOnSelectedCourseListForExpand;
    }

    public void setReadyWithViewsOnSelectedCourseListForExpand(boolean readyWithViewsOnSelectedCourseListForExpand) {
        isReadyWithViewsOnSelectedCourseListForExpand = readyWithViewsOnSelectedCourseListForExpand;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCommentsForSingleCourse(String courseCode) {
        if(Build.VERSION.SDK_INT >= 24){
            return commentsForSingleCourses.getOrDefault(courseCode, "");
        }
        if(commentsForSingleCourses.containsKey(courseCode)){
            return commentsForSingleCourses.get(courseCode);
        }
        return "";
    }

    public void setCommentsForSingleCourse(String courseCode, String comments) {
        commentsForSingleCourses.put(courseCode, comments);
    }

    public int getNumberOfSingleCourses(){
        return courseCodeList.size();
    }

    public boolean isSameCourse(Course course){
        for(String courseCode: course.courseCodeList){
            if(courseCodeList.contains(courseCode)){
                return true;
            }
        }
        return false;
    }

    public void updateSingleCourseHashMap(Course newCourse){
        // This function is going to update the current course's singleCourseHashMap with the new_course's
        // one. (If there is a match being found...)
        // TO-DO: Try preserve locationBuildingInfo if link is not changed... (however, the case where locationBuilingInfo is being updated should also be considered)
        HashMap<String, SingleCourse> newCourseSingleCourseHashMap = newCourse.singleCourseHashMap;
        for (String newCourseCourseNum: newCourseSingleCourseHashMap.keySet()){
            if(singleCourseHashMap.containsKey(newCourseCourseNum)){
//                Log.d(TAG, "updateSingleCourseHashMap: Going to update singleCourseInfo for " + newCourseCourseNum);
                if(singleCourseHashMap.get(newCourseCourseNum).getCourseLocationWebsiteLink().equals(newCourseSingleCourseHashMap.get(newCourseCourseNum).getCourseLocationWebsiteLink())){
                    newCourseSingleCourseHashMap.get(newCourseCourseNum).setCourseLocationBuildingName(singleCourseHashMap.get(newCourseCourseNum).getCourseLocationBuildingName());
                    LatLng latLng = singleCourseHashMap.get(newCourseCourseNum).getCourseLocationBuildingLatLng();
                    if (latLng != null) {
                        newCourseSingleCourseHashMap.get(newCourseCourseNum).setCourseLocationBuildingLatLng(latLng.latitude, latLng.longitude);
                    }
                }
                singleCourseHashMap.remove(newCourseCourseNum);
                singleCourseHashMap.put(newCourseCourseNum, newCourseSingleCourseHashMap.get(newCourseCourseNum));
            }
        }
    }
}
