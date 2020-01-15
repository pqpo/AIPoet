package me.pqpo.aipoet

import android.Manifest
import android.graphics.Color
import android.graphics.Typeface
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
import org.jetbrains.anko.toast
import java.io.IOException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.SystemClock
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MainActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }

    private var acrostic = false
    private var aiPoet: AiPoet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAiPoet()
        val typeface = Typeface.createFromAsset(
            assets,
            "font.ttc"
        )
        text.typeface = typeface
        song.typeface = typeface
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
            savePoetBitmap()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun savePoetBitmap() {
        val permission = Manifest.permission.WRITE_EXTERNAL_STORAGE
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                toast("获取权限失败")
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQUEST_CODE)
            }
        } else {
            doSave()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doSave()
            } else {
                toast("获取权限失败")
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun doSave() {
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
                try {
                    resolver.openOutputStream(uri)?.use {
                        bitmap.compress(
                            Bitmap.CompressFormat.JPEG,
                            100,
                            it
                        )
                    }
                    result = true
                } catch (e: Throwable) {
                    // ignore
                }
            }
            uiThread {
                toast(if (result) "保存成功" else "保存失败")
            }
        }
    }


}
