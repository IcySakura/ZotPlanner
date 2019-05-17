package net.donkeyandperi.zotplanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SingleCourse {
    private String courseName;
    private HashMap<String, String> elementList = new HashMap<>();
    private List<String> presetElementNameList = new ArrayList<>(Arrays.asList("Code", "Type",
            "Sec", "Units", "Instructor", "Time", "Place", "Max", "Enr", "WL", "Req", "Nor", "Rstr",
            "Textbooks", "Web", "Status"));
    private List<String> elementNameList = presetElementNameList;
    private String searchOptionYearTerm;
    private boolean isSummer = false;
    private List<String> courseQuarterBeginDate = new ArrayList<>();
    private List<String> courseQuarterEndDate = new ArrayList<>();
    private boolean isFollowing = false;
    private boolean isExpanedInDialog = false;

    public void setCourseName(String courseName)
    {
        this.courseName = courseName;
    }

    public String getCourseName() {
        return courseName;
    }

    public List<String> getElementNameList()
    {
        return elementNameList;
    }

    public String getElementNameFromList(int index)
    {
        return elementNameList.get(index);
    }

    public void setUpCourseElement(String courseElement, String elementValue)
    {
        elementList.put(courseElement, elementValue);
    }

    public String getCourseElement(String courseElement)
    {
        return elementList.get(courseElement);
    }

    public String getCourseCode(){
        return elementList.get("Code");
    }

    public void setSearchOptionYearTerm(String searchOptionYearTerm) {
        this.searchOptionYearTerm = searchOptionYearTerm;
    }

    public void setIsSummer(boolean is){
        isSummer = is;
    }

    public boolean isSummer(){
        return isSummer;
    }

    public String getSearchOptionYearTerm() {
        return searchOptionYearTerm;
    }

    public void setCourseBeginYear(String year){
        courseQuarterBeginDate.set(0, year);
    }

    public String getCourseBeginYear(){
        return courseQuarterBeginDate.get(0);
    }

    public void setCourseBeginMonthAndDay(String monthAndDay){
        String[] iMonthAndDay = monthAndDay.split(" ");
        courseQuarterBeginDate.set(1, iMonthAndDay[0]);
        courseQuarterBeginDate.set(2, iMonthAndDay[1]);
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

    public void setCourseQuarterBeginDate(List<String> cqbd){
        courseQuarterBeginDate = cqbd;
    }

    public void setCourseQuarterEndDate(List<String> cqed){
        courseQuarterEndDate = cqed;
    }

    public void setFollowing(boolean following) {
        isFollowing = following;
    }

    public boolean isFollowing() {
        return isFollowing;
    }

    public String getCourseType(){
        return getCourseElement(elementNameList.get(1));
    }

    public String getCourseSection(){
        return getCourseElement(elementNameList.get(2));
    }

    public String getCourseCredit(){
        return getCourseElement(elementNameList.get(3));
    }

    public String getCourseProfessor(){
        return getCourseElement(elementNameList.get(4));
    }

    public String getCourseTime(){
        return getCourseElement(elementNameList.get(5));
    }

    public String getCoursePlace(){
        return getCourseElement(elementNameList.get(6));
    }

    public String getCourseMaxPeople(){
        return getCourseElement(elementNameList.get(7));
    }

    public String getCourseCurrentPeople(){
        return getCourseElement(elementNameList.get(8));
    }

    public String getCourseWaitlistPeople(){
        return getCourseElement(elementNameList.get(9));
    }

    public String getCourseRequestPeople(){
        return getCourseElement(elementNameList.get(10));
    }

    public String getCourseNormalPeople(){
        return getCourseElement(elementNameList.get(11));
    }

    public String getCourseRestriction(){
        return getCourseElement(elementNameList.get(12));
    }

    public String getCourseStatus(){
        return getCourseElement(elementNameList.get(15));
    }

    public boolean isExpanedInDialog() {
        return isExpanedInDialog;
    }

    public void setExpanedInDialog(boolean expanedInDialog) {
        isExpanedInDialog = expanedInDialog;
    }
}
