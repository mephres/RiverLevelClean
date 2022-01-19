package com.intas.metrolog.ui.scanner

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Dialog
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.Result
import com.intas.metrolog.R
import com.intas.metrolog.databinding.NfcFragmentBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.main.MainViewModel
import com.intas.metrolog.util.Util
import me.dm7.barcodescanner.zxing.ZXingScannerView

class NfcFragment : BottomSheetDialogFragment(), ZXingScannerView.ResultHandler {
    private val mainViewModel: MainViewModel by activityViewModels()

    private var nfcAdapter: NfcAdapter? = null
    private var scannerMode: String = MODE_UNKNOWN
    private var equipSerialNumber: String? = null
    private var equipItem: EquipItem? = null
    private var flash = false

    private val mScannerView by lazy {
        ZXingScannerView(requireActivity())
    }

    private val nfcViewModel by lazy {
        ViewModelProvider(this)[NfcViewModel::class.java]
    }

    private val binding by lazy {
        NfcFragmentBinding.inflate(layoutInflater)
    }

    private val modes = arrayOf(
        MODE_SCAN_GET_EVENT_BY_EQUIP, MODE_SCAN_START_EVENT,
        MODE_ADD_TAG_FOR_EQUIP, MODE_ADD_NEW_REQUEST
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUI()
        setScannerType()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onResume() {
        super.onResume()
        enableReaderMode()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
        nfcAdapter?.disableReaderMode(requireActivity())
    }

    /**
     * Переопределенная функция класса QR-сканера [ZXingScannerView.ResultHandler]
     * @param rawResult - отсканирования метка QR/штрих-кода
     */
    override fun handleResult(rawResult: Result?) {
        equipSerialNumber = rawResult?.text
        equipSerialNumber?.let {
            launchMode()
        }
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(SCANNER_MODE)) {
            return
        }

        val mode = args.getString(SCANNER_MODE)
        if (!modes.contains(mode)) {
            return
        }
        mode?.let { scannerMode = it }

        if (scannerMode == MODE_ADD_TAG_FOR_EQUIP) {
            if (!args.containsKey(EQUIP_ITEM)) {
                return
            }
            equipItem = args.getParcelable(EQUIP_ITEM)

            if (equipItem == null) {
                return
            }
        }
    }

