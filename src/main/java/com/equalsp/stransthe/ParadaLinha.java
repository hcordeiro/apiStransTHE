package com.equalsp.stransthe;

import java.util.List;

class ParadaLinha {
	private Linha Linha;
	private List<Parada> Paradas;

	public Linha getLinha() {
		return Linha;
	}

	public void setLinha(Linha linha) {
		this.Linha = linha;
	}

	public List<Parada> getParadas() {
		return Paradas;
	}

	public void setParadas(List<Parada> paradas) {
		this.Paradas = paradas;
	}

}
