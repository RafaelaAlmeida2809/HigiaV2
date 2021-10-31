package br.com.higiaorganizadordasaude;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosRemediosOpenHelper;
import dataBase.Medico;
import dataBase.Remedio;

public class perfilRemedioActivity extends AppCompatActivity implements MyInterface {
    ActivityResultLauncher<Intent> activityResultLauncher;
    private FrameLayout imagemModal;
    public boolean AtivadoModal = false;
    public String idRemedio;
    public String idMedico;
    public TextView nomeRemedio;
    public TextView nomeMedico;
    public TextView dosagemRemedio;
    public TextView formatoRemedio;
    public TextView dataInicio;
    public TextView dataFim;
    public TextView horario1;
    public TextView horario2;
    public TextView horario3;
    public TextView horario4;
    public TextView quantidadeRemedio;
    int IdUsuarioAtual;
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_remedio);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        nomeRemedio = findViewById(R.id.textNomeRemedio);
        nomeMedico = findViewById(R.id.textNomeMedico);
        dosagemRemedio = findViewById(R.id.textDosagemRemedio);
        formatoRemedio = findViewById(R.id.textFormatoRemedio);
        dataInicio = findViewById(R.id.textDataInicioRemedio);
        dataFim = findViewById(R.id.textDataFimRemedio);
        horario1 = findViewById(R.id.textHorarioRemedio);
        horario2 = findViewById(R.id.textHorarioRemedio2);
        horario3 = findViewById(R.id.textHorarioRemedio3);
        horario4 = findViewById(R.id.textHorarioRemedio4);
        quantidadeRemedio = findViewById(R.id.textQuantidadeRemedio);
        imagemModal = findViewById(R.id.imagemModal);

        //Receber valor de Editar
        Bundle bundle = getIntent().getExtras();
        idRemedio = bundle.getString("idRemedio");
        //Inicia os componentes da pagina.
        AtualizarTextPerfil();

        //Retorno da activity
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            ReiniciarAba();
                        }
                    }
                });
    }
    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }

    public void AbrirAbaRemedio(View v) {
        Intent intent = new Intent();
        intent.putExtra("retorno","Voltei");
        setResult(RESULT_OK,intent);
        this.finish();
    }
    public void Modal(View v) {
        if (imagemModal.getVisibility() == View.INVISIBLE) {
            imagemModal.setVisibility(View.VISIBLE);
        } else {
            imagemModal.setVisibility(View.INVISIBLE);
        }
    }
    public void ModalApagar (View v)
    {
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_delRemedio),getResources().getString(R.string.texto_delRemedio),this,this);
    }
    public void RetornoModal(boolean resultado) {
        if (resultado) {
            ApagarRemedio();
        }
    }
    public void ApagarRemedio()
    {
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        DROH.DeletaRemedio(Integer.parseInt(idRemedio),IdUsuarioAtual);
        finish();
    }

    public void ReiniciarAba() {
        startActivity(funcoes.BundleActivy(this,perfilRemedioActivity.class,"idRemedio",idRemedio));
        finish();
    }
    public void EditarRemedio(View v) {
        /*Bundle bundle = new Bundle();
        bundle.putString("idRemedio",idRemedio);
        Intent adicionarRemedioActivity = new Intent(this, adicionarRemedioActivity.class);
        adicionarRemedioActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarRemedioActivity);*/
        activityResultLauncher.launch(funcoes.BundleActivy(this,adicionarRemedioActivity.class,"idRemedio",idRemedio));
    }
    public void AbrirPerfilMedico (View v){
        /*Bundle bundle = new Bundle();
        bundle.putString("idMedico",idMedico);
        Intent perfilMedicoActivity = new Intent(this, perfilMedicoActivity.class);
        perfilMedicoActivity.putExtras(bundle);
        activityResultLauncher.launch(perfilMedicoActivity);*/
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilMedicoActivity.class,"idMedico",idMedico));
    }
    public  void AtualizarTextPerfil()
    {
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        Remedio remedio = DROH.BuscaRemedio(Integer.parseInt(idRemedio),IdUsuarioAtual);
        idMedico = remedio.getIdMedico()+"";
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        Medico medico = DMOH.BuscaMedico(Integer.parseInt(idMedico),IdUsuarioAtual);
        nomeRemedio.setText(remedio.getNome());
        nomeMedico.setText(medico.getNome());
        dosagemRemedio.setText(remedio.getDosagem());
        formatoRemedio.setText(remedio.getFormato());
        dataInicio.setText(((remedio.getDiaInicio() == 0)? "":remedio.getDiaInicio() + "/") + ((remedio.getMesInicio() == 0)? "":remedio.getMesInicio() + "/") + ((remedio.getAnoInicio() == 0)? "":remedio.getAnoInicio() ));
        dataFim.setText(((remedio.getDiaFim() == 0)? "":remedio.getDiaFim() + "/") + ((remedio.getMesFim() == 0)? "":remedio.getMesFim() + "/") + ((remedio.getAnoFim() == 0)? "":remedio.getAnoFim() ));
        horario1.setText(remedio.getHorario1());
        horario2.setText(remedio.getHorario2());
        horario3.setText(remedio.getHorario3());
        horario4.setText(remedio.getHorario4());
        quantidadeRemedio.setText(remedio.getQuantidade());
        DMOH.close();
        DROH.close();
    }

}

