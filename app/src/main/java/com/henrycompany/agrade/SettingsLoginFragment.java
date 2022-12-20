package com.henrycompany.agrade;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.henrycompany.agrade.zym.MyHandler;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SettingsLoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SettingsLoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // Controller class that manage data
    Controller controller;

    public SettingsLoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SettingLogin.
     */
    // TODO: Rename and change types and number of parameters
    public static SettingsLoginFragment newInstance(String param1, String param2) {
        SettingsLoginFragment fragment = new SettingsLoginFragment();
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
        return inflater.inflate(R.layout.fragment_settings_login, container, false);
    }

    private String mode;
    private String account;
    private String password;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.controller = (Controller) getArguments().getSerializable("Controller");

        this.mode = "Sign In";

        TextView titleText = view.findViewById(R.id.settings_login_title);
        TextInputEditText accountInput = view.findViewById(R.id.settings_login_account_textInputEditText);
        TextInputEditText passwordInput = view.findViewById(R.id.settings_login_password_textInputEditText);
        Button confirmButton = view.findViewById(R.id.settings_login_confirmButton);
        TextView warningText = view.findViewById(R.id.settings_login_warningText);
        TextView signUpText = view.findViewById(R.id.settings_login_sign_up);

        signUpText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUpText.setText("");
                titleText.setText("Sign Up Now");
                confirmButton.setText("Sign Up");
                SettingsLoginFragment.this.mode = "Sign Up";
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get user input
                SettingsLoginFragment.this.account = accountInput.getText().toString();
                SettingsLoginFragment.this.password = passwordInput.getText().toString();

                // avoid empty input
                if (SettingsLoginFragment.this.account.length() < 1 || SettingsLoginFragment.this.password.length() < 1){
                    warningText.setText("Account/Password cannot be empty!");
                    return;
                }

                confirmButton.setEnabled(false);

                if (mode.equals("Sign Up")) {
                    MyHandler myHandler = new MyHandler(Looper.myLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == MyHandler.checkAccountExist) {
                                if ((boolean) msg.obj) {
                                    warningText.setText("Account already exists");
                                    confirmButton.setEnabled(true);
                                } else {
                                    SettingsLoginFragment.this.controller.postRequestOnCreateAccount(SettingsLoginFragment.this.account, SettingsLoginFragment.this.password);
                                    titleText.setText("Sign In Now");
                                    confirmButton.setText("Sign In");
                                    warningText.setText("");
                                    signUpText.setText("");
                                    SettingsLoginFragment.this.mode = "Sign In";
                                    Toast.makeText(view.getContext(), "Sign Up Success", Toast.LENGTH_SHORT).show();
                                    confirmButton.setEnabled(true);
                                }
                            }
                        }
                    };
                    SettingsLoginFragment.this.controller.checkAccountExist(SettingsLoginFragment.this.account, myHandler);
                }
                else if (mode.equals("Sign In")) {
                    MyHandler handler = new MyHandler(Looper.myLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == MyHandler.checkPasswordMatchAccount_SUCCESS) {

                                MyHandler handler_getID = new MyHandler(Looper.myLooper()){
                                    @Override
                                    public void handleMessage(Message msg) {
                                        super.handleMessage(msg);
                                        if (msg.what == MyHandler.getUserIDByAccountAndPassword) {
                                            if ((long) msg.obj != -1) {
                                                SettingsLoginFragment.this.controller.setUserID((long) msg.obj);
                                                Fragment fragment = new SettingsFragment();
                                                // pass the controller to fragment
                                                Bundle bundle = new Bundle();
                                                bundle.putSerializable("Controller", SettingsLoginFragment.this.controller);
                                                fragment.setArguments(bundle);
                                                // replace the current fragment
                                                replaceFragment(fragment);
                                            }
                                            else {
                                                warningText.setText("Account/Password not match");
                                                confirmButton.setEnabled(true);
                                            }
                                        }
                                    }
                                };
                                SettingsLoginFragment.this.controller.getUserIDByAccountAndPassword(SettingsLoginFragment.this.account, SettingsLoginFragment.this.password, handler_getID);
                            }
                            else if (msg.what == MyHandler.checkPasswordMatchAccount_NO_ACCOUNT) {
                                warningText.setText("Account not exists");
                                confirmButton.setEnabled(true);
                            }
                            else if (msg.what == MyHandler.checkPasswordMatchAccount_WRONG_PASSWORD) {
                                warningText.setText("Wrong Password");
                                confirmButton.setEnabled(true);
                            }
                        }
                    };
                    SettingsLoginFragment.this.controller.checkPasswordMatchAccount(password,SettingsLoginFragment.this.account, handler);
                }
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