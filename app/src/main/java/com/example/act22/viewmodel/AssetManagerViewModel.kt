import android.util.Log
import androidx.collection.emptyLongSet
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.act22.data.model.Asset
import com.example.act22.data.model.AssetType
import com.example.act22.data.model.SortingCriteria
import com.example.act22.data.repository.AssetRepository
import com.example.act22.data.repository.AssetRepositoryFirebaseImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AssetManagerViewModel(
    private val repository: AssetRepository = AssetRepositoryFirebaseImpl()
) : ViewModel() {

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        data class Success(val assets: List<Asset>) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    fun fetchAllAssets() {
        Log.d("AssetManagerViewModel", "Fetching all assets...")
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            try {
                val assets = repository.getAllAssets()
                Log.d("AssetManagerViewModel", "Fetched ${assets.size} assets")

                _uiState.value = UiState.Success(assets)
            } catch (e: Exception) {
                Log.e("AssetManagerViewModel", "Error fetching assets: ${e.message}", e)
                _uiState.value = UiState.Error("Failed to load assets: ${e.message}")
            }
        }
    }

    fun searchAssets(query: String) {
        Log.d("AssetManagerViewModel", "Searching assets with query: $query")
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.value = UiState.Loading
            try {
                val assets = repository.searchAssets(query)
                Log.d("AssetManagerViewModel", "Found ${assets.size} assets for query: $query")
                _uiState.value = UiState.Success(assets)
            } catch (e: Exception) {
                Log.e("AssetManagerViewModel", "Error searching assets: ${e.message}", e)
                _uiState.value = UiState.Error("Search failed: ${e.message}")
            }
        }
    }

    fun filterAssetsByType(type: AssetType) {
        Log.d("AssetManagerViewModel", "Filtering assets by type: $type")
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val assets = repository.filterAssetsByType(type)
                Log.d("AssetManagerViewModel", "Filtered ${assets.size} assets for type: $type")
                _uiState.value = UiState.Success(assets)
            } catch (e: Exception) {
                Log.e("AssetManagerViewModel", "Error filtering assets: ${e.message}", e)
                _uiState.value = UiState.Error("Filtering failed: ${e.message}")
            }
        }
    }

    // Sort assets
    fun sortAssets(criteria: SortingCriteria) {
        Log.d("AssetManagerViewModel", "Sorting assets with criteria: $criteria")
        _uiState.value = UiState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val assets = repository.sortAssets(criteria)
                Log.d("AssetManagerViewModel", "Sorted ${assets.size} assets by: $criteria")
                _uiState.value = UiState.Success(assets)
            } catch (e: Exception) {
                Log.e("AssetManagerViewModel", "Error sorting assets: ${e.message}", e)
                _uiState.value = UiState.Error("Sorting failed: ${e.message}")
            }
        }
    }

}
