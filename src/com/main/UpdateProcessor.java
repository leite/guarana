/*
 * TODO:
 * 
 * 1) call it each 15 minutes
 * 2) read all pulled updates in queue
 * 3) resolve duplicates ?
 * 4) update in Sql Cloud / Datastore
 * 5) retrieve all data from Sql Cloud / Datastore and put into memcache for fast access
 * 
 */

package com.main;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.serializers.CollectionSerializer;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.esotericsoftware.kryo.io.Output;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskHandle;

@SuppressWarnings("serial")
public class UpdateProcessor extends HttpServlet  {
  
  //private CacheList caches;
  //private HostList hosts;
  private DataHolder data;
  DatastoreService ds;
  long hostMaxTime;
  long cacheMaxTime;
  long time;
  
  public UpdateProcessor() {
    
    //
    
  }
  
  public void init(ServletConfig config) throws ServletException { 
    time = Math.round((new Date()).getTime() / 1000); 
    hostMaxTime  = (time - Long.parseLong(getServletConfig().getInitParameter("hostExpirationTime")));
    cacheMaxTime = (time - Long.parseLong(getServletConfig().getInitParameter("urlExpirationTime")));
    super.init(config);
  }

  public final void doGet(HttpServletRequest rq, HttpServletResponse rs) {
    
    Queue queue            = null;
    List<TaskHandle> tasks = null;
    Host tmpHost           = null;
    Cache tmpCache         = null;
    String slicedPayload[] = null;
    boolean found          = false;
    int newHosts           = 0;
    int newCaches          = 0;
    int updatedCaches      = 0;
    int updatedHosts       = 0;
    int deletedHosts       = 0;
    int i                  = 0;
    int hostsSize          = 0;
    int cachesSize         = 0;
    long hostIp            = 0;
    
    //String[] hostsToCache  = ;
    //String[] cachesToCache = ;

    try {
    
      // host|timeStamp|ip|port|clientNick|clientVersion|hostUptime|totalLeaves|maxLeaves
      // --------------------------------------------------------------------------------------------------------
      // host|191919191|129090909|6346|RAZA|2.5.8.0|121999292|200|300
      
      // url|url (address)|cacheName|cacheVersion|clientNick|clientVersion|rank|timeStamp|urlCount|ipCount|g1|g2
      // --------------------------------------------------------------------------------------------------------
      // url|http://cache.leite.us/|GUAR|0.3|RAZA|2.5.5.3|10|123232323|40|80|false|true
      
      ds    = DatastoreServiceFactory.getDatastoreService();
      queue = QueueFactory.getQueue("update");
      
      loadData();
      
      while(!((tasks = queue.leaseTasks(30, TimeUnit.SECONDS, 5)).isEmpty())){
        
        for (TaskHandle leasedTask : tasks) {
          
          slicedPayload = (new String(leasedTask.getPayload())).split("\\|");
          
          if(slicedPayload[0] == "host") {
            
            hostsSize = data.hostsIndex.length;
            hostIp    = Long.parseLong(slicedPayload[2]);
            
            for(i=0; i<hostsSize; ++i) {
              if(data.hostsIndex[i] == hostIp) {
                data.hosts.get(i).timeStamp   = Long.parseLong(slicedPayload[1]);
                data.hosts.get(i).port        = Long.parseLong(slicedPayload[3]);
                data.hosts.get(i).uptime      = Long.parseLong(slicedPayload[6]);
                data.hosts.get(i).totalLeaves = Long.parseLong(slicedPayload[7]);
                data.hosts.get(i).maxLeaves   = Long.parseLong(slicedPayload[8]);
                found = true;
                break;
              }
            }
            
            if(!found) {
              tmpHost = new Host();
              tmpHost.timeStamp     = Long.parseLong(slicedPayload[1]);
              tmpHost.ip            = hostIp;
              tmpHost.port          = Long.parseLong(slicedPayload[3]);
              tmpHost.clientNick    = slicedPayload[4];
              tmpHost.clientVersion = slicedPayload[5];
              tmpHost.uptime        = Long.parseLong(slicedPayload[6]);
              tmpHost.totalLeaves   = Long.parseLong(slicedPayload[7]);
              tmpHost.maxLeaves     = Long.parseLong(slicedPayload[8]);
              data.hosts.add(tmpHost);
            }
          } else {
            
            cachesSize = data.cachesIndex.length;

            for(i=0; i<cachesSize; ++i) {
              if(data.cachesIndex[i] == slicedPayload[1]) {
                data.caches.get(i).cacheVersion  = slicedPayload[2];
                data.caches.get(i).cacheVersion  = slicedPayload[3];
                data.caches.get(i).clientNick    = slicedPayload[4];
                data.caches.get(i).clientVersion = slicedPayload[5];
                data.caches.get(i).rank          = Integer.parseInt(slicedPayload[6]);
                data.caches.get(i).timeStamp     = Long.parseLong(slicedPayload[7]);
                data.caches.get(i).urlCount      = Integer.parseInt(slicedPayload[8]);
                data.caches.get(i).ipCount       = Integer.parseInt(slicedPayload[9]);
                data.caches.get(i).g1            = Boolean.parseBoolean(slicedPayload[10]);
                data.caches.get(i).g2            = Boolean.parseBoolean(slicedPayload[11]);
                found = true;
                break;
              }
            }

            if(!found) {
              tmpCache = new Cache();
              tmpCache.url           = slicedPayload[1];
              tmpCache.cacheVersion  = slicedPayload[2];
              tmpCache.cacheVersion  = slicedPayload[3];
              tmpCache.clientNick    = slicedPayload[4];
              tmpCache.clientVersion = slicedPayload[5];
              tmpCache.rank          = Integer.parseInt(slicedPayload[6]);
              tmpCache.timeStamp     = Long.parseLong(slicedPayload[7]);
              tmpCache.firstSeen     = tmpCache.timeStamp;
              tmpCache.urlCount      = Integer.parseInt(slicedPayload[8]);
              tmpCache.ipCount       = Integer.parseInt(slicedPayload[9]);
              tmpCache.g1            = Boolean.parseBoolean(slicedPayload[10]);
              tmpCache.g2            = Boolean.parseBoolean(slicedPayload[11]);
              data.caches.add(tmpCache);
            }
          }

          queue.deleteTask(leasedTask.getName());
          found         = false;
          slicedPayload = null;
        }
      }

      //
      long [] newHostIndex         = new long[data.hosts.size()];
      StringBuilder hostsToCache[] = new StringBuilder[32];
      
      hostsToCache[0] = new StringBuilder();
      
      
      // generate index, cache and clean old objects
      for(i=0; i<data.hosts.size(); ++i) {
        if(data.hosts.get(i).timeStamp<hostMaxTime){
          data.hosts.remove(i);
          --i;
        } else {
          newHostIndex[i] = data.hosts.get(i).ip;
        }
      }
      
      new logger.LogManager().logInfo( 
        String.format( 
          "\nnew host(s):        %d\nupdated host(s):    %d\ndeleted host(s):    %d\nprocessed host(s)   %d\ntotal host(s):      %d\n\nnew cache(s):     %d\nupdated cache(s):   %d\nprocessed cache(s): %d\ntotal cache(s):     %d",
          newHosts, updatedHosts, deletedHosts, (updatedHosts+newHosts), data.hosts.size(), newCaches, updatedCaches, (updatedCaches+newCaches), data.caches.size()
        )
      );

      return;
    } catch ( Exception ex ) {
      
      new logger.LogManager().logExc(ex);
    } finally {
      
      queue         = null;
      tasks         = null; 
      slicedPayload = null;
      tmpHost       = null;
      tmpCache      = null;
    }
    
  }

  
  private void generateCache() {
    
    boolean leaves  = false;
    boolean vendors = false;
    boolean uptime  = false;
    int i           = 0;
    int x           = -1;
    int moduleValue = 0;
    int cachesSize  = data.caches.size() * 32;
    int hostsSize   = data.hosts.size() * 32;
    HashMap <String, String> cacheFragments = new HashMap<String, String>();
    
    // load stringBuilder array
    //for(i = 0; i < 32; ++i) { 
    //  cacheFragment[i] = new StringBuilder(); 
    //}
    
    // caches array
    // u|http://cache5.leite.us/|319
    for(i = 0; i < cachesSize; i+=2) { 
      
      moduleValue = i % 32;
      x += (moduleValue == 0 ? 1 : 0);
      if(data.caches.get(x).timeStamp < cacheMaxTime) {
        i += 32;
        continue;
      }
      cacheFragment[moduleValue].append("u|" + data.caches.get(x).url + "|" + (time - data.caches.get(x).timeStamp) + "|\n");
    }
    
    x = -1;

    // hosts array
    // h|109.213.214.58:6346|241|||RAZA|140487|
    for(i = 0; i < hostsSize; ++i) {
      moduleValue = i % 32;
      x += (moduleValue == 0 ? 1 : 0);
      if(data.hosts.get(x).timeStamp < hostMaxTime) {
        i += 32;
        continue;
      }
      
      if((i%2) == 0) {

        leaves  = (i%4)  == 0;
        vendors = (i%8)  == 0;
        uptime  = (i%16) == 0;

        if(leaves || vendors || uptime) {
          // H|ip:port|age in cache|clustering|leaves|4 letter vendor code|reported uptime of host|max. leaves possible
          cacheFragment[moduleValue].append(
            "h|" + numberToIp(data.hosts.get(x).ip) + 
            ":"  + data.hosts.get(x).port + 
            "|"  + (time - data.hosts.get(x).timeStamp) +
            "||" + (leaves  ? data.hosts.get(x).totalLeaves : "") + 
            "|"  + (vendors ? data.hosts.get(x).clientNick  : "") +
            "|"  + (uptime  ? data.hosts.get(x).uptime      : "") +
            "|"  + (leaves  ? data.hosts.get(x).maxLeaves   : "") + "\n"
          );
        } else {
          cacheFragment[moduleValue].append(
            "h|" + numberToIp(data.hosts.get(x).ip) + 
            ":"  + data.hosts.get(x).port +
            "|"  + (time-data.hosts.get(x).timeStamp) + "\n"
          );  
        }
      }
    }
    
    //
    for(i = 0; i < 32; ++i) {

    }
    
  }

