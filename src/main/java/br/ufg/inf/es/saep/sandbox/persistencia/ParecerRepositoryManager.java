package br.ufg.inf.es.saep.sandbox.persistencia;

import br.ufg.inf.es.saep.sandbox.dominio.*;
import br.ufg.inf.es.saep.sandbox.util.AvaliavelTyperAdapter;
import br.ufg.inf.es.saep.sandbox.util.Constants;
import br.ufg.inf.es.saep.sandbox.util.MongoDBUtil;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.mongodb.*;
import static com.mongodb.client.model.Filters.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import org.bson.Document;
import org.bson.types.ObjectId;

import javax.print.Doc;
import java.util.ArrayList;
import java.util.List;

/**
 * Gerencia e coordenaações na parte do banco de dados
 * responsável pelo Parecer.
 *
 * Adicionalmente, também é responsável pela persistência
 * e manipulação dos dados referentes aos Radocs que estão
 * sendo avaliados durante o processo.
 */
public class ParecerRepositoryManager implements ParecerRepository {

    /**
     * Responsável pela conexão com o banco de dados.
     *
     * Atualmente apenas a base de dados 'saep' está sendo
     * utilizada na camada de persistência.
     *
     */
    private MongoClient mongoClient = null;

    /**
     * Trata-se de uma interface que possui a responsabilidade
     * de se conectar com uma base de dados específica no
     * MongoDB.
     */
    private MongoDatabase db = null;

    /**
     * Pertence à biblioteca Gson e possui a capacidade de
     * serializar e desserializar objetos Java em objetos JSON.
     *
     * É utilizado para reduzir o trabalho de manipulação de classes
     * e objetos Java, visto que o MongoDB utiliza documentos JSON
     * para persistir os dados no banco de dados.
     *
     */
    private Gson gson = null;

    /**
     * Cria gerenciador do repositório do parecer, inicializando o
     * cliente do MongoDB, a base de dados que será utilizada e construindo
     * o objeto Gson para a serialização e desserialização de dados.
     *
     * No caso do objeto gson, é também registrado um Typer Adapter para a
     * interface Avaliavel, visto que ela é implementada por mais de uma classe
     * e o Gson precisa se adaptar a cada uma delas.
     *
     */
    public ParecerRepositoryManager(){
        mongoClient = MongoDBUtil.getClientInstance();
        db = mongoClient.getDatabase(Constants.DB_NAME);
        GsonBuilder gsonBuilder = new GsonBuilder();

        /* Registrando um Typer Adapter, visto que a Interface Avaliavel é implementada por mais de uma classe
           Informações retiradas de: http://technology.finra.org/code/serialize-deserialize-interfaces-in-java.html */
        gsonBuilder.registerTypeAdapter(Avaliavel.class, new AvaliavelTyperAdapter());
        gson = gsonBuilder.create();
    }

    /**
     * Adiciona nota ao parecer. Caso a nota a ser acrescentada
     * se refira a um item {@link Avaliavel} para o qual já
     * exista uma nota, então a corrente substitui a anterior.
     *
     * @throws IdentificadorDesconhecido Caso o identificador
     * fornecido não identifique um parecer existente.
     *
     * @param id O identificador único do parecer.
     *
     * @param nota A alteração a ser acrescentada ao
     * pareder.
     */
    public void adicionaNota(String id, Nota nota) {
        if(id == null){
            throw new CampoExigidoNaoFornecido("id");
        }

        if(nota == null){
            throw new CampoExigidoNaoFornecido("nota");
        }

        if(byId(id) == null){
            throw new ParecerNaoEncontrado("Parecer de id[" + id + "] não foi encontrado.");
        }

        MongoCollection pareceresCollection = db.getCollection(Constants.DB_COLLECTION_PARECERES);
        MongoCollection notasCollection = db.getCollection(Constants.DB_COLLECTION_NOTAS);
        Document notaDocument = Document.parse(gson.toJson(nota));
        ObjectId objId;

        notaDocument.append("id_parecer", id);
        notasCollection.insertOne(notaDocument);
        objId = (ObjectId) notaDocument.get("_id");
        pareceresCollection.updateOne(
                new Document("_id", id),
                new Document("$push", new Document("notas", objId))
        );
    }

