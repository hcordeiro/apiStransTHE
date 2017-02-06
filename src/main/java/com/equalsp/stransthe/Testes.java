package com.equalsp.stransthe;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.equalsp.stransthe.rotas.PontoDeInteresse;
import com.equalsp.stransthe.rotas.Rota;
import com.equalsp.stransthe.rotas.RotaService;
import com.equalsp.stransthe.rotas.Trecho;

public class Testes {

	public static void main(String[] args) throws Exception {
		InthegraAPI service = new InthegraService("aa91935448534d519da1cda34d0b1ee4", "c2387331@trbvn.com", "c2387331@trbvn.com");
		DesktopFileHanlder fileHanlder = new DesktopFileHanlder();
		CachedInthegraService cachedService = new CachedInthegraService(service, fileHanlder, 1, TimeUnit.DAYS);
		
//		List<Linha> linhas = cachedService.getLinhas();
//		System.out.println(linhas.size());
//		for (Linha linha : linhas) {
//			System.out.println(linha.toString());
//			System.out.println(linha.getDenomicao());
//			System.out.println("----");
//		}
		
//		CachedInthegraService cachedService = new CachedInthegraService(service, fileHanlder);
		
//		Linha linha = cachedService.getLinhas("0405").get(0);
//		List<Parada> paradas = cachedService.getParadas(linha);
//		for (Parada parada : paradas) {
//			System.out.println(parada.getLat() + "," + parada.getLong());
//		}
//		Linha linha = cachedService.getLinhas("03198").get(0);
//		List<Veiculo> veiculos = cachedService.getVeiculos();
//		for (Veiculo veiculo : veiculos) {
//			System.out.println(veiculo.getCodigoVeiculo());
//		}

		RotaService rotasService = new RotaService(cachedService);
		PontoDeInteresse casa = new PontoDeInteresse(-5.069496666666667,-42.759083333333336);
//		PontoDeInteresse juliana = new PontoDeInteresse(-5.074779, -42.751059);
		
//		PontoDeInteresse ufpi = new PontoDeInteresse(-5.062416233932089,-42.79463276267052);		
		PontoDeInteresse ifpi = new PontoDeInteresse(-5.088553, -42.810413499999996);
//		PontoDeInteresse planalto = new PontoDeInteresse(-5.055198, -42.746245);
		double distanciaPe = 415;
		
		
		long start = System.currentTimeMillis();		
		Set<Rota> rotas = rotasService.getRotas(casa, ifpi, distanciaPe);
		long end = System.currentTimeMillis();
		long delta = end - start;
		double seconds = delta / 1000.0;
		System.out.println("Fim : " +  seconds);
		System.out.println("-------");
		
		System.out.println(rotas.size());
		for (Rota rota : rotas) {
			List<Trecho> trechos = rota.getTrechos();
			System.out.println(rota);
	
			for (Trecho trecho : trechos) {
				System.out.println("\tTrecho " + trecho.getLinha());
				if (trecho.getOrigem() instanceof Parada) {
					Parada p = (Parada) trecho.getOrigem();
					System.out.println("\t\t" + trecho.getOrigem().getLat() + ", " + trecho.getOrigem().getLong() + " - " + p.getDenomicao());	
				} else { 
					System.out.println("\t\t" + trecho.getOrigem().getLat() + ", " + trecho.getOrigem().getLong());
				}
				if (trecho.getDestino() instanceof Parada) {
					Parada p = (Parada) trecho.getDestino();
					System.out.println("\t\t" + trecho.getDestino().getLat() + ", " + trecho.getDestino().getLong() + " - " + p.getDenomicao());
				} else {
					System.out.println("\t\t" + trecho.getDestino().getLat() + ", " + trecho.getDestino().getLong());
				}
			}
		}
		
		/*
		List<Linha> linhas = service.getLinhas("FREI SERAFIM");
		Linha linha = linhas.get(0);
		System.out.println(linha.getDenomicao());
		System.out.println("Veiculos: ");
		List<Veiculo> veiculos = service.getVeiculos(linha);
		for (Veiculo veiculo : veiculos) {
			System.out.println(veiculo.getCodigoVeiculo());
		}

		System.out.println("Paradas: ");
		List<Parada> paradas = service.getParadas("FREI");
		for (Parada parada : paradas) {
			System.out.println(parada.getCodigoParada() + ": " + parada.getDenomicao());
		}*/

//		List<Parada> paradasFreiSerafim1 = cachedService.getParadas("AV. FREI SERAFIM 1");
//		for (Parada parada : paradasFreiSerafim1) {
//			System.out.println(parada.getDenomicao());
//		}

//		Parada paradaFreiSerafim1 = paradasFreiSerafim1.get(0);
//		System.out.println("Linhas da parada " + paradaFreiSerafim1);
//		for (Linha l : cachedService.getLinhas(paradaFreiSerafim1)) {
//			System.out.println(l.getDenomicao());
//		}
//		CachedInthegraService cachedService = new CachedInthegraService(service, 1, TimeUnit.DAYS);
//		List<Parada> paradasFreiSerafim1 = cachedService.getParadas("AV. FREI SERAFIM 1");
//		for (Parada parada : paradasFreiSerafim1) {
//			System.out.println(parada.getDenomicao());
//		}

//		Parada paradaFreiSerafim1 = paradasFreiSerafim1.get(0);
//		System.out.println("Linhas da parada " + paradaFreiSerafim1);
//		for (Linha l : cachedService.getLinhas(paradaFreiSerafim1)) {
//			System.out.println(l.getDenomicao());
//		}
		
//		RotaService rotaService = new RotaService(cachedService);
//		PontoDeInteresse a = new PontoDeInteresse(-5.080375, -42.775798);
//		PontoDeInteresse b = new PontoDeInteresse(-5.089095, -42.810302);
//		Set<Rota> rotas = rotaService.getRotas(a, b, 200);
//		for (Rota rota : rotas) {
//			Trecho t = rota.getTrechos().get(1);
//			System.out.println(t.getLinha().getCodigoLinha() + " - " + t.getLinha().getDenomicao());
//			System.out.println(t.getOrigem());
//			System.out.println(t.getDestino());
//			System.out.println(rota.getDistanciaTotal());
//		}
//		
//		System.out.println();
//		System.out.println("Parada a parada:");
//		Parada p2 = paradasFreiSerafim1.get(paradasFreiSerafim1.size()-1);
//		Set<Rota> rotas2 = rotaService.getRotas(paradaFreiSerafim1, p2);
//		for (Rota rota : rotas2) {
//			Trecho t = rota.getTrechos().get(0);
//			System.out.println(t.getLinha().getCodigoLinha() + " - " + t.getLinha().getDenomicao());
//			System.out.println(t.getOrigem());
//			System.out.println(t.getDestino());
//			System.out.println(rota.getDistanciaTotal());
//		}
	}
}
