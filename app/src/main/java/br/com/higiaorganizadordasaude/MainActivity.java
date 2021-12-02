package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.squareup.picasso.Picasso;

import java.util.Locale;

import dataBase.DadosUsuariosOpenHelper;
import dataBase.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements MyInterface {

    ActivityResultLauncher<Intent> activityResultLauncher;
    private GoogleSignInClient mGoogleSignInClient;
    GoogleSignInAccount signInAccount;
    FrameLayout imagemModal;
    TextView textNomeUsuario;
    CircleImageView imagemPerfil;
    ImageView imagemHigia;
    ImageView imagemFundo;
    int IdUsuarioAtual;
    boolean retornoPerfil;
    Usuario usuarioAtual;
    FuncoesCompartilhadas funcoes;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        textNomeUsuario = findViewById(R.id.textNomeUsuario);
        imagemPerfil = findViewById(R.id.imagemPerfil);
        imagemModal = findViewById(R.id.imagemModal);
        imagemHigia = findViewById(R.id.imagemHigia);
        imagemFundo = findViewById(R.id.imagemFundo);

        //Inicia os componentes da pagina.
        regularTamanho();
        if (signInAccount != null) {
            //preencher os dados da pessoa;
            textNomeUsuario.setText(signInAccount.getDisplayName());
            Uri imageUri =  signInAccount.getPhotoUrl();
            Picasso.with(this).load(imageUri).fit().placeholder(R.mipmap.ic_launcher_round).into(imagemPerfil);
        }

        //Mudar idioma
        String[] siglaLinguagem = getResources().getStringArray(R.array.linguagenSigla);
        DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(getApplicationContext());
        Usuario usuario = DUOH.BuscaUsuarioPeloEmail(signInAccount.getEmail());
        if(!getResources().getConfiguration().locale.getLanguage().equals(siglaLinguagem[usuario.getLinguagem()])) {
            MudarIdioma(siglaLinguagem[usuario.getLinguagem()]);
        }

        //Retorno
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ReinicarActivity();
                        }
                        else {
                            ReinicarActivity();
                        }
                    }
                });
    }

    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }

    public void ReinicarActivity(){
        Intent thisActivity = new Intent(this, MainActivity.class);
        startActivity(thisActivity);
        finish();
    }

    public void abrirOpcoes(View v){
        if (imagemModal.getVisibility() == View.INVISIBLE) {
            imagemModal.setVisibility(View.VISIBLE);
        } else {
            imagemModal.setVisibility(View.INVISIBLE);
        }
    }

    public void AbrirAbaMedicos(View v){
        Intent medicoActivity = new Intent(this,medicoActivity.class);
        startActivity(medicoActivity);
    }

    public void AbrirAbaExames(View v){
        Intent exameActivity = new Intent(this,exameActivity.class);
        startActivity(exameActivity);
    }

    public void AbrirAbaConsultas(View v){
        Intent consultaActivity = new Intent(this,consultaActivity.class);
        startActivity(consultaActivity);
    }

    public void AbrirAbaRemedio(View v){
        Intent remedioActivity = new Intent(this,remedioActivity.class);
        startActivity(remedioActivity);
    }

    public  void abrirPerfil(View v){
        Intent perfilActivity = new Intent(this,perfilActivity.class);
        activityResultLauncher.launch(perfilActivity);
    }

    public void ModalDeslogar (View v) {
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_deslogarConta),getResources().getString(R.string.texto_deslogarConta), this, this);
    }

    public void RetornoModal(boolean resultado) {
        if (resultado) {
            funcoes.DeslogarGoogle(this);
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            finish();
            startActivity(intent);
        }
    }

    public  void regularTamanho()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int tamanhoHigia = ((displayMetrics.heightPixels*550)/1920);
        ImageView imagemHigia = findViewById(R.id.imagemHigia);
        imagemHigia.setLayoutParams(new LinearLayout.LayoutParams(tamanhoHigia, tamanhoHigia));
    }

    public void MudarIdioma (String linguagem){
        Locale local = new Locale(linguagem);
        Locale.setDefault(local);
        Resources resources = this.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = local;
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
        Intent intent = new Intent(getApplicationContext(),perfilActivity.class);
        startActivity(intent);
        finish();
    }
}

