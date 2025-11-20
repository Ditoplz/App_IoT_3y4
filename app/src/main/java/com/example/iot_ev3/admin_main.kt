package com.example.iot_ev3

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class admin_main : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_admin_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. Recuperar los datos del usuario que vienen desde el login
        val userId = intent.getStringExtra("id") ?: ""
        val userType = intent.getStringExtra("tipo") ?: ""

        // 2. Obtener las referencias a las vistas de Logs
        val imgLogs = findViewById<ImageView>(R.id.img_admin_main_logs)
        val txtLogs = findViewById<TextView>(R.id.txt_admin_main_logs)

        // 3. Crear una función para navegar a la pantalla de logs
        val irALogs = {
            val intentLogs = Intent(this, logs::class.java)
            // 4. Pasar los datos del usuario a la pantalla de logs
            intentLogs.putExtra("id", userId)
            intentLogs.putExtra("tipo", userType)
            startActivity(intentLogs)
        }

        // 5. Asignar la función a los listeners de click
        imgLogs.setOnClickListener { irALogs() }
        txtLogs.setOnClickListener { irALogs() }
    }
}