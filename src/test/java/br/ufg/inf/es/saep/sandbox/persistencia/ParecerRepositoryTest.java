package br.ufg.inf.es.saep.sandbox.persistencia;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class ParecerRepositoryTest {
    private static ParecerRepositoryManager prm = null;

    @BeforeClass
    public static void setUpClass() {
        prm = new ParecerRepositoryManager();
        prm.limparBancoDeDados();
    }

    @Test
    public void verificaSeParecerFoiInserido() {
        Parecer parecer = SaepTestUtil.getParecerInstance();
        Parecer parecerDeRetorno;

        prm.persisteParecer(parecer);
        parecerDeRetorno = prm.byId(parecer.getId());
        Assert.assertNotNull("Parecer não foi inserido ao banco de dados.", parecerDeRetorno);
    }

    @Test
    public void verificaSeNotaFoiInserida() {
        Parecer parecer = SaepTestUtil.getParecerInstance();
        Parecer parecerDeRetorno;
        int numeroDeNotasAntesDaAdicao;

        prm.persisteParecer(parecer);
        parecerDeRetorno = prm.byId(parecer.getId());
        Assert.assertNotNull("Parecer não foi inserido ao banco de dados.", parecerDeRetorno);

        numeroDeNotasAntesDaAdicao = parecerDeRetorno.getNotas().size();
        prm.adicionaNota(parecer.getId(), SaepTestUtil.getNotaInstance());
        parecerDeRetorno = prm.byId(parecer.getId());
        Assert.assertEquals("Valor esperado era [" + (numeroDeNotasAntesDaAdicao + 1) + "[ mas o valor obtido foi [" + parecerDeRetorno.getNotas().size() + "].", numeroDeNotasAntesDaAdicao + 1, parecerDeRetorno.getNotas().size());
    }

    @Test(expected = ParecerNaoEncontrado.class)
    public void verificaExcecaoAoTentarInserirNotaEmParecerNaoExistente() {
        prm.adicionaNota("lorem", SaepTestUtil.getNotaInstance());
    }

    @Test
    public void verificaSeNotaFoiRemovida() {
        Parecer parecer = SaepTestUtil.getParecerInstance();
        Parecer parecerDeRetorno;
        Nota nota;
        Avaliavel original;
        int numeroDeNotasAntesDaAdicao;
        int numeroDeNotasAposAdicao;

        prm.limparBancoDeDados();
        prm.persisteParecer(parecer);
        parecerDeRetorno = prm.byId(parecer.getId());
        Assert.assertNotNull("Parecer não foi inserido ao banco de dados.", parecerDeRetorno);

        numeroDeNotasAntesDaAdicao = parecerDeRetorno.getNotas().size();
        nota = SaepTestUtil.getNotaInstance();
        original = nota.getItemOriginal();
        prm.adicionaNota(parecer.getId(), nota);
        parecerDeRetorno = prm.byId(parecer.getId());
        Assert.assertEquals("Valor esperado era [" + (numeroDeNotasAntesDaAdicao + 1) + "[ mas o valor obtido foi [" + parecerDeRetorno.getNotas().size() + "].", numeroDeNotasAntesDaAdicao + 1, parecerDeRetorno.getNotas().size());

        numeroDeNotasAposAdicao = parecerDeRetorno.getNotas().size();
        prm.removeNota(parecerDeRetorno.getId(), original);
        parecerDeRetorno = prm.byId(parecer.getId());
        Assert.assertEquals("Valor esperado era [" + (numeroDeNotasAposAdicao - 1) + "] mas o valor obtido foi [" + parecerDeRetorno.getNotas().size() + "].", numeroDeNotasAposAdicao - 1, parecerDeRetorno.getNotas().size());
    }

    @Test(expected = ParecerNaoEncontrado.class)
    public void verificaExcecaoAoTentarRemoverNotaEmParecerNaoExistente() {
        prm.removeNota("lorem", SaepTestUtil.getPontuacaoInstance("ipsum"));
    }

    @Test
    public void verificaSeFundamentacaoFoiAlterada() {
        Parecer parecer = SaepTestUtil.getParecerInstance();
        Parecer parecerDeRetorno;

        prm.persisteParecer(parecer);
        parecerDeRetorno = prm.byId(parecer.getId());
        Assert.assertNotNull("Parecer não foi inserido ao banco de dados.", parecerDeRetorno);

        String fundamentacaoAnterior = parecerDeRetorno.getFundamentacao();
        prm.atualizaFundamentacao(parecerDeRetorno.getId(), "minha nova fundamentacao");
        parecerDeRetorno = prm.byId(parecerDeRetorno.getId());
        Assert.assertNotEquals("Os valores das fundamentações são iguais.", fundamentacaoAnterior, parecerDeRetorno.getFundamentacao());
    }

    @Test
    public void verificaSeParecerFoiRemovido() {
        Parecer parecer = SaepTestUtil.getParecerInstance();
        Parecer parecerDeRetorno;

        prm.persisteParecer(parecer);
        parecerDeRetorno = prm.byId(parecer.getId());
        Assert.assertNotNull("Parecer não foi inserido ao banco de dados.", parecerDeRetorno);

        prm.removeParecer(parecerDeRetorno.getId());
        parecerDeRetorno = prm.byId(parecerDeRetorno.getId());
        Assert.assertNull("Parecer não foi removido do banco de dados", parecerDeRetorno);
    }

    @Test
    public void verificaSeRadocFoiInserido() {
        Radoc radoc = SaepTestUtil.getRadocInstance();
        Radoc radocDeRetorno;

        prm.persisteRadoc(radoc);
        radocDeRetorno = prm.radocById(radoc.getId());
        Assert.assertNotNull("Radoc não foi inserido ao banco de dados.", radocDeRetorno);
    }

    @Test
    public void verificaSeRadocFoiRemovido() {
        Radoc radoc = SaepTestUtil.getRadocInstance();
        Radoc radocDeRetorno;

        prm.persisteRadoc(radoc);
        radocDeRetorno = prm.radocById(radoc.getId());
        Assert.assertNotNull("Radoc não foi inserido ao banco de dados.", radocDeRetorno);

        prm.removeRadoc(radocDeRetorno.getId());
        radocDeRetorno = prm.radocById(radocDeRetorno.getId());
        Assert.assertNull("Radoc não foi removido do banco de dados.", radocDeRetorno);
    }

    @Test(expected = RadocNaoEncontrado.class)
    public void verificaExcecaoAoTentarRemoverRadocInexistente() {
        prm.removeRadoc("lorem");
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaInsercaoDeNotaSemIdDoParecer() {
        prm.adicionaNota(null, SaepTestUtil.getNotaInstance());
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaInsercaoDeNotaSemObjetoNota() {
        prm.adicionaNota("lorem", null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRemocaoDeNotaSemIdDoParecer() {
        prm.removeNota(null, SaepTestUtil.getPontuacaoInstance("hey"));
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRemocaoDeNotaSemAvaliavel() {
        prm.removeNota("lorem", null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaInsercaoDeParecerSemObjetoParecer() {
        prm.persisteParecer(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaAtualizaFundamentacaoSemIdDoParecer() {
        prm.atualizaFundamentacao(null, "blabla");
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaAtualizaFundamentacaoSemFundamentacao() {
        prm.atualizaFundamentacao("lorem", null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRecuperaParecerSemId() {
        prm.byId(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRemocaoDeParecerSemId() {
        prm.removeParecer(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRemocaoDeRadocSemId() {
        prm.removeRadoc(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaPersisteRadocSemObjetoRadoc() {
        prm.persisteRadoc(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRemoveRadocSemId() {
        prm.removeRadoc(null);
    }


}
