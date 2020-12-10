package com.buddheshwar.smartchat.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.buddheshwar.smartchat.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity {

    Button btnUpdate;
    EditText etName,etStatus;
    CircleImageView imgProfile;

    String currentUserId;
    FirebaseAuth firebaseAuth;
    DatabaseReference rootRef;
    StorageReference userProfileImageReference;
    private static final int gallaryInt=1;
    String image;

    Toolbar settingToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        init();


        retrieveUserData();


        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
            }
        });



        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(SettingActivity.this);

             /*   Intent gallaryIntent=new Intent();
                gallaryIntent.setAction(Intent.ACTION_GET_CONTENT);
                gallaryIntent.setType("image/*");
                startActivityForResult(gallaryIntent,gallaryInt);*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if(requestCode==gallaryInt&&resultCode==RESULT_OK&&data!=null){
            Uri uri=data.getData();
           /* CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);*/



        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode==RESULT_OK){

                Uri uri=result.getUri();
                StorageReference filepath=userProfileImageReference.child(currentUserId+".jpg");

                filepath.putFile(uri).continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful()){
                            throw  task.getException();
                        }
                        return filepath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){

                            Uri downloadUrl=(Uri)task.getResult();
                            String myUrl=downloadUrl.toString();




                            rootRef.child("Users").child(currentUserId).child("image").setValue(myUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                Toast.makeText(SettingActivity.this, "Profile Updated Successfullly", Toast.LENGTH_SHORT).show();
                                                Picasso.get().load(myUrl).placeholder(R.drawable.profile_image).into(imgProfile);

                                            }else{
                                                Toast.makeText(SettingActivity.this, task.getException().toString()+"", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                    }
                });

            }
        }
    }

    private void retrieveUserData() {
        rootRef.child("Users").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()&&snapshot.hasChild("name")&&snapshot.hasChild("image")){

                            String name=snapshot.child("name").getValue().toString();
                            String status=snapshot.child("status").getValue().toString();
                            String image=snapshot.child("image").getValue().toString();

                            etName.setText(name);
                            etStatus.setText(status);
                            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(imgProfile);

                        }
                        else if(snapshot.exists()&&snapshot.hasChild("name")){

                            String name=snapshot.child("name").getValue().toString();
                            String status=snapshot.child("status").getValue().toString();

                            etName.setText(name);
                            etStatus.setText(status);
                        }
                        else{
                            Toast.makeText(SettingActivity.this, "Please set and update your information", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

    private void update() {
        String name=etName.getText().toString();
        String status=etStatus.getText().toString();

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please enter username", Toast.LENGTH_SHORT).show();
            return;
        }if(TextUtils.isEmpty(status)){

            Toast.makeText(this, "Please enter about", Toast.LENGTH_SHORT).show();
            return;
        }

        HashMap<String,Object> profileMap=new HashMap<>();
        profileMap.put("uid",currentUserId);
        profileMap.put("name",name);
        profileMap.put("status",status);

        DatabaseReference imageRef=rootRef.child("Users").child(currentUserId).child("image");


        rootRef.child("Users").child(currentUserId).updateChildren(profileMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(SettingActivity.this, "Profile updated ", Toast.LENGTH_SHORT).show();
                            sendUserToMain();
                        }
                        else{
                            Toast.makeText(SettingActivity.this,"Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });


    }

    private void sendUserToMain() {
        Intent i=new Intent(SettingActivity.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);
    }

    private void init() {

        btnUpdate=findViewById(R.id.btn_set);
        etName=findViewById(R.id.et_user_name);
        etStatus=findViewById(R.id.et_status);
        imgProfile=findViewById(R.id.profile_image);

        settingToolbar=findViewById(R.id.setting_tool_bar);
        setSupportActionBar(settingToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Setting");
        firebaseAuth=FirebaseAuth.getInstance();
        currentUserId=firebaseAuth.getCurrentUser().getUid();
        rootRef=FirebaseDatabase.getInstance().getReference();
        userProfileImageReference= FirebaseStorage.getInstance().getReference().child("Profile Images");
    }

    @Override
    public void onBackPressed() {
        verifyUserExistance();
    }

    private void verifyUserExistance() {
        String userId=firebaseAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("name").exists()){

                   /* startActivity(new Intent(SettingActivity.this,MainActivity.class));*/
                    finish();
                    Toast.makeText(SettingActivity.this, "Backable", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(SettingActivity.this, "Update your information", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}