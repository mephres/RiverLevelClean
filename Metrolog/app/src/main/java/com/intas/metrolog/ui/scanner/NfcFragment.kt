package com.intas.metrolog.ui.scanner

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.app.Dialog
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.zxing.ResultPoint
import com.intas.metrolog.R
import com.intas.metrolog.databinding.NfcFragmentBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.ui.requests.add.AddRequestFragment
import com.intas.metrolog.util.Util
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.camera.CameraSettings


class NfcFragment : BottomSheetDialogFragment() {

    private var nfcAdapter: NfcAdapter? = null
    private var scannerMode: String = MODE_UNKNOWN
    private var equipSerialNumber: String? = null
    private var equipItem: EquipItem? = null
    private var flash = false
    private var qrMode = false

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

        savedInstanceState?.let {
            qrMode = it.getBoolean(NFC_FRAGMENT_STATE)
        }

        setUI()
        initQRScannerDecoder()
        setScannerClickListener()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onResume() {
        super.onResume()
        checkScannerType()
    }

    override fun onPause() {
        super.onPause()
        qrMode = binding.qrScannerCardView.isVisible
        binding.qrScannerView.pause()
        nfcAdapter?.disableReaderMode(requireActivity())
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(NFC_FRAGMENT_STATE, qrMode)
    }

    /**
     * Функция настройки сканнеров, в зависимости от выбранного типа сканирования [qrMode] (NFC или QR)
     *
     */
    private fun checkScannerType() {
        if (qrMode) {

            qrMode = false
            binding.changeScanTypeToNFC.visibility = View.VISIBLE
            binding.changeScanTypeToQR.visibility = View.GONE
            binding.flash.visibility = View.VISIBLE

            binding.qrScannerView.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.colorAccent
                )
            )

            val cameraSettings = CameraSettings()
            cameraSettings.requestedCameraId = 0 // front/back/etc
            cameraSettings.focusMode = CameraSettings.FocusMode.AUTO
            cameraSettings.isAutoFocusEnabled = true
            cameraSettings.isBarcodeSceneModeEnabled = true

            binding.qrScannerView.barcodeView.cameraSettings = cameraSettings
            binding.qrScannerView.setStatusText("")
            binding.qrScannerView.resume()

            binding.qrScannerCardView.visibility = View.VISIBLE

        } else {
            qrMode = true
            binding.qrScannerView.pauseAndWait()

            binding.qrScannerCardView.visibility = View.INVISIBLE
            binding.changeScanTypeToNFC.visibility = View.GONE
            binding.flash.visibility = View.GONE
            binding.changeScanTypeToQR.visibility = View.VISIBLE

            enableReaderMode()
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

        binding.qrScannerCardView.visibility = View.INVISIBLE
    }

    private fun initQRScannerDecoder() {
        binding.qrScannerView.barcodeView.decodeContinuous(object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                equipSerialNumber = result.toString()
                equipSerialNumber?.let {
                    launchMode()
                    Log.d("QR_SCANNER_DECODER_RES", "equipSerialNumber = $it")
                }
                binding.qrScannerView.barcodeView.stopDecoding()
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {
                Log.d("QR_SCANNER_DECODER_RP", resultPoints.toString())
            }
        })
    }

    /**
     * Функция выбора режима сканирования(NFC или QR)
     */
    private fun setScannerClickListener() {
        binding.changeScanTypeToQR.setOnClickListener {
            checkScannerType()
        }

        binding.changeScanTypeToNFC.setOnClickListener {
            checkScannerType()
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
            binding.flash.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_flash_off_black_24dp
                )
            )
            binding.qrScannerView.setTorchOn()
        } else {
            binding.flash.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_flash_on_black_24dp
                )
            )
            binding.qrScannerView.setTorchOff()
        }
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
                equipSerialNumber?.let {
                    getEquipByRFID(it)
                }
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

    /**
     * Функция получения экземпляра оборудования по RFID-тэгу для создания заявки
     * @param rfid - отсканированная метка
     */
    private fun getEquipByRFID(rfid: String) {
        nfcViewModel.getEquipByRFID(rfid)
        nfcViewModel.onEquipItemSuccess = {
            val addRequestFragment = AddRequestFragment.newInstanceWithRfid(it)
            addRequestFragment.show(
                requireActivity().supportFragmentManager,
                AddRequestFragment.ADD_REQUEST_FRAGMENT_TAG
            )
            closeFragment()
        }
        nfcViewModel.onFailure = {
            val tagGetFailure = getString(R.string.nfc_tag_get_equip_failure)
            Toast.makeText(requireContext(), String.format(tagGetFailure, it), Toast.LENGTH_SHORT)
                .show()
            closeFragment()
        }
        nfcViewModel.onError = {
            Toast.makeText(
                requireContext(), getString(R.string.nfc_tag_get_equip_error),
                Toast.LENGTH_SHORT
            ).show()
            closeFragment()
        }
    }

    private fun closeFragment() {
        nfcAdapter?.disableReaderMode(requireActivity())
        binding.qrScannerView.pause()
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

        private const val NFC_FRAGMENT_STATE = "nfc_fragment_state"

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