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

public class DadosUsuariosOpenHelper extends SQLiteOpenHelper {
    public DadosUsuariosOpenHelper(@Nullable Context context) {
        super(context, "DadosMedicos", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE usuario(id Integer PRIMARY KEY AUTOINCREMENT,email TEXT, linguagem Integer);";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = "DROP TABLE IF EXISTS usuario;";
        db.execSQL(sql);
        onCreate(db);
    }

    public void AdicionarNovoUsuario(String email, int linguagem ) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put("email", email);
        dados.put("linguagem", linguagem);
        db.insert("usuario", null, dados);
    }
    public Usuario BuscaUsuarioPeloEmail(String email) {
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT * FROM usuario WHERE email = '" + email + "'  limit 1;";
        Cursor c = db.rawQuery(sql, null);
        Usuario usuario = new Usuario();
        while (c.moveToNext()) {
            usuario.setId(c.getInt(c.getColumnIndex("id")));
            usuario.setEmail(c.getString(c.getColumnIndex("email")));
            usuario.setLinguagem(c.getInt(c.getColumnIndex("linguagem")));
        }
        db.close();
        return usuario;
    }
    public Integer QuantidadeUsuarios(){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id FROM usuario";
        Cursor c = db.rawQuery(sql,null);
        List<Integer> idUsuario = new ArrayList<Integer>();
        while (c.moveToNext()) {
            idUsuario.add(c.getInt(c.getColumnIndex("id")));
        }
        return idUsuario.size();
    }
    public Integer BuscaUsuarioPeloEmail2(String email) {
        Integer ID = -1;
        SQLiteDatabase db = getReadableDatabase();
        String sql = "SELECT id FROM usuario WHERE email = '" + email + "'  limit 1;";
        Cursor c = db.rawQuery(sql, null);
        while (c.moveToNext()) {
            ID = c.getInt(c.getColumnIndex("id"));
        }
        db.close();
        return ID;
    }
    public void DeletaUsuario(int id){
        SQLiteDatabase db = getReadableDatabase();
        String sql = "DELETE FROM usuario WHERE id = "+ id +";";
        db.execSQL(sql);
        db.close();
    }
    public void EditarUsuario(int id, int linguagem){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues dados = new ContentValues();
        dados.put("linguagem",linguagem);
        db.update("usuario",dados,"id = ?", new String[]{id+""});
    }
}
