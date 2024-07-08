import org.jetbrains.kotlin.resolve.compatibility

plugins {
    // 使用 kotlin dsl 作为 gradle 构建脚本语言
    `kotlin-dsl`
    `maven-publish`
}

kotlin {
    compilerOptions {
        jvmToolchain(11)
    }
}

dependencies {
    val agpVersion = "8.2.2"
    val kotlinVersion = "1.7.10"
    val asmVersion = "9.3"
    // AGP 依赖
    implementation("com.android.tools.build:gradle:$agpVersion") {
        exclude(group = "org.ow2.asm")
    }
    // Kotlin 依赖 —— 插件使用 Kotlin 实现
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion") {
        exclude(group = "org.ow2.asm")
    }
    // ASM 依赖库
    implementation("org.ow2.asm:asm:$asmVersion")
    implementation("org.ow2.asm:asm-commons:$asmVersion")
    implementation("org.ow2.asm:asm-util:$asmVersion")
}

gradlePlugin {
    plugins {
        register("autoTracker") {
            id = "com.mq.sls.auto.track"
            group = "com.mq.sls.auto.track"
            version = "0.0.1"
            implementationClass = "com.mq.sls.track.AutoTrackPlugin"
        }
    }
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin")
    }
}

//publishing {
//    repositories {
//        mavenLocal()
//    }
//    publications {
//        create<MavenPublication>("release") {
////            groupId = "com.mq.sls.auto.track"
////            artifactId = "track"
//        }
//    }
//}