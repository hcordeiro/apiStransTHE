package com.equalsp.stransthe;

import java.io.IOException;

public interface CachedServiceFileHander {
	final String FILE_NAME = "cachedInthegraService.json";
	public String loadCacheFile() throws IOException;
	public void saveCacheFile(String content)throws IOException;
}
