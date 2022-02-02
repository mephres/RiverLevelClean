package com.intas.metrolog.ui.events.event_comment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentEventCommentBinding
import com.intas.metrolog.ui.requests.add.AddRequestViewModel

class EventCommentFragment : BottomSheetDialogFragment() {

    var onSaveCommentListener: ((String, Int) -> Unit)? = null

    private var eventCommentMode: String = MODE_UNKNOWN
    private var eventId: Long = 0
    private var eventStatus: Int = 0
    private val modesArray = arrayOf(
        MODE_COMMENT_WITH_IMAGE,
        MODE_COMMENT_WITHOUT_IMAGE
    )

    private val binding by lazy {
        FragmentEventCommentBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[AddRequestViewModel::class.java]
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArgs()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkMode()

        binding.eventCommentImageFab.setOnClickListener {

        }

        binding.saveEventCommentButton.setOnClickListener {
            saveComment()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    private fun saveComment() {

        val comment = binding.eventCommentTextInputLayout.editText?.text.toString()
        if (comment.isEmpty()) {
            showToast(getString(R.string.event_comment_add_comment_error_message))
            return
        }
        onSaveCommentListener?.invoke(comment, eventStatus)
        closeFragment()
    }

    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(EVENT_COMMENT_MODE)) {
            return
        }

        val mode = args.getString(EVENT_COMMENT_MODE)
        if (!modesArray.contains(mode)) {
            return
        }
        mode?.let { eventCommentMode = it }

        if (eventCommentMode == MODE_COMMENT_WITHOUT_IMAGE || eventCommentMode == MODE_COMMENT_WITH_IMAGE) {
            if (!args.containsKey(EVENT_ID) || !args.containsKey(EVENT_STATUS)) {
                return
            }
            eventId = args.getLong(EVENT_ID)
            eventStatus = args.getInt(EVENT_STATUS)
        }
    }

    private fun checkMode() {
        when (eventCommentMode) {
            MODE_COMMENT_WITH_IMAGE -> {
                binding.imageListCardView.visibility = View.VISIBLE
            }

            MODE_COMMENT_WITHOUT_IMAGE -> {
                binding.imageListCardView.visibility = View.GONE
            }
        }
    }

    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(EVENT_COMMENT_FRAGMENT_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val EVENT_COMMENT_FRAGMENT_TAG = "event_comment_fragment_tag"

        private const val MODE_UNKNOWN = "unknown_mode"
        private const val EVENT_COMMENT_MODE = "event_comment_mode"
        private const val MODE_COMMENT_WITH_IMAGE = "mode_comment_with_image"
        private const val MODE_COMMENT_WITHOUT_IMAGE = "mode_comment_without_image"

        private const val EVENT_ID = "event_id"
        private const val EVENT_STATUS = "event_status"

        fun newInstanceWithImage(eventId: Long, eventStatus: Int) = EventCommentFragment().apply {
            arguments = Bundle().apply {
                putString(EVENT_COMMENT_MODE, MODE_COMMENT_WITH_IMAGE)
                putLong(EVENT_ID, eventId)
                putInt(EVENT_STATUS, eventStatus)
            }
        }

        fun newInstanceWithoutImage(eventId: Long, eventStatus: Int) =
            EventCommentFragment().apply {
                arguments = Bundle().apply {
                    putString(EVENT_COMMENT_MODE, MODE_COMMENT_WITHOUT_IMAGE)
                    putLong(EVENT_ID, eventId)
                    putInt(EVENT_STATUS, eventStatus)
                }
            }
    }
}