package br.com.higiaorganizadordasaude;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dataBase.DadosUsuariosOpenHelper;
import dataBase.Usuario;

public class FuncoesCompartilhadas {
    GoogleSignInAccount signInAccount;
    private GoogleSignInClient mGoogleSignInClient;
    List<String> listaVazia = new ArrayList<String>();
    public void ModalConfirmacao (String titulo, String mensagem, Context contexto, MyInterface myInterface){

        AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
        builder.setCancelable(true);
        builder.setTitle(titulo);
        builder.setMessage(mensagem);
        builder.setPositiveButton(R.string.sim,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        myInterface.RetornoModal(true);
                    }
                });
        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                myInterface.RetornoModal(false);
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void MudarIdioma (String linguagem, Resources resources){
        Locale local = new Locale(linguagem);
        Locale.setDefault(local);
        Configuration configuration = resources.getConfiguration();
        configuration.locale = local;
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
    }
    public Integer VerificarLoguin(Context context){
        createRequest(context);
        signInAccount = GoogleSignIn.getLastSignedInAccount(context);
        DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(context.getApplicationContext());
        Usuario usuario = DUOH.BuscaUsuarioPeloEmail(signInAccount.getEmail());
        Integer IdUsuarioAtual = -1;
        if (signInAccount != null) {
            IdUsuarioAtual = usuario.getId();
        }
        return IdUsuarioAtual;
    }
    public void createRequest(Context context) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(context.getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
    }
    public void CriarSpinner(Context context, Spinner spinner, int arrayInt, List<String> lista ){
        ArrayAdapter<CharSequence> adapter = null;
        if(arrayInt!=-1) {
             adapter = ArrayAdapter.createFromResource(context, arrayInt, android.R.layout.simple_spinner_item);
        }
        else {
            adapter = new ArrayAdapter(context,android.R.layout.simple_spinner_item,lista);
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener((AdapterView.OnItemSelectedListener) context);
    }


}
