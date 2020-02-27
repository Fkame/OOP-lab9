import java.lang.Exception;
import java.util.*;
import java.net.MalformedURLException;
import java.net.*;
import java.io.*;

public class Crawler {

	public static final int HTTP_PORT = 80;
	public static final String HOOK_REF = "<a href=\"";
	public static final String BAD_REQUEST_LINE = "HTTP/1.1 400 Bad Request";	
	public static final int NUM_OF_DEFAULT_THREADS = 4;
	

	// Данные для тестирования без ввода
	public static final String testURL = "http://users.cms.caltech.edu/~donnie/cs11/java/";
	//public static final String testURL = "http://users.cms.caltech.edu/~donnie/cs11/java/lectures/cs11-java-lec1.pdf";
	public static final int testDepth = 1;

	
	// Глубина поиска
	public int depth;
	
	//Количество потоков, которое вводит пользователь или их число подается при запуске программы
	public int numOfThreads;

	// Конструктор
	public Crawler() {
		
	}


	// Точка входа
	public static void main (String[] args) {

		Crawler crawler = new Crawler();

		crawler.numOfThreads = Crawler.NUM_OF_DEFAULT_THREADS;
		
		// Считывание данных с консоли как аргументов, или запрос данных от пользователя
		// Функция getFirstURLDepthPair также настраивает максимальную глубину
		URLDepthPair firstRezAndSetDepth = crawler.getFirstURLDepthPair(args);
		crawler.numOfThreads = CrawlerHelper.getNumOfThreads(args);
		
		URLPool pool = new URLPool(crawler.depth);
        pool.put(firstRezAndSetDepth);
		
		int totalThreads = 0;
        int initialActive = Thread.activeCount();
		
		while (pool.getWaitThreads() != crawler.numOfThreads) {
			
			//System.out.println("Wait threads = " + pool.getWaitThreads() + "\n");
			
            if (Thread.activeCount() - initialActive < crawler.numOfThreads) {
                CrawlerTask crawlerTask = new CrawlerTask(pool);
                new Thread(crawlerTask).start();
            }
            else {
                try {
                    Thread.sleep(100);  // 0.1 second
                }
                // Catch InterruptedException.
                catch (InterruptedException ie) {
                    System.out.println("Caught: unexpected InterruptedException, ignoring...");
                }

            }
        }
		
		// Вывод результатов
		System.out.println("");
		System.out.println("-----------------------------------");
		System.out.println("----------Progs work end-----------");
		System.out.println("-----------Rezults:----------------");
		System.out.println("-----------------------------------");
		
		LinkedList<URLDepthPair> list = pool.getWatchedList();
		System.out.println("Watched pages:");
		int count = 1;
		for (URLDepthPair page : list) {
			System.out.println(count + " |  " + page.toString());
			count += 1;
		}
		
		list = pool.getBlockedList();
		System.out.println("\nPages that have not been parsed:");
		count = 1;
		for (URLDepthPair page : list) {
			System.out.println(count + " |  " + page.toString());
			count += 1;
		}
		
		System.out.println("-----------------------------------");
		System.out.println("----------End of rezults-----------");
		System.out.println("-----------End of prog-------------");
		System.out.println("-----------------------------------");
		
		System.exit(0);
		
	}
	
