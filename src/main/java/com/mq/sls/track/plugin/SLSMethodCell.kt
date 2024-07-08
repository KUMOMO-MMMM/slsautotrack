package com.mq.sls.track.plugin

import org.objectweb.asm.MethodVisitor

class SLSMethodCell(
    val name: String, val desc: String, val parent: String,
    /**
     * 方法所在的接口或类
     */
    val agentName: String,
    /**
     * 采集数据的方法描述
     */
    val agentDesc: String,
    /**
     * 采集数据的方法参数起始索引（ 0：this，1+：普通参数 ）
     */
    val paramsStart: Int,
    /**
     * 采集数据的方法参数个数
     */
    val paramsCount: Int,
    /**
     * 参数类型对应的ASM指令，加载不同类型的参数需要不同的指令
     */
    val opcodes: List<Int>
) {
    override fun equals(other: Any?): Boolean {
        if (other is SLSMethodCell) {
            return this.name == other.name && this.desc == other.desc && this.parent == other.parent
        }
        return false
    }

    /**
     * 插入对应的原方法
     */
    fun visitMethod(methodVisitor: MethodVisitor, opcode: Int, owner: String) {
        for (i in paramsStart until paramsStart + paramsCount step 1) {
            methodVisitor.visitVarInsn(opcodes[i - paramsStart], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, name, desc, false)
    }

    /**
     * 插入 Hook 的方法
     */
    fun visitHookMethod(methodVisitor: MethodVisitor, opcode: Int, owner: String) {
        for (i in paramsStart until paramsStart + paramsCount step 1) {
            methodVisitor.visitVarInsn(opcodes[i - paramsStart], i)
        }
        methodVisitor.visitMethodInsn(opcode, owner, agentName, agentDesc, false)
    }

    val nameDesc: String
        get() = name + desc

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + desc.hashCode()
        result = 31 * result + parent.hashCode()
        return result
    }

}