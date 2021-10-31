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
import android.widget.FrameLayout;
import android.widget.TextView;
import dataBase.Consulta;
import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.Medico;

public class perfilConsultaActivity extends AppCompatActivity implements MyInterface{
    private FrameLayout imagemModal;
    public String idConsulta;
    public String idMedico;
    TextView textData;
    TextView textHorario;
    TextView nomeMedico;
    TextView especialidadeMedico;
    ActivityResultLauncher<Intent> activityResultLauncher;
    int IdUsuarioAtual;
    Medico thisMedico;
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_consulta);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        IdUsuarioAtual =  funcao.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        imagemModal = findViewById(R.id.imagemModal);
        textData = findViewById(R.id.textData);
        textHorario = findViewById(R.id.textHorario);
        nomeMedico = findViewById(R.id.textNomeMedico);
        especialidadeMedico = findViewById(R.id.textEspecialidadeMedico);

        //Receber valor de Editar
        Bundle bundle = getIntent().getExtras();
        idConsulta = bundle.getString("idConsulta");

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

    public void AbrirAbaConsulta(View v) {
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

    public void AbrirModalApagar(View v){
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        funcao.ModalConfirmacao(getResources().getString(R.string.titulo_delConsulta),getResources().getString(R.string.texto_delConsulta),this,this);
    }

    public void RetornoModal(boolean resultado){
        if(resultado) {
            ApagarConsulta();
        }
    }

    public void ApagarConsulta(){
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        DCOH.DeletaConsulta(Integer.parseInt(idConsulta),IdUsuarioAtual);
        DCOH.close();
        AbrirAbaConsulta(null);
    }

    public void ReiniciarAba() {
        startActivity(funcoes.BundleActivy(this,perfilConsultaActivity.class,"idConsulta",idConsulta));
        finish();
    }

    public void EditarConsulta(View v){
        activityResultLauncher.launch(funcoes.BundleActivy(this,adicionarConsultaActivity.class,"idConsulta",idConsulta));
    }

    public void AbrirPerfilMedico (View v){
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilMedicoActivity.class,"idMedico",idMedico));
    }

    public void AbrirGoogleMaps(View v){
        startActivity(funcoes.AbrirGoogleMaps(thisMedico));
    }

    public  void AtualizarTextPerfil(){
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        Consulta consulta = DCOH.BuscaConsulta(Integer.parseInt(idConsulta),IdUsuarioAtual);
        idMedico = consulta.getIdMedico()+"";
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        thisMedico = DMOH.BuscaMedico(Integer.parseInt(idMedico),IdUsuarioAtual);
        nomeMedico.setText(thisMedico.getNome());
        especialidadeMedico.setText(thisMedico.getEspecialidade());
        textData.setText(((consulta.getDia() == 0)? "":consulta.getDia() + "/") + ((consulta.getMes() == 0)? "":consulta.getMes() + "/") + ((consulta.getAno() == 0)? "":consulta.getAno() ));
        textHorario.setText(consulta.getHora());
        DMOH.close();
        DCOH.close();
    }
}

