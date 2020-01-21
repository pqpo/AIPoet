package me.pqpo.aipoet

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

class PoetFileProvider: FileProvider() {

    companion object {
        fun getUriForFile(context: Context, file: File): Uri {
            return getUriForFile(context, authority, file)
        }

        const val authority = "me.pqpo.aipoet.aipoet.fileprovider"
    }



}