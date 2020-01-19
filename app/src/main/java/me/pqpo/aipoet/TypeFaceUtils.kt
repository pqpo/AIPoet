package me.pqpo.aipoet

import android.content.Context
import android.graphics.Typeface

/**
 * Created by qiulinmin@u51.com on 2020-01-19.
 */
object TypeFaceUtils{

    private var typeface: Typeface? = null

    fun getTypeFace(context: Context): Typeface? {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(
                context.assets,
                "font.ttc"
            )
        }
        return typeface
    }

}