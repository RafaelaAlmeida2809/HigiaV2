package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;
import java.util.Locale;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Usuario;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // atribuindo Views
        textNomeUsuario = findViewById(R.id.textNomeUsuario);
        imagemPerfil = findViewById(R.id.imagemPerfil);
        imagemModal = findViewById(R.id.imagemModal);
        imagemHigia = findViewById(R.id.imagemHigia);
        imagemFundo = findViewById(R.id.imagemFundo);

        //Verificar Loguin
        createRequest();
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(getApplicationContext());
        usuarioAtual = DUOH.BuscaUsuarioPeloEmail(signInAccount.getEmail());
        if (usuarioAtual.getEmail() != null || usuarioAtual.getEmail().length() > 0) {
            IdUsuarioAtual = usuarioAtual.getId();
        }
        else {
            Intent LoguinActivity = new Intent(getApplicationContext(),LoguinActivity.class);
            finish();
            startActivity(LoguinActivity);
        }
        DUOH.close();

        //Inicia os componentes da pagina.
        regularTamanho();
        if (signInAccount != null) {
            //preencher os dados da pessoa;
            textNomeUsuario.setText(signInAccount.getDisplayName());
            Uri imageUri =  signInAccount.getPhotoUrl();
            Picasso.with(this).load(imageUri).fit().placeholder(R.mipmap.ic_launcher_round).into(imagemPerfil);
        }
        //Retorno
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {

                        }
                        else if(retornoPerfil) {
                            ReinicarActivity();
                        }
                    }
                });
    }

    public void createRequest() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
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
        Toast.makeText(getApplicationContext(), "Ainda em desenvolvimento", Toast.LENGTH_SHORT).show();
    }

    public void AbrirAbaRemedio(View v){
        Toast.makeText(getApplicationContext(), "Ainda em desenvolvimento", Toast.LENGTH_SHORT).show();
    }

    public  void abrirPerfil(View v){
        Toast.makeText(getApplicationContext(), "Ainda em desenvolvimento", Toast.LENGTH_SHORT).show();
    }

    public void ModalDeslogar (View v)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle("Deslogar conta");
        builder.setMessage("Você tem certeza que deseja deslogar?");
        builder.setPositiveButton("Sim",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Deslogar();
                    }
                });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void Deslogar (){
        FirebaseAuth.getInstance().signOut();
        mGoogleSignInClient.signOut();
        Intent intent = new Intent(getApplicationContext(),LoguinActivity.class);
        finish();
        startActivity(intent);
    }

    public  void regularTamanho()
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int tamanhoHigia = ((displayMetrics.heightPixels*550)/1920);
        ImageView imagemHigia = findViewById(R.id.imagemHigia);
        imagemHigia.setLayoutParams(new LinearLayout.LayoutParams(tamanhoHigia, tamanhoHigia));
    }
}