    /**
     * Remove a nota cujo item {@link Avaliavel} original é
     * fornedido.
     *
     * @param id O identificador único do parecer.
     * @param original Instância de {@link Avaliavel} que participa
     *                 da {@link Nota} a ser removida como origem.
     */
    public void removeNota(String id, Avaliavel original) {
        MongoCollection pareceresCollection = db.getCollection(Constants.DB_COLLECTION_PARECERES);MongoCollection notasCollection = db.getCollection(Constants.DB_COLLECTION_NOTAS);
        MongoCursor<Document> mCursorNotas = notasCollection.find(eq("id_parecer", id)).iterator();

        if(id == null){
            throw new CampoExigidoNaoFornecido("id");
        }

        if(original == null){
            throw new CampoExigidoNaoFornecido("original");
        }

        if(byId(id) == null){
            throw new ParecerNaoEncontrado("Parecer de id[" + id + "] não foi encontrado.");
        }

        /* Remover da coleção de notas */
        while(mCursorNotas.hasNext()){
            Document docNota = mCursorNotas.next();
            Document docNotaOriginal = (Document) docNota.get("original");
            Document docOriginal = Document.parse(gson.toJson(original));

            if(compareAvaliavel(docNotaOriginal, docOriginal)){
                ObjectId objId = (ObjectId) docNota.get("_id");
                notasCollection.deleteOne(new Document("_id", objId));

                /* Remove da lista de notas do parecer */
                Document match = new Document("_id", id);
                Document update = new Document("notas", objId);
                pareceresCollection.updateOne(match, new Document("$pull", update));
            }
        }

    }

    /**
     * Acrescenta o parecer ao repositório.
     *
     * @throws IdentificadorExistente Caso o
     * identificador seja empregado por parecer
     * existente (já persistido).
     *
     * @param parecer O parecer a ser persistido.
     *
     */
    public void persisteParecer(Parecer parecer) {
        if(parecer == null){
            throw new CampoExigidoNaoFornecido("parecer");
        }

        MongoCollection pareceresCollection = db.getCollection(Constants.DB_COLLECTION_PARECERES);
        Document docParecer = Document.parse(gson.toJson(parecer));

        docParecer.remove("id");
        docParecer.remove("notas");
        docParecer.put("_id", parecer.getId());
        pareceresCollection.insertOne(docParecer);

        /* Adicionando lista de notas */
        if(parecer.getNotas() == null) return;

        for(Nota nota : parecer.getNotas()){
            adicionaNota(parecer.getId(), nota);
        }
    }

    /**
     * Altera a fundamentação do parecer.
     *
     * <p>Fundamentação é o texto propriamente dito do
     * parecer. Não confunda com as alterações de
     * valores (dados de relatos ou de pontuações).
     *
     * <p>Após a chamada a esse método, o parecer alterado
     * pode ser recuperado pelo método {@link #byId(String)}.
     * Observe que uma instância disponível antes dessa chamada
     * torna-se "inválida".
     *
     * @throws IdentificadorDesconhecido Caso o identificador
     * fornecido não identifique um parecer.
     *
     * @param parecer O identificador único do parecer.
     * @param fundamentacao Novo texto da fundamentação do parecer.
     */
    public void atualizaFundamentacao(String parecer, String fundamentacao) {
        if(parecer == null){
            throw new CampoExigidoNaoFornecido("parecer");
        }

        if(fundamentacao == null){
            throw new CampoExigidoNaoFornecido("fundamentacao");
        }

        MongoCollection pareceresCollection = db.getCollection(Constants.DB_COLLECTION_PARECERES);
        MongoCursor<Document> mCursorPareceres = pareceresCollection.find(eq("_id", parecer)).iterator();

        if(mCursorPareceres.hasNext()){
            Document docParecer = mCursorPareceres.next();
            pareceresCollection.updateOne(new Document("_id", parecer),
                    new Document("$set", new Document("fundamentacao", fundamentacao)));
        }
        else {
            throw new ParecerNaoEncontrado("Parecer de id[" + parecer + "] não foi encontrado.");
        }
    }

