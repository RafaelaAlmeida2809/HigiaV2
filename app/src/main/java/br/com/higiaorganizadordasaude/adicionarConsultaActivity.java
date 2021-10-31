package br.com.higiaorganizadordasaude;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.github.rtoshiro.util.format.SimpleMaskFormatter;
import com.github.rtoshiro.util.format.text.MaskTextWatcher;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import dataBase.Consulta;
import dataBase.DadosConsultasOpenHelper;
import dataBase.DadosMedicosOpenHelper;
import dataBase.Medico;

public class adicionarConsultaActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,MyInterface{
    Spinner spinner;
    Spinner spinnerMesInicio;
    int tamanhoLista;
    Button textHorario;
    EditText textDia;
    EditText textAno;
    List<String> nome_Medicos = new ArrayList();
    List<Integer> id_Medicos = new ArrayList();
    String idConsulta;
    String idCalendarioAtual;
    Consulta consulta;
    String funcaoRecebida;
    TextView textNomePagina;
    ActivityResultLauncher<Intent> activityResultLauncher;
    int idMedicoEscolhido;
    int mesEscolhido;
    int idConsultaInt;
    boolean adicionandoMedico;
    boolean salvandoAgenda;
    Uri agendaUri;
    TimePickerDialog timePickerDialog;
    int IdUsuarioAtual;
    String acao;
    boolean permitido;
    private int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    FuncoesCompartilhadas funcoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_consulta);

        //atribuindo funcoes compartilhadas;
        funcoes = new FuncoesCompartilhadas();

        //Verificar Login
        IdUsuarioAtual =  funcoes.VerificarLogin(this);
        if(IdUsuarioAtual == -1){
            AbrirLogin();
        }

        // atribuindo Views
        spinner = findViewById(R.id.spinnerMedico);
        spinnerMesInicio = findViewById(R.id.spinnerMesInicio);
        textHorario=  findViewById(R.id.textHorarioRemedio);
        textDia = findViewById(R.id.textDiaInicio);
        textAno = findViewById(R.id.textAnoInicio);
        textNomePagina = findViewById(R.id.textNomePagina);

        //Carregar spinners
        funcoes.CriarSpinner(this,spinnerMesInicio,R.array.meses_array,null);

        //Mascara Horario
        SimpleMaskFormatter mascaraHorario = new SimpleMaskFormatter("NN:NN");
        MaskTextWatcher textoMascaraHorario1 = new MaskTextWatcher(textHorario,mascaraHorario);
        textHorario.addTextChangedListener(textoMascaraHorario1);

        //Receber valor de Editar
        Bundle bundle = getIntent().getExtras();
        idConsulta = bundle.getString("idConsulta");

        //Inicia os componentes da pagina.
        AtualizarMedicos();

        //Atualizar campos caso esteja na funcao editar
        if (idConsulta != null && idConsulta != "") {
            CarregarConsulta();
        }

        /*//Pedir permissão
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(adicionarConsultaActivity.this,new String[]{Manifest.permission.READ_CALENDAR},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
        if( ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(adicionarConsultaActivity.this, new String[]{Manifest.permission.WRITE_CALENDAR},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
        permitido = true;
            Toast.makeText(this, "Todas as permissões ok", Toast.LENGTH_LONG).show();
        }*/
            //Retorno da activity
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            if(adicionandoMedico)
                            {
                                Intent data = result.getData();
                                AtualizarMedicos();
                                adicionandoMedico = false;
                            }
                        }
                        else if(salvandoAgenda)
                        {
                            salvandoAgenda = false;
                            //VerificarAgenda();
                        }
                    }
                });
    }
    public void AbrirLogin(){
        Intent LoginActivity = new Intent(getApplicationContext(), LoginActivity.class);
        finish();
        startActivity(LoginActivity);
    }
    public void AtualizarMedicos()
    {
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        List<Medico> medicos = DMOH.BuscaMedicos("nome","ASC",IdUsuarioAtual);
        tamanhoLista = medicos.size();
        nome_Medicos.clear();
        nome_Medicos.add(getResources().getString(R.string.selecione_medico));
        id_Medicos.clear();
        id_Medicos.add(0);
        for(int i = 0; i<medicos.size(); i++)
        {
            nome_Medicos.add(medicos.get(i).getNome());
            id_Medicos.add(medicos.get(i).getId());
        }
        nome_Medicos.add(getResources().getString(R.string.adicione_medico));
        funcoes.CriarSpinner(this,spinner,-1,nome_Medicos);
        DMOH.close();
    }
    public void AbrirAbaAdicionarMedico() {
        Bundle bundle = new Bundle();
        bundle.putString("idMedico",null);
        Intent adicionarMedicoActivity = new Intent(this, adicionarMedicoActivity.class);
        adicionarMedicoActivity.putExtras(bundle);
        activityResultLauncher.launch(adicionarMedicoActivity);
    }
    public void ModalVoltar(View v){
        acao="Voltar";
        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_voltarPagina),getResources().getString(R.string.texto_voltarPagina),this,this);
    }

    public void AbrirRelogio(View v) {
        Button b = findViewById(v.getId());
        timePickerDialog = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            b.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute));
        }, 0, 0, true);
        timePickerDialog.show();
    }
    public void AdicionarNaAgenda(String Acao) {

        Uri uri = null;

        Intent intent = null;
        String idCalendario = "12";
        if (Acao == "Editar" && idCalendarioAtual.length() > 6) {
            intent = new Intent(Intent.ACTION_EDIT);
            uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(idCalendarioAtual));
            //uri = Uri.parse("content://com.android.calendar/events/" + idCalendarioAtual);
            idCalendario = idCalendarioAtual;
            Long Id = null;

                Id = Long.parseLong(idCalendarioAtual);
                Toast.makeText(this, Id+"", Toast.LENGTH_LONG).show();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {

                        Cursor cursor = getContentResolver().query(CalendarContract.Events.CONTENT_URI, null, null, null, null);
                        while (cursor.moveToNext()) {
                            if (cursor.getColumnIndex(CalendarContract.Events._ID) == Id) {
                                Toast.makeText(this, "Achou", Toast.LENGTH_LONG).show();
                            } else if (cursor.getColumnName(cursor.getColumnIndex(CalendarContract.Events._ID)) == idCalendarioAtual) {
                                Toast.makeText(this, "Achou2", Toast.LENGTH_LONG).show();
                            }

                }
            }
        }
        else {
            intent = new Intent(Intent.ACTION_INSERT);
            String a = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            idCalendario = a.replace("_", "");
            idCalendarioAtual = idCalendario;
            Toast.makeText(this, "Criando", Toast.LENGTH_LONG).show();
            //uri = CalendarContract.Events.CONTENT_URI;
            uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, Long.parseLong(idCalendarioAtual));//
        }

        String[] horario = textHorario.getText().toString().split(":");
        Calendar DataHorarioInicio = Calendar.getInstance();
        int ano = Integer.parseInt(textAno.getText().toString());
        int dia = Integer.parseInt(textDia.getText().toString());
        DataHorarioInicio.set(ano, mesEscolhido - 1, dia, Integer.parseInt(horario[0]), Integer.parseInt(horario[1]));
        Calendar DataHorarioFim = Calendar.getInstance();
        DataHorarioFim.set(ano, mesEscolhido - 1, dia, Integer.parseInt(horario[0]) + 1, Integer.parseInt(horario[1]));
        DadosMedicosOpenHelper DMOH = new DadosMedicosOpenHelper(getApplicationContext());
        Medico medico = DMOH.BuscaMedico(idMedicoEscolhido, IdUsuarioAtual);
        DMOH.close();

        String DescricaoEvento = "Consulta com o(a) Dr(a) " + medico.getNome() + ", " + medico.getEspecialidade() +
                ", contato: " + medico.getTelefone();
        String LocalEvento = ((medico.getLogradouro() == "") ? "" : medico.getLogradouro() + ", ")
                + ((medico.getNumero() == 0) ? "" : "n°" + medico.getNumero() + ", ")
                + ((medico.getBairro() == "") ? "" : medico.getBairro() + ", ")
                + ((medico.getCidade() == "") ? "" : medico.getCidade())
                + ((medico.getCidade() == "" && medico.getEstado() == "") ? ", " : "/")
                + ((medico.getEstado() == "") ? "" : medico.getEstado())
                + ((medico.getCep() == 0) ? "" : ", " + medico.getCep());
        intent
                .setData(uri)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, DataHorarioInicio.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, DataHorarioFim.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_ID, Long.parseLong(idCalendario))
                .putExtra(CalendarContract.Events.TITLE, "Consulta")
                .putExtra(CalendarContract.Events.DESCRIPTION, DescricaoEvento)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, LocalEvento)
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        salvandoAgenda = true;
        adicionandoMedico = false;
        setResult(RESULT_OK, intent);
        activityResultLauncher.launch(intent);
    }

    public  void VoltarAbaAnterior()
    {
        Intent intent = new Intent();
        intent.putExtra("retorno","Voltei");
        setResult(RESULT_OK,intent);
        this.finish();
    }

    public void RetornoModal(boolean resultado){
        if(resultado) {
            if(acao.equals("Voltar")){
                VoltarAbaAnterior();
            }
            else if(acao.equals("Calendario")) {
                Toast.makeText(getApplicationContext(), "Aguarde...", Toast.LENGTH_SHORT).show();
                AdicionarNaAgenda("Criar");;
                DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
                idConsultaInt = (int) DCOH.AdicionarNovaConsulta(consulta);
                DCOH.close();
                VoltarAbaAnterior();
            }
        }
        else {
            if(acao.equals("Calendario")) {
                consulta.setIdCalendario("12");
                DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
                idConsultaInt = (int) DCOH.AdicionarNovaConsulta(consulta);
                DCOH.close();
                VoltarAbaAnterior();
            }
        }
        acao = "";
    }

    public  void CarregarConsulta()
    {
        funcaoRecebida = "Editar";
        textNomePagina.setText(getResources().getString(R.string.editar_consulta));
        DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
        Consulta consultaAtual = DCOH.BuscaConsulta(Integer.parseInt(idConsulta.trim()),IdUsuarioAtual);
        spinner.setSelection(consultaAtual.getIdMedico());
        textDia.setText(consultaAtual.getDia()+"");
        spinnerMesInicio.setSelection(consultaAtual.getMes());
        textAno.setText(consultaAtual.getAno()+"");
        textHorario.setText(consultaAtual.getHora());
        idConsultaInt = consultaAtual.getId();
        idCalendarioAtual = consultaAtual.getIdCalendario();
        DCOH.close();
    }
    public void AdicionarNovaConsulta(View v) {
        String[] horario = textHorario.getText().toString().split(":");
        if(idMedicoEscolhido == 0 ||textDia.getText().toString().isEmpty()|| mesEscolhido == 0 || textAno.getText().toString().isEmpty()
                ||textHorario.getText().toString().isEmpty())
        {
            Toast.makeText(getApplicationContext(), R.string.campo_obrigatorio, Toast.LENGTH_SHORT).show();
        }
        else {
            Resources res = getResources();
            String[] Dias = res.getStringArray(R.array.dias_array);
            if (Integer.parseInt(textDia.getText().toString()) <= Integer.parseInt(Dias[mesEscolhido])) {
                if (Integer.parseInt(textAno.getText().toString()) < 2100 && Integer.parseInt(textAno.getText().toString()) > 1900) {
                    DadosConsultasOpenHelper DCOH = new DadosConsultasOpenHelper(getApplicationContext());
                    consulta = new Consulta();
                    consulta.setIdMedico(idMedicoEscolhido);
                    consulta.setDia(Integer.parseInt(textDia.getText().toString()));
                    consulta.setMes(mesEscolhido);
                    consulta.setAno(Integer.parseInt(textAno.getText().toString()));
                    consulta.setHora(textHorario.getText().toString());
                    consulta.setIdUsuario(IdUsuarioAtual);
                    if (funcaoRecebida == "Editar") {
                        consulta.setIdCalendario(idCalendarioAtual);
                        AdicionarNaAgenda("Editar");
                    } else {
                        acao = "Calendario";
                        //if(permitido) {
                        funcoes.ModalConfirmacao(getResources().getString(R.string.titulo_addConsulta), getResources().getString(R.string.texto_addConsulta), this, this);
                        //}
                    }
                    DCOH.close();
                } else {
                    Toast.makeText(getApplicationContext(), R.string.ano_valido, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.dia_valido, Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String text = parent.getItemAtPosition(position).toString();
        if(parent.getId() == R.id.spinnerMedico) {
            if (position == tamanhoLista + 1) {
                adicionandoMedico = true;
                AbrirAbaAdicionarMedico();
            } else {
                adicionandoMedico = false;
                idMedicoEscolhido = id_Medicos.get(position);
            }
        }
        else if(parent.getId() == R.id.spinnerMesInicio)
        {
            ((TextView) parent.getChildAt(0)).setGravity(0x11);
            mesEscolhido = position;
            Toast.makeText(parent.getContext(),position + "",Toast.LENGTH_SHORT);
        }
        Toast.makeText(parent.getContext(),text,Toast.LENGTH_SHORT);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getApplicationContext(), R.string.permissao_aceita, Toast.LENGTH_SHORT).show();
                permitido = true;
            } else {
                Toast.makeText(getApplicationContext(), R.string.permissao_negada, Toast.LENGTH_SHORT).show();
                permitido = false;
            }
            return;
        }
    }
    @Override
    public void onBackPressed(){
        ModalVoltar(null);
    }
}
