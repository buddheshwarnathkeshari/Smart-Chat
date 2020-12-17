package com.buddheshwar.smartchat.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buddheshwar.smartchat.Message;
import com.buddheshwar.smartchat.MessageAdapter;
import com.buddheshwar.smartchat.R;
import com.buddheshwar.smartchat.ocrapp.activities.OcrCaptureActivity;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseSmartReply;
import com.google.firebase.ml.naturallanguage.smartreply.FirebaseTextMessage;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestion;
import com.google.firebase.ml.naturallanguage.smartreply.SmartReplySuggestionResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    String messageReceiverId,messageReceiverName,messageReceiverImage,messageSenderUid,saveCurrentTime,saveCurrentDate;
    TextView tvName,tvLastSeen;
    CircleImageView imgProfile;
    Toolbar toolbar;
    FirebaseAuth mAuth;
    DatabaseReference rootRef;
    List<Message> messageList=new ArrayList<>();
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    ImageButton imgBtnSendMessage,imgBtnScanText,imgBtnsendFiles;
    EditText etMessage;
    RecyclerView rvUserMEssageList;
    String checker="";

    TextView[] tvSuggestions;
    LinearLayout llSuggestion;
    String myUrl="";
    StorageTask uploadTask;
    Uri fileUri;


    private static final int RC_OCR_CAPTURE = 9003;
    private static final String TAG = "MainActivity";


    private List<FirebaseTextMessage> chatHistory = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);



        tvSuggestions=new TextView[3];
        llSuggestion=findViewById(R.id.ll_suggestions);
        tvSuggestions[0]=findViewById(R.id.tv_reply_1);
        tvSuggestions[1]=findViewById(R.id.tv_reply_2);
        tvSuggestions[2]=findViewById(R.id.tv_reply_3);
        init();
        setInfo();
        setListeners();

        rootRef.child("Messages").child(messageSenderUid).child(messageReceiverId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Message message=snapshot.getValue(Message.class);

                        if(message.getFrom().equals(messageSenderUid)){
                            chatHistory.add(FirebaseTextMessage.createForLocalUser(message.getMessage(),System.currentTimeMillis()));
                           // suggestReplyingMessages(chatHistory);
                            llSuggestion.setVisibility(View.GONE);

                       } else if(message.getFrom().equals(messageReceiverId)){
                            chatHistory.add(FirebaseTextMessage.createForRemoteUser(message.getMessage(),System.currentTimeMillis(),messageReceiverId));

                            llSuggestion.setVisibility(View.VISIBLE);
                            suggestReplyingMessages(chatHistory);
                        }

                        messageList.add(message);
                        messageAdapter.notifyDataSetChanged();
                        rvUserMEssageList.smoothScrollToPosition(rvUserMEssageList.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    private void suggestReplyingMessages(List<FirebaseTextMessage> chat) {

        FirebaseSmartReply smartReply = FirebaseNaturalLanguage.getInstance().getSmartReply();

        smartReply.suggestReplies(chat).addOnSuccessListener(result -> {
            if (result.getStatus() == SmartReplySuggestionResult.STATUS_SUCCESS) {

                int i=0;
                for (SmartReplySuggestion suggestion : result.getSuggestions()) {

                    tvSuggestions[i++].setText(suggestion.getText());

                }
            }
        });
    }

    private void setInfo() {
        tvName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(imgProfile);

        rootRef.child("Users").child(messageReceiverId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.child("UserState").hasChild("state")){
                    String state=snapshot.child("UserState").child("state").getValue().toString();
                    String time=snapshot.child("UserState").child("time").getValue().toString();
                    String date=snapshot.child("UserState").child("date").getValue().toString();

                    if(state.equals("online")){
                        tvLastSeen.setText("Online");
                    }
                    else if(state.equals("offline")){
                        tvLastSeen.setText("Last Seen: "+date+" "+time);
                    }
                }
                else{
                    tvLastSeen.setText("Offline");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void setListeners() {

        imgBtnsendFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]=new CharSequence[]{
                        "Images",
                        "PDF files",
                        "Other files"
                };

                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Select file type");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        switch(i){
                            case 0:
                                checker="image";
                                Intent intent=new Intent();
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                intent.setType("image/*");
                                startActivityForResult(intent.createChooser(intent,"Select Image"),438);

                                break;
                            case 1:
                                checker="pdf";

                                Intent intent2=new Intent();
                                intent2.setAction(Intent.ACTION_GET_CONTENT);
                                intent2.setType("application/pdf");
                                startActivityForResult(intent2.createChooser(intent2,"Select PDF file"),438);



                                break;
                            case 2:
                                checker="docx";
                                Intent intent3=new Intent();
                                intent3.setAction(Intent.ACTION_GET_CONTENT);
                                intent3.setType("application/*");
                                startActivityForResult(intent3.createChooser(intent3,"Select MS-Word file"),438);

                                break;
                        }
                    }
                });
                builder.show();
            }
        });


        imgBtnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });
        imgBtnScanText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CharSequence options[]=new CharSequence[]{

                        "Speak",
                        "Scan"
                };

                AlertDialog.Builder builder=new AlertDialog.Builder(ChatActivity.this);
                builder.setTitle("Get Text");

                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            Intent intent =new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                            if(intent.resolveActivity(getPackageManager())!=null)
                            {
                                startActivityForResult(intent,1001);
                            }
                            else
                            {
                                Toast.makeText(ChatActivity.this,"your device does not support this feature",Toast.LENGTH_SHORT).show();
                            }
                        }
                        else if(i==1){

                            Intent intent = new Intent(getApplicationContext(), OcrCaptureActivity.class);
                            intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                            intent.putExtra(OcrCaptureActivity.UseFlash, false);

                            startActivityForResult(intent, RC_OCR_CAPTURE);
                        }
                    }
                });

                builder.show();


            }
        });
        imgBtnScanText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {

                Intent intent = new Intent(getApplicationContext(), OcrCaptureActivity.class);
                intent.putExtra(OcrCaptureActivity.AutoFocus, true);
                intent.putExtra(OcrCaptureActivity.UseFlash, true);

                startActivityForResult(intent, RC_OCR_CAPTURE);
                return true;
            }
        });

    }

    private void sendMessage() {
        String msg=etMessage.getText().toString();
        if(!TextUtils.isEmpty(msg)){
            String messageSenderRef="Messages/"+ messageSenderUid+"/"+messageReceiverId;
            String messageReceiverRef="Messages/"+ messageReceiverId+"/"+messageSenderUid;

            DatabaseReference userMessageKeyReference=rootRef.child("Messages")
                    .child(messageSenderUid).child(messageReceiverId).push();

            String messagePushId=userMessageKeyReference.getKey();

            Map messageTextBody=new HashMap();
            messageTextBody.put("message",msg);
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderUid);
            messageTextBody.put("to",messageReceiverId);
            messageTextBody.put("messageID",messagePushId);
            messageTextBody.put("time",saveCurrentTime);
            messageTextBody.put("date",saveCurrentDate);


            Map messageBodyDetails=new HashMap();
            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageTextBody);
            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageTextBody);

            rootRef.updateChildren(messageBodyDetails)
            .addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(!task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                    }


                    etMessage.setText(" ");
                }
            });


        }
    }

    private void init() {
        messageReceiverId=getIntent().getExtras().get("USERID").toString();
        messageReceiverName=getIntent().getExtras().get("USERNAME").toString();
        messageReceiverImage=getIntent().getExtras().get("IMAGE").toString();

        rootRef= FirebaseDatabase.getInstance().getReference();

        mAuth=FirebaseAuth.getInstance();
        messageSenderUid=mAuth.getCurrentUser().getUid();

        imgBtnSendMessage=findViewById(R.id.img_btn_send_msg);
        imgBtnScanText=findViewById(R.id.img_btn_scan);
        etMessage=findViewById(R.id.et_message);
        imgBtnsendFiles=findViewById(R.id.img_btn_send_file);


        toolbar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar=getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(false);
        LayoutInflater layoutInflater=(LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView =layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);


        tvName=findViewById(R.id.custom_profile_name);
        tvLastSeen=findViewById(R.id.custom_last_seen);
        imgProfile=findViewById(R.id.custom_profile_image);

        messageAdapter=new MessageAdapter(messageList);
        rvUserMEssageList=findViewById(R.id.message_list_of_users);
        linearLayoutManager=new LinearLayoutManager(this);
       // linearLayoutManager.setReverseLayout(true);
        rvUserMEssageList.setLayoutManager(linearLayoutManager);
        rvUserMEssageList.setAdapter(messageAdapter);





        Calendar calender= Calendar.getInstance();

        SimpleDateFormat currentDate=new SimpleDateFormat("MMM dd,yyyy");
        saveCurrentDate=currentDate.format(calender.getTime());

        SimpleDateFormat currentTime=new SimpleDateFormat("hh:mm a");
        saveCurrentTime=currentTime.format(calender.getTime());


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==1001){
            if(resultCode==RESULT_OK&&data!=null)
            {
                ArrayList<String> res=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                etMessage.setText(res.get(0));
            }
        }
        else if(requestCode == RC_OCR_CAPTURE) {

            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    String text = data.getStringExtra(OcrCaptureActivity.TextBlockObject);
                    etMessage.setText(text);

                } else {
                    Toast.makeText(this, R.string.ocr_failure, Toast.LENGTH_SHORT).show();
                    //statusMessage.setText(R.string.ocr_failure);
                    Log.d(TAG, "No Text captured, intent data is null");
                }
            } else {
                Toast.makeText(this, ""+String.format(getString(R.string.ocr_error),
                        CommonStatusCodes.getStatusCodeString(resultCode)), Toast.LENGTH_SHORT).show();

            }
        }
        else if(requestCode==438 &&resultCode==RESULT_OK  && data!=null &&data.getData()!=null){


            //TODO: loading bar
            fileUri=data.getData();
            String name="file";
            if(fileUri!=null)
            name=new File(fileUri.getPath()).getName();


            Toast.makeText(this, ""+name, Toast.LENGTH_SHORT).show();
            if(!checker.equals("image")){


                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Document Files");

                String messageSenderRef="Messages/"+ messageSenderUid+"/"+messageReceiverId;
                String messageReceiverRef="Messages/"+ messageReceiverId+"/"+messageSenderUid;

                DatabaseReference userMessageKeyReference=rootRef.child("Messages")
                        .child(messageSenderUid).child(messageReceiverId).push();

                String messagePushId=userMessageKeyReference.getKey();

                StorageReference filePath=storageReference.child(messagePushId+"."+checker);


                filePath.putFile(fileUri)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                double p=(100.0*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();

                                //TODO: add progress
                               tvName.setText(""+p);

                            }
                        })
                        .continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful()){
                            throw  task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){



                            Uri downloadUrl=(Uri)task.getResult();
                            myUrl=downloadUrl.toString();


                            Map messageImageBody=new HashMap();
                            messageImageBody.put("message",myUrl);
                            messageImageBody.put("name",fileUri.getLastPathSegment());
                            messageImageBody.put("type",checker);
                            messageImageBody.put("from",messageSenderUid);
                            messageImageBody.put("to",messageReceiverId);
                            messageImageBody.put("messageID",messagePushId);
                            messageImageBody.put("time",saveCurrentTime);
                            messageImageBody.put("date",saveCurrentDate);


                            Map messageBodyDetails=new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageImageBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageImageBody);

                            rootRef.updateChildren(messageBodyDetails);

                            //TODO: loading bar

                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //TODO dismiss loading bar
                        Toast.makeText(ChatActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });


            }
            else {
                StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("Image Files");


                String messageSenderRef="Messages/"+ messageSenderUid+"/"+messageReceiverId;
                String messageReceiverRef="Messages/"+ messageReceiverId+"/"+messageSenderUid;

                DatabaseReference userMessageKeyReference=rootRef.child("Messages")
                        .child(messageSenderUid).child(messageReceiverId).push();

                String messagePushId=userMessageKeyReference.getKey();

                StorageReference filePath=storageReference.child(messagePushId+"."+"jpg");
                uploadTask =filePath.putFile(fileUri);
                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {

                        if(!task.isSuccessful()){
                            throw  task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Uri downloadUrl=(Uri)task.getResult();
                            myUrl=downloadUrl.toString();




                            Map messageImageBody=new HashMap();
                            messageImageBody.put("message",myUrl);
                            messageImageBody.put("name",fileUri.getLastPathSegment());
                            messageImageBody.put("type",checker);
                            messageImageBody.put("from",messageSenderUid);
                            messageImageBody.put("to",messageReceiverId);
                            messageImageBody.put("messageID",messagePushId);
                            messageImageBody.put("time",saveCurrentTime);
                            messageImageBody.put("date",saveCurrentDate);


                            Map messageBodyDetails=new HashMap();
                            messageBodyDetails.put(messageSenderRef+"/"+messagePushId,messageImageBody);
                            messageBodyDetails.put(messageReceiverRef+"/"+messagePushId,messageImageBody);

                            rootRef.updateChildren(messageBodyDetails)
                                    .addOnCompleteListener(new OnCompleteListener() {
                                        @Override
                                        public void onComplete(@NonNull Task task) {
                                            if(!task.isSuccessful()){
                                                Toast.makeText(ChatActivity.this, ""+task.getException(), Toast.LENGTH_SHORT).show();
                                            }


                                            etMessage.setText(" ");
                                        }
                                    });
                        }
                    }
                });
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    public void suggestionClicked(View view) {
        etMessage.setText(((TextView)view).getText().toString());
        sendMessage();
    }


}
