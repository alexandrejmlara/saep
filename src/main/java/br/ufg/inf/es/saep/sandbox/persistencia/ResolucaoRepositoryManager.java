package br.ufg.inf.es.saep.sandbox.persistencia;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import br.ufg.inf.es.saep.sandbox.util.AvaliavelTyperAdapter;
import br.ufg.inf.es.saep.sandbox.util.Constants;
import br.ufg.inf.es.saep.sandbox.util.MongoDBUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;

/**
 * Operações para oferecer a noção de coleções
 * de resoluções em memória.
 * <p>
 * <p>Uma resolução é formada por um conjunto de regras.
 * Está além do escopo do SAEP a edição de resoluções.
 * Dessa forma, a persistência não inclui atualização,
 * mas apenas consulta, acréscimo e remoção.
 * <p>
 * <p>Dada a sensibilidade, os raros usuários autorizados
 * e a frequência, a edição pode ser realizada por pessoal
 * técnico que produzirá uma instância de {@link Resolucao} a
 * ser recebida pelo presente repositório.
 * <p>
 * <p>Não existe opção para atualizar uma {@link Resolucao}.
 * Um parecer disponível, se tem a resolução correspondente
 * alterada, pode dar origem a um resultado distinto.
 * Em consequência, não existe opção para atualização de
 * {@link Resolucao}.
 *
 * @see Resolucao
 */
public class ResolucaoRepositoryManager implements ResolucaoRepository {
    private MongoClient mongoClient = null;
    private MongoDatabase db = null;
    private Gson gson = null;

    /**
     * Inicializa o banco de dados e o objeto Gson
     */
    public ResolucaoRepositoryManager() {
        mongoClient = MongoDBUtil.getClientInstance();
        db = mongoClient.getDatabase(Constants.DB_NAME);
        GsonBuilder gsonBuilder = new GsonBuilder();

        /* Registrando um Typer Adapter, visto que a Interface Avaliavel é implementada por mais de uma classe
           Informações retiradas de: http://technology.finra.org/code/serialize-deserialize-interfaces-in-java.html */
        gsonBuilder.registerTypeAdapter(Avaliavel.class, new AvaliavelTyperAdapter());
        gson = gsonBuilder.create();
    }

    /**
     * Recupera a instância de {@code Resolucao} correspondente
     * ao identificador.
     *
     * @param id O identificador único da resolução a
     *           ser recuperada.
     * @return {@code Resolucao} identificada por {@code id}.
     * O retorno {@code null} indica que não existe resolução
     * com o identificador fornecido.
     * @see #persiste(Resolucao)
     */
    public Resolucao byId(String id) {

        if (id == null) {
            throw new CampoExigidoNaoFornecido("id");
        }

        MongoCollection resolucoesCollection = db.getCollection(Constants.DB_COLLECTION_RESOLUCOES);
        MongoCursor<Document> mCursorResolucoes = resolucoesCollection.find(eq("_id", id)).iterator();

        if (mCursorResolucoes.hasNext()) {
            Document docResolucao = mCursorResolucoes.next();
            Resolucao resolucao;

            docResolucao.put("id", docResolucao.get("_id"));
            resolucao = gson.fromJson(docResolucao.toJson(), Resolucao.class);

            return resolucao;
        }

        return null;
    }

    /**
     * Persiste uma resolução.
     *
     * @param resolucao A resolução a ser persistida.
     * @return O identificador único da resolução, conforme
     * fornecido em propriedade do objeto fornecido. Observe que
     * o método retorna {@code null} para indicar que a
     * operação não foi realizada de forma satisfatória,
     * possivelmente por já existir resolução com
     * identificador semelhante.
     * @throws CampoExigidoNaoFornecido Caso o identificador não
     *                                  seja fornecido.
     * @throws IdentificadorExistente   Caso uma resolução com identificador
     *                                  igual àquele fornecido já exista.
     * @see #byId(String)
     * @see #remove(String)
     */
    public String persiste(Resolucao resolucao) {
        if (resolucao == null) {
            throw new CampoExigidoNaoFornecido("resolucao");
        }

        MongoCollection resolucoesCollection = db.getCollection(Constants.DB_COLLECTION_RESOLUCOES);
        Document docResolucao = Document.parse(gson.toJson(resolucao));

        docResolucao.put("_id", resolucao.getId());
        docResolucao.remove("id");
        resolucoesCollection.insertOne(docResolucao);

        return resolucao.getId();
    }

