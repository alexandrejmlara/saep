package br.ufg.inf.es.saep.sandbox.util;

import br.ufg.inf.es.saep.sandbox.dominio.Avaliavel;
import br.ufg.inf.es.saep.sandbox.dominio.Pontuacao;
import br.ufg.inf.es.saep.sandbox.dominio.Relato;
import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Typer Adapter utilizado pelo Gson no processo de serialização
 * e desserialização dos objetos.
 *
 * É necessário, pois a interface Avaliavel é implementada por mais
 * de uma classe e o Gson precisa saber como lidar com cada classe que
 * ela implemente.
 *
 */
public class AvaliavelTyperAdapter implements JsonSerializer<Avaliavel>, JsonDeserializer<Avaliavel> {

    /**
     * Invoca este método call-back durante a desserialização quando
     * é encontrado um campo com o tipo especificado.
     *
     * @param jsonElement O dado Json sendo desserializado
     * @param type O tipo do objeto que é para ser desserializado
     * @param jsonDeserializationContext O contexto da desserialização
     * @return Um objeto desserializado do tipo especificado.
     * @throws JsonParseException Caso o json não estiver no formato especificado pelo tipo.
     */

    public Avaliavel deserialize(JsonElement jsonElement, Type type,
                                 JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        if(jsonObject.has("tipo")) {
            Type relatoType = Relato.class;

            return jsonDeserializationContext.deserialize(jsonObject, relatoType);
        } else {
            Type pontuacaoType = Pontuacao.class;

            return jsonDeserializationContext.deserialize(jsonObject, pontuacaoType);
        }
    }

    /**
     * Invoca este método call-back durante a serialização quando
     * é encontrado um campo com o tipo especificado.
     *
     * @param jsonElement O objeto que precisa ser convertido para Json
     * @param type O tipo do objeto a ser serializado
     * @param jsonSerializationContext O contexto da serialização
     * @return Um {@code JsonElement} correspondente ao objeto especificado.
     */

    public JsonElement serialize(Avaliavel jsonElement, Type type, JsonSerializationContext jsonSerializationContext) {
        JsonElement element = jsonSerializationContext.serialize(jsonElement, jsonElement.getClass());

        return element;
    }
}
