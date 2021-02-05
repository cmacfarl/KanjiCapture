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

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.airbnb.lottie.LottieAnimationView;
import com.google.common.util.concurrent.ListenableFuture;

import org.cmacfarl.kanjicapture.about.KanjiCaptureAboutActivity;
import org.cmacfarl.kanjicapture.graphics.GraphicOverlay;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KanjiCaptureActivity extends AppCompatActivity implements KanjiAnalyzerListener
{
    private static final String TAG = "Scanner";

    public static final String EXTRA_KANJIS = "org.cmacfarl.kanjicapture.KANJIS";
    public static final String KANJI_LIST = "org.cmacfarl.kanjicapture.KANJI_LIST";

    private static final int lensFacing = CameraSelector.LENS_FACING_BACK;

    private KanjiAnalyzer analyzer;

    private ExecutorService cameraExecutor;
    private CameraSelector cameraSelector;
    private ProcessCameraProvider cameraProvider;
    private ImageCapture imageCapture;

    private Button startScanButton;
    private Preview preview;
    private GraphicOverlay graphicOverlay;
    private LottieAnimationView lottieView;

    private void startCamera()
    {
        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    cameraProvider = cameraProviderFuture.get();
                    bindPreview();
                    bindCapture();
                } catch (ExecutionException | InterruptedException e) {
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    void bindPreview()
    {
        preview = new Preview.Builder().build();

        cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);

        PreviewView viewFinder = findViewById(R.id.viewFinder);
        preview.setSurfaceProvider(viewFinder.getSurfaceProvider());
    }

    void bindCapture()
    {
        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(findViewById(R.id.viewFinder).getDisplay().getRotation())
                        .setTargetResolution(new Size(480,640))
                        .build();

        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, imageCapture);
    }

    private void processImage()
    {
        startScanButton.setVisibility(View.GONE);
        lottieView.setVisibility(View.VISIBLE);
        lottieView.setProgress(0);
        lottieView.playAnimation();

        imageCapture.takePicture(cameraExecutor,
                new ImageCapture.OnImageCapturedCallback() {
                    @Override
                    public void onCaptureSuccess(ImageProxy imageProxy) {
                        // insert your code here.
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                cameraProvider.unbind(preview);
                            }
                        });
                        analyzer.analyze(imageProxy);
                    }
                    @Override
                    public void onError(ImageCaptureException error) {
                        // insert your code here.
                        Log.d(TAG, "Couldn't get image");
                    }

                }
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lottieView = findViewById(R.id.lav_searching);
        lottieView.setVisibility(View.GONE);

        startScanButton = findViewById(R.id.camera_capture_button);
        startScanButton.setOnClickListener(l -> { processImage(); });

        graphicOverlay = findViewById(R.id.graphic_overlay);
        if (graphicOverlay == null) {
            Log.d(TAG, "graphicOverlay is null");
        }

        analyzer = new KanjiAnalyzer(this);
        startCamera();
        cameraExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void onKanjiFound(List<String> kanjis)
    {
        lottieView.pauseAnimation();
        lottieView.setVisibility(View.GONE);
        startScanButton.setVisibility(View.VISIBLE);
        bindPreview();

        Intent intent = new Intent(this, KanjiResultsActivity.class);
        Bundle kanjiBundle = new Bundle();
        kanjiBundle.putSerializable(KANJI_LIST, (Serializable)kanjis);
        intent.putExtra(EXTRA_KANJIS, kanjiBundle);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.about_item) {
            Intent i = new Intent(this, KanjiCaptureAboutActivity.class);
            startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}