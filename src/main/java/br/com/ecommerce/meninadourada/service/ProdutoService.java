package br.com.ecommerce.meninadourada.service;

import br.com.ecommerce.meninadourada.dto.ImagemProdutoRequestDTO;
import br.com.ecommerce.meninadourada.dto.ProdutoRequestDTO;
import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException;
import br.com.ecommerce.meninadourada.model.ImagemProduto;
import br.com.ecommerce.meninadourada.model.Produto;
import br.com.ecommerce.meninadourada.model.VariacaoProduto;

import br.com.ecommerce.meninadourada.repository.ProdutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bson.types.ObjectId; // Importe para usar ObjectId
// import java.util.UUID; // Removido, pois não estamos mais usando UUID para IDs
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Camada de Serviço para gerenciar operações relacionadas a Produtos.
 * Contém a lógica de negócio para cadastrar, buscar, atualizar e deletar produtos.
 */
@Service
public class ProdutoService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoService.class);

    private final ProdutoRepository produtoRepository;

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    /**
     * Cadastra um novo produto com suas variações e imagens no MongoDB.
     *
     * @param dto O DTO de requisição contendo os dados do produto.
     * @return O objeto Produto salvo no MongoDB.
     */
    @Transactional
    public Produto cadastrarProdutoComVariacoes(ProdutoRequestDTO dto) {
        logger.info("Iniciando cadastro de produto: {}", dto.getNome());

        // Cria uma nova instância de Produto e gera um ObjectId como ID
        Produto produto = new Produto();
        produto.setId(new ObjectId().toHexString()); // Gera um ObjectId para o Produto
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setAtivo(true); // Produtos são ativos por padrão

        if (dto.getVariacoes() != null && !dto.getVariacoes().isEmpty()) {
            dto.getVariacoes().forEach(variacaoDTO -> {
                // Cria uma nova instância de VariacaoProduto e gera um ObjectId como ID
                VariacaoProduto variacao = new VariacaoProduto();
                variacao.setId(new ObjectId().toHexString()); // Gera um ObjectId para a Variação
                variacao.setCor(variacaoDTO.getCor());
                variacao.setTamanho(variacaoDTO.getTamanho());
                variacao.setPreco(variacaoDTO.getPreco());
                variacao.setEstoque(variacaoDTO.getEstoque());

                if (variacaoDTO.getImagens() != null && !variacaoDTO.getImagens().isEmpty()) {
                    variacaoDTO.getImagens().forEach(imagemDTO -> {
                        // Cria uma nova instância de ImagemProduto
                        ImagemProduto imagem = new ImagemProduto();
                        imagem.setUrl(imagemDTO.getUrl());
                        imagem.setAltText(imagemDTO.getAltText());
                        imagem.setIsPrincipal(imagemDTO.getIsPrincipal() != null ? imagemDTO.getIsPrincipal() : false);
                        variacao.addImagem(imagem);
                    });
                }
                produto.addVariacao(variacao);
            });
        }

        Produto savedProduto = produtoRepository.save(produto);
        logger.info("Produto cadastrado com sucesso. ID: {}", savedProduto.getId());
        return savedProduto;
    }

    /**
     * Cadastra uma lista de produtos em lote no MongoDB.
     * Esta operação irá gerar novos IDs para cada produto e variação,
     * o que significa que chamar este método múltiplas vezes com a mesma
     * massa de dados resultará em produtos duplicados no banco de dados.
     * Para evitar duplicação semântica, seria necessário implementar
     * lógica de verificação de existência baseada em atributos de negócio (ex: SKU).
     *
     * @param dtos Uma lista de DTOs de Produto.
     * @return Uma lista dos objetos Produto salvos.
     * @throws IllegalArgumentException Se a lista de DTOs for nula ou vazia.
     * @throws RuntimeException         Se ocorrer um erro durante a persistência.
     */
    @Transactional
    public List<Produto> cadastrarProdutosEmLote(List<ProdutoRequestDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            logger.warn("Tentativa de cadastrar produtos em lote com lista vazia ou nula.");
            throw new IllegalArgumentException("A lista de produtos para cadastro em lote não pode ser vazia.");
        }

        logger.info("Iniciando cadastro de {} produtos em lote.", dtos.size());
        try {
            List<Produto> produtosParaSalvar = dtos.stream()
                    .map(dto -> {
                        // Reutiliza a lógica de mapeamento de um único produto
                        Produto produto = new Produto();
                        produto.setId(new ObjectId().toHexString()); // Gera um ObjectId para o Produto
                        produto.setNome(dto.getNome());
                        produto.setDescricao(dto.getDescricao());
                        produto.setAtivo(true);

                        if (dto.getVariacoes() != null && !dto.getVariacoes().isEmpty()) {
                            dto.getVariacoes().forEach(variacaoDTO -> {
                                VariacaoProduto variacao = new VariacaoProduto();
                                variacao.setId(new ObjectId().toHexString()); // Gera um ObjectId para a Variação
                                variacao.setCor(variacaoDTO.getCor());
                                variacao.setTamanho(variacaoDTO.getTamanho());
                                variacao.setPreco(variacaoDTO.getPreco());
                                variacao.setEstoque(variacaoDTO.getEstoque());

                                if (variacaoDTO.getImagens() != null && !variacaoDTO.getImagens().isEmpty()) {
                                    variacaoDTO.getImagens().forEach(imagemDTO -> {
                                        ImagemProduto imagem = new ImagemProduto();
                                        imagem.setUrl(imagemDTO.getUrl());
                                        imagem.setAltText(imagemDTO.getAltText());
                                        imagem.setIsPrincipal(imagemDTO.getIsPrincipal() != null ? imagemDTO.getIsPrincipal() : false);
                                        variacao.addImagem(imagem);
                                    });
                                }
                                produto.addVariacao(variacao);
                            });
                        }
                        return produto;
                    })
                    .collect(Collectors.toList());

            List<Produto> savedProducts = produtoRepository.saveAll(produtosParaSalvar);
            logger.info("{} produtos cadastrados em lote com sucesso.", savedProducts.size());
            return savedProducts;
        } catch (Exception e) {
            logger.error("Erro ao cadastrar produtos em lote: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao cadastrar produtos em lote: " + e.getMessage(), e);
        }
    }

    /**
     * Lista todos os produtos do MongoDB com suporte a paginação.
     *
     * @param page O número da página a ser recuperada (base 0).
     * @param size O número de itens por página.
     * @return Uma página de objetos Produto.
     */
    public Page<Produto> listarTodosProdutosPaginado(int page, int size) {
        logger.info("Buscando produtos paginados (página: {}, tamanho: {}).", page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<Produto> produtosPage = produtoRepository.findAll(pageable);
        logger.info("Página {} de produtos recuperada. Total de elementos: {}.", produtosPage.getNumber(), produtosPage.getTotalElements());
        return produtosPage;
    }

    /**
     * Busca um produto pelo seu ID.
     *
     * @param id O ID do produto a ser buscado.
     * @return O objeto Produto encontrado.
     * @throws ResourceNotFoundException Se o produto não for encontrado.
     */
    public Produto buscarProdutoPorId(String id) {
        logger.info("Buscando produto com ID: {}", id);
        Optional<Produto> produtoOptional = produtoRepository.findById(id);
        Produto produto = produtoOptional.orElseThrow(() -> {
            logger.warn("Produto com ID {} não encontrado.", id);
            return new ResourceNotFoundException("Produto não encontrado com ID: " + id);
        });
        logger.info("Produto com ID {} encontrado.", id);
        return produto;
    }

    /**
     * Atualiza um produto existente.
     *
     * @param id  O ID do produto a ser atualizado.
     * @param dto O DTO com os dados atualizados do produto.
     * @return O objeto Produto atualizado.
     * @throws ResourceNotFoundException Se o produto não for encontrado.
     */
    @Transactional
    public Produto atualizarProduto(String id, ProdutoRequestDTO dto) {
        logger.info("Iniciando atualização do produto com ID: {}", id);
        Produto produtoExistente = produtoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Tentativa de atualizar produto inexistente. ID: {}", id);
                    return new ResourceNotFoundException("Produto não encontrado com ID: " + id);
                });

        produtoExistente.setNome(dto.getNome());
        produtoExistente.setDescricao(dto.getDescricao());

        // CUIDADO: Esta lógica limpa todas as variações e imagens existentes e adiciona as novas do DTO.
        // Para uma atualização mais granular (manter/atualizar/deletar variações específicas),
        // a lógica seria mais complexa, comparando IDs e fazendo merge.
        produtoExistente.getVariacoes().clear();

        if (dto.getVariacoes() != null && !dto.getVariacoes().isEmpty()) {
            dto.getVariacoes().forEach(variacaoDTO -> {
                VariacaoProduto variacao = new VariacaoProduto();
                variacao.setId(new ObjectId().toHexString()); // Gera novo ObjectId para novas variações
                variacao.setCor(variacaoDTO.getCor());
                variacao.setTamanho(variacaoDTO.getTamanho());
                variacao.setPreco(variacaoDTO.getPreco());
                variacao.setEstoque(variacaoDTO.getEstoque());

                if (variacaoDTO.getImagens() != null && !variacaoDTO.getImagens().isEmpty()) {
                    variacaoDTO.getImagens().forEach(imagemDTO -> {
                        ImagemProduto imagem = new ImagemProduto();
                        imagem.setUrl(imagemDTO.getUrl());
                        imagem.setAltText(imagemDTO.getAltText());
                        imagem.setIsPrincipal(imagemDTO.getIsPrincipal() != null ? imagemDTO.getIsPrincipal() : false);
                        variacao.addImagem(imagem);
                    });
                }
                produtoExistente.addVariacao(variacao);
            });
        }

        Produto updatedProduto = produtoRepository.save(produtoExistente);
        logger.info("Produto com ID {} atualizado com sucesso.", updatedProduto.getId());
        return updatedProduto;
    }

    /**
     * Deleta um produto pelo seu ID.
     *
     * @param id O ID do produto a ser deletado.
     * @throws ResourceNotFoundException Se o produto não for encontrado.
     */
    @Transactional
    public void deletarProduto(String id) {
        logger.info("Iniciando exclusão do produto com ID: {}", id);
        Produto produtoParaDeletar = produtoRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Tentativa de deletar produto inexistente. ID: {}", id);
                    return new ResourceNotFoundException("Produto não encontrado com ID: " + id);
                });

        produtoRepository.delete(produtoParaDeletar);
        logger.info("Produto com ID {} deletado com sucesso.", id);
    }
}
