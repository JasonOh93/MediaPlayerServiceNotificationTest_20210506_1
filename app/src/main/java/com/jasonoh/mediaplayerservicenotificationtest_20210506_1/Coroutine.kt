package com.jasonoh.mediaplayerservicenotificationtest_20210506_1

import android.content.Context
import android.graphics.SurfaceTexture
import android.util.Log
import android.view.Surface
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class Coroutine {
    companion object{

        fun BackgroundTask(myService: MyService, surfaceTexture: SurfaceTexture, context: Context) {
            CoroutineScope(Dispatchers.Main).launch {
                async(Dispatchers.Default){
                    myService.playVideo(surfaceTexture)
//                    Toast.makeText(context, "코틀린에서 이전이다.", Toast.LENGTH_SHORT).show()
                }.await()
                Log.e("TAG", "코틀린에서 끝났다 그 다음이다.");
//                Toast.makeText(context, "코틀린에서 끝났다 그 다음이다.", Toast.LENGTH_SHORT).show()
            }
        }

    }
}