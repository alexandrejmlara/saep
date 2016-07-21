package br.ufg.inf.es.saep.sandbox.persistencia;

import br.ufg.inf.es.saep.sandbox.dominio.*;

import java.util.*;

public class SaepTestUtil {

    public static Parecer getParecerInstance(){
        String id = UUID.randomUUID().toString();
        String resolucao = UUID.randomUUID().toString();

        // Cria lista de radocs
        List<String> radocs = new ArrayList<String>();
        radocs.add(UUID.randomUUID().toString());
        radocs.add(UUID.randomUUID().toString());

        // Cria lista de pontuacoes
        List<Pontuacao> pontuacoes = new ArrayList<Pontuacao>();
        pontuacoes.add(getPontuacaoInstance("multiplicacao"));
        pontuacoes.add(getPontuacaoInstance("soma"));
        String fundamentacao = "fundamento";

        // Cria lista de notas
        List<Nota> notas = new ArrayList<Nota>();
        notas.add(getNotaInstance());
        notas.add(getNotaInstance());
        notas.add(getNotaInstance());

        return new Parecer(id, resolucao, radocs, pontuacoes, fundamentacao, notas);
    }

    public static Pontuacao getPontuacaoInstance( String atributo ){
        Random r = new Random();
        return new Pontuacao(atributo, new Valor(r.nextInt(100)));
    }

    public static Nota getNotaInstance(){
        Random r = new Random();
        Avaliavel original = new Pontuacao("Atividade 1", new Valor(r.nextInt(50)));
        Avaliavel novo = new Pontuacao("Atividade 2", new Valor(r.nextInt(200)));
        return new Nota(original, novo, UUID.randomUUID().toString());
    }

    public static Radoc getRadocInstance(){
        List<Relato> relatos = new ArrayList<Relato>();
        relatos.add(getRelatoInstance());
        relatos.add(getRelatoInstance());
        relatos.add(getRelatoInstance());
        Radoc radoc = new Radoc(UUID.randomUUID().toString(), 2014, relatos);

        return radoc;
    }

    public static Relato getRelatoInstance(){
        Map<String, Valor> valores = new HashMap<String, Valor>();
        valores.put("valor1", getValorInstance());
        valores.put("valor2", getValorInstance());
        valores.put("valor3", getValorInstance());
        valores.put("valor4", getValorInstance());

        Relato relato = new Relato(UUID.randomUUID().toString(), valores);

        return relato;
    }

    public static Valor getValorInstance(){
        return new Valor(UUID.randomUUID().toString());
    }

    public static Resolucao getResolucaoInstance(){
        List<Regra> regras = new ArrayList<Regra>();
        regras.add(getRegraInstance());
        regras.add(getRegraInstance());
        regras.add(getRegraInstance());
        Resolucao resolucao = new Resolucao(UUID.randomUUID().toString(), "nome", "descricao", new Date(), regras);
        return resolucao;
    }

    public static Regra getRegraInstance(){
        String variavel = "varia";
        int tipo = Regra.PONTOS;
        String descricao = "descreva aqui";
        float valorMaximo = 20;
        float valorMinimo = 2;
        String expressao = "nenhuma";
        String entao = "entao";
        String senao = "senao";
        String tipoRelato = "meu relato";
        int pontosPorItem = 2;
        List<String> dependeDe = new ArrayList<String>();
        dependeDe.add("muito trabalho");
        dependeDe.add("paciência");
        dependeDe.add("descanso");
        Regra regra = new Regra(variavel, tipo, descricao, valorMaximo, valorMinimo, expressao, entao, senao, tipoRelato, pontosPorItem, dependeDe);

        return regra;
    }

    public static Tipo getTipoInstance(){
        String tipo = UUID.randomUUID().toString();
        Set<Atributo> atributos = new HashSet<Atributo>();
        atributos.add(getAtributoInstance());
        atributos.add(getAtributoInstance());
        atributos.add(getAtributoInstance());
        return new Tipo(tipo, "hey", "descricao", atributos);
    }

    public static Tipo getTipoInstance( String nome ){
        String tipo = UUID.randomUUID().toString();
        Set<Atributo> atributos = new HashSet<Atributo>();
        atributos.add(getAtributoInstance());
        atributos.add(getAtributoInstance());
        atributos.add(getAtributoInstance());
        return new Tipo(tipo, nome, "descricao", atributos);
    }

    public static Atributo getAtributoInstance(){
        return new Atributo(UUID.randomUUID().toString(), "descreva aqui", Atributo.REAL);
    }


}
