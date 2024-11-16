package com.example.facy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button button;
    TextView textview;
    ImageView imageview;
    private final static int REQUEST_IMAGE_CAPTURE=124;
    InputImage firebaseVersion;
    FaceDetector visionFaceDetector;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button=findViewById(R.id.camera_btn);
        textview=findViewById(R.id.text1);
        imageview=findViewById(R.id.imageview);

        FirebaseApp.initializeApp(this);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OpenFile();
            }
        });

        Toast.makeText(this,"App is Starrted",Toast.LENGTH_SHORT).show();

    }

    private void OpenFile() {
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager())!=null){
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

        }else {

            Toast.makeText(this,"Failed",Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bundle bundle=data.getExtras();
        Bitmap bitmap=(Bitmap)bundle.get("data");

        FaceDetectionProcess(bitmap);
        Toast.makeText(this,"Success!",Toast.LENGTH_SHORT).show();

    }

    public void FaceDetectionProcess(Bitmap bitmap){

        textview.setText("Processing Image.....");
        final StringBuilder builder=new StringBuilder();

        BitmapDrawable drawable=(BitmapDrawable) imageview.getDrawable();
        InputImage image =InputImage.fromBitmap(bitmap,0);
        FaceDetectorOptions highAccuracyOpt=new FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .enableTracking().build();
        FaceDetector detector = FaceDetection.getClient(highAccuracyOpt);

        Task<List<Face>> result=detector.process(image);
        result.addOnSuccessListener(new OnSuccessListener<List<Face>>() {
            @Override
            public void onSuccess(List<Face> faces) {

                if(faces.size()!=0){
                    if (faces.size()==1){
                        builder.append(faces.size()+"Face Detection\n\n");

                    }else if (faces.size()>1){
                        builder.append(faces.size()+"Face Detection\n\n");
                    }
                }

                for (Face face:faces){


                    int id=face.getTrackingId();
                    Float roty=face.getHeadEulerAngleY();
                    Float rotz= face.getHeadEulerAngleZ();

                    builder.append("1. Face Tracking Id[" +id+ "]\n");
                    //builder.append("2. Head Rotation to Right["+String.format("%.2f",roty)+"deg]\n");
                    //builder.append("3. Head Tilted Sideway["+String.format("%.2f",rotz)+"deg]\n");


                    if (face.getSmilingProbability()>0){
                        float SmilingProbabliity=face.getSmilingProbability();
                        builder.append("2. Smiling Probablity["+String.format("%.2f",SmilingProbabliity)+"]\n");
                    }

                    if (face.getLeftEyeOpenProbability()>0){
                        float leftEye=face.getLeftEyeOpenProbability();
                        builder.append("3. Left Eye Open Prob["+String.format("%.2f",leftEye)+"]\n");
                    }


                    if (face.getRightEyeOpenProbability()>0){
                        float rightEye=face.getRightEyeOpenProbability();
                        builder.append("4. Right Eye Open Prob["+String.format("%.2f",rightEye)+"]\n");
                    }
                    builder.append("\n");
                }

                ShowDetection("Face Detection",builder,true);

            }
        });

        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                StringBuilder builder1=new StringBuilder();
                builder1.append("Sorry ! There is an error!");
                ShowDetection("Face Detection",builder,false);
            }
        });

    }

    public void ShowDetection(final String title,final StringBuilder builder,boolean success){

        if(success ==true){
            textview.setText(null);
            textview.setMovementMethod(new ScrollingMovementMethod());

            if (builder.length()!=0){
                textview.append(builder);
                if (title.substring(0,title.indexOf(' ')).equalsIgnoreCase("OCR")){
                    textview.append("\n(Hold the text to copy it)");
                }else {
                    textview.append("(Hold the text to copy it!)");
                }

                textview.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        ClipboardManager clipboars=(ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip=ClipData.newPlainText(title,builder);
                        clipboars.setPrimaryClip(clip);
                        return true;
                    }
                });
            }
            else{
                textview.append(title.substring(0,title.indexOf(' '))+"Failed to Find Anything!");
            }
        } else if (success ==false) {
            textview.setText(null);
            textview.setMovementMethod(new ScrollingMovementMethod());
            textview.append(builder);
        }
    }

}