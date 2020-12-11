package com.buddheshwar.smartchat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.buddheshwar.smartchat.Contacts;
import com.buddheshwar.smartchat.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindFriendActivity extends AppCompatActivity {

    Toolbar mToolBar;
    RecyclerView recyclerView;
    DatabaseReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friend);

        mToolBar=findViewById(R.id.find_friend_tool_bar);
        recyclerView=findViewById(R.id.recycler_view);
usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        setSupportActionBar(mToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> firebaseRecyclerOptions=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(usersRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,FindFriendViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, FindFriendViewHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendViewHolder findFriendViewHolder, int i, @NonNull Contacts contacts) {

                findFriendViewHolder.tvStatus.setText(contacts.getStatus());
                findFriendViewHolder.tvName.setText(contacts.getName());
                Picasso.get().load(contacts.getImage()).placeholder(R.drawable.profile_image).into(findFriendViewHolder.imgProfile);

                findFriendViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String visit_user_id=getRef(i).getKey();
                        Intent i=new Intent(FindFriendActivity.this,ProfileActivity.class);
                        i.putExtra("UID",visit_user_id);
                        startActivity(i);

                        overridePendingTransition(android.R.anim.accelerate_decelerate_interpolator,android.R.anim.accelerate_decelerate_interpolator);


                    }
                });
            }

            @NonNull
            @Override
            public FindFriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                FindFriendViewHolder viewHolder=new FindFriendViewHolder(view);
                return viewHolder;
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }

    public static class FindFriendViewHolder extends RecyclerView.ViewHolder{

        TextView tvName,tvStatus;
        ImageView imgProfile;
        public FindFriendViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName=itemView.findViewById(R.id.tv_name);
            tvStatus=itemView.findViewById(R.id.tv_status);
            imgProfile=itemView.findViewById(R.id.user_profile_image);

        }
    }
}