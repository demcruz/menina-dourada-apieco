package br.com.ecommerce.meninadourada.model;

import org.springframework.data.mongodb.core.mapping.Field;
import java.util.Objects; // Importe para equals e hashCode

/**
 * Representa uma imagem associada a uma variação de produto.
 * No MongoDB, esta classe será armazenada como um objeto aninhado dentro de VariacaoProduto.
 */
public class ImagemProduto {

    // URL da imagem.
    @Field("url")
    private String url;

    // Texto alternativo para a imagem.
    @Field("altText")
    private String altText;

    // Flag para indicar se esta é a imagem principal da variação.
    @Field("isPrincipal")
    private Boolean isPrincipal;

    // Construtor padrão
    public ImagemProduto() {
    }

    // Construtor com todos os argumentos
    public ImagemProduto(String url, String altText, Boolean isPrincipal) {
        this.url = url;
        this.altText = altText;
        this.isPrincipal = isPrincipal;
    }

    // Getters e Setters
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAltText() {
        return altText;
    }

    public void setAltText(String altText) {
        this.altText = altText;
    }

    public Boolean getIsPrincipal() {
        return isPrincipal;
    }

    public void setIsPrincipal(Boolean isPrincipal) {
        this.isPrincipal = isPrincipal;
    }

    // Método toString
    @Override
    public String toString() {
        return "ImagemProduto{" +
                "url='" + url + '\'' +
                ", altText='" + altText + '\'' +
                ", isPrincipal=" + isPrincipal +
                '}';
    }

    // Métodos equals e hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ImagemProduto that = (ImagemProduto) o;
        return Objects.equals(url, that.url); // Considerando URL como identificador único para equals/hashCode
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}