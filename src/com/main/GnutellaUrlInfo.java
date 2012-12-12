package com.main;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
  public int getUrlCount()        { return urlCount; }
  public int getIpCount()         { return ipCount; }
  public boolean isG1()           { return g1; }
  public boolean isG2()           { return g2; }
  public long getiAccess()        { return iAccess; }
  public String getCacheName()    { return cacheName; }
  public String getCacheVersion() { return cacheVersion; }
  public int getRank()            { return rank; }
  public String getUrl()          { return url; }
  public String getNetwork()      { return network; }
  
  //
  @SuppressWarnings("deprecation")
  public boolean getUrl(String url, String network, String cacheVendor, String cacheVersion) {
    
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
      
      
      _url = new URL(url + "?client="+ cacheVendor +"&version=" + URLEncoder.encode(cacheVendor + " " + cacheVersion) + "&getnetworks=1&cache=1&net=" + URLEncoder.encode(network) + "&ping=1&get=1&hostfile=1&urlfile=1");
      
      System.out.println("call to ... " + url + "?client="+ cacheVendor +"&version=" + URLEncoder.encode(cacheVendor + " " + cacheVersion) + "&getnetworks=1&cache=1&net=" + URLEncoder.encode(network) + "&ping=1&get=1&hostfile=1&urlfile=1");
      
      // request settings, allow truncated and no redirects please
      request = new HTTPRequest( _url , HTTPMethod.GET, Builder.allowTruncate().doNotFollowRedirects());
      service = URLFetchServiceFactory.getURLFetchService();
      
      response = service.fetch(request);
      
      if ( response.getResponseCode() != 200 ) {
        System.out.println(url);
        System.out.println(response.getResponseCode());
        System.out.println(new String(response.getContent(), "UTF-8").toLowerCase());
        return false; 
      }
      
      // convert bytes to string
      html = new String(response.getContent(), "UTF-8").toLowerCase();
      this.url = url;

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
          
          // should parse
          // i|pong|skulls 0.2.8|gnutella-gnutella2|1|tcp 
          // i|pong|boa|gnutella2
          // i|pong|DKAC/Enticing-Enumon 
          // i|pong|Guarana 0.2|gnutella2"
          Pattern identify = Pattern.compile("i\\|pong\\|([^\\s?\\|$?]*)\\s?([^\\|?$?]*)\\|?$?([^\\|?$?]*)\\|?$?", Pattern.DOTALL);
          Matcher matched  = identify.matcher(lines[i]);
          
          if(matched.find()) {
            cacheName    = matched.group(1);
            cacheVersion = matched.group(2) == "" ? "0" : matched.group(2);
            if(matched.group(3) != "") {
              netList = matched.group(3).split("\\-");
            }
          }
        }
      }
      //
      rank += urlCount < 4 ? 0 : (urlCount > 10 ? 10 : 5);
      rank += ipCount < 5  ? 0 : (ipCount  > 20 ? 10 : 5);
      rank += (netList == null) ? 0 : (netList.length > 0 ? 10: 0);
      rank += iAccess > 3000 ? 10 : (iAccess > 1000 ? 8 : 0 );
      rank += cacheName.length() > 2 ? 10 : 0;
      //
      rank = rank / 5;
      //
      if(netList == null) {
        g2 = true;
      } else {
        len = netList.length;
        for ( i=0; i<len; ++i ) {
          if( netList[i].equals("gnutella") )  { g1 = true; }
          if( netList[i].equals("gnutella2") ) { g2 = true; }
        } 
      }
      
      return true;
    } catch ( IOException ex ) {  
      
      new logger.LogManager().logExc(ex);
      return false;
    } finally {
      
      request  = null;
      response = null;
      service  = null;
      _url     = null;
      html     = null;
      lines    = null;
    }
    
  }
  
}
