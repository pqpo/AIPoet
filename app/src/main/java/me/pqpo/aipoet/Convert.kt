package me.pqpo.aipoet

import android.content.res.AssetFileDescriptor
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import java.io.*

class Convert {

    companion object {
        private const val TAG = "Convert"
    }

    private var convertModel: Model? = null

    @Throws(IOException::class)
    fun loadConvertFile(fd: AssetFileDescriptor): Boolean {
        val jsonStr = BufferedReader(InputStreamReader(fd.createInputStream())).readText()
        if (!jsonStr.isBlank()) {
            convertModel = Gson().fromJson<Model>(jsonStr, object : TypeToken<Model>() {}.type)
            val size = convertModel?.ix2word?.size?:0
            Log.d(TAG, "load convert success! size: $size")
            return size > 0
        }
        return false
    }

    fun word2Index(word: String): Int {
        return convertModel?.word2ix?.get(word)?:-1
    }

    fun index2Word(index: Int): String {
        return convertModel?.ix2word?.get(index.toString())?:""
    }


    class Model {
        var word2ix: Map<String, Int>? = null
        var ix2word: Map<String, String>? = null
    }

}
