package br.ufg.inf.es.saep.sandbox.persistencia;

/**
 * Indica que o parecer não foi encontrado ao realizar a operação.
 */
public class ParecerNaoEncontrado extends RuntimeException {

    /**
     * Informa uma mensagem contendo detalhes sobre o que gerou
     * a exceção.
     *
     * @param mensagem Contém informações sobre o motivo da exceção.
     */
    public ParecerNaoEncontrado(String mensagem) {
        super(mensagem);
    }
}
