package com.henrycompany.agrade;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.henrycompany.agrade.zym.MyHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ParadomoFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ParadomoFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyHandler myHandler = new MyHandler(Looper.myLooper());

    // Controller class that manage data
    Controller controller;

    // paradomo info
    private CountDownTimer countDownTimer = null;
    private String currentCategoryName = null;
    private long totalFocusingMinutes = 0;
    private long remainingFocusingMilliseconds = 0;
    private long endingFocusingMilliseconds = 0;
    private boolean timerIsRunning = false;
    private ArrayList<String> categories = null;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ParadomoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ParadomoFragment newInstance(String param1, String param2) {
        ParadomoFragment fragment = new ParadomoFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public ParadomoFragment() {
        // Required empty public constructor
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_paradomo, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // set up the controller
        this.controller = (Controller) getArguments().getSerializable("Controller");

        // resume the previous stored information
        this.resumeTimerInfo();

        // views in paradomo page
        ProgressBar paradomo_circular_progress_bar = view.findViewById(R.id.paradomo_circular_progress_bar);
        LinearLayout paradomo_circular_progress_bar_center_view = view.findViewById(R.id.paradomo_circular_progress_bar_center_view);
        TextView paradomo_time_display_view = view.findViewById(R.id.paradomo_time_display_view);
        TextView paradomo_category_display_view = view.findViewById(R.id.paradomo_category_display_view);
        Button paradomo_start_button = view.findViewById(R.id.paradomo_start_button);
        ImageView paradomo_img_turtle_on_stone = view.findViewById(R.id.paradomo_img_turtle_on_stone);

        // set up default displayed information
        paradomo_circular_progress_bar.setProgress((int) this.totalFocusingMinutes * 100 / 120);
        paradomo_time_display_view.setText(this.totalFocusingMinutes + ":00");
        paradomo_category_display_view.setText(currentCategoryName);

        // set up the slide to adjust function on the circular progress bar
        paradomo_circular_progress_bar.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                // locate the center of the circular progress bar
                float centerX = (float) paradomo_circular_progress_bar.getWidth() / 2;
                float centerY = (float) paradomo_circular_progress_bar.getHeight() / 2;

                // the degree of colored region
                int degree = (int) (Math.atan2(motionEvent.getY() - centerY, motionEvent.getX() -centerX) * 180 / Math.PI);

                // distort the degree by +90
                if (degree <= 0) {
                    degree += 360;
                }
                degree += 90;
                if (degree > 360) {
                    degree -= 360;
                }

                // map the progress of circular progress bar to minutes (n/120)
                totalFocusingMinutes = (int) ((degree* 100 / 360 ) * 1.2);
                totalFocusingMinutes = (int) (totalFocusingMinutes - totalFocusingMinutes % 5) + 5;

                // convert the minutes into milliseconds left
                remainingFocusingMilliseconds = (long) totalFocusingMinutes * 60 * 1000;
                int percentage = (int) totalFocusingMinutes * 100 / 120;

                // set the time display panel on the center of circular progress bar
                paradomo_time_display_view.setText(String.valueOf(totalFocusingMinutes)+":00");

                // set and update the progress of progress bar
                paradomo_circular_progress_bar.setProgress(percentage);
                paradomo_circular_progress_bar.invalidate();

                return true;
            }


        });

        // set up the start button which trigger the timer
        paradomo_start_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // when the timer is running, stop the timer
                if (timerIsRunning && countDownTimer != null){
                    // stop the timer
                    countDownTimer.cancel();
                    timerIsRunning=false;
                    // resume default information
                    totalFocusingMinutes = 30;
                    remainingFocusingMilliseconds = totalFocusingMinutes * 60 * 1000;
                    endingFocusingMilliseconds = System.currentTimeMillis() + remainingFocusingMilliseconds;
                    // resume the view
                    paradomo_start_button.setText("Start");
                    paradomo_circular_progress_bar.setProgress(25);
                    paradomo_time_display_view.setText("30:00");
                    // enable the onclick function
                    paradomo_circular_progress_bar.setEnabled(true);
                    paradomo_circular_progress_bar_center_view.setEnabled(true);
                    // notice user
                    Toast.makeText(getActivity(), "Paradomo Cancelled", Toast.LENGTH_SHORT).show();
                }
                // when the timer is not running, or running before last close, resume the timer
                else {
                    timerIsRunning=true;
                    // update the view
                    paradomo_start_button.setText("Stop");
                    // disable starting another timer
                    paradomo_circular_progress_bar.setEnabled(false);
                    paradomo_circular_progress_bar_center_view.setEnabled(false);
                    // initiate countdown timer
                    countDownTimer = new CountDownTimer(remainingFocusingMilliseconds, 1000) {
                        public void onTick(long millisUntilFinished) {
                            // update the displayed view
                            paradomo_time_display_view.setText(String.format("%02d", ((int) millisUntilFinished / 60000)) + ":" + String.format("%02d", ((int) millisUntilFinished % 60000 / 1000)));
                            paradomo_circular_progress_bar.setProgress((int)(millisUntilFinished*100/(120*60*1000)));
                            // update the information
                            remainingFocusingMilliseconds = millisUntilFinished;
                            endingFocusingMilliseconds = System.currentTimeMillis() + millisUntilFinished;
                        }
                        public void onFinish() {
                            // post the record to the server
                            controller.postFocusingRecord(currentCategoryName, endingFocusingMilliseconds, totalFocusingMinutes);
                            // stop the timer
                            countDownTimer.cancel();
                            timerIsRunning=false;
                            // resume default information
                            totalFocusingMinutes = 30;
                            remainingFocusingMilliseconds = totalFocusingMinutes * 60 * 1000;
                            endingFocusingMilliseconds = System.currentTimeMillis() + remainingFocusingMilliseconds;
                            // resume the view
                            paradomo_start_button.setText("Start");
                            paradomo_circular_progress_bar.setProgress(25);
                            paradomo_time_display_view.setText("30:00");
                            // enable the onclick function
                            paradomo_circular_progress_bar.setEnabled(true);
                            paradomo_circular_progress_bar_center_view.setEnabled(true);
                            // notice user
                            Toast.makeText(getActivity(), "Finished !", Toast.LENGTH_SHORT).show();
                            ParadomoFragment.this.paradomoFinishNotification();
                        }
                    };
                    // start the countdown timer
                    countDownTimer.start();
                }
            }
        });

        // if timer is currently running, resume the timer.
        if (timerIsRunning) {
            paradomo_start_button.callOnClick();
        }

        // set up the category selection dialogues
        paradomo_circular_progress_bar_center_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // set up the category selection dialogue
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.CustomDialogTheme);
                final View category_selection_view = getLayoutInflater().inflate(R.layout.dialogue_category_selection,null);
                builder.setView(category_selection_view);
                builder.setTitle("Select Category                                                     ");
                builder.setCancelable(true);
                builder.setPositiveButton(android.R.string.ok, null);
                AlertDialog category_selection_dialogue = builder.create();
                FlexboxLayout flexboxLayout = category_selection_view.findViewById(R.id.task_selection);

                // set the add new category button at the bottom of the dialog
                category_selection_dialogue.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        Button button = ((AlertDialog) category_selection_dialogue).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setText("ADD");
                        // when click the add category, create another dialogue to allow add function
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // set up the builder
                                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext(), R.style.CustomDialogTheme);
                                builder.setTitle("Input a new category                   ");

                                // set up the layout inside the dialogue
                                final View category_adding_view = getLayoutInflater().inflate(R.layout.dialogue_category_adding,null);
                                builder.setView(category_adding_view);
                                EditText input = category_adding_view.findViewById(R.id.dialogue_task_adding_input);

                                // when click OK, add the category to both category selection dialogue and that array list, you know
                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // get the input string
                                        String category_name = input.getText().toString();

                                        // add the input string to array list
                                        categories.add(category_name);

                                        // add to category selection dialogue and set up its behavior when select and delete
                                        MaterialButton button = new MaterialButton(category_selection_dialogue.getContext());
                                        button.setCornerRadius(30);
                                        button.setText(category_name);
                                        button.setBackgroundColor(button.getContext().getResources().getColor(R.color.all));
                                        button.setTextColor(button.getContext().getResources().getColor(R.color.white));
                                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                        params.setMargins(10,10,10,0);
                                        button.setLayoutParams(params);

                                        // Select the item
                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                currentCategoryName = button.getText().toString();
                                                paradomo_category_display_view.setText(currentCategoryName);
                                                category_selection_dialogue.cancel();
                                            }
                                        });

                                        // Delete the item
                                        button.setOnLongClickListener(new View.OnLongClickListener() {
                                            @Override
                                            public boolean onLongClick(View view) {
                                                AlertDialog.Builder builder = new AlertDialog.Builder(category_selection_view.getContext(), R.style.CustomDialogTheme);
                                                builder.setTitle("Warning");
                                                builder.setMessage("Are you sure you want to delete this item?");
                                                builder.setCancelable(true);
                                                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        categories.remove(button.getText().toString());
                                                        ((ViewGroup) button.getParent()).removeView(button);
                                                    }
                                                });
                                                builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                    }
                                                });
                                                AlertDialog dialog = builder.create();
                                                dialog.show();
                                                return true;
                                            }
                                        });
                                        flexboxLayout.addView(button);
                                    }
                                });
                                // when click cancel, stop adding any new category
                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                builder.show();
                            }
                        });
                    }
                });

                // add all current category in arraylist categories to the category selection dislogue
                for(int i = 0; i < categories.size(); i++) {
                    // set up the button
                    MaterialButton button = new MaterialButton(category_selection_view.getContext());
                    button.setCornerRadius(30);
                    button.setText(categories.get(i));
                    button.setBackgroundColor(button.getContext().getResources().getColor(R.color.all));
                    button.setTextColor(button.getContext().getResources().getColor(R.color.white));
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    params.setMargins(10,10,10,0);
                    button.setLayoutParams(params);

                    // Select the item
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            currentCategoryName = button.getText().toString();
                            paradomo_category_display_view.setText(currentCategoryName);
                            category_selection_dialogue.cancel();
                        }
                    });

                    // Delete the item
                    button.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(category_selection_view.getContext(), R.style.CustomDialogTheme);
                            builder.setTitle("Warning");
                            builder.setMessage("Are you sure you want to delete this item?");
                            builder.setCancelable(true);
                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    categories.remove(button.getText().toString());
                                    ((ViewGroup) button.getParent()).removeView(button);
                                }
                            });
                            builder.setNegativeButton("No",new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                            return true;
                        }
                    });
                    flexboxLayout.addView(button);
                }
                category_selection_dialogue.show();
            }
        });

        paradomo_img_turtle_on_stone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (((int) (Math.random()*100) )%4) {
                    case 0:
                        paradomo_img_turtle_on_stone.setImageDrawable(getResources().getDrawable(R.drawable.turtle_original));
                        break;
                    case 1:
                        paradomo_img_turtle_on_stone.setImageDrawable(getResources().getDrawable(R.drawable.turtle_mosaic));
                        break;
                    case 2:
                        paradomo_img_turtle_on_stone.setImageDrawable(getResources().getDrawable(R.drawable.turtle_simple));
                        break;
                    case 3:
                        paradomo_img_turtle_on_stone.setImageDrawable(getResources().getDrawable(R.drawable.turtle_crown));
                        break;
                    default:
                        paradomo_img_turtle_on_stone.setImageDrawable(getResources().getDrawable(R.drawable.turtle_original));
                        break;
                }
            }
        });
        paradomo_img_turtle_on_stone.setEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        this.storeTimerInfo();
    }

    private void storeTimerInfo() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("currentCategoryName", this.currentCategoryName);
        editor.putLong("totalFocusingMinutes", this.totalFocusingMinutes);
        editor.putLong("remainingFocusingMilliseconds", this.remainingFocusingMilliseconds);
        editor.putLong("endingFocusingMilliseconds", this.endingFocusingMilliseconds);
        editor.putBoolean("timerIsRunning", this.timerIsRunning);
        Gson gson = new Gson();
        String json = gson.toJson(categories);
        editor.putString("categories", json);
        editor.apply();
    }

    private void resumeTimerInfo() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("prefs", Context.MODE_PRIVATE);
        this.currentCategoryName = prefs.getString("currentCategoryName", "Click to set category");
        this.totalFocusingMinutes = prefs.getLong("totalFocusingMinutes", 30);
        this.remainingFocusingMilliseconds = prefs.getLong("remainingFocusingMilliseconds", 30 * 60 * 1000);
        this.endingFocusingMilliseconds = prefs.getLong("endingFocusingMilliseconds", System.currentTimeMillis() + this.remainingFocusingMilliseconds);
        this.timerIsRunning = prefs.getBoolean("timerIsRunning", false);
        if (this.timerIsRunning) {
            remainingFocusingMilliseconds = endingFocusingMilliseconds - System.currentTimeMillis();
        }
        String json = prefs.getString("categories", null);
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<String>>() {}.getType();
        categories = gson.fromJson(json, type);
        if (categories == null){
            categories = new ArrayList<String>();
        }
    }

    private void paradomoFinishNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("notifications", "notifications", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableVibration(true);
            notificationChannel.setVibrationPattern(new long[] {100, 1000, 200, 340});
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), "notifications")
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle("A Grade")
                .setContentText("Paradomo has done! Nice job!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[] {100, 1000, 200, 340})
                .setAutoCancel(true);
        NotificationManagerCompat display = NotificationManagerCompat.from(requireContext());
        display.notify(1, builder.build());
    }
}