    /**
     * Recupera o parecer pelo identificador.
     *
     * @param id O identificador do parecer.
     *
     * @return O parecer recuperado ou o valor {@code null},
     * caso o identificador não defina um parecer.
     */
    public Parecer byId(String id) {
        if(id == null){
            throw new CampoExigidoNaoFornecido("id");
        }

        MongoCollection pareceresCollection = db.getCollection(Constants.DB_COLLECTION_PARECERES);
        MongoCursor<Document> mCursorPareceres = pareceresCollection.find(eq("_id", id)).iterator();

        if(mCursorPareceres.hasNext()){
            Document docParecer = mCursorPareceres.next();
            MongoCollection notasCollection;
            MongoCursor<Document> mCursorNotas;
            List<Document> listDocNotas;
            Parecer parecer;

            docParecer.put("id", docParecer.get("_id"));
            docParecer.remove("notas");

            /* Recebe cada nota do parecer */
            notasCollection = db.getCollection(Constants.DB_COLLECTION_NOTAS);
            mCursorNotas = notasCollection.find(eq("id_parecer", id)).iterator();
            listDocNotas = new ArrayList<Document>();

            while(mCursorNotas.hasNext()){
                Document nota = mCursorNotas.next();

                listDocNotas.add(nota);
            }

            docParecer.put("notas", listDocNotas);
            parecer = gson.fromJson(docParecer.toJson(), Parecer.class);

            return parecer;
        }

        return null;
    }

    /**
     * Remove o parecer.
     *
     * <p>Se o identificador fornecido é inválido
     * ou não correspondente a um parecer existente,
     * nenhuma situação excepcional é gerada.</p>
     *
     * @param id O identificador único do parecer.
     */
    public void removeParecer(String id) {
        if(id == null){
            throw new CampoExigidoNaoFornecido("id");
        }

        MongoCollection notasCollection = db.getCollection(Constants.DB_COLLECTION_NOTAS);
        MongoCollection pareceresCollection = db.getCollection(Constants.DB_COLLECTION_PARECERES);

        if(byId(id) == null){
            throw new ParecerNaoEncontrado("Parecer de id[" + id + "] não foi encontrado.");
        }

        /* Deletar relatos associados ao parecer */
        notasCollection.deleteMany(new Document("id_parecer", id));

        /* Deletar parecer */
        pareceresCollection.deleteOne(new Document("_id", id));
    }

    /**
     * Recupera o RADOC identificado pelo argumento.
     *
     * @param identificador O identificador único do
     *                      RADOC.
     *
     * @return O {@code Radoc} correspondente ao
     * identificador fornecido.
     */
    public Radoc radocById(String identificador) {
        if(identificador == null){
            throw new CampoExigidoNaoFornecido("identificador");
        }

        MongoCollection radocsCollection = db.getCollection(Constants.DB_COLLECTION_RADOCS);
        MongoCursor<Document> mCursorRadocs = radocsCollection.find(eq("_id", identificador)).iterator();

        if(mCursorRadocs.hasNext()){
            Document docRadoc = mCursorRadocs.next();
            MongoCollection relatosCollection;
            MongoCursor<Document> mCursorNotas;
            List<Document> listRadocNotas;
            Radoc radoc;

            docRadoc.put("id", docRadoc.get("_id"));
            docRadoc.remove("relatos");

            // Recebe cada relato do radoc
            relatosCollection = db.getCollection(Constants.DB_COLLECTION_RELATOS);
            mCursorNotas = relatosCollection.find(eq("id_radoc", identificador)).iterator();
            listRadocNotas = new ArrayList<Document>();

            while(mCursorNotas.hasNext()){
                Document nota = mCursorNotas.next();
                listRadocNotas.add(nota);
            }

            docRadoc.put("relatos", listRadocNotas);
            radoc = gson.fromJson(docRadoc.toJson(), Radoc.class);

            return radoc;
        }

        return null;
    }

    /**
     * Conjunto de relatos de atividades e produtos
     * associados a um docente.
     *
     * <p>Um conjunto de relatos é extraído de fonte
     * externa de informação. Uma cópia é mantida pelo
     * SAEP para consistência de pareceres efetuados ao
     * longo do tempo. Convém ressaltar que informações
     * desses relatórios podem ser alteradas continuamente.
     *
     * @throws IdentificadorExistente Caso o identificador
     * do objeto a ser persistido seja empregado por
     * RADOC existente.
     *
     * @param radoc O conjunto de relatos a ser persistido.
     *
     * @return O identificador único do RADOC.
     */
    public String persisteRadoc(Radoc radoc) {
        if(radoc == null){
            throw new CampoExigidoNaoFornecido("radoc");
        }

        MongoCollection relatosCollection = db.getCollection(Constants.DB_COLLECTION_RELATOS);
        MongoCollection radocsCollection = db.getCollection(Constants.DB_COLLECTION_RADOCS);

        Document docRadoc = Document.parse(gson.toJson(radoc));
        docRadoc.remove("relatos");
        docRadoc.remove("id");
        docRadoc.put("_id", radoc.getId());

        /* Salvar Relatos */
        List<Relato> relatos = radoc.getRelatos();
        List<ObjectId> relatosObjId = new ArrayList<ObjectId>();

        /* Inserindo relatos na collections Relatos */
        for( Relato r : relatos ){
            Document docRelato = Document.parse(gson.toJson(r));
            ObjectId objIdRelato;

            docRelato.put("id_radoc", docRadoc.get("_id"));
            relatosCollection.insertOne(docRelato);
            objIdRelato = docRelato.getObjectId("_id");

            /* Inserindo ids dos relatos no ArrayList */
            relatosObjId.add(objIdRelato);
        }

        docRadoc.put("relatos", relatosObjId);

        /* Inserindo radoc na collections Radocs */
        radocsCollection.insertOne(docRadoc);

        return radoc.getId();
    }

