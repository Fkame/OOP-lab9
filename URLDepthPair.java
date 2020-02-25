import java.net.MalformedURLException;
import java.net.URL;

public class URLDepthPair {
	
	private String url;
	private int depth;
	public static final String URL_PREFIX = "http://";
	public static final int MAX_DEPTH_VALUE = 100;
	
	/*
	* Конструктор. Выбрасывает исключения, если захочет
	*/
	public URLDepthPair(String url, int depth) throws MalformedURLException {
		
		// Проверка валидности введенных параметров, и выброс соответствующих исключений
		
		if (depth < 0 || depth > MAX_DEPTH_VALUE) {
			throw new IllegalArgumentException("Error limits of depth");
		} 
		
		if (!URLDepthPair.isHttpPrefixInURL(url)) {
			MalformedURLException ex = new MalformedURLException("Error of url prefix");
			throw ex;
		}
		
		this.url = url;
		this.depth = depth;
	}
	
	/*
	* Поверяет префикс URL на соответствие протоколу HTTP
	*/	
	public static boolean isHttpPrefixInURL(String url) {
		if (!url.startsWith(URL_PREFIX)) return false;
		return true;
	}
	
	/*
	* Поверяет глубину на превышение лимита
	*/	
	public static boolean isDepthAboveLimit(int depth) {
		if (depth > MAX_DEPTH_VALUE) return true;
		return false;
	}
	
	/*
	* Переопределение toString();
	*/
	public String toString() {
		return new String("[ " + this.url + ", " + this.depth + " ]");
	}
	
	/* 
	* Получение параметров
	*/
	public String getHostName() {
		try {
            URL url = new URL(this.url);
            return url.getHost();
        }
        catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
            return null;
        }
	}
	
	public String getPagePath() {
		try {
			URL url = new URL(this.url);
            return url.getPath();
        }
        catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
            return null;
        }
	}
	
	public String getURL() {
		return this.url;
	}
	
	public int getDepth() {
		return this.depth;
	}
	
	public void setDepth(int depth) {
		if (depth < 0 || depth > MAX_DEPTH_VALUE) {
			throw new IllegalArgumentException("Error limits of depth");
		} 
		this.depth = depth;
	}
	
	public static String fullUrlStringToHostName(String in) {
		try {
            URL url = new URL(in);
            return url.getHost();
        }
        catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
            return null;
        }
	}
	
	public static URL getUrlObjectFromUrlString(String urlStr) {
		try {
            URL url = new URL(urlStr);
            return url;
        }
        catch (MalformedURLException e) {
            System.err.println("MalformedURLException: " + e.getMessage());
            return null;
        }
	}
}