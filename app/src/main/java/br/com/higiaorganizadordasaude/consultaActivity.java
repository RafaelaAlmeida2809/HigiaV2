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
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import dataBase.Consulta;
import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.Medico;

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
    String colunaOrdenar ="idMedico";
    String ordemOrdenar ="ASC";
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        checkMedico = findViewById(R.id.checkBoxMedico);
        spinner1 = findViewById(R.id.spinnerConsulta1);
        spinner2 = findViewById(R.id.spinnerConsulta2);

        //Carregar spinners
        funcoes.CriarSpinner(this,spinner1,R.array.ordenar_Consulta_Coluna,null);
        funcoes.CriarSpinner(this,spinner2,R.array.ordenar_Ordem,null);

        //Inicia os componentes da pagina
        AtualizarBotoes("idMedico","ASC");

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
                        else {
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

    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }

    public void AbrirAbaInicial(View v)
    {
        this.finish();
    }

    public void AbrirAbaAdicionarConsulta(View v){
        activityResultLauncher.launch(funcoes.BundleActivy(this,adicionarConsultaActivity.class,"idConsulta",null));
    }

    public void AbrirAbaPerfilConsulta(View v){
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilConsultaActivity.class,"idConsulta",v.getTag().toString()));
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
                funcoes.CriarExpansorMedico(this,LayoutButton,idMedicoAtual,NomeMedicos.get(i),ListaLinearMedicos,ListaImageSeta);
                IdMedicos.add(idMedicoAtual);
            }
            abaMedicos = true;
        }
        else{
            AtualizarBotoes(colunaOrdenar,ordemOrdenar);
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
        else {
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
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        List<Integer> idConsultas = DCOH.buscaIdConsultas("idMedico", idMedicoAberto+"", ordem,IdUsuarioAtual);
        LayoutButton.removeAllViews();
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        for(int i = 0; i<idConsultas.size(); i++){
            Consulta consulta = DCOH.BuscaConsulta(idConsultas.get(i),IdUsuarioAtual);
            Medico medico = DMOH.BuscaMedico(consulta.getIdMedico(),IdUsuarioAtual);
            funcoes.CriarBotoes(this,LayoutButton,idConsultas.get(i),medico.getNome(),
                    ((consulta.getDia() == 0)? "":consulta.getDia() + "/") + ((consulta.getMes() == 0)? "":consulta.getMes() + "/") + ((consulta.getAno() == 0)? "":consulta.getAno() ),
                    consulta.getHora(),R.layout.botao_consulta_completo);
        }
        DCOH.close();
        DMOH.close();
    }

    public  void AtualizarBotoes(String orderColuna, String ordem){
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        List<Consulta> consultas = DCOH.BuscaConsultas(orderColuna,ordem,IdUsuarioAtual);
        LinearLayout LayoutButton = findViewById(R.id.LayoutButton);
        LayoutButton.removeAllViews();
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        for(int i = 0; i<consultas.size(); i++){
            Medico medico = DMOH.BuscaMedico(consultas.get(i).getIdMedico(),IdUsuarioAtual);
            funcoes.CriarBotoes(this,LayoutButton,consultas.get(i).getId(),medico.getNome(),
                    ((consultas.get(i).getDia() == 0)? "":consultas.get(i).getDia() + "/") + ((consultas.get(i).getMes() == 0)? "":consultas.get(i).getMes() + "/") + ((consultas.get(i).getAno() == 0)? "":consultas.get(i).getAno()),
                    consultas.get(i).getHora(),R.layout.botao_consulta_completo);
        }
        DCOH.close();
        DMOH.close();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        ((TextView) parent.getChildAt(0)).setGravity(0x11);
        if(parent.getId() == R.id.spinnerExame1 ) {
            if(position == 0){
                colunaOrdenar = "idMedico";
            }
            else if (position == 1){
                colunaOrdenar = "hora";
            }
            else{
                colunaOrdenar = "ano,mes,dia";
            }
        }
        else if(parent.getId() == R.id.spinnerExame2 ){
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
