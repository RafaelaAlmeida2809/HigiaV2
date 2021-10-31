package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.Exame;
import dataBase.Medico;

public class adicionarExameActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, MyInterface  {

    ActivityResultLauncher<Intent> activityResultLauncher;
    ScrollView scrollFundo;
    RelativeLayout modalImagem;
    Spinner spinner;
    Spinner spinnerMes;
    TextView textNomePagina;
    EditText textTipoExame;
    EditText textParteCorpo;
    EditText textDiaExame;
    EditText textAnoExame;
    String idExame;
    String funcaoRecebida;
    String currentPhotoPath;
    int tamanhoLista;
    int idMedico;
    int mesExame;
    int quantidadeImagens;
    int IdUsuarioAtual;
    int fotoAbertaAtual;
    boolean BuscandoImagem;
    boolean TirandoImagem;
    boolean AdicionandoMedico;
    boolean Ocupado;
    boolean excluindo;
    List<String> nome_Medicos = new ArrayList();
    List<Integer> id_Medicos = new ArrayList();
    List<Button> ListaImagensPhotos = new ArrayList<>();
    List<Uri> photoUri = new ArrayList<>();
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_exame);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        spinner = findViewById(R.id.spinnerMedico);
        spinnerMes = findViewById(R.id.spinnerMes);
        textTipoExame = findViewById(R.id.textTipoExame);
        textParteCorpo = findViewById(R.id.textParteCorpo);
        textDiaExame = findViewById(R.id.textDiaExame);
        textAnoExame = findViewById(R.id.textAnoExame);
        textNomePagina = findViewById(R.id.textNomePagina);
        scrollFundo = findViewById(R.id.scrollFundo);
        modalImagem = findViewById(R.id.modalImagem);
        //Carregar spinners
        funcoes.CriarSpinner(this,spinnerMes,R.array.meses_array,null);

        //Recebe idExame em caso de edição
        Bundle bundle = getIntent().getExtras();
        idExame = bundle.getString("idExame");

        //Inicia os componentes da pagina.
        AtualizarMedicos();
        CarregarModal();

        //Atualizar campos caso esteja na funcao editar
        if (idExame != null && idExame !="") {
            CarregarExames();
        }

        //Retorno da activity
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            if(TirandoImagem) {
                                Bitmap photo = (Bitmap) data.getExtras().get("data");
                                BitmapDrawable bitDraw = new BitmapDrawable(getApplicationContext().getResources(),photo);
                                ListaImagensPhotos.get(quantidadeImagens).setBackground(bitDraw);
                                RegularImagem(ListaImagensPhotos.get(quantidadeImagens));
                                quantidadeImagens++;
                                photoUri.add(null);
                                TirandoImagem = false;
                            }
                            else if(BuscandoImagem){
                                Uri selectedImageUri = data.getData();
                                InputStream inputStream;
                                try {
                                    inputStream = getContentResolver().openInputStream(selectedImageUri);
                                    Bitmap plantPicture = BitmapFactory.decodeStream(inputStream);
                                    BitmapDrawable bitDraw = new BitmapDrawable(getApplicationContext().getResources(),plantPicture);
                                    ListaImagensPhotos.get(quantidadeImagens).setBackground(bitDraw);
                                    RegularImagem(ListaImagensPhotos.get(quantidadeImagens));
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }
                                photoUri.add(null);
                                quantidadeImagens++;
                                BuscandoImagem = false;
                            }
                            else if(AdicionandoMedico){
                                AtualizarMedicos();
                                AdicionandoMedico = false;
                            }
                        }
                        TirandoImagem = false;
                        BuscandoImagem = false;
                        AdicionandoMedico = false;
                    }
                });
    }
    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }

    public void salvarImagens(int idExame){
        for(int i = 0; i<quantidadeImagens;i++) {
            if (photoUri.get(i) == null) {
                try {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                    }
                    BitmapDrawable bitmapDrawable = (BitmapDrawable) ListaImagensPhotos.get(i).getBackground();
                    Bitmap bitmap = bitmapDrawable.getBitmap();
                    FileOutputStream out = new FileOutputStream(photoFile);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
                    DEOH.AdicionarImagem(photoFile.getName(), idExame,IdUsuarioAtual);
                    out.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void CarregarExames(){
        funcaoRecebida = "Editar";
        textNomePagina.setText(getResources().getString(R.string.editar_exame));
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        Exame exame = DEOH.BuscaExame(Integer.parseInt(idExame),IdUsuarioAtual);
        textTipoExame.setText(exame.getTipo()+"");
        textParteCorpo.setText(exame.getParteCorpo()+"");
        if(exame.getDia() != 0) {
            textDiaExame.setText(exame.getDia() + "");
        }
        if(exame.getAno()!= 0) {
            textAnoExame.setText(exame.getAno() + "");
        }
        for(int i = 0;i < id_Medicos.size(); i++){
            if(exame.getIdMedico() == id_Medicos.get(i)){
                spinner.setSelection(i);
            }
        }
        spinnerMes.setSelection(exame.getMes());
        DEOH.BuscaImagensExame(exame,IdUsuarioAtual);
        List<String> listaImagens = exame.getNomesImagens();
        for (int i = 0; i < listaImagens.size(); i++){
                photoUri.add(Uri.parse(listaImagens.get(i)));
        }
    }

    public void AtualizarMedicos() {
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        List<Medico> medicos = DMOH.BuscaMedicos("nome", "ASC", IdUsuarioAtual);
        tamanhoLista = medicos.size();
        nome_Medicos.clear();
        nome_Medicos.add(getResources().getString(R.string.selecione_medico));
        id_Medicos.clear();
        id_Medicos.add(0);
        for (int i = 0; i < medicos.size(); i++) {
            nome_Medicos.add(medicos.get(i).getNome());
            id_Medicos.add(medicos.get(i).getId());
        }
        nome_Medicos.add(getResources().getString(R.string.adicione_medico));
        funcoes.CriarSpinner(this,spinner,-1,nome_Medicos);
        DMOH.close();
    }

    public void AbrirAbaAdicionarMedico() {
        activityResultLauncher.launch(funcoes.BundleActivy(this,adicionarMedicoActivity.class,"idMedico",null));
    }

    public  void VoltarAbaAnterior(View v){
        if(!Ocupado) {
            Intent intent = new Intent();
            intent.putExtra("retorno", "Voltei");
            setResult(RESULT_OK, intent);
            this.finish();
        }
    }

    public void AdicionarNovoExame(View v) {
        if(!Ocupado) {
            String tipoExame = textTipoExame.getText().toString().trim();
            String parteCorpo = textParteCorpo.getText().toString().trim();
           if (!tipoExame.isEmpty() && tipoExame.length() > 3 && !parteCorpo.isEmpty() && parteCorpo.length() > 1 && idMedico != 0) {
                Resources res = getResources();
                String[] Dias = res.getStringArray(R.array.dias_array);
                 if(textDiaExame.getText().toString().isEmpty() || ((Integer.parseInt(textDiaExame.getText().toString()) <= Integer.parseInt(Dias[mesExame]))&&(Integer.parseInt(textDiaExame.getText().toString())>0))) {
                    if(textAnoExame.getText().toString().isEmpty() || (Integer.parseInt(textAnoExame.getText().toString())<2100 && Integer.parseInt(textAnoExame.getText().toString())>1900)) {
                        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
                        Exame exame = new Exame();
                        exame.setTipo(textTipoExame.getText().toString());
                        exame.setParteCorpo(textParteCorpo.getText().toString());
                        exame.setIdMedico(idMedico);
                        exame.setMes(mesExame);
                        exame.setIdUsuario(IdUsuarioAtual);
                        if (!textDiaExame.getText().toString().isEmpty()) {
                            exame.setDia(Integer.parseInt(textDiaExame.getText().toString()));
                        }
                        else {
                            exame.setDia(0);
                        }
                        if (!textAnoExame.getText().toString().isEmpty()) {

                            exame.setAno(Integer.parseInt(textAnoExame.getText().toString()));
                        }
                        else {
                            exame.setAno(0);
                        }
                        if (funcaoRecebida == "Editar") {
                            if (deleteImages(DEOH.BuscaExame(Integer.parseInt(idExame), IdUsuarioAtual))) {
                                salvarImagens(Integer.parseInt(idExame));
                                DEOH.EditarExame(Integer.parseInt(idExame), exame, IdUsuarioAtual);
                            }
                        }
                        else {
                            int idExame = (int) DEOH.AdicionarNovoExame(exame);
                            salvarImagens(idExame);
                        }
                        DEOH.close();
                        VoltarAbaAnterior(null);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), R.string.ano_valido, Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), R.string.dia_valido, Toast.LENGTH_SHORT).show();
                }
            }
           else {
                Toast.makeText(getApplicationContext(), R.string.campo_obrigatorio, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void AbrirCamera(View v){
        if(!Ocupado) {
            if (quantidadeImagens < 12) {
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                TirandoImagem = true;
                activityResultLauncher.launch(intentCamera);
            }
        }
    }

    public void AbrirGaleria(View v) {
        if(!Ocupado) {
            if (quantidadeImagens < 12) {
                Intent intentGaleria = new Intent();
                intentGaleria.setType("image/*");
                intentGaleria.setAction(Intent.ACTION_GET_CONTENT);
                BuscandoImagem = true;
                activityResultLauncher.launch(Intent.createChooser(intentGaleria, "Select Picture"));
            }
        }
    }

    private File createImageFile()throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".JPEG", storageDir);
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private boolean deleteImages(Exame exame){
        DadosExamesOpenHelper DEOH = new DadosExamesOpenHelper(getApplicationContext());
        DEOH.BuscaImagensExame(exame,IdUsuarioAtual);
        int deletouImagens = 0;
        try {
            List<String> listaImagens = exame.getNomesImagens();
            List<Integer> listaIdImagens = exame.getIdImagens();
            for (int i = 0; i < listaImagens.size(); i++) {
                File f = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.pathSeparator + listaImagens.get(i));
                Uri selectedImageUri = FileProvider.getUriForFile(this, getResources().getString(R.string.caminho_imagem), f);
                File f2 = new File(selectedImageUri.getPath());
                ContentResolver resolver = getApplicationContext().getContentResolver();
                deletouImagens = resolver.delete(selectedImageUri,null,null);
                DEOH.DeletaImagem(listaIdImagens.get(i),IdUsuarioAtual);
            }
            DEOH.close();
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            DEOH.close();
            return false;
        }
    }

    public void CarregarModal(){
        LinearLayout LayoutModal = findViewById(R.id.LayoutVertical);
        int childCount = LayoutModal.getChildCount();
        for (int i = 0;i<childCount;i++){
            View child = LayoutModal.getChildAt(i);
            LinearLayout childLayout = findViewById(child.getId());
            int grandChildCount = childLayout.getChildCount();
            for (int j = 0;j<grandChildCount;j++){
                View grandChild = childLayout.getChildAt(j);
                Button b = findViewById(grandChild.getId());
                b.setTag(j);
                ListaImagensPhotos.add(b);
            }
        }
    }
    public void TrocarFocus(boolean bool){
        textTipoExame.setFocusableInTouchMode(bool);
        textTipoExame.setFocusable(bool);
        //
        textParteCorpo.setFocusableInTouchMode(bool);
        textParteCorpo.setFocusable(bool);
        //
        textDiaExame.setFocusableInTouchMode(bool);
        textDiaExame.setFocusable(bool);
        //
        textAnoExame.setFocusableInTouchMode(bool);
        textAnoExame.setFocusable(bool);
        //
        spinner.setEnabled(bool);
        spinner.setClickable(bool);
        //
        spinnerMes.setEnabled(bool);
        spinnerMes.setClickable(bool);
    }

    public void AbrirImagem(View v){
        if(!Ocupado) {
            fotoAbertaAtual = Integer.parseInt(v.getTag().toString());
            if(fotoAbertaAtual < quantidadeImagens){
                Ocupado = true;
                modalImagem.setVisibility(View.VISIBLE);
                ImageView imagemModal = findViewById(R.id.imagemModal);
                BitmapDrawable bitmapDrawable = (BitmapDrawable) ListaImagensPhotos.get(fotoAbertaAtual).getBackground();
                imagemModal.setBackground(bitmapDrawable);
                TrocarFocus(false);
            }
        }
    }

    public  void FecharModal(View v){
        modalImagem.setVisibility(View.INVISIBLE);
        TrocarFocus(true);
        Ocupado = false;
    }

    public  void ExcluirFoto(){
        for (int i = fotoAbertaAtual; i < quantidadeImagens-1; i++) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ListaImagensPhotos.get(i + 1).getBackground();
            ListaImagensPhotos.get(i).setBackground(bitmapDrawable);
        }
        ListaImagensPhotos.get(quantidadeImagens-1).setBackground(null);
        ListaImagensPhotos.get(quantidadeImagens-1).setLayoutParams(new LinearLayout.LayoutParams(1, 1));
        FecharModal(null);
        quantidadeImagens = quantidadeImagens-1;
    }

    public void ModalVoltar(View v){
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_voltarPagina),getResources().getString(R.string.texto_voltarPagina),this,this);
    }

    public void ModalExcluir(View v){
        excluindo = true;
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_excluir_Imagem),getResources().getString(R.string.texto_excluir_Imagem),this,this);
    }

    public void RetornoModal(boolean resultado){
       if(resultado) {
           if (excluindo) {
               ExcluirFoto();
               excluindo = false;
           } else {
               VoltarAbaAnterior(null);
           }
       }
    }

    public  void RegularImagem(Button imagem){
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int tamanhoWidth = ((displayMetrics.widthPixels*205)/1080);
        int tamanhoHeight = ((displayMetrics.heightPixels*275)/1920);
        imagem.setLayoutParams(new LinearLayout.LayoutParams(tamanhoWidth, tamanhoHeight));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        if(parent.getId() == R.id.spinnerMedico) {
            if (position == tamanhoLista + 1) {
                AdicionandoMedico = true;
                AbrirAbaAdicionarMedico();
            }
            else {
                idMedico = id_Medicos.get(position);
            }
        }
        else if(parent.getId() == R.id.spinnerMes){
            ((TextView) parent.getChildAt(0)).setGravity(0x11);
            mesExame = position;
        }
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onBackPressed(){
        ModalVoltar(null);
    }
}

