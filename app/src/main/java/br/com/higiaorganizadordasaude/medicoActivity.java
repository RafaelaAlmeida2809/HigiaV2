package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import dataBase.DadosMedicosOpenHelper;
import dataBase.Medico;

public class medicoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    ActivityResultLauncher<Intent> activityResultLauncher;
    Spinner spinner1;
    Spinner spinner2;
    int PosicaoSpinner;
    int IdUsuarioAtual;
    String colunaOrdenar;
    String ordemOrdenar;
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medico);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        IdUsuarioAtual =  funcao.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }
        // atribuindo Views
        spinner1 = findViewById(R.id.spinnerMedico1);
        spinner2 = findViewById(R.id.spinnerMedico2);

        //Carregar spinners
        funcao.CriarSpinner(this,spinner1,R.array.ordenar_Medicos,null);
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
                            AtualizarBotoes(colunaOrdenar, ordemOrdenar);
                        }
                        else {
                            AtualizarBotoes(colunaOrdenar, ordemOrdenar);
                        }
                    }
                });
    }

    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }

    public void AbrirAbaInicial(View v){
        this.finish();
    }

    public void AbrirAbaAdicionarMedico(View v) {
        /*Bundle bundle = new Bundle();
        bundle.putString("idMedico",null);
        Intent adicionarMedicoActivity = new Intent(this, adicionarMedicoActivity.class);
        adicionarMedicoActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarMedicoActivity);*/
        activityResultLauncher.launch(funcoes.BundleActivy(this,adicionarMedicoActivity.class,"idMedico",null));
    }

    public  void AbrirAbaPerfilMedico(View v){
        //Codigo responsavel por enviar os dados e instanciar a nova Activity
        /*Bundle bundle = new Bundle();
        bundle.putString("idMedico",v.getTag().toString());
        Intent perfilMedicoActivity = new Intent(this, perfilMedicoActivity.class);
        perfilMedicoActivity.putExtras(bundle);
        activityResultLauncher.launch(perfilMedicoActivity);*/
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilMedicoActivity.class,"idMedico",v.getTag().toString()));
    }

    public  void AtualizarBotoes(String orderColuna, String ordem){
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        List<Medico> medicos = DMOH.BuscaMedicos(orderColuna,ordem,IdUsuarioAtual);
        LinearLayout LayoutButton = findViewById(R.id.LayoutButton);
        LayoutButton.removeAllViews();
        for(int i = 0; i<medicos.size(); i++)
        {
            ViewStub stub = new ViewStub(this);
            stub.setLayoutResource(R.layout.botao_medico_completo);
            LayoutButton.addView(stub);
            View inflated = stub.inflate();
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)inflated.getLayoutParams();
            params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
            Button b = inflated.findViewById(R.id.buttonPerfil);
            b.setTag(medicos.get(i).getId());
            TextView t1 = inflated.findViewById(R.id.textView1);
            t1.setText(medicos.get(i).getNome());
            TextView t2 = inflated.findViewById(R.id.textView2 );
            t2.setText(medicos.get(i).getEspecialidade());
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        PosicaoSpinner = position;
        if(parent.getId() == R.id.spinnerMedico1 ) {
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
        else if(parent.getId() == R.id.spinnerMedico2 )
        {
            if (position == 0) {
                ordemOrdenar = "ASC";
            } else {
                ordemOrdenar = "DESC";
            }
        }
        if(colunaOrdenar != null && ordemOrdenar!= null) {
            AtualizarBotoes(colunaOrdenar, ordemOrdenar);
        }
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}

