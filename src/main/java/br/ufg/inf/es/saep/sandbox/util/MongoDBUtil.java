package br.ufg.inf.es.saep.sandbox.util;

import com.mongodb.DB;
import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

/**
 * Responsável pelo gerenciamento de conexões com o banco de dados.
 */
public class MongoDBUtil {

    /**
     * Recupera uma instância de conexão com o banco de dados especificando
     * endereço e porta
     *
     * @param servidor Endereço do servidor onde será realizada a conexão
     * @param porta Porta do servidor onde será realizada a conexão
     * @return Um novo objeto {@code MongoClient} que mantém a conexão com
     * o banco de dados
     */
    public static MongoClient getClientInstance(String servidor, int porta){
        return new MongoClient(new ServerAddress(servidor, porta));
    }

    /**
     * Recupera uma instância de conexão com o banco de dados local
     *
     * @return Um novo objeto {@code MongoClient} que mantém a conexão com
     * o banco de dados
     */
    public static MongoClient getClientInstance(){
        return new MongoClient();
    }



}
