package br.com.ecommerce.meninadourada.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects; // Importe para equals e hashCode

/**
 * Representa uma variação específica de um produto (ex: cor e tamanho).
 * No MongoDB, esta classe será armazenada como um objeto aninhado dentro do documento Produto.
 */
public class VariacaoProduto {

    // ID único para esta variação (gerado no backend, não é _id de documento principal).
    private String id;

    // Cor da variação.
    @Field("cor")
    private String cor;

    // Tamanho da variação.
    @Field("tamanho")
    private String tamanho;

    // Preço específico desta variação.
    @Field("preco")
    private BigDecimal preco;

    // Quantidade em estoque desta variação.
    @Field("estoque")
    private Integer estoque;

    // Lista de imagens associadas a esta variação.
    private List<ImagemProduto> imagens = new ArrayList<>();

    // Construtor padrão
    public VariacaoProduto() {
    }

    // Construtor com todos os argumentos
    public VariacaoProduto(String id, String cor, String tamanho, BigDecimal preco, Integer estoque, List<ImagemProduto> imagens) {
        this.id = id;
        this.cor = cor;
        this.tamanho = tamanho;
        this.preco = preco;
        this.estoque = estoque;
        this.imagens = imagens;
    }

    // Getters e Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

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

    public List<ImagemProduto> getImagens() {
        return imagens;
    }

    public void setImagens(List<ImagemProduto> imagens) {
        this.imagens = imagens;
    }

    /**
     * Adiciona uma imagem à lista de imagens desta variação.
     * @param imagem A imagem do produto a ser adicionada.
     */
    public void addImagem(ImagemProduto imagem) {
        if (this.imagens == null) {
            this.imagens = new ArrayList<>();
        }
        this.imagens.add(imagem);
    }

    // Método toString
    @Override
    public String toString() {
        return "VariacaoProduto{" +
                "id='" + id + '\'' +
                ", cor='" + cor + '\'' +
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
        VariacaoProduto that = (VariacaoProduto) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}