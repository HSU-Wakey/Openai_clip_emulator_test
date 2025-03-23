package com.example.Openai_clip_emulator_test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.Openai_clip_emulator_test.tflite.ClipImageEncoder;
import com.example.Openai_clip_emulator_test.tflite.ClipTextEncoder;
import com.example.Openai_clip_emulator_test.utils.SimilarityUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText editSearch;
    private Button btnSearch;
    private ImageView resultImageView;

    private ClipTextEncoder textEncoder;
    private ClipImageEncoder imageEncoder;

    private List<Integer> imageIds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        editSearch = findViewById(R.id.editSearch);
        btnSearch = findViewById(R.id.btnSearch);
        resultImageView = findViewById(R.id.resultImageView);

        try {
            textEncoder = new ClipTextEncoder(this);
            imageEncoder = new ClipImageEncoder(this);
        } catch (IOException e) {
            Toast.makeText(this, "모델 로딩 실패", Toast.LENGTH_SHORT).show();
            return;
        }

        // 사용할 이미지 목록 (drawable 리소스들)
        imageIds = Arrays.asList(
                R.drawable.image4,
                R.drawable.image5,
                R.drawable.image6,
                R.drawable.image7,
                R.drawable.image8,
                R.drawable.image9,
                R.drawable.image10,
                R.drawable.image11
        );

        btnSearch.setOnClickListener(v -> performSearch());
    }

    private void performSearch() {
        String query = editSearch.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "검색어를 입력하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        int[] tokenIds = getTokenForText(query);
        float[] textEncoding = textEncoder.getTextEncoding(tokenIds);

        float maxSimilarity = -1f;
        int bestImageResId = -1;

        for (int imageId : imageIds) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), imageId);
            float[] imageEncoding = imageEncoder.getImageEncoding(bitmap);

            float similarity = SimilarityUtils.cosineSimilarity(textEncoding, imageEncoding);
            Log.d("Search", "이미지 ID: " + imageId + ", 유사도: " + similarity);

            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                bestImageResId = imageId;
            }
        }

        if (bestImageResId != -1) {
            resultImageView.setImageResource(bestImageResId);
            Toast.makeText(this, "가장 유사한 이미지가 표시되었습니다", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "유사한 이미지를 찾을 수 없습니다", Toast.LENGTH_SHORT).show();
        }
    }

    // 임시 tokenizer (나중에 tokenizer 모델 연동 가능)
    private int[] getTokenForText(String text) {
        switch (text.toLowerCase()) {
            case "고양이":
            case "cat":
                return new int[]{49406, 320, 1125, 539, 320, 9111, 49407}; // a photo of a cat
            case "강아지":
            case "dog":
                return new int[]{49406, 320, 1125, 539, 320, 1000, 49407};
            case "해변":
            case "beach":
                return new int[]{49406, 320, 1125, 539, 320, 5810, 49407};
            default:
                return new int[]{49406, 320, 1125, 539, 320, 9111, 49407}; // default: cat
        }
    }

    @Override
    protected void onDestroy() {
        if (textEncoder != null) textEncoder.close();
        if (imageEncoder != null) imageEncoder.close();
        super.onDestroy();
    }
}
