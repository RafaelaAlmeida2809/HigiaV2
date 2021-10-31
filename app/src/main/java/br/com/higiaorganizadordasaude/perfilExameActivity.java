package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.Exame;
import dataBase.Medico;

public class perfilExameActivity extends AppCompatActivity implements MyInterface {

    ActivityResultLauncher<Intent> activityResultLauncher;
    FrameLayout imagemModal;
    RelativeLayout modalImagem;
    TextView tipoExame;
    TextView parteCorpoExame;
    TextView nomeMedicoExame;
    TextView dataExame;
    Button botaoEsquerdo;
    Button botaoDireito;
    List<Button> listaBotoesImagens = new ArrayList<>();
    int IdUsuarioAtual;
    int fotoAbertaAtual;
    int quantidadeImagens;
    String idExame;
    String idMedico;
    String nomeUsuario;
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_exame);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        tipoExame = findViewById(R.id.textTipoExame);
        parteCorpoExame = findViewById(R.id.textParteCorpo);
        nomeMedicoExame = findViewById(R.id.textNomeMedico);
        dataExame = findViewById(R.id.textDataExame);
        imagemModal = findViewById(R.id.imagemModal);
        modalImagem = findViewById(R.id.modalImagem);
        botaoEsquerdo= findViewById(R.id.botaoEsquerdo);
        botaoDireito= findViewById(R.id.botaoDireito);

        //Receber valor de Editar
        Bundle bundle = getIntent().getExtras();
        idExame = bundle.getString("idExame");

        //Inicia os componentes da pagina.
        CarregarButtons();
        AtualizarTextPerfil();

        //Retorno da activity
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            //AtualizarTextPerfil();
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

    public void AbrirAbaExame(View v) {
        Intent intent = new Intent();
        intent.putExtra("retorno","Voltei");
        setResult(RESULT_OK,intent);
        this.finish();
    }
    public void ReiniciarAba() {
        /*Bundle bundle = new Bundle();
        bundle.putString("idExame",idExame);
        Intent thisActivity = new Intent(this, perfilExameActivity.class);
        thisActivity.putExtras(bundle);
        startActivity(thisActivity);*/
        startActivity(funcoes.BundleActivy(this,perfilExameActivity.class,"idExame",idExame));
        finish();
    }

    public void Modal(View v) {
        if (imagemModal.getVisibility() == View.INVISIBLE) {
            imagemModal.setVisibility(View.VISIBLE);
        }
        else {
            imagemModal.setVisibility(View.INVISIBLE);
        }
    }
    public  void AbrirModalDeletar(View v) {
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_delExame),getResources().getString(R.string.texto_delExame),this,this);
    }
    public void RetornoModal(boolean resultado){
        if(resultado) {
            ApagarExame();
        }
    }

    public void ApagarExame() {
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        Exame exame = DEOH.BuscaExame(Integer.parseInt(idExame),IdUsuarioAtual);
        DEOH.BuscaImagensExame(exame,IdUsuarioAtual);
        int deletouImagens = 0;
        try {

            List<String> listaImagens = exame.getNomesImagens();
            for (int i = 0; i < listaImagens.size(); i++) {
                if(!listaImagens.get(i).toLowerCase().contains("pdf")) {
                    File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + listaImagens.get(i));
                    Uri selectedImageUri = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
                    File f2 = new File(selectedImageUri.getPath());
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    deletouImagens = resolver.delete(selectedImageUri, null, null);
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if( exame.getNomesImagens().size() <=0 || deletouImagens >0) {
            DEOH.DeletaExame(Integer.parseInt(idExame),IdUsuarioAtual);
            AbrirAbaExame(null);
        }
    }

    public void AbrirPerfilMedico (View v){
        /*Bundle bundle = new Bundle();
        bundle.putString("idMedico",idMedico);
        Intent perfilMedicoActivity = new Intent(this, perfilMedicoActivity.class);
        perfilMedicoActivity.putExtras(bundle);
        activityResultLauncher.launch(perfilMedicoActivity);*/
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilMedicoActivity.class,"idMedico",idMedico));
    }
    public void EditarExame(View v) {
        /*Bundle bundle = new Bundle();
        bundle.putString("idExame",idExame);
        Intent adicionarExameActivity = new Intent(this, adicionarExameActivity.class);
        adicionarExameActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarExameActivity);*/
        activityResultLauncher.launch(funcoes.BundleActivy(this,adicionarExameActivity.class,"idExame",idExame));

    }

    public void AbrirImagem(View v) {
        fotoAbertaAtual = Integer.parseInt(v.getTag().toString());
        if(fotoAbertaAtual < quantidadeImagens){
            try {
                modalImagem.setVisibility(View.VISIBLE);
                ImageView imagemModal = findViewById(R.id.localImagem);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) listaBotoesImagens.get(fotoAbertaAtual).getBackground();
                imagemModal.setBackground(bitmapDrawable);
                if( fotoAbertaAtual == 0 ) {
                    botaoEsquerdo.setVisibility(View.INVISIBLE);
                }
                else {
                    botaoEsquerdo.setVisibility(View.VISIBLE);
                }
                if(fotoAbertaAtual == quantidadeImagens -1){
                    botaoDireito.setVisibility(View.INVISIBLE);
                }
                else {

                    botaoDireito.setVisibility(View.VISIBLE);
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public  void FecharModal(View v) {
        modalImagem.setVisibility(View.INVISIBLE);
    }

    public void MudarFoto(View v){
        int valorBotao = Integer.parseInt(v.getTag().toString());
        ImageView imagemModal = findViewById(R.id.localImagem);
        BitmapDrawable bitmapDrawable = (BitmapDrawable) listaBotoesImagens.get(fotoAbertaAtual + valorBotao).getBackground();
        imagemModal.setBackground(bitmapDrawable);
        if((fotoAbertaAtual + valorBotao) <=0 ) {
            botaoEsquerdo.setVisibility(View.INVISIBLE);
        }
        else {
            botaoEsquerdo.setVisibility(View.VISIBLE);
        }
        if((fotoAbertaAtual + valorBotao) >= (quantidadeImagens -1)){
            botaoDireito.setVisibility(View.INVISIBLE);
        }
        else {

            botaoDireito.setVisibility(View.VISIBLE);
        }
        fotoAbertaAtual = fotoAbertaAtual+ valorBotao;

    }

    public  void Compartilhar(View v) {
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        Exame exame = DEOH.BuscaExame(Integer.parseInt(idExame),IdUsuarioAtual);
        DEOH.BuscaImagensExame(exame,IdUsuarioAtual);
        Intent email = new Intent(Intent.ACTION_SEND);
        email.putExtra(Intent.EXTRA_SUBJECT, R.string.exame_de +" "+ nomeUsuario);
        email.putExtra(Intent.EXTRA_TEXT, R.string.segue_anexo + " " + tipoExame.getText()+ " "+ R.string.do_a + " " + parteCorpoExame.getText()+
                ((dataExame.getText() == "")? "":", " + R.string.na_data_de+" " + dataExame.getText()));
        for (int i = 0; i < exame.getNomesImagens().size(); i++) {
            try {
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + exame.getNomesImagens().get(i));
                Uri arquivo = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
                email.putExtra(Intent.EXTRA_STREAM, arquivo);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DEOH.close();
        email.setType("message/rfc822");
        startActivity(Intent.createChooser(email, "Choose an Email client :"));
    }


    public  void AtualizarTextPerfil() {
        botaoEsquerdo.setTag(-1);
        botaoDireito.setTag(1);
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        Exame exame = DEOH.BuscaExame(Integer.parseInt(idExame),IdUsuarioAtual);
        DEOH.BuscaImagensExame(exame,IdUsuarioAtual);
        idMedico = exame.getIdMedico()+"";
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        Medico medico = DMOH.BuscaMedico(Integer.parseInt(idMedico),IdUsuarioAtual);
        tipoExame.setText(exame.getTipo());
        parteCorpoExame.setText(exame.getParteCorpo());
        nomeMedicoExame.setText(medico.getNome());
        dataExame.setText(((exame.getDia() == 0)? "":exame.getDia() + "/") + ((exame.getMes() == 0)? "":exame.getMes() + "/") + ((exame.getAno() == 0)? "":exame.getAno() ));
        String a = "";
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int tamanhoWidth = ((displayMetrics.widthPixels*205)/1080);
        int tamanhoHeight = ((displayMetrics.heightPixels*275)/1920);
        quantidadeImagens =  exame.getNomesImagens().size();
        for (int i = 0; i < exame.getNomesImagens().size(); i++){
            try {
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + exame.getNomesImagens().get(i));
                Uri selectedImageUri = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap plantPicture = BitmapFactory.decodeStream(inputStream);
                    BitmapDrawable bitDraw = new BitmapDrawable(getApplicationContext().getResources(), plantPicture);
                    listaBotoesImagens.get(i).setBackground(bitDraw);
                    listaBotoesImagens.get(i).setLayoutParams(new LinearLayout.LayoutParams(tamanhoWidth, tamanhoHeight));
                    listaBotoesImagens.get(i).setTag(i);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void CarregarButtons() {
        LinearLayout LayoutModal = findViewById(R.id.LayoutVertical);
        int childCount = LayoutModal.getChildCount();
        for (int i = 0;i<childCount;i++) {
            View child = LayoutModal.getChildAt(i);
            LinearLayout childLayout = findViewById(child.getId());
            int grandChildCount = childLayout.getChildCount();
            for (int j = 0;j<grandChildCount;j++) {
                View grandChild = childLayout.getChildAt(j);
                Button b = findViewById(grandChild.getId());
                listaBotoesImagens.add(b);
            }
        }
    }
}

