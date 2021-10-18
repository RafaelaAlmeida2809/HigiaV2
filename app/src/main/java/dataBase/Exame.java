package dataBase;

import java.util.ArrayList;
import java.util.List;

public class Exame {
    int id;
    String tipo;
    String parteCorpo;
    int Dia;
    int Mes;
    int Ano;
    int idMedico;
    List<String> nomesImagens = new ArrayList<>();
    List<Integer> idImagens = new ArrayList<>();
    int idUsuario;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getParteCorpo() {
        return parteCorpo;
    }

    public void setParteCorpo(String parteCorpo) {
        this.parteCorpo = parteCorpo;
    }

    public int getDia() {
        return Dia;
    }

    public void setDia(int dia) {
        Dia = dia;
    }

    public int getMes() {
        return Mes;
    }

    public int getAno() {
        return Ano;
    }

    public void setAno(int ano) {
        Ano = ano;
    }

    public void setMes(int mes) {
        Mes = mes;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public List<String> getNomesImagens() {
        return nomesImagens;
    }
    public void setNomesImagens(List<String> nomesImagens) {
        this.nomesImagens = nomesImagens;
    }

    public List<Integer> getIdImagens() {
        return idImagens;
    }

    public void setIdImagens(List<Integer> idImagens) {
        this.idImagens = idImagens;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}
