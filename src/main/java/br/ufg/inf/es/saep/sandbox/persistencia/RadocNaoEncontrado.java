package br.ufg.inf.es.saep.sandbox.persistencia;

/**
 * Indica que o radoc não foi encontrado ao realizar a operação.
 */
public class RadocNaoEncontrado extends RuntimeException {

    /**
     * Informa uma mensagem contendo detalhes sobre o que gerou
     * a exceção.
     *
     * @param mensagem Contém informações sobre o motivo da exceção.
     */
    public RadocNaoEncontrado(String mensagem) {
        super(mensagem);
    }
}
