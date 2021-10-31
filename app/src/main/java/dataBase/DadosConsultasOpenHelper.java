package dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DadosConsultasOpenHelper extends SQLiteOpenHelper {
    public DadosConsultasOpenHelper(@Nullable Context context) {
        super(context, "DadosMedicos", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE consulta(id Integer PRIMARY KEY AUTOINCREMENT,dia Integer, mes Integer, ano Integer,hora Integer,idCalendario TEXT, idMedico Integer,idUsuario Integer REFERENCES usuario (id) );";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS consulta;";
        db.execSQL(sql);
        onCreate(db);
    }
    public long AdicionarNovaConsulta (Consulta consulta)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put("dia",consulta.getDia());
        dados.put("mes",consulta.getMes());
        dados.put("ano",consulta.getAno());
        dados.put("hora",consulta.getHora());
        dados.put("idCalendario",consulta.getIdCalendario());
        dados.put("idMedico",consulta.getIdMedico());
        dados.put("idUsuario",consulta.getIdUsuario());
        return db.insert("consulta",null,dados);
    }

    public List<Consulta> BuscaConsultas(String orderColuna, String ordem, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        //ordem pode ser ASC (A-Z) ou DESC (Z-A)
        String sql = "SELECT * FROM consulta WHERE idUsuario = " + idUsuarioAtual + " ORDER BY " + orderColuna + " COLLATE NOCASE "+ ordem + ";";
        Cursor c = db.rawQuery(sql,null);
        List<Consulta> consultas = new ArrayList<Consulta>();
        while (c.moveToNext()){
            Consulta consulta = new Consulta();
            consulta.setId(c.getInt(c.getColumnIndex("id")));
            consulta.setDia(c.getInt(c.getColumnIndex("dia")));
            consulta.setMes(c.getInt(c.getColumnIndex("mes")));
            consulta.setAno(c.getInt(c.getColumnIndex("ano")));
            consulta.setHora(c.getString(c.getColumnIndex("hora")));
            consulta.setIdCalendario(c.getString(c.getColumnIndex("idCalendario")));
            consulta.setIdMedico(c.getInt(c.getColumnIndex("idMedico")));
            consulta.setIdUsuario(c.getInt(c.getColumnIndex("idUsuario")));
            consultas.add(consulta);
        }
        db.close();
        return consultas;
    }

        public Consulta BuscaConsulta(int id, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM consulta WHERE id = "+ id +" and idUsuario = " + idUsuarioAtual+";";
        Cursor c = db.rawQuery(sql,null);
        Consulta consulta= new Consulta();
        while (c.moveToNext()){
            consulta.setId(c.getInt(c.getColumnIndex("id")));
            consulta.setDia(c.getInt(c.getColumnIndex("dia")));
            consulta.setMes(c.getInt(c.getColumnIndex("mes")));
            consulta.setAno(c.getInt(c.getColumnIndex("ano")));
            consulta.setHora(c.getString(c.getColumnIndex("hora")));
            consulta.setIdCalendario(c.getString(c.getColumnIndex("idCalendario")));
            consulta.setIdMedico(c.getInt(c.getColumnIndex("idMedico")));
            consulta.setIdUsuario(c.getInt(c.getColumnIndex("idUsuario")));
        }
        db.close();
        return consulta;
    }
    public void DeletaConsulta(int id, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM consulta WHERE id = "+ id +" and idUsuario = " + idUsuarioAtual+";";
        db.execSQL(sql);
        db.close();
    }
    public void EditarConsulta(int id,Consulta consulta, int idUsuarioAtual){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put("dia",consulta.getDia());
        dados.put("mes",consulta.getMes());
        dados.put("ano",consulta.getAno());
        dados.put("hora",consulta.getHora());
        dados.put("idCalendario",consulta.getIdCalendario());
        dados.put("idMedico",consulta.getIdMedico());
        dados.put("idUsuario",consulta.getIdUsuario());
        //db.update("consulta",dados,"id = ?", new String[]{id+""});
        db.update("consulta",dados,"id = ?   and   idUsuario = ?", new String[]{id+"", idUsuarioAtual+""});
    }
    public List<String> buscaMedicosConsulta(int idUsuarioAtual){

        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT consulta.idMedico, medico.nome FROM consulta JOIN medico ON consulta.idMedico = medico.id WHERE consulta.idUsuario = "+ idUsuarioAtual +" AND medico.idUsuario = "+ idUsuarioAtual +" GROUP BY medico.nome ORDER BY medico.nome COLLATE NOCASE ASC;";
        Cursor c = db.rawQuery(sql, null);
        List<String> medicos = new ArrayList<String>();
        while(c.moveToNext()){
            medicos.add(c.getString(c.getColumnIndex("nome")));
        }
        return medicos;
    }
    public boolean deletarConsultaPorIdUsuario(int idUsuarioAtual)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM consulta WHERE idUsuario = "+ idUsuarioAtual + ";";
        //Cursor c = db.rawQuery(sql, null);
        db.execSQL(sql);
        return true;
    }
    public List<Integer> buscaIdConsultas(String nome,String valor,String ordem, int idUsuarioAtual) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM consulta WHERE " + nome + " = " + valor + " AND idUsuario = "+ idUsuarioAtual+" ORDER BY "+nome+" COLLATE NOCASE " + ordem +";";
        Cursor c = db.rawQuery(sql, null);
        List<Integer> consultas = new ArrayList<Integer>();
        while (c.moveToNext()) {
            consultas.add(c.getInt(c.getColumnIndex("id")));
        }
        return consultas;
    }
}
