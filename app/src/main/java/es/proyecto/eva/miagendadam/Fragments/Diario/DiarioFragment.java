package es.proyecto.eva.miagendadam.Fragments.Diario;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import es.proyecto.eva.miagendadam.R;
import es.proyecto.eva.miagendadam.VolleyController.AppController;

/**
 * TODO: (ignore) ORDENAR REGISTROS DE DIARIO POR FECHA Implementación de futuro...
* TODO: Añadir visualización de estadísticas de los registros con una opción en un icono del action bar
* Para ello crear consultas con uso de funciones como COUNT, SUM, AVG..
* Algunas estadísticas que se pretenden implementar:
* - Visualización de número total de registros existentes
* - Visualización de media de horas trabajadas en total
* - Día que más horas se ha trabajado
* -  "   " menos "    "  "   "
* - ... ?
* TODO: Añadir búsqueda de registros a través de filtrado:
* - Filtrado por búsqueda de día exacto
* - Filtrado por franja de horas
* - Filtrado por horas exactas
* - Filtrado por valoración
* - ... ?
*/

/***************************************************************************************************
 * Fragmento de la opción Diario que se incluye en la actividad NavMenu. Se muestra por defecto
 * al abrirse la aplicación tras haber iniciado sesión, o bien al seleccionar la opción manualmente
 * en el listado de opciones del menú desplegable lateral de la aplicación.
 * Obtiene los registros de diario del usuario activo y los muestra en un listado.
 * Permite la creación y edición de los registros interactuando para ello con otras actividades
 * (NuevoRegistroDiario, que se arranca al pulsar sobre el botón "+" de abajo, y VerYEditarRegistroDiario,
 * que se arranca al pulsar sobre algún registro del listado).
 * Por defecto el fragmento aparecerá por primera vez con un breve texto indicando que aún no existen
 * registros. Este texto desaparecerá en el momento en el que se cree algún registro.
 **************************************************************************************************/
public class DiarioFragment extends Fragment {
    FloatingActionButton btnNuevo;
    ListView listaResultado;
    TextView txt;
    // Datos obtenidos para mostrar en el listado
    private String dia;
    private String mes;
    private String anyo;
    private String fecha; // guardará día, mes y año del día obtenido
    private String horas;
    private String minutos;
    private String valoracion;

    // Datos del item seleccionado que se van a mostrar al pulsar sobre un registro
    public static String id_dia_seleccionado;
    // no hace falta que sean strings estáticos porque los vamos a utilizar para componer la fecha,
    // que será lo que necesitaremos en la clase VerYEditarRegistroDiario para que el usuario lo vea
    // y esa sí que tendrá que ser estática para poder acceder a ella desde la otra clase
    public static String dia_seleccionado;
    public static String mes_seleccionado;
    public static String anyo_seleccionado;
    public static String jornada_partida_seleccionada;
    public static String hora_inicio_1_seleccionada;
    public static String minuto_inicio_1_seleccionado;
    public static String hora_fin_1_seleccionada;
    public static String minuto_fin_1_seleccionado;
    public static String hora_inicio_2_seleccionada;
    public static String minuto_inicio_2_seleccionado;
    public static String hora_fin_2_seleccionada;
    public static String minuto_fin_2_seleccionado;
    public static String reunion_fct_seleccionada;
    public static String horas_reunion_seleccionada;
    public static String fecha_seleccionada;
    public static String descripcion_seleccionada;
    public static String valoracion_seleccionada;

    private StringRequest request; // petición volley
    public static JSONArray jsonArrayDiario; // array JSON con los registros obtenidos de la base de datos
    ArrayList <String> arrayFechas = new ArrayList<>(); // array en el que introduciremos las fechas obtenidas
    ArrayList <String> arrayHoras = new ArrayList<>(); // array en el que introduciremos las horas obtenidas
    ArrayList <String> arrayMinutos = new ArrayList<>(); // array en el que introduciremos los minutos obtenidos
    ArrayList <String> arrayValoraciones = new ArrayList<>(); // array en el que introduciremos las valoraciones obtenidas
    AdaptadorListaDiario adaptador; // objeto de la clase AdaptadorListaDiario que utilizaremos como adaptador personalizado para
    // nuestra lista de registros

//    private String url_consulta = "http://192.168.0.12/MiAgenda/select_dias.php";
//    private String  url_consulta = "http://192.168.0.159/MiAgenda/select_dias.php";
    private String url_consulta = "http://miagendafp.000webhostapp.com/select_dias.php";
    private String url_consulta2 = "http://miagendafp.000webhostapp.com/delete_all_registros.php";
    private String nombre_de_usuario = "";
    private boolean hayRegistros;
    private boolean masRecientesPrimero = false; // para saber que queremos que se vean primero los más recientes (guardaremos los datos
    // en preferencias para saberlo)
    private boolean menosRecientesPrimero = false; // para saber que queremos que se vean primero los menos recientes

