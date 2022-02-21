package com.example.testgraph

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.mikephil.charting.components.YAxis

import com.github.mikephil.charting.utils.ColorTemplate

import com.github.mikephil.charting.components.XAxis

import com.github.mikephil.charting.components.Legend

import com.github.mikephil.charting.components.Legend.LegendForm

import android.R
import android.R.attr
import android.graphics.Color
import android.graphics.Typeface
import android.view.View
import com.example.testgraph.databinding.ActivityMainBinding
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.Entry

import com.github.mikephil.charting.components.YAxis.AxisDependency

import com.github.mikephil.charting.data.LineDataSet
import android.R.attr.data

import android.R.attr.shape

import lecho.lib.hellocharts.listener.LineChartOnValueSelectListener;
import lecho.lib.hellocharts.listener.ViewportChangeListener
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.ValueShape;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.Chart;
import lecho.lib.hellocharts.view.LineChartView;






class MainActivity : AppCompatActivity(), ViewportChangeListener {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val chart: LineChartView? = null
    private var data: LineChartData? = null
    private val numberOfLines = 1
    private val maxNumberOfLines = 4
    private val numberOfPoints = 120

    var randomNumbersTab = Array(maxNumberOfLines) {
        FloatArray(
            numberOfPoints
        )
    }

    private val hasAxes = true
    private val hasAxesNames = true
    private val hasLines = true
    private val hasPoints = true
    private val shape = ValueShape.CIRCLE
    private val isFilled = false
    private val hasLabels = false
    private val isCubic = false
    private val hasLabelForSelected = false
    private val pointsHaveDifferentColor = false
    private val hasGradientToTransparent = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
       init()
        setData(200, 20F);

        // redraw
        binding.chart.invalidate();

        //generateValues()

        //generateData();

        // Disable viewport recalculations, see toggleCubic() method for more info.
        //binding.chart.setViewportCalculationEnabled(true);

