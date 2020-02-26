import java.util.*;

public class URLPool {
	
    private LinkedList<URLDepthPair> watchedList; 
    private LinkedList<URLDepthPair> notWatchedList;	
	private LinkedList<URLDepthPair> blockedList;
	
	/** Глубина, до которой производится поиск */
	private int depth;
	
	
	public URLPool(int depth) {
        watchedList = new LinkedList<URLDepthPair>();
        notWatchedList = new LinkedList<URLDepthPair>();
		blockedList = new LinkedList<URLDepthPair>();
		this.depth = depth;
	}
	
	
	/** Добавление элемента в список для просмотра или 
	 *в список недоступных для просмотра 
	 */
	public synchronized boolean put(URLDepthPair depthPair) {
        
        boolean added = false;

        // Если глубина меньше максимальной - тогда добавляем в список
		// ожидающих просмотра
        if (depthPair.getDepth() < this.depth) {
            notWatchedList.addLast(depthPair);
            added = true;
			
            this.notify();
        }
        // Если глубина больше максимальной - добавляем в список недоступных
        else {
            blockedList.add(depthPair);
        }
        
        return added;
    }
	
	/**
     * Получение следующей пары адрес-глубина из списка
     */
    public synchronized URLDepthPair get() {
        
        URLDepthPair myDepthPair = null;
        
        // Если список ожидающих просмотра ссылок пуст - вводим
		// поток в ожидание
        if (notWatchedList.size() == 0) {
            try {
                this.wait();
            }
            catch (InterruptedException e) {
                System.err.println("MalformedURLException: " + e.getMessage());
                return null;
            }
        } 
        // Убирает элемент из списка непросмотренных и передаёт его
        myDepthPair = notWatchedList.removeFirst();
        watchedList.add(myDepthPair);
      
        return myDepthPair;
    }
	
	
	public LinkedList<URLDepthPair> getWatchedList() {
		return this.watchedList;
	}
	
	public LinkedList<URLDepthPair> getNotWatchedList() {
		return this.notWatchedList;
	}
	
	public LinkedList<URLDepthPair> getBlockedList() {
		return this.blockedList;
	}
	
}