package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Exame;
import dataBase.Medico;
import dataBase.Usuario;

public class exameActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{

    ActivityResultLauncher<Intent> activityResultLauncher;
    String colunaOrdenar;
    String ordemOrdenar;
    List<String> NomeMedicos = new ArrayList<>();
    List<Integer> IdMedicos = new ArrayList<>();
    List<LinearLayout> ListaLinearMedicos = new ArrayList<>();
    List<ImageView> ListaImageSeta = new ArrayList<>();
    //List<RelativeLayout> ListaMedicosLayout = new ArrayList<>();   Tentar substituir as outras listas por essa;
    boolean abaMedicos = false;
    CheckBox checkMedico;
    int IdUsuarioAtual;
    GoogleSignInAccount signInAccount;
    private GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exame);

        //Verificar Loguin
        createRequest();
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(getApplicationContext());
        Usuario usuario = DUOH.BuscaUsuarioPeloEmail(signInAccount.getEmail());
        if (signInAccount != null) {
            IdUsuarioAtual = usuario.getId();
        }
        else {
            Intent LoguinActivity = new Intent(getApplicationContext(),LoguinActivity.class);
            finish();
            startActivity(LoguinActivity);
        }

        // atribuindo Views
        checkMedico = findViewById(R.id.checkBoxMedico);

        //Carregar spinners
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        Spinner spinner1 = findViewById(R.id.spinnerExame1);
        ArrayAdapter<CharSequence> adapter1 = ArrayAdapter.createFromResource(this,R.array.ordenar_Exame_Coluna,android.R.layout.simple_spinner_item);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner1.setAdapter(adapter1);
        spinner1.setOnItemSelectedListener(this);
        spinner1.getLayoutParams().width =  (displayMetrics.widthPixels/2)-40;
        Spinner spinner2 = findViewById(R.id.spinnerExame2);
        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(this,R.array.ordenar_Ordem,android.R.layout.simple_spinner_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner2.setAdapter(adapter2);
        spinner2.setOnItemSelectedListener(this);
        spinner2.getLayoutParams().width =  (displayMetrics.widthPixels/2)-40;

        //Inicia os componentes da pagina
        AtualizarBotoes("tipo","ASC");

        //Retorno
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            if(!abaMedicos){
                                AtualizarBotoes(colunaOrdenar, ordemOrdenar);
                            }
                            else {
                                OrganizarPorMedicos(null);
                                FecharMedicos();
                            }
                        }
                    }
                });
    }

    public void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void AbrirAbaInicial(View v) {
        this.finish();
    }

    public void AbrirAbaAdicionarExame(View v)
    {
        Bundle bundle = new Bundle();
        bundle.putString("idExame",null);
        Intent adicionarExameActivity = new Intent(this, adicionarExameActivity.class);
        adicionarExameActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarExameActivity);
    }

    public void AbrirAbaPerfilExame(View v)
    {
        Bundle bundle = new Bundle();
        bundle.putString("idExame",v.getTag().toString());
        Intent perfilExameActivity = new Intent(this, perfilExameActivity.class);
        perfilExameActivity.putExtras(bundle);
        activityResultLauncher.launch(perfilExameActivity);
    }

    public  void OrganizarPorMedicos(View v) {
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        NomeMedicos = DEOH.buscaMedicosExames(IdUsuarioAtual);
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        LinearLayout LayoutButton = findViewById(R.id.LayoutButton);
        LayoutButton.removeAllViews();
        ListaLinearMedicos.clear();
        ListaImageSeta.clear();
        IdMedicos.clear();
        if (checkMedico.isChecked()) {
            for (int i = 0; i < NomeMedicos.size(); i++) {
                ViewStub stub = new ViewStub(this);
                stub.setLayoutResource(R.layout.expansor_layout);
                LayoutButton.addView(stub);
                View inflated = stub.inflate();
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) inflated.getLayoutParams();
                params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
                Button b = inflated.findViewById(R.id.buttonExpandir);
                //b.setTag(DMOH.buscaIdMedicoString("nome", NomeMedicos.get(i), "ASC").get(0));
                b.setTag(DMOH.buscaIdMedico("nome", "'"+NomeMedicos.get(i)+"'", "ASC",IdUsuarioAtual).get(0));
                ImageView imageView = inflated.findViewById(R.id.imagemExpandir);
                LinearLayout linearLayout = inflated.findViewById(R.id.LayoutButtonExame);
                linearLayout.setVisibility(View.INVISIBLE);
                TextView t1 = inflated.findViewById(R.id.textView1);
                t1.setText(NomeMedicos.get(i));
                ListaLinearMedicos.add(linearLayout);
                ListaImageSeta.add(imageView);
                IdMedicos.add(DMOH.buscaIdMedico("nome", "'"+NomeMedicos.get(i)+"'", "ASC",IdUsuarioAtual).get(0));
                //IdMedicos.add(DMOH.buscaIdMedicoString("nome", NomeMedicos.get(i), "ASC").get(0));
            }
            abaMedicos = true;
        }
        else{
            AtualizarBotoes(colunaOrdenar, ordemOrdenar);
            abaMedicos = false;
        }
    }

    public void AbrirMedico(View v){
        int idMedicoAberto = Integer.parseInt(v.getTag().toString());
        int indice = IdMedicos.indexOf(idMedicoAberto);
        LinearLayout linearLayout = ListaLinearMedicos.get(indice);
        if(linearLayout.getVisibility() == View.INVISIBLE) {
            ListaImageSeta.get(indice).setRotation(180);
            linearLayout.setVisibility(View.VISIBLE);
            AtualizarBotoesAbaMedicos(colunaOrdenar, ordemOrdenar, linearLayout,idMedicoAberto);
        }
        else{
            ListaImageSeta.get(indice).setRotation(0);
            linearLayout.removeAllViews();
            linearLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void FecharMedicos() {
        for (int i = 0; i > ListaLinearMedicos.size(); i++) {
            ListaImageSeta.get(i).setRotation(0);
            ListaLinearMedicos.get(i).removeAllViews();
        }
    }


    public  void AtualizarBotoesAbaMedicos(String orderColuna, String ordem,LinearLayout LayoutButton, int idMedicoAberto){
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        //List<Integer> idExames = DEOH.buscaIdExamesInt("idMedico",idMedicoAberto,ordem);
        List<Integer> idExames = DEOH.buscaIdExames("idMedico",idMedicoAberto+"",ordem,IdUsuarioAtual);
        LayoutButton.removeAllViews();
        for(int i = 0; i<idExames.size(); i++)
        {
            Exame exame = DEOH.BuscaExame(idExames.get(i),IdUsuarioAtual);
            ViewStub stub = new ViewStub(this);
            stub.setLayoutResource(R.layout.botao_exame_completo);
            LayoutButton.addView(stub);
            View inflated = stub.inflate();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)inflated.getLayoutParams();
            params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
            Button b = inflated.findViewById(R.id.buttonPerfil);
            b.setTag(idExames.get(i));
            TextView t1 = inflated.findViewById(R.id.textView1);
            t1.setText(exame.getTipo());
            TextView t2 = inflated.findViewById(R.id.textView2 );
            t2.setText(exame.getParteCorpo());
            TextView t3 = inflated.findViewById(R.id.textView3 );
            t3.setText(((exame.getDia() == 0)? "":exame.getDia() + "/") + ((exame.getMes() == 0)? "":exame.getMes() + "/") + ((exame.getAno() == 0)? "":exame.getAno() ));
        }
    }

    public  void AtualizarBotoes(String orderColuna, String ordem)
    {
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        List<Exame> exames = DEOH.BuscaExames(orderColuna,ordem,IdUsuarioAtual);
        LinearLayout LayoutButton = findViewById(R.id.LayoutButton);
        LayoutButton.removeAllViews();
        for(int i = 0; i<exames.size(); i++)
        {
            ViewStub stub = new ViewStub(this);
            stub.setLayoutResource(R.layout.botao_exame_completo);
            LayoutButton.addView(stub);
            View inflated = stub.inflate();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)inflated.getLayoutParams();
            params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
            Button b = inflated.findViewById(R.id.buttonPerfil);
            b.setTag(exames.get(i).getId());
            //int tamanho = b.getLayoutParams().width /2;
            TextView t1 = inflated.findViewById(R.id.textView1);
            t1.setText(exames.get(i).getTipo());
            //t1.setLayoutParams(new RelativeLayout.LayoutParams(tamanho,t1.getLayoutParams().height));
            TextView t2 = inflated.findViewById(R.id.textView2 );
            t2.setText(exames.get(i).getParteCorpo());
            //t2.setLayoutParams(new RelativeLayout.LayoutParams(tamanho,t2.getLayoutParams().height)));
            TextView t3 = inflated.findViewById(R.id.textView3 );
            t3.setText(((exames.get(i).getDia() == 0)? "":exames.get(i).getDia() + "/") + ((exames.get(i).getMes() == 0)? "":exames.get(i).getMes() + "/") + ((exames.get(i).getAno() == 0)? "":exames.get(i).getAno() ));
            //t3.setLayoutParams(new RelativeLayout.LayoutParams(tamanho,t3.getLayoutParams().height)));
            //Toast.makeText(this, tamanho + " " + t2.getLayoutParams().width, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        if(parent.getId() == R.id.spinnerExame1 ) {
            if(position == 0)
            {
                colunaOrdenar = "tipo";
            }
            else if (position == 1)
            {
                colunaOrdenar = "parteCorpo";
            }
            else
            {
                colunaOrdenar = "ano,mes,dia";
            }
        }
        else if(parent.getId() == R.id.spinnerExame2 ){
            if (position == 0) {
                ordemOrdenar = "ASC";
            }
            else {
                ordemOrdenar = "DESC";
            }

        }
        if(colunaOrdenar != null && ordemOrdenar!= null) {
            if(!abaMedicos){
                AtualizarBotoes(colunaOrdenar, ordemOrdenar);
            }
            else{
                FecharMedicos();
            }
        }
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
