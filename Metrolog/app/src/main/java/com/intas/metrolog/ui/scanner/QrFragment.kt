package com.intas.metrolog.ui.scanner

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import com.google.zxing.Result
import com.intas.metrolog.R
import me.dm7.barcodescanner.zxing.ZXingScannerView

class QrFragment : Fragment(), ZXingScannerView.ResultHandler {
    private lateinit var onResultListener: OnResultListener
    private var flash = false
    private var flashItem: MenuItem? = null

    private val mScannerView by lazy {
        ZXingScannerView(requireActivity())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnResultListener) {
            onResultListener = context
        } else {
            throw RuntimeException("Activity must implement OnResultListener")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return mScannerView
    }

    override fun onResume() {
        super.onResume()
        Handler(Looper.getMainLooper()).postDelayed({
            mScannerView.setBorderColor(ContextCompat.getColor(requireContext(), R.color.colorAccent))
            mScannerView.setIsBorderCornerRounded(true)
            mScannerView.setBorderCornerRadius(8)
            mScannerView.setResultHandler(this)
            mScannerView.setAutoFocus(true)
            mScannerView.startCamera()
        }, 200)

    }

    override fun onPause() {
        super.onPause()
        mScannerView.setAutoFocus(false)
        mScannerView.stopCamera()
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        flashItem = menu.findItem(R.id.action_flash)
        val qrItem = menu.findItem(R.id.action_qr)
        flashItem?.isVisible = true
        qrItem.isVisible = false
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_flash -> {
                setFlash()
                return true
            }
            android.R.id.home -> {
                requireActivity().supportFragmentManager.popBackStack()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun handleResult(rawResult: Result?) {
        rawResult?.let {
            onResultListener.onResult(it.text)
            requireActivity().supportFragmentManager.popBackStack()
        }
    }

    private fun setFlash() {
        flash = !flash
        if (flash) {
            flashItem?.setIcon(R.drawable.ic_flash_off_black_24dp)
        } else {
            flashItem?.setIcon(R.drawable.ic_flash_on_black_24dp)
        }
        mScannerView.flash = flash
    }

    interface OnResultListener {
        fun onResult(result: String)
    }

    companion object {
        fun newInstance() =
            QrFragment().apply {

            }
    }
}