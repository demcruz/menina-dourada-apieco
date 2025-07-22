package br.com.ecommerce.meninadourada.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.Objects; // Importe para equals e hashCode

/**
 * DTO para a requisição de criação/atualização de um Produto.
 * Contém os dados básicos do produto e uma lista de suas variações aninhadas.
 */
public class ProdutoRequestDTO {

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 100, message = "O nome não pode ter mais de 100 caracteres")
    private String nome;

    @Size(max = 1000, message = "A descrição não pode ter mais de 1000 caracteres")
    private String descricao;

    @NotEmpty(message = "Pelo menos uma variação de produto é obrigatória")
    @Valid // Garante que as validações dentro de VariacaoProdutoRequestDTO sejam aplicadas
    private Set<VariacaoProdutoRequestDTO> variacoes;

    // Construtor padrão
    public ProdutoRequestDTO() {
    }

    // Construtor com todos os argumentos
    public ProdutoRequestDTO(String nome, String descricao, Set<VariacaoProdutoRequestDTO> variacoes) {
        this.nome = nome;
        this.descricao = descricao;
        this.variacoes = variacoes;
    }

    // Getters e Setters
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

    public Set<VariacaoProdutoRequestDTO> getVariacoes() {
        return variacoes;
    }

    public void setVariacoes(Set<VariacaoProdutoRequestDTO> variacoes) {
        this.variacoes = variacoes;
    }

    // Método toString
    @Override
    public String toString() {
        return "ProdutoRequestDTO{" +
                "nome='" + nome + '\'' +
                ", descricao='" + descricao + '\'' +
                ", variacoes=" + variacoes +
                '}';
    }

    // Métodos equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProdutoRequestDTO that = (ProdutoRequestDTO) o;
        return Objects.equals(nome, that.nome) && Objects.equals(descricao, that.descricao) && Objects.equals(variacoes, that.variacoes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, descricao, variacoes);
    }
}