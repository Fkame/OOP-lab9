import java.lang.Exception;
import java.util.*;
import java.net.MalformedURLException;
import java.net.*;
import java.io.*;

public class Crawler {

	public static final int HTTP_PORT = 80;
	public static final String HOOK_REF = "<a href=\"";
	public static final String HOOK_HTTP = "<a href=\"http://";
	public static final String HOOK_HTTPS = "<a href=\"https://";
	public static final String HOOK_BACK = "<a href=\"../";
	public static final String BAD_REQUEST_LINE = "HTTP/1.1 400 Bad Request";
	

	// ����� ��� ���஢���� ��� �����
	public static final String testURL = "http://users.cms.caltech.edu/~donnie/cs11/java/";
	//public static final String testURL = "http://users.cms.caltech.edu/~donnie/cs11/java/lectures/cs11-java-lec1.pdf";
	public static final int testDepth = 1;

	// ���᮪ ������� ᠩ⮢, � ��� ���������
	LinkedList<URLDepthPair> notVisitedList;
	LinkedList<URLDepthPair> visitedList;

	// ��㡨�� ���᪠
	int depth;

	// ���������
	public Crawler() {
		notVisitedList = new LinkedList<URLDepthPair>();
		visitedList = new LinkedList<URLDepthPair>();
	}


	// ��窠 �室�
	public static void main (String[] args) {

		Crawler crawler = new Crawler();

		crawler.getFirstURLDepthPair(args);
		crawler.startParse();
		crawler.showResults();
		//crawler.testParse();
	}


