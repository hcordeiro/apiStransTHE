package com.equalsp.stransthe.rotas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import com.equalsp.stransthe.CachedInthegraService;
import com.equalsp.stransthe.Linha;
import com.equalsp.stransthe.Localizacao;
import com.equalsp.stransthe.Parada;

/**
 * 
 * @author Erick Passos
 *
 */
public class RotaService {

	private CachedInthegraService cachedService;
	
	/*
	 * API PUBLICA
	 * construtor
	 * busca de rotas (3 opções)
	 * busca de paradas próximas
	 */

	public RotaService(CachedInthegraService cachedService) {
		super();
		this.cachedService = cachedService;
	}
	
	public Set<Rota> getRotas(PontoDeInteresse a, PontoDeInteresse b, double distanciaPe) throws IOException {
		List<Parada> origens = getParadasProximas(a, distanciaPe, 5);
		List<Parada> destinos = getParadasProximas(b, distanciaPe, 5);
		Set<Rota> rotas = getRotas_UnicoOnibus(origens, destinos);
		if (rotas.isEmpty()) {
			origens = getParadasProximas(a, distanciaPe, 5);
			destinos = getParadasProximas(b, distanciaPe, 2);
			rotas = getRotas_DoisOnibus(origens, destinos, distanciaPe);
		}
		
		for (Rota rota : rotas) {
			adicionarTrechosPedestre(rota, a, b);
		}
		return rotas;
	}
	
	public Set<Rota> getRotas(Parada origem, Parada destino) throws IOException {
		List<Parada> origens = new ArrayList<Parada>();
		origens.add(origem);
		List<Parada> destinos = new ArrayList<Parada>();
		destinos.add(destino);
		return getRotas_UnicoOnibus(origens, destinos);
	}

	public Set<Rota> getRotas_UnicoOnibus(List<Parada> origens, List<Parada> destinos) throws IOException {
		Set<Rota> rotas = new TreeSet<Rota>();
		Parada ultimaParada;
		for (Parada origem : origens) {
			List<Linha> linhasOrigem = getLinhas(origem);
			for (Linha linha : linhasOrigem) {
				List<Parada> destinosDaLinha = destinosDaLinhaAposOrigem(linha, origem, destinos);
				if (!destinosDaLinha.isEmpty()) {
					for (Parada destino : destinosDaLinha) {
						Rota rota = new Rota();
						List<Parada> proximasParadas = paradasDaLinhaAteDestino(linha, origem, destino);
						ultimaParada = origem;
						for (Parada parada : proximasParadas) {
							if (ultimaParada != origem) {
								Trecho trecho = new Trecho();
								trecho.setOrigem(ultimaParada);
								trecho.setDestino(parada);
								trecho.setLinha(linha);
								rota.getTrechos().add(trecho);							
							}
							ultimaParada = parada;
						}
						rotas.add(rota);
					}	
				}
			}
		}
		
		Map<Linha, List<Rota>> rotasPorLinha = new HashMap<>();
        for (Rota r : rotas) {
            Linha l = r.getTrechos().get(1).getLinha();
            if (rotasPorLinha.containsKey(l)) {
                rotasPorLinha.get(l).add(r);
            } else {
                List<Rota> rotasDaLinha = new ArrayList<>();
                rotasDaLinha.add(r);
                rotasPorLinha.put(l, rotasDaLinha);
            }
        }

		Set<Rota> rotasList = new TreeSet<Rota>();
		for (List<Rota> rs : rotasPorLinha.values()) {
			Collections.sort(rs, new Comparator<Rota>() {
				@Override
				public int compare(Rota r1, Rota r2) {
					return ((Integer) r1.getTrechos().size()).compareTo(r2.getTrechos().size());
				}
			});
			rotasList.add(rs.get(0));
		}
		
		return rotasList;
	}
	
