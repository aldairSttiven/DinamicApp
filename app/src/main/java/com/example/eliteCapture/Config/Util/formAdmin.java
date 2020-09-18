package com.example.eliteCapture.Config.Util;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.eliteCapture.Config.Util.Controls.AUT_DES_CBX;
import com.example.eliteCapture.Config.Util.Controls.CBE;
import com.example.eliteCapture.Config.Util.Controls.DPV;
import com.example.eliteCapture.Config.Util.Controls.ETN_ETA;
import com.example.eliteCapture.Config.Util.Controls.RB;
import com.example.eliteCapture.Config.Util.Controls.RS_RSE_RSC;
import com.example.eliteCapture.Config.Util.Controls.SCA_FIL;
import com.example.eliteCapture.Model.Data.Admin;
import com.example.eliteCapture.Model.Data.Tab.ProcesoTab;
import com.example.eliteCapture.Model.Data.Tab.UsuarioTab;
import com.example.eliteCapture.Model.View.Tab.ContenedorTab;
import com.example.eliteCapture.Model.View.Tab.RespuestasTab;
import com.example.eliteCapture.Model.View.iContenedor;
import com.example.eliteCapture.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class formAdmin {

    LinearLayout linearPrinc, linearBodypop;
    Context context;
    String path;
    Boolean inicial;
    Dialog popregla;

    iContenedor iCon;

    ContenedorTab contenedor;
    ProcesoTab pro = null;
    UsuarioTab usu = null;

    SharedPreferences sp;
    Admin adm = null;

    boolean temporal;
    int estado = 1, consecutivo;

    public formAdmin(LinearLayout linearPrinc,LinearLayout linearBodypop, Context context, String path, Boolean inicial, int estado, int consecutivo) {
        try {
            this.linearPrinc = linearPrinc;
            this.linearBodypop = linearBodypop;
            this.context = context;
            this.path = path;
            this.inicial = inicial;
            this.estado = estado;
            this.consecutivo = consecutivo;
            sp = context.getSharedPreferences("share", context.MODE_PRIVATE);
            adm = new Admin(null, path);//administra la conexion de las entidades

            inicializarPopRegla(); //inicicaliza el Dialog para el campo de RSE que edita la regla del conteo
            iCon = new iContenedor(path);

            getcodProceso_Usuario();

            contenedor = validarTemporal();//valida si hay datos temporales de los formularios
        }catch (Exception e){
            Log.i("formAdmin", e.toString());
        }
    }

    public void inicializarPopRegla(){
        popregla = new Dialog(context);
        popregla.setContentView(R.layout.popreglaconteos);
        Window window = popregla.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    }

    //CREA CONTROLES DEL FORMULARIO
    public void CrearForm(String ubicacion) {
        try {
            List<RespuestasTab> lista = null;
            switch (ubicacion) {
                case "H":
                    lista = contenedor.getHeader();
                    break;
                case "Q":
                    lista = contenedor.getQuestions();
                    break;
                case "F":
                    lista = contenedor.getFooter();
                    break;
            }

            for (RespuestasTab r : lista) {
                Log.i("FORMULARIO","ubicacion : "+ubicacion+" tipo : "+r.getTipo());
                View v = null;
                switch (r.getTipo()) {
                    case "CBE":
                        v = new CBE(context, ubicacion, r, path, inicial).crear();
                        break;
                    case "RB":
                        v = new RB(context, ubicacion, r, path, inicial).crear();
                        break;
                    case "DPV":
                        v = new DPV(context, ubicacion, r, path, inicial).crear();
                        break;
                    case "ETN":
                    case "ETA":
                        v = new ETN_ETA(context, ubicacion, r, path, inicial).crear();
                        break;
                    case "FIL":
                    case "SCA":
                    case "SCN":
                        v = new SCA_FIL(context, ubicacion, r, path, inicial).crear();
                        break;
                    case "AUT":
                    case "DES":
                    case "CBX":
                        v = new AUT_DES_CBX(context, ubicacion, r, path, inicial).crear();
                        break;
                    case "RS":
                    case "RSE":
                    case "RSC":
                        v = new RS_RSE_RSC(context, ubicacion, r, path, inicial).crear();
                        break;
                }

                if(ubicacion.equals("H")){
                    linearBodypop.addView(v);
                }else if (ubicacion.equals("Q")){
                    linearPrinc.addView(v);
                }else{
                    linearPrinc.addView(v);
                }
            }
        } catch (Exception ex) {
            Log.i("FORMULARIO","Error : "+ex);
        }
    }


    //VALIDA SI HAY FORMULARIOS PENDIENTES DESDE EL JSON TEMP
    public ContenedorTab validarTemporal() throws Exception {

        ContenedorTab conTemp = iCon.optenerTemporal();
        try {
            if (conTemp != null) {
                if (conTemp.getIdProceso() == pro.getCodigo_proceso()) {

                    temporal = true;
                    return conTemp;

                } else {
                    temporal = false;
                    return contenedorLimipio();
                }
            }

            return contenedorLimipio();
        } catch (Exception e) {
            Log.i("Contenedor_Error", e.toString());
            temporal = false;
        }
        return contenedorLimipio();
    }

    public ContenedorTab contenedorLimipio() throws Exception {
        return iCon.generarContenedor(
                usu.getId_usuario(),
                getPhoneName(),
                adm.getDetalles()
                        .forDetalle(pro.getCodigo_proceso()));
    }


    /*============================
    SHARED PREFENCES & DATA PHONE
    =============================*/
    public void getcodProceso_Usuario() {
        // metodo para extraer del SharedPreferences el id del proceso
        pro = new Gson().fromJson(sp.getString("proceso", ""), new TypeToken<ProcesoTab>() {}.getType());
        // metodo para extraer del SharedPreferences el id del usuario
        usu = new Gson().fromJson(sp.getString("usuario", ""), new TypeToken<UsuarioTab>() {}.getType());
    }

    public String getPhoneName() {//metodo para obtener el nombre del telefono
        try {
            BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
            return myDevice.getName();
        } catch (Exception e) {
            return "EmulatorDevice";
        }
    }

}
