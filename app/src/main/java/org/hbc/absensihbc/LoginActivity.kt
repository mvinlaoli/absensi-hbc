package org.hbc.absensihbc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    // Sandi pengurus (hardcode)
    private val SANDI_PENGURUS = "hbc2026"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val edtSandi = findViewById<EditText>(R.id.edtSandi)
        val btnMasuk = findViewById<Button>(R.id.btnMasuk)
        val lblError = findViewById<TextView>(R.id.lblError)

        btnMasuk.setOnClickListener {
            val sandi = edtSandi.text.toString().trim()
            if (sandi.isEmpty()) {
                lblError.text = "Sandi tidak boleh kosong"
                return@setOnClickListener
            }
            if (sandi == SANDI_PENGURUS) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                lblError.text = "Sandi salah. Coba lagi."
                edtSandi.setText("")
            }
        }
    }
}