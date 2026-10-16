package com.example.pocketrecipes.ui

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import com.example.pocketrecipes.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

fun Context.dp(value: Int) = (value * resources.displayMetrics.density).toInt()
fun Context.color(id: Int) = ContextCompat.getColor(this, id)
fun View.roundBackground(color: Int, radius: Int = 16) {
    background = GradientDrawable().apply { setColor(color); cornerRadius = context.dp(radius).toFloat() }
}
fun LinearLayout.space(size: Int = 16) { addView(View(context), LinearLayout.LayoutParams(1, context.dp(size))) }
fun LinearLayout.label(value: String, size: Int = 16, strong: Boolean = false, color: Int = R.color.pr_text): TextView =
    TextView(context).also {
        it.text = value; it.textSize = size.toFloat(); it.setTextColor(context.color(color))
        it.setLineSpacing(context.dp(3).toFloat(), 1f)
        if (strong) it.setTypeface(null, Typeface.BOLD)
        addView(it, LinearLayout.LayoutParams(-1, -2))
    }
fun LinearLayout.heading(text: String) { space(); label(text, 18, true); space(12) }
fun LinearLayout.button(text: String, secondary: Boolean = false, onClick: () -> Unit): MaterialButton =
    MaterialButton(context).also { b ->
        b.text = text; b.isAllCaps = false; b.textSize = 14f; b.letterSpacing = 0f; b.cornerRadius = context.dp(16)
        b.minHeight = context.dp(48); b.insetTop = 0; b.insetBottom = 0
        b.setPadding(context.dp(16), context.dp(12), context.dp(16), context.dp(12))
        b.backgroundTintList = ColorStateList.valueOf(context.color(if (secondary) R.color.pr_soft else R.color.pr_primary))
        b.setTextColor(context.color(if (secondary) R.color.pr_strong else R.color.pr_text))
        b.setOnClickListener { onClick() }
        addView(b, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = context.dp(12) })
    }
fun LinearLayout.field(title: String, value: String = "", numeric: Boolean = false, multiline: Boolean = false): TextInputLayout {
    val box = TextInputLayout(context).apply {
        hint = title; boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
        setBoxCornerRadii(context.dp(12).toFloat(), context.dp(12).toFloat(), context.dp(12).toFloat(), context.dp(12).toFloat())
        boxBackgroundColor = context.color(R.color.pr_surface); boxStrokeColor = context.color(R.color.pr_strong)
    }
    val input = TextInputEditText(box.context).apply {
        setText(value); textSize = 16f; setTextColor(context.color(R.color.pr_text))
        setPadding(context.dp(16), context.dp(16), context.dp(16), context.dp(16))
        inputType = if (numeric) InputType.TYPE_CLASS_NUMBER else InputType.TYPE_CLASS_TEXT or
            (if (multiline) InputType.TYPE_TEXT_FLAG_MULTI_LINE or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            else InputType.TYPE_TEXT_FLAG_CAP_SENTENCES)
        if (multiline) { minLines = 2; gravity = Gravity.TOP } else setSingleLine(true)
    }
    box.addView(input, LinearLayout.LayoutParams(-1, -2))
    addView(box, LinearLayout.LayoutParams(-1, -2).apply { topMargin = context.dp(4); bottomMargin = context.dp(12) })
    return box
}
fun LinearLayout.menuRow(title: String, subtitle: String? = null, action: () -> Unit): View {
    val row = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
        minimumHeight = context.dp(64); setPadding(context.dp(12), context.dp(12), context.dp(12), context.dp(12))
        roundBackground(context.color(R.color.pr_surface))
    }
    val labels = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL }
    row.addView(labels, LinearLayout.LayoutParams(0, -2, 1f))
    labels.label(title)
    if (subtitle != null) { labels.space(4); labels.label(subtitle, 12, color = R.color.pr_muted) }
    row.addView(ImageView(context).apply {
        setImageResource(R.drawable.ic_chevron); imageTintList = ColorStateList.valueOf(context.color(R.color.pr_strong))
    }, LinearLayout.LayoutParams(context.dp(24), context.dp(24)))
    row.isClickable = true; row.isFocusable = true; row.contentDescription = listOfNotNull(title, subtitle).joinToString(", ")
    row.setOnClickListener { action() }
    addView(row, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = context.dp(12) })
    return row
}
fun LinearLayout.empty(title: String, description: String, icon: Int) {
    val box = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
        setPadding(context.dp(24), context.dp(48), context.dp(24), context.dp(48))
        roundBackground(context.color(R.color.pr_soft), 24)
    }
    box.addView(ImageView(context).apply {
        setImageResource(icon); imageTintList = ColorStateList.valueOf(context.color(R.color.pr_strong))
    }, LinearLayout.LayoutParams(context.dp(48), context.dp(48)))
    box.space(24); box.label(title, 24, true, R.color.pr_strong).gravity = Gravity.CENTER
    box.space(16); box.label(description, color = R.color.pr_muted).gravity = Gravity.CENTER
    addView(box, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = context.dp(16) })
}
fun Context.confirm(title: String, positive: String = "Удалить", action: () -> Unit) {
    MaterialAlertDialogBuilder(this).setTitle(title).setMessage("Это действие невозможно отменить.")
        .setNegativeButton("Отмена", null).setPositiveButton(positive) { _, _ -> action() }.show()
}

fun com.google.android.material.chip.Chip.pocketStyle() {
    val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
    chipBackgroundColor = ColorStateList(states, intArrayOf(context.color(R.color.pr_strong), context.color(R.color.pr_soft)))
    setTextColor(ColorStateList(states, intArrayOf(context.color(R.color.pr_background), context.color(R.color.pr_strong))))
    chipStrokeWidth = 0f; chipCornerRadius = context.dp(24).toFloat()
    chipMinHeight = context.dp(43).toFloat(); textSize = 14f; isCheckedIconVisible = false
    setEnsureMinTouchTargetSize(true)
}
