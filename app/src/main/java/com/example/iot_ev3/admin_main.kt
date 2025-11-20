package com.example.iot_ev3

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog

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

        val userId = intent.getStringExtra("id") ?: ""
        val userType = intent.getStringExtra("tipo") ?: ""

        val btnVolver = findViewById<ImageView>(R.id.img_admin_main_volver)
        val imgLogs = findViewById<ImageView>(R.id.img_admin_main_logs)
        val txtLogs = findViewById<TextView>(R.id.txt_admin_main_logs)
        val imgUsuarios = findViewById<ImageView>(R.id.img_admin_main_personas)
        val txtUsuarios = findViewById<TextView>(R.id.txt_admin_main_usuarios)
        val imgSensores = findViewById<ImageView>(R.id.img_admin_main_sensores)
        val txtSensores = findViewById<TextView>(R.id.txt_admin_main_sensores)

        btnVolver.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Cerrar Sesión?")
                .setContentText("Será redirigido a la pantalla de login.")
                .setConfirmText("Sí")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    val intent = Intent(this, login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setCancelText("No")
                .setCancelClickListener { sDialog -> sDialog.dismissWithAnimation() }
                .show()
        }

        val irALogs = {
            val intentLogs = Intent(this, logs::class.java)
            intentLogs.putExtra("id", userId)
            intentLogs.putExtra("tipo", userType)
            startActivity(intentLogs)
        }
        imgLogs.setOnClickListener { irALogs() }
        txtLogs.setOnClickListener { irALogs() }

        val irACrudUsuarios = {
            val intentUsuarios = Intent(this, admin_crud_usuarios::class.java)
            intentUsuarios.putExtra("id", userId)
            intentUsuarios.putExtra("tipo", userType)
            startActivity(intentUsuarios)
        }
        imgUsuarios.setOnClickListener { irACrudUsuarios() }
        txtUsuarios.setOnClickListener { irACrudUsuarios() }

        val irACrudSensores = {
            val intentSensores = Intent(this, admin_crud_sensores::class.java)
            intentSensores.putExtra("id", userId)
            intentSensores.putExtra("tipo", userType)
            startActivity(intentSensores)
        }
        imgSensores.setOnClickListener { irACrudSensores() }
        txtSensores.setOnClickListener { irACrudSensores() }
    }
}