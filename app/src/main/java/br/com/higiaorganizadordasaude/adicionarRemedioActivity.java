package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosExamesOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.DadosRemediosOpenHelper;
import dataBase.DadosUsuariosOpenHelper;
import dataBase.Exame;
import dataBase.Medico;
import dataBase.Remedio;
import dataBase.Usuario;

public class adicionarRemedioActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,MyInterface {
    List<String> nome_Medicos = new ArrayList();
    List<Integer> id_Medicos = new ArrayList();
    private int HorariosVisiveis = 1;
    public EditText textNomeRemedio;
    public EditText textDosagemRemedio;
    public EditText textFormatoRemedio;
    public EditText textDiaInicio;
    public EditText textAnoInicio;
    public EditText textDiaFim;
    public EditText textAnoFim;
    public EditText textQuantidadeRemedio;
    private Button textHorario1;
    private Button textHorario2;
    private Button textHorario3;
    private Button textHorario4;
    private Button botaoHorario1;
    private Button botaoHorario2;
    private Button botaoHorario3;
    private Button botaoHorario2Menos;
    private Button botaoHorario3Menos;
    private Button botaoHorario4Menos;
    ActivityResultLauncher<Intent> activityResultLauncher;
    Spinner spinner;
    Spinner spinnerMesInicio;
    Spinner spinnerMesFim;
    int tamanhoLista;
    int idMedico;
    String idRemedio;
    String funcaoRecebida;
    String acao;
    int mesInicio;
    int mesFim;
    TextView textNomePagina;
    int IdUsuarioAtual;
    int valorI;
    Calendar calendar;
    TimePickerDialog timePickerDialog;
    List<String> listaHoras = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_remedio);

        //Verificar Loguin
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        IdUsuarioAtual =  funcao.VerificarLoguin(this);
        if(IdUsuarioAtual == -1){
            AbrirLoguin();
        }

        // atribuindo Views
        spinner = findViewById(R.id.spinnerMedico);
        spinnerMesInicio = findViewById(R.id.spinnerMesInicio);
        spinnerMesFim = findViewById(R.id.spinnerMesFim);
        textNomePagina = findViewById(R.id.textNomePagina);
        textHorario1=  findViewById(R.id.textHorarioRemedio);
        textHorario2=  findViewById(R.id.textHorarioRemedio2);
        textHorario3=  findViewById(R.id.textHorarioRemedio3);
        textHorario4=  findViewById(R.id.textHorarioRemedio4);
        botaoHorario1= findViewById(R.id.botaoHorarioRemedio1);
        botaoHorario2= findViewById(R.id.botaoHorarioRemedio2);
        botaoHorario3= findViewById(R.id.botaoHorarioRemedio3);
        botaoHorario2Menos=findViewById(R.id.botaoHorarioRemedio2Menos);
        botaoHorario3Menos=findViewById(R.id.botaoHorarioRemedio3Menos);
        botaoHorario4Menos=findViewById(R.id.botaoHorarioRemedio4Menos);
        textNomeRemedio = findViewById(R.id.textNomeRemedio);
        textDosagemRemedio = findViewById(R.id.textDosagemRemedio);
        textFormatoRemedio = findViewById(R.id.textFormatoRemedio);
        textDiaInicio = findViewById(R.id.textDiaInicio);
        textAnoInicio = findViewById(R.id.textAnoInicio);
        textDiaFim = findViewById(R.id.textDiaFim);
        textAnoFim = findViewById(R.id.textAnoFim);
        textQuantidadeRemedio = findViewById(R.id.textQuantidadeRemedio);


        //Carregar spinners
        funcao.CriarSpinner(this,spinnerMesInicio,R.array.meses_array,null);
        funcao.CriarSpinner(this,spinnerMesFim,R.array.meses_array,null);

        //Mascara Horario
        SimpleMaskFormatter mascaraHorario = new SimpleMaskFormatter("NN:NN");
        MaskTextWatcher textoMascaraHorario1 = new MaskTextWatcher(textHorario1,mascaraHorario);
        textHorario1.addTextChangedListener(textoMascaraHorario1);
        MaskTextWatcher textoMascaraHorario2 = new MaskTextWatcher(textHorario2,mascaraHorario);
        textHorario2.addTextChangedListener(textoMascaraHorario2);
        MaskTextWatcher textoMascaraHorario3 = new MaskTextWatcher(textHorario3,mascaraHorario);
        textHorario3.addTextChangedListener(textoMascaraHorario3);
        MaskTextWatcher textoMascaraHorario4 = new MaskTextWatcher(textHorario4,mascaraHorario);
        textHorario4.addTextChangedListener(textoMascaraHorario4);

        //Receber valor de Editar
        Bundle bundle = getIntent().getExtras();
        idRemedio = bundle.getString("idRemedio");

        //Inicia os componentes da pagina.
        AtualizarMedicos();

        //Atualizar campos caso esteja na funcao editar
        if (idRemedio != null && idRemedio != "") {
            CarregarRemedio();
        }

        //Retorno da activity
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            AtualizarMedicos();
                        }
                    }
                });
    }

    public void AbrirLoguin(){
        Intent LoguinActivity = new Intent(getApplicationContext(),LoguinActivity.class);
        finish();
        startActivity(LoguinActivity);
    }

    public void AtualizarMedicos()
    {
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        List<Medico> medicos = DMOH.BuscaMedicos("nome","ASC",IdUsuarioAtual);
        tamanhoLista = medicos.size();
        nome_Medicos.clear();
        nome_Medicos.add(getResources().getString(R.string.selecione_medico));
        id_Medicos.add(0);
        for(int i = 0; i<medicos.size(); i++)
        {
            nome_Medicos.add(medicos.get(i).getNome());
            id_Medicos.add(medicos.get(i).getId());
        }
        nome_Medicos.add(getResources().getString(R.string.adicione_medico));
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        funcao.CriarSpinner(this,spinner,-1,nome_Medicos);
    }
    public void RetornoModal(boolean resultado){
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        if(resultado) {
            if(acao.equals("Despertador")){
                Toast.makeText(getApplicationContext(), R.string.aguarde, Toast.LENGTH_SHORT).show();
                AdicionarDespertador(valorI);
            }
        }
        else {
            if(acao.equals("Despertador")) {
                if (listaHoras.size() > (valorI + 1)) {
                    valorI = valorI+1;
                    funcao.ModalConfirmacao(getResources().getString(R.string.titulo_despertador),(getResources().getString(R.string.texto_despertador)+" " + listaHoras.get(valorI) +"?"),this,this);
                } else {
                    VoltarAbaAnterior(null);
                }
            }
        }
    }
    public void ModalVoltar(View v){
        acao="Voltar";
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        funcao.ModalConfirmacao(getResources().getString(R.string.titulo_voltarPagina),getResources().getString(R.string.texto_voltarPagina),this,this);
    }

    public void AbrirAbaAdicionarMedico() {
        Bundle bundle = new Bundle();
        bundle.putString("idMedico",null);
        Intent adicionarMedicoActivity = new Intent(this, adicionarMedicoActivity.class);
        adicionarMedicoActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarMedicoActivity);
    }
    public  void VoltarAbaAnterior(View v)
    {
        Intent intent = new Intent();
        intent.putExtra("retorno","Voltei");
        setResult(RESULT_OK,intent);
        this.finish();
    }
    public void CarregarRemedio(){
        funcaoRecebida = "Editar";
        textNomePagina.setText(getResources().getString(R.string.editar_remedio));
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        Remedio remedio = DROH.BuscaRemedio(Integer.parseInt(idRemedio.trim()),IdUsuarioAtual);
        textNomeRemedio.setText(remedio.getNome());
        spinner.setSelection(remedio.getIdMedico());
        textDosagemRemedio.setText(remedio.getDosagem());
        textFormatoRemedio.setText(remedio.getFormato());
        textDiaInicio.setText(remedio.getDiaInicio()+"");
        spinnerMesInicio.setSelection(remedio.getMesInicio());
        textAnoInicio.setText(remedio.getAnoInicio()+"");
        textDiaFim.setText(remedio.getDiaFim()+"");
        spinnerMesFim.setSelection(remedio.getMesFim());
        textAnoFim.setText(remedio.getAnoFim()+"");
        textHorario1.setText(remedio.getHorario1());
        textHorario2.setText(remedio.getHorario2());
        textHorario3.setText(remedio.getHorario3());
        textHorario4.setText(remedio.getHorario4());
        textQuantidadeRemedio.setText(remedio.getQuantidade());
        DROH.close();
    }
    public void AdicionarNovoRemedio(View v) {
        String nomeRemedio = textNomeRemedio.getText().toString().trim();
        String dosagemRemedio = textDosagemRemedio.getText().toString().trim();
        String formatoRemedio = textFormatoRemedio.getText().toString().trim();
        if (!nomeRemedio.isEmpty() && nomeRemedio.length() > 3 && !dosagemRemedio.isEmpty() && dosagemRemedio.length() > 1 && !formatoRemedio.isEmpty() && formatoRemedio.length() > 1 && idMedico != 0) {
            Resources res = getResources();
            String[] Dias = res.getStringArray(R.array.dias_array);
            if ((textDiaInicio.getText().toString().isEmpty() || Integer.parseInt(textDiaInicio.getText().toString()) <= Integer.parseInt(Dias[mesInicio])) &&
                    (textDiaFim.getText().toString().isEmpty() || Integer.parseInt(textDiaFim.getText().toString()) <= Integer.parseInt(Dias[mesFim]))) {
                if ((textAnoInicio.getText().toString().isEmpty() || (Integer.parseInt(textAnoInicio.getText().toString()) < 2100 && Integer.parseInt(textAnoInicio.getText().toString()) > 1900)) &&
                        (textAnoFim.getText().toString().isEmpty() || (Integer.parseInt(textAnoFim.getText().toString()) < 2100 && Integer.parseInt(textAnoFim.getText().toString()) > 1900))) {

                    DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
                    Remedio remedio = new Remedio();
                    remedio.setNome(textNomeRemedio.getText().toString());
                    remedio.setDosagem(textDosagemRemedio.getText().toString());
                    remedio.setFormato(textFormatoRemedio.getText().toString());
                    remedio.setMesInicio(mesInicio);
                    remedio.setMesFim(mesFim);
                    remedio.setIdUsuario(IdUsuarioAtual);
                    if (!textDiaInicio.getText().toString().isEmpty()) {
                        remedio.setDiaInicio(Integer.parseInt(textDiaInicio.getText().toString()));
                    } else {
                        remedio.setDiaInicio(0);
                    }
                    if (!textAnoInicio.getText().toString().isEmpty()) {
                        remedio.setAnoInicio(Integer.parseInt(textAnoInicio.getText().toString()));
                    } else {
                        remedio.setAnoInicio(0);
                    }
                    if (!textDiaFim.getText().toString().isEmpty()) {
                        remedio.setDiaFim(Integer.parseInt(textDiaFim.getText().toString()));
                    } else {
                        remedio.setDiaFim(0);
                    }
                    if (!textAnoFim.getText().toString().isEmpty()) {
                        remedio.setAnoFim(Integer.parseInt(textAnoFim.getText().toString()));
                    } else {
                        remedio.setAnoFim(0);
                    }

                    if (!textHorario1.getText().toString().isEmpty()) {
                        remedio.setHorario1(textHorario1.getText().toString());
                        listaHoras.add(textHorario1.getText().toString());
                    } else {
                        remedio.setHorario1("");
                    }
                    if (!textHorario2.getText().toString().isEmpty()) {
                        remedio.setHorario2(textHorario2.getText().toString());
                        listaHoras.add(textHorario2.getText().toString());
                    } else {
                        remedio.setHorario2("");
                    }
                    if (!textHorario3.getText().toString().isEmpty()) {
                        remedio.setHorario3(textHorario3.getText().toString());
                        listaHoras.add(textHorario3.getText().toString());
                    } else {
                        remedio.setHorario3("");
                    }
                    if (!textHorario4.getText().toString().isEmpty()) {
                        remedio.setHorario4(textHorario4.getText().toString());
                        listaHoras.add(textHorario4.getText().toString());
                    } else {
                        remedio.setHorario4("");
                    }
                    if (!textQuantidadeRemedio.getText().toString().isEmpty()) {
                        remedio.setQuantidade(textQuantidadeRemedio.getText().toString());
                    } else {
                        remedio.setQuantidade("");
                    }
                    remedio.setIdMedico(idMedico);

                    if (funcaoRecebida == "Editar") {
                        DROH.EditarRemedio(Integer.parseInt(idRemedio), remedio, IdUsuarioAtual);
                    } else {
                        DROH.AdicionarNovoRemedio(remedio);
                        if (listaHoras.size() > 0) {
                            valorI = 0;
                            FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
                            funcao.ModalConfirmacao(getResources().getString(R.string.titulo_despertador),(getResources().getString(R.string.texto_despertador)+" " + listaHoras.get(valorI) +"?"),this,this);
                        }
                    }
                    DROH.close();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.ano_valido, Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), R.string.dia_valido, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.campo_obrigatorio, Toast.LENGTH_SHORT).show();
        }
    }
    public void AbrirRelogio(View v) {
        Button b = findViewById(v.getId());
        timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            b.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
        }, 0, 0, true);
        timePickerDialog.show();
    }
    public void AdicionarDespertador(int i){
        Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
        String[] Hora = listaHoras.get(i).split(":");
        intent.putExtra(AlarmClock.EXTRA_HOUR,Integer.parseInt(Hora[0]));
        intent.putExtra(AlarmClock.EXTRA_MINUTES,Integer.parseInt(Hora[1]));
        intent.putExtra(AlarmClock.EXTRA_MESSAGE,R.string.hora_tomar + " " + textNomeRemedio.getText() +" "+ textDosagemRemedio.getText() +" "+textFormatoRemedio.getText());
        intent.setFlags(intent.FLAG_FROM_BACKGROUND);
        startActivity(intent);
        FuncoesCompartilhadas funcao = new FuncoesCompartilhadas();
        if(listaHoras.size()> (i+1)){
            valorI = i+1;
            funcao.ModalConfirmacao(getResources().getString(R.string.titulo_despertador),(getResources().getString(R.string.texto_despertador)+" " + listaHoras.get(i) +"?"),this,this);
        }
        else {
            VoltarAbaAnterior(null);
        }

    }
    public void DeletarDespertador(int i){

        Intent intent = new Intent(AlarmClock.ACTION_DISMISS_ALARM);
        DadosRemediosOpenHelper DROH = new DadosRemediosOpenHelper(getApplicationContext());
        Remedio remedio = DROH.BuscaRemedio(Integer.parseInt(idRemedio.trim()),IdUsuarioAtual);
        DROH.close();
        String[] Hora = listaHoras.get(i).split(":");
        intent.putExtra(AlarmClock.EXTRA_HOUR,Integer.parseInt(Hora[0]));
        intent.putExtra(AlarmClock.EXTRA_MINUTES,Integer.parseInt(Hora[1]));
        intent.setFlags(intent.FLAG_FROM_BACKGROUND);
        startActivity(intent);
    }

    public void AdicionarNovoHorario(View v)
    {
        HorariosVisiveis += 1;
        if(HorariosVisiveis == 2)
        {
            textHorario2.setVisibility(View.VISIBLE);
            botaoHorario1.setVisibility(View.INVISIBLE);
            botaoHorario2.setVisibility(View.VISIBLE);
            botaoHorario2Menos.setVisibility(View.VISIBLE);
        }
        else if(HorariosVisiveis == 3)
        {
            textHorario3.setVisibility(View.VISIBLE);
            botaoHorario2.setVisibility(View.INVISIBLE);
            botaoHorario2Menos.setVisibility(View.INVISIBLE);
            botaoHorario3.setVisibility(View.VISIBLE);
            botaoHorario3Menos.setVisibility(View.VISIBLE);
        }
        else if(HorariosVisiveis == 4)
        {
            textHorario4.setVisibility(View.VISIBLE);
            botaoHorario3.setVisibility(View.INVISIBLE);
            botaoHorario3Menos.setVisibility(View.INVISIBLE);
            botaoHorario4Menos.setVisibility(View.VISIBLE);
        }
    }
    public void RemoverNovoHorario(View v)
    {
        HorariosVisiveis -= 1;
        if(HorariosVisiveis == 1)
        {
            textHorario2.setVisibility(View.INVISIBLE);
            textHorario2.setText("");
            botaoHorario1.setVisibility(View.VISIBLE);
            botaoHorario2.setVisibility(View.INVISIBLE);
            botaoHorario2Menos.setVisibility(View.INVISIBLE);
        }
        else if(HorariosVisiveis == 2)
        {
            textHorario3.setVisibility(View.INVISIBLE);
            textHorario3.setText("");
            botaoHorario2.setVisibility(View.VISIBLE);
            botaoHorario3.setVisibility(View.INVISIBLE);
            botaoHorario3Menos.setVisibility(View.INVISIBLE);
            botaoHorario2Menos.setVisibility(View.VISIBLE);
        }
        else if(HorariosVisiveis == 3)
        {
            textHorario4.setVisibility(View.INVISIBLE);
            textHorario4.setText("");
            botaoHorario3.setVisibility(View.VISIBLE);
            botaoHorario4Menos.setVisibility(View.INVISIBLE);
            botaoHorario3Menos.setVisibility(View.VISIBLE);
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        if(parent.getId() == R.id.spinnerMedico) {
            if (position == tamanhoLista + 1) {
                AbrirAbaAdicionarMedico();
            } else {
                idMedico = id_Medicos.get(position);
            }
        }
        else if(parent.getId() == R.id.spinnerMesInicio)
        {
            mesInicio = position;
        }
        else if(parent.getId() == R.id.spinnerMesFim)
        {
            mesFim = position;
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
