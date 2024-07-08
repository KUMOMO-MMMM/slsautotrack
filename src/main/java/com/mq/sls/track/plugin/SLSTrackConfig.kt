package com.mq.sls.track.plugin

import org.objectweb.asm.Opcodes

object SLSTrackConfig {
    const val SLS_TRACK_API = "com/sls/tracker/SLSTracker"
    val nameDescMap = mutableMapOf<String, SLSMethodCell>()
    val lambdaMethods = mutableMapOf<String, SLSMethodCell>()

    init {
        SLSMethodCell(
            name = "onClick",
            desc = "(Landroid/view/View;)V",
            parent = "Landroid/view/View\$OnClickListener;",
            agentName = "trackViewOnClick",
            agentDesc = "(Landroid/view/View;)V",
            paramsStart = 1, paramsCount = 1,
            listOf(Opcodes.ALOAD)
        ).addLambdaMethod()
            .addNameDescMap()

        SLSMethodCell(
            name = "onNavigationItemSelected",
            desc = "(Landroid/view/MenuItem;)Z",
            parent = "Lcom/google/android/material/navigation/NavigationBarView\$OnItemSelectedListener;",
            agentName = "trackNaviItem",
            agentDesc = "(Landroid/view/MenuItem;)V",
            paramsStart = 1, paramsCount = 1,
            listOf(Opcodes.ALOAD)
        ).addLambdaMethod()
            .addNameDescMap()
    }

    private fun SLSMethodCell.addLambdaMethod(): SLSMethodCell {
        lambdaMethods[this.parent + this.name + this.desc] = this
        return this
    }

    private fun SLSMethodCell.addNameDescMap(): SLSMethodCell {
        nameDescMap[this.name + this.desc] = this
        return this
    }
}