package com.equalsp.stransthe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Linha implements Serializable {

	private static final long serialVersionUID = 3912271062432682524L;

	private String CodigoLinha;

	private String Denomicao;

	private String Origem;

	private String Retorno;

	private boolean Circular;

	public String getCodigoLinha() {
		return CodigoLinha;
	}

	public void setCodigoLinha(String codigoLinha) {
		CodigoLinha = codigoLinha;
	}

	public String getDenomicao() {
		return Denomicao;
	}

	public void setDenomicao(String denomicao) {
		Denomicao = denomicao;
	}

	public String getOrigem() {
		return Origem;
	}

	public void setOrigem(String origem) {
		this.Origem = origem;
	}

	public String getRetorno() {
		return Retorno;
	}

	public void setRetorno(String retorno) {
		this.Retorno = retorno;
	}

	public boolean isCircular() {
		return Circular;
	}

	public void setCircular(boolean circular) {
		this.Circular = circular;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Linha) {
			Linha other = (Linha) obj;
			return CodigoLinha.equals(other.CodigoLinha);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return CodigoLinha == null ? 0 : CodigoLinha.hashCode();
	}
	
	@Override
	public String toString() {
		return getCodigoLinha() + " - " + toTitleCase(getDenomicao());
	}
	
	private static final List<String> lowerCaseWords = new ArrayList<>(Arrays.asList(new String[]{"VIA", "DE", "DA"}));
	private static final List<String> upperCaseWords = new ArrayList<>(Arrays.asList(new String[]{"XXIII", "II", "I.A.P.C", "HD"}));
	
	private String toTitleCase(String string) {
		StringBuilder sb = new StringBuilder();
		String[] words = string.replace(" - ", "-").replace("-", " - ").replace(" / ", "/").replace("/", " / ").split(" ");
		
		for (int i = 0; i < words.length; i++) {
			if (words[i].length() > 0) {
				if (words[i].equals(".") || words[i].equals("/")) {
					sb.append(words[i]);
				} else {
					if (i > 0) {
						if (!words[i-1].equals("/")) {
							sb.append(" ");
						}
					}
					if (upperCaseWords.contains(words[i])) {
						sb.append(words[i]);
					} else if (lowerCaseWords.contains(words[i])) {
						sb.append(words[i].toLowerCase());
					} else  {
						sb.append(Character.toUpperCase(words[i].charAt(0)) + words[i].subSequence(1, words[i].length()).toString().toLowerCase());
					}
				}
			}
		}
		return sb.toString();
	}

}
