package com.eulerity.hackathon.imagefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;

// Class to implement Crawling logic using multiple methods. getAllUrls functions recursively. findImageUrls functions using Queues and multi threading. 
public class WebCrawler {
	//Initial Variables
	private HashSet<String> urls; 
	private ArrayList<String> urlList;
	private HashSet<String> imageURLS;
	private ArrayList<String> imgList; 
	Queue<String> urlQueue = new LinkedList<>();
	Queue<String> imgQueue = new LinkedList<>();
	Set<String> urlSet = new HashSet<>();
	Set<String> imgSet = new HashSet<>();
	int threads = 1;
	
	private static final int MAX_iter = 1; //Max Depth to go to
	
	public WebCrawler() { //Constructor for Webcrawler class 
		urls = new HashSet<String>(); //HashSet with all the links
		urlList = new ArrayList<String>();
		imageURLS= new HashSet<String>(); //HashSet to hold all image links
		imgList = new ArrayList<String>();
		
		
	}
	// Method to find the Page and Image Links associated with a starting page-Basic Web Crawl Recursion
	public void getAllUrls(String URL, int iter) throws URISyntaxException
	{
		if(!urls.contains(URL) && (iter <= MAX_iter))
		{//Adds URLS to HashSet and output for check
			if(urls.add(URL))
				//System.out.println("Iteration: " + iter + " URL: " + URL);
			try {
				//Methodology to get URL and Img info
				Document document = Jsoup.connect(URL).ignoreContentType(true).get();
				Elements link = document.getElementsByAttributeValueContaining("abs:href", getDomainName(URL));
				Elements image = document.select("img");
				//System.out.println("Domain Name: " + getDomainName(URL));
				iter++;
				//Loop to iterate through each link with the same Domain name, 
				//I may convert to a queue system to implement Multi-Threading
				for(Element check : link) {
					for(Element img: image) {
						if(!imageURLS.contains(img.attr("src")))
						{
							imageURLS.add(img.attr("src"));
							//System.out.println("Image Source: " + img.attr("src"));
						}
					}
					getAllUrls(check.attr("abs:href"), iter);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				System.err.println("For " + URL + ": " + e.getMessage());
			}
			imgList = new ArrayList<>(imageURLS);
			urlList = new ArrayList<>(urls);
			
		}
	}
	//Method to return ArrayList for URls for incorporating multi threading using Queues 
		public ArrayList<String> getPageUrls(String URL) throws URISyntaxException
		{
			if(!urls.contains(URL))
			{
				try {
					//Methodology to get URL and Img info
					Document document = Jsoup.connect(URL).ignoreContentType(true).get();
					Elements link = document.getElementsByAttributeValueContaining("abs:href", getDomainName(URL));
					Elements image = document.select("img");
					
					//Loop to iterate through each link with the same Domain name
					for(Element check : link) {
						for(Element img: image) {
							if(!imageURLS.contains(img.attr("src"))) {	
							imageURLS.add(img.attr("src"));
							//System.out.println("Image Source: " + img.attr("src"));
						}
						urls.add(check.attr("abs:href"));
					}
					}
					}
				 catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println("For " + URL + ": " + e.getMessage());
				}
				imgList = new ArrayList<>(imageURLS);
				 urlList = new ArrayList<>(urls);
				 
		}
			return urlList;

		}
		public ArrayList<String> getPageImg(String URL) throws URISyntaxException
		{
			
			try {
				
				Document document = Jsoup.connect(URL).ignoreContentType(true).get();
				Elements image = document.select("img");
				for(Element img: image) {
					if(!imageURLS.contains(img.attr("src"))) {	
					imageURLS.add(img.attr("src"));
					}
				}
			}
				catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

				imgList = new ArrayList<>(imageURLS);
				

				return imgList;
			
			}
			

	//Code to run Multi-thread web crawl 
	public Set<String> findImageURLS(String URL) throws URISyntaxException {
	  
	    urlQueue.addAll(getPageUrls(URL));
	    imgQueue.addAll(getPageImg(URL));
	    crawl();
		    return imgSet;
	        }
	
	//Optimized Multi-Thread Web Crawl Logic 
	public void crawl() throws URISyntaxException {
         OUTER_LOOP: while(true) {
            String nextUrl;
            synchronized(this) {
                while(urlQueue.isEmpty()) {
                	System.out.println("is Empty");
                	if(threads == 0 || urlQueue.isEmpty())
                	{
                		break OUTER_LOOP;
                	}
                    try {
                        wait();   
                    } catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                nextUrl = urlQueue.poll();
                threads++;
            }
            List<String> URLs = getPageUrls(nextUrl);
            List<String> IMGs = getPageImg(nextUrl);
            synchronized(this) {
                for(String newUrl: URLs) {
                    if(!urlSet.contains(newUrl)) {
                    	//System.out.println(newUrl); //Check 
                        urlQueue.offer(newUrl);
                        urlSet.add(newUrl);
                    }
                for(String img: IMGs){    	        		
    	        	if (!imgSet.contains(img)) {
    	        		//System.out.println(img);//Check 
    	        		imgSet.add(img);
    	 	            imgQueue.offer(img);
    	        	}
                }
                threads--;
                notifyAll();
            }
        }
         }
    }
  	    

	
	//Code to return domain name to stay in domain url. 
	public static String getDomainName(String url) throws URISyntaxException {
	    URI uri = new URI(url);
	    String domain = uri.getHost();
	    return domain.startsWith("www.") ? domain.substring(4) : domain;
	}
	
	
	
	public static void main(String[] args) throws URISyntaxException {
		WebCrawler x = new WebCrawler();
		Set<String> test = x.findImageURLS("https://blog.hubspot.com/website/what-is-utf-8");
		//x.getAllUrls("https://blog.hubspot.com/website/what-is-utf-8", 0);
		//ArrayList<String> test = x.imageList();
		
		for(Object o:test)
		{
			System.out.println(o);
		}
		
	}
	
}
