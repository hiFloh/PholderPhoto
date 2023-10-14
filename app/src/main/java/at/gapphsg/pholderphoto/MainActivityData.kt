package at.gapphsg.pholderphoto

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainActivityData : ViewModel() {
    private var _fileName = MutableLiveData("")
    private var _dirty = MutableLiveData(false)
    private var _path = MutableLiveData<Uri>()

    //    private var _hist=MutableLiveData<Set<String>>();
    private val _settingPath = "Path"
    private val _settingName = "name"
//    private val SETTING_HIST="hist"


    val path: LiveData<Uri>
        get() = _path
    val dirty: LiveData<Boolean>
        get() = _dirty
    val name: LiveData<String>
        get() = _fileName

    fun onNameChange(name: String) {
        _dirty.value = name != _fileName.value
    }

    fun save(name: String) {
        _fileName.value = name
        _dirty.value = false
    }

    fun setPath(path: Uri) {
//        _hist.value?.add(path.toString());
        _path.value = path
    }

    fun load(pref: SharedPreferences) {
        _path.value = Uri.parse(pref.getString(_settingPath, ""))
        _fileName.value = pref.getString(_settingName, "")
//        pref.getStringSet(SETTING_HIST,_hist.value)
        _dirty.value = true
    }

    fun save(pref: SharedPreferences) {
        with(pref.edit()) {
            putString(_settingPath, path.value.toString())
            putString(_settingName, name.value.toString())
//            putStringSet(SETTING_HIST,_hist.value);
            apply()
        }
    }
}