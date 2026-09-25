package org.hbc.absensihbc

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class ScanActivity : AppCompatActivity() {
    private lateinit var spinnerKegiatan: Spinner
    private lateinit var lblStatus: TextView
    private var listKegiatan = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        lblStatus = findViewById(R.id.lblStatus)

        // Tombol X → kembali
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadKegiatan()

        findViewById<Button>(R.id.btnMulaiScan).setOnClickListener {
            IntentIntegrator(this).setOrientationLocked(false).initiateScan()
        }
    }

    private fun loadKegiatan() {
        val defaultList = listOf("Latihan HBC", "Natal", "Wisuda", "+ Tambah Kegiatan Baru")
        spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, defaultList)

        ApiClient.daftarKegiatan { list ->
            if (list.isNotEmpty()) {
                runOnUiThread {
                    listKegiatan = list.toMutableList()
                    listKegiatan.add("+ Tambah Kegiatan Baru")
                    spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKegiatan)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val nim = result.contents
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: "Latihan HBC"

            if (kegiatan == "+ Tambah Kegiatan Baru") {
                showTambahKegiatanDialog()
                return
            }

            lblStatus.text = "Memproses..."
            ApiClient.catatAbsensi(nim, kegiatan, "Hadir") { json ->
                runOnUiThread {
                    lblStatus.text = if (json != null && json.optBoolean("success")) {
                        "✅ Hadir: ${json.optString("nama")}"
                    } else {
                        "❌ Gagal: ${json?.optString("message") ?: "Tidak ada respon"}"
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showTambahKegiatanDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Nama kegiatan baru"

        AlertDialog.Builder(this)
            .setTitle("Tambah Kegiatan")
            .setView(input)
            .setPositiveButton("SIMPAN") { _, _ ->
                val nama = input.text.toString().trim()
                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                ApiClient.tambahKegiatan(nama, "Acara") { json ->
                    runOnUiThread {
                        if (json != null && json.optBoolean("success")) {
                            Toast.makeText(this, "Kegiatan ditambahkan", Toast.LENGTH_SHORT).show()
                            loadKegiatan()
                        } else {
                            Toast.makeText(this, "Gagal tambah kegiatan", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            .setNegativeButton("BATAL", null)
            .show()
    }
}