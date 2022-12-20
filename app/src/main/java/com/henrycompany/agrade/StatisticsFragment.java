package com.henrycompany.agrade;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.henrycompany.agrade.zym.MyHandler;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StatisticsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StatisticsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Controller class that manage data
    Controller controller;

    public StatisticsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StatisticsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StatisticsFragment newInstance(String param1, String param2) {
        StatisticsFragment fragment = new StatisticsFragment();
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
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.controller = (Controller) getArguments().getSerializable("Controller");

        /* 此处设置顶部数据 */
        TextView totalFocusingTimeFrequency = view.findViewById(R.id.totalFocusingTimeFrequency);
        TextView totalFocusingTimeHours = view.findViewById(R.id.totalFocusingTimeHours);
        TextView totalFocusingTimeDailyAvgHours = view.findViewById(R.id.totalFocusingTimeDailyAvgHours);
        GregorianCalendar date = new GregorianCalendar();

        /* 此处设置每日学习分布饼图 */
        PieChart dailyFocusingDistributionPieChart = view.findViewById(R.id.dailyFocusingDistributionPieChart);

        /* 此处设置每周学习分布饼图 */
        PieChart weeklyFocusingDistributionPieChart = view.findViewById(R.id.weeklyFocusingDistributionPieChart);

        /* 此处设置每月学习趋势折线图 */
        LineChart monthlyFocusingFocusingLineChart = view.findViewById(R.id.monthlyFocusingTrendLineChart);

        // 此处设置年度学习趋势
        LineChart annuallyFocusingFocusingLineChart = view.findViewById(R.id.annuallyFocusingTrendLineChart);

        StatisticsFragment.this.preprocessPieChart(dailyFocusingDistributionPieChart);
        StatisticsFragment.this.preprocessPieChart(weeklyFocusingDistributionPieChart);


        MyHandler myHandler_totalFocusingTimeFrequency = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getTotalFocusingTimeFrequency) {
                    totalFocusingTimeFrequency.setText((String) msg.obj);
                }
            }
        };
        StatisticsFragment.this.controller.getTotalFocusingTimeFrequency(myHandler_totalFocusingTimeFrequency);

        MyHandler myHandler_totalFocusingTimeHours = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getTotalFocusingTimeHours) {
                    totalFocusingTimeHours.setText((String) msg.obj);
                }
            }
        };
        StatisticsFragment.this.controller.getTotalFocusingTimeHours(myHandler_totalFocusingTimeHours);

        MyHandler myHandler_totalFocusingTimeDailyAvgHours = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getTotalFocusingTimeDailyAvgHours) {
                    totalFocusingTimeDailyAvgHours.setText((String) msg.obj);
                }
            }
        };
        StatisticsFragment.this.controller.getTotalFocusingTimeDailyAvgHours(myHandler_totalFocusingTimeDailyAvgHours);


        MyHandler myHandler_dailyFocusingDistributionPieChart = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getDailyFocusingDistributionPieChartValues) {
                    StatisticsFragment.this.initializePieChart(dailyFocusingDistributionPieChart, (ArrayList<Map<String,Object>>) msg.obj);
                    dailyFocusingDistributionPieChart.invalidate();
                }
            }
        };
        StatisticsFragment.this.controller.getDailyFocusingDistributionPieChartValues(myHandler_dailyFocusingDistributionPieChart);


        MyHandler myHandler_weeklyFocusingDistributionPieChart = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getWeeklyFocusingDistributionPieChartValues) {
                    StatisticsFragment.this.initializePieChart(weeklyFocusingDistributionPieChart, (ArrayList<Map<String,Object>>) msg.obj);
                    weeklyFocusingDistributionPieChart.invalidate();
                }
            }
        };
        StatisticsFragment.this.controller.getWeeklyFocusingDistributionPieChartValues(myHandler_weeklyFocusingDistributionPieChart);

        MyHandler myHandler_monthlyFocusingFocusingLineChart = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getMonthlyFocusingFocusingLineChartCoordinates) {
                    StatisticsFragment.this.initializeLineChart(monthlyFocusingFocusingLineChart,  (ArrayList<Map<String, Integer>>) msg.obj, "h",  date.get(Calendar.MONTH)+1 + "-");
                    monthlyFocusingFocusingLineChart.invalidate();
                }
            }
        };
        StatisticsFragment.this.controller.getMonthlyFocusingFocusingLineChartCoordinates(myHandler_monthlyFocusingFocusingLineChart);


        MyHandler myHandler_annuallyFocusingFocusingLineChart = new MyHandler(Looper.myLooper()) {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MyHandler.getAnnuallyFocusingFocusingLineChartCoordinates) {
                    StatisticsFragment.this.initializeLineChart(annuallyFocusingFocusingLineChart,  (ArrayList<Map<String, Integer>>) msg.obj, "h", date.get(Calendar.YEAR) +"-");
                    annuallyFocusingFocusingLineChart.invalidate();
                }
            }
        };
        StatisticsFragment.this.controller.getAnnuallyFocusingFocusingLineChartCoordinates(myHandler_annuallyFocusingFocusingLineChart);
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

    private void initializeLineChart(LineChart lineChart,  ArrayList<Map<String, Integer>> coordinates, String ySuffix, String xPrefix){
        // set up values
        List<Entry> dataEntries = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++){
            dataEntries.add(new Entry(coordinates.get(i).get("xValue"), coordinates.get(i).get("yValue")));
        }

        // set up line chart
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDragEnabled(false);
        lineChart.setDrawGridBackground(false);
        lineChart.setDrawBorders(false);
        lineChart.setBackgroundColor(Color.WHITE);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
        lineChart.animateY(2500);
        lineChart.animateX(1500);

        // set up paint color
        Paint lineChartPaint = lineChart.getPaint(Chart.PAINT_INFO);
        lineChartPaint.setColor(getResources().getColor(R.color.black));

        // set up legend
        Legend legend = lineChart.getLegend();
        legend.setForm(Legend.LegendForm.LINE);
        legend.setTextSize(12f);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);

        // set up axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinimum(0f);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);
        xAxis.setLabelCount(6,false);
        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setAxisMinimum(0f);
        leftYAxis.setLabelCount(8);
        leftYAxis.setDrawGridLines(false);
        leftYAxis.setDrawGridLines(false);
        YAxis rightYaxis = lineChart.getAxisRight();
        rightYaxis.setAxisMinimum(0f);
        rightYaxis.setDrawGridLines(false);
        rightYaxis.setEnabled(false);

        // set up dataset
        LineDataSet lineDataSet = new LineDataSet(dataEntries, "");
        lineDataSet.setColor(R.color.all);
        lineDataSet.setCircleColor(R.color.all);
        lineDataSet.setLineWidth(1f);
        lineDataSet.setCircleRadius(3f);
        lineDataSet.setDrawCircleHole(false);
        lineDataSet.setDrawCircles(false);
        lineDataSet.setValueTextSize(10f);
        lineDataSet.setDrawFilled(true);
        lineDataSet.setFormLineWidth(1f);
        lineDataSet.setFormSize(15.f);
        lineDataSet.setDrawHorizontalHighlightIndicator(false);
        lineDataSet.setDrawVerticalHighlightIndicator(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);

        ArrayList<String> xvalue = new ArrayList<>();
        for (int i = 1; i < 100; i++) {
            xvalue.add(xPrefix + i);
        }
        xAxis.setValueFormatter(new ValueFormatter() {
            public String getAxisLabel(float value, AxisBase axis) {
                return xvalue.get((int) value);
            }
        });
        leftYAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getAxisLabel(float value, AxisBase axis) {
                return (int) value + ySuffix;
            }
        });

        Drawable drawable = getResources().getDrawable(R.drawable.faded_green);
        setChartFillDrawable(drawable, lineChart);
    }

    public void setChartFillDrawable(Drawable drawable, LineChart lineChart) {
        if (lineChart.getData() != null && lineChart.getData().getDataSetCount() > 0) {
            LineDataSet lineDataSet = (LineDataSet) lineChart.getData().getDataSetByIndex(0);
            lineDataSet.setDrawFilled(true);
            lineDataSet.setFillDrawable(drawable);
            lineChart.invalidate();
        }
    }
}