    /**
     * Remove o RADOC.
     *
     * <p>Após essa operação o RADOC correspondente não
     * estará disponível para consulta.
     *
     * <p>Não é permitida a remoção de um RADOC para o qual
     * há pelo menos um parecer referenciando-o.
     *
     * @throws ExisteParecerReferenciandoRadoc Caso exista pelo
     * menos um parecer que faz referência para o RADOC cuja
     * remoção foi requisitada.
     *
     * @param identificador O identificador do RADOC.
     */
    public void removeRadoc(String identificador) {
        if(identificador == null){
            throw new CampoExigidoNaoFornecido("identificador");
        }

        MongoCollection relatosCollection = db.getCollection(Constants.DB_COLLECTION_RELATOS);
        MongoCollection radocsCollection = db.getCollection(Constants.DB_COLLECTION_RADOCS);

        if(radocById(identificador) == null){
            throw new RadocNaoEncontrado("Radoc de id[" + identificador + "] não foi encontrado.");
        }

        /* Deletar relatos associados ao Radoc */
        relatosCollection.deleteMany(new Document("id_radoc", identificador));

        /* Deletar radoc */
        radocsCollection.deleteOne(new Document("_id", identificador));

    }

    /**
     * Realiza a comparação entre dois objetos {@link Document} e retorna
     * se eles são iguais ou não.
     *
     * @param docNotaOriginal O objeto {@code Document} original da primeira nota.
     * @param docOriginal O objeto {@code Document} original da segunda nota.
     * @return O valor {@code true} caso ambos {@code Document} forem iguais ou
     * {@code false} caso sejam diferentes.
     */
    private boolean compareAvaliavel( Document docNotaOriginal, Document docOriginal ){

        /* Verifica se relato ou pontuação */
        if(docNotaOriginal.containsKey("tipo")){
            Relato notaOriginal = gson.fromJson(docNotaOriginal.toJson(), Relato.class);
            Relato original = gson.fromJson(docOriginal.toJson(), Relato.class);

            return notaOriginal.equals(original);
        } else {
            Pontuacao notaOriginal = gson.fromJson(docNotaOriginal.toJson(), Pontuacao.class);
            Pontuacao original = gson.fromJson(docOriginal.toJson(), Pontuacao.class);

            if(notaOriginal.getAtributo().equals(original.getAtributo())
                    && equalValorAvaliavel(notaOriginal.getValor(), original.getValor()))
                return true;
        }

        return false;
    }

    /**
     * Realiza a comparação entre dois objetos {@link Valor} e retorna
     * se eles são iguais ou não.
     *
     * @param notaOriginal O objeto {@code Valor} original da primeira nota.
     * @param original O objeto {@code Valor} original da segunda nota.
     * @return O valor {@code true} caso ambos {@code Valor} forem iguais ou
     * {@code false} caso sejam diferentes.
     */
    private boolean equalValorAvaliavel( Valor notaOriginal, Valor original ){
        boolean equalValor = true;

        if( notaOriginal.getString()!= null && original.getString() != null){
            if(!notaOriginal.getString().equals(original.getString())) {
                equalValor = false;
            }
        }

        if((notaOriginal.getBoolean() == original.getBoolean())
                && notaOriginal.getFloat() == original.getFloat()) {
            equalValor = true;
        } else{
            equalValor = false;
        }

        return equalValor;
    }

    /**
     * Deleta completamente o banco de dados.
     */
    public void limparBancoDeDados(){
        db.drop();
    }

}
