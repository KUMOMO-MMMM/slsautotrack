package com.sls.track.plugin

import com.android.build.api.instrumentation.ClassData
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.Type
import org.objectweb.asm.commons.AdviceAdapter

class AutoTrackClickMethodVisitor(
    val classData: ClassData,
    private val originMv: MethodVisitor,
    access: Int,
    name: String?,
    val descriptor: String?
) : AdviceAdapter(Opcodes.ASM9, originMv, access, name, descriptor) {

    private var nameDesc = name + methodDesc

    override fun visitInvokeDynamicInsn(
        name: String?,
        descriptor: String?,
        bootstrapMethodHandle: Handle?,
        vararg bootstrapMethodArguments: Any?
    ) {
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, *bootstrapMethodArguments)
        try {
            val owner = bootstrapMethodHandle!!.owner
            if ("java/lang/invoke/LambdaMetafactory" != owner) {
                return
            }
            val simpleDesc = (bootstrapMethodArguments[0] as Type).descriptor
            val key = Type.getReturnType(descriptor).descriptor + name + simpleDesc
            val methodCell = SLSTrackConfig.lambdaMethods[key]
            if (methodCell != null) {
                val handle = bootstrapMethodArguments.getOrNull(1) as Handle?
                if (handle != null) {
                    SLSTrackConfig.lambdaMethods[handle.name + handle.desc] = methodCell
                    println("method cell: ${handle.name + handle.desc}")
                }
            }
        } catch (e: Exception) {
            println("invokeDynamic exception: $e")
        }
    }

    override fun onMethodEnter() {
        super.onMethodEnter()
        val slsMethodCell = SLSTrackConfig.nameDescMap[nameDesc]
        if (slsMethodCell != null) {
            // 这里需要考虑更多参数的场景
            originMv.visitVarInsn(Opcodes.ALOAD, 1)
            originMv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                SLSTrackConfig.SLS_TRACK_API,
                slsMethodCell.agentName,
                slsMethodCell.agentDesc,
                false
            )
        }
        checkLambdaMethod()
    }

    private fun checkLambdaMethod() {
        val lambdaCell = SLSTrackConfig.lambdaMethods[nameDesc]
        if (lambdaCell != null) {
            val types = Type.getArgumentTypes(lambdaCell.desc)
            val length = types.size
            val lambdaTypes = Type.getArgumentTypes(descriptor)
            val paramStart = lambdaTypes.size - length
            if (paramStart < 0) {
                return
            }
            for (i in 0 until length) {
                if (lambdaTypes[paramStart + i].descriptor != types[i].descriptor) {
                    return
                }
            }
            val isStaticMethod = access.isStatic()
            for (i in paramStart until paramStart + lambdaCell.paramsCount) {
                originMv.visitVarInsn(
                    lambdaCell.opcodes[i - paramStart],
                    getVisitPosition(lambdaTypes, i, isStaticMethod)
                )
            }
            originMv.visitMethodInsn(
                INVOKESTATIC,
                SLSTrackConfig.SLS_TRACK_API,
                lambdaCell.agentName,
                lambdaCell.agentDesc,
                false
            )
        }
    }

    /**
     * 获取方法参数下标为 index 的对应 ASM index
     *
     * @param types          方法参数类型数组
     * @param index          方法中参数下标，从 0 开始
     * @param isStaticMethod 该方法是否为静态方法
     * @return 访问该方法的 index 位参数的 ASM index
     */
    private fun getVisitPosition(types: Array<Type>?, index: Int, isStaticMethod: Boolean): Int {
        if (types == null || index < 0 || index >= types.size) {
            throw Error("getVisitPosition error")
        }
        return if (index == 0) {
            if (isStaticMethod) 0 else 1
        } else {
            getVisitPosition(types, index - 1, isStaticMethod) + types[index - 1].size
        }
    }
}

fun Int.isStatic(): Boolean {
    return (this and Opcodes.ACC_STATIC) != 0;
}

fun Int.convertOpcodes(): Int {
    /**
     * 获取 LOAD 或 STORE 的相反指令，例如 ILOAD => ISTORE，ASTORE => ALOAD
     *
     * @param LOAD 或 STORE 指令
     * @return 返回相对应的指令
     */
    var result = this
    when (this) {
        Opcodes.ILOAD -> result = Opcodes.ISTORE
        Opcodes.ALOAD -> result = Opcodes.ASTORE
        Opcodes.LLOAD ->
            result = Opcodes.LSTORE

        Opcodes.FLOAD ->
            result = Opcodes.FSTORE

        Opcodes.DLOAD ->
            result = Opcodes.DSTORE

        Opcodes.ISTORE ->
            result = Opcodes.ILOAD

        Opcodes.ASTORE ->
            result = Opcodes.ALOAD

        Opcodes.LSTORE ->
            result = Opcodes.LLOAD

        Opcodes.FSTORE ->
            result = Opcodes.FLOAD

        Opcodes.DSTORE ->
            result = Opcodes.DLOAD
    }
    return result
}