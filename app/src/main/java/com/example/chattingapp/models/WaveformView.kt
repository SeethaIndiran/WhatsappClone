package com.example.chattingapp.models

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class WaveformView(context:Context,attrs:AttributeSet?): View(context,attrs) {

    private  var paint = Paint()
    private var amplitudes = ArrayList<Float>()
    private var spikes = ArrayList<RectF>()

    private var radius = 6f
    private var width = 9f
    private var d= 6f

    private var sw = 0f
    private var sh = 100f

    private var maxSpike = 0

    init{
        paint.color = Color.rgb(244,81,30)

        sw = resources.displayMetrics.widthPixels.toFloat()

        maxSpike = (sw / (width+d)).toInt()
    }

    fun addAmplitudes(amp:Float){
        val norm = Math.min(amp.toInt()/7,70).toFloat()
        amplitudes.add(norm)
        spikes.clear()

        val amps = amplitudes.takeLast(maxSpike)
        for(i in amps.indices){
            val left = sw-i*(width+d)
            val top = sh/2 - amps[i]/2
            val right = left + width
            val bottom = top + amps[i]
            spikes.add(RectF(left, top, right, bottom))
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
       spikes.forEach {
           canvas?.drawRoundRect(it,radius,radius,paint)
       }
    }
}