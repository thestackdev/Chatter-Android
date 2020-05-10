package com.firebase.chatter.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.firebase.chatter.PageAdapter;
import com.firebase.chatter.R;
import com.firebase.chatter.fragments.ChatsFragment;
import com.firebase.chatter.fragments.FriendRequests;
import com.firebase.chatter.fragments.FriendsFragment;
import com.firebase.chatter.fragments.SearchFragment;
import com.firebase.chatter.fragments.SettingsFragment;
import com.firebase.chatter.helper.AppAccents;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

import java.util.Objects;

import a.gautham.library.AppUpdater;
import a.gautham.library.helper.Display;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private BottomNavigationView bottomNavigationView;
    private MenuItem prevMenuItem;

    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpUiViews();

        if (firebaseUser != null) {

            String current_user_id = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(current_user_id);
            databaseReference.child("online").setValue("true");
            databaseReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);

        } else {
            sendToLoginActivity();
        }



        AppUpdater appUpdater = new AppUpdater(this);
        appUpdater.setDisplay(Display.DIALOG);
        appUpdater.setUpGithub("krishna0928", "Chatter");
        appUpdater.start();

    }

    @SuppressLint("ResourceType")
    private void setUpUiViews() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        final MaterialToolbar toolbar = findViewById(R.id.toolbarMainActivity);
        setSupportActionBar(toolbar);
        final TextView toolbar_title = findViewById(R.id.toolbar_title_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        RelativeLayout root = findViewById(R.id.root);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);

        AppAccents appAccents = new AppAccents(this);
        appAccents.init();

        root.setBackgroundColor(Color.parseColor(appAccents.getAccentColor()));
        appBarLayout.setBackgroundColor(Color.parseColor(appAccents.getAccentColor()));
        toolbar_title.setTextColor(Color.parseColor(appAccents.getTitleTextColor()));
        bottomNavigationView.setBackgroundColor(Color.parseColor(appAccents.getAccentColor()));

        Window window = this.getWindow();

        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.parseColor(appAccents.getAccentColor()));

        bottomNavigationView.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        switch (item.getItemId()){

                            case R.id.menu_chats:
                                viewPager.setCurrentItem(0);
                                break;
                            case R.id.menu_search:
                                viewPager.setCurrentItem(1);
                                break;
                            case R.id.menu_friends:
                                viewPager.setCurrentItem(2);
                                break;
                            case R.id.menu_friendRequests:
                                viewPager.setCurrentItem(3);
                                break;
                            case R.id.menu_settings:
                                viewPager.setCurrentItem(4);
                                break;

                        }
                        return false;
                    }
                }
        );

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                if (position==0)
                    toolbar_title.setText(getString(R.string.chats));
                else if (position==1)
                    toolbar_title.setText(getString(R.string.search));
                else if (position==2)
                    toolbar_title.setText(getString(R.string.friends));
                else if (position==3)
                    toolbar_title.setText(getString(R.string.requests));
                else if (position==4)
                    toolbar_title.setText(getString(R.string.settings));

                if (prevMenuItem != null) {
                    prevMenuItem.setChecked(false);
                }else {
                    bottomNavigationView.getMenu().getItem(0).setChecked(false);
                }

                bottomNavigationView.getMenu().getItem(position).setChecked(true);
                prevMenuItem = bottomNavigationView.getMenu().getItem(position);

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        setupViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {
        PageAdapter adapter = new PageAdapter(getSupportFragmentManager());

        ChatsFragment chatFragment = new ChatsFragment();
        SearchFragment searchFragment = new SearchFragment();
        FriendsFragment friendsFragment = new FriendsFragment();
        FriendRequests friendRequests = new FriendRequests();
        SettingsFragment settingsFragment = new SettingsFragment();

        adapter.addFragment(chatFragment);
        adapter.addFragment(searchFragment);
        adapter.addFragment(friendsFragment);
        adapter.addFragment(friendRequests);
        adapter.addFragment(settingsFragment);
        viewPager.setAdapter(adapter);
    }

    private void sendToLoginActivity() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        databaseReference.child("online").setValue("true").addOnSuccessListener(aVoid -> {
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference.child("online").setValue(ServerValue.TIMESTAMP);
    }
}