    /**
     * Анимация для NFC-пиктограммы
     */
    private fun setUI() {
        val colorFrom = ContextCompat.getColor(requireContext(), R.color.colorAccent)
        val colorTo = ContextCompat.getColor(requireContext(), R.color.md_white_1000)
        val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo, colorFrom)
        colorAnimation.duration = 3000 // milliseconds
        colorAnimation.repeatCount = 100
        colorAnimation.addUpdateListener { valueAnimator: ValueAnimator ->
            binding.logoImageView.setColorFilter(
                valueAnimator.animatedValue as Int
            )
        }
        colorAnimation.start()
    }

    /**
     * Функция выбора режима сканирования(NFC или QR)
     */
    private fun setScannerType() {
        binding.changeScanTypeToQR.setOnClickListener {
            binding.qrLayout.addView(mScannerView)
            binding.changeScanTypeToNFC.visibility = View.VISIBLE
            binding.changeScanTypeToQR.visibility = View.GONE
            binding.flash.visibility = View.VISIBLE

            mScannerView.setBorderColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent
                )
            )
            mScannerView.setIsBorderCornerRounded(true)
            mScannerView.setBorderCornerRadius(8)
            mScannerView.setResultHandler(this)
            mScannerView.setAutoFocus(true)
            mScannerView.startCamera()

        }

        binding.changeScanTypeToNFC.setOnClickListener {
            mScannerView.setAutoFocus(false)
            mScannerView.stopCamera()
            binding.qrLayout.removeView(mScannerView)

            binding.changeScanTypeToNFC.visibility = View.GONE
            binding.flash.visibility = View.GONE
            binding.changeScanTypeToQR.visibility = View.VISIBLE

            enableReaderMode()
        }

        binding.flash.setOnClickListener {
            setFlash()
        }
    }

    /**
     * Функция включения/выключения подсветки в режиме QR
     */
    private fun setFlash() {
        flash = !flash
        if (flash) {
            binding.flash.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_off_black_24dp))
        } else {
            binding.flash.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_flash_on_black_24dp))
        }
        mScannerView.flash = flash
    }

    /**
     * Доступ к NFC-адаптеру и чтение меток в enableReaderMode
     */
    private fun enableReaderMode() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(requireActivity())

        nfcAdapter?.enableReaderMode(
            requireActivity(), {
                receivedInput(it)
            }, NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    /**
     * Функция обработки RFID-тэга в строковый вид
     * @param tag - отсканированный RFID-тэг
     */
    private fun receivedInput(tag: Tag) {
        requireActivity().runOnUiThread {
            val id = tag.id
            equipSerialNumber = Util.bytesToHex(id)
            equipSerialNumber?.let {
                launchMode()
            }
        }
    }

    /**
     * Режимы работы NFC и QR сканеров
     */
    private fun launchMode() {
        nfcAdapter?.disableReaderMode(requireActivity())
        when (scannerMode) {
            MODE_SCAN_GET_EVENT_BY_EQUIP -> {

            }
            MODE_SCAN_START_EVENT -> {

            }
            MODE_ADD_TAG_FOR_EQUIP -> {
                Util.safeLet(equipItem, equipSerialNumber) { equipItem, equipNumber ->
                    setRFIDtoEquip(equipItem, equipNumber)
                }
            }
            MODE_ADD_NEW_REQUEST -> {

            }
        }
    }

    /**
     * Функция установки метки на оборудование
     * @param equip - экземпляр класса [EquipItem]
     * @param rfid - отсканированная метка
     */
    private fun setRFIDtoEquip(equip: EquipItem, rfid: String) {

        nfcViewModel.setRFIDtoEquip(equip, rfid)
        nfcViewModel.onSuccess = {
            val tagSetSuccess = getString(R.string.nfc_tag_is_set)
            Toast.makeText(requireContext(), String.format(tagSetSuccess, rfid), Toast.LENGTH_SHORT)
                .show()
            closeFragment()
        }

        nfcViewModel.onFailure = {
            val tagSetFailure = getString(R.string.nfc_tag_set_failure)
            Toast.makeText(requireContext(), String.format(tagSetFailure, it), Toast.LENGTH_SHORT)
                .show()
            closeFragment()
        }
        nfcViewModel.onError = {
            Toast.makeText(
                requireContext(), getString(R.string.nfc_tag_set_error),
                Toast.LENGTH_SHORT
            ).show()
            closeFragment()
        }
    }

    private fun closeFragment() {
        nfcAdapter?.disableReaderMode(requireActivity())
        mScannerView.setAutoFocus(false)
        mScannerView.stopCamera()
        val fragment =
            parentFragmentManager.findFragmentByTag(NFC_FRAGMENT_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    companion object {
        const val NFC_FRAGMENT_TAG = "nfc_fragment_tag"

        private const val SCANNER_MODE = "scanner_mode"
        private const val MODE_UNKNOWN = "unknown_mode"
        private const val MODE_SCAN_GET_EVENT_BY_EQUIP = "mode_scan_get_event_by_equip"
        private const val MODE_SCAN_START_EVENT = "mode_scan_start_event"
        private const val MODE_ADD_TAG_FOR_EQUIP = "mode_add_tag_for_equip"
        private const val MODE_ADD_NEW_REQUEST = "mode_add_new_request"

        private const val EQUIP_ITEM = "equip_item"

        fun newInstanceAddTag(equip: EquipItem) = NfcFragment().apply {
            arguments = Bundle().apply {
                putParcelable(EQUIP_ITEM, equip)
                putString(SCANNER_MODE, MODE_ADD_TAG_FOR_EQUIP)
            }
        }

        fun newInstanceGetEvent() = NfcFragment().apply {
            arguments = Bundle().apply {
                putString(SCANNER_MODE, MODE_SCAN_GET_EVENT_BY_EQUIP)
            }
        }

        fun newInstanceStartEvent() = NfcFragment().apply {
            arguments = Bundle().apply {
                putString(SCANNER_MODE, MODE_SCAN_START_EVENT)
            }
        }

        fun newInstanceAddRequest() = NfcFragment().apply {
            arguments = Bundle().apply {
                putString(SCANNER_MODE, MODE_ADD_NEW_REQUEST)
            }
        }
    }
}