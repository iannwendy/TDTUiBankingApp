// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.squareup:javapoet:1.13.0")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// Task để kiểm tra xem Stripe backend đã chạy chưa
tasks.register("checkStripeBackend") {
    group = "application"
    description = "Kiểm tra xem Stripe backend server đã chạy chưa"
    
    doLast {
        val process = ProcessBuilder("bash", "-c", "lsof -ti:4242 || echo ''")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        
        if (output.isNotEmpty()) {
            println("✅ Stripe backend server đã chạy (PID: $output)")
        } else {
            println("ℹ️  Stripe backend server chưa chạy")
        }
    }
}

// Task để tự động chạy Stripe backend server
tasks.register<Exec>("startStripeBackend") {
    group = "application"
    description = "Khởi động Stripe backend server"
    
    val rootDir = project.rootDir
    val scriptPath = rootDir.resolve("run_stripe_backend.sh")
    
    // Kiểm tra xem script có tồn tại không
    if (!scriptPath.exists()) {
        throw GradleException("Không tìm thấy script: ${scriptPath.absolutePath}")
    }
    
    // Đảm bảo script có quyền thực thi
    scriptPath.setExecutable(true)
    
    // Kiểm tra xem server đã chạy chưa
    doFirst {
        val process = ProcessBuilder("bash", "-c", "lsof -ti:4242 || echo ''")
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .start()
        val output = process.inputStream.bufferedReader().readText().trim()
        
        if (output.isNotEmpty()) {
            println("ℹ️  Stripe backend server đã chạy (PID: $output), bỏ qua khởi động")
            throw StopExecutionException("Server đã chạy")
        }
    }
    
    // Chạy script trong background
    commandLine("bash", "-c", "nohup ${scriptPath.absolutePath} > /tmp/stripe_backend.log 2>&1 &")
    
    doLast {
        println("✅ Stripe backend server đang khởi động...")
        println("📝 Logs được lưu tại: /tmp/stripe_backend.log")
        println("🌐 Server sẽ chạy tại: http://localhost:4242")
        println("⏳ Đợi 3 giây để server khởi động...")
        Thread.sleep(3000)
    }
}

// Task để dừng Stripe backend server
tasks.register<Exec>("stopStripeBackend") {
    group = "application"
    description = "Dừng Stripe backend server"
    
    commandLine("bash", "-c", "pkill -f 'node.*server.js' || pkill -f 'stripe_backend' || true")
    
    doLast {
        println("✅ Đã dừng Stripe backend server")
    }
}

// Hook vào preDebugBuild được cấu hình trong app/build.gradle.kts