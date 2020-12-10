package com.buddheshwar.smartchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.buddheshwar.smartchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    Button btnSendOtp,btnVerify;
    EditText etMob,etOTP;
    PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    FirebaseAuth firebaseAuth;
    String mVerificationId;
    PhoneAuthProvider.ForceResendingToken mResendToken;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        btnSendOtp=findViewById(R.id.btn_send_otp);
        btnVerify=findViewById(R.id.btn_verify);
        etMob=findViewById(R.id.et_mobile);
        etOTP=findViewById(R.id.et_otp);
        firebaseAuth=FirebaseAuth.getInstance();

        btnSendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phoneNumber=etMob.getText().toString();
                if(TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Phone number required", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {

                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(firebaseAuth)
                                    .setPhoneNumber(phoneNumber)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(PhoneLoginActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }catch (Exception e){
                    Toast.makeText(PhoneLoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etOTP.setVisibility(View.VISIBLE);
                btnVerify.setVisibility(View.VISIBLE);
                String otpEntered=etOTP.getText().toString();
                if(TextUtils.isEmpty(otpEntered)){
                    Toast.makeText(PhoneLoginActivity.this, "Enter OTP", Toast.LENGTH_SHORT).show();
                }
                else{
                    try {
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, otpEntered);
                        signInWithPhoneAuthCredential(credential);

                    }
                    catch(Exception e){
                        Toast.makeText(PhoneLoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        callbacks =new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {

                btnVerify.setVisibility(View.GONE);
                etOTP.setVisibility(View.GONE);
                etMob.setVisibility(View.VISIBLE);
                btnSendOtp.setVisibility(View.VISIBLE);
                Toast.makeText(PhoneLoginActivity.this, "Invalid...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {


                btnVerify.setVisibility(View.VISIBLE);
                etOTP.setVisibility(View.VISIBLE);
                etMob.setVisibility(View.GONE);
                btnSendOtp.setVisibility(View.GONE);

                mVerificationId = verificationId;
                mResendToken = token;

                Toast.makeText(PhoneLoginActivity.this, "OTP sent..", Toast.LENGTH_SHORT).show();

                // ...


            }
        };
    }



    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {

                            Toast.makeText(PhoneLoginActivity.this, "Logged In", Toast.LENGTH_SHORT).show();

                            sendUserToMain();

                        } else {

                            Toast.makeText(PhoneLoginActivity.this, "Error: "+task.getException().toString(), Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }

    private void sendUserToMain() {
        Intent intent=new Intent(PhoneLoginActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}

