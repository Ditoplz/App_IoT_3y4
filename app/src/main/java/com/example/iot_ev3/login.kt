package com.example.iot_ev3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONException

class login : AppCompatActivity() {

    private lateinit var usu: EditText
    private lateinit var clave: EditText
    private lateinit var btn: Button
    private lateinit var datos: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        usu = findViewById(R.id.input_login_rut)
        clave = findViewById(R.id.input_login_pass)
        btn = findViewById(R.id.btn_login_ingresar)
        datos = Volley.newRequestQueue(this)

        btn.setOnClickListener {
            val userText = usu.text.toString()
            val passText = clave.text.toString()
            if (userText.isNotEmpty() && passText.isNotEmpty()) {
                consultarDatos(userText, passText)
            } else {
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de Validación")
                    .setContentText("Por favor, complete todos los campos.")
                    .setConfirmText("Aceptar")
                    .show()
            }
        }
    }

    private fun consultarDatos(usu: String, pass: String) {
        val url = "http://34.206.51.125/apiconsultausu.php?usu=$usu&pass=$pass"
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val estado = response.getString("estado")
                    if (estado == "0") {
                        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Acceso Denegado")
                            .setContentText("Asegúrse que los datos ingresados sean correctos y de tener permisos para acceder.")
                            .setConfirmText("Aceptar")
                            .show()
                    } else {
                        val id = response.getString("id")
                        val tipo = response.getString("tipo")

                        when (tipo) {
                            "admin" -> {
                                val intent = Intent(this, admin_main::class.java)
                                intent.putExtra("id", id)
                                intent.putExtra("tipo", tipo)
                                startActivity(intent)
                            }
                            "operador" -> {
                                val intent = Intent(this, operador_main::class.java)
                                intent.putExtra("id", id)
                                intent.putExtra("tipo", tipo)
                                startActivity(intent)
                            }
                            else -> {
                                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error de Rol")
                                    .setContentText("El rol de usuario no es reconocido.")
                                    .setConfirmText("Aceptar")
                                    .show()
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error de Respuesta")
                        .setContentText("El formato de la respuesta del servidor es incorrecto.")
                        .setConfirmText("Aceptar")
                        .show()
                }
            },
            { error ->
                error.printStackTrace()
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error de Red")
                    .setContentText("No se pudo conectar con el servidor. Verifique su conexión.")
                    .setConfirmText("Aceptar")
                    .show()
            }
        )
        datos.add(request)
    }
}