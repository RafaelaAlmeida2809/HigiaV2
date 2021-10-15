package dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DadosRemediosOpenHelper extends SQLiteOpenHelper {
    public DadosRemediosOpenHelper(@Nullable Context context) {
        super(context, "DadosMedicos", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE remedio(id Integer PRIMARY KEY AUTOINCREMENT,nome TEXT,formato TEXT,dosagem TEXT, diaInicio Integer,mesInicio Integer,anoInicio Integer,  diaFim Integer,mesFim Integer,anoFim Integer, horario1 TEXT, horario2 TEXT, horario3 TEXT, horario4 TEXT,quantidade TEXT,idMedico Integer,idUsuario Integer REFERENCES usuario (id) );";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS remedio;";
        db.execSQL(sql);
        onCreate(db);
    }
    public void AdicionarNovoRemedio (Remedio remedio)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put("nome",remedio.getNome());
        dados.put("formato",remedio.getFormato());
        dados.put("dosagem",remedio.getDosagem());
        dados.put("diaInicio",remedio.getDiaInicio());
        dados.put("mesInicio",remedio.getMesInicio());
        dados.put("anoInicio",remedio.getAnoInicio());
        dados.put("diaFim",remedio.getDiaFim());
        dados.put("mesFim",remedio.getMesFim());
        dados.put("anoFim",remedio.getAnoFim());
        dados.put("horario1",remedio.getHorario1());
        dados.put("horario2",remedio.getHorario2());
        dados.put("horario3",remedio.getHorario3());
        dados.put("horario4",remedio.getHorario4());
        dados.put("quantidade",remedio.getQuantidade());
        dados.put("idMedico",remedio.getIdMedico());
        dados.put("idUsuario",remedio.getIdUsuario());
        db.insert("remedio",null,dados);

    }
    public List<Remedio> BuscaRemedios(String orderColuna, String ordem,int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        //ordem pode ser ASC (A-Z) ou DESC (Z-A)
        String sql = "SELECT * FROM remedio WHERE idUsuario = " + idUsuarioAtual +" ORDER BY " + orderColuna + " COLLATE NOCASE "+ ordem + ";";

        Cursor c = db.rawQuery(sql,null);
        List<Remedio> remedios= new ArrayList<Remedio>();
        while (c.moveToNext()){
            Remedio remedio = new Remedio();
            remedio.setId(c.getInt(c.getColumnIndex("id")));
            remedio.setNome(c.getString(c.getColumnIndex("nome")));
            remedio.setFormato(c.getString(c.getColumnIndex("formato")));
            remedio.setDosagem(c.getString(c.getColumnIndex("dosagem")));
            remedio.setDiaInicio(c.getInt(c.getColumnIndex("diaInicio")));
            remedio.setMesInicio(c.getInt(c.getColumnIndex("mesInicio")));
            remedio.setAnoInicio(c.getInt(c.getColumnIndex("anoInicio")));
            remedio.setDiaFim(c.getInt(c.getColumnIndex("diaFim")));
            remedio.setMesFim(c.getInt(c.getColumnIndex("mesFim")));
            remedio.setAnoFim(c.getInt(c.getColumnIndex("anoFim")));
            remedio.setHorario1(c.getString(c.getColumnIndex("horario1")));
            remedio.setHorario2(c.getString(c.getColumnIndex("horario2")));
            remedio.setHorario3(c.getString(c.getColumnIndex("horario3")));
            remedio.setHorario4(c.getString(c.getColumnIndex("horario4")));
            remedio.setQuantidade(c.getString(c.getColumnIndex("quantidade")));
            remedio.setIdMedico(c.getInt(c.getColumnIndex("idMedico")));
            remedio.setIdUsuario(c.getInt(c.getColumnIndex("idUsuario")));
            remedios.add(remedio);
        }
        db.close();
        return remedios;
    }
    public Remedio BuscaRemedio(int id,int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM remedio WHERE id = "+ id +" AND idUsuario = " + idUsuarioAtual +";";
        Cursor c = db.rawQuery(sql,null);
        Remedio remedio= new Remedio();
        while (c.moveToNext()){
            remedio.setId(c.getInt(c.getColumnIndex("id")));
            remedio.setNome(c.getString(c.getColumnIndex("nome")));
            remedio.setFormato(c.getString(c.getColumnIndex("formato")));
            remedio.setDosagem(c.getString(c.getColumnIndex("dosagem")));
            remedio.setDiaInicio(c.getInt(c.getColumnIndex("diaInicio")));
            remedio.setMesInicio(c.getInt(c.getColumnIndex("mesInicio")));
            remedio.setAnoInicio(c.getInt(c.getColumnIndex("anoInicio")));
            remedio.setDiaFim(c.getInt(c.getColumnIndex("diaFim")));
            remedio.setMesFim(c.getInt(c.getColumnIndex("mesFim")));
            remedio.setAnoFim(c.getInt(c.getColumnIndex("anoFim")));
            remedio.setHorario1(c.getString(c.getColumnIndex("horario1")));
            remedio.setHorario2(c.getString(c.getColumnIndex("horario2")));
            remedio.setHorario3(c.getString(c.getColumnIndex("horario3")));
            remedio.setHorario4(c.getString(c.getColumnIndex("horario4")));
            remedio.setQuantidade(c.getString(c.getColumnIndex("quantidade")));
            remedio.setIdMedico(c.getInt(c.getColumnIndex("idMedico")));
            remedio.setIdUsuario(c.getInt(c.getColumnIndex("idUsuario")));
        }
        db.close();
        return remedio;
    }
    public void DeletaRemedio(int id,int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM remedio WHERE id = "+ id +" AND idUsuario = " + idUsuarioAtual+";";
                db.execSQL(sql);
        db.close();
    }
    public void EditarRemedio(int id,Remedio remedio,int idUsuarioAtual){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();

        dados.put("nome",remedio.getNome());
        dados.put("formato",remedio.getFormato());
        dados.put("dosagem",remedio.getDosagem());
        dados.put("diaInicio",remedio.getDiaInicio());
        dados.put("mesInicio",remedio.getMesInicio());
        dados.put("anoInicio",remedio.getAnoInicio());
        dados.put("diaFim",remedio.getDiaFim());
        dados.put("mesFim",remedio.getMesFim());
        dados.put("anoFim",remedio.getAnoFim());
        dados.put("horario1",remedio.getHorario1());
        dados.put("horario2",remedio.getHorario2());
        dados.put("horario3",remedio.getHorario3());
        dados.put("horario4",remedio.getHorario4());
        dados.put("quantidade",remedio.getQuantidade());
        dados.put("idMedico",remedio.getIdMedico());
        dados.put("idUsuario",remedio.getIdUsuario());
        //db.update("remedio",dados,"id = ?", new String[]{id+""});
        db.update("remedio",dados,"id = ?   and   idUsuario = ?", new String[]{id+"", idUsuarioAtual+""});
    }
    public List<String> buscaMedicosRemedios(int idUsuarioAtual){

        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT remedio.idMedico, medico.nome FROM remedio JOIN medico ON remedio.idMedico = medico.id WHERE remedio.idUsuario = "+ idUsuarioAtual +" AND medico.idUsuario = "+ idUsuarioAtual +" GROUP BY medico.nome ORDER BY medico.nome COLLATE NOCASE ASC;";
        Cursor c = db.rawQuery(sql, null);
        List<String> medicos = new ArrayList<String>();
        while(c.moveToNext()){
            medicos.add(c.getString(c.getColumnIndex("nome")));
        }
        return medicos;
    }
    public boolean deletarRemediosPorIdUsuario(int idUsuarioAtual)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM remedio WHERE idUsuario = "+ idUsuarioAtual + ";";
        //Cursor c = db.rawQuery(sql, null);
        db.execSQL(sql);
        return true;
    }
    /*
    public List<Integer> buscaIdRemedioString(String nome,String valor,String ordem) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id FROM remedio WHERE " + nome + " = '" + valor + "' ORDER BY "+nome+" COLLATE NOCASE " + ordem +";";
        Cursor c = db.rawQuery(sql, null);
        List<Integer> remedios = new ArrayList<Integer>();
        while (c.moveToNext()) {
            remedios.add(c.getInt(c.getColumnIndex("id")));
        }
        return remedios;
    }
    public List<Integer> buscaIdRemedioInt(String nome,int valor,String ordem) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM remedio WHERE " + nome + " = " + valor + " ORDER BY "+nome+" COLLATE NOCASE " + ordem +";";
        Cursor c = db.rawQuery(sql, null);
        List<Integer> remedios = new ArrayList<Integer>();
        while (c.moveToNext()) {
            remedios.add(c.getInt(c.getColumnIndex("id")));
        }
        return remedios;
    }*/
    public List<Integer> buscaIdRemedio(String nome,String valor,String ordem,int idUsuarioAtual) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM remedio WHERE " + nome + " = " + valor + " AND idUsuario = " + idUsuarioAtual + " ORDER BY "+nome+" COLLATE NOCASE " + ordem +";";
        Cursor c = db.rawQuery(sql, null);
        List<Integer> remedios = new ArrayList<Integer>();
        while (c.moveToNext()) {
            remedios.add(c.getInt(c.getColumnIndex("id")));
        }
        return remedios;
    }
}


