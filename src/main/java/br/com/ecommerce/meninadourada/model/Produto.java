package br.com.ecommerce.meninadourada.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects; // Importe para equals e hashCode

/**
 * Representa um produto principal no MongoDB.
 * A anotação @Document mapeia esta classe para uma coleção no MongoDB.
 */
@Document(collection = "produtos") // Nome da coleção no MongoDB
public class Produto {

    // O ID do produto. @Id mapeia para o campo _id do MongoDB.
    @Id
    private String id;

    // Nome do produto.
    @Field("nome")
    private String nome;

    // Descrição detalhada do produto.
    @Field("descricao")
    private String descricao;

    // Status de atividade do produto (true se ativo, false se inativo).
    @Field("ativo")
    private Boolean ativo;

    // Lista de variações do produto (ex: diferentes cores e tamanhos).
    private List<VariacaoProduto> variacoes = new ArrayList<>();

    // Construtor padrão (sem argumentos)
    public Produto() {
    }

    // Construtor com todos os argumentos
    public Produto(String id, String nome, String descricao, Boolean ativo, List<VariacaoProduto> variacoes) {
        this.id = id;
        this.nome = nome;
        this.descricao = descricao;
        this.ativo = ativo;
        this.variacoes = variacoes;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public List<VariacaoProduto> getVariacoes() {
        return variacoes;
    }

    public void setVariacoes(List<VariacaoProduto> variacoes) {
        this.variacoes = variacoes;
    }

    /**
     * Adiciona uma variação à lista de variações do produto.
     * @param variacao A variação do produto a ser adicionada.
     */
    public void addVariacao(VariacaoProduto variacao) {
        if (this.variacoes == null) {
            this.variacoes = new ArrayList<>();
        }
        this.variacoes.add(variacao);
    }

    // Método toString
    @Override
    public String toString() {
        return "Produto{" +
                "id='" + id + '\'' +
                ", nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", ativo=" + ativo +
                ", variacoes=" + variacoes +
                '}';
    }

    // Métodos equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Produto produto = (Produto) o;
        return Objects.equals(id, produto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}