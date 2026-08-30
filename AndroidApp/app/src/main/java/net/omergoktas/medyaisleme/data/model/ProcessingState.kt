package net.omergoktas.medyaisleme.data.model

import android.net.Uri

sealed class ProcessingState {
    object Idle : ProcessingState()
    
    data class Uploading(
        val progressPercent: Int,
        val loadedBytes: Long,
        val totalBytes: Long
    ) : ProcessingState()
    
    object ServerProcessing : ProcessingState()
    
    object Downloading : ProcessingState()
    
    data class Success(
        val filename: String,
        val fileUri: Uri?,
        val message: String = "Tebrikler! İşlem başarıyla tamamlandı ve dosyanız İndirilenler klasörüne kaydedildi."
    ) : ProcessingState()
    
    data class Error(
        val message: String,
        val isNetworkError: Boolean = false
    ) : ProcessingState()
}
