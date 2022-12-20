package com.henrycompany.agrade.zym;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.henrycompany.agrade.R;
import com.henrycompany.agrade.ToDoListFragment;

import org.w3c.dom.Text;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import android.app.DatePickerDialog;
import android.app.Dialog;
public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "ActionBottomDialog";
    private EditText newTaskText;
    private Button newTaskSaveButton;
    private ImageButton addDate;
    private TextView dateText;

    private DatabaseHandler db;

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.DialogStyle);
    }

    public void onPause() {
        super.onPause();
        getActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.new_task_copy, container, false);
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        newTaskText = requireView().findViewById(R.id.newTaskText);
        newTaskText.setMaxLines(1);
        newTaskSaveButton = getView().findViewById(R.id.newTaskButton);
        addDate = getView().findViewById(R.id.dataButton);
        dateText = getView().findViewById(R.id.newTaskDate);

        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                LocalDate localDate = null;
                int year = 2022;
                int month = 11;
                int day = 22;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    year  = localDate.getYear();
                    month = localDate.getMonthValue();
                    day   = localDate.getDayOfMonth();
                }
                DatePickerDialog dialog = new DatePickerDialog(getContext(),R.style.datepicker, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        dateText.setText(year + "-" + (month+1) + "-" + dayOfMonth);
                    }
                }, year, month-1, day);
                dialog.show();
                dialog.getWindow().setBackgroundDrawableResource(R.drawable.bg_rectangle_calendar);
                dialog.getWindow().setIcon(R.drawable.app_icon);
                dialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(getContext().getResources().getColor(R.color.all));
                dialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(getContext().getResources().getColor(R.color.all));

            }
        });

        dateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDate.callOnClick();
            }
        });
        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if(bundle != null){
            isUpdate = true;
            String task = bundle.getString("task");
            String date = bundle.getString("date");
            newTaskText.setText(task);
            dateText.setText(date);

            assert task != null;

        }

        if(newTaskText.getText().toString().length() > 0) {
            newTaskSaveButton.setEnabled(true);
            newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.all));
        }
        else{
            newTaskSaveButton.setTextColor(Color.GRAY);
            newTaskSaveButton.setEnabled(false);
        }

        db = new DatabaseHandler(getActivity());
        db.openDatabase();
//        newTaskSaveButton.setClickable(false);
        newTaskText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                newTaskSaveButton.setEnabled(false);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("") || s.toString().trim().isEmpty()){
//                    newTaskSaveButton.setClickable(false);

                    newTaskSaveButton.setTextColor(Color.GRAY);
                    newTaskSaveButton.setEnabled(false);
                }
                else{
//                    newTaskSaveButton.setClickable(true);
                    newTaskSaveButton.setEnabled(true);
                    newTaskSaveButton.setTextColor(ContextCompat.getColor(requireContext(), R.color.all));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final boolean finalIsUpdate = isUpdate;
        newTaskSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = newTaskText.getText().toString();
                if(finalIsUpdate){
                    db.updateTask(bundle.getInt("id"), text, dateText.getText().toString());
                }
                else {
                    ToDoModel task = new ToDoModel();
                    task.setTask(text);
                    task.setDate(dateText.getText().toString());
                    task.setStatus(0);
                    db.insertTask(task);
                }
                dismiss();
            }
        });
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog){
        Fragment frag = getActivity().getSupportFragmentManager().findFragmentById(R.id.frameLayout);
        if(frag instanceof DialogCloseListener)
            ((DialogCloseListener)frag).handleDialogClose(dialog);
    }
}