package com.example.ocrmaznun.ui.result

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import org.json.JSONObject
import com.example.ocrmaznun.R
import com.example.ocrmaznun.ui.theme.DarkBlue
import com.example.ocrmaznun.ui.theme.DarkSurface
import com.example.ocrmaznun.ui.theme.Gray
import com.example.ocrmaznun.ui.theme.WhitePrimary

@Composable
fun ResultScreen(ocrResult: String, navController: NavController) {
    val parsedResult = parseResult(ocrResult)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Gray, DarkBlue)
                )
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Hasil OCR",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = WhitePrimary
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (parsedResult.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    parsedResult.forEach { (key, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = WhitePrimary
                                )
                            )
                            Text(
                                text = value,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = WhitePrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = WhitePrimary.copy(alpha = 0.4f))
                    }
                }
            }
        } else {
            Text(
                text = "Tidak dapat memproses hasil.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = WhitePrimary
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("scan") },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = WhitePrimary,
                contentColor = DarkBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Kembali",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun parseResult(result: String): Map<String, String> {
    return try {
        val jsonObject = JSONObject(result)
        when {
            jsonObject.has("id") -> {
                mapOf(
                    "ID" to jsonObject.optString("id"),
                    "Nama" to jsonObject.optString("name"),
                    "Tanggal Lahir" to jsonObject.optString("birthdate"),
                    "Alamat" to jsonObject.optString("address"),
                    "RT/RW" to jsonObject.optString("RT/RW"),
                    "Provinsi" to jsonObject.optString("province"),
                    "Kota/Kabupaten" to jsonObject.optString("city"),
                    "Kecamatan" to jsonObject.optString("kecamatan"),
                    "Kelurahan" to jsonObject.optString("kelurahan")
                )
            }
            jsonObject.has("fullName") -> {
                mapOf(
                    "Nama Lengkap" to jsonObject.optString("fullName"),
                    "Nomor Dokumen" to jsonObject.optString("documentNumber"),
                    "Tanggal Lahir" to jsonObject.optString("birthDate"),
                    "Negara" to jsonObject.optString("countryFullName")
                )
            }
            else -> {
                Log.d("ResultScreen", "Data tidak sesuai dengan format KTP atau Paspor")
                emptyMap()
            }
        }
    } catch (e: Exception) {
        Log.e("ResultScreen", "Error parsing result: ${e.localizedMessage}", e)
        emptyMap()
    }
}

