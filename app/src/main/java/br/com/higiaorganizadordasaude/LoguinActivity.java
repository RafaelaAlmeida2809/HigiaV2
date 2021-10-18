package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Locale;

import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosRemediosOpenHelper;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Usuario;

public class LoguinActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    ActivityResultLauncher<Intent> activityResultLauncher;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    GoogleSignInAccount signInAccount;
    Button botaoLogarGoogle;
    Spinner spinnerLinguagem;
    int linguaremEscolhida;
    boolean primeiro = true;
    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        FirebaseUser usuario = mAuth.getCurrentUser();
        if(usuario!= null)
        {
            Toast.makeText(getApplicationContext(), R.string.carregando, Toast.LENGTH_SHORT).show();
            AbrirAbaInicial();
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loguin);

        //Verificar Loguin
        mAuth = FirebaseAuth.getInstance();
        createRequest();
        FirebaseUser usuario = mAuth.getCurrentUser();
        if(usuario!= null)
        {
            AbrirAbaInicial();
        }

        // atribuindo Views
        botaoLogarGoogle = findViewById(R.id.google_singIn);

        //Carregar spinners
        spinnerLinguagem =findViewById(R.id.spinnerLinguagem);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.linguagens, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLinguagem.setAdapter(adapter);
        spinnerLinguagem.setOnItemSelectedListener(this);

        //Atualizar Linguagem
        String[] siglaLinguagem = getResources().getStringArray(R.array.linguagenSigla);
        for(int i = 0;i<siglaLinguagem.length;i++){
            if(getResources().getConfiguration().locale.getLanguage().equals(siglaLinguagem[i]))
            {
                spinnerLinguagem.setSelection(i);
            }
        }

        //Atualizar componente activity
        regularTamanho();

        //Retorno
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                            try {
                                // Google Sign In was successful, authenticate with Firebase
                                GoogleSignInAccount account = task.getResult(ApiException.class);
                                firebaseAuthWithGoogle(account.getIdToken());
                            } catch (ApiException e) {
                                // Google Sign In failed, update UI appropriately
                                Toast.makeText(getApplicationContext(), "Erro no loguin", Toast.LENGTH_SHORT).show();
                            }
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

    public void signIn(View v) {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        Toast.makeText(getApplicationContext(), R.string.aguarde, Toast.LENGTH_SHORT).show();
        activityResultLauncher.launch(signInIntent);

    }
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            AbrirAbaInicial();

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.erro_conexao, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    public void AbrirAbaInicial()
    {
        DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(getApplicationContext());
        signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if(DUOH.QuantidadeUsuarios()<=0){
            UpgradeBanco();
        }
        Usuario usuario = DUOH.BuscaUsuarioPeloEmail(signInAccount.getEmail());
        if(usuario.getEmail() == null || usuario.getEmail().length() <= 0){
            DUOH.AdicionarNovoUsuario(signInAccount.getEmail(),linguaremEscolhida);

        }
        else {
            DUOH.EditarUsuario(usuario.getId(),linguaremEscolhida);
        }
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        finish();
        startActivity(intent);
    }
    public void UpgradeBanco() {
        //
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        SQLiteDatabase db = DMOH.getReadableDatabase();
        DMOH.onUpgrade(db, 1, 1);
        DMOH.close();
        //
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        SQLiteDatabase db2 = DEOH.getReadableDatabase();
        DEOH.onUpgrade(db2, 1, 1);
        DEOH.close();
        //
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        SQLiteDatabase db3 = DROH.getReadableDatabase();
        DROH.onUpgrade(db3, 1, 1);
        DROH.close();
        //
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        SQLiteDatabase db4 = DCOH.getReadableDatabase();
        DCOH.onUpgrade(db4, 1, 1);
        DCOH.close();
        //
        DadosUsuariosOpenHelper DUOH = new DadosUsuariosOpenHelper(getApplicationContext());
        SQLiteDatabase db5 = DUOH.getReadableDatabase();
        DUOH.onUpgrade(db5, 1, 1);
        DUOH.close();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        GoogleSignInAccount account = null;
        boolean b = false;
        try {
            // Google Sign In was successful, authenticate with Firebase
            account = task.getResult(ApiException.class);
            b = true;
        } catch (ApiException e) {
            // Google Sign In failed, update UI appropriately
            Toast.makeText(getApplicationContext(), "Erro 1", Toast.LENGTH_SHORT).show();
        }
        try {
            if(b) {
                firebaseAuthWithGoogle(account.getIdToken());
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Erro 2", Toast.LENGTH_SHORT).show();
        }
    }

    public void MudarIdioma (String linguagem){
        Locale local = new Locale(linguagem);
        Locale.setDefault(local);
        Resources resources = this.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.locale = local;
        resources.updateConfiguration(configuration,resources.getDisplayMetrics());
        AtualizarPagina();

    }
    public  void AtualizarPagina(){
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
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        linguaremEscolhida = position;
        if(primeiro){
            primeiro = false;
        }
        else {
            if (linguaremEscolhida == 0) {
                MudarIdioma("pt");
            } else if (linguaremEscolhida == 1) {
                MudarIdioma("en");
            }
        }
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
