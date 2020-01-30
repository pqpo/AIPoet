package me.pqpo.aipoet

import android.content.Context
import android.graphics.Typeface

object TypeFaceUtils{

    private var typeface: Typeface? = null

    fun getTypeFace(context: Context): Typeface? {
        if (typeface == null) {
            typeface = Typeface.createFromAsset(
                context.assets,
                "fonts.ttf"
            )
        }
        return typeface
    }

}