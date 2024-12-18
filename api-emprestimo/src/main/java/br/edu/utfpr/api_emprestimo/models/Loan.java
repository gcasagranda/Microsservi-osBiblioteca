package br.edu.utfpr.api_emprestimo.models;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@Document("Loan")
public class Loan {

    @Id
    private String id;
    private String idUsuario;
    private String idLivro;
    private LocalDate dataEmprestimo;
    private LocalDate dataDevolucaoEsperada;
    private LocalDate dataRealDevolucao;
    private boolean emprestimoAtivo;

    public Loan() {
    }

    public Loan(String id, String idUsuario, String idLivro, LocalDate dataEmprestimo,
                LocalDate dataDevolucaoEsperada, LocalDate dataRealDevolucao,
                boolean emprestimoAtivo) {
        this.id = id;
        this.idUsuario = idUsuario;
        this.idLivro = idLivro;
        this.dataEmprestimo = dataEmprestimo;
        this.dataDevolucaoEsperada = dataDevolucaoEsperada;
        this.dataRealDevolucao = dataRealDevolucao;
        this.emprestimoAtivo = emprestimoAtivo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(String idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getIdLivro() {
        return idLivro;
    }

    public void setIdLivro(String idLivro) {
        this.idLivro = idLivro;
    }

    public LocalDate getDataEmprestimo() {
        return dataEmprestimo;
    }

    public void setDataEmprestimo(LocalDate dataEmprestimo) {
        this.dataEmprestimo = dataEmprestimo;
    }

    public LocalDate getDataDevolucaoEsperada() {
        return dataDevolucaoEsperada;
    }

    public void setDataDevolucaoEsperada(LocalDate dataDevolucaoEsperada) {
        this.dataDevolucaoEsperada = dataDevolucaoEsperada;
    }

    public LocalDate getDataRealDevolucao() {
        return dataRealDevolucao;
    }

    public void setDataRealDevolucao(LocalDate dataRealDevolucao) {
        this.dataRealDevolucao = dataRealDevolucao;
    }

    public boolean isEmprestimoAtivo() {
        return emprestimoAtivo;
    }

    public void setEmprestimoAtivo(boolean emprestimoAtivo) {
        this.emprestimoAtivo = emprestimoAtivo;
    }

}
