package com.silas.omaster.ui.service

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.silas.omaster.R
import com.silas.omaster.model.PresetItem
import com.silas.omaster.model.PresetSection
import com.silas.omaster.util.PresetI18n
import com.silas.omaster.util.formatSigned

/**
 * 悬浮窗服务 - 高级美观版
 *
 * 优化内容：
 * 1. 毛玻璃效果背景
 * 2. 渐变标题栏
 * 3. 图标化参数展示
 * 4. 精致的收起/展开动画
 * 5. 悬浮球采用品牌色渐变
 * 6. 动态渲染内容（基于 sections）
 */
class FloatingWindowService : Service() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var params: WindowManager.LayoutParams? = null
    private var isExpanded = true

    // 配色方案
    private val primaryColor = Color.parseColor("#FF6B35")      // 品牌橙色
    private val primaryDark = Color.parseColor("#E55A2B")       // 深橙色
    private val backgroundColor = Color.parseColor("#801A1A1A") // 毛玻璃背景（50%透明度，更透明便于取景）
    private val cardBackground = Color.parseColor("#26FFFFFF")  // 卡片背景
    private val textPrimary = Color.parseColor("#FFFFFF")       // 主文字
    private val textSecondary = Color.parseColor("#B3FFFFFF")   // 次要文字
    private val textMuted = Color.parseColor("#80FFFFFF")       // 弱化文字

    companion object {
        private const val EXTRA_NAME = "name"
        private const val EXTRA_SECTIONS = "sections"
        private const val EXTRA_PRESET_ID = "preset_id"
        private const val EXTRA_PRESET_INDEX = "preset_index"
        private const val EXTRA_PRESET_LIST = "preset_list"

        // 保存状态到 Intent 的键
        private const val EXTRA_IS_EXPANDED = "is_expanded"
        private const val EXTRA_POS_X = "pos_x"
        private const val EXTRA_POS_Y = "pos_y"
        private const val EXTRA_ACTION = "action"

        // Action 类型
        private const val ACTION_SHOW = "show"
        private const val ACTION_UPDATE = "update"

        // 广播 Action
        const val ACTION_SWITCH_PRESET = "com.silas.omaster.SWITCH_PRESET"
        const val EXTRA_SWITCH_DIRECTION = "switch_direction" // "prev" or "next"

        // 服务实例（用于更新内容）
        @Volatile
        private var instance: FloatingWindowService? = null

        fun show(context: Context, preset: com.silas.omaster.model.MasterPreset, presetIndex: Int = 0, presetIds: List<String> = emptyList()) {
            val intent = Intent(context, FloatingWindowService::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_SHOW)
                putExtra(EXTRA_NAME, preset.name)
                // 获取动态生成的 sections
                val sections = preset.getDisplaySections(context)
                putParcelableArrayListExtra(EXTRA_SECTIONS, ArrayList(sections))
                
                putExtra(EXTRA_PRESET_ID, preset.id ?: "")
                putExtra(EXTRA_PRESET_INDEX, presetIndex)
                putStringArrayListExtra(EXTRA_PRESET_LIST, ArrayList(presetIds))
                putExtra(EXTRA_IS_EXPANDED, true)
            }
            context.startService(intent)
        }

        /**
         * 更新悬浮窗内容（不重启服务，避免闪动）
         */
        fun update(context: Context, preset: com.silas.omaster.model.MasterPreset, presetIndex: Int = 0, presetIds: List<String> = emptyList()) {
            val intent = Intent(context, FloatingWindowService::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_UPDATE)
                putExtra(EXTRA_NAME, preset.name)
                val sections = preset.getDisplaySections(context)
                putParcelableArrayListExtra(EXTRA_SECTIONS, ArrayList(sections))
                
                putExtra(EXTRA_PRESET_ID, preset.id ?: "")
                putExtra(EXTRA_PRESET_INDEX, presetIndex)
                putStringArrayListExtra(EXTRA_PRESET_LIST, ArrayList(presetIds))
                putExtra(EXTRA_IS_EXPANDED, instance?.isExpanded ?: true)
            }
            context.startService(intent)
        }

        fun hide(context: Context) {
            context.stopService(Intent(context, FloatingWindowService::class.java))
        }

        /**
         * 检查服务是否正在运行
         */
        fun isRunning(): Boolean = instance != null
    }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        removeWindow()
        instance = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            if (floatingView == null) {
                stopSelf()
            }
            return START_NOT_STICKY
        }

        val action = intent.getStringExtra(EXTRA_ACTION) ?: ACTION_SHOW
        val rawName = intent.getStringExtra(EXTRA_NAME) ?: getString(R.string.floating_preset)
        val name = PresetI18n.getLocalizedPresetName(this, rawName)
        
        val sections = intent.getParcelableArrayListExtra<PresetSection>(EXTRA_SECTIONS) ?: arrayListOf()

        isExpanded = intent.getBooleanExtra(EXTRA_IS_EXPANDED, true)
        val savedX = intent.getIntExtra(EXTRA_POS_X, -1)
        val savedY = intent.getIntExtra(EXTRA_POS_Y, -1)
        val currentIndex = intent.getIntExtra(EXTRA_PRESET_INDEX, 0)
        val presetList = intent.getStringArrayListExtra(EXTRA_PRESET_LIST) ?: arrayListOf()
        val totalCount = presetList.size

        when (action) {
            ACTION_UPDATE -> {
                // 更新模式：只更新内容，不移除窗口（避免闪动）
                updateWindowContent(
                    name, sections, currentIndex, totalCount
                )
            }
            else -> {
                // 显示模式：重新创建窗口
                removeWindow()
                if (isExpanded) {
                    showExpandedWindow(
                        name, sections, savedX, savedY,
                        currentIndex, totalCount
                    )
                } else {
                    showCollapsedWindow(
                        name, sections, savedX, savedY
                    )
                }
            }
        }

        return START_STICKY
    }

    // 保存视图引用，用于更新内容
    private var mainContainer: LinearLayout? = null
    private var titleTextView: TextView? = null

    /**
     * 更新窗口内容（不重新创建窗口，避免闪动）
     */
    private fun updateWindowContent(
        name: String,
        sections: ArrayList<PresetSection>,
        currentIndex: Int,
        totalCount: Int
    ) {
        // 如果窗口不存在，直接创建新窗口
        if (floatingView == null || mainContainer == null) {
            showExpandedWindow(
                name, sections, 50, 300,
                currentIndex, totalCount
            )
            return
        }

        try {
            // 更新标题
            titleTextView?.text = name

            // 尝试直接更新视图内容，避免重建视图
            val contentContainer = mainContainer?.findViewWithTag<LinearLayout>("content_container")
            
            // 简单起见，直接重建内容区域，因为 sections 结构可能变化
            // 移除旧内容并添加新内容
            contentContainer?.let { container ->
                // 使用 post 确保在 UI 线程执行
                container.post {
                    container.removeAllViews()
                    container.addView(createContentArea(sections))
                    // 请求重新布局
                    container.requestLayout()
                    floatingView?.requestLayout()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果更新失败，重新创建窗口
            showExpandedWindow(
                name, sections, params?.x ?: 50, params?.y ?: 300,
                currentIndex, totalCount
            )
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun showExpandedWindow(
        name: String,
        sections: ArrayList<PresetSection>,
        savedX: Int = -1,
        savedY: Int = -1,
        currentIndex: Int = 0,
        totalCount: Int = 1
    ) {
        try {
            val wm = windowManager ?: return

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
                x = if (savedX >= 0) savedX else 50
                y = if (savedY >= 0) savedY else 300
            }

            val rootLayout = createExpandedView(
                name, sections, currentIndex, totalCount
            ) { collapseToBubble(name, sections) }

            floatingView = rootLayout
            wm.addView(floatingView, params)
            setupDrag(wm)
            
            // 初始显示时自动贴边
            floatingView?.post { snapToEdge(wm) }

        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    private fun showCollapsedWindow(
        name: String,
        sections: ArrayList<PresetSection>,
        savedX: Int = -1,
        savedY: Int = -1
    ) {
        try {
            val wm = windowManager ?: return

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
                x = if (savedX >= 0) savedX else 50
                y = if (savedY >= 0) savedY else 300
            }

            val miniButton = createCollapsedView(name) {
                val intent = Intent(this, FloatingWindowService::class.java).apply {
                    putExtra(EXTRA_NAME, name)
                    putParcelableArrayListExtra(EXTRA_SECTIONS, sections)
                    putExtra(EXTRA_IS_EXPANDED, true)
                    putExtra(EXTRA_POS_X, params?.x ?: 50)
                    putExtra(EXTRA_POS_Y, params?.y ?: 300)
                }
                startService(intent)
            }

            floatingView = miniButton
            wm.addView(floatingView, params)
            setupDrag(wm)
            
            // 初始显示时自动贴边
            floatingView?.post { snapToEdge(wm) }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 创建展开视图 - 高级美观设计
     */
    private fun createExpandedView(
        name: String,
        sections: ArrayList<PresetSection>,
        currentIndex: Int = 0,
        totalCount: Int = 1,
        onCollapse: () -> Unit
    ): FrameLayout {
        val windowWidth = getWindowWidth()

        return FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                windowWidth,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )

            // 主容器 - 毛玻璃效果，固定宽度
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    windowWidth,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                background = createGlassmorphismBackground()
                setPadding(dpToPx(20), dpToPx(16), dpToPx(20), dpToPx(20))
            }
            mainContainer = container

            // 渐变标题栏（带切换按钮）
            val header = createGradientHeader(name, onCollapse, currentIndex, totalCount)
            container.addView(header)

            // 保存标题TextView引用
            titleTextView = (header as? LinearLayout)?.findViewWithTag<TextView>("title_text")

            // 内容容器（带tag，用于更新时查找）
            val contentContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                tag = "content_container"
            }

            // 添加内容
            contentContainer.addView(createContentArea(sections))

            container.addView(contentContainer)
            addView(container)
        }
    }

    /**
     * 创建内容区域（可复用） - 动态渲染
     */
    private fun createContentArea(sections: List<PresetSection>): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL

            sections.forEach { section ->
                // Section Title
                section.title?.let { title ->
                    addView(createSectionTitle(title))
                }

                // Section Items
                val items = section.items
                var i = 0
                while (i < items.size) {
                    val item = items[i]
                    if (item.span == 2) {
                        // Full width item (highlighted)
                        val icon = getIconForLabel(item.label)
                        addView(createHighlightedParam(icon, item.label, item.value))
                        i++
                    } else {
                        // Half width item
                        val left = item
                        var right: PresetItem? = null
                        if (i + 1 < items.size && items[i+1].span == 1) {
                            right = items[i+1]
                            i++
                        }
                        
                        val leftIcon = getIconForLabel(left.label)
                        val leftView = createSmallParamItem(leftIcon, left.label, left.value)
                        
                        val rightView = right?.let {
                            val rightIcon = getIconForLabel(it.label)
                            createSmallParamItem(rightIcon, it.label, it.value)
                        }
                        
                        addView(createParamRow(leftView, rightView))
                        i++
                    }
                }
            }
        }
    }
    
    /**
     * 根据标签获取对应图标
     */
    private fun getIconForLabel(label: String): String {
        return when {
            label.contains("滤镜") || label.contains("Filter") -> getString(R.string.floating_filter_icon)
            label.contains("柔光") || label.contains("Soft") -> getString(R.string.floating_soft_icon)
            label.contains("影调") || label.contains("Tone") -> getString(R.string.floating_tone_icon)
            label.contains("饱和") || label.contains("Saturation") -> getString(R.string.floating_saturation_icon)
            label.contains("冷暖") || label.contains("Warm") -> getString(R.string.floating_warm_icon)
            label.contains("青品") || label.contains("Cyan") -> getString(R.string.floating_cyan_icon)
            label.contains("锐度") || label.contains("Sharpness") -> getString(R.string.floating_sharpness_icon)
            label.contains("暗角") || label.contains("Vignette") -> getString(R.string.floating_vignette_icon)
            label.contains("白平衡") || label.contains("WB") -> "🌡️"
            label.contains("曝光") || label.contains("EV") -> "☀️"
            label.contains("ISO") -> "📸"
            label.contains("快门") || label.contains("Shutter") -> "⏱️"
            label.contains("建议") || label.contains("Tips") -> "💡"
            else -> "⚙️"
        }
    }

    /**
     * 创建收起视图 - 圆形应用图标
     */
    private fun createCollapsedView(
        name: String,
        onExpand: () -> Unit
    ): FrameLayout {
        val size = dpToPx(56)

        return FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(size, size)

            // 外发光效果 - 品牌色外溢
            val glowView = View(context).apply {
                layoutParams = FrameLayout.LayoutParams(size, size)
                background = createGlowBackground()
            }

            // 主按钮容器 - 圆形边框
            val button = FrameLayout(context).apply {
                layoutParams = FrameLayout.LayoutParams(
                    dpToPx(48),
                    dpToPx(48)
                ).apply {
                    gravity = Gravity.CENTER
                }
                
                // 圆形黑色底色（防止图标透明部分看到背景）
                background = GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(Color.BLACK)
                    setStroke(dpToPx(1), Color.parseColor("#1A000000")) // 极淡的描边增加立体感
                }

                // 应用图标
                val iconView = ImageView(context).apply {
                    layoutParams = FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                    ).apply {
                        val margin = dpToPx(1) // 留一点边距，显示底色的圆边
                        setMargins(margin, margin, margin, margin)
                    }
                    setImageResource(R.mipmap.ic_launcher_round)
                    scaleType = ImageView.ScaleType.FIT_CENTER
                }
                
                addView(iconView)
            }

            addView(glowView)
            addView(button)

            // 整个容器可点击
            setOnClickListener { onExpand() }
        }
    }

    /**
     * 创建毛玻璃背景
     */
    private fun createGlassmorphismBackground(): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dpToPx(24).toFloat()
            setColor(backgroundColor)
            // 添加边框效果
            setStroke(dpToPx(1), Color.parseColor("#33FFFFFF"))
        }
    }

    /**
     * 创建渐变标题栏（带切换预设按钮）
     */
    private fun createGradientHeader(
        name: String,
        onCollapse: () -> Unit,
        currentIndex: Int = 0,
        totalCount: Int = 1
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(12))

            // 上一个预设按钮
            val prevBtn = createIconButton("◀") {
                sendPresetSwitchBroadcast("prev")
            }
            addView(prevBtn)
            addView(createSpacing(dpToPx(6)))

            // 预设名称 - 带渐变效果
            val titleView = TextView(context).apply {
                text = name
                textSize = if (name.length > 8) 15f else 18f
                paint.shader = LinearGradient(
                    0f, 0f, 200f, 0f,
                    primaryColor,
                    Color.parseColor("#FFB347"),
                    Shader.TileMode.CLAMP
                )
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                gravity = Gravity.CENTER
                tag = "title_text"
            }
            this@FloatingWindowService.titleTextView = titleView

            addView(titleView)

            // 下一个预设按钮
            addView(createSpacing(dpToPx(6)))
            val nextBtn = createIconButton("▶") {
                sendPresetSwitchBroadcast("next")
            }
            addView(nextBtn)

            addView(createSpacing(dpToPx(6)))

            // 收起按钮
            val collapseBtn = createIconButton("▼") { onCollapse() }
            addView(collapseBtn)

            addView(createSpacing(dpToPx(6)))

            // 关闭按钮
            val closeBtn = createIconButton("✕") { stopSelf() }
            addView(closeBtn)
        }
    }

    /**
     * 发送切换预设广播
     */
    private fun sendPresetSwitchBroadcast(direction: String) {
        val intent = Intent(ACTION_SWITCH_PRESET).apply {
            putExtra(EXTRA_SWITCH_DIRECTION, direction)
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    /**
     * 创建图标按钮
     */
    private fun createIconButton(icon: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = icon
            textSize = 14f
            setTextColor(textSecondary)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(32), dpToPx(32))
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(cardBackground)
            }
            setOnClickListener { onClick() }
        }
    }

    /**
     * 创建区域标题
     */
    private fun createSectionTitle(title: String): TextView {
        return TextView(this).apply {
            text = title
            textSize = 11f
            setTextColor(textMuted)
            setPadding(0, dpToPx(12), 0, dpToPx(8))
        }
    }

    /**
     * 创建高亮参数项（滤镜专用）
     */
    private fun createHighlightedParam(icon: String, label: String, value: String, valueTag: String? = null): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(12), dpToPx(10), dpToPx(12), dpToPx(10))
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(12).toFloat()
                setColor(Color.parseColor("#20FF6B35"))
                setStroke(dpToPx(1), Color.parseColor("#40FF6B35"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, dpToPx(8))
            }

            // 图标
            addView(TextView(context).apply {
                text = icon
                textSize = 16f
                setTextColor(primaryColor)
            })

            addView(createSpacing(dpToPx(8)))

            // 标签
            addView(TextView(context).apply {
                text = label
                textSize = 13f
                setTextColor(textSecondary)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            // 值
            addView(TextView(context).apply {
                text = value
                textSize = 14f
                setTextColor(primaryColor)
                setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2))
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(6).toFloat()
                    setColor(Color.parseColor("#30FF6B35"))
                }
                // 设置 Tag 方便查找更新
                if (valueTag != null) {
                    tag = valueTag
                }
            })
        }
    }

    /**
     * 创建小型参数项（用于网格）
     */
    private fun createSmallParamItem(icon: String, label: String, value: String, valueTag: String? = null): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dpToPx(8), dpToPx(6), dpToPx(8), dpToPx(6))
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(8).toFloat()
                setColor(cardBackground)
            }
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                setMargins(0, 0, dpToPx(4), 0)
            }

            addView(TextView(context).apply {
                text = icon
                textSize = 12f
                setTextColor(primaryColor)
            })

            addView(createSpacing(dpToPx(4)))

            addView(TextView(context).apply {
                text = "$label "
                textSize = 11f
                setTextColor(textMuted)
            })

            addView(TextView(context).apply {
                text = value
                textSize = 12f
                setTextColor(textPrimary)
                // 设置 Tag 方便查找更新
                if (valueTag != null) {
                    tag = valueTag
                }
            })
        }
    }

    /**
     * 创建参数行（两个参数并排）
     */
    private fun createParamRow(left: LinearLayout, right: LinearLayout?): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dpToPx(4), 0, 0)

            addView(left)
            if (right != null) {
                right.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply {
                    setMargins(dpToPx(4), 0, 0, 0)
                }
                addView(right)
            } else {
                // 占位
                addView(View(context).apply {
                    layoutParams = LinearLayout.LayoutParams(0, 0, 1f)
                })
            }
        }
    }

    /**
     * 创建渐变圆形背景（收起按钮）
     */
    private fun createGradientCircleBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            colors = intArrayOf(primaryColor, primaryDark)
            gradientType = GradientDrawable.RADIAL_GRADIENT
            gradientRadius = dpToPx(24).toFloat()
        }
    }

    /**
     * 创建外发光效果
     */
    private fun createGlowBackground(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            colors = intArrayOf(
                Color.parseColor("#40FF6B35"),
                Color.parseColor("#20FF6B35"),
                Color.TRANSPARENT
            )
            gradientType = GradientDrawable.RADIAL_GRADIENT
            gradientRadius = dpToPx(28).toFloat()
        }
    }

    /**
     * 创建间距
     */
    private fun createSpacing(size: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(size, size)
        }
    }

    private fun collapseToBubble(
        name: String,
        sections: ArrayList<PresetSection>
    ) {
        try {
            val currentX = params?.x ?: 50
            val currentY = params?.y ?: 300

            removeWindow()
            isExpanded = false

            val intent = Intent(this, FloatingWindowService::class.java).apply {
                putExtra(EXTRA_NAME, name)
                putParcelableArrayListExtra(EXTRA_SECTIONS, sections)
                putExtra(EXTRA_IS_EXPANDED, false)
                putExtra(EXTRA_POS_X, currentX)
                putExtra(EXTRA_POS_Y, currentY)
            }
            startService(intent)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 悬浮窗宽度 - 固定 280dp
     * 无论横竖屏都使用相同的小宽度，确保不会铺满屏幕
     */
    private fun getWindowWidth(): Int {
        return dpToPx(280)
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
            private val clickThreshold = 20f

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
                        if (Math.abs(dx) > clickThreshold || Math.abs(dy) > clickThreshold) {
                            isClick = false
                        }
                        
                        val metrics = DisplayMetrics()
                        wm.defaultDisplay.getMetrics(metrics)
                        
                        params?.x = initialX + dx.toInt()
                        
                        // 垂直方向限制，防止超出屏幕
                        val newY = initialY + dy.toInt()
                        val maxY = metrics.heightPixels - (floatingView?.height ?: 0)
                        params?.y = newY.coerceIn(0, maxY)
                        
                        floatingView?.let { view ->
                            params?.let { p ->
                                wm.updateViewLayout(view, p)
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!isClick) {
                            // 实现贴边收纳逻辑
                            snapToEdge(wm)
                        }
                    }
                }
                return false
            }
        })
    }

    /**
     * 将悬浮窗平滑移动至屏幕边缘
     */
    private fun snapToEdge(wm: WindowManager) {
        val view = floatingView ?: return
        val p = params ?: return
        
        val metrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(metrics)
        val screenWidth = metrics.widthPixels
        val viewWidth = view.width

        // 计算目标位置：左边(0)或右边(screenWidth - viewWidth)
        // 如果是收起状态，可以进一步实现“半收纳”效果，即只露出一半图标
        val targetX = if (p.x + viewWidth / 2 < screenWidth / 2) {
            if (!isExpanded) -viewWidth / 2 else 0
        } else {
            if (!isExpanded) screenWidth - viewWidth / 2 else screenWidth - viewWidth
        }

        // 使用动画平滑移动
        val animator = ValueAnimator.ofInt(p.x, targetX)
        animator.duration = 300
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { animation ->
            if (floatingView != null) {
                p.x = animation.animatedValue as Int
                wm.updateViewLayout(view, p)
            }
        }
        animator.start()
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
