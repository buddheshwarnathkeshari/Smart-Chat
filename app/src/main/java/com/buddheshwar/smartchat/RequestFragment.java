package com.buddheshwar.smartchat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class RequestFragment extends Fragment {

    DatabaseReference reqRef,userRef,contactRef;


    public RequestFragment() {
        // Required empty public constructor
    }

    RecyclerView myRequestList;
    FirebaseAuth firebaseAuth;
    String uid;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_requests, container, false);

        myRequestList=view.findViewById(R.id.rv_requests);
        myRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseAuth=FirebaseAuth.getInstance();
        uid=firebaseAuth.getUid();



        contactRef=FirebaseDatabase.getInstance().getReference().child("Contacts");
        reqRef= FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(reqRef.child(uid),Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, RequestViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, RequestViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull RequestViewHolder cViewHolder, int i, @NonNull Contacts contacts) {
                String userID=getRef(i).getKey();

                DatabaseReference getTypeRef=getRef(i).child("request_type").getRef();

                getTypeRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if(snapshot.exists()){
                        String type=snapshot.getValue().toString();

                        if(type.equals("received")){
                            userRef.child(userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String name=snapshot.child("name").getValue().toString();
                                    if(snapshot.hasChild("image")){
                                        String status=snapshot.child("status").getValue().toString();
                                        String image=snapshot.child("image").getValue().toString();

                                        cViewHolder.tvStatus.setText(status);
                                        cViewHolder.tvName.setText(name);
                                        Picasso.get().load(image).placeholder(R.drawable.profile_image).into(cViewHolder.imgProfile);


                                    }
                                    else{
                                        String status=snapshot.child("status").getValue().toString();
                                        cViewHolder.tvStatus.setText("Wants to connect with you...");
                                        cViewHolder.tvName.setText(name);

                                    }


                                    cViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            CharSequence options[]=new CharSequence[]{
                                                    "Accept",
                                                    "Cancel"
                                            };

                                            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                            builder.setTitle(name+ "'s request");

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    if(i==0){

                                                        contactRef.child(uid).child(userID).child("Contact").setValue("Saved")
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {

                                                                        if(task.isSuccessful()){
                                                                            contactRef.child(userID).child(uid).child("Contact").setValue("Saved")
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {

                                                                                            if(task.isSuccessful()){

                                                                                                reqRef.child(uid).child(userID).removeValue()
                                                                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                            @Override
                                                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                                                if(task.isSuccessful()){

                                                                                                                    reqRef.child(userID).child(uid).removeValue()
                                                                                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                                                                @Override
                                                                                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                                                                                    Toast.makeText(getContext(), "Added", Toast.LENGTH_SHORT).show();
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
                                                        //TODO: 42 5:45
                                                    }
                                                    else if(i==1){

                                                        reqRef.child(uid).child(userID).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){

                                                                            reqRef.child(userID).child(uid).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            Toast.makeText(getContext(), "Deleted", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });


                                                    }
                                                }
                                            });

                                            builder.show();
                                        }
                                    });


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });
                        }
                        else if(type.equals("sent")){
//                            cViewHolder.itemView.setVisibility(View.GONE);

                            Button btnAccept=cViewHolder.itemView.findViewById(R.id.btn_Accept);
                            Button btnDecline=cViewHolder.itemView.findViewById(R.id.btn_decline);

                            btnAccept.setText("Req Sent");
                            btnDecline.setVisibility(View.GONE);





                            userRef.child(userID).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {

                                    String name=snapshot.child("name").getValue().toString();
                                    if(snapshot.hasChild("image")){
                                        String status=snapshot.child("status").getValue().toString();
                                        String image=snapshot.child("image").getValue().toString();

                                        cViewHolder.tvStatus.setText(status);
                                        cViewHolder.tvName.setText(name);
                                        Picasso.get().load(image).placeholder(R.drawable.profile_image).into(cViewHolder.imgProfile);


                                    }
                                    else{
                                        String status=snapshot.child("status").getValue().toString();
                                        cViewHolder.tvStatus.setText("You have sent a request");
                                        cViewHolder.tvName.setText(name);

                                    }


                                    cViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            CharSequence options[]=new CharSequence[]{

                                                    "Cancel Chat Request"
                                            };

                                            AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                            builder.setTitle("Already sent request");

                                            builder.setItems(options, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                   if(i==0){

                                                        reqRef.child(uid).child(userID).removeValue()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if(task.isSuccessful()){

                                                                            reqRef.child(userID).child(uid).removeValue()
                                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                        @Override
                                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                                            if(task.isSuccessful())
                                                                                            Toast.makeText(getContext(), "You have cancelled the request", Toast.LENGTH_SHORT).show();
                                                                                        }
                                                                                    });
                                                                        }
                                                                    }
                                                                });


                                                    }
                                                }
                                            });

                                            builder.show();
                                        }
                                    });


                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });



                        }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @NonNull
            @Override
            public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_request_display_layout,parent,false);
                return new RequestViewHolder(view);
            }
        };
        myRequestList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class RequestViewHolder extends RecyclerView.ViewHolder{
        TextView tvName,tvStatus;
        CircleImageView imgProfile;
        Button btnAccept,btnDecline;
        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName=itemView.findViewById(R.id.tv_name);
            tvStatus=itemView.findViewById(R.id.tv_status);
            btnAccept=itemView.findViewById(R.id.btn_Accept);
            btnDecline=itemView.findViewById(R.id.btn_decline);
            imgProfile=itemView.findViewById(R.id.user_profile_image);

        }
    }
}