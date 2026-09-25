package org.hbc.absensihbc

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class ManualActivity : AppCompatActivity() {
    private lateinit var spinnerNama: Spinner
    private lateinit var spinnerKegiatan: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var lblStatus: TextView
    private var listAnggota: List<JSONObject> = emptyList()
    private var listKegiatan: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        spinnerNama = findViewById(R.id.spinnerNama)
        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        lblStatus = findViewById(R.id.lblStatusManual)

        // Load anggota
        ApiClient.listAnggota { list ->
            runOnUiThread {
                listAnggota = list
                val namaList = list.map { "${it.optString("nama")} - ${it.optString("nim")}" }
                spinnerNama.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, namaList)
            }
        }

        // Load kegiatan
        loadKegiatan()

        // Spinner status
        spinnerStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Hadir", "Izin", "Sakit", "Alpa"))

        findViewById<Button>(R.id.btnSimpanManual).setOnClickListener {
            val pos = spinnerNama.selectedItemPosition
            if (pos < 0 || pos >= listAnggota.size) {
                lblStatus.text = "Pilih nama dulu"
                return@setOnClickListener
            }
            val nim = listAnggota[pos].optString("nim")
            val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""
            val status = spinnerStatus.selectedItem?.toString() ?: "Izin"

            if (kegiatan.startsWith("+")) {
                lblStatus.text = "Pilih kegiatan dulu"
                return@setOnClickListener
            }

            lblStatus.text = "Menyimpan..."
            ApiClient.catatAbsensi(nim, kegiatan, status) { json ->
                runOnUiThread {
                    lblStatus.text = if (json != null && json.optBoolean("success")) {
                        "✅ $status tercatat untuk ${json.optString("nama")}"
                    } else {
                        "❌ Gagal: ${json?.optString("message") ?: "Tidak ada respon"}"
                    }
                }
            }
        }
    }

    private fun loadKegiatan() {
        listKegiatan = mutableListOf("Latihan HBC", "Natal", "Wisuda", "+ Tambah Kegiatan Baru")
        spinnerKegiatan.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listKegiatan)
        spinnerKegiatan.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                if (listKegiatan[position] == "+ Tambah Kegiatan Baru") showTambahKegiatanDialog()
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
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}