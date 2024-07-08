package com.mq.sls.track

import com.android.build.api.instrumentation.AsmClassVisitorFactory
import com.android.build.api.instrumentation.ClassContext
import com.android.build.api.instrumentation.ClassData
import com.android.build.api.instrumentation.InstrumentationParameters
import com.android.build.api.instrumentation.InstrumentationScope
import com.android.build.api.variant.AndroidComponentsExtension
import com.mq.sls.track.plugin.AutoTrackClickMethodVisitor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class AutoTrackPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("====== auto track =======")
        val androidComponents = target.extensions.getByType(AndroidComponentsExtension::class.java)
        androidComponents.onVariants { variant ->
            variant.instrumentation.transformClassesWith(ClickAsmClassVisitorFactory::class.java, InstrumentationScope.ALL) {

            }
        }
    }
}

abstract class ClickAsmClassVisitorFactory : AsmClassVisitorFactory<ClickConfigParams> {

    override fun createClassVisitor(classContext: ClassContext, nextClassVisitor: ClassVisitor): ClassVisitor {
        return object : ClassVisitor(Opcodes.ASM9, nextClassVisitor) {

            override fun visitMethod(
                access: Int,
                name: String?,
                descriptor: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                val mv = super.visitMethod(access, name, descriptor, signature, exceptions)
                return AutoTrackClickMethodVisitor(
                    classContext.currentClassData,
                    mv,
                    access,
                    name,
                    descriptor
                )
            }
        }
    }

    override fun isInstrumentable(classData: ClassData): Boolean {
        val className = classData.className
        if (className.startsWith("androidx") ||
            className.startsWith("com.google.android.material")
        ) {
            return false
        }
        return true
    }
}

interface ClickConfigParams : InstrumentationParameters {
//    @get:Input
//    val useInclude: Property<Boolean>
}