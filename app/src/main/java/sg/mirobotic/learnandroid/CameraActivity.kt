package sg.mirobotic.learnandroid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.TextureViewMeteringPointFactory
import androidx.core.content.ContextCompat
import com.google.common.util.concurrent.ListenableFuture
import sg.mirobotic.learnandroid.databinding.ActivityCameraBinding
import sg.mirobotic.learnandroid.utils.ImageUtility
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors

class CameraActivity : AppCompatActivity() {

    private lateinit var processCameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var processCameraProvider: ProcessCameraProvider
    private var photoFile: File? = null

    companion object {
        private const val TAG = "register"
    }

    private lateinit var context: Context

    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        context = this

        processCameraProviderFuture = ProcessCameraProvider.getInstance(context)

        processCameraProviderFuture.addListener({
            processCameraProvider = processCameraProviderFuture.get()
            binding.viewFinder.post { setupCamera() }
        }, ContextCompat.getMainExecutor(context))

        binding.btnRetake.setOnClickListener {
            binding.viewCamera.visibility = View.VISIBLE
            binding.viewPhoto .visibility = View.GONE
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (::processCameraProvider.isInitialized) {
            processCameraProvider.unbindAll()
        }
    }

    private fun setupCamera() {
        processCameraProvider.unbindAll()
        val camera = processCameraProvider.bindToLifecycle(
            this,
            CameraSelector.DEFAULT_FRONT_CAMERA,
            buildPreviewUseCase(),
            buildImageCaptureUseCase(),
            buildImageAnalysisUseCase())
        setupTapForFocus(camera.cameraControl)
    }

    private fun buildPreviewUseCase(): Preview {
        val display = binding.viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val preview = Preview.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .build()
            .apply {
                previewSurfaceProvider = binding.viewFinder.previewSurfaceProvider
            }
        preview.previewSurfaceProvider = binding.viewFinder.previewSurfaceProvider
        return preview
    }

    private fun buildImageCaptureUseCase(): ImageCapture {
        val display = binding.viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val capture = ImageCapture.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setFlashMode(ImageCapture.FLASH_MODE_AUTO)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        val executor = Executors.newSingleThreadExecutor()
        binding.btnCapture.setOnClickListener {
            capture.takePicture(
                getFile(),
                executor,
                object : ImageCapture.OnImageSavedCallback {
                    override fun onImageSaved(file: File) {
                        Log.e("cameraFragment","file ${file.absolutePath}")
                        runOnUiThread {

                            binding.viewCamera.visibility = View.GONE
                            binding.viewPhoto.visibility = View.VISIBLE
                            var bitmap = BitmapFactory.decodeFile(file.path)
                            bitmap = ImageUtility.rotateBitmap(bitmap)

                            binding.imgPhoto.setImageBitmap(bitmap)
                            photoFile = ImageUtility.bitmapToFile(context, bitmap)

                            Log.e(TAG,"PHOTO SIZE ${photoFile!!.length()/(1024 * 1024)} MB")

                        }
                    }

                    override fun onError(imageCaptureError: Int, message: String, cause: Throwable?) {
                        Toast.makeText(context, "Error: $message", Toast.LENGTH_LONG).show()
                        Log.e("CameraFragment", "Capture error $imageCaptureError: $message", cause)
                    }
                })
        }
        return capture
    }

    private fun buildImageAnalysisUseCase(): ImageAnalysis {
        val display = binding.viewFinder.display
        val metrics = DisplayMetrics().also { display.getMetrics(it) }
        val analysis = ImageAnalysis.Builder()
            .setTargetRotation(display.rotation)
            .setTargetResolution(Size(metrics.widthPixels, metrics.heightPixels))
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_BLOCK_PRODUCER)
            .setImageQueueDepth(10)
            .build()
        analysis.setAnalyzer(
            Executors.newSingleThreadExecutor(), { imageProxy ->
                imageProxy.close()
            })
        return analysis
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupTapForFocus(cameraControl: CameraControl) {
        binding.viewFinder.setOnTouchListener { _, event ->
            if (event.action != MotionEvent.ACTION_UP) {
                return@setOnTouchListener true
            }

            val textureView = binding.viewFinder.getChildAt(0) as? TextureView ?: return@setOnTouchListener true
            val factory = TextureViewMeteringPointFactory(textureView)

            val point = factory.createPoint(event.x, event.y)
            val action = FocusMeteringAction.Builder.from(point).build()
            cameraControl.startFocusAndMetering(action)
            return@setOnTouchListener true
        }
    }

    private fun getFile(): File {
        val date = Date()
        val simpleDateFormat = SimpleDateFormat("ddMMyy-hh:mm:ss", Locale.ENGLISH)
        val file = File(filesDir.absolutePath,"${simpleDateFormat.format(date)}.jpg")
        file.createNewFile()
        return file
    }
}