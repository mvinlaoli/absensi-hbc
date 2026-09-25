package org.hbc.absensihbc

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult

class ScanActivity : AppCompatActivity() {
    private lateinit var spinnerKegiatan: Spinner
    private lateinit var lblStatus: TextView
    private var listKegiatan: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan)

        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        lblStatus = findViewById(R.id.lblStatus)

        loadKegiatan()

        findViewById<Button>(R.id.btnMulaiScan).setOnClickListener {
            IntentIntegrator(this).setOrientationLocked(false).initiateScan()
        }
    }

    private fun loadKegiatan() {
        listKegiatan = mutableListOf("Latihan HBC", "Natal", "Wisuda", "+ Tambah Kegiatan Baru")
        spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKegiatan)
        spinnerKegiatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (listKegiatan[position] == "+ Tambah Kegiatan Baru") {
                    showTambahKegiatanDialog()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        ApiClient.daftarKegiatan { list ->
            if (list.isNotEmpty()) runOnUiThread {
                listKegiatan = (list + "+ Tambah Kegiatan Baru").toMutableList()
                spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKegiatan)
            }
        }
    }

    private fun showTambahKegiatanDialog() {
        val input = EditText(this)
        input.hint = "Nama kegiatan baru"
        AlertDialog.Builder(this)
            .setTitle("Tambah Kegiatan")
            .setView(input)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = input.text.toString().trim()
                if (nama.isNotEmpty()) {
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
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            val nim = result.contents
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: "Latihan HBC"
            if (kegiatan.startsWith("+")) {
                lblStatus.text = "Pilih kegiatan dulu"
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
        } else super.onActivityResult(requestCode, resultCode, data)
    }
}