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
        minSdk = 23
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

    // 添加 splits 配置，按 ABI 拆分 APK
    splits {
        abi {
            // 启用 ABI 拆分
            isEnable = true
            // 重置当前支持的 ABI 列表（如果不调用 reset()，include 会追加到默认列表）
            reset()
            // 指定需要拆分的 ABI 类型，可根据项目实际支持的 ABI 调整
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            // 生成一个包含所有 ABI 的通用 APK（用于不支持拆分的场景）
            isUniversalApk = true
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
    // 核心依赖（已使用 catalog，保持不变）
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM 平台依赖（已使用 catalog）
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // ⚠️ 替换硬编码的 Material 依赖
    implementation(libs.androidx.compose.material)    // 对应 "androidx.compose.material:material:1.7.0"

    // 导航组件（已使用 catalog）
    implementation(libs.androidx.navigation.compose)

    // Kotlin Serialization（已使用 catalog）
    implementation(libs.kotlinx.serialization.json)

    // ⚠️ 替换所有 Ktor 硬编码依赖
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Coil（已使用 catalog）
    implementation(libs.coil.compose)

    // Gson（已使用 catalog）
    implementation(libs.gson)

    // Room 数据库已移除，使用 SharedPreferences 替代

    // ⚠️ 替换友盟硬编码依赖
    implementation(libs.umeng.common)
    implementation(libs.umeng.asms)

    // 测试依赖（已使用 catalog）
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