	/*
	* Создаёт новый объект-пару и по ноебходимости переводит из одного списка в другой
	*/ 
	public static void createURlDepthPairObject(String url, int depth, LinkedList<URLDepthPair> listOfUrl) {
		URLDepthPair newURL = null;
		try{
			// Формироване нового объекта и добавление его в список
			newURL = new URLDepthPair(url, depth);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		listOfUrl.addLast(newURL);
	}
	

	/*
	* Проверка командной строки, ввода пользователя и добавление первого объекта URLDepthPair
	* В список непросмотренных
	* Если нет ввода из командной строки передавать просто null
	*/
	public URLDepthPair getFirstURLDepthPair(String[] args) {
		CrawlerHelper help = new CrawlerHelper();

		// Чтение аргументов из командной строки
		URLDepthPair urlDepth = help.getURLDepthPairFromArgs(args);
		if (urlDepth == null) {
			System.out.println("Args are empty or have exception. Now you need to enter URL and depth manually!\n");

			// Получение ввода от пользователей
			urlDepth = help.getURLDepthPairFromInput();
		}

		// Получение и замена глубины
		this.depth = urlDepth.getDepth();
		urlDepth.setDepth(0);

		
		return(urlDepth);

		// Вывод первого объекта URLDepthPair
		//System.out.println("First site: " + urlDepth.toString() + "\n");
	}


	/*
	* Статический метод, производящий парсинг по конкретной ссылке
	*/
	public static LinkedList<URLDepthPair> parsePage(URLDepthPair element) {
		// Список полученных ссылок в ходе парсинга
		LinkedList<URLDepthPair> listOfUrl = new LinkedList<URLDepthPair>();
		
		Socket socket = null;
			
		try {
			// Открываем сокет
			//System.out.println("Trying to connect to " + nowPage.getHostName());
			socket = new Socket(element.getHostName(), HTTP_PORT);
			//System.out.println("Connection to [ " + element.getURL() + " ] created!");

			// Установка таймаута
			try {
				socket.setSoTimeout(5000);
			}
			catch (SocketException exc) {
				System.err.println("SocketException: " + exc.getMessage());
				return null;
			}

			// Вывод информации о текущей странице
			//CrawlerHelper.getInfoAboutUrl(element.getURL(), true);

			// Для отправки запросов на сервер
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// Отправка запроса на получение html-страницы
			out.println("GET " + element.getPagePath() + " HTTP/1.1");
			out.println("Host: " + element.getHostName());
			out.println("Connection: close");
			out.println("");

			// Получение ответа
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// Проверка на bad request
			String line = in.readLine();

			if (line.startsWith(BAD_REQUEST_LINE)) {
				//System.out.println("ERROR: BAD REQUEST!");
				System.out.println(line + "\n");
				return null;
			}
			//System.out.println("REQUEST IS GOOD!\n");

			// Чтение основного файла
			//System.out.println("---Start of file---");

			// В цикле ниже происходит поиск и сбок всех ссылок со страницы
			// Для этого осуществляется просмотр всех строк html-кода страницы
			int strCount = 0;
			//int strCount2 = 0;
			while(line != null) {
				// На всякий случай обработка исключений, потому что bufferedReader может вполне выкинуть его
				try {
					/*
					* Вывод только строк с ссылками на http страницы
					* Или на подстраницы данного хоста
					* Или чего-то вроде ../url.html - это возврат назад и переход на другой уровень
					*/
						
					//Извлечнение строки из html-кода
					line = in.readLine();
					strCount += 1;
						
					// Извлечение ссылки из тэга, если она там есть, если нет, идём к следующей строке
					String url = CrawlerHelper.getURLFromHTMLTag(line);
					if (url == null) continue;
						
					// Если ссылка ведёт на сайт с протоколом https - пропускаем
					if (url.startsWith("https://")) {
						//System.out.println(strCount + " |  " + url + " --> https-refference\n");
						continue;
					}
						
					// Если ссылка - ссылка с возвратом
					if (url.startsWith("../")) {		
						String newUrl = CrawlerHelper.urlFromBackRef(element.getURL(), url);
						//System.out.println(strCount + " |  " + url + " --> " +  newUrl + "\n");
						Crawler.createURlDepthPairObject(newUrl, element.getDepth() + 1, listOfUrl);
					} 
						
					// Если это новая http ссылка
					else if (url.startsWith("http://")) {
						String newUrl = CrawlerHelper.cutTrashAfterFormat(url);
						//System.out.println(strCount + " |  " + url + " --> " + newUrl + "\n");
						Crawler.createURlDepthPairObject(newUrl, element.getDepth() + 1, listOfUrl);
					} 
						
					// Значит, это подкаталог, возможно у него будет мусор
					// Или содержит название файла в конце
                    // После очистки можно клеить с основной ссылкой
					else {		
						String newUrl;
						newUrl = CrawlerHelper.cutURLEndFormat(element.getURL()) + url;		
						//System.out.println(strCount + " |  " + url + " --> " + newUrl + "\n");
						Crawler.createURlDepthPairObject(newUrl, element.getDepth() + 1, listOfUrl);
					}
						
					//strCount2 += 1;
				}
				catch (Exception e) {
					break;
				}
			}
				
			if (strCount == 1) {
				System.out.println("No http refs in this page!");
				return null;
			}
			//System.out.println("---End of file---\n");
			//System.out.println("Page had been closed\n");
				
		}
		catch (UnknownHostException e) {
			System.out.println("Opps, UnknownHostException catched, so [" + element.getURL() + "] is not workable now!");
		}
		catch (IOException e) {
			e.printStackTrace();
		}
			
		return listOfUrl;
	}

	
	/*
	* Вывод в консоль результатов
	*/
	public static void showResults(URLDepthPair element, LinkedList<URLDepthPair> listOfUrl) {
		System.out.println("---Rezults of working---");
		System.out.println("Origin page: " + element.getURL());

		System.out.println("Pages that were founded:");
		int count = 1;
		for (URLDepthPair pair : listOfUrl) {
			System.out.println(count + " |  " + pair.toString());
			count += 1;
		}
		System.out.println("-----End of rezults-----");
		System.out.println("");
	}
	
}