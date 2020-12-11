package com.buddheshwar.smartchat.activities;

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

import com.buddheshwar.smartchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button btnLogin,btnLoginUsingPhone;
    EditText etEmail,etPass;
    TextView tvPhone,tvForget,tvRegister;
    private FirebaseAuth firebaseAuth;
    DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);

                overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                allowUserToLogin();
            }
        });
        btnLoginUsingPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i=new Intent(LoginActivity.this, PhoneLoginActivity.class);
                startActivity(i);

                overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);
                finish();
            }
        });

    }

    private void allowUserToLogin() {

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

        firebaseAuth.signInWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            String currentUID=firebaseAuth.getCurrentUser().getUid();
                            String deviceToken= FirebaseInstanceId.getInstance().getToken();

                            usersRef.child(currentUID).child("device_token")
                                    .setValue(deviceToken)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful()){

                                                sendUserToMain();
                                                Toast.makeText(LoginActivity.this, "Login Successfully...", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                        }
                        else
                            Toast.makeText(LoginActivity.this, "Login failed"+task.getException().toString(), Toast.LENGTH_SHORT).show();
                    }
                });


    }

    private void sendUserToMain() {
        Intent i=new Intent(LoginActivity.this,MainActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(i);

        overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);
    }

    private void init() {
        btnLogin=findViewById(R.id.btn_login);
        etEmail=findViewById(R.id.et_email);
        etPass=findViewById(R.id.et_pass);
        tvPhone=findViewById(R.id.btn_using_phone);
        tvForget=findViewById(R.id.tv_forgot);
        tvRegister=findViewById(R.id.tv_need);
        btnLoginUsingPhone=findViewById(R.id.btn_using_phone);

        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        firebaseAuth=firebaseAuth.getInstance();
    }


    private void SendUserToMainActivity() {

        Intent loginIntent=new Intent(LoginActivity.this,MainActivity.class);
        startActivity(loginIntent);

        overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);
    }
}