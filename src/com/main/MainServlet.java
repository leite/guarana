/*
*    
*   TODO:
*   
*  1 ) be asyncronous ( fetch url / memcache / mysql use Future ) *UPDATE no needed to be async at all
*  2 ) be wise and modular ( use task queue, enable billing to handle 20M api calls ) *UPDATE no billing needed
*  3 ) be fast use threads *UPDATE fine!
* 
*/
package com.main;

// Utils
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Servlet
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest; 
import javax.servlet.http.HttpServletResponse;

//Appengine low api
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

// commom 
import com.main.Sets;
import appengine.MemCache;

@SuppressWarnings("serial")
public final class MainServlet extends HttpServlet {
  
  private static Sets sets;
  private Vars vars;
  private MemCache cache;
  private HttpServletResponse resp;
  private Pattern clientPattern;
  private Pattern ipPattern;
  
  //private ServletContext context;
  
  public void init(ServletConfig config) throws ServletException {  
    sets          = new Sets(getServletConfig());
    cache         = new MemCache();
    clientPattern = Pattern.compile("([^\\d?$?]*)(\\d?[^$]*)$");
    ipPattern     = Pattern.compile("[.?:]");
    super.init(config);
  }
  
  // we only support get
  @SuppressWarnings("unchecked")
  public final void doGet(HttpServletRequest rq, HttpServletResponse rs) {
    
    vars = new Vars(rq.getParameterMap());
    resp = rs;
    
    StringBuilder content = null, update = null;
    ServletOutputStream out = null;
    
    try {
      
      content = new StringBuilder();
      update  = new StringBuilder();
      out     = rs.getOutputStream();
      
      rs.setContentType("text/plain");
      rs.setCharacterEncoding("UTF-8");
      
      // DEBUG purposes
      if( sets.debug && ( vars.dump || vars.truncate ) ) {
        
        // dump memory
        if( vars.dump )
        
        // truncate memory
        if( vars.truncate )
        
        //
        out.println("debug / truncated / dump report\n");
      }
      
      //get, ping, update request 
      if ( vars.bFile || vars.get || vars.update || vars.ping ) {
        
        // check if has network and valid it
        if( vars.net == null ) {
          sendError("No Network"); return;
        } else {
          if( !validateNet(vars.net) ) { sendError("Unsupported network"); return; }
        }
      
        // check if has client
        if( vars.client == null ) { sendError("No Client"); return; }  
        
        // check (old gnutella2 clients send clientName clientVersion together)
        checkClientAndVersion();
        
        if( vars.ping )
          content.append( vars.getNetworks ? doPong(true) + listNets(): doPong(false) );
        else
          if( vars.getNetworks ) content.append( listNets() ); 
        
        if( vars.update ) {
          
          if( vars.ip != null ) {
            
            //if ( rq.getRemoteAddr() != vars.ip.substring( 0, vars.ip.indexOf(":") ) && !sets.debug ) {
            //  sendError( "Query IP doesn't match client IP" ); return;
            //}
            
            Long[] validIp;
              
            if ((validIp = validateIp(vars.ip)) == null) {
              update.append( "i|update|WARNING|Invalid Ip\n" );
            }else{
              
              if (isTooEarly(validIp[0])) {
                update.append("i|update|WARNING|Returned too soon\n");
              } else {
                
                //POSSIBLY RAZA 2.2.5.6 BUG!
                if (vars.xLeaves>vars.xMax) {
                  update.append("i|update|WARNING|Bad host\n");
                }else{
                  pushHostToQueue(validIp[0], validIp[1]);
                  update.append("i|update|OK\ni|update|period|"+ sets.hostExpirationTime +"\n");
                }
              }
            }
            
          }
          
          if (vars.url != null) {
            
            String validUrl;
            
            if ((validUrl = validateUrl(vars.url)).isEmpty()) {
              update.append("i|update|WARNING|\n");
            } else {
              
              GnutellaUrlInfo info = new GnutellaUrlInfo();
              
              if(info.getUrl(validUrl, vars.net, sets.cacheVendor, sets.cacheVersion)) {
                
                // push information to update queue
                pushUrlToQueue(info);
                update.append("i|update|OK\ni|update|period|"+ sets.urlExpirationTime +"\n");
              } else {
                
                // oh man, thats bad
                update.append("i|update|WARNING|Bad Cache\n");
              }
            }
          }
        
        }  
        
        // everything is fine
        rs.setStatus(200);
        
        if(vars.get || vars.update){
        	update.append("i|access|period|"+ sets.accessWait + "\n");
        }
        
        out.print(content.toString());
        out.print(getContentFromCache());
        out.print(update.toString());
        
        content.setLength(0);
        update.setLength(0);
        return;
      }
      
    } catch (Exception ex) {
      new logger.LogManager().logExc(ex);
    } finally {
      vars    = null;
      content = null;
      update  = null;
      out     = null;
    }
  }
  
