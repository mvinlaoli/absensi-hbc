package org.hbc.absensihbc

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnScan).setOnClickListener {
            startActivity(Intent(this, ScanActivity::class.java))
        }
        findViewById<Button>(R.id.btnManual).setOnClickListener {
            startActivity(Intent(this, ManualActivity::class.java))
        }
        findViewById<Button>(R.id.btnJamCp).setOnClickListener {
            startActivity(Intent(this, JamCpActivity::class.java))
        }
    }
}
