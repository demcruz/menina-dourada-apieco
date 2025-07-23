package br.com.ecommerce.meninadourada.controller;


import br.com.ecommerce.meninadourada.dto.ProdutoRequestDTO;
import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException;
import br.com.ecommerce.meninadourada.model.Produto;
import br.com.ecommerce.meninadourada.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // Importe para MultipartFile
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper; // IMPORTANTE: Certifique-se de que esta importação está presente e correta!

@RestController
@RequestMapping("/api/produtos")
public class ProdutoController {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoController.class);

    private final ProdutoService produtoService;
    private final ObjectMapper objectMapper; // Declaração da variável ObjectMapper

    /**
     * Construtor que injeta a instância de ProdutoService e ObjectMapper.
     * O Spring irá injetar automaticamente uma instância de ObjectMapper
     * se a dependência 'spring-boot-starter-web' estiver no pom.xml.
     * @param produtoService A instância do serviço de Produto.
     * @param objectMapper O ObjectMapper para lidar com JSON.
     */
    @Autowired
    public ProdutoController(ProdutoService produtoService, ObjectMapper objectMapper) {
        this.produtoService = produtoService;
        this.objectMapper = objectMapper; // Atribuição da instância injetada
    }

    /**
     * Endpoint HTTP POST para cadastrar um novo produto, incluindo upload de imagens para o S3.
     * Recebe o JSON do produto como uma parte da requisição e os arquivos de imagem como outra parte.
     *
     * @param productDataJson O JSON do ProdutoRequestDTO como uma string.
     * @param files Uma lista de arquivos de imagem (opcional).
     * @return ResponseEntity com o Produto criado e status HTTP 201 (Created).
     */
    @PostMapping("/insert") // REMOVIDO: consumes = {"multipart/form-data"} - Implícito pelo MultipartFile
    public ResponseEntity<Produto> cadastrarProduto(
            @RequestParam("productData") String productDataJson, // ALTERADO: Usando @RequestParam para o JSON
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            // Deserializa o JSON string para ProdutoRequestDTO
            ProdutoRequestDTO produtoRequestDTO = objectMapper.readValue(productDataJson, ProdutoRequestDTO.class);
            logger.info("Recebida requisição para cadastrar novo produto: {}", produtoRequestDTO.getNome());

            // Chama o serviço para cadastrar o produto, passando os arquivos
            Produto novoProduto = produtoService.cadastrarProdutoComVariacoes(produtoRequestDTO, files);
            logger.info("Produto cadastrado com sucesso. ID: {}", novoProduto.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
        } catch (Exception e) {
            logger.error("Erro ao cadastrar produto com upload de imagem: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint HTTP POST para cadastrar múltiplos produtos em lote.
     * NOTA: Este endpoint não suporta upload de imagens diretamente no lote com esta abordagem.
     * As URLs das imagens devem ser pré-existentes no S3 e incluídas no JSON.
     *
     * @param produtoRequestDTOs Uma lista de DTOs contendo os dados dos produtos a serem cadastrados.
     * @return ResponseEntity com a lista de Produtos criados e status HTTP 201 (Created).
     */
    @PostMapping("/batch-insert") // Mantém o comportamento original (JSON puro, URLs pré-existentes)
    public ResponseEntity<List<Produto>> cadastrarProdutosEmLote(@Valid @RequestBody List<ProdutoRequestDTO> produtoRequestDTOs) {
        logger.info("Recebida requisição para cadastrar {} produtos em lote.", produtoRequestDTOs.size());
        List<Produto> novosProdutos = produtoService.cadastrarProdutosEmLote(produtoRequestDTOs);
        logger.info("{} produtos cadastrados em lote com sucesso.", novosProdutos.size());
        return ResponseEntity.status(HttpStatus.CREATED).body(novosProdutos);
    }

    /**
     * Endpoint HTTP GET para listar todos os produtos com suporte a paginação.
     *
     * @param page O número da página (começa em 0).
     * @param size O número de itens por página (padrão 6).
     * @return ResponseEntity com uma página de Produtos e status HTTP 200 (OK).
     */
    @GetMapping("/all")
    public ResponseEntity<Page<Produto>> listarTodosProdutosPaginado(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        logger.info("Recebida requisição para listar produtos (página: {}, tamanho: {}).", page, size);
        Page<Produto> produtosPaginados = produtoService.listarTodosProdutosPaginado(page, size);
        logger.info("Retornando {} produtos na página {} de {}.", produtosPaginados.getNumberOfElements(), produtosPaginados.getNumber(), produtosPaginados.getTotalPages());
        return ResponseEntity.ok(produtosPaginados);
    }

    /**
     * Endpoint HTTP GET para buscar um produto por ID.
     *
     * @param id O ID do produto a ser buscado.
     * @return ResponseEntity com o Produto encontrado e status HTTP 200 (OK).
     * @throws ResourceNotFoundException Se o produto com o ID especificado não for encontrado.
     */
    @GetMapping("/findById/{id}")
    public ResponseEntity<Produto> buscarProdutoPorId(@PathVariable String id) {
        logger.info("Recebida requisição para buscar produto com ID: {}", id);
        Produto produto = produtoService.buscarProdutoPorId(id);
        logger.info("Produto encontrado com sucesso. ID: {}", produto.getId());
        return ResponseEntity.ok(produto);
    }

    /**
     * Endpoint HTTP PUT para atualizar um produto existente, incluindo upload de imagens para o S3.
     * Recebe o JSON do produto como uma parte da requisição e os arquivos de imagem como outra parte.
     *
     * @param id O ID do produto a ser atualizado.
     * @param productDataJson O JSON do ProdutoRequestDTO como uma string.
     * @param files Uma lista de arquivos de imagem (opcional).
     * @return ResponseEntity com o Produto atualizado e status HTTP 200 (OK).
     */
    @PutMapping(value = "/update/{id}", consumes = {"multipart/form-data"})
    public ResponseEntity<Produto> atualizarProduto(
            @PathVariable String id,
            @RequestPart("productData") String productDataJson,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        try {
            ProdutoRequestDTO produtoRequestDTO = objectMapper.readValue(productDataJson, ProdutoRequestDTO.class);
            logger.info("Recebida requisição para atualizar produto com ID: {}", id);

            Produto produtoAtualizado = produtoService.atualizarProduto(id, produtoRequestDTO, files);
            logger.info("Produto atualizado com sucesso. ID: {}", produtoAtualizado.getId());
            return ResponseEntity.ok(produtoAtualizado);
        } catch (Exception e) {
            logger.error("Erro ao atualizar produto com upload de imagem: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint HTTP DELETE para deletar um produto.
     *
     * @param id O ID do produto a ser deletado.
     * @return ResponseEntity com status HTTP 204 (No Content) se a exclusão for bem-sucedida.
     * @throws ResourceNotFoundException Se o produto com o ID especificado não for encontrado.
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletarProduto(@PathVariable String id) {
        logger.info("Recebida requisição para deletar produto com ID: {}", id);
        produtoService.deletarProduto(id);
        logger.info("Produto deletado com sucesso. ID: {}", id);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/delete-all") // Novo endpoint para deletar todos
    public ResponseEntity<Void> deletarTodosProdutos() {
        logger.warn("Recebida requisição para DELETAR TODOS OS PRODUTOS. Esta operação é irreversível!");
        produtoService.deletarTodosProdutos(); // Chamada ao novo método no service
        logger.info("Todos os produtos foram deletados com sucesso.");
        return ResponseEntity.noContent().build();
    }

    /**
     * Handler de exceção para ResourceNotFoundException.
     * Retorna status HTTP 404 (Not Found) quando um recurso não é encontrado.
     * @param ex A exceção ResourceNotFoundException.
     * @return ResponseEntity com a mensagem de erro e status HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<String> handleResourceNotFoundException(ResourceNotFoundException ex) {
        logger.warn("Recurso não encontrado: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
