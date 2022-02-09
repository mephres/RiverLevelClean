package com.intas.metrolog.ui.operation.operation_control

import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputLayout
import com.intas.metrolog.R
import com.intas.metrolog.databinding.FragmentOperationControlInputValueBinding
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.FieldItem
import com.intas.metrolog.pojo.event.event_operation.operation_control.field.dict_data.FieldDictData
import com.intas.metrolog.ui.operation.operation_control.adapter.OperationControlSpinnerAdapter

class OperationControlInputValueFragment : BottomSheetDialogFragment() {

    var onSaveValueListener: ((Boolean) -> Unit)? = null
    private var operationId: Long = 0

    var inputParameterArray = arrayOfNulls<TextInputLayout>(12)
    var spinnerArray = arrayOfNulls<TextInputLayout>(1)

    lateinit var selectedDictData: FieldDictData

    private val binding by lazy {
        FragmentOperationControlInputValueBinding.inflate(layoutInflater)
    }

    private val viewModel by lazy {
        ViewModelProvider(this)[OperationControlInputValueViewModel::class.java]
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

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireContext(), theme)
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        return dialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setUI()

        // получение списка параметров операционного контроля для данной операции
        viewModel.getOperationDoneParameter(operationId)
        viewModel.fieldList.observe(this, { fl ->
            setupFields(fl)
            viewModel.dictDataMap.observe(this, { ddm ->
                setupDict(fl, ddm)
            })
        })

