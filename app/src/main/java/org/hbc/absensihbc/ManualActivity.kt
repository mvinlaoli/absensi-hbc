package org.hbc.absensihbc

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class ManualActivity : AppCompatActivity() {
    private lateinit var spinnerAnggota: Spinner
    private lateinit var spinnerKegiatan: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var lblStatus: TextView
    private var listAnggota = mutableListOf<JSONObject>()
    private var listKegiatan = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        spinnerAnggota = findViewById(R.id.spinnerAnggota)
        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        lblStatus = findViewById(R.id.lblStatusManual)

        // Tombol X → kembali
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadAnggota()
        loadKegiatan()

        spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Hadir", "Izin", "Sakit", "Alpa")
        )

        findViewById<Button>(R.id.btnSimpanManual).setOnClickListener {
            val pos = spinnerAnggota.selectedItemPosition
            if (pos < 0 || pos >= listAnggota.size) {
                Toast.makeText(this, "Pilih anggota dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nim = listAnggota[pos].optString("nim")
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""
            val status = spinnerStatus.selectedItem?.toString() ?: "Izin"

            if (kegiatan == "+ Tambah Kegiatan Baru") {
                showTambahKegiatanDialog()
                return@setOnClickListener
            }

            lblStatus.text = "Menyimpan..."
            ApiClient.catatAbsensi(nim, kegiatan, status) { json ->
                runOnUiThread {
                    lblStatus.text = if (json != null && json.optBoolean("success")) {
                        "✅ $status: ${json.optString("nama")}"
                    } else {
                        "❌ Gagal: ${json?.optString("message") ?: "Tidak ada respon"}"
                    }
                }
            }
        }
    }

    private fun loadAnggota() {
        ApiClient.daftarAnggota { list ->
            runOnUiThread {
                if (list.isNotEmpty()) {
                    listAnggota = list.toMutableList()
                    val namaList = list.map { "${it.optString("nama")} (${it.optString("nim")})" }
                    spinnerAnggota.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, namaList)
                } else {
                    Toast.makeText(this, "Data anggota kosong", Toast.LENGTH_SHORT).show()
                }
            }
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

    private fun showTambahKegiatanDialog() {
        val input = android.widget.EditText(this)
        input.hint = "Nama kegiatan baru"

        AlertDialog.Builder(this)
            .setTitle("Tambah Kegiatan")
            .setView(input)
            .setPositiveButton("SIMPAN") { _, _ ->
                val nama = input.text.toString().trim()
                if (nama.isEmpty()) return@setPositiveButton
                ApiClient.tambahKegiatan(nama, "Acara") { json ->
                    runOnUiThread {
                        if (json != null && json.optBoolean("success")) {
                            Toast.makeText(this, "Ditambahkan", Toast.LENGTH_SHORT).show()
                            loadKegiatan()
                        }
                    }
                }
            }
            .setNegativeButton("BATAL", null)
            .show()
    }
}