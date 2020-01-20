package me.pqpo.aipoet

import android.Manifest
import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.SystemClock
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import me.pqpo.aipoet.core.AiPoet
import me.pqpo.aipoet.core.PoetryStyle
import me.pqpo.aipoet.core.UnmappedWordException
import org.jetbrains.anko.*
import java.io.File


class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE_SAVE_BITMAP = 100
        const val PERMISSION_REQUEST_CODE_CHANGE_BG = 101

        const val REQUEST_CODE_PICK = 200
        const val REQUEST_CODE_CROP = 201

        const val SP_CONFIG_NAME = "config"
        const val SP_CONFIG_KEY_TEXT_COLOR = "textColor"
    }

    private var acrostic = false
    private var aiPoet: AiPoet? = null

    private lateinit var backgroundFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAiPoet()
        text.typeface = TypeFaceUtils.getTypeFace(this)
        song.typeface = TypeFaceUtils.getTypeFace(this)
        initClickListeners()
        setAcrosticStatus(true)
        et_style.setText(PoetryStyle.getRandomStyle())
        et_acrostic.setText("人工智能")
        song.performClick()
        Handler().post {
            ViewCompat.setTransitionName(iv_yz, "");
        }
        ll_option.post {
            val llHeight = ll_option.height
            ll_option.translationY = llHeight.toFloat()

            val animator = ll_option.animate().translationY(0.0f)
            animator.duration = 800
            animator.start()
        }
        backgroundFile = File(getExternalFilesDir(null), "background.jpg")
        loadBackground()
        loadTextColor()
    }

    private fun loadTextColor() {
        text.textColor = getSharedPreferences(SP_CONFIG_NAME, Context.MODE_PRIVATE)
            .getInt(SP_CONFIG_KEY_TEXT_COLOR, 0xFF333333.toInt())

    }

    private fun loadBackground() {
        if (backgroundFile.isFile) {
            val bitmap = BitmapFactory.decodeFile(backgroundFile.absolutePath)
            bitmap?.let{
                rl_card.background = BitmapDrawable(resources, bitmap)
            }
        }
    }

    private fun initClickListeners() {
        cl_root.setOnClickListener {
            clearEditFocus()
        }
        song.setOnClickListener {
            val acrosticStr = et_acrostic.text.toString()
            val styleStr = et_style.text.toString()
            if (acrostic && acrosticStr.isBlank()) {
                toast("请输入藏头词")
                return@setOnClickListener
            }
            if (styleStr.isBlank()) {
                toast("请输入作诗风格")
                return@setOnClickListener
            }
            try {
                text.text = "${aiPoet?.song(if (acrostic) acrosticStr else "", styleStr, acrostic)}"
            } catch (e: UnmappedWordException) {
                toast("遇到了无法映射的文字：${e.word}")
            }
        }
        tv_random.setOnClickListener {
            et_style.setText(PoetryStyle.getRandomStyle())
            val animator = tv_random.animate().rotationBy(180.0f)
            animator.duration = 500
            animator.start()
        }
        tv_normal.setOnClickListener {
            setAcrosticStatus(false)
        }
        tv_acrostic.setOnClickListener {
            setAcrosticStatus(true)
        }
    }

    private fun clearEditFocus() {
        et_style.clearFocus()
        et_acrostic.clearFocus()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(et_style.windowToken, 0)
    }

    private fun setAcrosticStatus(acrostic: Boolean) {
        this.acrostic = acrostic
        if (acrostic) {
            til_acrostic.visibility = View.VISIBLE
            setActiveTextView(tv_acrostic, tv_normal)
        } else {
            til_acrostic.visibility = View.GONE
            setActiveTextView(tv_normal, tv_acrostic)
        }
        clearEditFocus()
    }

    private fun setActiveTextView(tvPositive: TextView?, tvNegative: TextView?) {
        tvPositive?.setCompoundDrawablesWithIntrinsicBounds(
            resources.getDrawable(R.drawable.point_red, theme),
            null, null, null
        )
        tvPositive?.setTextColor(Color.parseColor("#666666"))
        tvNegative?.setCompoundDrawablesWithIntrinsicBounds(
            resources.getDrawable(R.drawable.point_gray, theme),
            null, null, null
        )
        tvNegative?.setTextColor(Color.parseColor("#999999"))
    }

    private fun initAiPoet() {
        aiPoet = try {
            AiPoet(this);
        } catch (e: IOException) {
            null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId
        if (itemId == R.id.action_menu_copy) {
            val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val mClipData = ClipData.newPlainText("ai poet", text.text.toString())
            cm.setPrimaryClip(mClipData)
            toast("复制成功")
        } else if (itemId == R.id.action_menu_share) {
            checkPermissionAndApply(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_CODE_SAVE_BITMAP) {
                savePoetBitmap()
            }
        } else if (itemId == R.id.action_menu_change_bg) {
            checkPermissionAndApply(Manifest.permission.WRITE_EXTERNAL_STORAGE, PERMISSION_REQUEST_CODE_CHANGE_BG) {
                changeBg()
            }
        } else if (itemId == R.id.action_menu_rest_bg) {
            if (backgroundFile.isFile) {
                backgroundFile.delete()
                rl_card.backgroundResource = R.mipmap.bg
            }
        } else if (itemId == R.id.action_menu_toggle_text_color) {
            text.textColor = if(text.currentTextColor == 0xFF333333.toInt()) 0xFFffffff.toInt() else 0xFF333333.toInt()
            getSharedPreferences(SP_CONFIG_NAME, Context.MODE_PRIVATE).edit {
                putInt(SP_CONFIG_KEY_TEXT_COLOR, text.currentTextColor)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeBg() {
        val intent = Intent("android.intent.action.PICK", MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        startActivityForResult(intent, REQUEST_CODE_PICK)
    }

    private fun checkPermissionAndApply(permission: String, requestCode: Int,  action: () -> Unit) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
        } else {
            action()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == PERMISSION_REQUEST_CODE_SAVE_BITMAP) {
                savePoetBitmap()
            } else if (requestCode == PERMISSION_REQUEST_CODE_CHANGE_BG) {
                changeBg()
            }
        } else {
            toast("获取权限失败")
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun savePoetBitmap() {
        doAsync {
            val bitmap = Bitmap.createBitmap(cv_poet.width, cv_poet.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            cv_poet.draw(canvas)
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "ai_poet_${SystemClock.currentThreadTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            }
            var result = false
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            uri?.let {
                resolver.openOutputStream(uri)?.use {
                    bitmap.compress(
                        Bitmap.CompressFormat.JPEG,
                        100,
                        it
                    )
                    result = true
                }
            }
            uiThread {
                toast(if (result) "保存成功" else "保存失败")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PICK) {
            saveFileAndCrop(data)
        } else if (requestCode == REQUEST_CODE_CROP) {
            loadBackground()
        }
    }

    private fun saveFileAndCrop(data: Intent?) {
        val tempFile = File(externalCacheDir, "bg_temp.jpg")
        data?.data?.let {
            val inputStream = contentResolver.openInputStream(it)
            inputStream?.use {
                val outputStream = tempFile.outputStream()
                outputStream.use {
                    inputStream.copyTo(outputStream)
                    val cropIntent = Intent("com.android.camera.action.CROP");
                    cropIntent.setDataAndType(PoetFileProvider.getUriForFile(this@MainActivity, tempFile), "image/*")
                    cropIntent.putExtra("crop", "true");
                    cropIntent.putExtra("aspectX", rl_card.width)
                    cropIntent.putExtra("aspectY", rl_card.height)
                    cropIntent.putExtra("outputX", rl_card.width)
                    cropIntent.putExtra("outputY", rl_card.height)
                    cropIntent.putExtra("scale", true)
                    cropIntent.putExtra("return-data", false)
                    cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(backgroundFile))
                    cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
                    cropIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    startActivityForResult(cropIntent, REQUEST_CODE_CROP)
                }
            }
        }
    }

}
