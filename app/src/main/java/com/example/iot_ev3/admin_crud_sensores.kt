package com.example.iot_ev3

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.pedant.SweetAlert.SweetAlertDialog
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

data class SensorInfo(
    val idSensor: Int,
    val nombreCompleto: String,
    val rut: String,
    val departamento: String,
    val codigoSensor: String,
    val tipoSensor: String,
    val estadoSensor: String
)

class SensoresAdapter(
    private val sensores: List<SensorInfo>,
    private val onSensorClicked: (SensorInfo) -> Unit
) : RecyclerView.Adapter<SensoresAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreUsuario: TextView = view.findViewById(R.id.txt_item_sensor_nombre_usuario)
        val rutUsuario: TextView = view.findViewById(R.id.txt_item_sensor_rut_usuario)
        val depto: TextView = view.findViewById(R.id.txt_item_sensor_depto)
        val codigoSensor: TextView = view.findViewById(R.id.txt_item_sensor_codigo)
        val tipoSensor: TextView = view.findViewById(R.id.txt_item_sensor_tipo)
        val estadoSensor: TextView = view.findViewById(R.id.txt_item_sensor_estado_sensor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sensor, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val sensor = sensores[position]
        holder.nombreUsuario.text = sensor.nombreCompleto
        holder.rutUsuario.text = sensor.rut
        holder.depto.text = "Depto: ${sensor.departamento}"
        holder.codigoSensor.text = "Código: ${sensor.codigoSensor}"
        holder.tipoSensor.text = "Tipo: ${sensor.tipoSensor}"
        holder.estadoSensor.text = if (sensor.estadoSensor == "1") "Activo" else "Inactivo"

        val color = if (sensor.estadoSensor == "1") Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        holder.estadoSensor.setBackgroundColor(color)

        holder.itemView.setOnClickListener { onSensorClicked(sensor) }
    }

    override fun getItemCount() = sensores.size
}

class admin_crud_sensores : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SensoresAdapter
    private var sensorList = mutableListOf<SensorInfo>()
    private lateinit var adminId: String
    private lateinit var adminType: String

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            consultarSensores("")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_crud_sensores)

        adminId = intent.getStringExtra("id") ?: ""
        adminType = intent.getStringExtra("tipo") ?: ""

        val btnVolver = findViewById<ImageView>(R.id.img_admin_crud_sensores_volver)
        val btnAgregar = findViewById<Button>(R.id.btn_admin_crud_sensores_agregar_sensor)
        val searchView = findViewById<SearchView>(R.id.input_admin_crud_sensores_buscar_usuario)

        requestQueue = Volley.newRequestQueue(this)

        recyclerView = findViewById(R.id.view_admin_crud_sensores_lista)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = SensoresAdapter(sensorList) { sensor ->
            mostrarDialogoCambiarEstado(sensor)
        }
        recyclerView.adapter = adapter

        btnVolver.setOnClickListener {
            finish()
        }

        btnAgregar.setOnClickListener {
            val intent = Intent(this, admin_agregar_sensor::class.java)
            intent.putExtra("id", adminId)
            intent.putExtra("tipo", adminType)
            activityLauncher.launch(intent)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                consultarSensores(query ?: "")
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                consultarSensores(newText ?: "")
                return true
            }
        })

        consultarSensores("")
    }

    private fun consultarSensores(query: String) {
        val url = "http://34.206.51.125/api_crud_sensores.php?admin_id=$adminId&query=$query"
        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                sensorList.clear()
                for (i in 0 until response.length()) {
                    val sensorJson: JSONObject = response.getJSONObject(i)
                    val sensor = SensorInfo(
                        idSensor = sensorJson.getInt("idSensor"),
                        nombreCompleto = "${sensorJson.getString("nombres")} ${sensorJson.getString("apellido_p")} ${sensorJson.getString("apellido_m")}",
                        rut = sensorJson.getString("rut"),
                        departamento = "${sensorJson.getString("torre")}${sensorJson.getString("numero_depto")}",
                        codigoSensor = sensorJson.getString("codigo"),
                        tipoSensor = sensorJson.getString("tipo_sensor"),
                        estadoSensor = sensorJson.getString("estado_sensor")
                    )
                    sensorList.add(sensor)
                }
                adapter.notifyDataSetChanged()
            },
            { error ->
                error.printStackTrace()
                SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("Error").setContentText("No se pudieron cargar los sensores. Error: ${error.message}").show()
            }
        )
        requestQueue.add(jsonArrayRequest)
    }

    private fun mostrarDialogoCambiarEstado(sensor: SensorInfo) {
        val estadoActual = if (sensor.estadoSensor == "1") "Activo" else "Inactivo"
        val nuevoEstadoTexto = if (sensor.estadoSensor == "1") "Inactivo" else "Activo"
        val nuevoEstadoValor = if (sensor.estadoSensor == "1") "0" else "1"

        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Cambiar Estado")
            .setContentText("El estado actual del sensor es '$estadoActual'. ¿Desea cambiarlo a '$nuevoEstadoTexto'?")
            .setConfirmText("Sí, cambiar")
            .setCancelText("No")
            .setConfirmClickListener { sDialog ->
                sDialog.dismissWithAnimation()
                actualizarEstadoSensor(sensor.idSensor, nuevoEstadoValor)
            }
            .show()
    }

    private fun actualizarEstadoSensor(idSensor: Int, nuevoEstado: String) {
        val url = "http://34.206.51.125/api_editar_estado_sensor.php"
        val loadingDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE).apply {
            titleText = "Actualizando..."
            setCancelable(false)
            show()
        }

        val stringRequest = object : StringRequest(Method.POST, url,
            { response ->
                loadingDialog.dismiss()
                try {
                    val jsonResponse = JSONObject(response)
                    if (jsonResponse.getInt("estado") == 1) {
                        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("¡Éxito!")
                            .setContentText(jsonResponse.getString("mensaje"))
                            .setConfirmClickListener { 
                                it.dismissWithAnimation()
                                consultarSensores("") // Refrescar la lista
                            }
                            .show()
                    } else {
                        mostrarError(jsonResponse.getString("mensaje"))
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
                params["id_sensor"] = idSensor.toString()
                params["nuevo_estado"] = nuevoEstado
                return params
            }
        }
        requestQueue.add(stringRequest)
    }

    private fun mostrarError(mensaje: String) {
        SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE).setTitleText("Error").setContentText(mensaje).show()
    }
}