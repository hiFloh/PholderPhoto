package at.gapphsg.pholderphoto

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModelProvider
import at.gapphsg.pholderphoto.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Date


open class MainActivity : AppCompatActivity() {
    private lateinit var _pathOpenDialogLauncher: ActivityResultLauncher<Intent>
    private lateinit var _takePictureLauncher: ActivityResultLauncher<Intent>
    private lateinit var _mSetting: SharedPreferences
    private lateinit var _viewModel: MainActivityData

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _mSetting =
            getSharedPreferences(getString(R.string.prefferenceFileKey), Context.MODE_PRIVATE)
        _viewModel = ViewModelProvider(this)[MainActivityData::class.java]
        val binding: ActivityMainBinding =
            DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewmodel = _viewModel
        binding.lifecycleOwner = this


        _viewModel.load(_mSetting)
        findViewById<TextInputEditText>(R.id.name)?.addTextChangedListener {
            _viewModel.onNameChange(findViewById<TextInputEditText>(R.id.name).text.toString())
        }

        _viewModel.dirty.observe(this, ::onDirtyUpdate)
        findViewById<Button>(R.id.save).setOnClickListener(::onSave)
        findViewById<Button>(R.id.slect_folder).setOnClickListener(::onFolderSelect)
        findViewById<FloatingActionButton>(R.id.floatingActionButton).setOnClickListener(::onTakePicture)

        _pathOpenDialogLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("sd", result.toString())
                    // Handle the result
                    val s: Uri? = result.data?.data
                    if (s != null) {
                        contentResolver.takePersistableUriPermission(s, 0)
                        _viewModel.setPath(s)
                    } else {
                        //TODO:Error Handling
                    }
                }
            }
        _takePictureLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {
                    Log.d("sd", result.toString())
                } else {
                    if (lastSavefile != null) {
                        lastSavefile!!.delete()
                        lastSavefile = null
                    }
                }
            }

        val x = _viewModel.name.value
        findViewById<TextInputEditText>(R.id.name).setText(x)
    }

    override fun onStop() {
        super.onStop()
        _viewModel.save(_mSetting)
    }

    private fun onDirtyUpdate(s: Boolean) {

        if (_viewModel.dirty.value == true) {
//                var x=findViewById<ConstraintLayout>(R.id.bg).background
//                Log.d("color",x.toString())
            findViewById<ConstraintLayout>(R.id.bg).setBackgroundColor(
                ContextCompat.getColor(
                    baseContext,
                    R.color.red
                )
            )
        } else {
            findViewById<ConstraintLayout>(R.id.bg).background = null
        }
    }

    private fun onSave(v: View) {
        v
        _viewModel.save(findViewById<TextInputEditText>(R.id.name).text.toString())
    }

    private fun onFolderSelect(v: View) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
        _pathOpenDialogLauncher.launch(intent)
    }

    private var lastSavefile: DocumentFile? = null

    @RequiresApi(Build.VERSION_CODES.N)
    fun onTakePicture(v: View) {
        val d = DocumentFile.fromTreeUri(this, _viewModel.path.value!!)
        val t = _viewModel.name.value + SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        contentResolver.takePersistableUriPermission(_viewModel.path.value!!, 0)
        try {
            val file = d!!.createFile("image/jpeg", "$t.jpeg")
            lastSavefile = file
            if (d.canWrite()) {
                val i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                i.putExtra(MediaStore.EXTRA_OUTPUT, file!!.uri)
                _takePictureLauncher.launch(i)
            }
        } catch (e: Exception) {
            null
        }
    }

}

