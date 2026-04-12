package com.silas.omaster.ui.service

import android.animation.ValueAnimator
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.PixelFormat
import android.graphics.Shader
import android.graphics.Typeface
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
import com.silas.omaster.data.config.ConfigCenter
import com.silas.omaster.data.local.FloatingWindowMode
import com.silas.omaster.model.PresetItem
import com.silas.omaster.model.PresetSection
import com.silas.omaster.util.IconFont
import com.silas.omaster.util.Logger
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
    private var edgeAnimator: ValueAnimator? = null  // ✅ 新增：保存动画引用用于生命周期管理

    // 配色方案
    private val primaryColor = Color.parseColor("#FF6B35")      // 品牌橙色
    private val primaryDark = Color.parseColor("#E55A2B")       // 深橙色
    private val cardBackground = Color.parseColor("#26FFFFFF")  // 卡片背景
    private val textPrimary = Color.parseColor("#FFFFFF")       // 主文字
    private val textSecondary = Color.parseColor("#B3FFFFFF")   // 次要文字
    private val textMuted = Color.parseColor("#80FFFFFF")       // 弱化文字
    
    // 背景颜色根据设置动态计算
    private fun getBackgroundColor(context: Context): Int {
        val opacity = ConfigCenter.getInstance(context).floatingWindowOpacity
        val alpha = (opacity * 255 / 100).coerceIn(30, 255)
        return Color.argb(alpha, 26, 26, 26) // #1A1A1A with dynamic alpha
    }

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
            Logger.i("FloatingWindowService", "显示悬浮窗: ${preset.name}, 索引: $presetIndex, 总数: ${presetIds.size}")
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
            Logger.d("FloatingWindowService", "更新悬浮窗: ${preset.name}, 索引: $presetIndex")
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
            Logger.i("FloatingWindowService", "隐藏悬浮窗")
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
        // ✅ 取消正在执行的贴边动画，防止内存泄漏和崩溃
        edgeAnimator?.cancel()
        edgeAnimator = null
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

        // 读取悬浮窗模式设置
        val configMode = ConfigCenter.getInstance(this).floatingWindowMode
        
        // Realme 预设强制使用标准悬浮窗（新版悬浮窗图标不适配 Realme 相机 UI）
        val mode = if (isRealmePreset(sections)) {
            FloatingWindowMode.STANDARD
        } else {
            configMode
        }

        when (action) {
            ACTION_UPDATE -> {
                // 更新模式：只更新内容，不移除窗口（避免闪动）
                updateWindowContent(
                    name, sections, currentIndex, totalCount, mode
                )
            }
            else -> {
                // 显示模式：重新创建窗口
                removeWindow()
                if (isExpanded) {
                    when (mode) {
                        FloatingWindowMode.STANDARD -> showExpandedWindow(
                            name, sections, savedX, savedY,
                            currentIndex, totalCount
                        )
                        FloatingWindowMode.COMPACT -> showCompactWindow(
                            name, sections, savedX, savedY,
                            currentIndex, totalCount
                        )
                    }
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
     * 更新窗口内容（数据驱动刷新，避免 UI 重建）
     */
    private fun updateWindowContent(
        name: String,
        sections: ArrayList<PresetSection>,
        currentIndex: Int,
        totalCount: Int,
        mode: FloatingWindowMode = FloatingWindowMode.STANDARD
    ) {
        // 如果窗口不存在，直接创建新窗口
        if (floatingView == null || mainContainer == null) {
            when (mode) {
                FloatingWindowMode.STANDARD -> showExpandedWindow(
                    name, sections, 50, 300,
                    currentIndex, totalCount
                )
                FloatingWindowMode.COMPACT -> showCompactWindow(
                    name, sections, 50, 300,
                    currentIndex, totalCount
                )
            }
            return
        }

        try {
            // ✅ 1. 直接更新标题（无动画，避免闪烁）
            titleTextView?.text = name

            // ✅ 2. 更新内容区域（只更新数据，不重建视图）
            val contentContainer = mainContainer?.findViewWithTag<LinearLayout>("content_container")
            contentContainer?.let { container ->
                container.post {
                    when (mode) {
                        FloatingWindowMode.STANDARD -> updateStandardContent(sections)
                        FloatingWindowMode.COMPACT -> updateCompactContent(sections)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果更新失败（如 sections 结构变化），降级为重建窗口
            when (mode) {
                FloatingWindowMode.STANDARD -> showExpandedWindow(
                    name, sections, params?.x ?: 50, params?.y ?: 300,
                    currentIndex, totalCount
                )
                FloatingWindowMode.COMPACT -> showCompactWindow(
                    name, sections, params?.x ?: 50, params?.y ?: 300,
                    currentIndex, totalCount
                )
            }
        }
    }

    /**
     * 标准模式：只更新参数值（不重建视图）
     */
    private fun updateStandardContent(sections: List<PresetSection>) {
        // 遍历所有 section 和 item，根据 tag 查找并更新 TextView
        sections.forEach { section ->
            section.items.forEach { item ->
                // 为每个参数生成唯一的 tag（与创建时一致）
                val valueTag = "value_${item.label}_${item.span}"
                
                // 查找所有匹配的 TextView 并更新
                // 由于中文标签可能包含特殊字符，使用递归查找
                floatingView?.let { root ->
                    val valueView = findViewWithTagRecursive(root, valueTag)
                    valueView?.text = PresetI18n.resolveValue(this, item.value)
                }
            }
        }
    }

    /**
     * 递归查找带指定 Tag 的 TextView
     */
    private fun findViewWithTagRecursive(root: View, tag: String): TextView? {
        if (tag == root.tag) {
            return root as? TextView
        }
        
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) {
                val child = root.getChildAt(i)
                val result = findViewWithTagRecursive(child, tag)
                if (result != null) {
                    return result
                }
            }
        }
        
        return null
    }

    /**
     * 紧凑模式：动态更新参数值（不重建视图）
     * 根据实际存在的参数数量动态更新
     */
    private fun updateCompactContent(sections: List<PresetSection>) {
        val paramData = extractDynamicParams(sections)

        // 动态更新参数值 - 只更新实际存在的参数
        paramData.forEachIndexed { index, (value, _) ->
            val valueView = floatingView?.findViewWithTag<TextView>("compact_value_$index")
            valueView?.text = value
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
     * 显示新版紧凑悬浮窗（参数条样式）
     */
    private fun showCompactWindow(
        name: String,
        sections: ArrayList<PresetSection>,
        savedX: Int = -1,
        savedY: Int = -1,
        currentIndex: Int = 0,
        totalCount: Int = 1
    ) {
        try {
            val wm = windowManager ?: return

            // 获取屏幕宽度，计算悬浮窗宽度（屏幕宽度 - 32dp）
            val metrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(metrics)
            val screenWidth = metrics.widthPixels
            val windowWidth = screenWidth - dpToPx(32)

            params = WindowManager.LayoutParams(
                windowWidth,
                dpToPx(120), // 高度增加到 120dp（标题栏40dp + 参数区80dp）
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
                // 默认位置：屏幕底部偏上，避开相机控制区
                x = if (savedX >= 0) savedX else dpToPx(16)
                y = if (savedY >= 0) savedY else metrics.heightPixels - dpToPx(300)
            }

            val rootLayout = createCompactView(
                name, sections, currentIndex, totalCount, windowWidth
            ) { collapseToBubble(name, sections) }

            floatingView = rootLayout
            wm.addView(floatingView, params)
            setupDrag(wm)

        } catch (e: Exception) {
            e.printStackTrace()
            stopSelf()
        }
    }

    /**
     * 创建紧凑视图 - 新版参数条样式
     */
    private fun createCompactView(
        name: String,
        sections: ArrayList<PresetSection>,
        currentIndex: Int = 0,
        totalCount: Int = 1,
        windowWidth: Int,
        onCollapse: () -> Unit
    ): FrameLayout {
        return FrameLayout(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                windowWidth,
                dpToPx(120)
            )

            // 主容器
            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    windowWidth,
                    dpToPx(120)
                )
                background = createCompactBackground(context)
            }
            mainContainer = container

            // 添加标题栏（预设名称 + 切换按钮 + 收起按钮）
            container.addView(createCompactHeader(name, onCollapse, currentIndex, totalCount))

            // 内容容器（带tag，用于更新时查找）
            val contentContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                tag = "content_container"
            }
            contentContainer.addView(createCompactContentArea(sections))
            container.addView(contentContainer)

            addView(container)
        }
    }

    /**
     * 创建紧凑标题栏
     */
    private fun createCompactHeader(
        name: String,
        onCollapse: () -> Unit,
        currentIndex: Int = 0,
        totalCount: Int = 1
    ): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(40)
            )
            setPadding(dpToPx(12), dpToPx(4), dpToPx(12), dpToPx(4))

            // 上一个预设按钮
            val prevBtn = createCompactIconButton("◀") {
                sendPresetSwitchBroadcast("prev")
            }
            addView(prevBtn)

            addView(createSpacing(dpToPx(8)))

            // 预设名称
            val titleView = TextView(context).apply {
                text = name
                textSize = 14f
                setTextColor(primaryColor)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                maxLines = 1
                ellipsize = android.text.TextUtils.TruncateAt.END
            }
            titleTextView = titleView
            addView(titleView)

            addView(createSpacing(dpToPx(8)))

            // 下一个预设按钮
            val nextBtn = createCompactIconButton("▶") {
                sendPresetSwitchBroadcast("next")
            }
            addView(nextBtn)

            addView(createSpacing(dpToPx(8)))

            // 收起按钮
            val collapseBtn = createCompactIconButton("▼") { onCollapse() }
            addView(collapseBtn)

            addView(createSpacing(dpToPx(4)))

            // 关闭按钮
            val closeBtn = createCompactIconButton("✕") { stopSelf() }
            addView(closeBtn)
        }
    }

    /**
     * 创建紧凑图标按钮
     */
    private fun createCompactIconButton(icon: String, onClick: () -> Unit): TextView {
        return TextView(this).apply {
            text = icon
            textSize = 12f
            setTextColor(textSecondary)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(dpToPx(28), dpToPx(28))
            background = GradientDrawable().apply {
                cornerRadius = dpToPx(6).toFloat()
                setColor(cardBackground)
            }
            // 禁用父视图拦截按钮的触摸事件
            setOnTouchListener { _, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        // 请求父视图不要拦截此事件
                        parent.requestDisallowInterceptTouchEvent(true)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false // 继续传递给 onClickListener
            }
            setOnClickListener { onClick() }
        }
    }

    /**
     * 创建紧凑内容区域 - 动态适配参数数量（支持8-11参数）
     * 根据实际传入的 sections 中的参数数量动态显示，避免空白占位符
     */
    private fun createCompactContentArea(sections: List<PresetSection>): LinearLayout {
        // 提取实际存在的参数（过滤掉无效的"-"）
        val paramData = extractDynamicParams(sections)
        val actualParamCount = paramData.size

        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(80) // 参数区域高度保持 80dp
            )
            setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4))

            // 第一行：参数值（带索引 Tag）
            val valuesRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                gravity = Gravity.CENTER_VERTICAL

                // 只显示实际存在的参数值
                paramData.forEachIndexed { index, (value, _) ->
                    addView(createCompactValueCell(value, index))
                }
            }
            addView(valuesRow)

            // 分割线
            addView(View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(1)
                )
                setBackgroundColor(Color.parseColor("#20FFFFFF"))
            })

            // 第二行：图标（使用 Iconfont 或文字标签）
            val iconsRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    0,
                    1f
                )
                gravity = Gravity.CENTER_VERTICAL

                // 只显示实际存在的参数对应的图标
                paramData.forEach { (_, iconCode) ->
                    addView(createCompactIconCell(iconCode))
                }
            }
            addView(iconsRow)
        }
    }

    /**
     * 从 sections 中动态提取参数（支持8-11参数）
     * 只返回实际存在的参数，不包含空白占位符
     * 返回: List<Pair<参数值, 图标编码>>
     */
    private fun extractDynamicParams(sections: List<PresetSection>): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()

        // 检查是否有Realme特有参数
        var hasRealmeParams = false
        var hasRealmeExtendedParams = false
        sections.forEach { section ->
            section.items.forEach { item ->
                if (item.label.contains("对比度（亮部）") || item.label.contains("对比度（暗部）")) {
                    hasRealmeParams = true
                }
                // Realme 扩展参数检测（不包含"对比度"，因为OPPO预设也可能有对比度）
                if (item.label.contains("清晰") || item.label.contains("褪色") || 
                    item.label.contains("颗粒") || item.label.contains("颗粒强度")) {
                    hasRealmeExtendedParams = true
                }
            }
        }

        // 根据预设类型选择参数映射
        val labelToIcon = if (hasRealmeParams) {
            // Realme GR预设：12个参数，全部使用文字
            // 参数顺序：滤镜、饱和度、色相、影调、对比度、对比度(亮)、对比度(暗)、锐度、明暗、清晰、颗粒强度、颗粒尺寸
            listOf(
                listOf("滤镜", "Filter") to IconFont.FILTER_TEXT,
                listOf("饱和", "Saturation") to IconFont.SATURATION_TEXT,
                listOf("色相", "Hue") to IconFont.HUE_TEXT,
                listOf("影调", "Tone") to IconFont.TONE_TEXT,
                listOf("对比度", "Contrast") to IconFont.CONTRAST,
                listOf("对比度（亮部）", "对比度（亮）", "contrast_highlight") to IconFont.CONTRAST_HIGHLIGHT_TEXT,
                listOf("对比度（暗部）", "对比度（暗）", "contrast_shadow") to IconFont.CONTRAST_SHADOW_TEXT,
                listOf("锐度", "Sharpness") to IconFont.SHARPNESS_TEXT,
                listOf("明暗", "Brightness") to IconFont.BRIGHTNESS_TEXT,
                listOf("清晰", "Clarity") to IconFont.CLARITY,
                listOf("颗粒", "颗粒强度", "Grain", "grain_intensity") to IconFont.GRAIN_INTENSITY_TEXT,
                listOf("颗粒尺寸", "grain_size") to IconFont.GRAIN_SIZE_TEXT
            )
        } else if (hasRealmeExtendedParams) {
            // Realme预设（老版本）：全部使用文字
            listOf(
                // 基础8参数 - 文字
                listOf("滤镜", "Filter") to IconFont.FILTER_TEXT,
                listOf("柔光", "Soft") to IconFont.SOFT_LIGHT_TEXT,
                listOf("影调", "Tone") to IconFont.TONE_TEXT,
                listOf("饱和", "Saturation") to IconFont.SATURATION_TEXT,
                listOf("冷暖", "Warm") to IconFont.WARM_COOL_TEXT,
                listOf("青品", "Cyan") to IconFont.CYAN_TEXT,
                listOf("锐度", "Sharpness") to IconFont.SHARPNESS_TEXT,
                listOf("暗角", "Vignette") to IconFont.VIGNETTE_TEXT,
                // 扩展3参数 - 文字
                listOf("清晰", "Clarity") to IconFont.CLARITY,
                listOf("对比度", "褪色", "Contrast", "Fade") to IconFont.CONTRAST,
                listOf("颗粒", "颗粒强度", "Grain") to IconFont.GRAIN
            )
        } else {
            // OPPO/一加预设：使用图标
            listOf(
                // 基础8参数 - 图标
                listOf("滤镜", "Filter") to IconFont.FILTER,
                listOf("柔光", "Soft") to IconFont.SOFT_LIGHT,
                listOf("影调", "Tone") to IconFont.TONE,
                listOf("饱和", "Saturation") to IconFont.SATURATION,
                listOf("冷暖", "Warm") to IconFont.WARM_COOL,
                listOf("青品", "Cyan") to IconFont.CYAN,
                listOf("锐度", "Sharpness") to IconFont.SHARPNESS,
                listOf("暗角", "Vignette") to IconFont.VIGNETTE,
                // 扩展3参数 - 文字
                listOf("清晰", "Clarity") to IconFont.CLARITY,
                listOf("对比度", "褪色", "Contrast", "Fade") to IconFont.CONTRAST,
                listOf("颗粒", "颗粒强度", "Grain") to IconFont.GRAIN
            )
        }

        // 按顺序检查每个参数是否存在
        labelToIcon.forEach { (labels, icon) ->
            var found = false
            var value = "-"

            sections.forEach { section ->
                section.items.forEach { item ->
                    labels.forEach { label ->
                        if (!found && item.label.contains(label)) {
                            value = PresetI18n.resolveValue(this, item.value)
                            found = true
                        }
                    }
                }
            }

            // 只添加实际存在的参数（值不为"-"）
            if (found && value != "-") {
                result.add(value to icon)
            }
        }

        return result
    }

    /**
     * 检测是否为 Realme 预设
     * Realme 预设特有参数：对比度（亮部）、对比度（暗部）
     */
    private fun isRealmePreset(sections: List<PresetSection>): Boolean {
        sections.forEach { section ->
            section.items.forEach { item ->
                if (item.label.contains("对比度（亮部）") || item.label.contains("对比度（暗部）")) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 从 sections 中提取11个参数值（支持真我理光GR等11参数预设）
     * 用于更新模式，保持固定11个位置
     */
    private fun extract11Params(sections: List<PresetSection>): List<String> {
        // 检测是否为 Realme GR 预设
        val isRealmeGR = sections.any { section ->
            section.items.any { item ->
                item.label.contains("对比度（亮部）") || item.label.contains("对比度（暗部）")
            }
        }
        
        return if (isRealmeGR) {
            extractRealmeGRParams(sections)
        } else {
            extractStandard11Params(sections)
        }
    }
    
    /**
     * 提取 Realme GR 预设的12个参数
     * Realme GR 参数顺序：
     * 0.滤镜、1.饱和度、2.色相(青品)、3.影调、4.对比度、5.对比度(亮)、6.对比度(暗)、
     * 7.锐度、8.明暗(冷暖)、9.清晰、10.颗粒强度、11.颗粒尺寸
     */
    private fun extractRealmeGRParams(sections: List<PresetSection>): List<String> {
        val result = MutableList(12) { "-" }
        
        // Realme GR 专用参数映射（12参数）
        val labelToIndex = mapOf(
            "滤镜" to 0, "Filter" to 0,
            "饱和" to 1, "Saturation" to 1,
            "色相" to 2, "Hue" to 2,
            "影调" to 3, "Tone" to 3,
            "对比度" to 4, "Contrast" to 4,
            "对比度（亮部）" to 5, "对比度（亮）" to 5, "contrast_highlight" to 5,
            "对比度（暗部）" to 6, "对比度（暗）" to 6, "contrast_shadow" to 6,
            "锐度" to 7, "Sharpness" to 7,
            "明暗" to 8, "Brightness" to 8,
            "清晰" to 9, "Clarity" to 9,
            "颗粒" to 10, "颗粒强度" to 10, "Grain" to 10, "grain_intensity" to 10,
            "颗粒尺寸" to 11, "grain_size" to 11
        )
        
        sections.forEach { section ->
            section.items.forEach { item ->
                labelToIndex.forEach { (label, index) ->
                    if (item.label.contains(label)) {
                        result[index] = PresetI18n.resolveValue(this, item.value)
                    }
                }
            }
        }
        
        return result
    }
    
    /**
     * 提取标准11参数（OPPO/一加/Realme老版本）
     * 标准参数顺序：滤镜、柔光、影调、饱和、冷暖、青品、锐度、暗角、清晰、对比度、颗粒
     */
    private fun extractStandard11Params(sections: List<PresetSection>): List<String> {
        val result = MutableList(11) { "-" }

        // 标准参数标签到索引的映射
        val labelToIndex = mapOf(
            "滤镜" to 0, "Filter" to 0,
            "柔光" to 1, "Soft" to 1,
            "影调" to 2, "Tone" to 2,
            "饱和" to 3, "Saturation" to 3,
            "冷暖" to 4, "Warm" to 4,
            "青品" to 5, "Cyan" to 5,
            "锐度" to 6, "Sharpness" to 6,
            "暗角" to 7, "Vignette" to 7,
            "清晰" to 8, "Clarity" to 8,
            "对比度" to 9, "褪色" to 9, "Contrast" to 9, "Fade" to 9,
            "颗粒" to 10, "Grain" to 10, "颗粒强度" to 10
        )

        sections.forEach { section ->
            section.items.forEach { item ->
                labelToIndex.forEach { (label, index) ->
                    if (item.label.contains(label)) {
                        result[index] = PresetI18n.resolveValue(this, item.value)
                    }
                }
            }
        }

        return result
    }

    /**
     * 创建紧凑参数值单元格（带 Tag 用于更新）
     */
    private fun createCompactValueCell(value: String, index: Int = 0): TextView {
        return TextView(this).apply {
            text = value
            textSize = 10f  // 11列时减小字体，避免溢出
            setTextColor(textPrimary)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(0, dpToPx(2), 0, dpToPx(2))
            // ✅ 添加唯一 Tag 用于数据刷新
            tag = "compact_value_$index"
        }
    }

    /**
     * 创建紧凑图标单元格（支持 Iconfont 和文字标签）
     */
    private fun createCompactIconCell(iconCode: String): TextView {
        return TextView(this).apply {
            text = iconCode
            textSize = 14f  // 11列时减小图标，保持美观
            setTextColor(primaryColor)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            setPadding(0, dpToPx(2), 0, dpToPx(2))
            // 判断是否为 Iconfont 图标（Unicode 编码字符）
            // 如果是文字标签（如"清晰"、"褪色"、"颗粒"），使用默认字体
            if (iconCode.startsWith("\\u") || iconCode.length == 1 && iconCode[0].code >= 0xE600) {
                typeface = IconFont.getTypeface(this@FloatingWindowService)
            } else {
                typeface = Typeface.DEFAULT  // 文字标签使用默认字体
            }
        }
    }

    /**
     * 创建紧凑背景
     */
    private fun createCompactBackground(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dpToPx(16).toFloat()
            setColor(getBackgroundColor(context))
            setStroke(dpToPx(1), Color.parseColor("#33FFFFFF"))
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
                background = createGlassmorphismBackground(context)
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
                        val localizedValue = PresetI18n.resolveValue(this@FloatingWindowService, item.value)
                        addView(createHighlightedParam(icon, item.label, localizedValue))
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
                        val leftLocalizedValue = PresetI18n.resolveValue(this@FloatingWindowService, left.value)
                        val leftView = createSmallParamItem(leftIcon, left.label, leftLocalizedValue)
                        
                        val rightView = right?.let {
                            val rightIcon = getIconForLabel(it.label)
                            val rightLocalizedValue = PresetI18n.resolveValue(this@FloatingWindowService, it.value)
                            createSmallParamItem(rightIcon, it.label, rightLocalizedValue)
                        }
                        
                        addView(createParamRow(leftView, rightView))
                        i++
                    }
                }
            }
        }
    }
    
    /**
     * 根据标签获取对应图标（使用 Iconfont）
     */
    private fun getIconForLabel(label: String): String {
        return when {
            label.contains("滤镜") || label.contains("Filter") -> IconFont.FILTER
            label.contains("柔光") || label.contains("Soft") -> IconFont.SOFT_LIGHT
            label.contains("影调") || label.contains("Tone") -> IconFont.TONE
            label.contains("饱和") || label.contains("Saturation") -> IconFont.SATURATION
            label.contains("冷暖") || label.contains("Warm") -> IconFont.WARM_COOL
            label.contains("青品") || label.contains("Cyan") -> IconFont.CYAN
            label.contains("锐度") || label.contains("Sharpness") -> IconFont.SHARPNESS
            label.contains("暗角") || label.contains("Vignette") -> IconFont.VIGNETTE
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
                    // 禁用 ImageView 的触摸事件，让事件传递给父容器
                    isClickable = false
                    isFocusable = false
                }
                
                addView(iconView)
                // 禁用 button 容器的触摸事件拦截，让事件传递给最外层容器
                isClickable = false
                isFocusable = false
            }

            addView(glowView)
            addView(button)

            // 整个容器可点击
            setOnClickListener { onExpand() }
            // 确保外层容器可以接收触摸事件用于拖动
            isClickable = true
            isFocusable = true
        }
    }

    /**
     * 创建毛玻璃背景
     */
    private fun createGlassmorphismBackground(context: Context): GradientDrawable {
        return GradientDrawable().apply {
            cornerRadius = dpToPx(24).toFloat()
            setColor(getBackgroundColor(context))
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
     * 创建高亮参数项（滤镜专用）- 带唯一 Tag
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

            // 图标（使用 Iconfont）
            addView(TextView(context).apply {
                text = icon
                textSize = 18f
                setTextColor(primaryColor)
                typeface = IconFont.getTypeface(this@FloatingWindowService)
            })

            addView(createSpacing(dpToPx(8)))

            // 标签
            addView(TextView(context).apply {
                text = label
                textSize = 13f
                setTextColor(textSecondary)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            // 值 - ✅ 添加唯一 Tag 用于数据刷新
            addView(TextView(context).apply {
                text = value
                textSize = 14f
                setTextColor(primaryColor)
                setPadding(dpToPx(8), dpToPx(2), dpToPx(8), dpToPx(2))
                background = GradientDrawable().apply {
                    cornerRadius = dpToPx(6).toFloat()
                    setColor(Color.parseColor("#30FF6B35"))
                }
                // 使用 label + span 作为唯一标识
                tag = "value_${label}_2"
            })
        }
    }

    /**
     * 创建小型参数项（用于网格）- 带唯一 Tag
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

            // 图标（使用 Iconfont）
            addView(TextView(context).apply {
                text = icon
                textSize = 14f
                setTextColor(primaryColor)
                typeface = IconFont.getTypeface(this@FloatingWindowService)
            })

            addView(createSpacing(dpToPx(4)))

            addView(TextView(context).apply {
                text = "$label "
                textSize = 11f
                setTextColor(textMuted)
            })

            // 值 - ✅ 添加唯一 Tag 用于数据刷新
            addView(TextView(context).apply {
                text = value
                textSize = 12f
                setTextColor(textPrimary)
                // 使用 label + span 作为唯一标识
                tag = "value_${label}_1"
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
            private var isDragging = false  // ✅ 新增：跟踪是否正在拖动

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params?.x ?: 0
                        initialY = params?.y ?: 0
                        touchX = event.rawX
                        touchY = event.rawY
                        isClick = true
                        isDragging = false  // ✅ 重置拖动状态
                    }
                    MotionEvent.ACTION_MOVE -> {
                        val dx = event.rawX - touchX
                        val dy = event.rawY - touchY
                        if (Math.abs(dx) > clickThreshold || Math.abs(dy) > clickThreshold) {
                            isClick = false
                            isDragging = true  // ✅ 标记为拖动中
                        }

                        // 只有在拖动时才更新位置
                        if (isDragging) {
                            val metrics = DisplayMetrics()
                            wm.defaultDisplay.getMetrics(metrics)

                            params?.x = initialX + dx.toInt()

                            // 垂直方向限制，防止超出屏幕
                            val newY = initialY + dy.toInt()
                            val viewHeight = floatingView?.height ?: dpToPx(56)
                            val maxY = metrics.heightPixels - viewHeight
                            params?.y = newY.coerceIn(0, maxY.coerceAtLeast(0))

                            floatingView?.let { view ->
                                params?.let { p ->
                                    try {
                                        wm.updateViewLayout(view, p)
                                    } catch (e: Exception) {
                                        Logger.e("FloatingWindowService", "拖动更新布局失败", e)
                                    }
                                }
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        if (isDragging) {
                            // ✅ 只在拖动操作时才贴边，避免与点击事件冲突
                            snapToEdge(wm)
                        }
                        isDragging = false  // ✅ 重置拖动状态
                        // ✅ 如果是点击操作，返回 false 让 onClick 处理
                        // ✅ 如果是拖动操作，返回 true 消费事件，防止触发 onClick
                        return !isClick
                    }
                }
                // ✅ MOVE 事件：只在拖动时返回 true 消费事件
                return isDragging && event.action == MotionEvent.ACTION_MOVE
            }
        })
    }

    /**
     * 将悬浮窗平滑移动至屏幕边缘
     */
    private fun snapToEdge(wm: WindowManager) {
        val view = floatingView ?: return
        val p = params ?: return

        // ✅ 检查视图是否已经有效测量
        if (view.width <= 0 || view.height <= 0) {
            Logger.w("FloatingWindowService", "snapToEdge: 视图尚未完成测量，跳过贴边")
            return
        }

        // ✅ 取消之前的动画（防止重复执行）
        edgeAnimator?.cancel()
        edgeAnimator = null

        try {
            val metrics = DisplayMetrics()
            wm.defaultDisplay.getMetrics(metrics)
            val screenWidth = metrics.widthPixels
            val viewWidth = view.width

        // 计算目标位置：左边(0)或右边(screenWidth - viewWidth)
        // 如果是收起状态，可以进一步实现“半收纳”效果，即只露出一半图标
        val targetX = if (p.x + viewWidth / 2 < screenWidth / 2) {
            // 左侧收纳
            if (!isExpanded) {
                // 收起状态：半收纳，限制最小值防止崩溃
                (-viewWidth / 2).coerceAtLeast(-dpToPx(28))
            } else {
                0
            }
        } else {
            // 右侧收纳
            if (!isExpanded) {
                // 收起状态：半收纳，限制最大值防止崩溃
                (screenWidth - viewWidth / 2).coerceAtMost(screenWidth - dpToPx(28))
            } else {
                (screenWidth - viewWidth).coerceAtLeast(0)
            }
        }

        // 使用动画平滑移动
            // ✅ 增加完整的生命周期管理和异常保护
            val animator = ValueAnimator.ofInt(p.x, targetX)
            animator.duration = 300
            animator.interpolator = DecelerateInterpolator()

            animator.addUpdateListener { animation ->
                try {
                    // ✅ 多重检查，确保安全更新布局
                    if (floatingView !== view || params !== p) {
                        // View 或 Params 已被替换，取消动画
                        animator.cancel()
                        return@addUpdateListener
                    }

                    // ✅ 检查 View 的父容器是否还存在（即是否仍附加在 WindowManager 上）
                    if (view.parent == null) {
                        animator.cancel()
                        return@addUpdateListener
                    }

                    p.x = animation.animatedValue as Int
                    wm.updateViewLayout(view, p)
                } catch (e: IllegalArgumentException) {
                    // ✅ View 未附加到窗口管理器
                    Logger.e("FloatingWindowService", "snapToEdge 动画更新失败: View not attached to window manager", e)
                    animator.cancel()
                } catch (e: IllegalStateException) {
                    // ✅ View 已被移除或状态异常
                    Logger.e("FloatingWindowService", "snapToEdge 动画更新失败: View removed or invalid state", e)
                    animator.cancel()
                } catch (e: Exception) {
                    // ✅ 其他未知异常
                    Logger.e("FloatingWindowService", "snapToEdge 动画更新异常", e)
                    animator.cancel()
                }
            }

            // ✅ 动画结束时清理引用，防止内存泄漏
            animator.addListener(object : android.animation.Animator.AnimatorListener {
                override fun onAnimationStart(animation: android.animation.Animator) {}
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    if (edgeAnimator === animator) {
                        edgeAnimator = null
                    }
                }
                override fun onAnimationCancel(animation: android.animation.Animator) {
                    if (edgeAnimator === animator) {
                        edgeAnimator = null
                    }
                }
                override fun onAnimationRepeat(animation: android.animation.Animator) {}
            })

            edgeAnimator = animator
            animator.start()
        } catch (e: Exception) {
            Logger.e("FloatingWindowService", "snapToEdge 执行失败", e)
        }
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