    /**
     * Remove a resolução com o identificador
     * fornecido.
     *
     * @param identificador O identificador único da
     *                      resolução a ser removida.
     * @return O valor {@code true} se a operação foi
     * executada de forma satisfatória e {@code false},
     * caso contrário.
     * @see #persiste(Resolucao)
     */
    public boolean remove(String identificador) {
        if (identificador == null) {
            throw new CampoExigidoNaoFornecido("identificador");
        }

        MongoCollection resolucoesCollection = db.getCollection(Constants.DB_COLLECTION_RESOLUCOES);
        DeleteResult deleteResult = resolucoesCollection.deleteOne(new Document("_id", identificador));

        if (deleteResult.getDeletedCount() > 0) {
            return true;
        }

        return false;
    }

    /**
     * Recupera a lista dos identificadores das
     * resoluções disponíveis.
     *
     * @return Identificadores das resoluções disponíveis.
     */
    public List<String> resolucoes() {
        MongoCollection resolucoesCollection = db.getCollection(Constants.DB_COLLECTION_RESOLUCOES);
        MongoCursor<Document> mCursorResolucoes = resolucoesCollection.find().iterator();
        List<String> resolucoes = new ArrayList<String>();

        while (mCursorResolucoes.hasNext()) {
            String resolucaoID = mCursorResolucoes.next().getString("_id");

            resolucoes.add(resolucaoID);
        }

        return resolucoes;
    }

    /**
     * Persiste o tipo fornecido.
     *
     * @param tipo O objeto a ser persistido.
     * @throws IdentificadorExistente Caso o tipo já
     *                                esteja persistido no repositório.
     */
    public void persisteTipo(Tipo tipo) {
        if (tipo == null) {
            throw new CampoExigidoNaoFornecido("tipo");
        }
        MongoCollection tiposCollection = db.getCollection(Constants.DB_COLLECTION_TIPOS);
        Document docTipo = Document.parse(gson.toJson(tipo));

        docTipo.remove("id");
        docTipo.put("_id", tipo.getId());
        tiposCollection.insertOne(docTipo);
    }

    /**
     * Remove o tipo.
     *
     * @param codigo O identificador do tipo a
     *               ser removido.
     * @throws ResolucaoUsaTipoException O tipo
     *                                   é empregado por pelo menos uma resolução.
     */
    public void removeTipo(String codigo) {
        if (codigo == null) {
            throw new CampoExigidoNaoFornecido("codigo");
        }

        MongoCollection tiposCollection = db.getCollection(Constants.DB_COLLECTION_TIPOS);

        tiposCollection.deleteOne(new Document("_id", codigo));
    }

    /**
     * Recupera o tipo com o código fornecido.
     *
     * @param codigo O código único do tipo.
     * @return A instância de {@link Tipo} cujo
     * código único é fornecido. Retorna {@code null}
     * caso não exista tipo com o código indicado.
     */
    public Tipo tipoPeloCodigo(String codigo) {
        if (codigo == null) {
            throw new CampoExigidoNaoFornecido("codigo");
        }

        MongoCollection tiposCollection = db.getCollection(Constants.DB_COLLECTION_TIPOS);
        MongoCursor<Document> mCursorTipos = tiposCollection.find(eq("_id", codigo)).iterator();

        if (mCursorTipos.hasNext()) {
            Document docTipo = mCursorTipos.next();
            Tipo tipo;

            docTipo.put("id", docTipo.get("_id"));
            tipo = gson.fromJson(docTipo.toJson(), Tipo.class);

            return tipo;
        }

        return null;
    }

    /**
     * Recupera a lista de tipos cujos nomes
     * são similares àquele fornecido. Um nome é
     * similar àquele do tipo caso contenha o
     * argumento fornecido. Por exemplo, para o nome
     * "casa" temos que "asa" é similar.
     * <p>
     * Um nome é dito similar se contém a sequência
     * indicada.
     *
     * @param nome Sequência que será empregada para
     *             localizar tipos por nome.
     * @return A coleção de tipos cujos nomes satisfazem
     * um padrão de semelhança com a sequência indicada.
     */
    public List<Tipo> tiposPeloNome(String nome) {
        if (nome == null) {
            throw new CampoExigidoNaoFornecido("nome");
        }

        MongoCollection tiposCollection = db.getCollection(Constants.DB_COLLECTION_TIPOS);
        MongoCursor<Document> mCursorTipos = tiposCollection.find().iterator();
        List<Tipo> listTipos = new ArrayList<Tipo>();

        while (mCursorTipos.hasNext()) {
            Tipo tipo = gson.fromJson(mCursorTipos.next().toJson(), Tipo.class);

            listTipos.add(tipo);
        }

        return listTipos;
    }

    /**
     * Deleta completamente o banco de dados.
     */
    public void limparBancoDeDados() {
        db.drop();
    }
}
