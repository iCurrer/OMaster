package com.example.omaster.ui.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import com.example.omaster.util.formatSigned

class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var isExpanded = true

    companion object {
        private const val EXTRA_NAME = "name"
        private const val EXTRA_FILTER = "filter"
        private const val EXTRA_SOFT_LIGHT = "soft_light"
        private const val EXTRA_TONE = "tone"
        private const val EXTRA_SATURATION = "saturation"
        private const val EXTRA_WARM_COOL = "warm_cool"
        private const val EXTRA_CYAN_MAGENTA = "cyan_magenta"
        private const val EXTRA_SHARPNESS = "sharpness"
        private const val EXTRA_VIGNETTE = "vignette"
        private const val EXTRA_WHITE_BALANCE = "white_balance"
        private const val EXTRA_COLOR_TONE = "color_tone"
        private const val EXTRA_EXPOSURE = "exposure"
        private const val EXTRA_COLOR_TEMPERATURE = "color_temperature"
        private const val EXTRA_COLOR_HUE = "color_hue"

        fun show(context: Context, preset: com.example.omaster.model.MasterPreset) {
            val intent = Intent(context, FloatingWindowService::class.java).apply {
                putExtra(EXTRA_NAME, preset.name)
                putExtra(EXTRA_FILTER, preset.filter)
                putExtra(EXTRA_SOFT_LIGHT, preset.softLight)
                putExtra(EXTRA_TONE, preset.tone)
                putExtra(EXTRA_SATURATION, preset.saturation)
                putExtra(EXTRA_WARM_COOL, preset.warmCool)
                putExtra(EXTRA_CYAN_MAGENTA, preset.cyanMagenta)
                putExtra(EXTRA_SHARPNESS, preset.sharpness)
                putExtra(EXTRA_VIGNETTE, preset.vignette)
                putExtra(EXTRA_WHITE_BALANCE, preset.whiteBalance ?: "")
                putExtra(EXTRA_COLOR_TONE, preset.colorTone ?: "")
                putExtra(EXTRA_EXPOSURE, preset.exposureCompensation ?: "")
                putExtra(EXTRA_COLOR_TEMPERATURE, preset.colorTemperature ?: -1)
                putExtra(EXTRA_COLOR_HUE, preset.colorHue ?: -999)
            }
            context.startService(intent)
        }

        fun hide(context: Context) {
            context.stopService(Intent(context, FloatingWindowService::class.java))
        }
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val name = intent.getStringExtra(EXTRA_NAME) ?: "预设"
        val filter = intent.getStringExtra(EXTRA_FILTER) ?: "原图"
        val softLight = intent.getStringExtra(EXTRA_SOFT_LIGHT) ?: "无"
        val tone = intent.getIntExtra(EXTRA_TONE, 0)
        val saturation = intent.getIntExtra(EXTRA_SATURATION, 0)
        val warmCool = intent.getIntExtra(EXTRA_WARM_COOL, 0)
        val cyanMagenta = intent.getIntExtra(EXTRA_CYAN_MAGENTA, 0)
        val sharpness = intent.getIntExtra(EXTRA_SHARPNESS, 0)
        val vignette = intent.getStringExtra(EXTRA_VIGNETTE) ?: "关"
        val whiteBalance = intent.getStringExtra(EXTRA_WHITE_BALANCE) ?: ""
        val colorTone = intent.getStringExtra(EXTRA_COLOR_TONE) ?: ""
        val exposure = intent.getStringExtra(EXTRA_EXPOSURE) ?: ""
        val colorTemperature = intent.getIntExtra(EXTRA_COLOR_TEMPERATURE, -1)
        val colorHue = intent.getIntExtra(EXTRA_COLOR_HUE, -999)

        showWindow(name, filter, softLight, tone, saturation, warmCool, cyanMagenta, sharpness, vignette, whiteBalance, colorTone, exposure, colorTemperature, colorHue)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        removeWindow()
    }

    private fun showWindow(
        name: String,
        filter: String,
        softLight: String,
        tone: Int,
        saturation: Int,
        warmCool: Int,
        cyanMagenta: Int,
        sharpness: Int,
        vignette: String,
        whiteBalance: String,
        colorTone: String,
        exposure: String,
        colorTemperature: Int,
        colorHue: Int
    ) {
        try {
            val wm = windowManager ?: return

            // 保存数据到成员变量
            currentName = name
            currentFilter = filter
            currentSoftLight = softLight
            currentTone = tone
            currentSaturation = saturation
            currentWarmCool = warmCool
            currentCyanMagenta = cyanMagenta
            currentSharpness = sharpness
            currentVignette = vignette
            currentWhiteBalance = whiteBalance
            currentColorTone = colorTone
            currentExposure = exposure
            currentColorTemperature = colorTemperature
            currentColorHue = colorHue

            params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    @Suppress("DEPRECATION")
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 50
                y = 300
            }

            val rootLayout = createExpandedView(name, filter, softLight, tone, saturation, warmCool, cyanMagenta, sharpness, vignette, whiteBalance, colorTone, exposure, colorTemperature, colorHue)
            floatingView = rootLayout
            wm.addView(floatingView, params)

            setupDrag(wm)

        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun createExpandedView(
        name: String,
        filter: String,
        softLight: String,
        tone: Int,
        saturation: Int,
        warmCool: Int,
        cyanMagenta: Int,
        sharpness: Int,
        vignette: String,
        whiteBalance: String,
        colorTone: String,
        exposure: String,
        colorTemperature: Int,
        colorHue: Int
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 28, 32, 28)
            background = GradientDrawable().apply {
                cornerRadius = 24f
                setColor(Color.parseColor("#F2000000"))
            }
            layoutParams = LinearLayout.LayoutParams(
                dpToPx(320),
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            // 标题栏
            addView(createTitleBar(name))

            // 分隔线
            addView(createDivider())

            // Pro 模式参数（如果有）
            var hasProParams = false
            if (exposure.isNotEmpty()) {
                addView(createParamRow("曝光补偿", exposure, false))
                hasProParams = true
            }
            if (colorTemperature != -1) {
                addView(createParamRow("色温", "${colorTemperature}K", false))
                hasProParams = true
            }
            if (colorHue != -999) {
                addView(createParamRow("色调", colorHue.formatSigned(), false))
                hasProParams = true
            }
            if (whiteBalance.isNotEmpty()) {
                addView(createParamRow("白平衡", whiteBalance, false))
                hasProParams = true
            }
            if (colorTone.isNotEmpty()) {
                addView(createParamRow("色调风格", colorTone, false))
                hasProParams = true
            }
            if (hasProParams) {
                addView(createDivider())
            }

            // 参数列表
            addView(createParamRow("滤镜", filter, true))
            addView(createParamRow("柔光", softLight, false))
            addView(createParamRow("影调", tone.formatSigned(), false))
            addView(createParamRow("饱和度", saturation.formatSigned(), false))
            addView(createParamRow("冷暖", warmCool.formatSigned(), false))
            addView(createParamRow("青品", cyanMagenta.formatSigned(), false))
            addView(createParamRow("锐度", sharpness.toString(), false))
            addView(createParamRow("暗角", vignette, false))
        }
    }

    private fun createTitleBar(name: String): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL

            // 预设名称
            val titleView = TextView(this@FloatingWindowService).apply {
                text = name
                textSize = 20f
                setTextColor(Color.parseColor("#FF6B35"))
                setPadding(0, 0, 40, 0)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            // 收起按钮
            val collapseView = TextView(this@FloatingWindowService).apply {
                text = "▼"
                textSize = 16f
                setTextColor(Color.parseColor("#CCFFFFFF"))
                setPadding(16, 8, 16, 8)
                background = GradientDrawable().apply {
                    cornerRadius = 10f
                    setColor(Color.parseColor("#33FFFFFF"))
                }
                setOnClickListener {
                    collapseToBubble(name)
                }
            }

            // 关闭按钮
            val closeView = TextView(this@FloatingWindowService).apply {
                text = "✕"
                textSize = 22f
                setTextColor(Color.WHITE)
                setPadding(20, 8, 8, 8)
                setOnClickListener {
                    stopSelf()
                }
            }

            addView(titleView)
            addView(collapseView)
            addView(closeView)
        }
    }

    private fun createDivider(): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            ).apply {
                setMargins(0, 12, 0, 12)
            }
            setBackgroundColor(Color.parseColor("#22FFFFFF"))
        }
    }

    private fun createParamRow(label: String, value: String, isHighlight: Boolean): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(4, 10, 4, 10)

            val labelView = TextView(this@FloatingWindowService).apply {
                text = label
                textSize = 15f
                setTextColor(Color.parseColor("#88FFFFFF"))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }

            val valueView = TextView(this@FloatingWindowService).apply {
                text = value
                textSize = if (isHighlight) 16f else 15f
                setTextColor(if (isHighlight) Color.parseColor("#FF6B35") else Color.WHITE)
                if (isHighlight) {
                    setPadding(12, 4, 12, 4)
                    background = GradientDrawable().apply {
                        cornerRadius = 8f
                        setColor(Color.parseColor("#22FF6B35"))
                    }
                }
            }

            addView(labelView)
            addView(valueView)
        }
    }

    private var currentName = ""
    private var currentFilter = ""
    private var currentSoftLight = ""
    private var currentTone = 0
    private var currentSaturation = 0
    private var currentWarmCool = 0
    private var currentCyanMagenta = 0
    private var currentSharpness = 0
    private var currentVignette = ""
    private var currentWhiteBalance = ""
    private var currentColorTone = ""
    private var currentExposure = ""
    private var currentColorTemperature = -1
    private var currentColorHue = -999

    private fun collapseToBubble(name: String) {
        try {
            val wm = windowManager ?: return
            removeWindow()

            isExpanded = false
            params?.width = WindowManager.LayoutParams.WRAP_CONTENT
            params?.height = WindowManager.LayoutParams.WRAP_CONTENT

            // 保存当前数据
            currentName = name

            val buttonSize = 64

            // 使用方形圆角按钮，带展开图标
            val miniButton = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    dpToPx(buttonSize),
                    dpToPx(buttonSize)
                )
                background = GradientDrawable().apply {
                    cornerRadius = 16f
                    setColor(Color.parseColor("#FF6B35"))
                }

                // 使用 ▲ 图标表示可以展开
                val iconView = TextView(this@FloatingWindowService).apply {
                    text = "▲"
                    textSize = 22f
                    setTextColor(Color.WHITE)
                    gravity = Gravity.CENTER
                }
                addView(iconView)
            }

            miniButton.setOnClickListener {
                // 展开 - 重新创建完整视图
                removeWindow()
                isExpanded = true
                params?.width = WindowManager.LayoutParams.WRAP_CONTENT
                params?.height = WindowManager.LayoutParams.WRAP_CONTENT

                val expandedView = createExpandedView(
                    currentName,
                    currentFilter,
                    currentSoftLight,
                    currentTone,
                    currentSaturation,
                    currentWarmCool,
                    currentCyanMagenta,
                    currentSharpness,
                    currentVignette,
                    currentWhiteBalance,
                    currentColorTone,
                    currentExposure,
                    currentColorTemperature,
                    currentColorHue
                )
                floatingView = expandedView
                wm.addView(floatingView, params)
                setupDrag(wm)
            }

            floatingView = miniButton
            wm.addView(floatingView, params)
            setupDrag(wm)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun setupDrag(wm: WindowManager) {
        floatingView?.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var touchX = 0f
            private var touchY = 0f
            private var isClick = false

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params?.x ?: 0
                        initialY = params?.y ?: 0
                        touchX = event.rawX
                        touchY = event.rawY
                        isClick = true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - touchX
                        val dy = event.rawY - touchY
                        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) {
                            isClick = false
                        }
                        params?.x = initialX + dx.toInt()
                        params?.y = initialY + dy.toInt()
                        floatingView?.let { view ->
                            params?.let { p ->
                                wm.updateViewLayout(view, p)
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        // 点击事件在各自的 View 中处理
                    }
                }
                return false
            }
        })
    }

    private fun removeWindow() {
        try {
            floatingView?.let { view ->
                windowManager?.removeView(view)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        floatingView = null
    }

}
