package pro.sketchware.fragments.settings.selector.block

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.TextView

import androidx.lifecycle.lifecycleScope

import com.sketchware.remod.R
import com.sketchware.remod.databinding.FragmentBlockSelectorManagerBinding
import com.sketchware.remod.databinding.DialogBlockConfigurationBinding as DialogCreateBinding
import com.sketchware.remod.databinding.DialogSelectorActionsBinding

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken

import com.google.android.material.appbar.MaterialToolbar

import pro.sketchware.fragments.base.BaseFragment
import pro.sketchware.utility.SketchwareUtil.toast
import pro.sketchware.utility.FileUtil.writeFile
import pro.sketchware.utility.FileUtil.isExistFile
import pro.sketchware.fragments.settings.selector.block.details.BlockSelectorDetailsFragment

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.io.readText

import java.io.File

import a.a.a.aB

class BlockSelectorManagerFragment : BaseFragment() {
    
    private var _binding: FragmentBlockSelectorManagerBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        saved: Bundle?
    ): View {
        _binding = FragmentBlockSelectorManagerBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    private var selectors: MutableList<Selector> = mutableListOf()
    private lateinit var adapter: BlockSelectorAdapter

    override fun onViewCreated(
        view: View, 
        saved: Bundle?
    ) {
        configureToolbar(binding.toolbar)
        handleInsetts(binding.root)
        adapter = BlockSelectorAdapter(
            onClick = { selector, index ->
                openFragment(BlockSelectorDetailsFragment(index, selectors))
            },
            onLongClick = { selector, index ->
                showActionsDialog(index = index)
            }
        )
        lifecycleScope.launch {
            if (isExistFile(BlockSelectorConsts.BLOCK_SELECTORS_FILE.absolutePath)) {
                selectors = parseJson(
                    BlockSelectorConsts.BLOCK_SELECTORS_FILE.readText(
                        Charsets.UTF_8
                    )
                )
            } else {
                selectors.add(
                    Selector(
                        name = "typeview",
                        title = "Select typeview:",
                        data = getTypeViewList()
                    )
                )
                saveAllSelectors()
            }
        }
        binding.list.adapter = adapter
        adapter.submitList(selectors)
        
        binding.createNew.setOnClickListener {
            showCreateEditDialog()
        }
        
        super.onViewCreated(view, saved)
    }
    
    private fun parseJson(
        jsonString: String
    ): MutableList<Selector> {
        val gson = Gson()
        val listType = object : TypeToken<List<Selector>>() {}.type
        return gson.fromJson(jsonString, listType)
    }
    
    private fun showCreateEditDialog(
        index: Int = 0,
        isEdit: Boolean = false
    ) {
        val dialogBinding = DialogCreateBinding.inflate(LayoutInflater.from(requireContext())).apply {
            tilPalettesPath.hint = "Selector name"
            tilBlocksPath.hint = "Selector title (ex: Select View:)"
            if (isEdit) {
                palettesPath.setText(selectors.get(index).name)
                blocksPath.setText(selectors.get(index).title)
            }
            palettesPath.setOnTextChanged(
                onTextChanged = {
                    if (itemAlreadyExists(it.toString())) {
                        tilPalettesPath.setError("An item with this name already exists")
                    } else {
                        tilPalettesPath.setError(null)
                    }
                }
            )
            if (palettesPath.text?.toString().equals("typeview")) {
                palettesPath.isEnabled = false
                tilPalettesPath.setOnClickListener {
                    toast("You cannot change the name of this selector")
                }
            }
        }
        val dialog = aB(requireActivity()).apply {
            dialogTitleText = if (!isEdit) "New Selector" else "Edit Selector"
            dialogCustomView = dialogBinding.getRoot()
            dialogYesText = if (!isEdit) "Create" else "Save"
            dialogNoText = "Cancel"
            dialogYesListener = View.OnClickListener {
                val selectorName = dialogBinding.palettesPath.text?.toString()
                val selectorTitle = dialogBinding.blocksPath.text?.toString()
                
                if (selectorName.isNullOrEmpty()) {
                    toast("Please type Selector name")
                    return@OnClickListener
                }
                if (selectorTitle.isNullOrEmpty()) {
                    toast("Please type Selector title")
                    return@OnClickListener
                }
                if (!isEdit) {
                    if (!itemAlreadyExists(selectorName)) {
                        selectors.add(
                            Selector(
                                name = selectorName,
                                title = selectorTitle,
                                data = mutableListOf()
                            )
                        )
                    } else {
                        toast("An item with this name already exists")
                    }
                } else {
                    selectors[index] = Selector(
                        name = selectorName,
                        title = selectorTitle,
                        data = selectors.get(index).data
                    )
                }
                saveAllSelectors()
                adapter.notifyDataSetChanged()
                dismiss()
            }
            dialogNoListener= View.OnClickListener {
                dismiss()
            }
        }
        dialog.show()
    }
    
    private fun showActionsDialog(
        index: Int
    ) {
        val dialogBinding = DialogSelectorActionsBinding.inflate(LayoutInflater.from(requireContext()))
        val dialog = aB(requireActivity()).apply {
            dialogTitleText = "Actions"
            dialogCustomView = dialogBinding.root
        }
        dialogBinding.apply {
            edit.setOnClickListener {
                dialog.dismiss()
                showCreateEditDialog(
                    index = index,
                    isEdit = true
                )
            }
            export.setOnClickListener {
                exportSelector(
                    selector = selectors.get(index)
                )
            }
            delete.setOnClickListener {
                if(selectors.get(index).name.equals("typeview")) {
                    toast("you cannot delete the typeview.")
                    return@setOnClickListener
                }
                dialog.dismiss()
                showConfirmationDialog(
                    message = "Are you sure you want to delete this Selector?",
                    onConfirm = {
                        selectors.removeAt(index)
                        saveAllSelectors()
                        adapter.notifyDataSetChanged()
                        it.dismiss()
                    },
                    onCancel = {
                        it.dismiss()
                    }
                )
            }
        }
        dialog.show()
    }
    
    private fun showConfirmationDialog(
        message: String,
        onConfirm: (aB) -> Unit,
        onCancel: (aB) -> Unit
    ) {
        val dialog = aB(requireActivity()).apply {
            dialogTitleText = "Attention"
            dialogMessageText = message
            dialogYesText = "Yes"
            dialogNoText = "Cancel"
            setCancelable(false)
            dialogYesListener = View.OnClickListener {
                onConfirm(this)
            }
            dialogNoListener = View.OnClickListener {
                onCancel(this)
            }
        }
        dialog.show()
    }
    
    override fun configureToolbar(toolbar: MaterialToolbar) {
        super.configureToolbar(toolbar)
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.import_block_selector_menus -> {
                    // todo 😭
                    true
                }
                R.id.export_all_block_selector_menus -> {
                    saveAllSelectors(
                        path = BlockSelectorConsts.EXPORT_FILE.absolutePath,
                        message = "Exported in ${BlockSelectorConsts.EXPORT_FILE.absolutePath}"
                    )
                    true
                }
                else -> false
            }
        }
    }
   
    private fun saveAllSelectors(
        path: String = BlockSelectorConsts.BLOCK_SELECTORS_FILE.absolutePath,
        message: String = "Saved"
    ) {
        writeFile(
            path,
            getGson().toJson(selectors)
        )
        toast(message)
    }
    
    private fun exportSelector(
        selector: Selector
    ) {
        writeFile(
            BlockSelectorConsts.EXPORT_FILE.absolutePath.replace("All_Menus", selector.name),
            getGson().toJson(selector)
        )
    }
    
    private suspend fun getSelectorFromPath(
        path: File
    ): Selector {
        val json = path.readText(Charsets.UTF_8)
        return getGson().fromJson(json, Selector::class.java)
    }
    
    private fun getGson() : Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private fun itemAlreadyExists(
        toCompare: String
    ): Boolean = selectors.any {
        it.name.lowercase() == toCompare.lowercase()
    }
    
    /*
    * A Default list of Selector Itens
    */
    private fun getTypeViewList(): MutableList<String> {
        return mutableListOf(
            "View",
            "ViewGroup",
            "LinearLayout",
            "RelativeLayout",
            "ScrollView",
            "HorizontalScrollView",
            "TextView",
            "EditText",
            "Button",
            "RadioButton",
            "CheckBox",
            "Switch",
            "ImageView",
            "SeekBar",
            "ListView",
            "Spinner",
            "WebView",
            "MapView",
            "ProgressBar"
        )
    }
    
    // big 😡
    private fun EditText.setOnTextChanged(
        onTextChanged: (CharSequence) -> Unit,
        beforeTextChanged: (CharSequence) -> Unit = { },
        afterTextChanged: () -> Unit = { }
    ) {
        this.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(
                s: CharSequence?,
                start: Int, 
                before: Int, 
                count: Int
            ) {
                s?.let {
                    onTextChanged(it)
                }
            }
            override fun beforeTextChanged(
                s: CharSequence?, 
                start: Int,
                count: Int, 
                after: Int
            ) {
                s?.let {
                    beforeTextChanged(it)
                }
            }
            override fun afterTextChanged(e: Editable?) {
                afterTextChanged()
            }
        })
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}