  // pong
  private final String doPong(boolean withNets){
    return "i|pong|" + sets.cacheName + " " + sets.cacheVersion + ((withNets)? '|'+ join(sets.supportedNetworks, "-") : "") + "\n";
  }
        
  // list supported networks
  private final String listNets(){
    return "i|networks|" + join(sets.supportedNetworks, "|") + "\ni|nets|"+ join(sets.supportedNetworks, "-") +"\n";
  }  
  
  // validate ip range and port
  private final Long[] validateIp(String host) {
    try {
    
      if(host == null || host == "")
        return null;
      
      String[] ret = ipPattern.split(host);
      
      if (ret==null || ret.length!=5)
        return null;
      
      long ip, e, a, b, c, d;
      
      a = Long.parseLong(ret[0]);
      b = Long.parseLong(ret[1]);
      c = Long.parseLong(ret[2]);
      d = Long.parseLong(ret[3]);
      e = Long.parseLong(ret[4]);
      
      if (a>255 || b>255 || c>255 || d>255 || e>65555) 
        return null;
      
      ip = (16777216 * a) + (65536 * b) + (256 * c) + d;
      
      if(ip > 0L && 16777215L > ip || ip > 167772160L && 184549375L > ip || ip > 2130706432L && 2147483647L > ip || ip > 2851995648L && 2852061183L > ip ||
      ip > 2886729728L && 2887778303L > ip || ip > 3221225984L && 3221226239L > ip || ip > 3227017984L && 3227018239L > ip ||
      ip > 3232235520L && 3232301055L > ip || ip > 3323068416L && 3323199487L > ip || ip > 3325256704L && 3325256959L > ip ||
      ip > 3405803776L && 3405804031L > ip || ip > 3758096384L && 4026531839L > ip || ip > 4026531840L && 4294967295L > ip)
        return null;
      
      Long[] valid = { ip, e };
      
      return valid;
          
    } catch (Exception ex) {
      return null;
    }
  }
  
  //validate Url
  private final String validateUrl(String url) {
    
    if(url.isEmpty()) { return ""; }
    
    String pattern = "\\b(http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
    if(!url.matches(pattern)) { return ""; }
    
    Iterator<String> it = sets.urlBlacklist.iterator();
    while (it.hasNext()) { 
      if(url.indexOf(it.next()) > -1)
        return ""; 
    } 
    
    return url.toLowerCase().replaceAll("\\/(default|index)\\.(aspx|php|cgi|cfm|asp|pl|lp|jsp|js)", "");
  }
  
  // get content from memcache
  private final String getContentFromCache() {
    
    String cacheContent = null;
    if (vars.bFile || vars.get || (vars.hostFile && vars.urlFile)) { // - leaves - vendors - uptime
      cacheContent = cache.get("url_ip" + (vars.getLeaves ? "_leaves":"") + (vars.getVendors ? "_vendors" : "") + (vars.getUptime ? "_uptime" : "")); 
    } else if (vars.urlFile || vars.gwcs) {
      cacheContent = cache.get("url");
    } else if (vars.hostFile || vars.showHosts) {
      cacheContent = cache.get("ip" + (vars.getLeaves ? "_leaves":"") + (vars.getVendors ? "_vendors" : "") + (vars.getUptime ? "_uptime" : ""));
    }
    return cacheContent==null ? "" : cacheContent;
  }
  
