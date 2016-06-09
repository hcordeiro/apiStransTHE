package com.equalsp.stransthe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class CachedInthegraService implements InthegraAPI {

	private static final int ITEM_NOT_FOUND = 130;
	
	private final InthegraAPI delegate;
	
	protected final CachedServiceFileHander fileHandler;
	protected final long timeoutInMillis;
	protected final ReentrantLock lock = new ReentrantLock();
	
	protected long expireAt = 0;
	protected Map<Linha, List<Parada>> cacheLinhaParadas = new HashMap<>();
	protected Map<Parada, List<Linha>> cacheParadaLinhas = new HashMap<>();

	public CachedInthegraService(InthegraAPI delegate, CachedServiceFileHander fileHandler) {
		if (delegate == null) {
			throw new IllegalArgumentException("delegate não pode ser null");
		}
		if (fileHandler == null) {
			throw new IllegalArgumentException("fileHandler não pode ser null");
		}
		this.delegate = delegate;
		this.fileHandler = fileHandler;
		this.timeoutInMillis = 0;
	}
	
	public CachedInthegraService(InthegraAPI delegate, CachedServiceFileHander fileHandler, long tempoExpiracao, TimeUnit unit) {
		if (delegate == null) {
			throw new IllegalArgumentException("delegate não pode ser null");
		}
		if (fileHandler == null) {
			throw new IllegalArgumentException("fileHandler não pode ser null");
		}
		this.delegate = delegate;
		this.fileHandler = fileHandler;
		this.timeoutInMillis = unit.toMillis(tempoExpiracao);
	}

	@Override
	public void initialize() throws IOException {
		if (System.currentTimeMillis() > expireAt) {
			lock.lock();
			try {
				boolean successfullyLoaded = loadFromFile();
				if (!successfullyLoaded) {
					refreshCache();
					if (timeoutInMillis != 0) {
						expireAt = System.currentTimeMillis() + timeoutInMillis;
					} else {
						expireAt = Long.MAX_VALUE;
					}
					saveToFile();
				}
			} finally {
				lock.unlock();
			}
		}
	}

	@Override
	public List<Linha> getLinhas() throws IOException {
		initialize();
		return new ArrayList<>(cacheLinhaParadas.keySet());
	}

	@Override
	public List<Linha> getLinhas(String busca) throws IOException {
		List<Linha> linhas = new ArrayList<>();
		for (Linha linha : getLinhas()) {
			if (linha.getCodigoLinha().equals(busca) || linha.getDenomicao().contains(busca)) {
				linhas.add(linha);
			}
		}
		return linhas;
	}

	@Override
	public List<Parada> getParadas() throws IOException {
		initialize();
		return new ArrayList<>(cacheParadaLinhas.keySet());
	}

	@Override
	public List<Parada> getParadas(String busca) throws IOException {
		List<Parada> paradas = new ArrayList<>();
		for (Parada parada : getParadas()) {
			if (parada.getCodigoParada().equals(busca) || parada.getDenomicao().contains(busca)) {
				paradas.add(parada);
			}
		}
		return paradas;
	}

	@Override
	public List<Parada> getParadas(Linha linha) throws IOException {
		initialize();
		return cacheLinhaParadas.get(linha);
	}

	@Override
	public List<Linha> getLinhas(Parada parada) throws IOException {
		initialize();
		return cacheParadaLinhas.get(parada);
	}

	@Override
	public List<Veiculo> getVeiculos() throws IOException {
		return delegate.getVeiculos();
	}

	@Override
	public List<Veiculo> getVeiculos(Linha linha) throws IOException {
		return delegate.getVeiculos(linha);
	}
	
	protected void refreshCache() throws IOException {
		cacheLinhaParadas.clear();
		cacheParadaLinhas.clear();

		List<Linha> linhas = delegate.getLinhas();
		List<Parada> paradas;
		for (Linha linha : linhas) {
			try {
				paradas = delegate.getParadas(linha);
				cacheLinhaParadas.put(linha, paradas);

				for (Parada parada : paradas) {
					List<Linha> linhasDaParada = cacheParadaLinhas.get(parada);
					if (linhasDaParada == null) {
						linhasDaParada = new ArrayList<>();
						cacheParadaLinhas.put(parada, linhasDaParada);
					}
					linhasDaParada.add(linha);
				}

			} catch (InthegraException e) {
				if (e.getErro().getCode() != ITEM_NOT_FOUND) {
					throw e;
				}
			}
		}
	}
	
	private void saveToFile() throws IOException {
		Gson gson = new GsonBuilder().create();
		JsonObject cachedJsonObject = new JsonObject();
		
		cachedJsonObject.addProperty("expireAt", gson.toJson(expireAt));
		
		JsonArray linhasParadasJsonArray = new JsonArray();
		for (Linha linha : cacheLinhaParadas.keySet()) {
			JsonObject linhaParadaJsonObject = new JsonObject();
			String linhaJson = gson.toJson(linha);
			
			List<Parada> paradas = cacheLinhaParadas.get(linha);
			JsonArray paradasJsonArray = new JsonArray();
			for (Parada parada : paradas) {
				String paradaJson = gson.toJson(parada);
				paradasJsonArray.add(paradaJson);
			}
			linhaParadaJsonObject.addProperty("linha", linhaJson);
			linhaParadaJsonObject.add("paradas", paradasJsonArray);
			
			linhasParadasJsonArray.add(linhaParadaJsonObject);
		}
		cachedJsonObject.add("linhasParadas", linhasParadasJsonArray);
		//cachedJsonObject.add("linhasParadas", gson.toJsonTree(cacheLinhaParadas));
		
		JsonArray paradasLinhasJsonArray = new JsonArray();
		for (Parada parada : cacheParadaLinhas.keySet()) {
			JsonObject paradaLinhasJsonObject = new JsonObject();
			String paradaJson = gson.toJson(parada);
			
			List<Linha> linhas = cacheParadaLinhas.get(parada);
			JsonArray linhasJsonArray = new JsonArray();
			for (Linha linha : linhas) {
				String linhaJson = gson.toJson(linha);
				linhasJsonArray.add(linhaJson);
			}
			paradaLinhasJsonObject.addProperty("parada", paradaJson);
			paradaLinhasJsonObject.add("linhas", linhasJsonArray);
			
			paradasLinhasJsonArray.add(paradaLinhasJsonObject);
		}
		cachedJsonObject.add("paradasLinhas", paradasLinhasJsonArray);
		//cachedJsonObject.add("paradasLinhas", gson.toJsonTree(cacheParadaLinhas));

		String cacheJson = gson.toJson(cachedJsonObject);
		fileHandler.saveCacheFile(cacheJson);
	}
	
	protected boolean loadFromFile() throws IOException {
		String fileContent = fileHandler.loadCacheFile();
		if (!fileContent.isEmpty()) {
			Gson gson = new GsonBuilder().create();
			JsonObject cacheJson = gson.fromJson(fileContent, JsonObject.class);
			expireAt = cacheJson.get("expireAt").getAsLong();
			
			if (System.currentTimeMillis() > expireAt) {
				return false;
			} else {
				cacheLinhaParadas.clear();
				cacheParadaLinhas.clear();
				
				JsonArray linhasParadas = cacheJson.getAsJsonArray("linhasParadas");
				for (int i = 0; i < linhasParadas.size(); ++i) {
					JsonObject jsonObject = linhasParadas.get(i).getAsJsonObject();
					Linha linha = gson.fromJson(jsonObject.get("linha").getAsString(), Linha.class);
					JsonArray paradasJsonArray = jsonObject.getAsJsonArray("paradas");
					
					List<Parada> paradasDaLinha = new ArrayList<>();
					for (int j = 0; j < paradasJsonArray.size(); j++) {
						JsonElement paradaObejct = paradasJsonArray.get(j);
						Parada parada = gson.fromJson(paradaObejct.getAsString(), Parada.class);
						paradasDaLinha.add(parada);
					}
					
					cacheLinhaParadas.put(linha, paradasDaLinha);
				}
				
				JsonArray paradasLinhas = cacheJson.getAsJsonArray("paradasLinhas");
				for (int i = 0; i < paradasLinhas.size(); ++i) {
					JsonObject jsonObject = paradasLinhas.get(i).getAsJsonObject();
					
					Parada parada = gson.fromJson(jsonObject.get("parada").getAsString(), Parada.class);
					JsonArray linhasJsonArray = jsonObject.getAsJsonArray("linhas");
					
					List<Linha> linhasDaParada = new ArrayList<>();
					for (int j = 0; j < linhasJsonArray.size(); j++) {
						JsonElement paradaObejct = linhasJsonArray.get(j);
						Linha linha = gson.fromJson(paradaObejct.getAsString(), Linha.class);
						linhasDaParada.add(linha);
					}
					cacheParadaLinhas.put(parada, linhasDaParada);
				}
				return true;
			}
		}
		return false;
	}

}