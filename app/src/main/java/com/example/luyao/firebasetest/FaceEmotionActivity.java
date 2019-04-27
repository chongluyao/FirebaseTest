package com.example.luyao.firebasetest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.IOException;
import java.io.InputStream;

public class FaceEmotionActivity extends AppCompatActivity {

    private static final String TAG = "FaceDetectionActivity";
    Button startBtn;
    TextView textView;
    ImageView imageView;

    FirebaseModelInterpreter firebaseInterpreter;
    FirebaseModelInputOutputOptions inputOutputOptions;
    FirebaseModelInputs inputs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_emotion);

        startBtn = (Button)findViewById(R.id.btn_start);
        textView = (TextView)findViewById(R.id.textView);
        imageView = (ImageView)findViewById(R.id.imageView);

        BitmapFactory.Options opt = new BitmapFactory.Options();
        // opt.in
        InputStream imageStream = null;
        try {
            imageStream = getAssets().open("test_face.png");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);

        FirebaseLocalModel localSource =
                new FirebaseLocalModel.Builder("face_model")  // Assign a name to this model
                        .setAssetFilePath("face_analyze.tflite")
                        .build();
        FirebaseModelManager.getInstance().registerLocalModel(localSource);
        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setLocalModelName("face_model")
                .build();
        try {
            firebaseInterpreter =
                    FirebaseModelInterpreter.getInstance(options);
            inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 64, 64, 1})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})
                            .build();
            // input
            Bitmap new_bitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true);
            int batchNum = 0;
            float[][][][] input = new float[1][64][64][1];
            for (int x = 0; x < 64; x++) {
                for (int y = 0; y < 64; y++) {
                    int pixel = new_bitmap.getPixel(x, y);
                    // Normalize channel values to [-1.0, 1.0]. This requirement varies by
                    // model. For example, some models might require values to be normalized
                    // to the range [0.0, 1.0] instead.

                    // input[batchNum][x][y][0] = (Color.red(pixel) / 255.0f - 0.5f) * 2;
                    input[batchNum][y][x][0] = (Color.red(pixel) / 255.0f - 0.5f) * 2;
                }
            }
            inputs = new FirebaseModelInputs.Builder()
                    .add(input)  // add() as many input arrays as your model requires
                    .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + "time=" + System.currentTimeMillis());
                try {
                    firebaseInterpreter.run(inputs, inputOutputOptions)
                            .addOnSuccessListener(
                                    new OnSuccessListener<FirebaseModelOutputs>() {
                                        @Override
                                        public void onSuccess(FirebaseModelOutputs result) {
                                            // ...
                                            float[][] output = result.getOutput(0);
                                            float[] probabilities = output[0];

                                            Log.d(TAG, "onClick: " + "time=" + System.currentTimeMillis());
                                            Log.d(TAG, "onSuccess: " + probabilities[0] + " " + probabilities[1]);
                                        }
                                    })
                            .addOnFailureListener(
                                    new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Task failed with an exception
                                            // ...
                                        }
                                    });
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
