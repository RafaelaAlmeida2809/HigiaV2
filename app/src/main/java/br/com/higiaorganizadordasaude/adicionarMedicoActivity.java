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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import dataBase.DadosMedicosOpenHelper;
import dataBase.Medico;


public class adicionarMedicoActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, MyInterface  {

    ActivityResultLauncher<Intent> activityResultLauncher;
    HashMap<String,String> dadosCEP = new HashMap<>();
    JSONObject jsonObjectBase = new JSONObject();
    EditText textoNomeRua;
    EditText numeroTelefone;
    EditText textNomeMedico;
    EditText textEspecialidade;
    EditText telefoneMedico;
    EditText textCep;
    EditText textNumero;
    EditText textEstado;
    EditText textCidade;
    EditText textBairro;
    TextView textNomePagina;
    ImageView imagemMedico;
    Button botaoDeletar;
    int IdUsuarioAtual;
    boolean possuiFoto = false;
    boolean pegarFotoGaleria;
    String currentPhotoPath;
    String idMedico;
    String funcaoRecebida;
    String valorJson ="";
    URL urlCep;
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_medico);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login

        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        textoNomeRua = findViewById(R.id.textNomeRua);
        textEstado =findViewById(R.id.textEstado);
        textCidade =findViewById(R.id.textCidade);
        textBairro= findViewById(R.id.textBairro);
        numeroTelefone = findViewById(R.id.telefoneMedico);
        textNomeMedico = findViewById(R.id.textNomeMedico);
        textEspecialidade = findViewById(R.id.textEspecialidade);
        telefoneMedico = findViewById(R.id.telefoneMedico);
        textCep = findViewById(R.id.textCep);
        textNumero = findViewById(R.id.textNumero);
        textNomePagina = findViewById(R.id.textNomePagina);
        imagemMedico=findViewById(R.id.imagemMedico);
        botaoDeletar = findViewById(R.id.botaoDeletar);

        //Mascara telefone
        SimpleMaskFormatter mascaraTelefone = new SimpleMaskFormatter("(NN) NNNNNNNNN");
        MaskTextWatcher textoMascaraTelefone = new MaskTextWatcher(numeroTelefone, mascaraTelefone);
        numeroTelefone.addTextChangedListener(textoMascaraTelefone);

        //Mascara Cep
        SimpleMaskFormatter mascaraCep = new SimpleMaskFormatter("NNNNN-NNN");
        MaskTextWatcher textMascaraCep = new MaskTextWatcher(textCep, mascaraCep);
        textCep.addTextChangedListener(textMascaraCep);

        //Receber valor de Editar
        Bundle bundle = getIntent().getExtras();
        idMedico = bundle.getString("idMedico");

        //Atualizar campos caso esteja na funcao editar
        if (idMedico != null && idMedico != "") {
            CarregarMedico();
        }

        //Retorno da activity
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if(pegarFotoGaleria) {
                                Uri selectedImageUri = data.getData();
                                InputStream inputStream;
                                try {
                                    imagemMedico.setBackground(null);
                                    inputStream = getContentResolver().openInputStream(selectedImageUri);
                                    Bitmap plantPicture = BitmapFactory.decodeStream(inputStream);
                                    BitmapDrawable bitDraw = new BitmapDrawable(getApplicationContext().getResources(), plantPicture);
                                    imagemMedico.setBackground(bitDraw);
                                    imagemMedico.setLayoutParams(new LinearLayout.LayoutParams(150,150));
                                    botaoDeletar.setLayoutParams(new LinearLayout.LayoutParams(30,30));
                                    botaoDeletar.setVisibility(View.VISIBLE);
                                    possuiFoto = true;
                                    pegarFotoGaleria = false;
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getApplicationContext(), R.string.erro_salvarImagem, Toast.LENGTH_SHORT).show();
                                }
                            }
                            else{
                                String phoneNumber = "";
                                try {
                                    Uri contactData = data.getData();
                                    Cursor cursor = getContentResolver().query(contactData, null, null, null, null);
                                    cursor.moveToFirst();
                                    int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                                    phoneNumber = cursor.getString(phoneIndex);
                                    if (phoneNumber.toString().length() > 11) {
                                        phoneNumber = phoneNumber.substring(phoneNumber.length() - 11);
                                        if(phoneNumber.substring(0,1) =="0"){
                                            phoneNumber = phoneNumber.substring(1);
                                        }
                                    }
                                    numeroTelefone.setText(phoneNumber);
                                    cursor.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                });
    }

    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }

    public  void VoltarAbaAnterior(){
        Intent intent = new Intent();
        intent.putExtra("retorno","Voltei");
        setResult(RESULT_OK,intent);
        this.finish();
    }

    public void AbrirModalVoltar(View v){
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_voltarPagina),getResources().getString(R.string.texto_voltarPagina),this,this);
    }
    public void RetornoModal(boolean resultado){
        if(resultado) {
            VoltarAbaAnterior();
        }
    }

    public  void PegarFotoGaleria (View v) {
        Intent intentGaleria = new Intent();
        intentGaleria.setType("image/*");
        intentGaleria.setAction(Intent.ACTION_GET_CONTENT);
        pegarFotoGaleria = true;
        activityResultLauncher.launch(Intent.createChooser(intentGaleria, "Select Picture"));
    }

    public  void ExcluirFoto(View v){
        imagemMedico.setBackground(null);
        botaoDeletar.setVisibility(View.INVISIBLE);
        botaoDeletar.setLayoutParams(new LinearLayout.LayoutParams(0,0));
        imagemMedico.setLayoutParams(new LinearLayout.LayoutParams(0,0));
        possuiFoto = false;
    }

    public  void AbrirContatos(View v){
        Intent intentContatos = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
        activityResultLauncher.launch(intentContatos);
    }

    public  void SalvarNovoMedico(View v){
        String nomeMedico = textNomeMedico.getText().toString().trim();
        String especialidadeMedico = textEspecialidade.getText().toString().trim();
        if(!nomeMedico.isEmpty() && nomeMedico.length()>2 && !especialidadeMedico.isEmpty() && especialidadeMedico.length()>5) {
            if(telefoneMedico.getText().toString().isEmpty() || telefoneMedico.getText().toString().length()==15 || telefoneMedico.getText().toString().length()==14) {
                if (textCep.getText().toString().isEmpty() || textCep.getText().toString().length() == 9) {
                    DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
                    Medico medico = new Medico();
                    medico.setNome(textNomeMedico.getText().toString());
                    medico.setEspecialidade(textEspecialidade.getText().toString());
                    medico.setIdUsuario(IdUsuarioAtual);
                    if (!telefoneMedico.getText().toString().isEmpty()) {
                        medico.setTelefone(telefoneMedico.getText().toString());
                    } else {
                        medico.setTelefone("");
                    }
                    if (!textCep.getText().toString().isEmpty()) {
                        medico.setCep(Integer.parseInt(textCep.getText().toString().replace("-","")));
                    } else {
                        medico.setCep(0);
                    }
                    if (!textNumero.getText().toString().isEmpty()) {
                        medico.setNumero(Integer.parseInt(textNumero.getText().toString()));
                    } else {
                        medico.setNumero(0);
                    }
                    if (!textoNomeRua.getText().toString().isEmpty()) {
                        medico.setLogradouro(textoNomeRua.getText().toString());
                    } else {
                        medico.setLogradouro("");
                    }
                    if (!textBairro.getText().toString().isEmpty()) {
                        medico.setBairro(textBairro.getText().toString());
                    } else {
                        medico.setBairro("");
                    }if (!textCidade.getText().toString().isEmpty()) {
                        medico.setCidade(textCidade.getText().toString());
                    } else {
                        medico.setCidade("");
                    }if (!textEstado.getText().toString().isEmpty()) {
                        medico.setEstado(textEstado.getText().toString());
                    } else {
                        medico.setEstado("");
                    }
                    boolean ErroImagem = false;
                    if(possuiFoto) {
                        boolean deletar = false;
                        if(funcaoRecebida == "Editar"){
                            Medico thisMedico = DMOH.BuscaMedico(Integer.parseInt(idMedico),IdUsuarioAtual);
                            deletar = DeletarImagem(thisMedico);
                        }
                        if((funcaoRecebida == "Editar" && deletar) || funcaoRecebida != "Editar") {
                            try {
                                File photoFile = null;
                                try {
                                    photoFile = createImageFile();
                                } catch (IOException ex) {
                                }
                                try {
                                    BitmapDrawable bitmapDrawable = (BitmapDrawable) imagemMedico.getBackground();
                                    Bitmap bitmap = bitmapDrawable.getBitmap();
                                    FileOutputStream out = new FileOutputStream(photoFile);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                    medico.setUriImagem(photoFile.getName());
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    ErroImagem = true;
                                    Toast.makeText(getApplicationContext(), R.string.erro_salvarImagem, Toast.LENGTH_SHORT).show();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                ErroImagem = true;
                                Toast.makeText(getApplicationContext(),  R.string.erro_salvarImagem, Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            ErroImagem = true;
                            Toast.makeText(getApplicationContext(),  R.string.erro_salvarImagem, Toast.LENGTH_SHORT).show();
                        }
                    }
                    else
                    {
                        medico.setUriImagem("");
                    }
                    if (funcaoRecebida == "Editar") {
                        if(!ErroImagem) {
                            DMOH.EditarMedico(Integer.parseInt(idMedico), medico, IdUsuarioAtual);
                        }
                    }
                    else {
                        DMOH.AdicionarNovoMedico(medico);
                    }
                    DMOH.close();
                    if(!ErroImagem) {
                        VoltarAbaAnterior();
                    }
                }
                else{
                    Toast.makeText(getApplicationContext(),R.string.cep_valido,Toast.LENGTH_SHORT).show();
                }
            }
            else{
                Toast.makeText(getApplicationContext(),R.string.telefone_valido,Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(),R.string.campo_obrigatorio,Toast.LENGTH_SHORT).show();
        }
    }

    public void CarregarMedico(){
        funcaoRecebida = "Editar";
        textNomePagina.setText(getResources().getString(R.string.editar_medico));
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        Medico medico = DMOH.BuscaMedico(Integer.parseInt(idMedico),IdUsuarioAtual);
        textNomeMedico.setText(medico.getNome());
        textEspecialidade.setText(medico.getEspecialidade());
        telefoneMedico.setText(medico.getTelefone());
        if (medico.getCep() == 0){
            textCep.setText("");
        }
        else {
            textCep.setText(medico.getCep() + "");
        }
        textNumero.setText(medico.getNumero() + "");
        if( medico.getUriImagem() != null &&  !medico.getUriImagem().equals("")) {
            try {
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + medico.getUriImagem());
                Uri selectedImageUri = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(selectedImageUri);
                    Bitmap plantPicture = BitmapFactory.decodeStream(inputStream);
                    BitmapDrawable bitDraw = new BitmapDrawable(getApplicationContext().getResources(), plantPicture);
                    imagemMedico.setBackground(bitDraw);
                    imagemMedico.setLayoutParams(new LinearLayout.LayoutParams(150, 150));
                    botaoDeletar.setLayoutParams(new LinearLayout.LayoutParams(30, 30));
                    botaoDeletar.setVisibility(View.VISIBLE);
                    possuiFoto = true;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DMOH.close();
    }

    public boolean DeletarImagem(Medico thisMedico){
        int deletouImagens = 0;
        try {
            File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + thisMedico.getUriImagem());
            Uri selectedImageUri = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
            File f2 = new File(selectedImageUri.getPath());
            ContentResolver resolver = getApplicationContext().getContentResolver();
            deletouImagens = resolver.delete(selectedImageUri, null, null);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    private File createImageFile()throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_Medico_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".JPEG", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }
    /////////////////////////CEP///////////////////////////////////////
    public  void ProcurarCep(View v){
        boolean ok = false;
        URL url = null;
        try{
            String cepDigitado = textCep.getText().toString();
            cepDigitado = cepDigitado.replace("-","");
            String urlString = "https://viacep.com.br/ws/" + cepDigitado + "/json/";
            urlCep = new URL(urlString);
            ok = true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            ok = false;
        }
        if(textCep.length()==9 && ok) {
            getDadosCEP classJson = new getDadosCEP();
            classJson.execute();
        }
    }

    public static JSONObject getJSONObjectFromURL(URL url) throws IOException, JSONException {
        HttpURLConnection urlConnection = null;
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        urlConnection.setDoOutput(true);
        urlConnection.connect();
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        String jsonString = sb.toString();
        return new JSONObject(jsonString);
    }
    public class getDadosCEP extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            Toast.makeText(getApplicationContext(),R.string.carregando_cep,Toast.LENGTH_SHORT).show();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                jsonObjectBase = adicionarMedicoActivity.getJSONObjectFromURL(urlCep);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            try {
                String logradouro = jsonObjectBase.getString("logradouro");
                String bairro = jsonObjectBase.getString("bairro");
                String cidade = jsonObjectBase.getString("localidade");
                String estado = jsonObjectBase.getString("uf");
                textoNomeRua.setText(logradouro);
                textEstado.setText(estado);
                textCidade.setText(cidade);
                textBairro.setText(bairro);
            } catch (JSONException e) {
                e.printStackTrace();
                try {
                    if(jsonObjectBase.length()==0){
                        Toast.makeText(getApplicationContext(),R.string.cep_incorreto,Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();

                }
            }
        }
    }
//////////////////////////////////////////FIM CEP///////////////////////////////////////////////////////////////
    @Override
    public void onBackPressed(){
       AbrirModalVoltar(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}

