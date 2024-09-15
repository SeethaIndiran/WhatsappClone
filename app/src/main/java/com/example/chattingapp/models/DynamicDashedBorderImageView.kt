package com.example.chattingapp.models

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView

class DynamicDashedBorderImageView(context: Context, attrs: AttributeSet,list:Int) : AppCompatImageView(context, attrs,list) {

    private val paint: Paint = Paint()

    init {
        // Set up paint for the dashed stroke
        paint.strokeWidth = 5f // Customize the stroke width as needed
        paint.color = Color.BLACK // Customize the stroke color as needed
        paint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)






    }


}