        initClickListener()
    }

    /**
     * Первоначальная инициализация массивов элементов интерфейса, скрытие EditText и Spinner
     */
    private fun setUI() {
        for (i in inputParameterArray.indices) {
            val buttonID = "parameter" + (i + 1)
            val resID = resources.getIdentifier(buttonID, "id", requireContext().packageName)
            inputParameterArray[i] = binding.root.findViewById(resID)
            inputParameterArray[i]?.visibility = View.GONE
        }

        for (i in spinnerArray.indices) {
            val buttonID = "spinner" + (i + 1)
            val resID = resources.getIdentifier(buttonID, "id", requireContext().packageName)
            spinnerArray[i] = binding.root.findViewById(resID)
            spinnerArray[i]?.visibility = View.GONE
        }
    }
    /**
     * Отображение и настройка элементов интерфейса типа EditText
     * @param fieldList - полный список измеряемых параметров операционного контроля
     */
    private fun setupFields(fieldList: List<FieldItem>) {
        var count = 0
        fieldList.forEach {
            //если тип параметра операционного контроля НЕ равен DICT, это означает, что данный параметр принимает значения измерения
            //поэтому отображаем его в виде EditText
            if (!it.type.equals("dict", true)) {
                inputParameterArray[count]?.editText?.setText(
                    it.defaultValue.toString(),
                    TextView.BufferType.EDITABLE
                )
                inputParameterArray[count]?.hint = it.name
                inputParameterArray[count]?.visibility = View.VISIBLE
                inputParameterArray[count]?.tag = it
            }

            when (it.type) {
                "float" -> inputParameterArray[count]?.editText?.inputType =
                    InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                "int" -> inputParameterArray[count]?.editText?.inputType =
                    InputType.TYPE_CLASS_NUMBER
                "string" -> inputParameterArray[count]?.editText?.inputType =
                    InputType.TYPE_CLASS_TEXT
                "dateTime" -> inputParameterArray[count]?.editText?.inputType =
                    InputType.TYPE_CLASS_DATETIME
                else -> inputParameterArray[count]?.editText?.inputType =
                    InputType.TYPE_CLASS_NUMBER
            }
        }
        count++
    }

    /**
     * Отображение и настройка элементов интерфейса типа Spinner
     * @param fieldList - полный список параметров измерения
     * @param dictDataMap - список методов измерения (список для спиннера) для всех параметров операционного контроля
     */
    private fun setupDict(
        fieldList: List<FieldItem>,
        dictDataMap: HashMap<Long, List<FieldDictData>>
    ) {
        var count = 0
        fieldList.forEach {
            //если тип параметра операционного контроля равен DICT, это означает, что данный параметр указывает на способ измерения
            // поэтому считаем такой тип параметра списком и отображаем его в виде spinner
            if (it.type.equals("dict", true)) {
                val dictDataList = dictDataMap[it.id]
                val defaultValue = it.defaultValue
                if (!dictDataList.isNullOrEmpty()) {
                    spinnerArray[count]?.let { spinner ->
                        // создание спиннера
                        createOperationControlSpinner(
                            spinner,
                            dictDataList,
                            defaultValue,
                            it
                        )
                        spinner.hint = getString(R.string.operation_control_parameter_spinner_hint)
                        spinner.visibility = View.VISIBLE
                    }
                    count++
                }
            }
        }
    }

    /**
     * Настройка и отображение списка для Spinner операционного контроля
     * @param spinner - элемент интерфейса типа Spinner
     * @param dictDataList - список методов измерения параметра операционного контроля
     * @param defaultValue - значение по-умолчанию для Spinner
     * @param field - измеряемый параметр, объект типа [FieldItem]
     *
     */
    private fun createOperationControlSpinner(
        spinner: TextInputLayout,
        dictDataList: List<FieldDictData>,
        defaultValue: String?,
        field: FieldItem
    ) {

        val adapter = OperationControlSpinnerAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            dictDataList
        )
        val textView = spinner.editText as AutoCompleteTextView
        textView.setAdapter(adapter)

        if (!defaultValue.isNullOrEmpty()) {

            // первоначальная установка инекса списка
            val selectIndex = try {
                defaultValue.toInt() - 1
            } catch (e: Exception) {
                0
            }

            selectedDictData = adapter.getItem(selectIndex)
            if (selectedDictData != null) {
                textView.setText(selectedDictData.code)
                spinner.tag = field
            }
        }

        textView.onItemClickListener =
            OnItemClickListener { adapterView: AdapterView<*>?, view: View?, i: Int, l: Long ->
                selectedDictData = adapter.getItem(i)
                if (selectedDictData != null) {
                    textView.setText(selectedDictData.code)
                    spinner.tag = field
                }
            }
    }

    /**
     * Проверка правильности введенных параметров операционного контроля
     * @return true - проверка пройдена, false - есть ошибки при заполнении
     */
    private fun validateInputParameters(): Boolean {
        var result = true
        inputParameterArray.forEach {
            it?.let {
                val tempValue = it.editText?.text.toString()
                if (it.visibility == View.VISIBLE && tempValue.isEmpty()) {
                    result = false
                    return@forEach
                }
            }
        }

        spinnerArray.forEach {
            it?.let {
                val tempValue = it.editText?.text.toString()
                if (it.visibility == View.VISIBLE && tempValue.isEmpty()) {
                    result = false
                    return@forEach
                }
            }
        }
        if (!result) {
            showToast(getString(R.string.operation_control_parameter_save_error))
        }
        return result
    }

    /**
     * Установка обработчика нажатий на элементы интерфейса
     */
    private fun initClickListener() {
        binding.operationControlSaveButton.setOnClickListener {
            // проверка правильности введенных значений операционного контроля
            if (!validateInputParameters()) {
                return@setOnClickListener
            }

            var fieldItem: FieldItem? = null

            // цикл по параметрам контроля типа EditText
            inputParameterArray.forEach {
                it?.let {
                    // если параметр отображается на фрагменте
                    if (it.visibility == View.VISIBLE) {
                        // берем из тэга параметр контроля
                        fieldItem = it.tag as FieldItem
                        // введенное значение данного параметра
                        val value = it.editText?.text.toString()
                        fieldItem?.let {
                            // сохранение введенного значения параметра
                            viewModel.saveParameter(it, value)
                        }
                    }
                }
            }

            // цикл по спиннерам
            spinnerArray.forEach {
                it?.let {
                    if (it.visibility == View.VISIBLE) {
                        val field = it.tag as FieldItem
                        val value = it.editText?.text.toString()

                        viewModel.saveParameter(field, value)
                    }
                }
            }

            fieldItem?.let {
                // установка флага для отсылки на сервер операционного контроля
                viewModel.setOperationControlReadyToSend(it.eventId, it.operationId)
                onSaveValueListener?.invoke(true)
            }
            closeFragment()
        }
    }

    /**
     * Получение параметров при запуске фрагмента
     */
    private fun parseArgs() {
        val args = requireArguments()
        if (!args.containsKey(OPERATION_ID)) {
            return
        }

        operationId = args.getLong(OPERATION_ID)
    }

    /**
     * Закрытие данного фрагмента
     */
    private fun closeFragment() {
        val fragment =
            parentFragmentManager.findFragmentByTag(OPERATION_CONTROL_FRAGMENT_TAG)
        fragment?.let {
            parentFragmentManager.beginTransaction().remove(it).commit()
        }
    }

    /**
     * Вывод тоста - сообщения
     */
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val OPERATION_CONTROL_FRAGMENT_TAG = "operation_control_fragment_tag"

        private const val OPERATION_ID = "operation_id"

        fun newInstance(operationId: Long) = OperationControlInputValueFragment().apply {
            arguments = Bundle().apply {
                putLong(OPERATION_ID, operationId)
            }
        }
    }
}