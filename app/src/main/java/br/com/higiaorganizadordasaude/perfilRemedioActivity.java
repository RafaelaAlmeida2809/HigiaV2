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
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosRemediosOpenHelper;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Exame;
import dataBase.Medico;
import dataBase.Remedio;
import dataBase.Usuario;

public class perfilRemedioActivity extends AppCompatActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_remedio);

        //Verificar Loguin
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        IdUsuarioAtual =  funcao.VerificarLoguin(this);
        if(IdUsuarioAtual == -1){
            AbrirLoguin();
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
                            AtualizarTextPerfil();
                        }
                    }
                });
    }
    public void AbrirLoguin(){
        Intent LoguinActivity = new Intent(getApplicationContext(),LoguinActivity.class);
        finish();
        startActivity(LoguinActivity);
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(R.string.titulo_delRemedio);
        builder.setMessage(R.string.texto_delRemedio);
        builder.setPositiveButton(R.string.sim,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ApagarRemedio();
                    }
                });
        builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void ApagarRemedio()
    {
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        DROH.DeletaRemedio(Integer.parseInt(idRemedio),IdUsuarioAtual);
        finish();
    }

    public void EditarRemedio(View v)
    {
        Bundle bundle = new Bundle();
        bundle.putString("idRemedio",idRemedio);
        Intent adicionarRemedioActivity = new Intent(this, adicionarRemedioActivity.class);
        adicionarRemedioActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarRemedioActivity);
    }
    public void AbrirPerfilMedico (View v){
        Bundle bundle = new Bundle();
        bundle.putString("idMedico",idMedico);
        Intent perfilMedicoActivity = new Intent(this, perfilMedicoActivity.class);
        perfilMedicoActivity.putExtras(bundle);
        activityResultLauncher.launch(perfilMedicoActivity);
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

