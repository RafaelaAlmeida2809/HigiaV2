package dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DadosExamesOpenHelper extends SQLiteOpenHelper {
    public DadosExamesOpenHelper(@Nullable Context context) {
        super(context, "DadosMedicos", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE imagem_exame(id Integer PRIMARY KEY AUTOINCREMENT,idExame Integer,nome TEXT,idUsuario Integer REFERENCES usuario (id));";
        db.execSQL(sql);

        sql = "CREATE TABLE exame(id Integer PRIMARY KEY AUTOINCREMENT,tipo TEXT,parteCorpo TEXT,dia Integer, mes Integer, ano Integer,idMedico Integer REFERENCES medico(id) on delete cascade,idUsuario Integer REFERENCES usuario (id) );";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS exame";
        db.execSQL(sql);
        sql = "DROP TABLE IF EXISTS imagem_exame";
        db.execSQL(sql);
        onCreate(db);
    }
    public long AdicionarNovoExame (Exame exame)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put("tipo",exame.getTipo());
        dados.put("parteCorpo",exame.getParteCorpo());
        dados.put("dia",exame.getDia());
        dados.put("mes",exame.getMes());
        dados.put("ano",exame.getAno());
        dados.put("idMedico",exame.getIdMedico());
        dados.put("idUsuario",exame.getIdUsuario());
        return db.insert("exame",null,dados);

    }

    public void AdicionarImagem (String nome, int idExame, int idUsuarioAtual)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put("nome",nome);
        dados.put("idExame",idExame);
        dados.put("idUsuario",idUsuarioAtual);
        db.insert("imagem_exame",null,dados);
    }

    public List<Exame> BuscaExames(String orderColuna, String ordem, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        //ordem pode ser ASC (A-Z) ou DESC (Z-A)
        String sql = "SELECT * FROM exame WHERE idUsuario = " + idUsuarioAtual + " ORDER BY " + orderColuna + " COLLATE NOCASE "+ ordem + ";";

        Cursor c = db.rawQuery(sql,null);
        List<Exame> exames= new ArrayList<Exame>();
        while (c.moveToNext()){
            Exame exame = new Exame();
            exame.setId(c.getInt(c.getColumnIndex("id")));
            exame.setTipo(c.getString(c.getColumnIndex("tipo")));
            exame.setParteCorpo(c.getString(c.getColumnIndex("parteCorpo")));
            exame.setDia(c.getInt(c.getColumnIndex("dia")));
            exame.setMes(c.getInt(c.getColumnIndex("mes")));
            exame.setAno(c.getInt(c.getColumnIndex("ano")));
            exame.setIdMedico(c.getInt(c.getColumnIndex("idMedico")));
            exame.setIdUsuario(c.getInt(c.getColumnIndex("idUsuario")));
            exames.add(exame);
        }
        db.close();
        return exames;
    }

    public void BuscaImagensExame(Exame exame, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        //ordem pode ser ASC (A-Z) ou DESC (Z-A)
        String sql = "SELECT * FROM imagem_exame WHERE idExame = "+exame.getId()+ " AND idUsuario = " + idUsuarioAtual +  " ORDER BY id;";
        Cursor c = db.rawQuery(sql,null);
        while (c.moveToNext()){
            exame.getNomesImagens().add(c.getString(c.getColumnIndex("nome")));
            exame.getIdImagens().add(c.getInt(c.getColumnIndex("id")));
        }
        db.close();
    }

    public Exame BuscaExame(int id, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM exame WHERE id = "+ id +" AND idUsuario = " + idUsuarioAtual + ";";
        Cursor c = db.rawQuery(sql,null);
        Exame exame= new Exame();
        while (c.moveToNext()){
            exame.setId(c.getInt(c.getColumnIndex("id")));
            exame.setTipo(c.getString(c.getColumnIndex("tipo")));
            exame.setParteCorpo(c.getString(c.getColumnIndex("parteCorpo")));
            exame.setDia(c.getInt(c.getColumnIndex("dia")));
            exame.setMes(c.getInt(c.getColumnIndex("mes")));
            exame.setAno(c.getInt(c.getColumnIndex("ano")));
            exame.setIdMedico(c.getInt(c.getColumnIndex("idMedico")));
            exame.setIdUsuario(c.getInt(c.getColumnIndex("idUsuario")));
        }
        db.close();
        return exame;
    }
    public void DeletaExame(int id, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM exame WHERE id = "+ id +" AND idUsuario = " + idUsuarioAtual + ";";
        db.execSQL(sql);
        //db.close();
        sql = "DELETE FROM imagem_exame WHERE idExame = "+ id +" AND idUsuario = " + idUsuarioAtual + ";";
        db.execSQL(sql);
        db.close();
    }
    public void DeletaImagem(int id, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM imagem_exame WHERE id = "+ id +" AND idUsuario = " + idUsuarioAtual + ";";
        db.execSQL(sql);
        db.close();
    }
    public void EditarExame(int id, Exame exame, int idUsuarioAtual){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();

        dados.put("tipo",exame.getTipo());
        dados.put("parteCorpo",exame.getParteCorpo());
        dados.put("dia",exame.getDia());
        dados.put("mes",exame.getMes());
        dados.put("ano",exame.getAno());
        dados.put("idMedico",exame.getIdMedico());
        dados.put("idUsuario",exame.getIdUsuario());
        //db.update("exame",dados,"id = ?", new String[]{id+""});
        db.update("exame",dados,"id = ?   and   idUsuario = ?", new String[]{id+"", idUsuarioAtual+""});
    }
    public List<String> buscaMedicosExames(int idUsuarioAtual){

        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT exame.idMedico, medico.nome FROM exame  JOIN medico ON exame.idMedico = medico.id WHERE exame.idUsuario = "+ idUsuarioAtual +" AND medico.idUsuario = "+ idUsuarioAtual +" GROUP BY medico.nome ORDER BY medico.nome COLLATE NOCASE ASC;";
        Cursor c = db.rawQuery(sql, null);
        List<String> medicos = new ArrayList<String>();
        while(c.moveToNext()){
            medicos.add(c.getString(c.getColumnIndex("nome")));
        }
        return medicos;
    }
    public boolean deletarExamePorIdUsuario(int idUsuarioAtual)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM exame WHERE idUsuario = "+ idUsuarioAtual + ";";
        //Cursor c = db.rawQuery(sql, null);
        db.execSQL(sql);
        return true;
    }
   /* public List<Integer> buscaIdExamesString(String nome,String valor,String ordem) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id FROM exame WHERE " + nome + " = '" + valor + "' ORDER BY "+nome+" COLLATE NOCASE " + ordem +";";
        Cursor c = db.rawQuery(sql, null);
        List<Integer> exames = new ArrayList<Integer>();
        while (c.moveToNext()) {
            exames.add(c.getInt(c.getColumnIndex("id")));
        }
        return exames;
    }
    public List<Integer> buscaIdExamesInt(String nome,int valor,String ordem) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM exame WHERE " + nome + " = " + valor + " ORDER BY "+nome+" COLLATE NOCASE " + ordem +";";
        Cursor c = db.rawQuery(sql, null);
        List<Integer> exames = new ArrayList<Integer>();
        while (c.moveToNext()) {
            exames.add(c.getInt(c.getColumnIndex("id")));
        }
        return exames;
    }
    */
   public List<Integer> buscaIdExames(String nome,String valor,String ordem, int idUsuarioAtual) {

        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM exame WHERE " + nome + " = " + valor + " AND idUsuario = " + idUsuarioAtual + " ORDER BY "+nome+" COLLATE NOCASE " + ordem +";";
        Cursor c = db.rawQuery(sql, null);
        List<Integer> exames = new ArrayList<Integer>();
        while (c.moveToNext()) {
            exames.add(c.getInt(c.getColumnIndex("id")));
        }
        return exames;
    }
}


