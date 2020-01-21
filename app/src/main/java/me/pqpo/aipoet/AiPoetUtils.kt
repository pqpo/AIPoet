package me.pqpo.aipoet

import android.graphics.*
import android.view.View
import android.graphics.RectF
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import org.jetbrains.anko.find


object AiPoetUtils {

    fun generateSharePicture(view: View, shareView: ViewGroup): Bitmap {
        val context = view.context
        var width = view.width
        var height = view.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val bitmapShader = BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP)
        val newBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(newBitmap)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = bitmapShader
        val radius = 18.0f * context.resources.displayMetrics.density
        canvas.drawRoundRect(RectF(0f, 0f, width.toFloat(), height.toFloat()), radius, radius, paint)
        bitmap.recycle()

        shareView.findViewById<View>(R.id.share_cv_poet).layoutParams.apply {
            height = (view.height.toFloat() / (view.width.toFloat() / width)).toInt()
        }
        shareView.find<TextView>(R.id.share_tv_name).typeface = TypeFaceUtils.getTypeFace(context)
        shareView.find<ImageView>(R.id.share_rl_card).setImageBitmap(newBitmap)
        val resultBitmap = Bitmap.createBitmap(shareView.width, shareView.height, Bitmap.Config.ARGB_8888)
        shareView.draw(Canvas(resultBitmap))
        return resultBitmap
    }


}