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
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.text.Normalizer;

import dataBase.Consulta;
import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosRemediosOpenHelper;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Medico;
import dataBase.Remedio;
import dataBase.Usuario;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_consulta);

        //Verificar Loguin
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        IdUsuarioAtual =  funcao.VerificarLoguin(this);
        if(IdUsuarioAtual == -1){
            AbrirLoguin();
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

    public void ApagarConsulta()
    {
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        DCOH.DeletaConsulta(Integer.parseInt(idConsulta),IdUsuarioAtual);
        DCOH.close();
        AbrirAbaConsulta(null);
    }

    public void EditarConsulta(View v)
    {
        Bundle bundle = new Bundle();
        bundle.putString("idConsulta",idConsulta);
        Intent adicionarConsultaActivity = new Intent(this, adicionarConsultaActivity.class);
        adicionarConsultaActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarConsultaActivity);
    }
    public void AbrirPerfilMedico (View v){
        Bundle bundle = new Bundle();
        bundle.putString("idMedico",idMedico);
        Intent perfilMedicoActivity = new Intent(this, perfilMedicoActivity.class);
        perfilMedicoActivity.putExtras(bundle);
        activityResultLauncher.launch(perfilMedicoActivity);
    }
    public void AbrirGoogleMaps(View v)
    {

        //https://www.google.com/maps/dir abre o modo rota
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
        startActivity(mapIntent);
    }
    public  void AtualizarTextPerfil()
    {
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
