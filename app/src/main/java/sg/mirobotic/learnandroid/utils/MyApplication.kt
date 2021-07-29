package sg.mirobotic.learnandroid.utils

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig
import java.util.*

class MyApplication: Application(),  CameraXConfig.Provider {


    override fun getCameraXConfig() = Camera2Config.defaultConfig()


}