	/*
	* ��室 �� �ᥬ ᠩ⠬ �� ��।����� ��㡨��
	*/
	public void startParse() {
		System.out.println("Stating parsing:\n");

		URLDepthPair nowPage = notVisitedList.getFirst();

		while (nowPage.getDepth() <= depth && !notVisitedList.isEmpty()) {

			// �� �����筮��� �㦭� �⮡� 横� ࠡ�⠫ �ࠢ��쭮 - �᫨ �ந������ �訡�� - 横� �⪠���� ���砫�
			// ��⮬� �� ��� ����� �� �㤥� ����� ��᫠, ����� �㦭� 㤠���� ����� �� �訡��, � ����� ����
			// � ��� ࠧ ��� ����� ������� ����, � ���� 㤠����� �� ᯨ᪠
			// � ��� ��� �㦥�, �⮡� ���� � 横�
			
			//System.out.println("Trying to get next page in start of cycle");
			nowPage = notVisitedList.getFirst();
			
			Socket socket = null;
			
			try {
				// ���뢠�� ᮪��
				//System.out.println("Trying to connect to " + nowPage.getHostName());
				socket = new Socket(nowPage.getHostName(), HTTP_PORT);
				System.out.println("Connection to [ " + nowPage.getURL() + " ] created!");

				// ��⠭���� ⠩����
				try {
					socket.setSoTimeout(5000);
				}
				catch (SocketException exc) {
					System.err.println("SocketException: " + exc.getMessage());
					moveURLPair(nowPage, socket);
					continue;
				}

				// �뢮� ���ଠ樨 � ⥪�饩 ��࠭��
				CrawlerHelper.getInfoAboutUrl(nowPage.getURL(), true);

				// ��� ��ࠢ�� ����ᮢ �� �ࢥ�
				PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

				// ��ࠢ�� ����� �� ����祭�� html-��࠭���
				out.println("GET " + nowPage.getPagePath() + " HTTP/1.1");
				out.println("Host: " + nowPage.getHostName());
				out.println("Connection: close");
				out.println("");

				// ����祭�� �⢥�
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

				// �஢�ઠ �� bad request
				String line = in.readLine();

				if (line.startsWith(BAD_REQUEST_LINE)) {
					System.out.println("ERROR: BAD REQUEST!");
					System.out.println(line + "\n");

					this.moveURLPair(nowPage, socket);
					continue;
				} else {
					System.out.println("REQUEST IS GOOD!\n");
				}

				// �⥭�� �᭮����� 䠩��
				System.out.println("---Start of file---");

				// � 横�� ���� �ந�室�� ���� � ᡮ� ��� ��뫮� � ��࠭���
				// ��� �⮣� �����⢫���� ��ᬮ�� ��� ��ப html-���� ��࠭���
				int strCount = 0;
				int strCount2 = 0;
				while(line != null) {
					// �� ��直� ��砩 ��ࠡ�⪠ �᪫�祭��, ��⮬� �� bufferedReader ����� ������ �모���� ���
					try {
						/*
						* �뢮� ⮫쪮 ��ப � ��뫪��� �� http ��࠭���
						* ��� �� �����࠭��� ������� ���
						* ��� 祣�-� �த� ../url.html - �� ������ ����� � ���室 �� ��㣮� �஢���
						*/
						
						//�����筥��� ��ப� �� html-����
						line = in.readLine();
						strCount += 1;
						
						// �����祭�� ��뫪� �� ��, �᫨ ��� ⠬ ����, �᫨ ���, ��� � ᫥���饩 ��ப�
						String url = CrawlerHelper.getURLFromHTMLTag(line);
						if (url == null) continue;
						
						// �᫨ ��뫪� ����� �� ᠩ� � ��⮪���� https - �ய�᪠��
						if (url.startsWith("https://")) {
							System.out.println(strCount2 + " --> " + strCount + " |  " + url + " --> https-refference\n");
							continue;
						}
						
						// �᫨ ��뫪� - ��뫪� � �����⮬
						if (url.startsWith("../")) {		
							String newUrl = CrawlerHelper.urlFromBackRef(nowPage.getURL(), url);
							System.out.println(strCount2 + " --> " + strCount + " |  " + url + " --> " +  newUrl + "\n");
							this.createURlDepthPairObject(newUrl, nowPage.getDepth() + 1);
						} 
						
						// �᫨ �� ����� http ��뫪�
						else if (url.startsWith("http://")) {
							String newUrl = CrawlerHelper.cutTrashAfterFormat(url);
							System.out.println(strCount2 + " --> " + strCount + " |  " + url + " --> " + newUrl + "\n");
							this.createURlDepthPairObject(newUrl, nowPage.getDepth() + 1);
						} 
						
						// �����, �� �����⠫��, �������� � ���� �㤥� ����
						// ��� ᮤ�ন� �������� 䠩�� � ����
                        // ��᫥ ���⪨ ����� ������ � �᭮���� ��뫪��
						else {		
							String newUrl;
							newUrl = CrawlerHelper.cutURLEndFormat(nowPage.getURL()) + url;
							
							System.out.println(strCount2 + " --> " + strCount + " |  " + url + " --> " + newUrl + "\n");
							this.createURlDepthPairObject(newUrl, nowPage.getDepth() + 1);
						}
						
						strCount2 += 1;
					}
					catch (Exception e) {
						break;
					}
				}
				
				if (strCount == 1) System.out.println("No http refs in this page!");
				System.out.println("---End of file---\n");

				System.out.println("Page had been closed\n");
				
			}
			catch (UnknownHostException e) {
				System.out.println("Opps, UnknownHostException catched, so [" + nowPage.getURL() + "] is not workable now!");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			
			// ��६�饭�� ᠩ� ��᫥ ��ᬮ�� � ᯨ᮪ ��ᬮ�७���
			moveURLPair(nowPage, socket);
			
			// ��� ���� �����筮���, ��� �ࠢ�쭮� ࠡ��� 横�� � ��砥, ����� �� �������� �訡��
			nowPage = notVisitedList.getFirst();
		}
	}

	/*
	* ��ॢ�� ��࠭��� �� ᯨ᪠ ����ᬮ�७��� � ��ᬮ�७��
	* ��� ᯨ᪠ ࠡ���� �࠭�� ����� �� �६��� �� ����������
	*/
	private void moveURLPair(URLDepthPair pair, Socket socket) {
		this.visitedList.addLast(pair);
		this.notVisitedList.removeFirst();
		
		if (socket == null) return;
		
		try {
			// �����⨥ ᮪��
			socket.close();
		}
		catch (UnknownHostException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();	
		}
	}
	
	/*
	* ������� ���� ��ꥪ�-���� � �� ����室����� ��ॢ���� �� ������ ᯨ᪠ � ��㣮�
	* �᫨ ��।��� �㫥�� ��ࠬ��� false, � ����� socket ����� ��ࠢ���� null
	*/ 
	private void createURlDepthPairObject(String url, int depth) {
		
		URLDepthPair newURL = null;
		try{
			// ��ନ஢��� ������ ��ꥪ� � ���������� ��� � ᯨ᮪
			newURL = new URLDepthPair(url, depth);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		notVisitedList.addLast(newURL);
	}
	

	/*
	* ����祭�� ᯨ᪮�
	*/
	public LinkedList<URLDepthPair> getVisitedSites() {
		return this.visitedList;
	}

	public LinkedList<URLDepthPair> getNotVisitedSites() {
		return this.notVisitedList;
	}

	/*
	* �뢮� � ���᮫� १���⮢
	*/

	public void showResults() {
		System.out.println("---Rezults of working---");

		System.out.println("Scanner scanned next sites:");
		int count = 1;
		for (URLDepthPair pair : visitedList) {
			System.out.println(count + " |  " + pair.toString());
			count += 1;
		}

		System.out.println("");
		System.out.println("Not visited next sites (because of depth limit of some another reasons):");
		count = 1;
		for (URLDepthPair pair : notVisitedList) {
			System.out.println(count + " |  " + pair.toString());
			count += 1;
		}

		System.out.println("-----End of rezults-----");
	}



	/*
	* �஢�ઠ ��������� ��ப�, ����� ���짮��⥫� � ���������� ��ࢮ�� ��ꥪ� URLDepthPair
	* � ᯨ᮪ ����ᬮ�७���
	* �᫨ ��� ����� �� ��������� ��ப� ��।����� ���� null
	*/
	public void getFirstURLDepthPair(String[] args) {
		CrawlerHelper help = new CrawlerHelper();

		// �⥭�� ��㬥�⮢ �� ��������� ��ப�
		URLDepthPair urlDepth = help.getURLDepthPairFromArgs(args);
		if (urlDepth == null) {
			System.out.println("Args are empty or have exception. Now you need to enter URL and depth manually!\n");

			// ����祭�� ����� �� ���짮��⥫��
			urlDepth = help.getURLDepthPairFromInput();
		}

		// ����祭�� � ������ ��㡨��
		this.depth = urlDepth.getDepth();
		urlDepth.setDepth(0);

		// ����ᥭ�� � ᯨ᮪
		notVisitedList.add(urlDepth);

		// �뢮� ��ࢮ�� ��ꥪ� URLDepthPair
		System.out.println("First site: " + urlDepth.toString() + "\n");
	}


	/*
	* ���⮢� ��� ��� ࠧࠡ�⪨ �����⬠ ����祭�� � ��ࠡ�⪨ html-����
	* ���-���⨯ �᭮����� ����
	*/
	private void testParse() {

		//  ����ன�� ��砫쭮�� ����
		URLDepthPair pair;
		depth = testDepth;
		try {
			pair = new URLDepthPair(testURL, 0);
			notVisitedList.add(pair);
		}
		catch (IllegalArgumentException e) {
			System.out.println(e.getMessage());
			return;
		}
		catch (MalformedURLException e) {
			System.out.println(e.getMessage());
			return;
		}

		System.out.println("Start pair created!");

		// ��ନ஢���� ��ꥪ� URL �� ��ப�, ᮤ�ঠ饩 URL
		URL url = null;
		try {
			url = new URL(pair.getURL());
		}
		catch (MalformedURLException e) {
			System.err.println("MalformedURLException: " + e.getMessage());
			return;
		}

		System.out.println("URL formed");

		// ����⨥ ������祭��
		try {
			Socket socket = new Socket(url.getHost(), HTTP_PORT);
			System.out.println("Connection to [ " + url + " ] created!");

			CrawlerHelper.getInfoAboutUrl(url, true);

			// ��� ��ࠢ�� ����ᮢ �� �ࢥ�
			PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

			// ��ࠢ�� ����� �� ����祭�� html-��࠭���
			out.println("GET " + url.getPath() + " HTTP/1.1");
			out.println("Host: " + url.getHost());
			out.println("Connection: close");
			out.println("");

			// ����祭�� �⢥�
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			// �஢�ઠ �� bad request
			String line = in.readLine();
			if (line.startsWith(BAD_REQUEST_LINE)) {
				System.out.println("ERROR: BAD REQUEST!");
				System.out.println(line + "\n");
			} else {
				System.out.println("REQUEST IS GOOD!\n");
			}

			// �⥭�� �᭮����� 䠩��
			System.out.println("---Start of file---");
			//System.out.println(line);
			int strCount = 1;
			while(line != null) {
				try {

					/*
					* �뢮� ⮫쪮 ��ப � ��뫪��� �� http ��࠭���
					* ��� �� �����࠭��� ������� ���, ����騥 http ��⮪��
					*/
					line = in.readLine();

					if (line.indexOf(HOOK_HTTP) != -1)
						System.out.println(strCount + " |  " + line);
					else if (line.indexOf(HOOK_REF) != -1)
					{
						int indexStart = line.indexOf(HOOK_REF) + HOOK_REF.length();
						int indexEnd = line.indexOf("\"", indexStart);
						String subRef = line.substring(indexStart, indexEnd);

						// ����祭��� ��뫪�, ᪮॥ �ᥣ� �����⠫��, �㦭� ��ꥤ����� � �।��騬 ��⥬,
						// �८�ࠧ����� ����祭��� ��ப� � url, � �஢���� ��⮪��
						// ��� �� �㦭�, �� ���� �����⠫�� ���-⠪�, ����� ��⮪�� ������ ᮢ������
						String fullSubRef = url + subRef;
						URL newUrl = URLDepthPair.getUrlObjectFromUrlString(fullSubRef);
						String newUrlProtocol = newUrl.getProtocol();

						System.out.println(strCount + " |  " + line + " --> [" + subRef + "] --> " + indexStart + ", " + indexEnd);
						System.out.println("Full ref = " + newUrl.toString() + ", protocol = " + newUrlProtocol + "\n");
					}
					strCount += 1;
				}
				catch (Exception e) {
					break;
				}
			}
			if (strCount == 1) System.out.println("No http refs in this page!");
			System.out.println("---End of file---\n");
		}
		catch (UnknownHostException e) {
            System.err.println("Don't know about host " + pair.getHostName());
			return;
		}
		catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + pair.getHostName());
            return;
        }
	}
}