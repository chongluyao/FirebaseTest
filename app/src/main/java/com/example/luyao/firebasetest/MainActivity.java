package com.example.luyao.firebasetest;

import android.content.Intent;
import android.media.FaceDetector;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    Button faceDetectionBtn;
    Button faceEmotionBtn;
    Button textEmotionBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize firebase
        FirebaseApp.initializeApp(this);

        faceDetectionBtn = (Button) findViewById(R.id.btn_face_detection);
        faceEmotionBtn = (Button) findViewById(R.id.btn_face_emotion);
        textEmotionBtn = (Button) findViewById(R.id.btn_text_emotion);

        faceDetectionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FaceDetectionActivity.class);
                startActivity(intent);
            }
        });

        faceEmotionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FaceEmotionActivity.class);
                startActivity(intent);
            }
        });

        textEmotionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TextEmotionActivity.class);
                startActivity(intent);
            }
        });
    }
}