	public Set<Rota> getRotas_DoisOnibus(List<Parada> origens, List<Parada> destinos, double distanciaPe) throws IOException {
		Set<Rota> rotas = new TreeSet<>();
		Map<Linha, Map<Linha, List<Rota>>> mapaRotas = new HashMap<>();
		
		Parada ultimaParada, primeiraParadadaLinhaQuePassanNoDestino;
		List<Linha> linhasQuePassamNoDestino, linhasQuePassamNaParadaProxima,linhasQuePassamNaOrigem,linhasEmComum;
		List<Parada> paradasDaLinhaQuePassaNoDestino, paradasProximas, paradasDaLinhaQuePassaNaOrigemAteParadaProxima;
		
		for (Parada destino : destinos) {
			linhasQuePassamNoDestino = getLinhas(destino);
			linhaQuePassaNoDestino: for (Linha linhaQuePassaNoDestino : linhasQuePassamNoDestino) {
				primeiraParadadaLinhaQuePassanNoDestino = getParadas(linhaQuePassaNoDestino).get(0);
				paradasDaLinhaQuePassaNoDestino = paradasDaLinhaAteDestino(linhaQuePassaNoDestino, primeiraParadadaLinhaQuePassanNoDestino, destino);
				Collections.reverse(paradasDaLinhaQuePassaNoDestino);
				for (Parada paradaDaLinhaQuePassaNoDestino : paradasDaLinhaQuePassaNoDestino) {
					paradasProximas = getParadasProximas(paradaDaLinhaQuePassaNoDestino, distanciaPe, 4);
					for (final Parada paradaProxima : paradasProximas) {
						linhasQuePassamNaParadaProxima = getLinhas(paradaProxima);
						origemLoop: for (final Parada origem : origens) {
							linhasQuePassamNaOrigem = getLinhas(origem);
							linhasEmComum = new ArrayList<>(linhasQuePassamNaParadaProxima);
							linhasEmComum.retainAll(linhasQuePassamNaOrigem);
							linhaQuePassaNaOrigem: for (Linha linhaQuePassaNaOrigem : linhasEmComum) {
								if (mapaRotas.containsKey(linhaQuePassaNaOrigem)) {
									Map<Linha, List<Rota>> map = mapaRotas.get(linhaQuePassaNaOrigem);
									if (map.containsKey(linhaQuePassaNoDestino)) {
										if (map.get(linhaQuePassaNoDestino).size() > 1) {
											continue linhaQuePassaNaOrigem;
										}
									}
								}

								if (linhasQuePassamNaParadaProxima.contains(linhaQuePassaNaOrigem)) {
									paradasDaLinhaQuePassaNaOrigemAteParadaProxima = paradasDaLinhaAteDestino(linhaQuePassaNaOrigem, origem, paradaProxima);
									if (paradasDaLinhaQuePassaNaOrigemAteParadaProxima.isEmpty()) {
										continue origemLoop;
									}

									Rota rotaNova = new Rota();
									ultimaParada = origem;
									for (Parada parada : paradasDaLinhaQuePassaNaOrigemAteParadaProxima) {
										if (ultimaParada != origem) {
											Trecho trecho = new Trecho();
											trecho.setOrigem(ultimaParada);
											trecho.setDestino(parada);
											trecho.setLinha(linhaQuePassaNaOrigem);
											rotaNova.getTrechos().add(trecho);
										}
										ultimaParada = parada;
									}

									List<Parada> paradasDaLinhaQuePassaNoDestinoAteDestino = paradasDaLinhaAteDestino(
											linhaQuePassaNoDestino, paradaDaLinhaQuePassaNoDestino, destino);
									if (!paradasDaLinhaQuePassaNoDestinoAteDestino.isEmpty()) {
										if (!ultimaParada.equals(paradaDaLinhaQuePassaNoDestino)) {
											Trecho trechoAPe = new Trecho();
											trechoAPe.setOrigem(ultimaParada);
											trechoAPe.setDestino(paradaDaLinhaQuePassaNoDestino);
											rotaNova.getTrechos().add(trechoAPe);
										}

										ultimaParada = paradaDaLinhaQuePassaNoDestino;
										for (Parada parada : paradasDaLinhaQuePassaNoDestinoAteDestino) {
											if (ultimaParada != parada) {
												Trecho trecho = new Trecho();
												trecho.setOrigem(ultimaParada);
												trecho.setDestino(parada);
												trecho.setLinha(linhaQuePassaNoDestino);
												rotaNova.getTrechos().add(trecho);
											}
											ultimaParada = parada;
										}

										// asdasdasdas
										if (!mapaRotas.containsKey(linhaQuePassaNaOrigem)) {
											Map<Linha, List<Rota>> rotasDaLinhaDeOrigem = new HashMap<>();
											List<Rota> rs = new ArrayList<>();
											rs.add(rotaNova);
											rotasDaLinhaDeOrigem.put(linhaQuePassaNoDestino, rs);
											mapaRotas.put(linhaQuePassaNaOrigem, rotasDaLinhaDeOrigem);
										} else {
											Map<Linha, List<Rota>> rotasDaLinhaDeOrigemEDestino = mapaRotas
													.get(linhaQuePassaNaOrigem);
											if (!rotasDaLinhaDeOrigemEDestino.containsKey(linhaQuePassaNoDestino)) {
												List<Rota> rs = new ArrayList<>();
												rs.add(rotaNova);
												rotasDaLinhaDeOrigemEDestino.put(linhaQuePassaNoDestino, rs);
											} else {
												rotasDaLinhaDeOrigemEDestino.get(linhaQuePassaNoDestino).add(rotaNova);
											}
										}
										rotas.add(rotaNova);
									}
								}
							}
						}
					}
				}
			}
		}
		
		Map<Linha, Map<Linha, List<Rota>>> rotasPorLinha_todas = new HashMap<>();
		for (Rota rota : rotas) {
			List<Linha> linhasPorRota = new ArrayList<>();
			for (Trecho trecho : rota.getTrechos()) {
				if (trecho.getLinha() != null) {
					linhasPorRota.add(trecho.getLinha());
				}
			}
			
			Linha l1 = linhasPorRota.get(0);
			Linha l2 = linhasPorRota.get(1);
			if (rotasPorLinha_todas.containsKey(l1)) {
				Map<Linha, List<Rota>> rotasPorLinha2 = rotasPorLinha_todas.get(l1);
				if (rotasPorLinha2.containsKey(l2)) {
					rotasPorLinha2.get(l2).add(rota);
				} else {
					List<Rota> rotasDaLinha = new ArrayList<>();
					rotasDaLinha.add(rota);
					rotasPorLinha2.put(l2, rotasDaLinha);
				}
            } else {
                Map<Linha, List<Rota>> rotasPorLinha2 = new HashMap<>();
                List<Rota> rotasDaLinha = new ArrayList<>();
                rotasDaLinha.add(rota);
                rotasPorLinha2.put(l2, rotasDaLinha);
                rotasPorLinha_todas.put(l1, rotasPorLinha2);
            }
		}
		
		Set<Rota> rotasList = new TreeSet<Rota>();
		for (Entry<Linha, Map<Linha, List<Rota>>> e1 : rotasPorLinha_todas.entrySet()) {
			Map<Linha, List<Rota>> value = e1.getValue();
			for (Entry<Linha, List<Rota>> e2 : value.entrySet()) {
				List<Rota> rs = e2.getValue();
				Collections.sort(rs, new Comparator<Rota>() {
	                @Override
	                public int compare(Rota r1, Rota r2) {
	                    return ((Integer) r1.getTrechos().size()).compareTo(r2.getTrechos().size());
	                }
	            });
				rotasList.add(rs.get(0));
			}
		}
		return rotasList;
	}
	
