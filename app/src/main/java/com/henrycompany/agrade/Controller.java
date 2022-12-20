package com.henrycompany.agrade;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.auth.User;
import com.google.type.DateTime;
import com.henrycompany.agrade.zym.MyHandler;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Controller implements Serializable {

    // 在初始化控制器时会载入user id
    private long userID;

    private FirebaseFirestore db;
    private final String USER_COLLECTION = "users";

    private final String STUDY_ROOM_COLLECTION = "studyRoom";

    private final String RECORDS = "records";
    private final String USER_REFERENCE = "userReference";

    private final String INROOM = "inRoom";

    private final String HASRECORDS = "hasRecords";

    private final String USERID = "UserID";
    private final String USERNAME = "Name";
    private final String ACCOUNT = "Account";
    private final String PASSWORD = "Password";
    private final String UNIVERSITY = "University";
    private final String FACULTY = "Faculty";

    private final String ROOMID = "RoomID";
    private final String ROOMNAME = "Name";

    private final String CATEGORY = "CategoryName";
    private final String DURATION = "Duration";
    private final String ENDTIME = "EndTime";
    private final String RECORDID = "RecordID";
    private final String DATE = "Date";


    public Controller()  {
        this.userID = -1L;
        this.db = FirebaseFirestore.getInstance();

    }

    public void setUserID(long userID) {
        //query the largest userID in the database
        this.userID = userID;
    }

    public long getUserID() {
        return this.userID;
    }

    // 检查用户是否登录(没登录的话id是-1）
    public boolean checkUserHasLogin() {
        if (this.userID < 0) {
            return false;
        }
        return true;
    }

    // 检查账户是否存在
    public void checkAccountExist(String account, MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(USER_COLLECTION).whereEqualTo(ACCOUNT, account).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                Message msg = new Message();
                                msg.what = MyHandler.checkAccountExist;
                                msg.obj = true;
                                handler.sendMessage(msg);
                            } else {
                                Message msg = new Message();
                                msg.what = MyHandler.checkAccountExist;
                                msg.obj = false;
                                handler.sendMessage(msg);
                            }
                        } else {
                            Message msg = new Message();
                            msg.what = MyHandler.ERROR;
                            msg.obj = task.getException();
                            handler.sendMessage(msg);
                        }
                    }
                });
            }
        });
        thread.start();
    }

    // 检查密码是否能够匹配账号
    public void checkPasswordMatchAccount(String password, String account, MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(USER_COLLECTION).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                if (document.get(ACCOUNT).equals(account)) {
                                    if ( document.get(PASSWORD).equals(password)) {
                                        Message msg = new Message();
                                        msg.what = MyHandler.checkPasswordMatchAccount_SUCCESS;
                                        handler.sendMessage(msg);
                                        return;
                                    } else {
                                        Message msg = new Message();
                                        msg.what = MyHandler.checkPasswordMatchAccount_WRONG_PASSWORD;
                                        handler.sendMessage(msg);
                                        return;
                                    }
                                }
                            }
                            Message msg = new Message();
                            msg.what = MyHandler.checkPasswordMatchAccount_NO_ACCOUNT;
                            handler.sendMessage(msg);
                        }
                        else {
                            Message msg = new Message();
                            msg.what = MyHandler.ERROR;
                            msg.obj = task.getException();
                            handler.sendMessage(msg);
                        }
                    }
                });
            }
        });
        thread.start();
    }

    // 检查自习室是否存在
    public void checkStudyRoomExist(long studyRoomID, MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(STUDY_ROOM_COLLECTION).whereEqualTo(ROOMID, studyRoomID).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().size() > 0) {
                                Message msg = new Message();
                                msg.what = MyHandler.checkStudyRoomExist;
                                msg.obj = true;
                                handler.sendMessage(msg);
                            } else {
                                Message msg = new Message();
                                msg.what = MyHandler.checkStudyRoomExist;
                                msg.obj = false;
                                handler.sendMessage(msg);
                            }
                        } else {
                            Message msg = new Message();
                            msg.what = MyHandler.ERROR;
                            msg.obj = task.getException();
                            handler.sendMessage(msg);
                        }
                    }
                });
            }
        });
        thread.start();
    }

    // 检查用户是否在自习室中
    public void checkUserInStudyRoom(MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(INROOM).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        long studyRoomID = queryDocumentSnapshots.getDocuments().get(0).getLong(ROOMID);
                        if (studyRoomID >= 0) {
                            Message msg = new Message();
                            msg.what = MyHandler.checkUserInStudyRoom;
                            msg.obj = true;
                            handler.sendMessage(msg);
                        }
                        else {
                            Message msg = new Message();
                            msg.what = MyHandler.checkUserInStudyRoom;
                            msg.obj = false;
                            handler.sendMessage(msg);
                        }
                    }
                    else {
                        Message msg = new Message();
                        msg.what = MyHandler.checkUserInStudyRoom;
                        msg.obj = false;
                        handler.sendMessage(msg);
                    }
                });
            }
        });
        thread.start();
    }

    // 使用账号和密码返回用户id
    public void getUserIDByAccountAndPassword(String account, String password, MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(USER_COLLECTION).whereEqualTo(ACCOUNT, account).whereEqualTo(PASSWORD, password).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (DocumentSnapshot document : task.getResult()) {
                                Message msg = new Message();
                                msg.what = MyHandler.getUserIDByAccountAndPassword;
                                msg.obj = document.get(USERID);
                                handler.sendMessage(msg);
                                Log.d("TAG", "get user id");
                                return;
                            }
                        } else {
                            Message msg = new Message();
                            msg.what = MyHandler.ERROR;
                            msg.obj = task.getException();
                            handler.sendMessage(msg);
                        }
                    }
                });
            }
        });
        thread.start();
    }

    // 返回当前用户所属的自习室ID
    public void getUserStudyRoomID(MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(INROOM).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        String studyRoomID = queryDocumentSnapshots.getDocuments().get(0).get(ROOMID).toString();
                        Message msg = new Message();
                        msg.what = MyHandler.getUserStudyRoomID;
                        msg.obj = studyRoomID;
                        handler.sendMessage(msg);
                    }
                });
            }
        });
        thread.start();
    }

    // 返回当前用户所属的自习室name
    public void getUserStudyRoomName(MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(INROOM).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        Long studyRoomID = queryDocumentSnapshots.getDocuments().get(0).getLong(ROOMID);
                        db.collection(STUDY_ROOM_COLLECTION).whereEqualTo(ROOMID, studyRoomID).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                            if (queryDocumentSnapshots1.size() != 0) {
                                String studyRoomName = queryDocumentSnapshots1.getDocuments().get(0).getString(ROOMNAME);
                                Message msg = new Message();
                                msg.what = MyHandler.getUserStudyRoomName;
                                msg.obj = studyRoomName;
                                handler.sendMessage(msg);
                            }
                        });
                    }
                });
            }
        });
        thread.start();
    }

    public void getSettingUserName(MyHandler handler){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(USER_COLLECTION).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        String settingUserName = queryDocumentSnapshots.getDocuments().get(0).getString(USERNAME);
                        Message msg = new Message();
                        msg.what = MyHandler.getSettingUserName;
                        msg.obj = settingUserName;
                        handler.sendMessage(msg);
                    }
                });
            }
        });
        thread.start();
    }

    public void getSettingUserUniversity(MyHandler handler){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(USER_COLLECTION).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        String settingUserUniversity = queryDocumentSnapshots.getDocuments().get(0).getString(UNIVERSITY);
                        Message msg = new Message();
                        msg.what = MyHandler.getSettingUserUniversity;
                        msg.obj = settingUserUniversity;
                        handler.sendMessage(msg);
                    }
                });
            }
        });
        thread.start();
    }

    public void getSettingUserFaculty(MyHandler handler){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(USER_COLLECTION).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        String settingUserFaculty = queryDocumentSnapshots.getDocuments().get(0).getString(FACULTY);
                        Message msg = new Message();
                        msg.what = MyHandler.getSettingUserFaculty;
                        msg.obj = settingUserFaculty;
                        handler.sendMessage(msg);
                    }
                });
            }
        });
        thread.start();
    }

    // 获取用户一共使用了多少次计时器
    public void getTotalFocusingTimeFrequency(MyHandler handler){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(RECORDS).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    Message msg = new Message();
                    msg.what = MyHandler.getTotalFocusingTimeFrequency;
                    msg.obj = String.valueOf(queryDocumentSnapshots.size());
                    handler.sendMessage(msg);
                });
            }
        });
        thread.start();
    }

    // 获取用户总共的专注时间
    public void getTotalFocusingTimeHours(MyHandler handler){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(RECORDS).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    double time = 0;
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        time += document.getDouble(DURATION);
                    }
                    Message msg = new Message();
                    msg.what = MyHandler.getTotalFocusingTimeHours;
                    msg.obj = String.valueOf(Math.round(time));
                    handler.sendMessage(msg);
                });
            }
        });
        thread.start();
    }

    // 获取用户平均每天的专注时间
    public void getTotalFocusingTimeDailyAvgHours(MyHandler handler){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(RECORDS).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    double hours = 0;
                    Date max_date = new Date(Long.MIN_VALUE);
                    Date min_date = new Date(Long.MAX_VALUE);

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        hours += document.getDouble(DURATION);
                        Date myDate = document.getTimestamp(ENDTIME).toDate();
                        if(myDate.compareTo(max_date) > 0) {
                            max_date = myDate;
                        } else if(myDate.compareTo(min_date) < 0) {
                            min_date = myDate;
                        }
                    }

                    long diffInMillies = Math.abs(max_date.getTime() - min_date.getTime());
                    long diff = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS) + 1;

                    double avgHours = hours / diff;

                    Message msg = new Message();
                    msg.what = MyHandler.getTotalFocusingTimeDailyAvgHours;
                    msg.obj = String.format("%.1f", avgHours);;
                    handler.sendMessage(msg);
                });
            }
        });
        thread.start();
    }

    public static boolean isSameDay(Date date1, Date date2) {
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMdd");
        return fmt.format(date1).equals(fmt.format(date2));
    }

    // 获取当前用户所属自习室的其他用户的资料，包括，用户ID，用户名，每日专注时间，以及它的乌龟图标。全部放进array list里。
    public void getUserStudyRoomUserInformation(MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                 // 这个array list是返回值。
                ArrayList<Map<String,Object>> study_room_user_information = new ArrayList<Map<String,Object>>();

                db.collection(INROOM).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.size() != 0) {
                        Long studyRoomID = queryDocumentSnapshots.getDocuments().get(0).getLong(ROOMID);

                        ArrayList<Long> user_ID = new ArrayList<Long>();

                        db.collection(INROOM).whereEqualTo(ROOMID, studyRoomID).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                            for (DocumentSnapshot document : queryDocumentSnapshots1) {
                                user_ID.add(document.getLong(USERID));
                            }

                            db.collection(USER_COLLECTION).get().addOnSuccessListener(queryDocumentSnapshots2 -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots2) {
                                    if (user_ID.contains(document.getLong(USERID).longValue())){
                                        Map<String,Object> map = new HashMap<String,Object>();
                                        map.put("User ID", document.getLong(USERID));
                                        map.put("User Name",document.getString(USERNAME));
                                        map.put("User Turtle Icon", R.drawable.turtle_original);
                                        map.put("User Daily Focusing Hours", 0D);
                                        study_room_user_information.add(map);
                                    }
                                }

                                db.collection(RECORDS).get().addOnSuccessListener(queryDocumentSnapshots3 -> {
                                    Date today = new Date(System.currentTimeMillis());
                                    for (DocumentSnapshot document : queryDocumentSnapshots3) {
                                        Long user_id = document.getLong(USERID);
                                        Double duration = document.getDouble(DURATION);
                                        Date endtime = document.getTimestamp(ENDTIME).toDate();

                                        for (Map<String, Object> map: study_room_user_information) {
                                            if (user_id.equals((Long) map.get("User ID")) && isSameDay(today, endtime)) {
                                                map.put("User Daily Focusing Hours", (Double) map.get("User Daily Focusing Hours") + duration);
                                            }
                                        }
                                    }

                                    Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
                                        public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                                            if (((Double) m1.get("User Daily Focusing Hours")).equals(((Double)(m2.get("User Daily Focusing Hours")))))
                                                return 0;
                                            else if (((Double) m1.get("User Daily Focusing Hours")) < ((Double)(m2.get("User Daily Focusing Hours"))))
                                                return 1;
                                            else
                                                return -1;
                                        }
                                    };

                                    Collections.sort(study_room_user_information, mapComparator);


                                    for (Map<String, Object> map: study_room_user_information) {
                                        map.put("User Daily Focusing Hours", String.format("%.1f", (Double) map.get("User Daily Focusing Hours")));
                                    }
                                    Message msg = new Message();
                                    msg.what = MyHandler.getUserStudyRoomUserInformation;
                                    msg.obj = study_room_user_information;
                                    handler.sendMessage(msg);
                                });
                            });
                        });
                    }
                });
            }
        });
        thread.start();
    }

    /*
    Controller.this.userID
     */
    public void getDailyFocusingDistributionPieChartValues(MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(RECORDS).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Map<String,Object>> yValues = new ArrayList<Map<String,Object>>();
                    ArrayList<String> categories = new ArrayList<String>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Date endtime = document.getTimestamp(ENDTIME).toDate();
                        Date today = new Date(System.currentTimeMillis());
                        Double duration = document.getDouble(DURATION);
                        String course = document.getString(CATEGORY);

                        if (!isSameDay(today, endtime)) {
                            continue;
                        }

                        if (categories.contains(course)) {
                            for (Map<String, Object> map: yValues) {
                                if (map.get("Label").equals(course)) {
                                    map.put("Value", (float) map.get("Value") + (float) duration.doubleValue() * 60.0f);
                                }
                            }
                        } else {
                            Map<String,Object> map = new HashMap<String,Object>();
                            map.put("Value", (float) (duration.doubleValue()) * 60.0f);
                            map.put("Label", course);
                            yValues.add(map);
                            categories.add(course);
                        }

                    }
                    Message msg = new Message();
                    msg.what = MyHandler.getDailyFocusingDistributionPieChartValues;
                    msg.obj = yValues;
                    handler.sendMessage(msg);

                });
            }
        });
        thread.start();
    }

    // 获取图表资料
    public void getDailyFocusingDistributionPieChartValuesByUserID(long userID, MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(RECORDS).whereEqualTo(USERID, userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Map<String,Object>> yValues = new ArrayList<Map<String,Object>>();
                    ArrayList<String> categories = new ArrayList<String>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Date endtime = document.getTimestamp(ENDTIME).toDate();
                        Date today = new Date(System.currentTimeMillis());
                        Double duration = document.getDouble(DURATION);
                        String course = document.getString(CATEGORY);

                        if (!isSameDay(today, endtime)) {
                            continue;
                        }

                        if (categories.contains(course)) {
                            for (Map<String, Object> map: yValues) {
                                if (map.get("Label").equals(course)) {
                                    map.put("Value", (float) map.get("Value") + (float) duration.doubleValue() * 60.0f);
                                }
                            }
                        } else {
                            Map<String,Object> map = new HashMap<String,Object>();
                            map.put("Value", (float) (duration.doubleValue()) * 60.0f);
                            map.put("Label", course);
                            yValues.add(map);
                            categories.add(course);
                        }
                    }
                    Message msg = new Message();
                    msg.what = MyHandler.getDailyFocusingDistributionPieChartValuesByUserID;
                    msg.obj = yValues;
                    handler.sendMessage(msg);

                });
            }
        });
        thread.start();
    }

    private boolean isSameWeek(Date today, Date tocheck) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(tocheck);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (0 == subYear) {
            if (cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR))
                return true;
        }
        return false;
    }

    public void getWeeklyFocusingDistributionPieChartValues(MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(RECORDS).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Map<String,Object>> yValues = new ArrayList<Map<String,Object>>();
                    ArrayList<String> categories = new ArrayList<String>();
                    Date today = new Date(System.currentTimeMillis());
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Date endtime = document.getTimestamp(ENDTIME).toDate();
                        Double duration = document.getDouble(DURATION);
                        String course = document.getString(CATEGORY);

                        if (!isSameWeek(today, endtime)) {
                            continue;
                        }

                        if (categories.contains(course)) {
                            for (Map<String, Object> map: yValues) {
                                if (map.get("Label").equals(course)) {
                                    map.put("Value", (float) map.get("Value") + (float) duration.doubleValue());
                                }
                            }
                        } else {
                            Map<String,Object> map = new HashMap<String,Object>();
                            map.put("Value", (float) (duration.doubleValue()));
                            map.put("Label", course);
                            yValues.add(map);
                            categories.add(course);
                        }
                    }
                    Message msg = new Message();
                    msg.what = MyHandler.getWeeklyFocusingDistributionPieChartValues;
                    msg.obj = yValues;
                    handler.sendMessage(msg);

                });
            }
        });
        thread.start();
    }

    private boolean isSameMonthOF(Date today, Date tocheck) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(tocheck);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (0 == subYear) {
            if (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH))
                return true;
        }
        return false;
    }

    public void getMonthlyFocusingFocusingLineChartCoordinates(MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                db.collection(RECORDS).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Map<String, Integer>> coordinates = new ArrayList<Map<String, Integer>>();
                    Date today = new Date(System.currentTimeMillis());
                    for (int i = 0; i < 31; i ++) {
                        Map<String,Integer> map = new HashMap<String,Integer>();
                        map.put("xValue", i);
                        map.put("yValue", 0);
                        coordinates.add(map);
                    }
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Date endtime = document.getTimestamp(ENDTIME).toDate();
                        Double duration = document.getDouble(DURATION);
                        String course = document.getString(CATEGORY);

                        if (!isSameMonthOF(today, endtime)) {
                            continue;
                        }

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(endtime);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        for (Map<String, Integer> map: coordinates) {
                            if (map.get("xValue") == day) {
                                map.put("yValue", map.get("yValue") + (int) (duration.doubleValue()));
                            }
                        }
                    }
                    Message msg = new Message();
                    msg.what = MyHandler.getMonthlyFocusingFocusingLineChartCoordinates;
                    msg.obj = coordinates;
                    handler.sendMessage(msg);

                });
            }
        });
        thread.start();
    }

    private boolean isSameYear(Date today, Date tocheck) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(today);
        cal2.setTime(tocheck);
        int subYear = cal1.get(Calendar.YEAR) - cal2.get(Calendar.YEAR);
        if (0 == subYear) {
            return true;
        }
        return false;
    }

    public void getAnnuallyFocusingFocusingLineChartCoordinates(MyHandler handler) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
