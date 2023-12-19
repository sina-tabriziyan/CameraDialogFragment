package com.sina.recordvideo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Size
import android.view.Surface
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import java.io.File
import java.util.*

class CameraDialogFragment : DialogFragment() {

    private val TAG = "com.sina.recordvideo.CameraDialogFragment"

    private lateinit var cameraManager: CameraManager
    private var cameraDevice: CameraDevice? = null
    private lateinit var mediaRecorder: MediaRecorder

    private var cameraId: String? = null
    private var surfaceTexture: SurfaceTexture? = null
    private var previewSize: Size? = null
    private var videoSize: Size? = null

    private var isRecording = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_fragment_camera, null)

        val btnRecord = view.findViewById<Button>(R.id.btn_record)
        btnRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        checkPermissions()

        return builder.setView(view).create()
    }

    private fun checkPermissions() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    initCamera()
                }

                override fun onPermissionDenied(deniedPermissions: List<String>) {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                }
            })
            .setDeniedMessage("If you deny the permission, you will not be able to use the camera.")
            .setPermissions(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            .check()
    }

    @SuppressLint("MissingPermission")
    private fun initCamera() {
        cameraManager = requireActivity().getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList[0]

        val characteristics = cameraManager.getCameraCharacteristics(cameraId!!)
        previewSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java)
            ?.maxByOrNull { it.width * it.height }

        videoSize = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(MediaRecorder::class.java)
            ?.maxByOrNull { it.width * it.height }

        surfaceTexture = SurfaceTexture(100)
        surfaceTexture?.setDefaultBufferSize(previewSize!!.width, previewSize!!.height)

        val cameraDeviceStateCallback = object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                cameraDevice = camera
                startPreview()
            }

            override fun onDisconnected(camera: CameraDevice) {
                camera.close()
                cameraDevice = null
            }

            override fun onError(camera: CameraDevice, error: Int) {
                camera.close()
                cameraDevice = null
            }
        }

        cameraManager.openCamera(cameraId!!, cameraDeviceStateCallback, null)
    }

    private fun startPreview() {
        val surface = Surface(surfaceTexture)
        val captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
        captureRequestBuilder?.addTarget(surface)
        captureRequestBuilder?.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
        cameraDevice?.createCaptureSession(
            listOf(surface),
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    captureRequestBuilder?.build()?.let { session.setRepeatingRequest(it, null, null) }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    Log.e(TAG, "Failed to configure capture session")
                }
            },
            null
        )
    }

    private fun startRecording() {
        if (isRecording) {
            return
        }

        isRecording = true

        mediaRecorder = MediaRecorder()
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setVideoSize(videoSize!!.width, videoSize!!.height)
        mediaRecorder.setVideoFrameRate(30)
        mediaRecorder.setAudioSamplingRate(44100)
        mediaRecorder.setAudioChannels(2)
        mediaRecorder.setOutputFile(getOutputFilePath())

        mediaRecorder.prepare()
        mediaRecorder.start()
    }

    private fun stopRecording() {
        if (!isRecording) {
            return
        }

        isRecording = false

        mediaRecorder.stop()
        mediaRecorder.reset()
        mediaRecorder.release()
    }

    private fun getOutputFilePath(): String {
        val directory =
            File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), "com.sina.recordvideo.CameraDialogFragment")
        directory.mkdirs()

        return File(directory, "${UUID.randomUUID()}.mp4").absolutePath
    }

    companion object {
        fun newInstance(): CameraDialogFragment {
            return CameraDialogFragment()
        }
    }
}