    // Creamos el menú en el action bar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_diario_fragment, menu);
    }

    // Selección de opciones del menú del action bar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /** TODO: (ignore) ORDENAR REGISTROS DE DIARIO POR FECHA Implementación de futuro...
            * case R.id.menu_ordenar_registros: // Opción de guardar los datos de usuario actualizados
                Log.i("DiarioFragment", "Action Ordenar registros");
                seleccionaOrdenRegistros();
                return true;
            **/
            case R.id.menu_busqueda_registros: // Opción de guardar los datos de usuario actualizados
               Log.i("DiarioFragment", "Action Búsqueda de registros");
            // abrimos pantalla de búsqueda de registros
                Intent intent = new Intent (getActivity(), BusquedaRegistros.class);
                startActivity(intent);
                return true;
            case R.id.menu_estadisticas: // Opción de guardar los datos de usuario actualizados
               Log.i("DiarioFragment", "Action Estadísticas de registros");
                return true;
            case R.id.menu_borrar_todo: // Opción de guardar los datos de usuario actualizados
                Log.i("DiarioFragment", "Action Borrar todo");
                // Preguntamos antes de proceder con el borrado de datos
                if (!hayRegistros){
                    Snackbar.make(getActivity().findViewById(android.R.id.content),
                            R.string.no_hay_registros, Snackbar.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle(R.string.alert_borrar_todo_title); // titulo del diálogo
                    builder.setMessage(R.string.alert_borrar_todo)
                            .setPositiveButton(R.string.alert_borrar_todo_btn_borrar, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    borrarRegistros();
                                }
                            })
                            .setNegativeButton(R.string.respuesta_dialog_no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    // dejamos en blanco, no se hace nada, solo cierra el diálogo
                                }
                            });

                    Dialog dialog = builder.create();
                    dialog.show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /***********************************************************************************************
     * Método que ordena los registros del diario  por más o menos recientes
    **********************************************************************************************
     public void seleccionaOrdenRegistros(){
        TODO: (ignore) ORDENAR REGISTROS DE DIARIO POR FECHA Implementación de futuro...
    }
    */
    public DiarioFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // para visualizar el menú en el action bar
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_diario, container, false);
        listaResultado = (ListView) view.findViewById(R.id.lista);
        txt = (TextView) view.findViewById(R.id.txt_vacio);
        // Obtenemos de las preferencias el nombre del usuario
        SharedPreferences preferences = this.getActivity().getSharedPreferences("credenciales", Context.MODE_PRIVATE);
        nombre_de_usuario = preferences.getString("nombre_de_usuario", "");
        Log.d("DiarioFragment", "onCreateView");
        // Al pulsar en el botón de nuevo (+) procedemos a crear un nuevo registro
        btnNuevo = (FloatingActionButton) view.findViewById(R.id.btn_nuevo_registro);
        btnNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Log.d("DiarioFragment", "Nuevo registro de diario");
                Intent intent = new Intent(getActivity(), NuevoRegistroDiario.class);
                startActivity(intent);
            }
        });

        // Al pulsar sobre algún item de la lista (sobre algún registro del diario) lo mostramos en detalle en otra actividad:
        listaResultado.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> customerAdapter, View footer, int selectedInt, long selectedLong) {
                // String listChoice = (String) listaResultado.getItemAtPosition(selectedInt); // para mostrar la selección pulsada
                int id = (int) listaResultado.getItemIdAtPosition(selectedInt); // obtenemos el id del elemento del listado seleccionado
                // para saber qué id de día debemos obtener
                try {
                    // obtenemos los datos del elemento seleccionado
                    id_dia_seleccionado = jsonArrayDiario.getJSONObject(id).getString("idDia");
                    dia_seleccionado = jsonArrayDiario.getJSONObject(id).getString("dia");
                    mes_seleccionado = jsonArrayDiario.getJSONObject(id).getString("mes");
                    anyo_seleccionado = jsonArrayDiario.getJSONObject(id).getString("anyo");
                    jornada_partida_seleccionada = jsonArrayDiario.getJSONObject(id).getString("jornada_partida");
                    hora_inicio_1_seleccionada = jsonArrayDiario.getJSONObject(id).getString("hora_inicio_1");
                    minuto_inicio_1_seleccionado = jsonArrayDiario.getJSONObject(id).getString("minuto_inicio_1");
                    hora_fin_1_seleccionada = jsonArrayDiario.getJSONObject(id).getString("hora_fin_1");
                    minuto_fin_1_seleccionado = jsonArrayDiario.getJSONObject(id).getString("minuto_fin_1");
                    hora_inicio_2_seleccionada = jsonArrayDiario.getJSONObject(id).getString("hora_inicio_2");
                    minuto_inicio_2_seleccionado = jsonArrayDiario.getJSONObject(id).getString("minuto_inicio_2");
                    hora_fin_2_seleccionada = jsonArrayDiario.getJSONObject(id).getString("hora_fin_2");
                    minuto_fin_2_seleccionado = jsonArrayDiario.getJSONObject(id).getString("minuto_fin_2");
                    reunion_fct_seleccionada = jsonArrayDiario.getJSONObject(id).getString("reunion_fct");
                    horas_reunion_seleccionada = jsonArrayDiario.getJSONObject(id).getString("horas_reunion");
                    fecha_seleccionada = dia_seleccionado + "/" + mes_seleccionado + "/" + anyo_seleccionado;
                    descripcion_seleccionada = jsonArrayDiario.getJSONObject(id).getString("descripcion");
                    valoracion_seleccionada = jsonArrayDiario.getJSONObject(id).getString("valoracion");
                    // después de obtener los datos abrimos la nueva actividad que nos permitirá visualizarlos
                    // y editarlos en sus correspondientes campos
                   Log.d("DiarioFragment", "Vista detalle de un registro");
                    Intent intent = new Intent(getActivity(), VerYEditarRegistroDiario.class);
                    startActivity(intent);

                } catch (Exception e) {
                    e.printStackTrace();
                   Log.e("DiarioFragment", "Error al obtener los datos del registro a visualizar en detalle");
                }
            }
        });
        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    // Se ejecuta cuando el fragmento se pausa, al abrir otra actividad o fragmento
    @Override
    public void onPause() {
        super.onPause();
       Log.d("DiarioFragment", "Fragmento pausado");
    }

    // Se ejecuta al abrir y al volver al fragmento cuando éste ya se había cargado alguna vez previamente
    // Aquí obtendremos de nuevo los registros, por si volvemos de actualizar o crear alguno, para
    // tener el listado actualizado con los últimos cambios hechos
    @Override
    public void onResume() {
        super.onResume();
        Log.d("DiarioFragment", "Fragment reanudado");
        obtenerRegistrosDiario();
    }

    /**************************************************************************************************
     * Método que elimina TODOS los registros del diario del usuario, solicitando confirmación previa
     *************************************************************************************************/
    public void borrarRegistros(){
        request = new StringRequest(Request.Method.POST, url_consulta2,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // notificamos del borrado
                        Snackbar.make(getActivity().findViewById(android.R.id.content),
                                R.string.registros_borrados, Snackbar.LENGTH_LONG).show();
                        // que no hay registros y no aparezcan los que había creados en pantalla
                        listaResultado.setAdapter(null); // vaciamos la lista para no ver los registros
                        // ponemos el texto de que no hay registros
                        txt.setText(R.string.texto_diario_vacio);
                        hayRegistros = false;
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                       // Toast.makeText(getActivity(), R.string.error_servidor, Toast.LENGTH_LONG).show();
                        Snackbar.make(getActivity().findViewById(android.R.id.content),
                                R.string.error_servidor, Snackbar.LENGTH_LONG).show();
                        Log.e("DiarioFragment", "Error al realizar la conexión con el servidor al borrar todos los registros del usuario");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // AQUI SE ENVIARAN LOS DATOS EMPAQUETADOS EN UN OBJETO MAP<clave, valor>
                Map<String, String> parametros = new HashMap<>();
                parametros.put("nUsuario", nombre_de_usuario); // pasamos el nombre de usuario como parámetro de la consulta para obtener sus registros del diario
                return parametros;
            }

        };
        AppController.getInstance().addToRequestQueue(request);
    }



    /***********************************************************************************************
     * Método que obtiene automáticamente los registros del usuario de la opción "Diario"
     **********************************************************************************************/
    public void obtenerRegistrosDiario() {
        request = new StringRequest(Request.Method.POST, url_consulta,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (!nombre_de_usuario.isEmpty()) { // aseguramos que las preferencias no están vacías
                            if (response.equals("0")) { // Respuesta 0 = El usuario no tiene registros en el diario
                                txt.setText(R.string.texto_diario_vacio);
                                hayRegistros = false;
                                if (!hayRegistros) {
                                    System.out.println("NO HAY REGISTROS");
                                }
                              //  Log.i("DiarioFragment", "El usuario no tiene registros creados");
                            } else { // El usuario tiene registros
                                try {
                                    hayRegistros = true;
                                    txt.setText(""); // ponemos el texto de que no hay registros en blanco por si acaso, y obtenemos datos
                                    response = response.replace("][", ","); // SUSTITUIMOS LOS CARACTERES QUE SEPARAN CADA RESULTADO DEL ARRAY
                                    // PORQUE SI NO NOS TOMARÍA SOLO EL PRIMER ARRAY. DE ESTA MANERA HACEMOS QUE LOS DETECTE COMO OBJETOS (EN VEZ DE COMO ARRAYS DIFERENTES)
                                    // DENTRO DE UN ÚNICO ARRAY
                                    // YA QUE LOS ARRAYS TIENEN FORMATO [{...}][{...}], ... CON LO QUE, SI OBTIENE ASÍ LOS RESULTADOS, SOLO VA A COGER EL PRIMERO
                                    // Y UN ARRAY DE OBJETOS TENDRÍA ESTE OTRO FORMATO [{...}, {...}, {...}] DONDE LOS CORCHETES DETERMINAN EL ARRAY, Y LAS LLAVES LOS OBJETOS.
                                    jsonArrayDiario = new JSONArray(response); // guardamos los registros en el array
                                   Log.i("DiarioFragment", "Registros obtenidos:");
                                    cargarRegistros(); // cargamos en pantalla los registros
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Log.e("DiarioFragment", "Error al obtener los registros del usuario");
                                }
                            }
                        } else { // si no hay preferencias, es decir, no hay datos del usuario (cosa improbable), notificamos
                            //Toast.makeText(getActivity(), R.string.error_no_hay_usuario, Toast.LENGTH_SHORT).show();
                            Snackbar.make(getActivity().findViewById(android.R.id.content),
                                    R.string.error_no_hay_usuario, Snackbar.LENGTH_SHORT).show();
                            Log.w("DiarioFragment", "No hay nombre de usuario para obtener los registros de diario correspondientes.");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Toast.makeText(getActivity(), R.string.error_servidor, Toast.LENGTH_LONG).show();
                        Snackbar.make(getActivity().findViewById(android.R.id.content),
                                R.string.error_servidor, Snackbar.LENGTH_SHORT).show();
                        Log.e("DiarioFragment", "Error al realizar la conexión con el servidor al obtener registros del usuario");
                    }
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // AQUI SE ENVIARAN LOS DATOS EMPAQUETADOS EN UN OBJETO MAP<clave, valor>
                Map<String, String> parametros = new HashMap<>();
                parametros.put("nUsuario", nombre_de_usuario); // pasamos el nombre de usuario como parámetro de la consulta para obtener sus registros del diario
                return parametros;
            }

        };
        AppController.getInstance().addToRequestQueue(request);
    }

    /*********************************************************************************************************
     * Método que carga los registros de diario del usuario activo obtenidos en un ListView personalizado
     ********************************************************************************************************/
    private void cargarRegistros() {
        arrayFechas = new ArrayList<>();
        arrayHoras = new ArrayList<>();
        arrayMinutos = new ArrayList<>();
        arrayValoraciones = new ArrayList<>();
        for (int i = 0; i < jsonArrayDiario.length(); i++){ // hasta que se hayan obtenido todos los registros:
            try {
                dia = jsonArrayDiario.getJSONObject(i).getString("dia"); // obtenemos dia
                mes = jsonArrayDiario.getJSONObject(i).getString("mes"); // obtenemos mes
                anyo = jsonArrayDiario.getJSONObject(i).getString("anyo"); // obtenemos año
                fecha = dia + "/" + mes + "/" + anyo;
                arrayFechas.add("Día " + fecha); // la añadimos al array de fechas
                horas = jsonArrayDiario.getJSONObject(i).getString("horas"); // obtenemos horas
                arrayHoras.add(horas + " horas"); // las añadimos al array de horas
                minutos = jsonArrayDiario.getJSONObject(i).getString("minutos"); // obtenemos minutos
                if (Integer.valueOf(minutos) > 0){ // si hay minutos se añaden
                    arrayMinutos.add(" y "+ minutos + " minutos");
                } else if (Integer.valueOf(minutos) < 1){ // si no se deja en blanco para no poner un 0
                    arrayMinutos.add("");
                }
                valoracion = jsonArrayDiario.getJSONObject(i).getString("valoracion"); // obtenemos valoración
                arrayValoraciones.add(valoracion); // las añadimos al array de valoraciones
            } catch (Exception e){ // Error al intentar obtener los datos
                e.printStackTrace();
                Log.e("DiarioFragment", "Error al cargar los registros de usuario");
            }
        }
        // Recorremos un array para comprobar que los datos son correctos (para verlo en la consola de Android Studio)
        for (int x = 0; x < arrayFechas.size(); x++){
            System.out.println("FECHA "+ x + ": " +arrayFechas.get(x));
        }

        // creamos adaptador personalizado a nuestra lista de registros
        adaptador = new AdaptadorListaDiario(getActivity(), arrayFechas, arrayHoras, arrayMinutos, arrayValoraciones);
        listaResultado.setAdapter(adaptador); // lo asociamos a la lista
    }
}
