package net.donkeyandperi.zotplanner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourseStaticData {
    public static List<String> presetElementNameList = new ArrayList<>(Arrays.asList("Code", "Type",
            "Sec", "Units", "Instructor", "Time", "Place", "Final", "Max", "Enr", "WL", "Req", "Nor", "Rstr",
            "Textbooks", "Web", "Status"));
    public static String defaultSearchOptionBreadth = "ANY";
    public static String defaultSearchOptionDept = "ALL";
    public static String defaultSearchOptionDivision = "ANY";
    public static String defaultSearchOptionClassType = "ALL";
    public static String defaultSearchOptionShowFinals = "1";
    public static String lastSearchOptionForCheck = "level_value_list";
    public static String defaultClassStatusWL = "Waitl";
    public static String defaultClassStatusOpen = "OPEN";
    public static String defaultClassStatusNewOnly = "NewOnly";
    public static String defaultClassStatusFull = "FULL";
    public static String simplifiedChinese = "zh-cn";
    public static String unitedStatesEnglish = "en-us";
    public static String japaneseJapan = "ja-rJP";
    public static String defaultLanguage = "default";
    public static int checkingTimeInterval1Min = 1;
    public static int checkingTimeInterval3Min = 3;
    public static int checkingTimeInterval5Min = 5;
    public static int checkingTimeInterval10Min = 10;
    public static int checkingTimeInterval15Min = 15;
    public static int checkingTimeInterval30Min = 30;
    public static int checkingTimeInterval60Min = 60;
    public static int checkingTimeInterval120Min = 120;
    public static String fallQuarterCode = "92";
    public static String winterQuarterCode = "03";
    public static String springQuarterCode = "14";
    public static String summerSession1Code = "25";
    public static String summerSession2Code = "76";
    public static String summerSessionQuarterCode = "39";
    public static String summerSessionComCode = "51";
}
