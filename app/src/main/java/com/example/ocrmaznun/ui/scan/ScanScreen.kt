package com.example.ocrmaznun.ui.scan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun RequestPermissions() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] ?: false
        val storageGranted = permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: false

        if (cameraGranted && storageGranted) {
            // Izin diberikan, lakukan apa yang diperlukan
        } else {
            // Izin ditolak
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        )
    }
}

@Composable
fun ScanScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Meminta izin ketika composable ini dipanggil
    RequestPermissions()

    // Peluncur kamera
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri?.let {
                coroutineScope.launch {
                    isProcessing = true
                    errorMessage = null

                    val result = processImage(context, it)
                    if (result.startsWith("error")) {
                        errorMessage = result
                    } else {
                        navController.navigate("result/${Uri.encode(result)}")
                    }

                    isProcessing = false
                }
            }
        }
    }

    // Membuat URI untuk menyimpan gambar
    LaunchedEffect(Unit) {
        val uri = createImageUri(context)
        imageUri = uri
        takePictureLauncher.launch(uri)  // Langsung buka kamera saat screen muncul
    }

    // Menampilkan loading saat proses berjalan
    if (isProcessing) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Menampilkan pesan kesalahan jika ada
    errorMessage?.let {
        Text(
            text = it,
            style = TextStyle(
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}

fun createImageUri(context: Context): Uri {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File.createTempFile(
        "JPEG_${timeStamp}_",
        ".jpg",
        storageDir
    )
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
}

private const val TAG = "ScanScreen"

private suspend fun processImage(context: Context, imageUri: Uri): String {
    return withContext(Dispatchers.IO) {
        try {
            // Mengatur opsi untuk mengurangi ukuran gambar
            val options = BitmapFactory.Options().apply {
                inSampleSize = 8 // Mengurangi ukuran gambar ke setengah dari ukuran asli
            }
            // Memuat bitmap dari URI dengan opsi yang ditetapkan
            val bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imageUri), null, options)

            if (bitmap == null) {
                return@withContext "error: Gagal memuat gambar."
            }

            // Membuat file sementara untuk menyimpan gambar yang dikompresi
            val compressedFile = File.createTempFile("temp_image_compressed", ".jpg", context.cacheDir)
            FileOutputStream(compressedFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream) // Kompresi ke 50%
            }

            // Membuat request body untuk multipart upload
            val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), compressedFile)
            val body = MultipartBody.Part.createFormData("image", compressedFile.name, requestFile)
            val userId = MultipartBody.Part.createFormData("user_id", "12345")

            // Menggunakan ML Kit untuk deteksi teks
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.Builder().build())
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            val detectedText = result.text

            // Mengidentifikasi jenis dokumen
            val documentType = identifyDocumentType(detectedText)

            Log.d(TAG, "Teks terdeteksi: $detectedText")
            Log.d(TAG, "Jenis dokumen terdeteksi: $documentType")

            // Mengirim gambar ke API yang sesuai
            val apiResponse = try {
                when (documentType) {
                    "ktp" -> {
                        Log.d(TAG, "Mengirim gambar ke API KTP")
                        ktpApiService(context).uploadKtp(userId, body)
                    }
                    "paspor" -> {
                        Log.d(TAG, "Mengirim gambar ke API Paspor")
                        pasporApiService(context).uploadPaspor(userId, body)
                    }
                    else -> throw IllegalArgumentException("Jenis dokumen tidak dikenal.")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Gagal menghubungi server: ${e.localizedMessage}", e)
                return@withContext "error: Gagal menghubungi server. ${e.localizedMessage}"
            }

            apiResponse.result
        } catch (e: Exception) {
            Log.e(TAG, "Kesalahan pemrosesan gambar: ${e.localizedMessage}", e)
            "error: ${e.localizedMessage}"
        }
    }
}

private fun identifyDocumentType(text: String): String {
    return when {
        text.contains("NIK", ignoreCase = true) -> "ktp"
        text.contains("PASSPORT", ignoreCase = true) -> "paspor"
        else -> "unknown"
    }
}

interface KtpApiService {
    @Multipart
    @POST("uploadKtp")
    suspend fun uploadKtp(
        @Part userId: MultipartBody.Part,
        @Part image: MultipartBody.Part
    ): ApiResponse
}

interface PasporApiService {
    @Multipart
    @POST("uploadPaspor")
    suspend fun uploadPaspor(
        @Part userId: MultipartBody.Part,
        @Part image: MultipartBody.Part
    ): ApiResponse
}

data class ApiResponse(val result: String)

private fun createApiService(context: Context, baseUrl: String, serviceClass: Class<*>): Any {
    val httpClient = OkHttpClient.Builder()
        .cache(Cache(File(context.cacheDir, "http_cache"), 10 * 1024 * 1024)) // Cache 10MB
        .build()

    return Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(serviceClass)
}

private fun ktpApiService(context: Context): KtpApiService =
    createApiService(context, "https://ktpdev.panorasnap.com/", KtpApiService::class.java) as KtpApiService

private fun pasporApiService(context: Context): PasporApiService =
    createApiService(context, "https://paspordev.panorasnap.com/", PasporApiService::class.java) as PasporApiService
