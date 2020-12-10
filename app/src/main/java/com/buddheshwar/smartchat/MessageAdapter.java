package com.buddheshwar.smartchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter  extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    List<Message> userMessagesList;
    FirebaseAuth mAuth;
    DatabaseReference usersRef;

    public MessageAdapter(List<Message> userMessagesList){
        this.userMessagesList =userMessagesList;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_message_layout,parent,false);

        mAuth=FirebaseAuth.getInstance();

        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        String messageSenderId=mAuth.getCurrentUser().getUid();
        Message message= userMessagesList.get(position);
        String fromUserId=message.getFrom();
        String messageTYpe=message.getType();

        usersRef=FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserId);

        //show dp(s)
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.hasChild("image")){
                    String receiverProfileImg=snapshot.child("image").getValue().toString();
                    Picasso.get().load(receiverProfileImg).placeholder(R.drawable.profile_image).into(holder.receiverProfileImage);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //show text message
        if(messageTYpe.equals("text")){


            holder.imgReceiverMsg.setVisibility(View.GONE);
            holder.imgSenderMsg.setVisibility(View.GONE);

            if(fromUserId.equals(messageSenderId)){

                holder.rlSender.setVisibility(View.VISIBLE);
                holder.rlReceiver.setVisibility(View.GONE);
                holder.receiverProfileImage.setVisibility(View.GONE);
                
                
                holder.tvSenderMEssage.setBackgroundResource(R.drawable.sender_message_layout);
                holder.tvSenderMEssage.setText(message.getMessage());           //+"\n\n"+message.getTime()+" - "+message.getDate()
                holder.tvSenderMessageTime.setText(message.getTime());


            }
            else{

                holder.rlSender.setVisibility(View.GONE);
                holder.rlReceiver.setVisibility(View.VISIBLE);
                holder.receiverProfileImage.setVisibility(View.VISIBLE);

                holder.tvReceiverMessage.setBackgroundResource(R.drawable.receiver_message_layout);
                holder.tvReceiverMessage.setText(message.getMessage());           //+"\n\n"+message.getTime()+" - "+message.getDate()
                holder.tvReceiverMessageTime.setText(message.getTime());

            }
        }

        else{
            holder.rlReceiver.setVisibility(View.GONE);
            holder.rlSender.setVisibility(View.GONE);

            if(messageTYpe.equals("image")){
                if(fromUserId.equals(messageSenderId)){
                    holder.imgSenderMsg.setVisibility(View.VISIBLE);
                    holder.imgReceiverMsg.setVisibility(View.GONE);
                    holder.receiverProfileImage.setVisibility(View.GONE);

                    Picasso.get().load(message.getMessage()).into(holder.imgSenderMsg);
                }
                else{
                    holder.imgReceiverMsg.setVisibility(View.VISIBLE);
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.imgSenderMsg.setVisibility(View.GONE);

                    Picasso.get().load(message.getMessage()).into(holder.imgReceiverMsg);
                }
            }
            else if(messageTYpe.equals("pdf")||messageTYpe.equals("docx")){
                if(fromUserId.equals(messageSenderId)){

                    holder.rlReceiver.setVisibility(View.GONE);
                    holder.imgSenderMsg.setVisibility(View.VISIBLE);
                    holder.imgReceiverMsg.setVisibility(View.GONE);
                    holder.receiverProfileImage.setVisibility(View.GONE);

                    Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/wchat-fba14.appspot.com/o/Utility%20files%2Fcopy.png?alt=media&token=95adaba9-d29b-450a-8e21-01a80fd0d51a").into(holder.imgReceiverMsg);


                }
                else{

                    holder.imgReceiverMsg.setVisibility(View.VISIBLE);
                    holder.receiverProfileImage.setVisibility(View.VISIBLE);
                    holder.imgSenderMsg.setVisibility(View.GONE);
                    holder.imgReceiverMsg.setBackgroundResource(R.drawable.file);
                }

             }

        }


        if(fromUserId.equals(messageSenderId)){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(userMessagesList.get(position).getType().equals("pdf")||userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete For Me",
                                "Download and View",
                                "Delete for everyone",
                                "Cancel"
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose Option");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:
                                        deleteSentMessages(position,holder);
                                        break;
                                    case 1:
                                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                        holder.itemView.getContext().startActivity(intent);
                                        break;
                                    case 2:

                                        deleteMessageForEveryone(position,holder);
                                        break;

                                    case 3:

                                        break;
                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete For Me",
                                "Copy",
                                "Delete for everyone",
                                "Cancel"
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose Option");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:
                                        deleteSentMessages(position,holder);
                                        break;
                                    case 1:
                                        Toast.makeText(holder.itemView.getContext(), "will be copied", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 2:

                                        deleteMessageForEveryone(position, holder);
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete For Me",
                                "View full Image",
                                "Delete for everyone",
                                "Cancel"
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose Option");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){

                                    case 0:
                                        deleteSentMessages(position, holder);
                                        break;
                                    case 1:

                                        Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                                        holder.itemView.getContext().startActivity(intent);

                                        break;
                                    case 2:
                                        deleteMessageForEveryone(position, holder);
                                        break;
                                }
                            }
                        });
                        builder.show();
                    }



                }
            });

        }

        else{
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(userMessagesList.get(position).getType().equals("pdf")||userMessagesList.get(position).getType().equals("docx")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete For Me",
                                "Download and View",
                                "Cancel"
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose Option");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:
                                        deleteReceivedMessages(position, holder);
                                        break;
                                    case 1:
                                        Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse(userMessagesList.get(position).getMessage()));
                                        holder.itemView.getContext().startActivity(intent);
                                        break;
                                    case 2:

                                        break;

                                }
                            }
                        });
                        builder.show();
                    }

                    else if(userMessagesList.get(position).getType().equals("text")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete For Me",
                                "Copy",
                                "Cancel"
                        };

                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose Option");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:

                                        deleteReceivedMessages(position, holder);
                                        break;
                                    case 1:
                                        Toast.makeText(holder.itemView.getContext(), "will be copied", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 2:

                                        break;
                                }
                            }
                        });
                        builder.show();
                    }
                    else if(userMessagesList.get(position).getType().equals("image")){
                        CharSequence options[]=new CharSequence[]{
                                "Delete For Me",
                                "View full Image",
                                "Cancel"
                        };
                        AlertDialog.Builder builder=new AlertDialog.Builder(holder.itemView.getContext());
                        builder.setTitle("Choose Option");

                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i){
                                    case 0:
                                        deleteReceivedMessages(position, holder);
                                        break;
                                    case 1:
                                        Intent intent=new Intent(holder.itemView.getContext(),ImageViewerActivity.class);
                                        intent.putExtra("url",userMessagesList.get(position).getMessage());
                                        holder.itemView.getContext().startActivity(intent);
                                       break;
                                }
                            }
                        });
                        builder.show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    private void deleteSentMessages(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();

                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,userMessagesList.size());
                }else
                    Toast.makeText(holder.itemView.getContext(), ""+task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void deleteReceivedMessages(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                    userMessagesList.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position,userMessagesList.size());
               } else
                    Toast.makeText(holder.itemView.getContext(), ""+task.getException(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void deleteMessageForEveryone(final int position, final MessageViewHolder holder){
        DatabaseReference rootRef=FirebaseDatabase.getInstance().getReference();
        rootRef.child("Messages")
                .child(userMessagesList.get(position).getTo())
                .child(userMessagesList.get(position).getFrom())
                .child(userMessagesList.get(position).getMessageID())
                .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){

                    rootRef.child("Messages")
                            .child(userMessagesList.get(position).getFrom())
                            .child(userMessagesList.get(position).getTo())
                            .child(userMessagesList.get(position).getMessageID())
                            .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){

                                userMessagesList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position,userMessagesList.size());
                                Toast.makeText(holder.itemView.getContext(), "Deleted", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }
            }
        });
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder{


        TextView tvSenderMEssage,tvReceiverMessage;
        CircleImageView receiverProfileImage;
        TextView tvSenderMessageTime,tvReceiverMessageTime;
        ImageView imgSenderMsg,imgReceiverMsg;
        RelativeLayout rlSender,rlReceiver;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            tvReceiverMessage=itemView.findViewById(R.id.receiver_message_text);
            tvSenderMEssage=itemView.findViewById(R.id.sender_message_text);
            receiverProfileImage=itemView.findViewById(R.id.message_profile_image);
            imgSenderMsg=itemView.findViewById(R.id.img_sender_view);
            imgReceiverMsg=itemView.findViewById(R.id.img_receiver_view);
            tvSenderMessageTime=itemView.findViewById(R.id.sender_message_time);
            tvReceiverMessageTime=itemView.findViewById(R.id.receiver_message_time);
            rlSender=itemView.findViewById(R.id.rl_sender);
            rlReceiver=itemView.findViewById(R.id.rl_receiver);
        }
    }

}
