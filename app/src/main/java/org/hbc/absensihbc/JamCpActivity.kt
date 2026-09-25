package org.hbc.absensihbc

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class JamCpActivity : AppCompatActivity() {
    private lateinit var spinnerAnggota: Spinner
    private lateinit var spinnerKegiatan: Spinner
    private lateinit var edtJam: EditText
    private lateinit var edtCp: EditText
    private lateinit var lblStatus: TextView
    private var listAnggota = mutableListOf<JSONObject>()
    private var listKegiatan = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jamcp)

        spinnerAnggota = findViewById(R.id.spinnerAnggota)
        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        edtJam = findViewById(R.id.edtJam)
        edtCp = findViewById(R.id.edtCp)
        lblStatus = findViewById(R.id.lblStatusJamCp)

        loadAnggota()
        loadKegiatan()

        findViewById<Button>(R.id.btnSimpanJamCp).setOnClickListener {
            val pos = spinnerAnggota.selectedItemPosition
            if (pos < 0 || pos >= listAnggota.size) {
                Toast.makeText(this, "Pilih anggota dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nim = listAnggota[pos].optString("nim")
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""
            val jam = edtJam.text.toString().toDoubleOrNull() ?: 0.0
            val cp = edtCp.text.toString().toIntOrNull() ?: 0

            if (kegiatan == "+ Tambah Kegiatan Baru") {
                showTambahKegiatanDialog()
                return@setOnClickListener
            }
            if (jam <= 0 && cp <= 0) {
                Toast.makeText(this, "Isi Jam atau CP", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lblStatus.text = "Menyimpan..."
            ApiClient.catatJamCp(nim, kegiatan, jam, cp) { json ->
                runOnUiThread {
                    lblStatus.text = if (json != null && json.optBoolean("success")) {
                        "✅ ${json.optString("nama")} (+$jam jam, +$cp CP)"
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
        val input = EditText(this)
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