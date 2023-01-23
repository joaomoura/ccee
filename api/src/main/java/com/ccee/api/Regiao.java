package com.ccee.api;

import java.util.ArrayList;


public class Regiao {

    private static class Geracao {
        private int valor;
    }
    private static class Compra {
        private int valor;
    }
    private static class PrecoMedio {
        private int valor;
    }

    private String sigla;
    private ArrayList<Geracao> geracoes = new ArrayList<>();
    private ArrayList<Compra> compras = new ArrayList<>();
    private ArrayList<PrecoMedio> precos = new ArrayList<>();

    public String getSigla() {
        return this.sigla;
    }

    public void setSigla(String sigla) {
        this.sigla = sigla;
    }

    public ArrayList<Geracao> getGeracoes() {
        return geracoes;
    }

    public void setGeracoes(ArrayList<Geracao> geracoes) {
        this.geracoes = geracoes;
    }

    public ArrayList<Compra> getCompras() {
        return compras;
    }

    public void setCompras(ArrayList<Compra> compras) {
        this.compras = compras;
    }

    public ArrayList<PrecoMedio> getPrecos() {
        return precos;
    }

    public void setPrecos(ArrayList<PrecoMedio> precos) {
        this.precos = precos;
    }

}
