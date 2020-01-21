package me.pqpo.aipoet

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.provider.MediaStore
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*

object AiPoetUtils {

    fun generateSharePicture(view: View) {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        
    }


}