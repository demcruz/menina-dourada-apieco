package br.com.ecommerce.meninadourada.service;

import br.com.ecommerce.meninadourada.dto.ImagemProdutoRequestDTO;
import br.com.ecommerce.meninadourada.dto.ProdutoRequestDTO;
import br.com.ecommerce.meninadourada.dto.VariacaoProdutoRequestDTO;
import br.com.ecommerce.meninadourada.model.ImagemProduto;
import br.com.ecommerce.meninadourada.model.Produto;
import br.com.ecommerce.meninadourada.model.VariacaoProduto;
import br.com.ecommerce.meninadourada.repository.ProdutoRepository;
import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile; // Importe para MultipartFile
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.bson.types.ObjectId;
import java.io.IOException; // Importe para IOException
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger; // Para controlar o índice dos arquivos
import java.util.stream.Collectors;

/**
 * Camada de Serviço para gerenciar operações relacionadas a Produtos.
 * Contém a lógica de negócio para cadastrar, buscar, atualizar e deletar produtos.
 */
@Service
public class ProdutoService {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoService.class);

    private final ProdutoRepository produtoRepository;
    private final S3Service s3Service; // Injete o S3Service

    @Autowired
    public ProdutoService(ProdutoRepository produtoRepository, S3Service s3Service) {
        this.produtoRepository = produtoRepository;
        this.s3Service = s3Service;
    }

    /**
     * Cadastra um novo produto com suas variações e imagens no MongoDB,
     * realizando o upload das imagens para o S3.
     *
     * @param dto O DTO de requisição contendo os dados do produto.
     * @param files Uma lista de arquivos de imagem a serem enviados para o S3.
     * @return O objeto Produto salvo no MongoDB.
     */
    @Transactional
    public Produto cadastrarProdutoComVariacoes(ProdutoRequestDTO dto, List<MultipartFile> files) {
        logger.info("Iniciando cadastro de produto: {}", dto.getNome());

        Produto produto = new Produto();
        produto.setId(new ObjectId().toHexString());
        produto.setNome(dto.getNome());
        produto.setDescricao(dto.getDescricao());
        produto.setAtivo(true);

        // Contador atômico para mapear os arquivos da lista 'files' para as imagens do DTO
        AtomicInteger fileIndex = new AtomicInteger(0);

        if (dto.getVariacoes() != null && !dto.getVariacoes().isEmpty()) {
            dto.getVariacoes().forEach(variacaoDTO -> {
                VariacaoProduto variacao = new VariacaoProduto();
                variacao.setId(new ObjectId().toHexString());
                variacao.setCor(variacaoDTO.getCor());
                variacao.setTamanho(variacaoDTO.getTamanho());
                variacao.setPreco(variacaoDTO.getPreco());
                variacao.setEstoque(variacaoDTO.getEstoque());

                if (variacaoDTO.getImagens() != null && !variacaoDTO.getImagens().isEmpty()) {
                    variacaoDTO.getImagens().forEach(imagemDTO -> {
                        ImagemProduto imagem = new ImagemProduto();
                        // Se a imagemDTO.getUrl() estiver vazia ou for um placeholder,
                        // e houver arquivos na lista 'files', tentamos fazer o upload.
                        // Assumimos que a ordem dos 'files' corresponde à ordem das imagens a serem upadas.
                        if ((imagemDTO.getUrl() == null || imagemDTO.getUrl().isEmpty()) && files != null && !files.isEmpty()) {
                            int currentFileIdx = fileIndex.getAndIncrement();
                            if (currentFileIdx < files.size()) {
                                try {
                                    String s3Url = s3Service.uploadFile(files.get(currentFileIdx));
                                    imagem.setUrl(s3Url); // Define a URL do S3
                                    logger.info("Imagem {} upada para S3: {}", files.get(currentFileIdx).getOriginalFilename(), s3Url);
                                } catch (IOException e) {
                                    logger.error("Falha ao fazer upload da imagem para S3: {}", e.getMessage(), e);
                                    throw new RuntimeException("Falha ao fazer upload da imagem para S3: " + e.getMessage(), e);
                                }
                            } else {
                                logger.warn("Mais imagens no DTO do que arquivos fornecidos. Imagem {} não terá URL S3.", imagemDTO.getAltText());
                                imagem.setUrl(imagemDTO.getUrl()); // Mantém a URL original (pode ser vazia)
                            }
                        } else {
                            imagem.setUrl(imagemDTO.getUrl()); // Usa a URL já fornecida (pode ser de um upload anterior ou externa)
                        }
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
     * Esta operação não suporta upload de imagens diretamente.
     * As URLs das imagens devem ser pré-existentes no S3 e incluídas no JSON.
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
                        Produto produto = new Produto();
                        produto.setId(new ObjectId().toHexString());
                        produto.setNome(dto.getNome());
                        produto.setDescricao(dto.getDescricao());
                        produto.setAtivo(true);

                        if (dto.getVariacoes() != null && !dto.getVariacoes().isEmpty()) {
                            dto.getVariacoes().forEach(variacaoDTO -> {
                                VariacaoProduto variacao = new VariacaoProduto();
                                variacao.setId(new ObjectId().toHexString());
                                variacao.setCor(variacaoDTO.getCor());
                                variacao.setTamanho(variacaoDTO.getTamanho());
                                variacao.setPreco(variacaoDTO.getPreco());
                                variacao.setEstoque(variacaoDTO.getEstoque());

                                if (variacaoDTO.getImagens() != null && !variacaoDTO.getImagens().isEmpty()) {
                                    variacaoDTO.getImagens().forEach(imagemDTO -> {
                                        ImagemProduto imagem = new ImagemProduto();
                                        imagem.setUrl(imagemDTO.getUrl()); // A URL da imagem (do S3) é recebida aqui
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
     * Atualiza um produto existente, realizando o upload de novas imagens para o S3.
     *
     * @param id  O ID do produto a ser atualizado.
     * @param dto O DTO com os dados atualizados do produto.
     * @param files Uma lista de arquivos de imagem a serem enviados para o S3.
     * @return O objeto Produto atualizado.
     * @throws ResourceNotFoundException Se o produto não for encontrado.
     */
    @Transactional
    public Produto atualizarProduto(String id, ProdutoRequestDTO dto, List<MultipartFile> files) {
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

        AtomicInteger fileIndex = new AtomicInteger(0);

        if (dto.getVariacoes() != null && !dto.getVariacoes().isEmpty()) {
            dto.getVariacoes().forEach(variacaoDTO -> {
                VariacaoProduto variacao = new VariacaoProduto();
                variacao.setId(new ObjectId().toHexString());
                variacao.setCor(variacaoDTO.getCor());
                variacao.setTamanho(variacaoDTO.getTamanho());
                variacao.setPreco(variacaoDTO.getPreco());
                variacao.setEstoque(variacaoDTO.getEstoque());

                if (variacaoDTO.getImagens() != null && !variacaoDTO.getImagens().isEmpty()) {
                    variacaoDTO.getImagens().forEach(imagemDTO -> {
                        ImagemProduto imagem = new ImagemProduto();
                        // Lógica similar ao cadastro: se a URL estiver vazia, tenta upload
                        if ((imagemDTO.getUrl() == null || imagemDTO.getUrl().isEmpty()) && files != null && !files.isEmpty()) {
                            int currentFileIdx = fileIndex.getAndIncrement();
                            if (currentFileIdx < files.size()) {
                                try {
                                    String s3Url = s3Service.uploadFile(files.get(currentFileIdx));
                                    imagem.setUrl(s3Url);
                                    logger.info("Imagem {} upada para S3 durante atualização: {}", files.get(currentFileIdx).getOriginalFilename(), s3Url);
                                } catch (IOException e) {
                                    logger.error("Falha ao fazer upload da imagem para S3 durante atualização: {}", e.getMessage(), e);
                                    throw new RuntimeException("Falha ao fazer upload da imagem para S3 durante atualização: " + e.getMessage(), e);
                                }
                            } else {
                                logger.warn("Mais imagens no DTO do que arquivos fornecidos para atualização. Imagem {} não terá URL S3.", imagemDTO.getAltText());
                                imagem.setUrl(imagemDTO.getUrl()); // Mantém a URL original (pode ser vazia)
                            }
                        } else {
                            imagem.setUrl(imagemDTO.getUrl()); // Usa a URL já fornecida
                        }
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


    @Transactional
    public void deletarTodosProdutos() {
        logger.warn("Iniciando exclusão de TODOS os produtos. Esta operação é irreversível!");
        try {
            produtoRepository.deleteAll(); // Usa o método deleteAll() do MongoRepository
            logger.info("Todos os produtos foram deletados com sucesso.");
        } catch (Exception e) {
            logger.error("Erro ao deletar todos os produtos: {}", e.getMessage(), e);
            throw new RuntimeException("Falha ao deletar todos os produtos: " + e.getMessage(), e);
        }
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
