package org.hbc.absensihbc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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

            showKonfirmasiDialog(nim, kegiatan)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun showKonfirmasiDialog(nim: String, kegiatan: String) {
        ApiClient.lookup(nim) { json ->
            runOnUiThread {
                if (json == null || !json.optBoolean("success")) {
                    lblStatus.text = "❌ NIM tidak ditemukan"
                    return@runOnUiThread
                }

                val nama = json.optString("nama")
                val isLatihanHBC = kegiatan.equals("Latihan HBC", ignoreCase = true)

                val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_scan_confirm, null)
                val lblNama = dialogView.findViewById<TextView>(R.id.lblNamaKonfirmasi)
                val edtJam = dialogView.findViewById<EditText>(R.id.edtJamKonfirmasi)
                val edtCp = dialogView.findViewById<EditText>(R.id.edtCpKonfirmasi)

                lblNama.text = "✅ $nama\n$kegiatan"

                if (isLatihanHBC) {
                    edtJam.setText("2")
                    edtCp.setText("0.2")
                } else {
                    edtJam.setText("")
                    edtCp.setText("")
                }

                AlertDialog.Builder(this)
                    .setTitle("Konfirmasi Kehadiran")
                    .setView(dialogView)
                    .setPositiveButton("SIMPAN") { _, _ ->
                        val jamStr = edtJam.text.toString().trim()
                        val cpStr = edtCp.text.toString().trim()

                        if (jamStr.isNotEmpty() && jamStr.toIntOrNull() == null) {
                            Toast.makeText(this, "Jam harus angka bulat", Toast.LENGTH_SHORT).show()
                            return@setPositiveButton
                        }

                        lblStatus.text = "Menyimpan..."
                        ApiClient.catatAbsensi(nim, kegiatan, "Hadir", "", jamStr, cpStr) { resp ->
                            runOnUiThread {
                                lblStatus.text = if (resp != null && resp.optBoolean("success")) {
                                    "✅ Hadir: ${resp.optString("nama")}"
                                } else {
                                    "❌ Gagal: ${resp?.optString("message") ?: "Tidak ada respon"}"
                                }
                            }
                        }
                    }
                    .setNegativeButton("BATAL", null)
                    .show()
            }
        }
    }

    private fun showTambahKegiatanDialog() {
        val input = EditText(this)
        input.hint = "Nama kegiatan baru"

        AlertDialog.Builder(this)
            .setTitle("Tambah Kegiatan")
            .setView(input)
            .setPositiveButton("SIMPAN") { _, _ ->
                val nama = input.text.toString().trim()
                if (nama.isEmpty()) {
                    spinnerKegiatan.setSelection(0)
                    return@setPositiveButton
                }
                ApiClient.tambahKegiatan(nama, "Acara") { json ->
                    runOnUiThread {
                        if (json != null && json.optBoolean("success")) {
                            Toast.makeText(this, "Kegiatan ditambahkan", Toast.LENGTH_SHORT).show()
                            loadKegiatan()
                        }
                    }
                }
            }
            .setNegativeButton("BATAL") { _, _ -> spinnerKegiatan.setSelection(0) }
            .setOnCancelListener { spinnerKegiatan.setSelection(0) }
            .show()
    }
}