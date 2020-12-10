package com.buddheshwar.smartchat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {

    RecyclerView myContactList;

    DatabaseReference userRef,contactRef;
    FirebaseAuth firebaseAuth;

    String currentUID;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ContactsFragment newInstance(String param1, String param2) {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_contacts, container, false);

        myContactList=view.findViewById(R.id.recycler_view);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        firebaseAuth=FirebaseAuth.getInstance();
        currentUID=firebaseAuth.getUid();
        contactRef= FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUID);
        userRef=FirebaseDatabase.getInstance().getReference().child("Users");

        return  view;
    }

    @Override
    public void onStart() {
        super.onStart();


        FirebaseRecyclerOptions options=new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactRef,Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts,ContactViewHolder> adapter=new FirebaseRecyclerAdapter<Contacts, ContactViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ContactViewHolder contactViewHolder, int i, @NonNull Contacts contacts) {

                String userID=getRef(i).getKey();

                userRef.child(userID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){



                            if(snapshot.child("UserState").hasChild("state")){
                                String state=snapshot.child("UserState").child("state").getValue().toString();

                                String time=snapshot.child("UserState").child("time").getValue().toString();

                                String date=snapshot.child("UserState").child("date").getValue().toString();

                                if(state.equals("online")){
                                    contactViewHolder.imgOnline.setVisibility(View.VISIBLE);
                                }
                                else if(state.equals("Offline")){
                                    contactViewHolder.imgOnline.setVisibility(View.INVISIBLE);
                                }


                            }
                            else{

                                contactViewHolder.imgOnline.setVisibility(View.INVISIBLE);
                            }



                        if(snapshot.hasChild("image")){
                            String name=snapshot.child("name").getValue().toString();
                            String status=snapshot.child("status").getValue().toString();
                            String image=snapshot.child("image").getValue().toString();

                            contactViewHolder.tvStatus.setText(status);
                            contactViewHolder.tvName.setText(name);
                            Picasso.get().load(image).placeholder(R.drawable.profile_image).into(contactViewHolder.imgProfile);

                        }
                        else{
                            String name=snapshot.child("name").getValue().toString();
                            String status=snapshot.child("status").getValue().toString();
                            contactViewHolder.tvStatus.setText(status);
                            contactViewHolder.tvName.setText(name);

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
            public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.users_display_layout,parent,false);
                ContactViewHolder contactViewHolder=new ContactViewHolder(view);
                return contactViewHolder;
            }
        };

        myContactList.setAdapter(adapter);
        adapter.startListening();


    }


    public static class ContactViewHolder extends RecyclerView.ViewHolder{
        TextView tvName,tvStatus;
        CircleImageView imgProfile;
        ImageView imgOnline;
        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName=itemView.findViewById(R.id.tv_name);
            tvStatus=itemView.findViewById(R.id.tv_status);
            imgProfile=itemView.findViewById(R.id.user_profile_image);
            imgOnline=itemView.findViewById(R.id.img_online);
        }
    }

}