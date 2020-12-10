package com.buddheshwar.smartchat.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.buddheshwar.smartchat.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class ProfileActivity extends AppCompatActivity {
String receiveruserid,currentState, senderuserid;
CircleImageView circleImageView;
TextView tvName,tvStatus;
Button btnSend,btnCancel;
DatabaseReference databaseReference,UserRef,chatRequestRef,contactRef,notificationRef;
FirebaseAuth mAuth;

    String s="0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth=FirebaseAuth.getInstance();

        senderuserid =mAuth.getCurrentUser().getUid();
        databaseReference= FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef=FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        notificationRef=FirebaseDatabase.getInstance().getReference().child("Notifications");
        receiveruserid =getIntent().getExtras().getString("UID");
       // Toast.makeText(this, ""+ receiveruserid, Toast.LENGTH_SHORT).show();


        tvName=findViewById(R.id.visit_user_name);
        tvStatus=findViewById(R.id.visit_user_status);
        circleImageView=findViewById(R.id.visit_profile_image);
        btnSend=findViewById(R.id.btn_request);

        btnCancel=findViewById(R.id.btn_decline);
        currentState="new";
        retrieveUSerInfo();


    }

    private void retrieveUSerInfo() {
        databaseReference.child(receiveruserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()&&snapshot.hasChild("image")){
                    String image=snapshot.child("image").getValue().toString();
                    String name=snapshot.child("name").getValue().toString();
                    String status=snapshot.child("status").getValue().toString();

                    tvName.setText(name);
                    tvStatus.setText(status);
                    Picasso.get().load(image).placeholder(R.drawable.profile_image).into(circleImageView);



                    ManageChatRequests();
                }
                else{

                    String status=snapshot.child("status").getValue().toString();
                    String name=snapshot.child("name").getValue().toString();


                    tvName.setText(name);
                    tvStatus.setText(status);

                    ManageChatRequests();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ManageChatRequests() {

        chatRequestRef.child(senderuserid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean flag=false;
                if(snapshot.exists()){


                    if(snapshot.hasChild(receiveruserid)){

                        flag=true;

                        String requesttype=snapshot.child(receiveruserid).child("request_type").getValue().toString();
                        if(requesttype.equals("sent")){
                            currentState="request_sent";
                            btnSend.setText("Cancel chat request");
                        }
                        else if(requesttype.equals("received")){
                            currentState="request_received";
                            btnSend.setText("Accept Chat Request");
                            btnCancel.setVisibility(View.VISIBLE);
                            btnCancel.setEnabled(true);

                           /* btnSend.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    acceptChatRequest();
                                }
                            });*/
                            btnCancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    cancelChatRequest();
                                }
                            });
                        }
                    }
                }

                if(!flag){
                    contactRef.child(senderuserid)
                            .addListenerForSingleValueEvent(new ValueEventListener() {

                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if(snapshot.exists()) {
                                        if (snapshot.hasChild(receiveruserid)) {
                                            currentState = "friends";
                                            btnSend.setText("Remove");//TODO ram ram
                                        }

                                    }

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        if(senderuserid.equals(receiveruserid)){
            btnSend.setVisibility(GONE);
        }
        else{
            btnSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    btnSend.setEnabled(false);
                    if(currentState.equals("new")){
                        sendChatRequest();
                    }
                    else if(currentState.equals("request_sent")){
                        cancelChatRequest();
                    }
                    else if(currentState.equals("request_received")){
                        acceptChatRequest();
                    }
                    else if(currentState.equals("friends")){
                        removeSpecificContact();
                    }

                }
            });
        }
    }

    private void removeSpecificContact() {
        contactRef.child(senderuserid).child(receiveruserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            contactRef.child(receiveruserid).child(senderuserid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                btnSend.setEnabled(true);
                                                if(currentState.equals("request_received")){
                                                    btnCancel.setVisibility(GONE);
                                                    btnSend.setText("Send Message Request");
                                                }

                                                currentState="new";
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptChatRequest() {
        contactRef.child(senderuserid).child(receiveruserid).child("Contacts").setValue("Saved")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    contactRef.child(receiveruserid).child(senderuserid).child("Contacts").setValue("Saved")
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        chatRequestRef.child(senderuserid).child(receiveruserid).removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            chatRequestRef.child(receiveruserid).child(senderuserid).removeValue()
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            btnSend.setEnabled(true);
                                                                            currentState="friends";
                                                                            btnSend.setText("RemoveR");
                                                                            btnCancel.setVisibility(GONE);
                                                                        }
                                                                    });
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
            }
        });


    }

    private void cancelChatRequest() {
        chatRequestRef.child(senderuserid).child(receiveruserid)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){

                            chatRequestRef.child(receiveruserid).child(senderuserid)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                btnSend.setEnabled(true);
                                                if(currentState.equals("request_received")){
                                                    btnCancel.setVisibility(GONE);
                                                    btnSend.setText("Message");
                                                }else{

                                                    btnSend.setText("Send Message Request");
                                                }

                                                currentState="new";
                                            }

                                        }
                                    });
                        }
                    }
                });
    }

    private void sendChatRequest() {
        chatRequestRef.child(senderuserid).child(receiveruserid).child("request_type").setValue("sent")
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    chatRequestRef.child(receiveruserid).child(senderuserid).child("request_type").setValue("received")
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){


                                HashMap<String,String> chatNotification=new HashMap<>();
                                chatNotification.put("from",senderuserid);
                                chatNotification.put("type","request");

                                notificationRef.child(receiveruserid).push()
                                        .setValue(chatNotification)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if(task.isSuccessful()){

                                                    btnSend.setEnabled(true);
                                                    currentState="request_sent";
                                                    btnSend.setText("Cancel chat request");
                                                }
                                            }
                                        });


                            }
                        }
                    });
                }
            }
        });

    }


}