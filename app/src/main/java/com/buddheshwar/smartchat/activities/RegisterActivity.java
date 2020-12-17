package com.buddheshwar.smartchat.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.buddheshwar.smartchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity {
    private Button btnRegister;
    EditText etEmail,etPass;
    TextView tvPhone, tvLogin;
    private FirebaseAuth mAuth;

    private ProgressDialog loadingBar;

    DatabaseReference rootReference;
    LottieAnimationView animationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        animationView=findViewById(R.id.loading_animation);
        animationView.setVisibility(View.GONE);

        mAuth=FirebaseAuth.getInstance();
        init();
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));

                overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);
            }
        });


        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewAccount();
            }
        });
    }

    private void createNewAccount() {

        String email=etEmail.getText().toString();
        String password =etPass.getText().toString();
        if(TextUtils.isEmpty(email)){
            Toast.makeText(this, "Enter your email...", Toast.LENGTH_SHORT).show();
            return;
}
        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "Enter your password...", Toast.LENGTH_SHORT).show();
            return;
        }

        animationView.setVisibility(View.VISIBLE);


        /*loadingBar.setTitle("Creating New Account");
        loadingBar.setMessage("Please wait till account creation...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();*/
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            String currentUID=mAuth.getCurrentUser().getUid();
                            String deviceToken= FirebaseInstanceId.getInstance().getToken();


                            rootReference.child("Users").child(currentUID).child("device_token").setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            animationView.setVisibility(View.GONE);

                                        }
                                    });

                            String currentUserId=mAuth.getCurrentUser().getUid();
                            rootReference.child("Users").child(currentUserId).setValue("");
                            Toast.makeText(RegisterActivity.this, "Successfull", Toast.LENGTH_SHORT).show();

                            animationView.setVisibility(View.GONE);

                            sendUserToMain();

                        }
                        else{

                            animationView.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });







    }

    private void sendUserToMain() {
        Intent i=new Intent(RegisterActivity.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);

        overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);
    }

    private void init() {
        btnRegister =findViewById(R.id.btn_register);
        etEmail=findViewById(R.id.et_email);
        etPass=findViewById(R.id.et_pass);
        tvPhone=findViewById(R.id.btn_using_phone);
        tvLogin =findViewById(R.id.tv_already);
        loadingBar=new ProgressDialog(getApplicationContext());
        rootReference= FirebaseDatabase.getInstance().getReference();
    }

    public void sendUserToLogin(){

        startActivity(new Intent(RegisterActivity.this,LoginActivity.class));

        overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);
    }


    public void usingPhone(View view) {

        Intent i=new Intent(RegisterActivity.this, PhoneLoginActivity.class);
        startActivity(i);

        overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);

        finish();
    }
}