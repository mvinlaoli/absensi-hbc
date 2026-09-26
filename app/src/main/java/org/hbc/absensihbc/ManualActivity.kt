package org.hbc.absensihbc

import android.os.Bundle
import android.view.View
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

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadAnggota()
        loadKegiatan()

        spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Hadir", "Izin", "Sakit", "Alpa")
        )

        // Listener: kalau pilih "+ Tambah Kegiatan Baru" → muncul dialog
        spinnerKegiatan.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = spinnerKegiatan.selectedItem?.toString() ?: return
                if (selected == "+ Tambah Kegiatan Baru") {
                    showTambahKegiatanDialog()
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

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
                Toast.makeText(this, "Pilih kegiatan dulu", Toast.LENGTH_SHORT).show()
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
                if (nama.isEmpty()) {
                    Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    spinnerKegiatan.setSelection(0)
                    return@setPositiveButton
                }
                ApiClient.tambahKegiatan(nama, "Acara") { json ->
                    runOnUiThread {
                        if (json != null && json.optBoolean("success")) {
                            Toast.makeText(this, "Kegiatan ditambahkan", Toast.LENGTH_SHORT).show()
                            loadKegiatan()
                        } else {
                            Toast.makeText(this, "Gagal: ${json?.optString("message")}", Toast.LENGTH_SHORT).show()
                            spinnerKegiatan.setSelection(0)
                        }
                    }
                }
            }
            .setNegativeButton("BATAL") { _, _ ->
                spinnerKegiatan.setSelection(0)
            }
            .setOnCancelListener {
                spinnerKegiatan.setSelection(0)
            }
            .show()
    }
}