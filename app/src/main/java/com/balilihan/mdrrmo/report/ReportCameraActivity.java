// report/ReportCameraActivity.java

package com.balilihan.mdrrmo.report;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;

import com.balilihan.mdrrmo.databinding.ActivityReportCameraBinding;
import com.balilihan.mdrrmo.utils.ImageUtils;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReportCameraActivity extends AppCompatActivity {

    private ActivityReportCameraBinding binding;
    private ImageCapture                imageCapture;
    private ExecutorService             cameraExecutor;

    // Key for passing photo path to ReportFormActivity
    public static final String EXTRA_PHOTO_PATH = "photo_path";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding       = ActivityReportCameraBinding.inflate(getLayoutInflater());
        cameraExecutor = Executors.newSingleThreadExecutor();
        setContentView(binding.getRoot());

        startCamera();
        setupClickListeners();
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> future =
                ProcessCameraProvider.getInstance(this);

        future.addListener(() -> {
            try {
                ProcessCameraProvider provider = future.get();

                // Preview — shows live camera feed on screen
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(
                        binding.previewView.getSurfaceProvider()
                );

                // ImageCapture — used when shutter button is pressed
                imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(
                                ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
                        )
                        .build();

                // Use back camera — front camera not needed for hazards
                CameraSelector cameraSelector =
                        CameraSelector.DEFAULT_BACK_CAMERA;

                // Unbind any previous use cases before rebinding
                provider.unbindAll();
                Camera camera = provider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (ExecutionException | InterruptedException e) {
                Toast.makeText(this,
                        "Camera failed to start",
                        Toast.LENGTH_SHORT).show();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void setupClickListeners() {
        binding.btnCapture.setOnClickListener(v -> capturePhoto());
        binding.btnClose.setOnClickListener(v -> finish());
    }

    private void capturePhoto() {
        if (imageCapture == null) return;

        // Disable button while capturing to prevent double tap
        binding.btnCapture.setEnabled(false);

        // Save photo to app's files directory
        File photoFile = new File(
                getFilesDir(),
                "report_" + System.currentTimeMillis() + ".jpg"
        );

        ImageCapture.OutputFileOptions options =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                options,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(
                            @NonNull ImageCapture.OutputFileResults results) {

                        // Compress photo to max 1MB on background thread
                        new Thread(() -> {
                            try {
                                String compressedPath = ImageUtils.compressImage(
                                        ReportCameraActivity.this,
                                        photoFile.getAbsolutePath()
                                );

                                // Navigate to form with compressed photo path
                                runOnUiThread(() -> {
                                    Intent intent = new Intent(
                                            ReportCameraActivity.this,
                                            ReportFormActivity.class
                                    );
                                    intent.putExtra(
                                            EXTRA_PHOTO_PATH,
                                            compressedPath
                                    );
                                    startActivity(intent);
                                    // Don't finish — user might want to retake
                                });

                            } catch (Exception e) {
                                runOnUiThread(() -> {
                                    binding.btnCapture.setEnabled(true);
                                    Toast.makeText(
                                            ReportCameraActivity.this,
                                            "Failed to process photo. Try again.",
                                            Toast.LENGTH_SHORT
                                    ).show();
                                });
                            }
                        }).start();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException e) {
                        binding.btnCapture.setEnabled(true);
                        Toast.makeText(
                                ReportCameraActivity.this,
                                "Capture failed. Try again.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }
}