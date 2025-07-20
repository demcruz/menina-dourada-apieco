package br.com.ecommerce.meninadourada.controller;


import br.com.ecommerce.meninadourada.dto.ProdutoRequestDTO;
import br.com.ecommerce.meninadourada.exception.ResourceNotFoundException;
import br.com.ecommerce.meninadourada.model.Produto;
import br.com.ecommerce.meninadourada.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page; // Importe para paginação
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List; // Importe para listas

@RestController
@RequestMapping("/api/produtos") // Define o caminho base para todos os endpoints neste controller
@CrossOrigin(origins = "http://localhost:3000")
public class ProdutoController {

    private static final Logger logger = LoggerFactory.getLogger(ProdutoController.class);

    private final ProdutoService produtoService;

    /**
     * Construtor que injeta a instância de ProdutoService.
     * @param produtoService A instância do serviço de Produto.
     */
    @Autowired
    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    /**
     * Endpoint HTTP POST para cadastrar um novo produto.
     * A URL agora inclui "/insert" explicitamente.
     *
     * @param produtoRequestDTO O DTO contendo os dados do produto a ser cadastrado.
     * @return ResponseEntity com o Produto criado e status HTTP 201 (Created).
     */
    @PostMapping("/insert") // Alterado para incluir "insert"
    public ResponseEntity<Produto> cadastrarProduto(@Valid @RequestBody ProdutoRequestDTO produtoRequestDTO) {
        logger.info("Recebida requisição para cadastrar novo produto: {}", produtoRequestDTO.getNome());
        Produto novoProduto = produtoService.cadastrarProdutoComVariacoes(produtoRequestDTO);
        logger.info("Produto cadastrado com sucesso. ID: {}", novoProduto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(novoProduto);
    }

    /**
     * Endpoint HTTP POST para cadastrar múltiplos produtos em lote.
     * Recebe uma lista de ProdutoRequestDTOs no corpo da requisição.
     *
     * @param produtoRequestDTOs Uma lista de DTOs contendo os dados dos produtos a serem cadastrados.
     * @return ResponseEntity com a lista de Produtos criados e status HTTP 201 (Created).
     */
    @PostMapping("/batch-insert") // Novo endpoint para inserção em lote
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
    @GetMapping("/all") // Novo endpoint para listar todos com paginação
    public ResponseEntity<Page<Produto>> listarTodosProdutosPaginado(
            @RequestParam(defaultValue = "0") int page, // Parâmetro de query para o número da página
            @RequestParam(defaultValue = "6") int size) { // Parâmetro de query para o tamanho da página
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
    @GetMapping("/findById/{id}") // Alterado para incluir "findById"
    public ResponseEntity<Produto> buscarProdutoPorId(@PathVariable String id) {
        logger.info("Recebida requisição para buscar produto com ID: {}", id);
        Produto produto = produtoService.buscarProdutoPorId(id);
        logger.info("Produto encontrado com sucesso. ID: {}", produto.getId());
        return ResponseEntity.ok(produto);
    }


    @PutMapping("/update/{id}") // Alterado para incluir "update"
    public ResponseEntity<Produto> atualizarProduto(@PathVariable String id, @Valid @RequestBody ProdutoRequestDTO produtoRequestDTO) {
        logger.info("Recebida requisição para atualizar produto com ID: {}", id);
        Produto produtoAtualizado = produtoService.atualizarProduto(id, produtoRequestDTO);
        logger.info("Produto atualizado com sucesso. ID: {}", produtoAtualizado.getId());
        return ResponseEntity.ok(produtoAtualizado);
    }

    /**
     * Endpoint HTTP DELETE para deletar um produto.
     * A URL agora inclui "/delete/{id}" explicitamente.
     *
     * @param id O ID do produto a ser deletado.
     * @return ResponseEntity com status HTTP 204 (No Content) se a exclusão for bem-sucedida.
     * @throws ResourceNotFoundException Se o produto com o ID especificado não for encontrado.
     */
    @DeleteMapping("/delete/{id}") // Alterado para incluir "delete"
    public ResponseEntity<Void> deletarProduto(@PathVariable String id) {
        logger.info("Recebida requisição para deletar produto com ID: {}", id);
        produtoService.deletarProduto(id);
        logger.info("Produto deletado com sucesso. ID: {}", id);
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


