package dataBase;

public class Medico {

    int id;
    String nome;
    String especialidade;
    String telefone;
    int cep;
    int numero;
    String logradouro;
    String bairro;
    String estado;
    String cidade;
    String uriImagem;
    int idUsuario;

    public int getId(){return id;}
    public void setId(int id){this.id = id;}
    public String getNome(){return nome;}
    public void setNome(String nome){this.nome = nome;}
    public String getEspecialidade(){return especialidade;}
    public void setEspecialidade(String especialidade){this.especialidade = especialidade;}
    public String getTelefone(){return telefone;}
    public void setTelefone(String telefone){this.telefone = telefone;}
    public int getCep(){return cep;}
    public void setCep(int cep){this.cep = cep;}
    public int getNumero(){return numero;}
    public void setNumero(int numero){this.numero = numero;}
    public String getLogradouro() {return logradouro;}
    public void setLogradouro(String logradouro) {this.logradouro = logradouro;}
    public String getBairro() {return bairro;}
    public void setBairro(String bairro) {this.bairro = bairro;}
    public String getEstado() {return estado;}
    public void setEstado(String estado) {this.estado = estado;}
    public String getCidade() {return cidade;}
    public void setCidade(String cidade) {this.cidade = cidade;}

    public String getUriImagem() {
        return uriImagem;
    }

    public void setUriImagem(String uriImagem) {
        this.uriImagem = uriImagem;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }
}
