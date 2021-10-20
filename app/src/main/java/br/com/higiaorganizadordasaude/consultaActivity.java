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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.ArrayList;
import java.util.List;

import dataBase.Consulta;
import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Exame;
import dataBase.Medico;
import dataBase.Usuario;

public class consultaActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener{

    ActivityResultLauncher<Intent> activityResultLauncher;
    CheckBox checkMedico;
    Spinner spinner1;
    Spinner spinner2;
    List<String> NomeMedicos = new ArrayList<>();
    List<Integer> IdMedicos = new ArrayList<>();
    List<LinearLayout> ListaLinearMedicos = new ArrayList<>();
    List<ImageView> ListaImageSeta = new ArrayList<>();
    int IdUsuarioAtual;
    boolean abaMedicos = false;
    String colunaOrdenar;
    String ordemOrdenar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);

        //Verificar Loguin
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        IdUsuarioAtual =  funcao.VerificarLoguin(this);
        if(IdUsuarioAtual == -1){
            AbrirLoguin();
        }

        // atribuindo Views
        checkMedico = findViewById(R.id.checkBoxMedico);
        spinner1 = findViewById(R.id.spinnerConsulta1);
        spinner2 = findViewById(R.id.spinnerConsulta2);

        //Carregar spinners
        funcao.CriarSpinner(this,spinner1,R.array.ordenar_Consulta_Coluna,null);
        funcao.CriarSpinner(this,spinner2,R.array.ordenar_Ordem,null);

        //Inicia os componentes da pagina
        AtualizarBotoes("idMedico","ASC");

        //Retorno
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ReiniciarAba();
                        }
                    }
                });
    }
    public void AbrirLoguin(){
        Intent LoguinActivity = new Intent(getApplicationContext(),LoguinActivity.class);
        finish();
        startActivity(LoguinActivity);
    }
    public void ReiniciarAba(){
        Intent consultaActivity = new Intent(this, consultaActivity.class);
        finish();
        startActivity(consultaActivity);
    }
    public void AbrirAbaInicial(View v)
    {
        this.finish();
    }
    public void AbrirAbaAdicionarConsulta(View v)
    {
        Bundle bundle = new Bundle();
        bundle.putString("idConsulta",null);
        Intent adicionarConsultaActivity = new Intent(this, adicionarConsultaActivity.class);
        adicionarConsultaActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarConsultaActivity);
    }
    public void AbrirAbaPerfil(View v)
    {
        Bundle bundle = new Bundle();
        bundle.putString("idConsulta",v.getTag().toString());
        Intent perfilConsultaActivity = new Intent(this, perfilConsultaActivity.class);
        perfilConsultaActivity.putExtras(bundle);
        activityResultLauncher.launch(perfilConsultaActivity);
    }
    public  void OrganizarPorMedicos(View v) {
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        NomeMedicos = DCOH.buscaMedicosConsulta(IdUsuarioAtual);
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        LinearLayout LayoutButton = findViewById(R.id.LayoutButton);
        LayoutButton.removeAllViews();
        ListaLinearMedicos.clear();
        ListaImageSeta.clear();
        IdMedicos.clear();
        if (checkMedico.isChecked()) {
            for (int i = 0; i < NomeMedicos.size(); i++) {
                int idMedicoAtual = DMOH.buscaIdMedico("nome", "'"+NomeMedicos.get(i)+"'", "ASC",IdUsuarioAtual).get(0);
                ViewStub stub = new ViewStub(this);
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
                t1.setText(NomeMedicos.get(i));
                ListaLinearMedicos.add(linearLayout);
                ListaImageSeta.add(imageView);
                //IdMedicos.add(DMOH.buscaIdMedicoString("nome", NomeMedicos.get(i), "ASC").get(0));
                IdMedicos.add(idMedicoAtual);
            }
            abaMedicos = true;
        }
        else
        {
            AtualizarBotoes(colunaOrdenar, ordemOrdenar);
            abaMedicos = false;
        }
    }
    public void abrirMedico(View v)
    {
        int idMedicoAberto = Integer.parseInt(v.getTag().toString());
        int indice = IdMedicos.indexOf(idMedicoAberto);
        LinearLayout linearLayout = ListaLinearMedicos.get(indice);
        if(linearLayout.getVisibility() == View.INVISIBLE) {
            ListaImageSeta.get(indice).setRotation(180);
            linearLayout.setVisibility(View.VISIBLE);
            AtualizarBotoesAbaMedicos(colunaOrdenar, ordemOrdenar, linearLayout,idMedicoAberto);
        }
        else
        {
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


    public  void AtualizarBotoesAbaMedicos(String orderColuna, String ordem,LinearLayout LayoutButton, int idMedicoAberto)
    {
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        //List<Integer> idConsultas = DCOH.buscaIdConsultasInt("idMedico", idMedicoAberto, ordem);
        List<Integer> idConsultas = DCOH.buscaIdConsultas("idMedico", idMedicoAberto+"", ordem,IdUsuarioAtual);
        LayoutButton.removeAllViews();
        for(int i = 0; i<idConsultas.size(); i++)
        {
            Consulta consulta = DCOH.BuscaConsulta(idConsultas.get(i),IdUsuarioAtual);
            ViewStub stub = new ViewStub(this);
            stub.setLayoutResource(R.layout.botao_exame_completo);
            LayoutButton.addView(stub);
            View inflated = stub.inflate();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)inflated.getLayoutParams();
            params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
            Button b = inflated.findViewById(R.id.buttonPerfil);
            b.setTag(consulta);
            TextView t1 = inflated.findViewById(R.id.textView1);
            DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
            Medico medico = DMOH.BuscaMedico(consulta.getIdMedico(),IdUsuarioAtual);
            t1.setText(medico.getNome());
            t1.setWidth(LayoutButton.getWidth()/2);
            TextView t2 = inflated.findViewById(R.id.textView2 );
            t2.setText(((consulta.getDia() == 0)? "":consulta.getDia() + "/") + ((consulta.getMes() == 0)? "":consulta.getMes() + "/") + ((consulta.getAno() == 0)? "":consulta.getAno() ));
            t2.setWidth(LayoutButton.getWidth()/2);
            TextView t3 = inflated.findViewById(R.id.textView3 );
            t3.setText(consulta.getHora());
            t3.setWidth(LayoutButton.getWidth()/2);
            DMOH.close();
        }
        DCOH.close();
    }
    public  void AtualizarBotoes(String orderColuna, String ordem)
    {
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        List<Consulta> consultas = DCOH.BuscaConsultas(orderColuna,ordem,IdUsuarioAtual);
        LinearLayout LayoutButton = findViewById(R.id.LayoutButton);
        LayoutButton.removeAllViews();
        for(int i = 0; i<consultas.size(); i++)
        {
            ViewStub stub = new ViewStub(this);
            stub.setLayoutResource(R.layout.botao_exame_completo);
            LayoutButton.addView(stub);
            View inflated = stub.inflate();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)inflated.getLayoutParams();
            params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
            Button b = inflated.findViewById(R.id.buttonPerfil);
            b.setTag(consultas.get(i).getId());
            TextView t1 = inflated.findViewById(R.id.textView1);
            DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
            Medico medico = DMOH.BuscaMedico(consultas.get(i).getIdMedico(),IdUsuarioAtual);
            t1.setText(medico.getNome());
            TextView t2 = inflated.findViewById(R.id.textView2 );
            t2.setText(((consultas.get(i).getDia() == 0)? "":consultas.get(i).getDia() + "/") + ((consultas.get(i).getMes() == 0)? "":consultas.get(i).getMes() + "/") + ((consultas.get(i).getAno() == 0)? "":consultas.get(i).getAno() ));
            TextView t3 = inflated.findViewById(R.id.textView3 );
            t3.setText(consultas.get(i).getHora());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        //((TextView) parent.getChildAt(0)).setTextSize(5);
        ((TextView) parent.getChildAt(0)).setGravity(0x11);

        if(parent.getId() == R.id.spinnerExame1 ) {
            if(position == 0)
            {
                colunaOrdenar = "idMedico";
            }
            else if (position == 1)
            {
                colunaOrdenar = "hora";
            }
            else
            {
                colunaOrdenar = "ano,mes,dia";
            }
        }
        else if(parent.getId() == R.id.spinnerExame2 )
        {
            if (position == 0) {
                ordemOrdenar = "ASC";
            } else {
                ordemOrdenar = "DESC";
            }

        }
        if(colunaOrdenar != null && ordemOrdenar!= null) {
            if(!abaMedicos){
                AtualizarBotoes(colunaOrdenar, ordemOrdenar);
            }
            else
            {
                FecharMedicos();
            }
        }
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    public  void regularTamanho()
    {
        ImageView imageView = findViewById(R.id.imagemFundo);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int margin = ((displayMetrics.widthPixels*400)/1920);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)imageView.getLayoutParams();
        params.setMargins(params.leftMargin, margin, params.rightMargin, params.bottomMargin);
    }
}
