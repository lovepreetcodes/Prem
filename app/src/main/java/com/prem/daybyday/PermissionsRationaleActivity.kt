package com.prem.daybyday

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity
import android.view.ViewGroup
import android.graphics.Color
import android.graphics.Typeface
import android.util.TypedValue

/** Explains Prem's Health Connect data use when the user opens the permission rationale. */
class PermissionsRationaleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val content = TextView(this).apply {
            text = "Prem reads your step-count data from Health Connect so it can display your daily steps and activity history. Prem does not need access to other health categories for this feature. Step data is used to show your activity inside Prem. You can revoke this access at any time in Health Connect settings."
            setTextColor(Color.WHITE)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 17f)
            typeface = Typeface.DEFAULT
            gravity = Gravity.CENTER_VERTICAL
            setPadding(48, 48, 48, 48)
            setBackgroundColor(Color.rgb(18, 18, 18))
        }
        setContentView(content, ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        ))
    }
}
