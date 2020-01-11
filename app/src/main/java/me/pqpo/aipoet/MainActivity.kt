package me.pqpo.aipoet

import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import android.widget.Toolbar
import androidx.appcompat.app.WindowDecorActionBar
import androidx.core.view.ViewCompat
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private var acrostic = false
    private var aiPoet:AiPoet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAiPoet()
        val typeface = Typeface.createFromAsset(
            assets,
            "font.otf"
        )
        text.typeface = typeface
        song.typeface = typeface
        initClickListeners()
        setAcrosticStatus(true)
        et_style.setText(PoetryStyle.getRandomStyle())
        et_acrostic.setText("人工智能")
        song.performClick()
        Handler().post{
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
                text.text = "${aiPoet?.song(if(acrostic) acrosticStr else "", styleStr, acrostic)}"
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
        tv_normal.setOnClickListener{
            setAcrosticStatus(false)
        }
        tv_acrostic.setOnClickListener{
            setAcrosticStatus(true)
        }
    }

    private fun setAcrosticStatus(acrostic: Boolean) {
        this.acrostic = acrostic
        if (acrostic) {
            et_acrostic.visibility = View.VISIBLE
            setActiveTextView(tv_acrostic, tv_normal)
        } else {
            et_acrostic.visibility = View.GONE
            setActiveTextView(tv_normal, tv_acrostic)
        }
    }

    private fun setActiveTextView(tvPositive: TextView?, tvNegative: TextView?) {
        tvPositive?.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.point_red, theme),
            null, null, null)
        tvPositive?.setTextColor(Color.parseColor("#666666"))
        tvNegative?.setCompoundDrawablesWithIntrinsicBounds(resources.getDrawable(R.drawable.point_gray, theme),
            null, null, null)
        tvNegative?.setTextColor(Color.parseColor("#999999"))
    }

    private fun initAiPoet() {
        aiPoet = try {
            AiPoet(this);
        } catch (e:IOException) {
            null
        }
    }


}
