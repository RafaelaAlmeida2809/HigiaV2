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
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.Exame;

public class exameActivity extends AppCompatActivity  implements AdapterView.OnItemSelectedListener,MyInterface{

    ActivityResultLauncher<Intent> activityResultLauncher;
    CheckBox checkMedico;
    Spinner spinner1;
    Spinner spinner2;
    String colunaOrdenar;
    String ordemOrdenar;
    List<String> NomeMedicos = new ArrayList<>();
    List<Integer> IdMedicos = new ArrayList<>();
    List<LinearLayout> ListaLinearMedicos = new ArrayList<>();
    List<ImageView> ListaImageSeta = new ArrayList<>();
    boolean abaMedicos = false;
    int IdUsuarioAtual;
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exame);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        checkMedico = findViewById(R.id.checkBoxMedico);
        spinner1 = findViewById(R.id.spinnerExame1);
        spinner2 = findViewById(R.id.spinnerExame2);

        //Carregar spinners
        funcoes.CriarSpinner(this,spinner1,R.array.ordenar_Exame_Coluna,null);
        funcoes.CriarSpinner(this,spinner2,R.array.ordenar_Ordem,null);


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

    public void AbrirAbaInicial(View v) {
        this.finish();
    }

    public void AbrirAbaAdicionarExame(View v){
        activityResultLauncher.launch(funcoes.BundleActivy(this,adicionarExameActivity.class,"idExame",null));
    }

    public void AbrirAbaPerfilExame(View v){
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilExameActivity.class,"idExame",v.getTag().toString()));
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
                int idMedicoAtual = DMOH.buscaIdMedico("nome", "'"+NomeMedicos.get(i)+"'", "ASC",IdUsuarioAtual).get(0);
                funcoes.CriarExpansorMedico(this,LayoutButton,idMedicoAtual,NomeMedicos.get(i),ListaLinearMedicos,ListaImageSeta);
                IdMedicos.add(idMedicoAtual);
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
    public void RetornoModal(boolean a){}


    public  void AtualizarBotoesAbaMedicos(String orderColuna, String ordem,LinearLayout LayoutButton, int idMedicoAberto){
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        List<Integer> idExames = DEOH.buscaIdExames("idMedico",idMedicoAberto+"",ordem,IdUsuarioAtual);
        LayoutButton.removeAllViews();
        for(int i = 0; i<idExames.size(); i++)
        {
            Exame exame = DEOH.BuscaExame(idExames.get(i),IdUsuarioAtual);
            funcoes.CriarBotoes(this,LayoutButton,idExames.get(i),exame.getTipo(),exame.getParteCorpo(),
                    ((exame.getDia() == 0)? "":exame.getDia() + "/") + ((exame.getMes() == 0)? "":exame.getMes() + "/") + ((exame.getAno() == 0)? "":exame.getAno() )
            ,R.layout.botao_exame_completo);
        }
    }

    public  void AtualizarBotoes(String orderColuna, String ordem) {
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        List<Exame> exames = DEOH.BuscaExames(orderColuna,ordem,IdUsuarioAtual);
        LinearLayout LayoutButton = findViewById(R.id.LayoutButton);
        LayoutButton.removeAllViews();
        for(int i = 0; i<exames.size(); i++){
            funcoes.CriarBotoes(this,LayoutButton,exames.get(i).getId(),exames.get(i).getTipo(),
                    exames.get(i).getParteCorpo(),((exames.get(i).getDia() == 0)? "":exames.get(i).getDia() + "/") + ((exames.get(i).getMes() == 0)? "":exames.get(i).getMes() + "/") + ((exames.get(i).getAno() == 0)? "":exames.get(i).getAno() )
            ,R.layout.botao_exame_completo);
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        if(parent.getId() == R.id.spinnerExame1 ) {
            if(position == 0){
                colunaOrdenar = "tipo";
            }
            else if (position == 1){
                colunaOrdenar = "parteCorpo";
            }
            else{
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

