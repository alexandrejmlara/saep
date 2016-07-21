package br.ufg.inf.es.saep.sandbox.persistencia;

import br.ufg.inf.es.saep.sandbox.dominio.CampoExigidoNaoFornecido;
import br.ufg.inf.es.saep.sandbox.dominio.Resolucao;
import br.ufg.inf.es.saep.sandbox.dominio.Tipo;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created by Alexandre Lara on 17/07/2016.
 */
public class ResolucaoRepositoryTest {
    private static ResolucaoRepositoryManager rrm = null;

    @BeforeClass
    public static void setUpClass() {
        rrm = new ResolucaoRepositoryManager();
        rrm.limparBancoDeDados();
    }

    @Test
    public void verificaSeResolucaoFoiInserida() {
        Resolucao resolucao = SaepTestUtil.getResolucaoInstance();
        Resolucao resolucaoDeRetorno;

        rrm.persiste(resolucao);
        resolucaoDeRetorno = rrm.byId(resolucao.getId());
        Assert.assertNotNull("Resolução não foi inserida ao banco de dados.", resolucaoDeRetorno);
    }

    @Test
    public void verificaSeResolucaoFoiRemovida() {
        Resolucao resolucao = SaepTestUtil.getResolucaoInstance();
        Resolucao resolucaoDeRetorno;

        rrm.limparBancoDeDados();
        rrm.persiste(resolucao);
        resolucaoDeRetorno = rrm.byId(resolucao.getId());
        Assert.assertNotNull("Resolução não foi inserida ao banco de dados.", resolucaoDeRetorno);

        rrm.remove(resolucaoDeRetorno.getId());
        resolucaoDeRetorno = rrm.byId(resolucaoDeRetorno.getId());
        Assert.assertNull("Parecer não foi removido do banco de dados", resolucaoDeRetorno);
    }

    @Test
    public void verificaSeTipoFoiInserido() {
        Tipo tipo = SaepTestUtil.getTipoInstance();
        Tipo tipoRetorno;

        rrm.persisteTipo(tipo);
        tipoRetorno = rrm.tipoPeloCodigo(tipo.getId());
        Assert.assertNotNull("Tipo não foi inserido ao banco de dados.", tipoRetorno);
    }

    @Test
    public void verificaSeVariosTiposForamInseridos() {
        rrm.limparBancoDeDados();

        for (int i = 0; i < 3; i++) {
            Tipo tipo = SaepTestUtil.getTipoInstance("apple");
            rrm.persisteTipo(tipo);
        }

        List<Tipo> tipos = rrm.tiposPeloNome("apple");
        Assert.assertTrue("Tipo não foi inserido ao banco de dados.", (tipos.size() == 3));
    }

    @Test
    public void verificaSeTipoFoiRemovido() {
        Tipo tipo = SaepTestUtil.getTipoInstance();
        Tipo tipoDeRetorno;

        rrm.persisteTipo(tipo);
        tipoDeRetorno = rrm.tipoPeloCodigo(tipo.getId());
        Assert.assertNotNull("Tipo não foi inserido ao banco de dados.", tipoDeRetorno);

        rrm.removeTipo(tipoDeRetorno.getId());
        tipoDeRetorno = rrm.tipoPeloCodigo(tipoDeRetorno.getId());
        Assert.assertNull("Tipo não foi removido do banco de dados.", tipoDeRetorno);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRecuperaResolucaoSemId() {
        rrm.byId(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaPersisteResolucaoSemObjetoResolucao() {
        rrm.persiste(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRemoveResolucaoSemId() {
        rrm.remove(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaPersisteTipoSemObjetoTipo() {
        rrm.persisteTipo(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRemoveTipoSemId() {
        rrm.removeTipo(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRecuperaTipoPeloCodigoSemCodigo() {
        rrm.tipoPeloCodigo(null);
    }

    @Test(expected = CampoExigidoNaoFornecido.class)
    public void verificaRecuperaTipoPeloNomeSemNome() {
        rrm.tiposPeloNome(null);
    }


}
