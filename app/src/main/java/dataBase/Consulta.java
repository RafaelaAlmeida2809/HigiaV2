package dataBase;

public class Consulta {
    int id;
    int Dia;
    int Mes;
    int Ano;
    String Hora;
    String idCalendario;
    int idMedico;
    int idUsuario;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getHora() {
        return Hora;
    }

    public void setHora(String hora) {
        Hora = hora;
    }

    public String getIdCalendario() {
        return idCalendario;
    }

    public void setIdCalendario(String idConsulta) {
        this.idCalendario = idConsulta;
    }

    public int getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(int idMedico) {
        this.idMedico = idMedico;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}
