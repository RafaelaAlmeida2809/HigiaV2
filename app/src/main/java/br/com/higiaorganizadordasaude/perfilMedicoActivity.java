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
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import dataBase.Consulta;
import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosRemediosOpenHelper;
import dataBase.Exame;
import dataBase.Medico;
import dataBase.Remedio;
import de.hdodenhof.circleimageview.CircleImageView;

public class perfilMedicoActivity extends AppCompatActivity implements MyInterface {

    ActivityResultLauncher<Intent> activityResultLauncher;
    FrameLayout imagemModal;
    LinearLayout linearLayout;
    List<LinearLayout> listaLinearFilho = new ArrayList<>();
    TextView nomeMedico;
    TextView especialidadeMedico;
    TextView telefoneMedico;
    TextView cepMedico;
    TextView numeroMedico;
    TextView textNomeRua;
    TextView textEstado;
    TextView textCidade;
    Button botaoGoogleMaps;
    Button botaoChamada;
    Button botaoWhats;
    CircleImageView imagemMedico;
    List<ImageView> listaSeta = new ArrayList<>();
    List<String> listaNome = new ArrayList<>();
    int IdUsuarioAtual;
    String idMedico;
    boolean segundo;
    Medico thisMedico;
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_medico);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        IdUsuarioAtual =  funcao.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        nomeMedico = findViewById(R.id.textNomeMedico);
        especialidadeMedico = findViewById(R.id.textEspecialidade);
        telefoneMedico = findViewById(R.id.textTelefone);
        cepMedico = findViewById(R.id.textCep);
        numeroMedico = findViewById(R.id.textNumero);
        imagemModal = findViewById(R.id.imagemModal);
        textNomeRua = findViewById(R.id.textNomeRua);
        textCidade = findViewById(R.id.textCidade);
        textEstado = findViewById(R.id.textEstado);
        botaoGoogleMaps = findViewById(R.id.botaoGoogleMaps);
        botaoChamada = findViewById(R.id.botaoChamada);
        botaoWhats = findViewById(R.id.botaoWhats);
        linearLayout = findViewById(R.id.LinearLayout1);
        imagemMedico = findViewById(R.id.imagemMedico);

        //Receber valor de Editar
        Bundle bundle = getIntent().getExtras();
        idMedico = bundle.getString("idMedico");

        //Inicia os componentes da pagina.
        AtualizarTextPerfil();

        //Retorno da activity
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            ReiniciarAba();
                        }
                        else {ReiniciarAba();}
                    }
                });
    }

    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }

    public void AbrirAbaMedico(View v) {
        Intent intent = new Intent();
        intent.putExtra("retorno","Voltei");
        setResult(RESULT_OK,intent);
        this.finish();
    }

    public void ReiniciarAba() {
        startActivity(funcoes.BundleActivy(this,perfilMedicoActivity.class,"idMedico",idMedico));
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
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        funcao.ModalConfirmacao(getResources().getString(R.string.titulo_delMedico1),getResources().getString(R.string.texto_delMedico1),this,this);
    }

    public void RetornoModal(boolean resultado){
        if(resultado) {
            if(!segundo){
                segundo = true;
                FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
                funcao.ModalConfirmacao(getResources().getString(R.string.titulo_delMedico2),getResources().getString(R.string.texto_delMedico2),this,this);
            }else {
                segundo = false;
                ApagarMedico();
            }
        }else {
            segundo = false;
        }
    }

    public void ApagarMedico() {
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        int deletouImagem = 0;
        try {
            File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + thisMedico.getUriImagem());
            Uri selectedImageUri = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
            File f2 = new File(selectedImageUri.getPath());
            ContentResolver resolver = getApplicationContext().getContentResolver();
            deletouImagem = resolver.delete(selectedImageUri, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        List<Integer> listaIdExames = DEOH.buscaIdExames("idMedico",Integer.parseInt(idMedico)+"","ASC",IdUsuarioAtual);
        for(int i = 0; i<listaIdExames.size();i++){
            Exame exame = DEOH.BuscaExame(listaIdExames.get(i),IdUsuarioAtual);
            DEOH.BuscaImagensExame(exame,IdUsuarioAtual);
            int deletouImagens = 0;
            try {
                for (int j = 0; j < exame.getNomesImagens().size(); j++) {
                    File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + exame.getNomesImagens().get(j));
                    Uri selectedImageUri = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
                    File f2 = new File(selectedImageUri.getPath());
                    ContentResolver resolver = getApplicationContext().getContentResolver();
                    deletouImagens = resolver.delete(selectedImageUri, null, null);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            DEOH.DeletaExame(listaIdExames.get(i),IdUsuarioAtual);
        }
        DEOH.close();
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        List<Integer> listaIdRemedios = DROH.buscaIdRemedio("idMedico", Integer.parseInt(idMedico)+"","ASC", IdUsuarioAtual);
        for(int i = 0; i<listaIdRemedios.size();i++){
            DROH.DeletaRemedio(listaIdRemedios.get(i),IdUsuarioAtual);
        }
        DROH.close();
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        List<Integer> listaIdConsultas = DCOH.buscaIdConsultas("idMedico", Integer.parseInt(idMedico)+"","ASC", IdUsuarioAtual);
        for(int i = 0; i<listaIdConsultas.size();i++){
            DCOH.DeletaConsulta(listaIdConsultas.get(i),IdUsuarioAtual);
        }
        DCOH.close();
        DMOH.DeletaMedico(Integer.parseInt(idMedico),IdUsuarioAtual);
        DMOH.close();
        AbrirAbaMedico(null);
    }

    public void AbrirGoogleMaps(View v) {
        startActivity(funcoes.AbrirGoogleMaps(thisMedico));
    }

    public void EditarMedico(View v) {
        activityResultLauncher.launch(funcoes.BundleActivy(this,adicionarMedicoActivity.class,"idMedico",idMedico));
    }

    public void AbrirChamada (View v){
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" +telefoneMedico.getText()));
        startActivity(intent);
    }

    public void AbrirWhats(View v) {
        Pattern p = Pattern.compile("\\d+");
        Matcher m = p.matcher(thisMedico.getTelefone());
        String telefone="";
        while(m.find()) {
            telefone = telefone+ m.group();
        }
        Uri uriWhats = Uri.parse("https://wa.me/55"+telefone);
        Intent intent = new Intent(Intent.ACTION_VIEW,uriWhats);
        intent.setPackage("com.whatsapp");
        startActivity(intent);
    }

    public void AtivarLinear(String text){
        listaNome.add(text);
        ViewStub stub = new ViewStub(this);
        stub.setLayoutResource(R.layout.expansor_layout);
        linearLayout.addView(stub);
        View inflated = stub.inflate();
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) inflated.getLayoutParams();
        params.setMargins(params.leftMargin, 5, params.rightMargin, params.bottomMargin);
        ImageView seta = inflated.findViewById(R.id.imagemExpandir);
        listaSeta.add(seta);
        Button b = inflated.findViewById(R.id.buttonExpandir);
        b.setTag(listaSeta.size());
        LinearLayout linearLayoutFilho = inflated.findViewById(R.id.LayoutButtonExame);
        linearLayoutFilho.setVisibility(View.INVISIBLE);
        TextView t1 = inflated.findViewById(R.id.textView1);
        t1.setText(text);
        listaLinearFilho.add(linearLayoutFilho);
    }

    public void AbrirMedico(View v) {
        String tagString = v.getTag().toString();
        int tagInt = Integer.parseInt(tagString);
        LinearLayout linear = listaLinearFilho.get(tagInt-1);
        if(linear.getVisibility() == View.INVISIBLE) {
            listaSeta.get(tagInt-1).setRotation(180);
            linear.setVisibility(View.VISIBLE);
            if(listaNome.get(tagInt-1) == "Exames"){
                AtualizarBotoesAbaExame("tipo", "ASC", linear, Integer.parseInt(idMedico));
            }
            else if(listaNome.get(tagInt-1) == "Rem??dios"){
                AtualizarBotoesAbaRemedios("tipo", "ASC", linear, Integer.parseInt(idMedico));
            }
            else if(listaNome.get(tagInt-1) == "Consultas"){
                AtualizarBotoesAbaConsulta("tipo", "ASC", linear, Integer.parseInt(idMedico));
            }
        }
        else {
            listaSeta.get(tagInt-1).setRotation(0);
            linear.removeAllViews();
            linear.setVisibility(View.INVISIBLE);
        }
    }

    public  void AtualizarTextPerfil() {
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        thisMedico = DMOH.BuscaMedico(Integer.parseInt(idMedico),IdUsuarioAtual);
        DMOH.close();
        nomeMedico.setText(thisMedico.getNome());
        especialidadeMedico.setText(thisMedico.getEspecialidade());
        telefoneMedico.setText(thisMedico.getTelefone());
        if((thisMedico.getCep()+"").length()>2) {
            cepMedico.setText(thisMedico.getCep() + "");
        }
        numeroMedico.setText(thisMedico.getNumero()+"");
        textCidade.setText(thisMedico.getCidade()+"");
        textEstado.setText(thisMedico.getEstado()+"");
        textNomeRua.setText(thisMedico.getLogradouro()+"");
        imagemMedico.setLayoutParams(new LinearLayout.LayoutParams(1, 1));
        if(thisMedico.getUriImagem() != null && thisMedico.getUriImagem() !="" && thisMedico.getUriImagem().length() > 4) {
            try {
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + thisMedico.getUriImagem());
                Uri selectedImageUri = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
                imagemMedico.setLayoutParams(new LinearLayout.LayoutParams(350, 350));
                Picasso.with(this).load(selectedImageUri).fit().placeholder(R.mipmap.ic_launcher_round).into(imagemMedico);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(cepMedico.getText().toString() != "" ||textCidade.getText().toString() != ""||textEstado.getText().toString() != ""||textNomeRua.getText().toString() != ""){
            botaoGoogleMaps.setVisibility(View.VISIBLE);
        }
        else {
            botaoGoogleMaps.setVisibility(View.INVISIBLE);
        }
        linearLayout.removeAllViews();
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        List<Integer> exames = DEOH.buscaIdExames("idMedico",Integer.parseInt(idMedico)+"","ASC",IdUsuarioAtual);
        if (exames.size()>0) {
            AtivarLinear("Exames");
        }
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        List<Integer> remedios = DROH.buscaIdRemedio("idMedico",Integer.parseInt(idMedico)+"","ASC",IdUsuarioAtual);
        if (remedios.size()>0) {
            AtivarLinear("Rem??dios");
        }
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        List<Integer> consultas = DCOH.buscaIdConsultas("idMedico",Integer.parseInt(idMedico)+"","ASC",IdUsuarioAtual);
        if (consultas.size()>0) {
            AtivarLinear("Consultas");
        }
        if((thisMedico.getCep()+"").length()<2 && (thisMedico.getNumero()+"").length()<=0 && (thisMedico.getCidade()+"").length()<=0
                && (thisMedico.getEstado()+"").length()<=0 && (thisMedico.getLogradouro()+"").length()<=0) {
            botaoGoogleMaps.setVisibility(View.INVISIBLE);
        }
        if((telefoneMedico.getText()).length()<=0) {
            botaoWhats.setVisibility(View.INVISIBLE);
            botaoChamada.setVisibility(View.INVISIBLE);
        }
    }

    ////////////////////////////////////////////////////CODIGOS DO EXAME/////////////////////////////////////////////////////////////
    public  void AtualizarBotoesAbaExame(String orderColuna, String ordem,LinearLayout LayoutButton, int idMedicoAberto){
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

    public void AbrirAbaPerfilExame(View v) {
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilExameActivity.class,"idExame",v.getTag().toString()));
    }

    ///////////////////////////////////////////////////CODIGOS DO REMEDIO////////////////////////////////////////////////////////////
    public  void AtualizarBotoesAbaRemedios(String orderColuna, String ordem,LinearLayout LayoutButton, int idMedicoAberto){
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        //List<Integer> idRemedios = DROH.buscaIdRemedioInt("idMedico",idMedicoAberto,ordem);
        List<Integer> idRemedios = DROH.buscaIdRemedio("idMedico",idMedicoAberto+"",ordem,IdUsuarioAtual);
        LayoutButton.removeAllViews();
        for(int i = 0; i<idRemedios.size(); i++){
            Remedio remedio = DROH.BuscaRemedio(idRemedios.get(i),IdUsuarioAtual);
            funcoes.CriarBotoes(this,LayoutButton,idRemedios.get(i),remedio.getNome(),remedio.getDosagem(),
                    remedio.getFormato(),R.layout.botao_remedio_completo);
        }
    }

    public void AbrirAbaPerfilRemedio(View v){
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilRemedioActivity.class,"idRemedio",v.getTag().toString()));
    }

    //////////////////////////////////////////////////CODIGOS DA CONSULTA////////////////////////////////////////////////////////////
    public  void AtualizarBotoesAbaConsulta (String orderColuna, String ordem,LinearLayout LayoutButton, int idMedicoAberto) {
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        //List<Integer> idConsultas = DCOH.buscaIdConsultasInt("idMedico", idMedicoAberto, ordem);
        List<Integer> idConsultas = DCOH.buscaIdConsultas("idMedico", idMedicoAberto+"", ordem,IdUsuarioAtual);
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        LayoutButton.removeAllViews();
        for (int i = 0; i < idConsultas.size(); i++) {
            Consulta consulta = DCOH.BuscaConsulta(idConsultas.get(i),IdUsuarioAtual);
            Medico medico = DMOH.BuscaMedico(consulta.getIdMedico(),IdUsuarioAtual);
            funcoes.CriarBotoes(this,LayoutButton,idConsultas.get(i),medico.getNome(),
                    ((consulta.getDia() == 0)? "":consulta.getDia() + "/") + ((consulta.getMes() == 0)? "":consulta.getMes() + "/") + ((consulta.getAno() == 0)? "":consulta.getAno() ),
                    consulta.getHora(),R.layout.botao_consulta_completo);
        }
    }

    public void AbrirAbaPerfilConsulta(View v){
        activityResultLauncher.launch(funcoes.BundleActivy(this,perfilConsultaActivity.class,"idConsulta",v.getTag().toString()));
    }

}

