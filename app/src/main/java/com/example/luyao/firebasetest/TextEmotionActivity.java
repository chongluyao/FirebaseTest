package com.example.luyao.firebasetest;

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
import java.util.Arrays;
import java.util.HashMap;

public class TextEmotionActivity extends AppCompatActivity {

    private static final String TAG = "TextDetectionActivity";
    Button startBtn;
    TextView inputTextView, resultTextView;
    HashMap<String, Integer> word2id;
    int seqLength = 60;
    FirebaseModelInterpreter firebaseInterpreter;
    FirebaseModelInputOutputOptions inputOutputOptions;
    FirebaseModelInputs inputs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_emotion);

        startBtn = (Button) findViewById(R.id.btn_start);
        inputTextView = (TextView) findViewById(R.id.textView_input);
        resultTextView = (TextView) findViewById(R.id.textView_result);

        // get vector
        try {
            // get vocabulary
            InputStream is = getAssets().open("weibo.vocab.txt");
            int length = 0;
            length = is.available();
            byte[]  buffer = new byte[length];
            is.read(buffer);
            String result = new String(buffer, "utf8");
            String[] results = result.split("\n");
            // get word2id
            word2id = new HashMap<>();
            for (int i = 0;i < results.length;i ++) {
                results[i] = results[i].replace(" ", "").replace("　", "");
                word2id.put(results[i], i);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // init model
        FirebaseLocalModel localSource =
                new FirebaseLocalModel.Builder("text_model")  // Assign a name to this model
                        .setAssetFilePath("text_analyze2.tflite")
                        .build();
        FirebaseModelManager.getInstance().registerLocalModel(localSource);
        Log.d(TAG, "onCreate: load option");
        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                .setLocalModelName("text_model")
                .build();
        try {
            firebaseInterpreter =
                    FirebaseModelInterpreter.getInstance(options);
            inputOutputOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.INT32, new int[]{1, 60})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 2})
                            .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get text
                String inputText = (String) inputTextView.getText();
                char[] characters = inputText.toCharArray();
                for (int i = 0;i < characters.length;i ++) {
                    Log.i(TAG, "onCreate: " + characters[i]);
                }
                // get vector
                int[][] vector = new int[1][60];
                int point = seqLength - 1;
                for (int i = characters.length - 1;i >= 0;i --) {
                    String s = String.valueOf(characters[i]);
                    vector[0][point] = word2id.get(s);
                    point --;
                }
                for (int i = 0;i < 1;i ++) {
                    for (int j = 0;j < seqLength;j ++) {
                        Log.d(TAG, "onClick: " + vector[i][j]);
                    }
                }
                Log.i(TAG, "onCreate: " + Arrays.toString(vector));
                // get inputs
                try {
                    Log.d(TAG, "onClick: start build");
                    inputs = new FirebaseModelInputs.Builder()
                            .add(vector)  // add() as many input arrays as your model requires
                            .build();
                    Log.d(TAG, "onClick: start run");
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
                                            Log.d(TAG, "onFailure: " + e.toString());
                                        }
                                    });
                } catch (FirebaseMLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}
