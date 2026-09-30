package org.hbc.absensihbc

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
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
    private lateinit var lblCp: TextView
    private var listAnggota = mutableListOf<JSONObject>()
    private var listKegiatan = mutableListOf<String>()

    // Default CP untuk Latihan HBC
    private val CP_LATIHAN_HBC = 0.2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jamcp)

        spinnerAnggota = findViewById(R.id.spinnerAnggota)
        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        edtJam = findViewById(R.id.edtJam)
        edtCp = findViewById(R.id.edtCp)
        lblStatus = findViewById(R.id.lblStatusJamCp)
        lblCp = findViewById(R.id.lblCp)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            finish()
        }

        loadAnggota()
        loadKegiatan()

        // Listener: kalau ganti kegiatan → auto isi CP
        spinnerKegiatan.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = spinnerKegiatan.selectedItem?.toString() ?: return

                if (selected == "+ Tambah Kegiatan Baru") {
                    showTambahKegiatanDialog()
                    return
                }

                // Auto fill CP kalau Latihan HBC
                if (selected.equals("Latihan HBC", ignoreCase = true)) {
                    edtCp.setText(CP_LATIHAN_HBC.toString())
                    edtCp.isEnabled = false
                    edtCp.alpha = 0.6f
                    lblCp.text = "CP (otomatis untuk Latihan HBC)"
                } else {
                    edtCp.isEnabled = true
                    edtCp.alpha = 1.0f
                    edtCp.setText("")
                    lblCp.text = "CP (input manual)"
                }
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btnSimpanJamCp).setOnClickListener {
            val pos = spinnerAnggota.selectedItemPosition
            if (pos < 0 || pos >= listAnggota.size) {
                Toast.makeText(this, "Pilih anggota dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nim = listAnggota[pos].optString("nim")
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""

            // Jam: integer (angka bulat)
            val jamText = edtJam.text.toString().trim()
            val jam = if (jamText.isEmpty()) 0 else {
                val j = jamText.toIntOrNull()
                if (j == null) {
                    Toast.makeText(this, "Jam harus angka bulat (tanpa koma)", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                j
            }

            // CP: untuk Latihan HBC = 0.2 (fixed), Event = manual
            val cp: Double = if (kegiatan.equals("Latihan HBC", ignoreCase = true)) {
                CP_LATIHAN_HBC
            } else {
                edtCp.text.toString().toDoubleOrNull() ?: 0.0
            }

            if (kegiatan == "+ Tambah Kegiatan Baru") {
                Toast.makeText(this, "Pilih kegiatan dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (jam <= 0) {
                Toast.makeText(this, "Isi Jam dulu", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lblStatus.text = "Menyimpan..."
            ApiClient.catatJamCp(nim, kegiatan, jam.toDouble(), cp.toInt()) { json ->
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
        val input = EditText(this)
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