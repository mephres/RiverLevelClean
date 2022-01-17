package com.intas.metrolog.ui.scanner

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.intas.metrolog.R
import com.intas.metrolog.databinding.ActivityScannerBinding
import com.intas.metrolog.pojo.equip.EquipItem
import com.intas.metrolog.util.Util

class ScannerActivity : AppCompatActivity(), QrFragment.OnResultListener {
    private var nfcAdapter: NfcAdapter? = null
    private var nfcPendingIntent: PendingIntent? = null
    private var scannerMode: String = MODE_UNKNOWN
    private var equipSerialNumber: String = MODE_UNKNOWN

    private val binding by lazy {
        ActivityScannerBinding.inflate(layoutInflater)
    }

    private val modes = arrayOf(
        MODE_SCAN_FIND_EVENT_BY_EQUIP, MODE_SCAN_START_EVENT,
        MODE_ADD_TAG_FOR_EQUIP, MODE_ADD_NEW_REQUEST
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        parseIntent()
        setToolbar()

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )
        if (intent != null) {
            processIntent(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.scanner_menu, menu)
        val flashItem = menu.findItem(R.id.action_flash)
        val qrItem = menu.findItem(R.id.action_qr)
        flashItem.isVisible = false
        qrItem.isVisible = true
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_qr -> {
                supportFragmentManager.beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.qrScannerView, QrFragment.newInstance())
                    .commit()
                return true
            }

            android.R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            processIntent(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, nfcPendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onResult(result: String) {
        equipSerialNumber = result
        launchMode()
    }

    private fun setToolbar() {
        setSupportActionBar(binding.includeToolbar.toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
            it.title = "Сканирование метки"
        }
    }

    private fun processIntent(checkIntent: Intent) {
        val tag = checkIntent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)
        val id = tag?.id
        id?.let {
            equipSerialNumber = Util.bytesToHex(it)
            launchMode()
        }
    }

    private fun launchMode() {
        val messageString = getString(R.string.nfc_serial_number_message) + equipSerialNumber
        binding.messageTextView.text = messageString
        when (scannerMode) {
            MODE_SCAN_FIND_EVENT_BY_EQUIP -> {

            }
            MODE_SCAN_START_EVENT -> {

            }
            MODE_ADD_TAG_FOR_EQUIP -> {

            }
            MODE_ADD_NEW_REQUEST -> {

            }
        }
    }


    private fun parseIntent() {
        if (!intent.hasExtra(SCANNER_MODE)) {
            return
        }

        val mode = intent.getStringExtra(SCANNER_MODE)
        if (!modes.contains(mode)) {
            return
        }
        mode?.let { scannerMode = it }
    }

    companion object {
        private const val SCANNER_MODE = "scanner_mode"
        private const val MODE_UNKNOWN = "unknown_mode"
        private const val MODE_SCAN_FIND_EVENT_BY_EQUIP = "mode_scan_find_event_by_equip"
        private const val MODE_SCAN_START_EVENT = "mode_scan_start_event"
        private const val MODE_ADD_TAG_FOR_EQUIP = "mode_add_tag_for_equip"
        private const val MODE_ADD_NEW_REQUEST = "mode_add_new_request"

        private const val EQUIP_ITEM = "equip_item"

        fun newIntentFindEvent(context: Context) =
            Intent(context, ScannerActivity::class.java).apply {
                putExtra(SCANNER_MODE, MODE_SCAN_FIND_EVENT_BY_EQUIP)
            }

        fun newIntentStartEvent(context: Context) =
            Intent(context, ScannerActivity::class.java).apply {
                putExtra(SCANNER_MODE, MODE_SCAN_START_EVENT)
            }

        fun newIntentAddTag(context: Context, equip: EquipItem) =
            Intent(context, ScannerActivity::class.java).apply {
                putExtra(SCANNER_MODE, MODE_ADD_TAG_FOR_EQUIP)
                putExtra(EQUIP_ITEM, equip)
            }

        fun newIntentAddEquipInfo(context: Context) =
            Intent(context, ScannerActivity::class.java).apply {
                putExtra(SCANNER_MODE, MODE_ADD_NEW_REQUEST)
            }
    }
}