plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // 添加 Kotlin Serialization 插件，用于类型安全的导航
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.silas.omaster"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.silas.omaster"
        minSdk = 34
        targetSdk = 36
        // 版本号规范：
        // versionCode: 内部版本号，每次发布必须递增
        // versionName: 对外显示版本号，格式 主.次.修订
        // 正式版: 1.0, 1.0.1, 1.1.0, 2.0.0
        // 测试版: 1.0.0-beta1, 1.0.0-beta2
        versionCode = 8
        versionName = "1.2.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 核心依赖
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM 平台依赖
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    // Material (for pull-to-refresh support)
    implementation("androidx.compose.material:material:1.4.3")

    // 导航组件 - 类型安全导航
    implementation(libs.androidx.navigation.compose)

    // Kotlin Serialization - 用于导航参数序列化
    implementation(libs.kotlinx.serialization.json)

    // Ktor HTTP client for fetching remote presets
    implementation("io.ktor:ktor-client-core:2.3.0")
    implementation("io.ktor:ktor-client-cio:2.3.0")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.0")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.0")
    // Coil - 图片加载库，用于加载本地 assets 图片
    implementation(libs.coil.compose)

    // Gson - JSON 解析库
    implementation(libs.gson)

    // Room 数据库已移除，使用 SharedPreferences 替代

    // 友盟统计 SDK
    implementation("com.umeng.umsdk:common:9.4.7") // 必选
    implementation("com.umeng.umsdk:asms:1.4.0") // 必选

    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
