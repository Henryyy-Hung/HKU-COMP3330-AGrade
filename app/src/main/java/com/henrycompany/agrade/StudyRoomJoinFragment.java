package com.henrycompany.agrade;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.henrycompany.agrade.zym.MyHandler;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StudyRoomJoinFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StudyRoomJoinFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Controller class that manage data
    Controller controller;

    public StudyRoomJoinFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StudyRoomJoinFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StudyRoomJoinFragment newInstance(String param1, String param2) {
        StudyRoomJoinFragment fragment = new StudyRoomJoinFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_study_room_join, container, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.controller = (Controller) getArguments().getSerializable("Controller");

        EditText newStudyRoomID = view.findViewById(R.id.study_room_join_textInputEditText);
        Button joinStudyRoom = view.findViewById(R.id.study_room_join_joinButton);
        TextView createStudyRoom = view.findViewById(R.id.study_room_join_createText);
        TextView warning = view.findViewById(R.id.study_room_join_warningText);

        joinStudyRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                warning.setText("");
                long studyRoomID;
                try{
                    studyRoomID = Long.parseLong(newStudyRoomID.getText().toString());
                }catch (NumberFormatException ex) {
                    warning.setText("Invalid Input.");
                    return;
                }

                MyHandler myHandler = new MyHandler(Looper.myLooper()){
                    @Override
                    public void handleMessage(android.os.Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == MyHandler.checkStudyRoomExist) {
                            if ((boolean) msg.obj) {
                                MyHandler handler_joinStudyRoom = new MyHandler(Looper.myLooper()){
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        if (msg.what == MyHandler.postRequestOnJoinStudyRoom) {
                                            // go to study room page
                                            Fragment fragment = new StudyRoomFragment();
                                            // pass the controller to fragment
                                            Bundle bundle = new Bundle();
                                            bundle.putSerializable("Controller", StudyRoomJoinFragment.this.controller);
                                            fragment.setArguments(bundle);
                                            // replace the current fragment
                                            replaceFragment(fragment);
                                        }
                                    }
                                };
                                StudyRoomJoinFragment.this.controller.postRequestOnJoinStudyRoom(studyRoomID, handler_joinStudyRoom);
                            } else {
                                warning.setText("No such study room");
                            }
                        }
                    }
                };
                StudyRoomJoinFragment.this.controller.checkStudyRoomExist(studyRoomID, myHandler);
            }
        });

        createStudyRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MyHandler handler = new MyHandler(Looper.myLooper()) {
                    @Override
                    public void handleMessage(android.os.Message msg) {
                        super.handleMessage(msg);
                        if (msg.what == MyHandler.postRequestOnCreateStudyRoom) {
                            MyHandler handler_joinStudyRoom = new MyHandler(Looper.myLooper()){
                                @Override
                                public void handleMessage(Message msg) {
                                    super.handleMessage(msg);
                                    if (msg.what == MyHandler.postRequestOnJoinStudyRoom) {
                                        // go to study room page
                                        Fragment fragment = new StudyRoomFragment();
                                        // pass the controller to fragment
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("Controller", StudyRoomJoinFragment.this.controller);
                                        fragment.setArguments(bundle);
                                        // replace the current fragment
                                        replaceFragment(fragment);
                                    }
                                }
                            };
                            StudyRoomJoinFragment.this.controller.postRequestOnJoinStudyRoom((long) msg.obj, handler_joinStudyRoom);
                        }
                    }
                };
                StudyRoomJoinFragment.this.controller.postRequestOnCreateStudyRoom(handler);

            }
        });
    }

    // change fragment for the page
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}