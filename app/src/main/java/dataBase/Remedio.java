package dataBase;

public class Remedio {

    int id;
    String nome;
    String formato;
    String dosagem;
    int DiaInicio;
    int MesInicio;
    int AnoInicio;
    int DiaFim;
    int MesFim;
    int AnoFim;
    String horario1;
    String horario2;
    String horario3;
    String horario4;
    String quantidade;
    int idMedico;
    int idUsuario;

    public int getId(){return id;}
    public void setId(int id){this.id = id;}
    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFormato() {
        return formato;
    }

    public void setFormato(String formato) {
        this.formato = formato;
    }

    public String getDosagem() {
        return dosagem;
    }

    public void setDosagem(String dosagem) {
        this.dosagem = dosagem;
    }

    public int getDiaInicio() {
        return DiaInicio;
    }

    public void setDiaInicio(int diaInicio) {
        DiaInicio = diaInicio;
    }

    public int getMesInicio() {
        return MesInicio;
    }

    public void setMesInicio(int mesInicio) {
        MesInicio = mesInicio;
    }

    public int getAnoInicio() {
        return AnoInicio;
    }

    public void setAnoInicio(int anoInicio) {
        AnoInicio = anoInicio;
    }

    public int getDiaFim() {
        return DiaFim;
    }

    public void setDiaFim(int diaFim) {
        DiaFim = diaFim;
    }

    public int getMesFim() {
        return MesFim;
    }

    public void setMesFim(int mesFim) {
        MesFim = mesFim;
    }

    public int getAnoFim() {
        return AnoFim;
    }

    public void setAnoFim(int anoFim) {
        AnoFim = anoFim;
    }

    public String getHorario1() {
        return horario1;
    }

    public void setHorario1(String horario1) {
        this.horario1 = horario1;
    }

    public String getHorario2() {
        return horario2;
    }

    public void setHorario2(String horario2) {
        this.horario2 = horario2;
    }

    public String getHorario3() {
        return horario3;
    }

    public void setHorario3(String horario3) {
        this.horario3 = horario3;
    }

    public String getHorario4() {
        return horario4;
    }

    public void setHorario4(String horario4) {
        this.horario4 = horario4;
    }

    public String getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(String quantidade) {
        this.quantidade = quantidade;
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
