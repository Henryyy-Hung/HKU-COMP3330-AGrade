package com.henrycompany.agrade;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.henrycompany.agrade.zym.MyHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity  {

    private Controller controller;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set up the controller
        this.controller = new Controller();
        this.resumeAccountInfo();

        // initiate tool bar on the top
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        // set the tool bar
        setSupportActionBar(toolbar);

        // set the drawer
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // set the side navigation bar to change fragment when click
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                drawerLayout.closeDrawer(GravityCompat.START);
                String title = null;
                Fragment fragment = null;
                switch (id) {
                    case R.id.nav_paradomo:
                        title = "Paradomo";
                        fragment = new ParadomoFragment();
                        break;
                    case R.id.nav_todoList:
                        title = "To Do List";
                        fragment = new ToDoListFragment();
                        break;
                    case R.id.nav_studyRoom:
                        title = "Study Room";
                        break;
                    case R.id.nav_statistics:
                        title = "Statistics";
                        fragment = new StatisticsFragment();
                        break;
                    case R.id.nav_ranking:
                        title = "Ranking";
                        fragment = new RankingFragment();
                        break;
                    case R.id.nav_settings:
                        ;
                        title = "Settings";
                        fragment = new SettingsFragment();
                        break;
                    default:
                        title = " Paradomo ";
                        fragment = new ParadomoFragment();
                        break;
                }
                if (!MainActivity.this.controller.checkUserHasLogin() && (title.equals("Study Room") || title.equals("Statistics"))) {
                    Toast.makeText(MainActivity.this, "Please Login to Use " + title, Toast.LENGTH_SHORT).show();
                }
                else if (!title.equals("Study Room")) {
                    // set the title bar
                    getSupportActionBar().setTitle(title);
                    // pass the controller to fragment
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Controller", MainActivity.this.controller);
                    fragment.setArguments(bundle);
                    // replace the current fragment
                    replaceFragment(fragment);
                }
                else {

                    getSupportActionBar().setTitle("Study Room");
                    EmptyFragment temp = new EmptyFragment();
                    replaceFragment(temp);

                    MyHandler myHandler = new MyHandler(Looper.myLooper()) {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            if (msg.what == MyHandler.checkUserInStudyRoom) {
                                String title = "Study Room";
                                Fragment fragment = null;
                                if ((boolean) msg.obj) {
                                    fragment = new StudyRoomFragment();
                                } else {
                                    fragment = new StudyRoomJoinFragment();
                                }
                                // set the title bar
                                getSupportActionBar().setTitle(title);
                                // pass the controller to fragment
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("Controller", MainActivity.this.controller);
                                fragment.setArguments(bundle);
                                // replace the current fragment
                                replaceFragment(fragment);
                            }
                        }
                    };
                    MainActivity.this.controller.checkUserInStudyRoom(myHandler);
                }
                return true;
            }
        });

        // set paradomo fragment as the home page
        String title = "Paradomo";
        Fragment fragment = new ParadomoFragment();
        // set the title bar
        getSupportActionBar().setTitle(title);
        // pass the controller to fragment
        Bundle bundle = new Bundle();
        bundle.putSerializable("Controller", MainActivity.this.controller);
        fragment.setArguments(bundle);
        // replace the current fragment
        replaceFragment(fragment);
    }

    // change fragment for the page
    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    @Override
    protected void onStop() {
        super.onStop();
        storeAccountInfo();
    }

    private void storeAccountInfo() {
        SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong("UserID", this.controller.getUserID());
        editor.apply();
    }

    private void resumeAccountInfo() {
        SharedPreferences prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);
        this.controller.setUserID(prefs.getLong("UserID", -1L));

    }
}