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

import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosRemediosOpenHelper;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Exame;
import dataBase.Remedio;
import dataBase.Usuario;

public class remedioActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    ActivityResultLauncher<Intent> activityResultLauncher;
    Spinner spinner1;
    Spinner spinner2;
    String colunaOrdenar;
    String ordemOrdenar;
    List<String> NomeMedicos = new ArrayList<>();
    List<Integer> IdMedicos = new ArrayList<>();
    List<LinearLayout> ListaLinearMedicos = new ArrayList<>();
    List<ImageView> ListaImageSeta = new ArrayList<>();
    boolean abaMedicos = false;
    CheckBox checkMedico;
    int IdUsuarioAtual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remedio);

        //Verificar Loguin
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        IdUsuarioAtual =  funcao.VerificarLoguin(this);
        if(IdUsuarioAtual == -1){
            AbrirLoguin();
        }

        // atribuindo Views
        checkMedico = findViewById(R.id.checkBoxMedico);
        spinner1 = findViewById(R.id.spinnerRemedio1);
        spinner2 = findViewById(R.id.spinnerRemedio2);

        //Carregar spinners
        funcao.CriarSpinner(this,spinner1,R.array.ordenar_Remedio_Coluna,null);
        funcao.CriarSpinner(this,spinner2,R.array.ordenar_Ordem,null);

        //Inicia os componentes da pagina
        AtualizarBotoes("nome","ASC");

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

    public void AbrirLoguin(){
        Intent LoguinActivity = new Intent(getApplicationContext(),LoguinActivity.class);
        finish();
        startActivity(LoguinActivity);
    }

    public void AbrirAbaInicial(View v)
    {
        this.finish();
    }

    public void AbrirAbaAdicionarRemedio(View v)
    {
        Bundle bundle = new Bundle();
        bundle.putString("idRemedio",null);
        Intent adicionarRemedioActivity = new Intent(this, adicionarRemedioActivity.class);
        adicionarRemedioActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarRemedioActivity);
    }

    public void AbrirAbaPerfil(View v)
    {
        Bundle bundle = new Bundle();
        bundle.putString("idRemedio",v.getTag().toString());
        Intent perfilRemedioActivity = new Intent(this, perfilRemedioActivity.class);
        perfilRemedioActivity.putExtras(bundle);
        activityResultLauncher.launch(perfilRemedioActivity);
    }

    public  void OrganizarPorMedicos(View v) {
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        NomeMedicos = DROH.buscaMedicosRemedios(IdUsuarioAtual);
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
                //IdMedicos.add(DMOH.buscaIdMedicoString("nome", NomeMedicos.get(i), "ASC").get(0));
                IdMedicos.add(DMOH.buscaIdMedico("nome", "'"+NomeMedicos.get(i)+"'", "ASC",IdUsuarioAtual).get(0));
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
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());

        List<Integer> idRemedios = DROH.buscaIdRemedio("idMedico",idMedicoAberto +"",ordem,IdUsuarioAtual);
        LayoutButton.removeAllViews();
        for(int i = 0; i<idRemedios.size(); i++)
        {
            Remedio remedio = DROH.BuscaRemedio(idRemedios.get(i),IdUsuarioAtual);
            ViewStub stub = new ViewStub(this);
            stub.setLayoutResource(R.layout.botao_exame_completo);
            LayoutButton.addView(stub);
            View inflated = stub.inflate();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)inflated.getLayoutParams();
            params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
            Button b = inflated.findViewById(R.id.buttonPerfil);
            b.setTag(idRemedios.get(i));
            TextView t1 = inflated.findViewById(R.id.textView1);
            t1.setText(remedio.getNome());
            t1.setWidth(LayoutButton.getWidth()/2);
            TextView t2 = inflated.findViewById(R.id.textView2 );
            t2.setText(remedio.getDosagem());
            t2.setWidth(LayoutButton.getWidth()/2);
            TextView t3 = inflated.findViewById(R.id.textView3 );
            t3.setText(remedio.getFormato());
            t3.setWidth(LayoutButton.getWidth()/2);
        }
    }
    public  void AtualizarBotoes(String orderColuna, String ordem)
    {
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        List<Remedio> remedios = DROH.BuscaRemedios(orderColuna,ordem,IdUsuarioAtual);
        LinearLayout LayoutButton = findViewById(R.id.LayoutButton);
        LayoutButton.removeAllViews();
        for(int i = 0; i<remedios.size(); i++)
        {
            ViewStub stub = new ViewStub(this);
            stub.setLayoutResource(R.layout.botao_exame_completo);
            LayoutButton.addView(stub);
            View inflated = stub.inflate();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)inflated.getLayoutParams();
            params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
            Button b = inflated.findViewById(R.id.buttonPerfil);
            b.setTag(remedios.get(i).getId());
            TextView t1 = inflated.findViewById(R.id.textView1);
            t1.setText(remedios.get(i).getNome());
            t1.setWidth(LayoutButton.getWidth()/2);
            TextView t2 = inflated.findViewById(R.id.textView2 );
            t2.setText(remedios.get(i).getDosagem());
            t2.setWidth(LayoutButton.getWidth()/2);
            TextView t3 = inflated.findViewById(R.id.textView3 );
            t3.setText(remedios.get(i).getFormato());
            t3.setWidth(LayoutButton.getWidth()/2);
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        if(parent.getId() == R.id.spinnerRemedio1 ) {
            if(position == 0)
            {
                colunaOrdenar = "nome";
            }
            else if (position == 1)
            {
                colunaOrdenar = "dosagem";
            }
            else
            {
                colunaOrdenar = "formato";
            }
        }
        else if(parent.getId() == R.id.spinnerRemedio2 )
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

}