//                ArrayList<Map<String, Integer>> coordinates = new ArrayList<Map<String, Integer>>();
//                for (int i = 0; i < 12; i ++) {
//                    Map<String,Integer> map = new HashMap<String,Integer>();
//                    map.put("xValue", i);
//                    map.put("yValue", 3*i);
//                    coordinates.add(map);
//                }
//                Message msg = new Message();
//                msg.what = MyHandler.getAnnuallyFocusingFocusingLineChartCoordinates;
//                msg.obj = coordinates;
//                handler.sendMessage(msg);
                db.collection(RECORDS).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Map<String, Integer>> coordinates = new ArrayList<Map<String, Integer>>();
                    Date today = new Date(System.currentTimeMillis());
                    for (int i = 0; i < 12; i ++) {
                        Map<String,Integer> map = new HashMap<String,Integer>();
                        map.put("xValue", i);
                        map.put("yValue", 0);
                        coordinates.add(map);
                    }
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Date endtime = document.getTimestamp(ENDTIME).toDate();
                        Double duration = document.getDouble(DURATION);
                        String course = document.getString(CATEGORY);

                        if (!isSameYear(today, endtime)) {
                            continue;
                        }

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(endtime);
                        int month = cal.get(Calendar.MONTH);

                        for (Map<String, Integer> map: coordinates) {
                            if (map.get("xValue") == month) {
                                map.put("yValue", map.get("yValue") + (int) (duration.doubleValue()));
                            }
                        }
                    }
                    Message msg = new Message();
                    msg.what = MyHandler.getAnnuallyFocusingFocusingLineChartCoordinates;
                    msg.obj = coordinates;
                    handler.sendMessage(msg);

                });
            }
        });
        thread.start();
    }

    // 获取学院排名资料
    public void getRankingInformation(int number_of_ranks, MyHandler handler) {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Map<String,Object>> ranking_information = new ArrayList<Map<String,Object>>();

                Map<String,Map<String, Double>> temp = new HashMap<String,Map<String, Double>>();

                db.collection(RECORDS).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    db.collection(USER_COLLECTION).get().addOnSuccessListener(queryDocumentSnapshots2 -> {
                        for (DocumentSnapshot document1 :queryDocumentSnapshots1) {
                            for (DocumentSnapshot document2 :queryDocumentSnapshots2) {
                                if (document1.getLong(USERID).equals(document2.getLong(USERID))) {
                                    String university = document2.getString(UNIVERSITY);
                                    String faculty = document2.getString(FACULTY);
                                    Double duration = document1.getDouble(DURATION);

                                    if (temp.get(university) == null) {
                                        Map<String, Double> map = new HashMap<String, Double>();
                                        temp.put(university, map);
                                    }
                                    if (temp.get(university).get(faculty) == null) {
                                            temp.get(university).put(faculty, duration);
                                    }
                                    else {
                                        temp.get(university).put(faculty, temp.get(university).get(faculty) + duration);
                                    }
                                }
                            }
                        }

                        for (Map.Entry<String, Map<String, Double>> entry1 : temp.entrySet()) {
                            for (Map.Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
                                Map<String,Object> map = new HashMap<String,Object>();
                                map.put("University",entry1.getKey());
                                map.put("Faculty", entry2.getKey());
                                map.put("Hours", entry2.getValue().doubleValue());
                                ranking_information.add(map);
                                Log.d("check map", entry1.getKey() + " " + entry2.getKey() + " " + entry2.getValue());
                            }
                        }

                        // initialize all ranks data
                        for (int i = 0; i < number_of_ranks; i++) {
                            Map<String,Object> map = new HashMap<String,Object>();
                            map.put("University","Null");
                            map.put("Faculty", "Null");
                            map.put("Hours", 0D);
                            ranking_information.add(map);
                        }

                        Comparator<Map<String, Object>> mapComparator = new Comparator<Map<String, Object>>() {
                            public int compare(Map<String, Object> m1, Map<String, Object> m2) {
                                if (((double) m1.get("Hours")) == ((double)(m2.get("Hours"))))
                                    return 0;
                                else if (((double) m1.get("Hours")) < ((double)(m2.get("Hours"))))
                                    return 1;
                                else
                                    return -1;
                            }
                        };

                        Collections.sort(ranking_information, mapComparator);

                        Message msg = new Message();
                        msg.what = MyHandler.getRankingInformation;
                        msg.obj = ranking_information;
                        handler.sendMessage(msg);
                    });

                });

            }
        });
        thread.start();
    }

    // 申请创建一个账户，随机分配一个十位数uid给该账户
    public void postRequestOnCreateAccount(String account, String password) {
        // do something ?
        CollectionReference users = db.collection(USER_COLLECTION);
        users.get().addOnSuccessListener(queryDocumentSnapshots -> {
            long maxID = 0;
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                long tempID = queryDocumentSnapshots.getDocuments().get(i).getLong(USERID);
                if (tempID > maxID) {
                    maxID = tempID;
                }
            }

            this.userID = maxID + 1;

            //create an account into database
            //create a new user into database
            //create a new document of users
            Map<String, Object> data1 = new HashMap<>();
            data1.put(USERID, this.userID);
            data1.put(ACCOUNT, account);
            data1.put(PASSWORD, password);
            data1.put(USERNAME, "User " + this.userID);
            data1.put(UNIVERSITY, "The University of Hong Kong");
            data1.put(FACULTY, "Faculty of Engineering");

            //generate a random document id
            String documentID = UUID.randomUUID().toString();
            db.collection(USER_COLLECTION).document(documentID).set(data1);
        });
    }

    // 创建一个新的自习室，并返回新建自习室的id
    public void postRequestOnCreateStudyRoom(MyHandler handler) {
        db.collection(STUDY_ROOM_COLLECTION).get().addOnSuccessListener(queryDocumentSnapshots -> {
            long maxID = 0;
            for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                long tempID = (long) queryDocumentSnapshots.getDocuments().get(i).get(ROOMID);
                if (tempID > maxID) {
                    maxID = tempID;
                }
            }

            long studyRoomID = maxID + 1;

            //create an account into database
            //create a new user into database
            //create a new document of users
            Map<String, Object> data1 = new HashMap<>();
            data1.put(ROOMID, studyRoomID);
            data1.put("Name", "Room " + studyRoomID);

            //generate a randrom document id
            String documentID = UUID.randomUUID().toString();
            db.collection(STUDY_ROOM_COLLECTION).document(documentID).set(data1);

            Message msg = new Message();
            msg.what = MyHandler.postRequestOnCreateStudyRoom;
            msg.obj = studyRoomID;
            handler.sendMessage(msg);
        });
    }

    // 将该用户加入studyRoomID自习室
    public void postRequestOnJoinStudyRoom(long studyRoomID, MyHandler handler) {
        db.collection(INROOM).whereEqualTo(USERID, this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() != 0) {
                Map<String, Object> data1 = new HashMap<>();
                data1.put(ROOMID, studyRoomID);
                db.collection(INROOM).document(queryDocumentSnapshots.getDocuments().get(0).getId()).update(data1);
            }
            else {
                Map<String, Object> data1 = new HashMap<>();
                data1.put(USERID, this.userID);
                data1.put(ROOMID, studyRoomID);
                String documentID = UUID.randomUUID().toString();
                db.collection(INROOM).document(documentID).set(data1);
            }
            Message msg = new Message();
            msg.what = MyHandler.postRequestOnJoinStudyRoom;
            msg.obj = true;
            handler.sendMessage(msg);
        });
    }

    // 向服务器递交用户退出当前自习室的请求。直接把用户的study room id设为-1就完事了
    public void postRequestOnQuitStudyRoom() {
        db.collection(INROOM).whereEqualTo(USERID, this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() != 0) {
                Map<String, Object> data1 = new HashMap<>();
                data1.put(ROOMID, -1L);
                db.collection(INROOM).document(queryDocumentSnapshots.getDocuments().get(0).getId()).update(data1);
            }
        });
    }

    // 为用户所属的自习室更换name
    public  void postRequestOnSetStudyRoomName(String newStudyRoomName) {
        db.collection(INROOM).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() != 0) {
                Long studyRoomID = queryDocumentSnapshots.getDocuments().get(0).getLong(ROOMID);
                db.collection(STUDY_ROOM_COLLECTION).whereEqualTo(ROOMID, studyRoomID).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                    if (queryDocumentSnapshots1.size() != 0) {
                        Map<String, Object> data1 = new HashMap<>();
                        data1.put(ROOMNAME, newStudyRoomName);
                        db.collection(STUDY_ROOM_COLLECTION).document(queryDocumentSnapshots1.getDocuments().get(0).getId()).update(data1);
                    }
                });
            }
        });
    }

    public  void postRequestOnSetNickName(String nickName) {
        db.collection(USER_COLLECTION).whereEqualTo(USERID, this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() != 0) {
                Map<String, Object> data1 = new HashMap<>();
                data1.put(USERNAME, nickName);
                db.collection(USER_COLLECTION).document(queryDocumentSnapshots.getDocuments().get(0).getId()).update(data1);
            }
        });
    }

    public  void postRequestOnSetPassword(String password) {
        db.collection(USER_COLLECTION).whereEqualTo(USERID, this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() != 0) {
                Map<String, Object> data1 = new HashMap<>();
                data1.put(PASSWORD, password);
                db.collection(USER_COLLECTION).document(queryDocumentSnapshots.getDocuments().get(0).getId()).update(data1);
            }
        });
    }

    public  void postRequestOnSetUniversity(String university) {
        db.collection(USER_COLLECTION).whereEqualTo(USERID, this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() != 0) {
                Map<String, Object> data1 = new HashMap<>();
                data1.put(UNIVERSITY, university);
                db.collection(USER_COLLECTION).document(queryDocumentSnapshots.getDocuments().get(0).getId()).update(data1);
            }
        });
    }

    public  void postRequestOnSetFaculty(String faculty) {
        db.collection(USER_COLLECTION).whereEqualTo(USERID, this.userID).get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.size() != 0) {
                Map<String, Object> data1 = new HashMap<>();
                data1.put(FACULTY, faculty);
                db.collection(USER_COLLECTION).document(queryDocumentSnapshots.getDocuments().get(0).getId()).update(data1);
            }
        });
    }

    // Duration 是分钟，endTime是毫秒，我已经帮你转成格林威治标准时了。
    public void postFocusingRecord(String categoryName, long endTime, long Duration) {

        // 这个endDateTime是格林威治标准时，不是本地时间
        Date endDateTime = new Date(endTime);
        //transfer endTime to timestamp
        Timestamp timestamp = new Timestamp(endDateTime);
        //transfer duration from minute to hours
        double duration = Duration / 60.0;
        db.collection(RECORDS).get().addOnSuccessListener(queryDocumentSnapshots -> {
            Long max1 = 0L;
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Long id = documentSnapshot.getLong(RECORDID);
                if (id > max1) {
                    max1 = id;
                }
            }
            Map<String, Object> data1 = new HashMap<>();
            data1.put(RECORDID, max1 + 1);
            data1.put(CATEGORY, categoryName);
            data1.put(ENDTIME, timestamp);
            data1.put(DURATION, duration);
            data1.put(USERID, this.userID);
            db.collection(RECORDS).document().set(data1);

            db.collection(USER_COLLECTION).whereEqualTo(USERID, Controller.this.userID).get().addOnSuccessListener(queryDocumentSnapshots2 -> {
                if (queryDocumentSnapshots.size() != 0) {
                    data1.put(USER_REFERENCE, queryDocumentSnapshots2.getDocuments().get(0).getReference());
                }
            });

            db.collection(HASRECORDS).get().addOnSuccessListener(queryDocumentSnapshots0 -> {
            Map<String, Object> data2 = new HashMap<>();
            db.collection(USER_COLLECTION).whereEqualTo(USERID, this.userID).get().addOnSuccessListener(queryDocumentSnapshots1 -> {
                if (queryDocumentSnapshots1.size() != 0) {
                    data2.put(USERID, queryDocumentSnapshots1.getDocuments().get(0).getReference());
                    db.collection(RECORDS).whereEqualTo(CATEGORY, categoryName).whereEqualTo(ENDTIME, endTime).whereEqualTo(DURATION, Duration).get().addOnSuccessListener(queryDocumentSnapshots2 -> {
                        if (queryDocumentSnapshots2.size() != 0) {
                            data2.put(RECORDID, queryDocumentSnapshots2.getDocuments().get(0).getReference());
                            db.collection(HASRECORDS).document().set(data2);
                            }
                        });
                    }
                });
            });

        });
    }
}