	public int paradasAte(Linha linha, PontoDeInteresse origem, Parada destino) throws IOException {
		Parada paradaOrigem = getParadasProximas(origem, 1000, 1).get(0);
		List<Parada> paradasLinha = getParadas(linha);
		
		boolean passouOrigem = false;
		int counter = 0;
		for (Parada parada : paradasLinha) {
			if (parada.equals(paradaOrigem)) {
				passouOrigem = true;
			} else if (passouOrigem) {
				counter++;
			}
			
			if (passouOrigem && parada.equals(destino)) {
				break;
			}
		}

		return counter;
	}
	
	/*
	 * FIM DE API PÚBLICA
	 */
	
	private List<Parada> getParadasProximas(Localizacao a, double distanciaMaxima, int quantidade) throws IOException {
		List<Parada> proximas = new ArrayList<Parada>();
		List<Parada> paradas = getParadas();
		Collections.sort(paradas, new ComparadorPorProximidade(a));
		
		for (Parada p : paradas) {
			if (a.getDistancia(p) <= distanciaMaxima) {
				proximas.add(p);
			}
			if (proximas.size() >= quantidade)
				break;
		}
		return proximas;
	}

	private void adicionarTrechosPedestre(Rota rota, PontoDeInteresse a, PontoDeInteresse b) {
		Trecho inicial = new Trecho();
		List<Trecho> trechos = rota.getTrechos();
		inicial.setOrigem(a);
		inicial.setDestino(trechos.get(0).getOrigem());

		Trecho f = new Trecho();
		f.setOrigem(trechos.get(trechos.size()-1).getDestino());
		f.setDestino(b);

		trechos.add(0, inicial);
		trechos.add(f);
	}

	@SuppressWarnings("unused")
	private Rota criaRota(Linha linha, Parada origem, Parada destino) {
		Rota r = new Rota();
		Trecho t = new Trecho();
		t.setDestino(destino);
		t.setOrigem(origem);
		t.setLinha(linha);
		r.getTrechos().add(t);
		return r;
	}
	
	private List<Parada> destinosDaLinhaAposOrigem(Linha linha, Parada origem, List<Parada> destinos) throws IOException {
		List<Parada> paradasLinha = getParadas(linha);
		List<Parada> paradas = new ArrayList<>();
		boolean passouOrigem = false;
		for (Parada parada : paradasLinha) {
			if (parada.equals(origem)) {
				//System.out.println("passou origem...");
				passouOrigem = true;
			} else if (passouOrigem && destinos.contains(parada)) {
				paradas.add(parada);
			}
		}
		return paradas;
	}
	
	private List<Parada> paradasDaLinhaAteDestino(Linha linha, Parada origem, Parada destino) throws IOException {
		List<Parada> paradasLinha = getParadas(linha);
		List<Parada> proximasParadas = new ArrayList<>();
		boolean passouOrigem = false;
		boolean passouDestino = false;
		
		for (Parada parada : paradasLinha) {
			if (parada.equals(origem)) {
				passouOrigem = true;
			}
			
			if (destino.equals(parada)){
				if (!passouOrigem) {
					break;
				}
				proximasParadas.add(parada);
				passouDestino = true;
				continue;
			}
			
			if (passouOrigem && !passouDestino) {
				proximasParadas.add(parada);
			}
		}
		return proximasParadas;
	}

	private List<Parada> getParadas() throws IOException {
		return cachedService.getParadas();
	}

	private List<Parada> getParadas(Linha l) throws IOException {
		return cachedService.getParadas(l);
	}

	private List<Linha> getLinhas(Parada p) throws IOException {
		return cachedService.getLinhas(p);
	}
}
