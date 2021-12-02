package br.com.higiaorganizadordasaude;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Locale;

import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosRemediosOpenHelper;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Exame;
import dataBase.Medico;
import dataBase.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class perfilActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,MyInterface {
    TextView textNomeUsuario;
    TextView textEmailUsuario;
    GoogleSignInAccount signInAccount;
    CircleImageView imagemPerfil;
    FrameLayout imagemModal;
    int IdUsuarioAtual;
    Spinner spinnerLinguagem;
    int linguaremEscolhida;
    int posicaoSpinner;
    Usuario usuarioAtual;
    boolean mudarLinguagem;
    String retorno = "";
    boolean segundo;
    FuncoesCompartilhadas funcoes;


    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }
        else {
            DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(getApplicationContext());
            usuarioAtual = DUOH.BuscaUsuarioPeloEmail(signInAccount.getEmail());
        }

        // atribuindo Views
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        textNomeUsuario = findViewById(R.id.textNomeUsuario);
        textNomeUsuario.setText(signInAccount.getDisplayName());
        textEmailUsuario = findViewById(R.id.textEmailUsuario);
        textEmailUsuario.setText(signInAccount.getEmail());
        imagemPerfil = findViewById(R.id.imagemPerfil);
        Uri imageUri =  signInAccount.getPhotoUrl();
        Picasso.with(this).load(imageUri).fit().placeholder(R.mipmap.ic_launcher_round).into(imagemPerfil);
        imagemModal = findViewById(R.id.imagemModal);
        spinnerLinguagem =findViewById(R.id.spinnerLinguagem);

        //Carregar spinners
        funcoes.CriarSpinner(this,spinnerLinguagem,R.array.linguagens,null);
        //Atualizar Linguagem
        String[] siglaLinguagem = getResources().getStringArray(R.array.linguagenSigla);
        for(int i = 0;i<siglaLinguagem.length;i++){
            if(getResources().getConfiguration().locale.getLanguage().equals(siglaLinguagem[i]))
            {
                spinnerLinguagem.setSelection(i);
            }
        }
    }
    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }

    public void VoltarAba(View v){
        finish();
    }

    public void AbrirModalDeslogar (View v){
        retorno = "deslogar";
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_deslogarConta),getResources().getString(R.string.texto_deslogarConta), this, this);
    }

    public  void AbrirModalDeletar(View v) {
        retorno = "deletar";
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_deletarConta),getResources().getString(R.string.texto_deletarConta), this, this);
    }

    public void RetornoModal(boolean resultado){
        if(resultado) {
            if(retorno.equals("deslogar")) {
                Deslogar();
            }
            else if(retorno.equals("deletar")) {
                if(!segundo){
                    segundo = true;
                    funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_deletarConta),getResources().getString(R.string.texto_deletarConta2), this, this);
                }
                else {
                    segundo = false;
                    retorno = "";
                    DeletarTudo();
                }
            }
            else if(retorno.equals("linguagem")){
                DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(getApplicationContext());
                DUOH.EditarUsuario(usuarioAtual.getId(), linguaremEscolhida);
                if (linguaremEscolhida == 0) {
                    MudarIdioma("pt");
                } else if (linguaremEscolhida == 1) {
                    MudarIdioma("en");
                }
                DUOH.close();
            }
        }

        else {
            if(retorno.equals("linguagem")) {
                mudarLinguagem = false;
                spinnerLinguagem.setSelection(usuarioAtual.getLinguagem());
            }
            segundo = false;
            retorno = "";
        }
    }

    public void DeletarTudo()
    {
        // Deleta os exames
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        List<Exame> exames = DEOH.BuscaExames("idMedico","ASC",IdUsuarioAtual);
        int deletouImagens = 0;
        try {
            for (int i = 0; i < exames.size();i++)
            {
                Exame exame = exames.get(i);
                DEOH.BuscaImagensExame(exame,IdUsuarioAtual);
                for (int j = 0; j < exame.getNomesImagens().size(); j++) {
                    File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + exame.getNomesImagens().get(j));
                    Uri selectedImageUri = FileProvider.getUriForFile(this, "com.example.higiaexames.provider", f);
                    File f2 = new File(selectedImageUri.getPath());
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    deletouImagens = resolver.delete(selectedImageUri,null,null);
                }
            }
            DEOH.deletarExamePorIdUsuario(IdUsuarioAtual);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Deleta os remedios
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        DROH.deletarRemediosPorIdUsuario(IdUsuarioAtual);
        // Deleta as consultas
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        DCOH.deletarConsultaPorIdUsuario(IdUsuarioAtual);
        // Deleta os médicos
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        List<Medico> medicos = DMOH.BuscaMedicos("nome","ASC",IdUsuarioAtual);
        deletouImagens = 0;
        try {
            for (int i = 0; i < medicos.size();i++)
            {
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + medicos.get(i).getUriImagem());
                Uri selectedImageUri = FileProvider.getUriForFile(this, "com.example.higiaexames.provider", f);
                File f2 = new File(selectedImageUri.getPath());
                ContentResolver resolver = getApplicationContext().getContentResolver();
                deletouImagens = resolver.delete(selectedImageUri,null,null);
            }
            DMOH.deletarMedicoPorIdUsuario(IdUsuarioAtual);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Deslogar ();
    }

    public void Modal(View v) {
        if (imagemModal.getVisibility() == View.INVISIBLE) {
            imagemModal.setVisibility(View.VISIBLE);
        } else {
            imagemModal.setVisibility(View.INVISIBLE);
        }
    }

    public void Deslogar (){
        funcoes.DeslogarGoogle(this);
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(intent);
    }

    public void MudarIdioma (String linguagem){
        Locale local = new Locale(linguagem);
        Locale.setDefault(local);
        Resources resources = this.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = local;
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
        DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(getApplicationContext());
        DUOH.EditarUsuario(usuarioAtual.getId(),linguaremEscolhida);
        DUOH.close();
        Intent intent = new Intent(getApplicationContext(),perfilActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        //linguaremEscolhida = position;
        if(mudarLinguagem) {
            retorno = "linguagem";
            linguaremEscolhida = position;
            funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_linguagem), getResources().getString(R.string.texto_linguagem), this, this);
        }
        Toast.makeText(parent.getContext(), text, Toast.LENGTH_SHORT);
        mudarLinguagem = true;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
