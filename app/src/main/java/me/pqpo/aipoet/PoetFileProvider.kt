package me.pqpo.aipoet

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

/**
 * Created by qiulinmin@u51.com on 2020-01-19.
 */
class PoetFileProvider: FileProvider() {

    companion object {
        fun getUriForFile(context: Context, file: File): Uri {
            return FileProvider.getUriForFile(context, authority, file)
        }

        const val authority = "me.pqpo.aipoet.aipoet.fileprovider"
    }



}