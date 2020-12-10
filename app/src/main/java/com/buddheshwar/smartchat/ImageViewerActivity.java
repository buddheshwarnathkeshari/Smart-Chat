package com.buddheshwar.smartchat;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {

    ImageView imageView;
    String imageUrl;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        imageView=findViewById(R.id.image_viewer);

        imageUrl=getIntent().getExtras().getString("url");

        Picasso.get().load(imageUrl).into(imageView);
    }
}