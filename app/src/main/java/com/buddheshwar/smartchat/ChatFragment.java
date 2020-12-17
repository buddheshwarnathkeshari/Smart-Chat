package com.buddheshwar.smartchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buddheshwar.smartchat.activities.ChatActivity;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class ChatFragment extends Fragment {

    View view;
    RecyclerView recyclerView;

    public ChatFragment() {
        // Required empty public constructor
    }
FirebaseAuth firebaseAuth;
    String currentUserId;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view= inflater.inflate(R.layout.fragment_chat, container, false);


        recyclerView=view.findViewById(R.id.chats_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firebaseAuth=FirebaseAuth.getInstance();
        currentUserId=firebaseAuth.getCurrentUser().getUid();
        chatRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserId);
        usersRef=FirebaseDatabase.getInstance().getReference().child("Users");

        return view;
    }


    DatabaseReference chatRef,usersRef;
    String image;
    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(chatRef,Contacts.class)
                .build();


        FirebaseRecyclerAdapter<Contacts,ChatViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ChatViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder chatViewHolder, int i, @NonNull Contacts contacts) {
                final String usersIds=getRef(i).getKey();

                usersRef.child(usersIds).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                           // String image="default_image";


                            String name = snapshot.child("name").getValue().toString();
                            String image = "default";
                            if (snapshot.hasChild("image")) {
                                image = snapshot.child("image").getValue().toString();

                                Picasso.get().load(image).placeholder(R.drawable.profile_image).into(chatViewHolder.profileImage);
                            }
                            if(snapshot.child("UserState").hasChild("state")){

                                String state=snapshot.child("UserState").child("state").getValue().toString();

                                String time=snapshot.child("UserState").child("time").getValue().toString();

                                String date=snapshot.child("UserState").child("date").getValue().toString();

                                if(state.equals("online")){
                                    chatViewHolder.tvstatus.setText("Online");
                                }
                                else if(state.equals("offline")){
                                    chatViewHolder.tvstatus.setText("Last Seen: "+date+" "+time);
                                }


                            }
                            else{

                                chatViewHolder.tvstatus.setText("offline");
                            }
                            chatViewHolder.tvname.setText(name);

                            String finalImage = image;
                            chatViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {

                                    Intent intent=new Intent(getContext(), ChatActivity.class);
                                    intent.putExtra("USERID",usersIds);
                                    intent.putExtra("USERNAME",name);
                                    intent.putExtra("IMAGE", finalImage);
                                    startActivity(intent);

                                    getActivity().overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

            }

            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);

                return new ChatViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder{

        CircleImageView profileImage;
        TextView tvname,tvstatus;
        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImage=itemView.findViewById(R.id.user_profile_image);
            tvname=itemView.findViewById(R.id.tv_name);
            tvstatus=itemView.findViewById(R.id.tv_status);
        }
    }
}