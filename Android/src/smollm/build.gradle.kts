plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val localEnableAdrenoOpenCl =
    providers.gradleProperty("localEnableAdrenoOpenCl").orNull?.toBooleanStrictOrNull() == true
val localAbiOnlyArm64 =
    providers.gradleProperty("localAbiOnlyArm64").orNull?.toBooleanStrictOrNull() == true
val openClIncludeDir = file("src/main/cpp/third_party/opencl/include").absolutePath
val openClLibDir = file("src/main/cpp/third_party/opencl/lib/arm64-v8a").absolutePath
val openClLibPath = file("src/main/cpp/third_party/opencl/lib/arm64-v8a/libOpenCL.so").absolutePath

android {
    namespace = "com.google.ai.edge.gallery.smollm"
    compileSdk = 36
    ndkVersion = "27.2.12479018"

    defaultConfig {
        minSdk = 35
        ndk {
            abiFilters +=
                if (localAbiOnlyArm64) {
                    listOf("arm64-v8a")
                } else {
                    listOf("arm64-v8a", "armeabi-v7a")
                }
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        externalNativeBuild {
            cmake {
                cppFlags += listOf()
                arguments += listOf("-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON")
                arguments += "-DCMAKE_BUILD_TYPE=Release"
                arguments += "-DBUILD_SHARED_LIBS=ON"
                arguments += "-DLLAMA_BUILD_COMMON=ON"
                arguments += "-DLLAMA_CURL=OFF"
                arguments += "-DGGML_LLAMAFILE=OFF"
                arguments += "-DLLAMA_OPENSSL=OFF"
                arguments += "-DLLAMA_BUILD_UI=OFF"
                arguments += "-DGGML_SCHED_NO_REALLOC=ON"
                if (localEnableAdrenoOpenCl) {
                    arguments += "-DGGML_OPENCL=ON"
                    arguments += "-DGGML_OPENCL_USE_ADRENO_KERNELS=ON"
                    arguments += "-DGGML_OPENCL_ADRENO_XMEM_GEMM=ON"
                    arguments += "-DOpenCL_INCLUDE_DIR=$openClIncludeDir"
                    arguments += "-DOpenCL_LIBRARY=$openClLibPath"
                    arguments += "-DOpenCL_LIBRARIES=$openClLibPath"
                    arguments += "-DOpenCL_LIBRARY_DIR=$openClLibDir"
                }
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
}
