package com.henrycompany.agrade.zym;

import android.os.Handler;
import android.os.Looper;

import com.google.firebase.firestore.FirebaseFirestore;

public class MyHandler extends Handler {

    public static final int ERROR = -1;

    public static final int checkAccountExist = 0;

    public static final int CHECKPASSWORD_SUCCESS = 1;
    public static final int CHECKPASSWORD_FAIL = 2;

    public static final int checkPasswordMatchAccount_SUCCESS = 3;
    public static final int checkPasswordMatchAccount_NO_ACCOUNT = 4;
    public static final int checkPasswordMatchAccount_WRONG_PASSWORD = 5;

    public static final int getUserIDByAccountAndPassword = 6;

    public static final int checkStudyRoomExist = 7;

    public static final int postRequestOnCreateStudyRoom = 8;

    public static final int checkUserInStudyRoom = 9;

    public static final int getUserStudyRoomID = 10;

    public static final int getUserStudyRoomName = 11;

    public static final int getSettingUserName = 12;

    public static final int getSettingUserUniversity = 13;

    public static final int getSettingUserFaculty = 14;

    public static final int getRankingInformation = 15;

    public static final int getTotalFocusingTimeFrequency = 16;

    public static final int getTotalFocusingTimeHours = 17;

    public static final int getTotalFocusingTimeDailyAvgHours = 18;

    public static final int getUserStudyRoomUserInformation = 19;

    public static final int getDailyFocusingDistributionPieChartValues = 20;

    public static final int getDailyFocusingDistributionPieChartValuesByUserID = 21;

    public static final int getWeeklyFocusingDistributionPieChartValues = 22;

    public static final int getMonthlyFocusingFocusingLineChartCoordinates = 23;

    public static final int getAnnuallyFocusingFocusingLineChartCoordinates = 24;

    public static final int postRequestOnJoinStudyRoom = 25;


    public MyHandler(Looper looper) {
        super(looper);
    }
}
