package net.donkeyandperi.zotplanner;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;

public class SingleCourse {
    private static final String TAG = "SingleCourse";
    private String courseName;
    private HashMap<String, String> elementList = new HashMap<>();
    private List<String> elementNameList = CourseStaticData.presetElementNameList;
    private String searchOptionYearTerm;
    private boolean isSummer = false;
    private List<String> courseQuarterBeginDate = new ArrayList<>();
    private List<String> courseQuarterEndDate = new ArrayList<>();
    private boolean isFollowing = false;
    private boolean isExpanedInDialog = false;
    private String comments = "";
    private String courseLocationWebsiteLink = null;
    private String courseLocationBuildingName = null;
    private Double courseLocationBuildingLat = 0.0;
    private Double courseLocationBuildingLng = 0.0;

    @NonNull
    @Override
    public String toString() {
        if (courseName.equals("Not selected")) {
            return courseName;
        }
        return "(" + getCourseElement("Type") + ") " + courseName;
    }

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

    public String getCourseCode(){
        if (getCourseName().equals("Not selected")) {
            return null;
        }
        return elementList.get("Code");
    }

    public String getCourseType(){
        return getCourseElement(elementNameList.get(1));
    }

    public String getCourseSection(){
        return getCourseElement(elementNameList.get(2));
    }

    public String getCourseUnits(){
        return getCourseElement(elementNameList.get(3));
    }

    public String getCourseInstructor(){
        return getCourseElement(elementNameList.get(4));
    }

    public String getCourseTime(){
        return getCourseElement(elementNameList.get(5));
    }

    public String getCoursePlace(){
        return getCourseElement(elementNameList.get(6));
    }

    public String getCourseFinals(){
        return getCourseElement(elementNameList.get(7));
    }

    public String getCourseMaxPeople(){
        return getCourseElement(elementNameList.get(8));
    }

    public String getCourseEntrance(){
        return getCourseElement(elementNameList.get(9));
    }

    public String getCourseWaitlistPeople(){
        return getCourseElement(elementNameList.get(10));
    }

    public String getCourseRequestPeople(){
        return getCourseElement(elementNameList.get(11));
    }

    public String getCourseNormalPeople(){
        return getCourseElement(elementNameList.get(12));
    }

    public String getCourseRestriction(){
        return getCourseElement(elementNameList.get(13));
    }

    public String getCourseStatus(){
        return getCourseElement(elementNameList.get(16));
    }

    public boolean isExpanedInDialog() {
        return isExpanedInDialog;
    }

    public void setExpanedInDialog(boolean expanedInDialog) {
        isExpanedInDialog = expanedInDialog;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCourseLocationBuildingName() {
        return courseLocationBuildingName;
    }

    public void setCourseLocationBuildingName(String courseLocationBuildingName) {
        this.courseLocationBuildingName = courseLocationBuildingName;
    }

    public String getCourseLocationWebsiteLink() {
        return courseLocationWebsiteLink;
    }

    public void setCourseLocationWebsiteLink(String courseLocationWebsiteLink) {
        this.courseLocationWebsiteLink = courseLocationWebsiteLink;
    }

    public LatLng getCourseLocationBuildingLatLng() {
        if (courseLocationBuildingLat == 0.0 && courseLocationBuildingLng == 0.0){
            return null;
        }
        return new LatLng(courseLocationBuildingLat, courseLocationBuildingLng);
    }

    public void setCourseLocationBuildingLatLng(double lat, double lng) {
        this.courseLocationBuildingLat = lat;
        this.courseLocationBuildingLng = lng;
    }
}
