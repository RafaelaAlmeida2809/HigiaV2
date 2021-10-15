package dataBase;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DadosMedicosOpenHelper extends SQLiteOpenHelper {
    public DadosMedicosOpenHelper(@Nullable Context context) {
        super(context, "DadosMedicos", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE medico(id Integer PRIMARY KEY AUTOINCREMENT,nome TEXT,especialidade TEXT,telefone TEXT,cep Integer,numero Integer,logradouro TEXT,bairro TEXT,estado TEXT,cidade TEXT,uriImagem TEXT,idUsuario Integer REFERENCES usuario (id) );";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS medico;";
        db.execSQL(sql);
        onCreate(db);
    }

    public boolean exists() {
        try {
            SQLiteDatabase db = getReadableDatabase();
            String sql = "SELECT * FROM medico";
            db.execSQL(sql);
            db.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public void AdicionarNovoMedico (Medico medico)
    {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put("nome",medico.getNome());
        dados.put("especialidade",medico.getEspecialidade());
        dados.put("telefone",medico.getTelefone());
        dados.put("cep",medico.getCep());
        dados.put("numero",medico.getNumero());
        dados.put("logradouro",medico.getLogradouro());
        dados.put("bairro",medico.getBairro());
        dados.put("estado",medico.getEstado());
        dados.put("cidade",medico.getCidade());
        dados.put("uriImagem",medico.getUriImagem());
        dados.put("idUsuario",medico.getIdUsuario());
        db.insert("medico",null,dados);

    }
    public List<Medico> BuscaMedicos(String orderColuna, String ordem, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        //ordem pode ser ASC (A-Z) ou DESC (Z-A)
        String sql = "SELECT * FROM medico WHERE idUsuario = " + idUsuarioAtual + " ORDER BY " + orderColuna + " COLLATE NOCASE "+ ordem + ";";
        Cursor c = db.rawQuery(sql,null);
        List<Medico> medicos= new ArrayList<Medico>();
        while (c.moveToNext()){
            Medico medico = new Medico();
            medico.setId(c.getInt(c.getColumnIndex("id")));
            medico.setNome(c.getString(c.getColumnIndex("nome")));
            medico.setEspecialidade(c.getString(c.getColumnIndex("especialidade")));
            medico.setTelefone(c.getString(c.getColumnIndex("telefone")));;
            medico.setCep(c.getInt(c.getColumnIndex("cep")));
            medico.setNumero(c.getInt(c.getColumnIndex("numero")));
            medico.setLogradouro(c.getString(c.getColumnIndex("logradouro")));
            medico.setBairro(c.getString(c.getColumnIndex("bairro")));
            medico.setEstado(c.getString(c.getColumnIndex("estado")));
            medico.setCidade(c.getString(c.getColumnIndex("cidade")));
            medico.setUriImagem(c.getString(c.getColumnIndex("uriImagem")));
            medico.setIdUsuario(c.getInt(c.getColumnIndex("idUsuario")));
            medicos.add(medico);
        }
        db.close();
        return medicos;
    }
    public Medico BuscaMedico(int id, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM medico WHERE id = "+ id +" AND idUsuario = " + idUsuarioAtual+";" ;
        Cursor c = db.rawQuery(sql,null);
        Medico medico= new Medico();
        while (c.moveToNext()){
            medico.setId(c.getInt(c.getColumnIndex("id")));
            medico.setNome(c.getString(c.getColumnIndex("nome")));
            medico.setEspecialidade(c.getString(c.getColumnIndex("especialidade")));
            medico.setTelefone(c.getString(c.getColumnIndex("telefone")));;
            medico.setCep(c.getInt(c.getColumnIndex("cep")));
            medico.setNumero(c.getInt(c.getColumnIndex("numero")));
            medico.setLogradouro(c.getString(c.getColumnIndex("logradouro")));
            medico.setBairro(c.getString(c.getColumnIndex("bairro")));
            medico.setEstado(c.getString(c.getColumnIndex("estado")));
            medico.setCidade(c.getString(c.getColumnIndex("cidade")));
            medico.setUriImagem(c.getString(c.getColumnIndex("uriImagem")));
            medico.setIdUsuario(c.getInt(c.getColumnIndex("idUsuario")));
        }
        db.close();
        return medico;
    }
    public void DeletaMedico(int id, int idUsuarioAtual){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM medico WHERE id = "+ id +" AND idUsuario = " + idUsuarioAtual+";";
        db.execSQL(sql);
        db.close();
    }
    public void EditarMedico(int id,Medico medico,int idUsuarioAtual){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();

        dados.put("nome",medico.getNome());
        dados.put("especialidade",medico.getEspecialidade());
        dados.put("telefone",medico.getTelefone());
        dados.put("cep",medico.getCep());
        dados.put("numero",medico.getNumero());
        dados.put("logradouro",medico.getLogradouro());
        dados.put("bairro",medico.getBairro());
        dados.put("estado",medico.getEstado());
        dados.put("cidade",medico.getCidade());
        dados.put("uriImagem",medico.getUriImagem());
        dados.put("idUsuario",medico.getIdUsuario());
        //db.update("medico",dados,"id = ?", new String[]{id+""});
        db.update("medico",dados,"id = ?   and   idUsuario = ?", new String[]{id+"", idUsuarioAtual+""});
    }
    public boolean deletarMedicoPorIdUsuario(int idUsuarioAtual)
    {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM medico WHERE idUsuario = "+ idUsuarioAtual + ";";
        //Cursor c = db.rawQuery(sql, null);
        db.execSQL(sql);
        return true;
    }
    public List<Integer> buscaIdMedico(String nome,String valor,String ordem, int idUsuarioAtual) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id FROM medico WHERE " + nome + " = " + valor + " AND idUsuario = " + idUsuarioAtual + " ORDER BY " + nome + " COLLATE NOCASE " + ordem +";";
        Cursor c = db.rawQuery(sql, null);
        List<Integer> medicos = new ArrayList<Integer>();
        while (c.moveToNext()) {
            medicos.add(c.getInt(c.getColumnIndex("id")));
        }
        return medicos;
    }
}