        //resetViewport();
    }

    private fun resetViewport() {
        // Reset viewport height range to (0,100)
        /*val v = Viewport(binding.chart.maximumViewport)
        v.bottom = 0f
        v.top = 100f
        v.left = 0f
        v.right = (20 - 1).toFloat()
        binding.chart.maximumViewport = v
        binding.chart.currentViewport = v*/
    }

    private fun generateData() {
        /*val lines: MutableList<Line> = ArrayList<Line>()
        for (i in 0 until numberOfLines) {
            val values: MutableList<PointValue> = ArrayList()
            for (j in 0 until numberOfPoints) {
                values.add(PointValue(j.toFloat(), randomNumbersTab.get(i).get(j)))
            }
            val line = Line(values)
            line.setColor(ChartUtils.COLORS[i])
            line.setShape(shape)
            line.setCubic(isCubic)
            line.setFilled(isFilled)
            line.setHasLabels(hasLabels)
            line.setHasLabelsOnlyForSelected(hasLabelForSelected)
            line.setHasLines(hasLines)
            line.setHasPoints(hasPoints)
            //line.setHasGradientToTransparent(hasGradientToTransparent)
            if (pointsHaveDifferentColor) {
                line.setPointColor(ChartUtils.COLORS[(i + 1) % ChartUtils.COLORS.size])
            }
            lines.add(line)
        }
        data = LineChartData(lines)
        if (hasAxes) {
            val axisX = Axis()
            val axisY: Axis = Axis().setHasLines(true)
            if (hasAxesNames) {
                axisX.setName("Axis X")
                axisY.setName("Axis Y")
            }
            data?.axisXBottom = axisX
            data?.setAxisYLeft(axisY)
        } else {
            data?.setAxisXBottom(null)
            data?.setAxisYLeft(null)
        }
        data?.setBaseValue(Float.NEGATIVE_INFINITY)
        data?.isValueLabelBackgroundAuto = true
        binding.chart.setLineChartData(data)
        binding.chart.setZoomEnabled(true);
        binding.chart.setScrollEnabled(true)*/
    }

    private fun generateValues() {
        for (i in 0 until maxNumberOfLines) {
            for (j in 0 until numberOfPoints) {
                randomNumbersTab[i][j] = Math.random().toFloat() * 100f
            }
        }
    }

    private fun init() {
        title = "LineChartActivity2"

        /*tvX = findViewById<View>(R.id.tvXMax)
        tvY = findViewById<View>(R.id.tvYMax)

        seekBarX = findViewById<View>(R.id.seekBar1)
        seekBarX.setOnSeekBarChangeListener(this)

        seekBarY = findViewById<View>(R.id.seekBar2)
        seekBarY.setOnSeekBarChangeListener(this)*/


        with(binding) {
            //chart.setOnChartValueSelectedListener(this@MainActivity)

            // no description text

            // no description text
            chart.getDescription().setEnabled(false)

            // enable touch gestures

            // enable touch gestures
            chart.setTouchEnabled(true)

            chart.setDragDecelerationFrictionCoef(0.9f)

            // enable scaling and dragging

            // enable scaling and dragging
            chart.setDragEnabled(true)
            chart.setScaleEnabled(true)
            chart.setDrawGridBackground(false)
            chart.setHighlightPerDragEnabled(true)

            // if disabled, scaling can be done on x- and y-axis separately

            // if disabled, scaling can be done on x- and y-axis separately
            chart.setPinchZoom(true)

            // set an alternative background color

            // set an alternative background color
            chart.setBackgroundColor(Color.LTGRAY)

            // add data

            // add data
            //seekBarX.setProgress(20)
            //seekBarY.setProgress(30)

            chart.animateX(1500)

            // get the legend (only possible after setting data)

            // get the legend (only possible after setting data)
            val l: Legend = chart.getLegend()

            // modify the legend ...

            // modify the legend ...
            l.form = LegendForm.LINE
            l.typeface = Typeface.DEFAULT_BOLD
            l.textSize = 11f
            l.textColor = Color.WHITE
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.LEFT
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)
//        l.setYOffset(11f);

            //        l.setYOffset(11f);
            val xAxis: XAxis = chart.getXAxis()
            xAxis.typeface = Typeface.DEFAULT_BOLD
            xAxis.textSize = 11f
            xAxis.textColor = Color.WHITE
            xAxis.setDrawGridLines(false)
            xAxis.setDrawAxisLine(false)

            val leftAxis: YAxis = chart.getAxisLeft()
            leftAxis.typeface = Typeface.DEFAULT_BOLD
            leftAxis.textColor = ColorTemplate.getHoloBlue()
            leftAxis.axisMaximum = 200f
            leftAxis.axisMinimum = 0f
            leftAxis.setDrawGridLines(true)
            leftAxis.isGranularityEnabled = true

            val rightAxis: YAxis = chart.getAxisRight()
            rightAxis.typeface = Typeface.DEFAULT_BOLD
            rightAxis.textColor = Color.RED
            rightAxis.axisMaximum = 900f
            rightAxis.axisMinimum = -200f
            rightAxis.setDrawGridLines(true)
            rightAxis.setDrawZeroLine(true)

            var yAxis: YAxis
            // // Y-Axis Style // //
            yAxis = chart.getAxisLeft()

            // disable dual axis (only use LEFT axis)
            chart.getAxisRight().setEnabled(true)

            // horizontal grid lines
            yAxis.enableGridDashedLine(10f, 10f, 0f)

            // axis range
            yAxis.axisMaximum = 200f
            yAxis.axisMinimum = -50f
        }
    }


    private fun setData(count: Int, range: Float) {
        val values1: ArrayList<Entry> = ArrayList()
        for (i in 0 until count) {
            val value = (Math.random() * (range / 2f)).toFloat() + 50
            values1.add(Entry(i.toFloat(), value))
        }
        val values2: ArrayList<Entry> = ArrayList()
        for (i in 0 until count) {
            val value = (Math.random() * range).toFloat() + 450
            values2.add(Entry(i.toFloat(), value))
        }
        val values3: ArrayList<Entry> = ArrayList()
        for (i in 0 until count) {
            val value = (Math.random() * range).toFloat() + 500
            values3.add(Entry(i.toFloat(), value))
        }
        val set1: LineDataSet
        val set2: LineDataSet
        val set3: LineDataSet
        with(binding) {
            if (chart.getData() != null &&
                chart.getData().getDataSetCount() > 0
            ) {
                set1 = chart.getData().getDataSetByIndex(0) as LineDataSet
                set2 = chart.getData().getDataSetByIndex(1) as LineDataSet
                set3 = chart.getData().getDataSetByIndex(2) as LineDataSet
                set1.setValues(values1)
                set2.setValues(values2)
                set3.setValues(values3)
                chart.getData().notifyDataChanged()
                chart.notifyDataSetChanged()
            } else {
                // create a dataset and give it a type
                set1 = LineDataSet(values1, "DataSet 1")
                set1.axisDependency = AxisDependency.LEFT
                set1.color = ColorTemplate.getHoloBlue()
                set1.setCircleColor(Color.WHITE)
                set1.lineWidth = 2f
                set1.circleRadius = 3f
                set1.fillAlpha = 65
                set1.fillColor = ColorTemplate.getHoloBlue()
                set1.highLightColor = Color.rgb(244, 117, 117)
                set1.setDrawCircleHole(false)
                //set1.setFillFormatter(new MyFillFormatter(0f));
                //set1.setDrawHorizontalHighlightIndicator(false);
                //set1.setVisible(false);
                //set1.setCircleHoleColor(Color.WHITE);

                // create a dataset and give it a type
                set2 = LineDataSet(values2, "DataSet 2")
                set2.axisDependency = AxisDependency.RIGHT
                set2.color = Color.RED
                set2.setCircleColor(Color.WHITE)
                set2.lineWidth = 2f
                set2.circleRadius = 3f
                set2.fillAlpha = 65
                set2.fillColor = Color.RED
                set2.setDrawCircleHole(false)
                set2.highLightColor = Color.rgb(244, 117, 117)
                //set2.setFillFormatter(new MyFillFormatter(900f));
                set3 = LineDataSet(values3, "DataSet 3")
                set3.axisDependency = AxisDependency.RIGHT
                set3.color = Color.YELLOW
                set3.setCircleColor(Color.WHITE)
                set3.lineWidth = 2f
                set3.circleRadius = 3f
                set3.fillAlpha = 65
                set3.fillColor = ColorTemplate.colorWithAlpha(Color.YELLOW, 200)
                set3.setDrawCircleHole(false)
                set3.highLightColor = Color.rgb(244, 117, 117)

                // create a data object with the data sets
                val data = LineData(set1, set2, set3)
                data.setValueTextColor(Color.WHITE)
                data.setValueTextSize(9f)

                // set data
                chart.setData(data)
                chart.setVisibleXRangeMaximum(20F)

            }
        }
    }

    override fun onViewportChanged(viewport: Viewport?) {
        //binding.chart.setCurrentViewport(viewport);
    }
}