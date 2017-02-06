package com.equalsp.stransthe.rotas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.equalsp.stransthe.Linha;

public class Rota implements Serializable, Comparable<Rota> {

	private static final long serialVersionUID = -5293540601998247559L;
	
	private List<Trecho> trechos = new ArrayList<Trecho>();

	public List<Trecho> getTrechos() {
		return trechos;
	}

	public long getTempoTotal() {
		long tempo = 0;
		for (Trecho trecho : trechos) {
			tempo += trecho.getTempo();
		}
		return tempo;
	}

	public double getDistanciaTotal() {
		double distancia = 0;
		for (Trecho trecho : trechos) {
			distancia += trecho.getDistancia();
		}
		return distancia;
	}

	@Override
	public int compareTo(Rota o) {
		if (o.getLinhas().equals(getLinhas())) {
			return ((Integer) trechos.size()).compareTo(o.getTrechos().size());
		}
		return 1;
	}
	

	public List<Linha> getLinhas() {
		List<Linha> linhasDoTrecho = new ArrayList<>();
		
		for (Trecho trecho : trechos) {
			Linha linha = trecho.getLinha();
			if (linha != null) {
				if (!linhasDoTrecho.contains(linha)){
					linhasDoTrecho.add(linha);	
				}
			}
		}
		return linhasDoTrecho;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		for (Linha linha : getLinhas()) {
			builder.append(linha.toString() + " | ");
		}
		builder.delete(builder.lastIndexOf("|")-1, builder.length());

		return builder.toString();
	}
}
