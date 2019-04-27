package com.example.luyao.firebasetest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Environment;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.List;

public class FaceDetectionActivity extends AppCompatActivity {

    private static final String TAG = "FaceDetectionActivity";
    Button startBtn;
    TextView textView;
    ImageView imageView;

    FirebaseVisionFaceDetector detector;
    FirebaseVisionImage image;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detection);

        startBtn = (Button)findViewById(R.id.btn_start);
        textView = (TextView)findViewById(R.id.textView);
        imageView = (ImageView)findViewById(R.id.imageView);

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .build();
        detector = FirebaseVision.getInstance()
                .getVisionFaceDetector(options);

        InputStream imageStream = null;
        try {
            imageStream = getAssets().open("test_origin.jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        bitmap = BitmapFactory.decodeStream(imageStream);

        // print bitmap info
        Log.d(TAG, "onCreate: " + "width: " + bitmap.getWidth());
        Log.d(TAG, "onCreate: " + "height: " + bitmap.getHeight());

        // way 1
        // image = FirebaseVisionImage.fromBitmap(bitmap);

        // way 2
        int bytes = bitmap.getByteCount();
        ByteBuffer buf = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buf);

        FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
                .setWidth(144)   // 480x360 is typically sufficient for
                .setHeight(192)  // image recognition
                .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                .setRotation(FirebaseVisionImageMetadata.ROTATION_0)
                .build();
        image = FirebaseVisionImage.fromByteBuffer(buf, metadata);
        saveBitmap(image.getBitmapForDebugging(), "test");

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: start");
                Task<List<FirebaseVisionFace>> result =
                        detector.detectInImage(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<FirebaseVisionFace>>() {
                                            @Override
                                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                                // Task completed successfully
                                                // ...
                                                Log.d(TAG, "onSuccess: finish detect " + faces.size());
                                                for (FirebaseVisionFace face : faces) {
                                                    Rect bounds = face.getBoundingBox();

                                                    // draw rect
                                                    Bitmap newBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                                                    Canvas canvas = new Canvas(newBitmap);
                                                    Paint paint = new Paint();
                                                    paint.setStyle(Paint.Style.STROKE);
                                                    canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.bottom, paint);
                                                    imageView.setImageBitmap(newBitmap);

                                                    // set text
                                                    textView.setText("" + bounds.left + " " + bounds.top + " " + bounds.right + " " + bounds.bottom);
                                                }
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
            }
        });

    }

    private void saveBitmap(Bitmap bitmap, String filename) {

        String dir = Environment.getExternalStorageDirectory() + "/FirebaseTest/";
        long millis = System.currentTimeMillis();
        String timename = "" + millis;
        try {
            File file = new File(dir + timename + filename + ".jpg");
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
