package ro.ubbcluj.cs.ilazar.myapp2.todo.item

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ro.ubbcluj.cs.ilazar.myapp2.core.Result
import ro.ubbcluj.cs.ilazar.myapp2.core.TAG
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.Item
import ro.ubbcluj.cs.ilazar.myapp2.todo.data.ItemRepository

class ItemEditViewModel : ViewModel() {
    private val mutableItem = MutableLiveData<Item>().apply { value = Item("", "") }
    private val mutableFetching = MutableLiveData<Boolean>().apply { value = false }
    private val mutableCompleted = MutableLiveData<Boolean>().apply { value = false }
    private val mutableException = MutableLiveData<Exception>().apply { value = null }

    val item: LiveData<Item> = mutableItem
    val fetching: LiveData<Boolean> = mutableFetching
    val fetchingError: LiveData<Exception> = mutableException
    val completed: LiveData<Boolean> = mutableCompleted

    fun loadItem(itemId: String) {
        viewModelScope.launch {
            Log.v(TAG, "loadItem...");
            mutableFetching.value = true
            mutableException.value = null
            when (val result = ItemRepository.load(itemId)) {
                is Result.Success -> {
                    Log.d(TAG, "loadItem succeeded");
                    mutableItem.value = result.data
                }
                is Result.Error -> {
                    Log.w(TAG, "loadItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableFetching.value = false
        }
    }

    fun saveOrUpdateItem(text: String) {
        viewModelScope.launch {
            Log.v(TAG, "saveOrUpdateItem...");
            val item = mutableItem.value ?: return@launch
            item.text = text
            mutableFetching.value = true
            mutableException.value = null
            val result: Result<Item>
            if (item._id.isNotEmpty()) {
                result = ItemRepository.update(item)
            } else {
                result = ItemRepository.save(item)
            }
            when (result) {
                is Result.Success -> {
                    Log.d(TAG, "saveOrUpdateItem succeeded");
                    mutableItem.value = result.data
                }
                is Result.Error -> {
                    Log.w(TAG, "saveOrUpdateItem failed", result.exception);
                    mutableException.value = result.exception
                }
            }
            mutableCompleted.value = true
            mutableFetching.value = false
        }
    }
}
