package com.example.itemrecog;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.itemrecog.ml.MobilenetV110224Quant;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
// download the ssd from here https://www.tensorflow.org/lite/examples/image_classification/overview

public class MainActivity extends AppCompatActivity {


    private static final int RESULT_LOAD_IMG = 0;
    private ImageView image_view;
    private ArrayList<String> arr;
//    private float [] check = {4,6,1,2,13,11,0,9,0};
    private float [] sortedArray;
    private List<Integer> sortArray = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        arr = new ArrayList<String>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("label.txt"), "UTF-8"));
            String mLine = null;
           while ((mLine = reader.readLine()) != null) {
                arr.add(mLine);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void funcSelect(View view) {

        Intent photoPicker = new Intent(Intent.ACTION_PICK);
        photoPicker.setType("image/*");
        startActivityForResult(photoPicker, RESULT_LOAD_IMG);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        image_view = findViewById(R.id.imageView);

        if (resultCode == RESULT_OK) {
            final Uri imageUri = data.getData();
            final InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectImageBitmap = BitmapFactory.decodeStream(imageStream);
                image_view.setImageBitmap(selectImageBitmap);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        } else {
            Toast.makeText(this, "you havent picked image", Toast.LENGTH_LONG).show();
        }
    }

    public void func_detect(View view) {

        TextView textView1 = findViewById(R.id.textViewResult1);
        TextView textView2 = findViewById(R.id.textViewResult2);
        TextView textView3 = findViewById(R.id.textViewResult3);

        TextView ResultPercent1 = findViewById(R.id.textViewResultPercent1);
        TextView ResultPercent2 = findViewById(R.id.textViewResultPercent2);
        TextView ResultPercent3 = findViewById(R.id.textViewResultPercent3);
        Bitmap bm = ((BitmapDrawable) image_view.getDrawable()).getBitmap();
        Bitmap resize = Bitmap.createScaledBitmap(bm, 224, 224, true);

        try {
            TensorImage selectImage = TensorImage.fromBitmap(resize);

            MobilenetV110224Quant model = MobilenetV110224Quant.newInstance(this);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.UINT8);

            ByteBuffer byteBuffer = selectImage.getBuffer();

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            MobilenetV110224Quant.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            List<Integer> maxIndex = getMax(outputFeature0.getFloatArray());
            textView1.setText(arr.get((int) sortedArray[sortedArray.length-1]));
            ResultPercent1.setText(outputFeature0.getFloatArray()[maxIndex.get(0)]+"");

         //   outputFeature0.getFloatArray()[maxIndex]=0;
         //   maxIndex = getMax( outputFeature0.getFloatArray())[1];
      /*      textView2.setText(arr.get(maxIndex[1]));
            ResultPercent2.setText(outputFeature0.getFloatArray()[maxIndex[1]]+"");

         //   outputFeature0.getFloatArray()[maxIndex]=0;
          //  maxIndex = getMax( outputFeature0.getFloatArray())[2];
            textView3.setText(arr.get(maxIndex[2]));
            ResultPercent3.setText(outputFeature0.getFloatArray()[maxIndex[2]]+"");
*/
            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }

    }
    public int getCorrespondingIndex(float[] unsortedArray, float[] sortedArray, int index, List<Integer> sortArray){
        for(int i=0; i<unsortedArray.length; i++){
            if(sortedArray[index]==(unsortedArray[i])) {
             if(!sortArray.contains(i))
                return i;
            }
        }
        return -1;
    }
    public List<Integer> getMax(float[] arr) {
        HashMap< Float,Integer> result = new HashMap<>();
        sortedArray= arr;
       Arrays.sort(sortedArray);

        int oldIndex;
        for(int i=sortedArray.length-1; i>=0; i--) {
            oldIndex = getCorrespondingIndex(arr, sortedArray, i, sortArray);
            sortArray.add(oldIndex);
        }        return sortArray;
    }
}
