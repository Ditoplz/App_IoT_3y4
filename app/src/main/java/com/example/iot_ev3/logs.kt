package com.example.iot_ev3

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.Calendar

data class Evento(
    val nombre: String?,
    val apellidoP: String?,
    val apellidoM: String?,
    val departamento: String?,
    val tipoSensor: String,
    val resultado: String,
    val fecha: String
)

class LogsAdapter(private val eventos: List<Evento>, private val userType: String) : RecyclerView.Adapter<LogsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nombreUsuario: TextView = view.findViewById(R.id.txt_item_nombre_usuario)
        val departamento: TextView = view.findViewById(R.id.txt_item_departamento)
        val tipoSensor: TextView = view.findViewById(R.id.txt_item_tipo_sensor)
        val fecha: TextView = view.findViewById(R.id.txt_item_fecha)
        val resultado: TextView = view.findViewById(R.id.txt_item_resultado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val evento = eventos[position]

        if (userType == "admin") {
            holder.nombreUsuario.visibility = View.VISIBLE
            holder.departamento.visibility = View.VISIBLE
            holder.nombreUsuario.text = "${evento.nombre} ${evento.apellidoP} ${evento.apellidoM}"
            holder.departamento.text = evento.departamento
        } else {
            holder.nombreUsuario.visibility = View.GONE
            holder.departamento.visibility = View.GONE
        }

        holder.tipoSensor.text = evento.tipoSensor
        holder.fecha.text = evento.fecha
        holder.resultado.text = evento.resultado

        val color = if (evento.resultado.equals("Exito", ignoreCase = true)) Color.parseColor("#4CAF50") else Color.parseColor("#F44336")
        holder.resultado.setBackgroundColor(color)
    }

    override fun getItemCount() = eventos.size
}

class logs : AppCompatActivity() {

    private lateinit var requestQueue: RequestQueue
    private lateinit var recyclerView: RecyclerView
    private lateinit var logsAdapter: LogsAdapter
    private var eventosList = mutableListOf<Evento>()

    private var fechaSeleccionada: String = ""
    private var resultadoSeleccionado: String = "Todos"
    private lateinit var idUsuario: String
    private lateinit var tipoUsuario: String

    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            consultarLogs()
            handler.postDelayed(this, 10000) // Repetir cada 10 segundos
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logs)

        idUsuario = intent.getStringExtra("id") ?: ""
        tipoUsuario = intent.getStringExtra("tipo") ?: ""

        val btnVolver = findViewById<ImageView>(R.id.img_logs_volver)
        val inputFecha = findViewById<EditText>(R.id.input_logs_fecha)
        val btnClearDate = findViewById<ImageView>(R.id.btn_clear_date)
        val spinnerResultado = findViewById<Spinner>(R.id.spinner_logs_respuesta)

        requestQueue = Volley.newRequestQueue(this)

        recyclerView = findViewById(R.id.view_logs_lista)
        recyclerView.layoutManager = LinearLayoutManager(this)
        logsAdapter = LogsAdapter(eventosList, tipoUsuario)
        recyclerView.adapter = logsAdapter

        btnVolver.setOnClickListener { finish() }

        inputFecha.setOnClickListener { mostrarDatePicker() }

        btnClearDate.setOnClickListener {
            inputFecha.text.clear()
            fechaSeleccionada = ""
            btnClearDate.visibility = View.GONE
            consultarLogs()
        }

        spinnerResultado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                resultadoSeleccionado = parent?.getItemAtPosition(position).toString()
                consultarLogs()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        handler.post(runnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            fechaSeleccionada = "$selectedDay-${selectedMonth + 1}-$selectedYear"
            findViewById<EditText>(R.id.input_logs_fecha).setText(fechaSeleccionada)
            findViewById<ImageView>(R.id.btn_clear_date).visibility = View.VISIBLE
            consultarLogs()
        }, year, month, day).show()
    }

    private fun consultarLogs() {
        var url = "http://34.206.51.125/apiconsultalogs.php?id=$idUsuario&tipo=$tipoUsuario"
        if (fechaSeleccionada.isNotEmpty()) {
            url += "&fecha=$fechaSeleccionada"
        }
        if (resultadoSeleccionado.isNotEmpty() && resultadoSeleccionado != "Todos") {
            url += "&resultado=$resultadoSeleccionado"
        }

        val jsonArrayRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                eventosList.clear()
                for (i in 0 until response.length()) {
                    val eventoJson: JSONObject = response.getJSONObject(i)
                    val evento = if (tipoUsuario == "admin") {
                        Evento(
                            nombre = eventoJson.getString("nombres"),
                            apellidoP = eventoJson.getString("apellido_p"),
                            apellidoM = eventoJson.getString("apellido_m"),
                            departamento = eventoJson.getString("departamento"),
                            tipoSensor = eventoJson.getString("tipo_sensor"),
                            resultado = eventoJson.getString("resultado"),
                            fecha = eventoJson.getString("fecha")
                        )
                    } else {
                        Evento(
                            nombre = null, apellidoP = null, apellidoM = null, departamento = null,
                            tipoSensor = eventoJson.getString("tipo_sensor"),
                            resultado = eventoJson.getString("resultado"),
                            fecha = eventoJson.getString("fecha")
                        )
                    }
                    eventosList.add(evento)
                }
                logsAdapter.notifyDataSetChanged()
            },
            { error -> error.printStackTrace() }
        )
        requestQueue.add(jsonArrayRequest)
    }
}