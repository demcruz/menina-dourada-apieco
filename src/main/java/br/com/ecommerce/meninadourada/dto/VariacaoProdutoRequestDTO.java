package br.com.ecommerce.meninadourada.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;
import java.util.Objects; // Importe para equals e hashCode

/**
 * DTO para a requisição de criação/atualização de uma Variação de Produto.
 * Contém os detalhes da variação (cor, tamanho, preço, estoque) e suas imagens.
 */
public class VariacaoProdutoRequestDTO {

    @NotBlank(message = "A cor da variação é obrigatória")
    @Size(max = 50, message = "A cor não pode ter mais de 50 caracteres")
    private String cor;

    @NotBlank(message = "O tamanho da variação é obrigatório")
    @Size(max = 10, message = "O tamanho não pode ter mais de 10 caracteres")
    private String tamanho;

    @NotNull(message = "O preço da variação é obrigatório")
    @Positive(message = "O preço da variação deve ser um valor positivo")
    private BigDecimal preco;

    @NotNull(message = "O estoque da variação é obrigatório")
    @Positive(message = "O estoque da variação deve ser um valor positivo ou zero")
    private Integer estoque;

    @NotEmpty(message = "Pelo menos uma imagem é obrigatória para a variação")
    @Valid // Garante que as validações dentro de ImagemProdutoRequestDTO sejam aplicadas
    private Set<ImagemProdutoRequestDTO> imagens;

    // Construtor padrão
    public VariacaoProdutoRequestDTO() {
    }

    // Construtor com todos os argumentos
    public VariacaoProdutoRequestDTO(String cor, String tamanho, BigDecimal preco, Integer estoque, Set<ImagemProdutoRequestDTO> imagens) {
        this.cor = cor;
        this.tamanho = tamanho;
        this.preco = preco;
        this.estoque = estoque;
        this.imagens = imagens;
    }

    // Getters e Setters
    public String getCor() {
        return cor;
    }

    public void setCor(String cor) {
        this.cor = cor;
    }

    public String getTamanho() {
        return tamanho;
    }

    public void setTamanho(String tamanho) {
        this.tamanho = tamanho;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public Integer getEstoque() {
        return estoque;
    }

    public void setEstoque(Integer estoque) {
        this.estoque = estoque;
    }

    public Set<ImagemProdutoRequestDTO> getImagens() {
        return imagens;
    }

    public void setImagens(Set<ImagemProdutoRequestDTO> imagens) {
        this.imagens = imagens;
    }

    // Método toString
    @Override
    public String toString() {
        return "VariacaoProdutoRequestDTO{" +
                "cor='" + cor + '\'' +
                ", tamanho='" + tamanho + '\'' +
                ", preco=" + preco +
                ", estoque=" + estoque +
                ", imagens=" + imagens +
                '}';
    }

    // Métodos equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariacaoProdutoRequestDTO that = (VariacaoProdutoRequestDTO) o;
        return Objects.equals(cor, that.cor) && Objects.equals(tamanho, that.tamanho) && Objects.equals(preco, that.preco) && Objects.equals(estoque, that.estoque) && Objects.equals(imagens, that.imagens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cor, tamanho, preco, estoque, imagens);
    }
}