  // push host to update queue
  private void pushUrlToQueue(GnutellaUrlInfo info) {
    
    StringBuilder payload = new StringBuilder();
    Queue queue           = QueueFactory.getQueue("update");
    
    // url|url (address)|cacheName|cacheVersion|clientNick|clientVersion|rank|timeStamp|urlCount|ipCount|g1|g2
    //
    // url|http://cache.leite.us/|GUAR|0.3|RAZA|2.5.5.3|10|123232323|40|80|false|true
    
    payload.append("url");                              payload.append("|");
    payload.append(info.getUrl());                      payload.append("|");
    payload.append(info.getCacheName());                payload.append("|");
    payload.append(info.getCacheVersion());             payload.append("|");
    payload.append(vars.client);                        payload.append("|");
    payload.append(vars.version);                       payload.append("|");
    payload.append(info.getRank());                     payload.append("|");
    payload.append(vars.timeStamp);                     payload.append("|");
    payload.append(info.getUrlCount());                 payload.append("|");
    payload.append(info.getIpCount());                  payload.append("|");
    payload.append((info.isG1() ? "true" : "false"));   payload.append("|");
    payload.append((info.isG2() ? "true" : "false"));   
    
    queue.add(TaskOptions.Builder.withMethod(Method.PULL).payload(payload.toString()));
    
    payload.setLength(0);
    payload = null;
    queue   = null;
  }
  
  // push host to update queue
  private void pushHostToQueue(long ip, long port) {
    
    StringBuilder payload = new StringBuilder();
    Queue queue           = QueueFactory.getQueue("update");
    
    // host|timeStamp|ip|port|clientNick|clientVersion|hostUptime|totalLeaves|maxLeaves
    //
    // host|191919191|129090909|6346|RAZA|2.5.8.0|121999292|200|300
    
    payload.append("host");         payload.append("|");
    payload.append(vars.timeStamp); payload.append("|");
    payload.append(ip);             payload.append("|");
    payload.append(port);           payload.append("|");
    payload.append(vars.client);    payload.append("|");
    payload.append(vars.version);   payload.append("|");
    payload.append(vars.uptime);    payload.append("|");
    payload.append(vars.xLeaves);   payload.append("|");
    payload.append(vars.xMax); 
    
    queue.add(TaskOptions.Builder.withMethod(Method.PULL).payload(payload.toString()));
    
    payload.setLength(0);
    payload = null;
    queue   = null;
  }
  
  // access control to avoid ddos
  private final boolean isTooEarly(Long ip){
    
    if (cache.increaseKey(ip, sets.accessWait) > 2) { return true; }
    return false;
  }
  
  // send error
  private final void sendError(String message) { 
    
    System.out.println("sendError: "+ message);
    
    try {
      
      this.resp.setStatus(200);
      this.resp.getOutputStream().print(message);
    } catch (IOException ex) {
      System.err.println(ex.getMessage());
    }
  }
    
  // validate network against supported networks
  private final boolean validateNet(String net) {
    
    if(net == null) 
      return false;
      
    if(sets.supportedNetworks.contains(net.toLowerCase())) 
      return true;
    
    return false;
  }
  
  //
  private final void checkClientAndVersion() {
    // client match
    Matcher clientMatch = clientPattern.matcher(vars.client);
    // assert search
    if(!clientMatch.find())         { return; }
    if(clientMatch.groupCount()!=2) { return; }
    // version match
    if(clientMatch.group(2).isEmpty() && vars.version!=null) {
      Matcher versionMatch = clientPattern.matcher(vars.version);
      // assert search
      if(!versionMatch.find())         { return; }
      if(versionMatch.groupCount()!=2) { return; }
      if(clientMatch.group(1).equals("TEST") && !versionMatch.group(1).isEmpty()){
        vars.client  = clientMatch.group(1) + versionMatch.group(1);
        vars.version = versionMatch.group(2);
      } else {
        vars.client  = clientMatch.group(1);
        vars.version = versionMatch.group(2);
      }
      versionMatch = null;
    } else {
      vars.client  = clientMatch.group(1);
      vars.version = clientMatch.group(2);
    }
    clientMatch = null;
  }
  
  private final static String join(Set<String> s, String delimiter) {
    if (s == null || s.isEmpty()) return "";
    Iterator<String> iter = s.iterator();
    StringBuilder builder = new StringBuilder(iter.next());
    while( iter.hasNext() ) {
      builder.append(delimiter).append(iter.next());
    }
    return builder.toString();
  }
  
  // 405 will be precise but 404 is more interesting here
  public final void doPost(HttpServletRequest req, HttpServletResponse resp) { resp.setStatus(404); }
  
  // 405 will be precise but 404 is more interesting here
  public final void doHead(HttpServletRequest req, HttpServletResponse resp) { resp.setStatus(404); }
  
}
