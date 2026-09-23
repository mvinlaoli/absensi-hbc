package org.hbc.absensihbc

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class ScanActivity : AppCompatActivity() {
    private lateinit var spinnerKegiatan: Spinner
    private lateinit var lblStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        lblStatus = findViewById(R.id.lblStatus)

        val defaultList = listOf("Latihan HBC", "Natal", "Wisuda")
        spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, defaultList)

        // Coba ambil daftar terbaru dari sheet "Riwayat Jenis Kegiatan"; kalau gagal, tetap pakai daftar default di atas
        ApiClient.daftarKegiatan { list ->
            if (list.isNotEmpty()) {
                runOnUiThread {
                    spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list)
                }
            }
        }

        findViewById<Button>(R.id.btnMulaiScan).setOnClickListener {
            IntentIntegrator(this).setOrientationLocked(false).initiateScan()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val nim = result.contents
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: "Latihan HBC"
            lblStatus.text = "Memproses..."
            ApiClient.catatAbsensi(nim, kegiatan, "Hadir") { json ->
                runOnUiThread {
                    lblStatus.text = if (json != null && json.optBoolean("success")) {
                        "Hadir: ${json.optString("nama")}"
                    } else {
                        "Gagal: ${json?.optString("message") ?: "Tidak ada respon dari server"}"
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }
}
