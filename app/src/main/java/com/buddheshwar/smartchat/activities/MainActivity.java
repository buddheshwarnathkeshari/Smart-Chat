package com.buddheshwar.smartchat.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.buddheshwar.smartchat.R;
import com.buddheshwar.smartchat.TabsAccessorAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private TabsAccessorAdapter tabsAccessorAdapter;


    String currentUserId;
 //   private FirebaseUser currentUser;
    DatabaseReference rootRef;

    FirebaseAuth firebaseAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Smart Chat");


        firebaseAuth=FirebaseAuth.getInstance();
//        currentUser=firebaseAuth.getCurrentUser();
        viewPager=findViewById(R.id.main_tabs_pager);
        tabsAccessorAdapter=new TabsAccessorAdapter(getSupportFragmentManager());
        viewPager.setAdapter(tabsAccessorAdapter);
        tabLayout=findViewById(R.id.main_tabs);
        rootRef= FirebaseDatabase.getInstance().getReference();
        tabLayout.setupWithViewPager(viewPager);


    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser=firebaseAuth.getCurrentUser();
        if(currentUser==null){
           // SendUserToLoginActivity();
            SendUserToPhoneLoginActivity();
        }
        else{
            updateUserState("online");
            verifyUserExistance();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        FirebaseUser currentUser=firebaseAuth.getCurrentUser();
        if(currentUser!=null){

            updateUserState("offline");
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        FirebaseUser currentUser=firebaseAuth.getCurrentUser();
        if(currentUser!=null){

            updateUserState("offline");
        }
    }

    private void verifyUserExistance() {
        String userId=firebaseAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists()){


                }
                else{
                    sendUserToSettingActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendUserToSettingActivity() {

        Intent settingIntent=new Intent(MainActivity.this,SettingActivity.class);
        startActivity(settingIntent);
        finish();

    }

    private void SendUserToLoginActivity() {

        Intent loginIntent=new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
    private void SendUserToPhoneLoginActivity() {

        Intent loginIntent=new Intent(MainActivity.this,PhoneLoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.option_menu,menu);

        return true;


    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.op_logout:
                updateUserState("offline");
                firebaseAuth.signOut();
                Toast.makeText(this, "Log out", Toast.LENGTH_SHORT).show();
                sendUserToLoginActivity();
                break;

            case R.id.op_find_friend:
                sendUserToFFActivity();
                break;

            case R.id.op_setting:
                startActivity(new Intent(MainActivity.this,SettingActivity.class));
                break;

            case R.id.op_create_group:
                requestNewGroup();
                break;
        }

        return true;
    }

    private void sendUserToFFActivity() {
        Intent intent=new Intent(MainActivity.this,FindFriendActivity.class);
        startActivity(intent);
    }

    private void requestNewGroup() {

        AlertDialog.Builder builder=new AlertDialog.Builder(MainActivity.this,R.style.AlertDialog);
        builder.setTitle("Enter group Name");
        final EditText etGroupName=new EditText(MainActivity.this);
        etGroupName.setHint("e.g. My Group");
        builder.setView(etGroupName);
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName=etGroupName.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(MainActivity.this, "Please enter group name", Toast.LENGTH_SHORT).show();
                }
                else{
                    createNewGroup(groupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               dialogInterface.cancel();
            }
        });


        builder.show();

    }

    private void createNewGroup(final String gName) {

        rootRef.child("Groups").child(gName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(MainActivity.this, gName+" created successfully", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void sendUserToLoginActivity() {
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
    }


    private void updateUserState(String state){
        String saveCurrentTime,saveCurrentDate;
        Calendar calender= Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate=currentDate.format(calender.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calender.getTime());

        HashMap<String, Object> onlineStateMap=new HashMap<>();
        onlineStateMap.put("time", saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);


        currentUserId=firebaseAuth.getCurrentUser().getUid();

        rootRef.child("Users").child(currentUserId).child("UserState")
                .updateChildren(onlineStateMap);




    }
}