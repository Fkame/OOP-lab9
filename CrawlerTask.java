import java.util.*;

public class CrawlerTask implements Runnable {
    
	// Объект-пара
    private URLDepthPair element;
    
	// Объект со всеми списками
    private URLPool myPool;
    
    /** Constructor to set the variable URL pool to the pool passed to method */    
	public CrawlerTask(URLPool pool) {
        this.myPool = pool;
    }
    
    /** Запуск заданий CrawlerTasks */
    public void run() {

		// Поток получает следующих элемент из списка непросмотренных
		// адресов или входит в режим ожидания
        element = myPool.get();
        
        // Глубина текущего элемента
        int myDepth = element.getDepth();
        
        // Получение всех ссылок после парсинга
        LinkedList<URLDepthPair> linksList = new LinkedList<URLDepthPair>();
        linksList = Crawler.parsePage(element);
        
		Crawler.showResults(element, linksList);
		
		for (URLDepthPair pair: linksList) {
			myPool.put(pair);
		}
		
    }
}