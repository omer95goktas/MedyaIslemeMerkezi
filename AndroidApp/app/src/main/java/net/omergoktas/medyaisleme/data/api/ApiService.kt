package net.omergoktas.medyaisleme.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Streaming

interface ApiService {

    // 1. Videodan Sese Dönüştür
    @Streaming
    @Multipart
    @POST("api/video-to-audio")
    suspend fun videoToAudio(
        @Part video: MultipartBody.Part,
        @Part("format") format: RequestBody
    ): Response<ResponseBody>

    // 2. Ses Formatı Değiştir
    @Streaming
    @Multipart
    @POST("api/audio-to-audio")
    suspend fun audioToAudio(
        @Part audio: MultipartBody.Part,
        @Part("format") format: RequestBody
    ): Response<ResponseBody>

    // 3. Video Formatı Değiştir & GIF Yap
    @Streaming
    @Multipart
    @POST("api/video-to-video")
    suspend fun videoToVideo(
        @Part video: MultipartBody.Part,
        @Part("format") format: RequestBody,
        @Part("gif_start") gifStart: RequestBody? = null,
        @Part("gif_duration") gifDuration: RequestBody? = null,
        @Part("gif_width") gifWidth: RequestBody? = null,
        @Part("gif_fps") gifFps: RequestBody? = null
    ): Response<ResponseBody>

    // 4. Sesten Video Oluştur
    @Streaming
    @Multipart
    @POST("api/audio-to-video")
    suspend fun audioToVideo(
        @Part audio: MultipartBody.Part,
        @Part image: MultipartBody.Part,
        @Part("resolution") resolution: RequestBody
    ): Response<ResponseBody>

    // 5. Resim Formatı Değiştir
    @Streaming
    @Multipart
    @POST("api/image-to-image")
    suspend fun imageToImage(
        @Part image: MultipartBody.Part,
        @Part("format") format: RequestBody
    ): Response<ResponseBody>

    // 6. İki Videoyu Birleştir
    @Streaming
    @Multipart
    @POST("api/merge-videos")
    suspend fun mergeVideos(
        @Part video1: MultipartBody.Part,
        @Part video2: MultipartBody.Part
    ): Response<ResponseBody>

    // 7. İki Sesi Birleştir
    @Streaming
    @Multipart
    @POST("api/merge-audios")
    suspend fun mergeAudios(
        @Part audio1: MultipartBody.Part,
        @Part audio2: MultipartBody.Part
    ): Response<ResponseBody>

    // 8. Belge Dönüştürücü
    @Streaming
    @Multipart
    @POST("api/convert-document")
    suspend fun documentConvert(
        @Part file: MultipartBody.Part,
        @Part("format") format: RequestBody
    ): Response<ResponseBody>

    // 9. Taranmış PDF OCR
    @Streaming
    @Multipart
    @POST("api/pdf-ocr")
    suspend fun pdfOcr(
        @Part pdfFile: MultipartBody.Part
    ): Response<ResponseBody>

    // 10. Resimden Yazı Okuma (OCR -> TXT)
    @Streaming
    @Multipart
    @POST("api/image-to-text")
    suspend fun imageToText(
        @Part image: MultipartBody.Part
    ): Response<ResponseBody>

    // 11. PDF Sayfalarını Böl
    @Streaming
    @Multipart
    @POST("api/split-pdf")
    suspend fun splitPdf(
        @Part pdfFile: MultipartBody.Part
    ): Response<ResponseBody>

    // 12. Birden Fazla PDF'i Birleştir
    @Streaming
    @Multipart
    @POST("api/merge-pdfs")
    suspend fun mergePdfs(
        @Part pdf1: MultipartBody.Part,
        @Part pdf2: MultipartBody.Part
    ): Response<ResponseBody>

    // 13. Sıfırdan Belge Oluşturucu
    @Streaming
    @Multipart
    @POST("api/create-document")
    suspend fun createDocument(
        @Part("doc_name") docName: RequestBody,
        @Part("format") format: RequestBody,
        @Part("content_html") contentHtml: RequestBody
    ): Response<ResponseBody>

    // Geri Bildirim / Hata Bildirimi
    @Multipart
    @POST("api/feedback")
    suspend fun submitFeedback(
        @Part("tool_name") toolName: RequestBody,
        @Part("error_type") errorType: RequestBody,
        @Part("source_format") sourceFormat: RequestBody,
        @Part("target_format") targetFormat: RequestBody,
        @Part("description") description: RequestBody,
        @Part("website_url") websiteUrl: RequestBody? = null,
        @Part("android_version") androidVersion: RequestBody,
        @Part("app_version") appVersion: RequestBody
    ): Response<ResponseBody>
}
