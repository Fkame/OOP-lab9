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
        
		for (URLDepthPair pair: linksList) {
			myPool.put(pair);
		}
		
		//System.exit(0);
		
		/*
        // Iterate through links from site.
        for (int i=0;i<linksList.size();i++) {
            String newURL = linksList.get(i);
            
            // Create a new depth pair for each link found and add to pool.
            URLDepthPair newDepthPair = new URLDepthPair(newURL, myDepth + 1);
            myPool.put(newDepthPair);
        }
		*/
    }
}