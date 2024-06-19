/*
 * Copyright 2023 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.yoga.ViewModel

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.yoga.R
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import kotlin.math.min
import kotlin.math.max

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: MutableList<MutableList<Float>>? = null
    private var pointPaint = Paint()
    private var linePaint = Paint()
    private var arrowPaint = Paint()
    private var arrowPoints: List<Float> = emptyList()

    //private var scaleFactor: Float = 1f
    //private var scaleFactorX:Float = 1f
    //private var scaleFactorY:Float = 1f
    private var imageWidth: Int = 1
    private var imageHeight: Int = 1

    init {
        initPaints()
    }

    fun clear() {
        results = null
        pointPaint.reset()
        linePaint.reset()
        arrowPaint.reset()
        invalidate()
        initPaints()
    }

    private fun initPaints() {
        linePaint.color =
            ContextCompat.getColor(context!!, R.color.mp_color_primary)
        linePaint.strokeWidth = LANDMARK_STROKE_WIDTH
        linePaint.style = Paint.Style.STROKE

        pointPaint.color = Color.YELLOW
        pointPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        pointPaint.style = Paint.Style.FILL

        arrowPaint.color = Color.BLUE // 默认箭头颜色
        arrowPaint.strokeWidth = LANDMARK_STROKE_WIDTH
        arrowPaint.style = Paint.Style.STROKE
    }

    private fun drawArrow(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float) {
        val arrowHeadLength = 30f  // 箭头头部的长度
        val arrowHeadAngle = Math.toRadians(45.0)  // 箭头头部的角度

        // 画箭身
        canvas.drawLine(startX, startY, endX, endY, arrowPaint)

        // 算出箭头头部的两个点
        val angle = Math.atan2((endY - startY).toDouble(), (endX - startX).toDouble())
        val x1 = endX - arrowHeadLength * Math.cos(angle - arrowHeadAngle).toFloat()
        val y1 = endY - arrowHeadLength * Math.sin(angle - arrowHeadAngle).toFloat()
        val x2 = endX - arrowHeadLength * Math.cos(angle + arrowHeadAngle).toFloat()
        val y2 = endY - arrowHeadLength * Math.sin(angle + arrowHeadAngle).toFloat()

        // 画箭头头部的两条线
        canvas.drawLine(endX, endY, x1, y1, arrowPaint)
        canvas.drawLine(endX, endY, x2, y2, arrowPaint)
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        results?.let { poseLandmarkerResult ->

            //val imageAspectRatio = imageWidth.toFloat() / imageHeight.toFloat()
            //val canvasAspectRatio = width.toFloat() / height.toFloat()

            val offsetX: Float
            val offsetY: Float

            val scaleFactor: Float = max(width.toFloat() / imageWidth.toFloat(), height.toFloat() / imageHeight.toFloat())
            offsetX = (width - imageWidth * scaleFactor) / 2
            offsetY = (height - imageHeight * scaleFactor) / 2
            for (normalizedLandmark in poseLandmarkerResult) {
                if (normalizedLandmark[3] > 0.7) {
                    canvas.drawPoint(
                            normalizedLandmark[0] * imageWidth * scaleFactor + offsetX,
                            normalizedLandmark[1] * imageHeight * scaleFactor + offsetY,
                            pointPaint
                    )
                }

                PoseLandmarker.POSE_LANDMARKS.forEach {
                    if (poseLandmarkerResult[it.start()][3] > 0.7 && poseLandmarkerResult[it.end()][3] > 0.7) {
                        canvas.drawLine(
                                poseLandmarkerResult[it.start()][0] * imageWidth * scaleFactor + offsetX,
                                poseLandmarkerResult[it.start()][1] * imageHeight * scaleFactor + offsetY,
                                poseLandmarkerResult[it.end()][0] * imageWidth * scaleFactor + offsetX,
                                poseLandmarkerResult[it.end()][1] * imageHeight * scaleFactor + offsetY,
                                linePaint
                        )
                    }
                }
            }
            /*
            for(normalizedLandmark in poseLandmarkerResult) {
                if (normalizedLandmark[3] > 0.7)
                {
                    canvas.drawPoint(
                            //normalizedLandmark.x() * imageWidth * scaleFactor,
                            //normalizedLandmark.y() * imageHeight * scaleFactor,
                            normalizedLandmark[0] * imageWidth * scaleFactorX,
                            normalizedLandmark[1] * imageHeight * scaleFactorY,
                            pointPaint
                    )
                }


                PoseLandmarker.POSE_LANDMARKS.forEach {
                    if (poseLandmarkerResult[it!!.start()][3] >0.7 && poseLandmarkerResult[it.end()][3] >0.7)
                    {
                        canvas.drawLine(
                                poseLandmarkerResult[it!!.start()][0] * imageWidth * scaleFactorX,
                                poseLandmarkerResult[it.start()][1] * imageHeight * scaleFactorY,
                                poseLandmarkerResult[it.end()][0] * imageWidth * scaleFactorX,
                                poseLandmarkerResult[it.end()][1] * imageHeight * scaleFactorY,
                                linePaint
                        )
                    }
                }
            }*/
        }
        // 绘制箭头
        if (arrowPoints.size == 4) {
            drawArrow(canvas, arrowPoints[0], arrowPoints[1], arrowPoints[2], arrowPoints[3])
        }
    }

    fun setResults(
        poseLandmarkerResults: MutableList<MutableList<Float>>,
        imageWidth: Int,
        imageHeight: Int,
        runningMode: RunningMode = RunningMode.IMAGE,
        arrowPoints: List<Float>,
    ) {
        results = poseLandmarkerResults

        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        //scaleFactor = when (runningMode) {
        //    RunningMode.IMAGE,
        //    RunningMode.VIDEO -> {
        //        min(width * 1f / imageWidth, height * 1f / imageHeight)
        //    }
        //    RunningMode.LIVE_STREAM -> {
        //        // PreviewView is in FILL_START mode. So we need to scale up the
        //        // landmarks to match with the size that the captured images will be
        //        // displayed.
        //        max(width * 1f / imageWidth, height * 1f / imageHeight)
        //    }
        //}
        //println("W : "+(width * 1f / imageWidth).toString())//W : 1.70625//W : 1.4109375
        //
        //println("H : "+(height * 1f / imageHeight).toString())//H : 1.3770833//H : 1.3645834

        //scaleFactorX = width * 1f / imageWidth
        //scaleFactorY = height * 1f / imageHeight
        //println(width)
        //println(height)
        this.arrowPoints = arrowPoints

        invalidate()
    }

    companion object {
        private const val LANDMARK_STROKE_WIDTH = 12F
    }
}