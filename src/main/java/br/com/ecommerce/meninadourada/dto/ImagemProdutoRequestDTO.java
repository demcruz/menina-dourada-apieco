package br.com.ecommerce.meninadourada.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.Objects; // Importe para equals e hashCode

/**
 * DTO para a requisição de criação/atualização de uma Imagem de Produto.
 * Contém o URL da imagem, texto alternativo e se é a imagem principal.
 */
public class ImagemProdutoRequestDTO {

    @NotBlank(message = "O URL da imagem é obrigatório")
    @URL(message = "URL de imagem inválido") // Valida se a string é um URL válido
    @Size(max = 500, message = "O URL da imagem não pode ter mais de 500 caracteres")
    private String url;

    @Size(max = 255, message = "O texto alternativo não pode ter mais de 255 caracteres")
    private String altText;

    private Boolean isPrincipal = false; // Indica se é a imagem principal para esta variação

    // Construtor padrão
    public ImagemProdutoRequestDTO() {
    }

    // Construtor com todos os argumentos
    public ImagemProdutoRequestDTO(String url, String altText, Boolean isPrincipal) {
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
        return "ImagemProdutoRequestDTO{" +
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
        ImagemProdutoRequestDTO that = (ImagemProdutoRequestDTO) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }
}