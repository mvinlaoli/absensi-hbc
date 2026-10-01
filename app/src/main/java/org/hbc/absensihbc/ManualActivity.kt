package org.hbc.absensihbc

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ManualActivity : AppCompatActivity() {
    private lateinit var spinnerAnggota: Spinner
    private lateinit var spinnerKegiatan: Spinner
    private lateinit var spinnerStatus: Spinner
    private lateinit var edtJam: EditText
    private lateinit var edtCp: EditText
    private lateinit var btnTanggal: Button
    private lateinit var layoutJamCp: LinearLayout
    private lateinit var lblStatus: TextView

    private var listAnggota = mutableListOf<JSONObject>()
    private var listKegiatan = mutableListOf<String>()
    private var selectedDate = Calendar.getInstance()

    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormatter = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        spinnerAnggota = findViewById(R.id.spinnerAnggota)
        spinnerKegiatan = findViewById(R.id.spinnerKegiatan)
        spinnerStatus = findViewById(R.id.spinnerStatus)
        edtJam = findViewById(R.id.edtJam)
        edtCp = findViewById(R.id.edtCp)
        btnTanggal = findViewById(R.id.btnTanggal)
        layoutJamCp = findViewById(R.id.layoutJamCp)
        lblStatus = findViewById(R.id.lblStatusManual)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        updateTanggalButton()
        btnTanggal.setOnClickListener { showDatePicker() }

        loadAnggota()
        loadKegiatan()

        spinnerStatus.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Hadir", "Izin", "Sakit", "Alpa")
        )

        spinnerKegiatan.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selected = spinnerKegiatan.selectedItem?.toString() ?: return
                if (selected == "+ Tambah Kegiatan Baru") {
                    showTambahKegiatanDialog()
                    return
                }
                updateJamCpVisibility()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        spinnerStatus.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                updateJamCpVisibility()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
        }

        findViewById<Button>(R.id.btnSimpanManual).setOnClickListener {
            simpanData()
        }
    }

    private fun updateTanggalButton() {
        btnTanggal.text = displayFormatter.format(selectedDate.time)
    }

    private fun showDatePicker() {
        val dialog = DatePickerDialog(
            this,
            { _, year, month, day ->
                selectedDate.set(year, month, day)
                updateTanggalButton()
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        )
        dialog.datePicker.maxDate = System.currentTimeMillis()
        dialog.show()
    }

    private fun updateJamCpVisibility() {
        val status = spinnerStatus.selectedItem?.toString() ?: ""
        val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""

        if (status == "Hadir" && kegiatan != "+ Tambah Kegiatan Baru" && kegiatan.isNotEmpty()) {
            layoutJamCp.visibility = View.VISIBLE

            val isLatihanHBC = kegiatan.equals("Latihan HBC", ignoreCase = true)
            if (isLatihanHBC) {
                if (edtJam.text.toString().isEmpty()) edtJam.setText("2")
                if (edtCp.text.toString().isEmpty()) edtCp.setText("0.2")
            } else {
                edtJam.setText("")
                edtCp.setText("")
            }
        } else {
            layoutJamCp.visibility = View.GONE
        }
    }

    private fun simpanData() {
        val pos = spinnerAnggota.selectedItemPosition
        if (pos < 0 || pos >= listAnggota.size) {
            Toast.makeText(this, "Pilih anggota dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val nim = listAnggota[pos].optString("nim")
        val kegiatan = spinnerKegiatan.selectedItem?.toString() ?: ""
        val status = spinnerStatus.selectedItem?.toString() ?: "Hadir"

        if (kegiatan == "+ Tambah Kegiatan Baru") {
            Toast.makeText(this, "Pilih kegiatan dulu", Toast.LENGTH_SHORT).show()
            return
        }

        val tanggalStr = dateFormatter.format(selectedDate.time)

        var jamStr = ""
        var cpStr = ""
        if (status == "Hadir" && layoutJamCp.visibility == View.VISIBLE) {
            jamStr = edtJam.text.toString().trim()
            cpStr = edtCp.text.toString().trim()

            if (jamStr.isNotEmpty() && jamStr.toIntOrNull() == null) {
                Toast.makeText(this, "Jam harus angka bulat", Toast.LENGTH_SHORT).show()
                return
            }
        }

        lblStatus.text = "Menyimpan..."

        ApiClient.catatAbsensi(nim, kegiatan, status, tanggalStr, jamStr, cpStr) { json ->
            runOnUiThread {
                lblStatus.text = if (json != null && json.optBoolean("success")) {
                    "✅ $status: ${json.optString("nama")} ($tanggalStr)"
                } else {
                    "❌ Gagal: ${json?.optString("message") ?: "Tidak ada respon"}"
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
                if (nama.isEmpty()) {
                    spinnerKegiatan.setSelection(0)
                    return@setPositiveButton
                }
                ApiClient.tambahKegiatan(nama, "Acara") { json ->
                    runOnUiThread {
                        if (json != null && json.optBoolean("success")) {
                            Toast.makeText(this, "Ditambahkan", Toast.LENGTH_SHORT).show()
                            loadKegiatan()
                        } else {
                            spinnerKegiatan.setSelection(0)
                        }
                    }
                }
            }
            .setNegativeButton("BATAL") { _, _ -> spinnerKegiatan.setSelection(0) }
            .setOnCancelListener { spinnerKegiatan.setSelection(0) }
            .show()
    }
}