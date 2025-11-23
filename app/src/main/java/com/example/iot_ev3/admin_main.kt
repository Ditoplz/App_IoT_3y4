package com.example.iot_ev3

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class admin_main : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var countdownText: TextView
    private var countdownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_main)

        val userId = intent.getStringExtra("id") ?: ""
        val userType = intent.getStringExtra("tipo") ?: ""

        requestQueue = Volley.newRequestQueue(this)

        val btnVolver = findViewById<ImageView>(R.id.img_admin_main_volver)
        val imgLogs = findViewById<ImageView>(R.id.img_admin_main_logs)
        val txtLogs = findViewById<TextView>(R.id.txt_admin_main_logs)
        val imgUsuarios = findViewById<ImageView>(R.id.img_admin_main_personas)
        val txtUsuarios = findViewById<TextView>(R.id.txt_admin_main_usuarios)
        val imgSensores = findViewById<ImageView>(R.id.img_admin_main_sensores)
        val txtSensores = findViewById<TextView>(R.id.txt_admin_main_sensores)
        val imgBarrera = findViewById<ImageView>(R.id.img_admin_main_barrera)
        val txtBarrera = findViewById<TextView>(R.id.txt_admin_main_barrera)
        countdownText = findViewById(R.id.txt_admin_main_countdown)

        btnVolver.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Cerrar Sesión?")
                .setContentText("Será redirigido a la pantalla de login.")
                .setConfirmText("Sí")
                .setCancelText("No")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    val intent = Intent(this, login::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .show()
        }

        imgLogs.setOnClickListener {
            val intentLogs = Intent(this, logs::class.java)
            intentLogs.putExtra("id", userId)
            intentLogs.putExtra("tipo", userType)
            startActivity(intentLogs)
        }
        txtLogs.setOnClickListener {
            val intentLogs = Intent(this, logs::class.java)
            intentLogs.putExtra("id", userId)
            intentLogs.putExtra("tipo", userType)
            startActivity(intentLogs)
        }

        imgUsuarios.setOnClickListener {
            val intentUsuarios = Intent(this, admin_crud_usuarios::class.java)
            intentUsuarios.putExtra("id", userId)
            intentUsuarios.putExtra("tipo", userType)
            startActivity(intentUsuarios)
        }
        txtUsuarios.setOnClickListener {
            val intentUsuarios = Intent(this, admin_crud_usuarios::class.java)
            intentUsuarios.putExtra("id", userId)
            intentUsuarios.putExtra("tipo", userType)
            startActivity(intentUsuarios)
        }

        imgSensores.setOnClickListener {
            val intentSensores = Intent(this, admin_crud_sensores::class.java)
            intentSensores.putExtra("id", userId)
            intentSensores.putExtra("tipo", userType)
            startActivity(intentSensores)
        }
        txtSensores.setOnClickListener {
            val intentSensores = Intent(this, admin_crud_sensores::class.java)
            intentSensores.putExtra("id", userId)
            intentSensores.putExtra("tipo", userType)
            startActivity(intentSensores)
        }

        imgBarrera.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Abrir Barrera?")
                .setContentText("¿Desea abrir la barrera de acceso?")
                .setConfirmText("Sí")
                .setCancelText("No")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    generarEventoBarrera(userId)
                }
                .show()
        }
        txtBarrera.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("¿Abrir Barrera?")
                .setContentText("¿Desea abrir la barrera de acceso?")
                .setConfirmText("Sí")
                .setCancelText("No")
                .setConfirmClickListener { sDialog ->
                    sDialog.dismissWithAnimation()
                    generarEventoBarrera(userId)
                }
                .show()
        }
    }

    private fun generarEventoBarrera(idUsuario: String) {
        val url = "http://34.206.51.125/api_generar_evento_barrera.php"
        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            titleText = "Procesando..."
            setCancelable(false)
            show()
        }

        val stringRequest = object : StringRequest(Request.Method.POST, url,
            { response ->
                loadingDialog.dismiss()
                try {
                    val jsonResponse = JSONObject(response)
                    val estado = jsonResponse.getInt("estado")
                    val mensaje = jsonResponse.getString("mensaje")

                    if (estado == 1) {
                        val resultado = jsonResponse.getString("resultado")
                        if (resultado == "Exito") {
                            SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("¡Éxito!")
                                .setContentText("Acceso concedido.")
                                .setConfirmClickListener { sDialog ->
                                    sDialog.dismissWithAnimation()
                                    iniciarContador()
                                }
                                .show()
                        } else {
                            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Acceso Denegado")
                                .setContentText("El usuario no está habilitado para esta acción.")
                                .show()
                        }
                    } else {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("Error").setContentText(mensaje).show()
                    }
                } catch (e: Exception) {
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("Error").setContentText("Error al procesar la respuesta.").show()
                }
            },
            { error ->
                loadingDialog.dismiss()
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("Error de Red").setContentText(error.message).show()
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["id_usuario"] = idUsuario
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun iniciarContador() {
        countdownTimer?.cancel()
        countdownText.visibility = View.VISIBLE

        countdownTimer = object : CountDownTimer(10000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val segundosRestantes = millisUntilFinished / 1000
                countdownText.text = "Cerrando en ${segundosRestantes}..."
            }

            override fun onFinish() {
                countdownText.text = "Barrera Cerrada"
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer?.cancel()
    }
}