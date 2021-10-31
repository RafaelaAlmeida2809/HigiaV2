package br.com.higiaorganizadordasaude;

import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Layout;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import dataBase.DadosUsuariosOpenHelper;
import dataBase.Medico;
import dataBase.Usuario;

public class FuncoesCompartilhadas {
    GoogleSignInAccount signInAccount;
    private GoogleSignInClient mGoogleSignInClient;

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
    public Integer VerificarLogin(Context context){
        createRequest(context);
        signInAccount = GoogleSignIn.getLastSignedInAccount(context);
        DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(context.getApplicationContext());
        Usuario usuario = DUOH.BuscaUsuarioPeloEmail(signInAccount.getEmail());
        Integer IdUsuarioAtual = -1;
        if (signInAccount != null) {
            IdUsuarioAtual = usuario.getId();
        }
        else {
            FirebaseAuth.getInstance().signOut();
            mGoogleSignInClient.signOut();
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
    public void DeslogarGoogle(Context context){
        createRequest(context);
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();
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
    public void CriarBotoes(Context context, LinearLayout LayoutButton,int tag,String string1,String string2,String string3,int botao){
        ViewStub stub = new ViewStub(context);
        stub.setLayoutResource(botao);
        LayoutButton.addView(stub);
        View inflated = stub.inflate();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)inflated.getLayoutParams();
        params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
        Button b = inflated.findViewById(R.id.buttonPerfil);
        b.setTag(tag);
        TextView t1 = inflated.findViewById(R.id.textView1);
        t1.setText(string1);
        TextView t2 = inflated.findViewById(R.id.textView2 );
        t2.setText(string2);
        TextView t3 = inflated.findViewById(R.id.textView3 );
        t3.setText(string3);
    }
    public void CriarExpansorMedico(Context context,LinearLayout LayoutButton,int idMedicoAtual,String nomeMedico,
                                    List<LinearLayout> ListaLinearMedicos, List<ImageView> ListaImageSeta){
        ViewStub stub = new ViewStub(context);
        stub.setLayoutResource(R.layout.expansor_layout);
        LayoutButton.addView(stub);
        View inflated = stub.inflate();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) inflated.getLayoutParams();
        params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
        Button b = inflated.findViewById(R.id.buttonExpandir);
        //b.setTag(DMOH.buscaIdMedicoString("nome", NomeMedicos.get(i), "ASC").get(0));
        b.setTag(idMedicoAtual);
        ImageView imageView = inflated.findViewById(R.id.imagemExpandir);
        LinearLayout linearLayout = inflated.findViewById(R.id.LayoutButtonExame);
        linearLayout.setVisibility(View.INVISIBLE);
        TextView t1 = inflated.findViewById(R.id.textView1);
        t1.setText(nomeMedico);
        ListaLinearMedicos.add(linearLayout);
        ListaImageSeta.add(imageView);


    }

    public Intent BundleActivy(Context context,Class<?> c,String nome,String valor){
        Bundle bundle = new Bundle();
        bundle.putString(nome,valor);
        Intent novaActivity = new Intent(context, c);
        novaActivity.putExtras(bundle);
        return novaActivity;
    }
    public Intent AbrirGoogleMaps(Medico thisMedico){
        String urlMap = "https://www.google.com/maps/search/" +((thisMedico.getCep()==0)? "":thisMedico.getCep())
                + ((thisMedico.getLogradouro()=="")? "":"+_+" + thisMedico.getLogradouro())
                + ((thisMedico.getBairro()=="")? "":"+_+" + thisMedico.getBairro())
                + ((thisMedico.getCidade()=="")? "":"+_+" + thisMedico.getCidade())
                + ((thisMedico.getEstado()=="")? "":"+_+" + thisMedico.getEstado())
                + ((thisMedico.getNumero()==0)? "":"+_+" + thisMedico.getNumero());
        urlMap.replace(" ","+");
        urlMap.replace("ç","c");
        urlMap = Normalizer.normalize(urlMap, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
        Uri uriMap = Uri.parse(urlMap);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, uriMap);
        mapIntent.setPackage("com.google.android.apps.maps");
        return mapIntent;
    }


}
