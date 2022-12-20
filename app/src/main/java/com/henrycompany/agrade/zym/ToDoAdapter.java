package com.henrycompany.agrade.zym;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.henrycompany.agrade.MainActivity;
import com.henrycompany.agrade.R;

import org.w3c.dom.Text;

import java.util.List;



public class ToDoAdapter extends RecyclerView.Adapter<ToDoAdapter.ViewHolder> {

    private List<ToDoModel> todoList;
    private DatabaseHandler db;
    private Fragment fragment;



    public ToDoAdapter(DatabaseHandler db, Fragment fragment) {
        this.db = db;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_layout, parent, false);
        TextView task = fragment.getView().findViewById(R.id.tasksText);
        if ( getItemCount() == 0 ) {
            task.setText("Add your tasks");
        } else {
            task.setText("Tasks");
        }
        return new ViewHolder(itemView);
    }

    public Fragment getFragment() {
        return fragment;
    }


    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        TextView task = fragment.getView().findViewById(R.id.tasksText);
        if ( getItemCount() == 0 ) {
            task.setText("Add your tasks");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        db.openDatabase();

        final ToDoModel item = todoList.get(position);
        holder.task.setText(item.getTask());
        holder.task.setChecked(toBoolean(item.getStatus()));
        holder.aDate.setText(item.getDate());
        TextView task = fragment.getView().findViewById(R.id.tasksText);
        if ( getItemCount() == 0 ) {
            task.setText("Add your tasks");
        } else {
            task.setText("Tasks");
        }
        holder.task.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    db.updateStatus(item.getId(), 1);
                } else {
                    db.updateStatus(item.getId(), 0);
                }
            }
        });

    }

    private boolean toBoolean(int n) {
        return n != 0;
    }

    @Override
    public int getItemCount() {
        return todoList.size();
    }

    public Context getContext() {
        return fragment.getContext();
    }

    public void setTasks(List<ToDoModel> todoList) {
        this.todoList = todoList;
        notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        ToDoModel item = todoList.get(position);
        db.deleteTask(item.getId());
        todoList.remove(position);
        notifyItemRemoved(position);
    }

    public void editItem(int position) {
        ToDoModel item = todoList.get(position);
        Bundle bundle = new Bundle();
        bundle.putInt("id", item.getId());
        bundle.putString("task", item.getTask());
        bundle.putString("date", item.getDate());
        AddNewTask fragment1 = new AddNewTask();
        fragment1.setArguments(bundle);
        fragment1.show(fragment.getParentFragmentManager(), AddNewTask.TAG);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox task;
        TextView aDate;

        ViewHolder(View view) {
            super(view);
            task = view.findViewById(R.id.todoCheckBox);
            aDate = view.findViewById(R.id.aTaskDate);
        }


    }
}
