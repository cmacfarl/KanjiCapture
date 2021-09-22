/*
 * Copyright 2021 Craig MacFarlane.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cmacfarl.kanjicapture;

import android.annotation.SuppressLint;
import android.media.Image;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionCloudTextRecognizerOptions;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import java.io.Console;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

class KanjiAnalyzer implements ImageAnalysis.Analyzer
{
    private final static String TAG = "KanjiAnalyzer";
    private KanjiAnalyzerListener listener;

    public KanjiAnalyzer(KanjiAnalyzerListener listener)
    {
        this.listener = listener;
    }

    private int degreesToFirebaseRotation(int degrees)
    {
        switch (degrees) {
            case 0:
                return FirebaseVisionImageMetadata.ROTATION_0;
            case 90:
                return FirebaseVisionImageMetadata.ROTATION_90;
            case 180:
                return FirebaseVisionImageMetadata.ROTATION_180;
            case 270:
                return FirebaseVisionImageMetadata.ROTATION_270;
            default:
                throw new IllegalArgumentException(
                        "Rotation must be 0, 90, 180, or 270.");
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    @Override
    public void analyze(ImageProxy imageProxy)
    {
        if (imageProxy == null || imageProxy.getImage() == null) {
            return;
        }
        Image mediaImage = imageProxy.getImage();
        int rot = imageProxy.getImageInfo().getRotationDegrees();
        int rotation = degreesToFirebaseRotation(rot);
        FirebaseVisionImage image = FirebaseVisionImage.fromMediaImage(mediaImage, rotation);

        // Pass image to an ML Kit Vision API
        // ...
        FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
                .getCloudTextRecognizer();

        // Or, to change the default settings:
        //   FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
        //          .getCloudTextRecognizer(options);
        // Or, to provide language hints to assist with language detection:
        // See https://cloud.google.com/vision/docs/languages for supported languages
        FirebaseVisionCloudTextRecognizerOptions options = new FirebaseVisionCloudTextRecognizerOptions.Builder()
                .setLanguageHints(Arrays.asList("ja"))
                .build();

        Log.d(TAG, "Starting image recognition");
        Task<FirebaseVisionText> result =
                detector.processImage(image)
                        .addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
                            @Override
                            public void onSuccess(FirebaseVisionText firebaseVisionText) {
                                // Task completed successfully
                                // ...
                                String str = firebaseVisionText.getText();
                                String kanjis = new String();
                                ArrayList<String> kanjiList = new ArrayList<String>();
                                for (int i = 0; i < str.length(); i++) {
                                    if (Character.UnicodeBlock.of(str.charAt(i)) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                                        kanjis += str.charAt(i) + " ";
                                        char ch = str.charAt(i);
                                        if (!kanjiList.contains(Character.toString(ch))) {
                                            kanjiList.add(Character.toString(ch));
                                        }
                                    }
                                }
                                Log.d(TAG, kanjis);
                                listener.onKanjiFound(kanjiList);
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Log.d(TAG, "No analysis");
                                    }
                                });

    }
}
