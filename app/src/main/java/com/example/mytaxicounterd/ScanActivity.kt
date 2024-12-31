package com.example.mytaxicounterd

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ar.core.ImageFormat
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class ScanActivity : AppCompatActivity() {
    private lateinit var barcodeScanner: BarcodeScanner
    private val CAMERA_REQUEST_CODE = 101
    private lateinit var cameraDevice: CameraDevice
    private lateinit var captureSession: CameraCaptureSession
    private var imageReader: ImageReader? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.navigationBarColor = resources.getColor(R.color.white, theme)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = resources.getColor(R.color.gold, theme)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
        }

        barcodeScanner = BarcodeScanning.getClient()

        startCamera()
    }

    private fun startCamera() {
        val surfaceView: SurfaceView = findViewById(R.id.scanner_view)
        val holder: SurfaceHolder = surfaceView.holder
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId = cameraManager.cameraIdList[0]
                if (ActivityCompat.checkSelfPermission(
                        this@ScanActivity,
                        Manifest.permission.CAMERA
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this@ScanActivity,
                        arrayOf(Manifest.permission.CAMERA),
                        CAMERA_REQUEST_CODE
                    )
                    return
                }
                cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                    override fun onOpened(camera: CameraDevice) {
                        val previewRequestBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                        val surface = holder.surface
                        previewRequestBuilder.addTarget(surface)

                        camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                            override fun onConfigured(session: CameraCaptureSession) {
                                session.setRepeatingRequest(previewRequestBuilder.build(), null, null);

                                // Set up ImageReader for capturing frames
                                imageReader = ImageReader.newInstance(640, 480, android.graphics.ImageFormat.YUV_420_888, 2)
                                imageReader?.setOnImageAvailableListener({ reader ->
                                    val image = reader.acquireLatestImage()
                                    if (image != null) {
                                        try {
                                            val rotationDegrees = 0 // Adjust this based on your camera orientation
                                            val inputImage = InputImage.fromMediaImage(image, rotationDegrees)
                                            processImage(inputImage, image) // Pass the image to processImage
                                        } finally {
                                            image.close() // Ensure the image is closed after processing
                                        }
                                    }
                                }, null)
                                imageReader?.surface?.let { previewRequestBuilder.addTarget(it) }
                                camera.createCaptureSession(listOf(surface, imageReader?.surface), object : CameraCaptureSession.StateCallback() {
                                    override fun onConfigured(session: CameraCaptureSession) {
                                        captureSession = session
                                        session.setRepeatingRequest(previewRequestBuilder.build(), null, null);
                                    }

                                    override fun onConfigureFailed(session: CameraCaptureSession) {
                                        Log.e("ScanActivity", "Camera configuration failed")
                                    }
                                }, null)
                            }

                            override fun onConfigureFailed(session: CameraCaptureSession) {
                                Log.e("ScanActivity", "Camera configuration failed")
                            }
                        }, null)
                    }

                    override fun onDisconnected(camera: CameraDevice) {
                        camera.close()
                    }

                    override fun onError(camera: CameraDevice, error: Int) {
                        camera.close()
                    }
                }, null)
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                imageReader?.close() // Close the ImageReader if it exists
                captureSession?.close() // Close the capture session
                cameraDevice?.close() // Close the camera device
            }
        })
    }

    private fun processImage(image: InputImage, mediaImage: android.media.Image) {
        Log.d("ScanActivity", "Starting QR code scanning...")
        barcodeScanner.process(image)
            .addOnSuccessListener { barcodes ->
                Log.d("ScanActivity", "QR code scanning completed successfully")
                if (barcodes.isNotEmpty()) {
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue
                        Log.d("ScanActivity", "QR Code detected: $rawValue")
                        if (rawValue != null) {
                            AlertDialog.Builder(this)
                                .setTitle("QR Code Detected")
                                .setMessage(rawValue)
                                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                                .show()
                        }
                    }
                } else {
                    Log.d("ScanActivity", "No QR codes detected")
                }
            }
            .addOnFailureListener { e ->
                Log.e("ScanActivity", "QR code scanning failed", e)
            }
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.e("ScanActivity", "QR code scanning failed with exception", task.exception)
                }
                mediaImage.close() // Close the image here
            }
    }
}
