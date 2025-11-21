package com.example.iot_ev3

import android.app.Activity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class admin_agregar_usuario : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var adminId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_agregar_usuario)

        adminId = intent.getStringExtra("id") ?: ""

        val btnVolver = findViewById<ImageView>(R.id.img_admin_agregar_usuario_volver)
        val btnGuardar = findViewById<Button>(R.id.btn_admin_agregar_usuario_registrar_usuario)
        val spinnerEstado = findViewById<Spinner>(R.id.spinner_admin_agregar_usuario_estado)

        requestQueue = Volley.newRequestQueue(this)

        ArrayAdapter.createFromResource(
            this,
            R.array.opciones_estado_usuario,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinnerEstado.adapter = adapter
        }

        btnVolver.setOnClickListener {
            finish()
        }

        btnGuardar.setOnClickListener {
            if (validarCampos()) {
                registrarUsuario()
            }
        }
    }

    private fun validarCampos(): Boolean {
        val nombres = findViewById<EditText>(R.id.input_admin_agregar_usuario_nombres).text.toString().trim()
        val apellidoP = findViewById<EditText>(R.id.input_admin_agregar_usuario_apellido_paterno).text.toString().trim()
        val apellidoM = findViewById<EditText>(R.id.input_admin_agregar_usuario_apellido_materno).text.toString().trim()
        val rut = findViewById<EditText>(R.id.input_admin_agregar_usuario_rut).text.toString().trim()
        val pass1 = findViewById<EditText>(R.id.input_admin_agregar_usuario_clave).text.toString()
        val pass2 = findViewById<EditText>(R.id.input_admin_agregar_usuario_repetir_clave).text.toString()
        val estadoPos = findViewById<Spinner>(R.id.spinner_admin_agregar_usuario_estado).selectedItemPosition

        if (nombres.isEmpty() || apellidoP.isEmpty() || apellidoM.isEmpty() || rut.isEmpty() || pass1.isEmpty()) {
            mostrarError("Todos los campos son obligatorios.")
            return false
        }

        if (!nombres.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()) || nombres.length > 45) {
            mostrarError("El campo 'Nombres' solo debe contener letras y un máximo de 45 caracteres.")
            return false
        }
        if (!apellidoP.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()) || apellidoP.length > 45) {
            mostrarError("El campo 'Apellido Paterno' solo debe contener letras y un máximo de 45 caracteres.")
            return false
        }
        if (!apellidoM.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$".toRegex()) || apellidoM.length > 45) {
            mostrarError("El campo 'Apellido Materno' solo debe contener letras y un máximo de 45 caracteres.")
            return false
        }

        if (!rut.matches("^\\d{7,8}-[\\dkK]$".toRegex())) {
            mostrarError("El formato del RUT no es válido (Ej: 12345678-9).")
            return false
        }

        if (pass1 != pass2) {
            mostrarError("Las contraseñas no coinciden.")
            return false
        }

        if (estadoPos == 0) {
            mostrarError("Debe seleccionar un estado para el usuario.")
            return false
        }

        return true
    }

    private fun registrarUsuario() {
        val url = "http://34.206.51.125/api_agregar_usuario.php"
        val nombres = findViewById<EditText>(R.id.input_admin_agregar_usuario_nombres).text.toString().trim()
        val apellidoP = findViewById<EditText>(R.id.input_admin_agregar_usuario_apellido_paterno).text.toString().trim()
        val apellidoM = findViewById<EditText>(R.id.input_admin_agregar_usuario_apellido_materno).text.toString().trim()
        val rut = findViewById<EditText>(R.id.input_admin_agregar_usuario_rut).text.toString().trim()
        val clave = findViewById<EditText>(R.id.input_admin_agregar_usuario_clave).text.toString()
        val estadoSpinner = findViewById<Spinner>(R.id.spinner_admin_agregar_usuario_estado).selectedItem.toString()
        val estado = if (estadoSpinner == "Activo") "1" else "0"

        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
        loadingDialog.titleText = "Registrando..."
        loadingDialog.setCancelable(false)
        loadingDialog.show()

        val stringRequest = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                loadingDialog.dismiss()
                try {
                    val jsonResponse = JSONObject(response)
                    val estadoRespuesta = jsonResponse.getInt("estado")
                    val mensaje = jsonResponse.getString("mensaje")

                    if (estadoRespuesta == 1) {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Éxito!")
                            .setContentText(mensaje)
                            .setConfirmClickListener { sDialog ->
                                sDialog.dismissWithAnimation()
                                setResult(Activity.RESULT_OK)
                                finish()
                            }
                            .show()
                    } else {
                        mostrarError(mensaje)
                    }
                } catch (e: Exception) {
                    mostrarError("Error al procesar la respuesta del servidor.")
                }
            },
            { error ->
                loadingDialog.dismiss()
                mostrarError("Error de red: ${error.message}")
            }) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["admin_id"] = adminId
                params["nombres"] = nombres
                params["apellido_p"] = apellidoP
                params["apellido_m"] = apellidoM
                params["rut"] = rut
                params["clave"] = clave
                params["estado"] = estado
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun mostrarError(mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
            .setTitleText("Error de Validación")
            .setContentText(mensaje)
            .show()
    }
}