  private void saveData() {

    Output out    = null;
    Kryo kryo     = null;
    Entity entity = null;

    try {
      
      kryo   = new Kryo();
      out    = new Output(1024, 1024*1024);  
      entity = new Entity("Data");
      
      kryo.register(String.class);
      kryo.register(String[].class);
      kryo.register(Long[].class);
      kryo.register(Host.class);
      kryo.register(Cache.class);
      kryo.register(List.class);
      kryo.register(DataHolder.class);
      
      kryo.writeObject(out, data);
      
      entity.setProperty("timeStamp", System.currentTimeMillis()/1000);
      entity.setProperty("value", out.getBuffer());
      
      ds.put(entity);
      
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      out    = null;
      kryo   = null;
      entity = null;
    }
  }


  // 
  private void loadData() {
    
    Query query            = null;
    List<Entity> entities  = null;
    PreparedQuery prepared = null;
    Kryo kryo              = null;
    Input input            = null;
    
    try {
      
      query = new Query("Data");
      query.addSort("timeStamp", SortDirection.DESCENDING);
      prepared = ds.prepare(query);
      entities = prepared.asList(FetchOptions.Builder.withLimit(1));
      
      if(entities.size()>0) {
        
        kryo  = new Kryo();
        input = new Input();
        input.setBuffer((byte[]) entities.get(0).getProperty("value"));
        
        kryo.register(String.class);
        kryo.register(String[].class);
        kryo.register(Long[].class);
        kryo.register(Host.class);
        kryo.register(Cache.class);
        kryo.register(List.class);
        kryo.register(DataHolder.class);
        
        data = kryo.readObject(input, DataHolder.class);
      } else {
        
        //
        data = new DataHolder();
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      query    = null;
      entities = null;
      prepared = null;
      kryo     = null;
      input    = null;
    }
  }
  
  // convert number to ip
  private String numberToIp(Long ipNumber){
   
    return String.format(
      "%d.%d.%d.%d", 
      (int) ((ipNumber/16777216) % 256), 
      (int) ((ipNumber/65536) % 256), 
      (int) ((ipNumber/256) % 256), 
      (int) (ipNumber % 256)
    );
  }

  // get ip of host
  private long getIpOfUrl( String url ) {
    return 0;
  }
  
  //
  private static Object arrayAlloc(Object oldArray, int newSize) {
    int oldSize = java.lang.reflect.Array.getLength(oldArray);
    Class elementType = oldArray.getClass().getComponentType();
    Object newArray = java.lang.reflect.Array.newInstance(elementType, newSize);
    int preserveLength = Math.min(oldSize, newSize);
    if (preserveLength > 0)
      System.arraycopy(oldArray, 0, newArray, 0, preserveLength);
    return newArray; 
  }

}
