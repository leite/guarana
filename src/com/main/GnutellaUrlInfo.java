package com.main;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.urlfetch.FetchOptions.Builder;

public class GnutellaUrlInfo {
	
	// private
	private String url;
	private String network;
	private int urlCount;
	private int ipCount;
	private boolean g1;
	private boolean g2;
	private long iAccess;
	private String cacheName;
	private String cacheVersion;
	private int rank;

	// getters
	public int getUrlCount() { return urlCount; }
	public int getIpCount() { return ipCount; }
	public boolean isG1() { return g1; }
	public boolean isG2() { return g2; }
	public long getiAccess() { return iAccess; }
	public String getCacheName() { return cacheName; }
	public String getCacheVersion() { return cacheVersion; }
	public int getRank() { return rank; }
	public String getUrl() { return url; }
	public String getNetwork() { return network; }
	
	//
	@SuppressWarnings("deprecation")
	public boolean getUrl( String url, String network, String cacheVendor, String cacheVersion ) {
		
		HTTPRequest request     = null;
		HTTPResponse response   = null;
		URLFetchService service = null;
		URL _url                = null;
		String html             = null;
		String[] lines          = null;
		String[] netList        = null;
		int i                   = 0;
		int len                 = 0;
		
		try {
			
			url = url + "?client=RAZA&version=" + URLEncoder.encode( cacheVendor + " " + cacheVersion ) + "&getnetworks=1&cache=1&net=" + URLEncoder.encode(network) + "&ping=1&get=1&hostfile=1&urlfile=1";
			_url = new URL(url);
			
			// request settings, allow truncated and no redirects please
			request = new HTTPRequest( _url , HTTPMethod.GET, Builder.allowTruncate().doNotFollowRedirects());
			service = URLFetchServiceFactory.getURLFetchService();
			
			response = service.fetch(request);
			
			if ( response.getResponseCode() != 200 ) { /* you can debug here */ return false; }
			
			// convert bytes to string
			html = new String(response.getContent(), "UTF-8").toLowerCase();
			// basic errors
			if( html.indexOf("ERROR")>-1 ){ return false; }
			if( html.indexOf(">")>-1 ){ return false; }
			//
			lines = html.split("\\r?\\n");
			len = lines.length;
			//
			for( i=0; i<len; ++i ) {
				if (lines[i].indexOf("u|")==0) {
					++urlCount;
				}else if (lines[i].indexOf("h|")==0) {
					++ipCount;
				}else if (lines[i].indexOf("i|networks")==0) {
					netList = lines[i].substring(11, lines[i].length()).split("\\|");
				}else if (lines[i].indexOf("i|access")==0) {
					iAccess = Long.parseLong(lines[i].substring(16, lines[i].length() ).replace("\r",""));
				}else if (lines[i].indexOf("i|pong")==0){
					
					cacheName= lines[i].substring( 7, lines[i].length() );
					cacheName= cacheName.substring( 0, cacheName.indexOf("|") );
					
					if( cacheName.indexOf(" ")>-1 ){
						cacheVersion= cacheName.substring( cacheName.lastIndexOf(" ")+1, cacheName.length() );
						cacheName = cacheName.substring( 0, cacheName.lastIndexOf(" ") );
					}else{
						cacheVersion = "0";
					}
				}
			}
			//
			rank += urlCount<4? 0:(urlCount>10? 10:5);
			rank += ipCount<5? 0:(ipCount>20? 10:5);
			rank += netList.length>0? 10:0;
			rank += iAccess>3000? 10:(iAccess>1000? 8:0);
			rank += cacheName.length()>2? 10:0;
			//
			rank = rank/5;
			//
			len = netList.length;
			for ( i=0; i<len; ++i ) {
				if( netList[i].equals("gnutella") )  { g1 = true; }
				if( netList[i].equals("gnutella2") ) { g2 = true; }
			}
			return true;
		} catch ( IOException ex ) {	
			
			ex.printStackTrace(); return false;
		} finally {
			
			request = null;
			response = null;
			service = null;
			_url = null;
			html = null;
			lines = null;
		}
		
	}
	
}
