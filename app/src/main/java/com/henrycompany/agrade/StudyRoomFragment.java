package com.henrycompany.agrade;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Looper;
import android.os.Message;
import android.provider.SyncStateContract;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.textservice.TextInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.henrycompany.agrade.zym.MyHandler;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudyRoomFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudyRoomFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Controller class that manage data
    Controller controller;
    ArrayList<Map<String,Object>> study_room_user_information;

    public StudyRoomFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudyRoomFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StudyRoomFragment newInstance(String param1, String param2) {
        StudyRoomFragment fragment = new StudyRoomFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_study_room, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.controller = (Controller) getArguments().getSerializable("Controller");

        // views of displaying information
        FloatingActionButton study_room_floating_button = view.findViewById(R.id.study_room_floating_button);
        ImageView study_room_info_board = view.findViewById(R.id.study_room_info_board);
        TextView study_room_room_name = view.findViewById(R.id.study_room_room_name);
        TextView study_room_room_id = view.findViewById(R.id.study_room_room_id);
        GridView study_room_gridview = (GridView) view.findViewById(R.id.study_room_gridview);

        MyHandler myHandler_room_name = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getUserStudyRoomName) {
                    study_room_room_name.setText((String) msg.obj);
                }
            }
        };
        StudyRoomFragment.this.controller.getUserStudyRoomName(myHandler_room_name);

        MyHandler myHandler_room_id = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getUserStudyRoomID) {
                    study_room_room_id.setText((String) msg.obj);
                }
            }
        };
        StudyRoomFragment.this.controller.getUserStudyRoomID(myHandler_room_id);

        MyHandler myHandler_room_member = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getUserStudyRoomUserInformation) {
                    // initialize study room user data (find id, name, study hour of all member of study room by room id)
                    study_room_user_information = (ArrayList<Map<String,Object>>) msg.obj;
                    // initialize turtles in the grid view
                    List<Map<String,Object>> temp = new ArrayList<Map<String,Object>>();
                    for (int i = 0; i < study_room_user_information.size(); i++) {
                        Map<String,Object> map = new HashMap<String,Object>();
                        map.put("User Name", study_room_user_information.get(i).get("User Name"));
                        map.put("User Daily Focusing Hours", study_room_user_information.get(i).get("User Daily Focusing Hours"));
                        if (i == 0) {
                            map.put("User Turtle Icon", R.drawable.turtle_crown);
                        }
                        else {
                            map.put("User Turtle Icon", study_room_user_information.get(i).get("User Turtle Icon"));
                        }
                        temp.add(map);
                    }
                    SimpleAdapter simpleAdapter = new SimpleAdapter(view.getContext(), temp, R.layout.cell_turtle_info, new String[]{"User Name", "User Daily Focusing Hours", "User Turtle Icon"},new int[] {R.id.study_room_user_name, R.id.study_room_user_study_time, R.id.study_room_user_icon} );
                    study_room_gridview.setAdapter(simpleAdapter);
                    setGridViewHeightBasedOnChildren(study_room_gridview, 3);
                }
            }
        };
        StudyRoomFragment.this.controller.getUserStudyRoomUserInformation(myHandler_room_member);

        // set the onclick listener on each turtle on the grid
        study_room_gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Map<String,Object> user_information = study_room_user_information.get(position);
                long user_id = (long) user_information.get("User ID");
                String user_name = (String) user_information.get("User Name");
                String user_focusing_hours = (String) user_information.get("User Daily Focusing Hours");


                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.DarkGreyDialogTheme);
                builder.setTitle(user_name + "                                                                      ");

                final View dialogue_study_room_popup = getLayoutInflater().inflate(R.layout.dialogue_study_room_popup, null);
                builder.setView(dialogue_study_room_popup);

                // 设置每日专注分布饼图的 占比和对应项目
                TextView study_room_dailyFocusingHours = dialogue_study_room_popup.findViewById(R.id.study_room_dailyFocusingHours);
                study_room_dailyFocusingHours.setText(user_focusing_hours +"");

                PieChart study_room_dailyFocusingDistributionPieChart = dialogue_study_room_popup.findViewById(R.id.study_room_dailyFocusingDistributionPieChart);
                StudyRoomFragment.this.preprocessPieChart(study_room_dailyFocusingDistributionPieChart);

                MyHandler myHandler_dailyFocusingDistributionPieChart = new MyHandler(Looper.myLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == MyHandler.getDailyFocusingDistributionPieChartValuesByUserID) {
                            StudyRoomFragment.this.initializePieChart(study_room_dailyFocusingDistributionPieChart, (ArrayList<Map<String,Object>>) msg.obj);
                            study_room_dailyFocusingDistributionPieChart.invalidate();
                        }
                    }
                };
                StudyRoomFragment.this.controller.getDailyFocusingDistributionPieChartValuesByUserID(user_id, myHandler_dailyFocusingDistributionPieChart);

                AlertDialog alert = builder.create();
                alert.getWindow().setLayout(1000, 400);
                alert.show();
            }
        });

        // set the onclick listener on float button at bottom right corner
        study_room_floating_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(view.getContext(), R.style.CustomDialogTheme);
                builder.setTitle("Warning");
                builder.setMessage("Are you sure you want to Leave this study room?");
                builder.setCancelable(true);
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        StudyRoomFragment.this.controller.postRequestOnQuitStudyRoom();
                        Fragment fragment = new StudyRoomJoinFragment();
                        // pass the controller to fragment
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("Controller", StudyRoomFragment.this.controller);
                        fragment.setArguments(bundle);
                        // replace the current fragment
                        replaceFragment(fragment);
                    }
                });
                builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        // set the onclick listener on study room info board -> to change room name
        study_room_info_board.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.CustomDialogTheme);
                builder.setTitle("Change Name of Room                  ");
                final View category_adding_view = getLayoutInflater().inflate(R.layout.dialogue_setting_input, null);
                builder.setView(category_adding_view);
                EditText input = category_adding_view.findViewById(R.id.dialogue_task_adding_input);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StudyRoomFragment.this.setStudyRoomName(input.getText().toString());
                                study_room_room_name.setText(input.getText().toString());
                            }
                        }
                );
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                AlertDialog alert = builder.create();
                alert.getWindow().setLayout(1000, 400);
                alert.show();
            }
        });
    }

    private void preprocessPieChart(PieChart pieChart) {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setEntryLabelTextSize(12f);

        // 设置饼图底部换行
        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(4f);
        l.setYEntrySpace(0f);
        l.setWordWrapEnabled(true);

        pieChart.setExtraOffsets(5,10,5,5);
        Paint PieChartPaint = pieChart.getPaint(Chart.PAINT_INFO);
        PieChartPaint.setColor(getResources().getColor(R.color.black));
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setDrawHoleEnabled(false);

        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setTransparentCircleRadius(100f);
    }

    private void initializePieChart(PieChart pieChart, ArrayList<Map<String,Object>> yValues){
        if (yValues.size() == 0) {
            return;
        }

        // 设置饼图的颜色
        ArrayList<Integer> piechart_colors = new ArrayList<>();
        piechart_colors.add(Color.rgb(253, 179, 191));
        piechart_colors.add(Color.rgb(183, 225, 225));
        piechart_colors.add(Color.rgb(224, 250, 250));
        piechart_colors.add(Color.rgb(177, 206, 210));
        piechart_colors.add(Color.rgb(253, 231, 202));
        piechart_colors.add(Color.rgb(186, 176, 247));
        piechart_colors.add(Color.rgb(240, 239, 252));
        piechart_colors.add(Color.rgb(234, 231, 225));
        piechart_colors.add(Color.rgb(153, 213, 245));
        piechart_colors.add(Color.rgb(198, 228, 244));
        piechart_colors.add(Color.rgb(236, 236, 236));
        piechart_colors.add(Color.rgb(235, 197, 184));
        piechart_colors.add(Color.rgb(228, 230, 247));
        piechart_colors.add(Color.rgb(213, 209, 231));
        piechart_colors.add(Color.rgb(254, 222, 238));
        piechart_colors.add(Color.rgb(187, 223, 216));

        // 设置饼图的值
        ArrayList<PieEntry> PieChartYValues = new ArrayList<>();
        for (int i = 0; i < yValues.size(); i++){
            PieChartYValues.add(new PieEntry((float) yValues.get(i).get("Value"), (String) yValues.get(i).get("Label")));
        }

        PieDataSet PieChartDataSet = new PieDataSet(PieChartYValues, "");
        PieChartDataSet.setSliceSpace(2f);
        PieChartDataSet.setSelectionShift(5f);
        PieChartDataSet.setValueTextSize(12f);
        PieChartDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        PieChartDataSet.setColors(piechart_colors);

        PieData PieChartPieData = new PieData(PieChartDataSet);
        PieChartPieData.setValueTextSize(12f);
        PieChartPieData.setValueFormatter(new PercentFormatter());
        pieChart.setData(PieChartPieData);
    }


    private void setStudyRoomName(String newStudyRoomName) {
        StudyRoomFragment.this.controller.postRequestOnSetStudyRoomName(newStudyRoomName);
    }

    // change fragment for the page
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    // inflate the grid view to suitable height (avoid it only show 1 row)
    public void setGridViewHeightBasedOnChildren(GridView gridView, int columns) {
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int items = listAdapter.getCount();
        int rows = 0;

        View listItem = listAdapter.getView(0, null, gridView);
        listItem.measure(0, 0);
        totalHeight = listItem.getMeasuredHeight();

        float x = 1;
        if( items > columns ){
            x = ((float) items ) /columns;
            rows =(int) Math.ceil(x);
            totalHeight *= rows;
        }
        ViewGroup.LayoutParams params = gridView.getLayoutParams();
        params.height = totalHeight;
        gridView.setLayoutParams(params);
    }
}
