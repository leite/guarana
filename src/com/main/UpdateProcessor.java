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

import java.util.List;
import java.util.concurrent.TimeUnit;

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

public class UpdateProcessor extends HttpServlet  {
<<<<<<< HEAD
	
	DatastoreService datastore;
	Kryo kryo;
	DataHolder data;
	
	
	public UpdateProcessor() {
		
	}
	
	public final void doGet(HttpServletRequest rq, HttpServletResponse rs) {
		
		Queue queue             = null;
		List<TaskHandle> tasks  = null;
		String payload          = null;
		int hostsProcessed     = 0;
		int urlsProcessed      = 0;
		int i                  = 0;
		
		try {
		
			// host|timeStamp|ip|port|clientNick|clientVersion|hostUptime|totalLeaves|maxLeaves
			// --------------------------------------------------------------------------------------------------------
			// host|191919191|129090909|6346|RAZA|2.5.8.0|121999292|200|300
			
			// url|url (address)|cacheName|cacheVersion|clientNick|clientVersion|rank|timeStamp|urlCount|ipCount|g1|g2
			// --------------------------------------------------------------------------------------------------------
			// url|http://cache.leite.us/|GUAR|0.3|RAZA|2.5.5.3|10|123232323|40|80|false|true
		
		  
			queue = QueueFactory.getQueue("update");
			
			while(!((tasks = queue.leaseTasks(30, TimeUnit.SECONDS, 5)).isEmpty())){
				for (TaskHandle leasedTask : tasks) {
				  
				  if(i==0) {
				    
				    datastore = DatastoreServiceFactory.getDatastoreService();
				    kryo      = new Kryo();
				    
		        kryo.register(DataHolder.class);
		        kryo.register(List.class);
		        kryo.register(HostList.class);
		        kryo.register(CacheList.class);
		        kryo.register(Host.class);
		        kryo.register(Cache.class);
		        kryo.register(String.class);
				    
		        loadLastItem();
				    
		        ++i;
				  }
					
					payload = new String(leasedTask.getPayload());
					
					if(payload.indexOf("host|")==0) {
						createOrUpdateHost(payload.split("|"));
						++hostsProcessed;
					} else {
					  createOrUpdateCache(payload.split("|"));
						++urlsProcessed;
					}
					queue.deleteTask(leasedTask.getName());
				}
			}
			
			new logger.LogManager().logInfo( hostsProcessed + " host(s), " + urlsProcessed + " url(s) processed" );
			
			return;
			
		} catch ( Exception ex ) {
			
			new logger.LogManager().logExc(ex);
		} finally {
			
			payload = null;
			queue = null;
		}
		
	}
	
	private void createOrUpdateHost(String[] params) {
		// host|timeStamp|ip|port|clientNick|clientVersion|hostUptime|totalLeaves|maxLeaves
		// host|191919191|129090909|6346|RAZA|2.5.8.0|121999292|200|300
		
	  Host host = null;
	  long ip  = Long.parseLong(params[2]);
		
	  for(int i=0; i<data.hostList.hosts.size(); ++i) {
	    if(data.hostList.hosts.get(i).ip==ip) {
	      host = data.hostList.hosts.remove(i);
	      break;
	    }
	  }
	  
	  host = host==null ? new Host() : host;
		
		host.clientNick    = params[4];
		host.clientVersion = params[5];
		host.totalLeaves   = Long.parseLong(params[7]);
		host.ip            = ip;
		host.maxLeaves     = Long.parseLong(params[8]);
		host.port          = Long.parseLong(params[3]);
		host.timeStamp     = Long.parseLong(params[1]);
		host.uptime        = Long.parseLong(params[6]);
		
		data.hostList.hosts.add(host);
	}
	
	private void createOrUpdateCache(String[] params) {
		// url|url (address)|cacheName|cacheVersion|clientNick|clientVersion|rank|timeStamp|urlCount|ipCount|g1|g2

		// url|http://cache.leite.us/|GUAR|0.3|RAZA|2.5.5.3|10|123232323|40|80|false|true
		
		Cache cache = null;
		String url  = params[1];
		
		for(int i =0; i<data.cacheList.caches.size(); ++i) {
		  if(data.cacheList.caches.get(i).url==url) {
		    cache = data.cacheList.caches.remove(i);
		    break;
		  }
		}
		
		if(cache==null) {
		  cache = new Cache();
		  cache.firstSeen    = Long.parseLong(params[7]);
		}
		
		cache.cacheName    = params[2];
		cache.cacheVersion = params[3];
		cache.clientNick   = params[4];
		cache.cacheVersion = params[5];
		cache.g1           = Boolean.parseBoolean(params[10]);
		cache.g2           = Boolean.parseBoolean(params[11]);
		cache.ip           = 0;
		cache.ipCount      = Integer.parseInt(params[9]);
		cache.rank         = Integer.parseInt(params[7]);
		cache.timeStamp    = Long.parseLong(params[7]);
		cache.url          = params[1];
		cache.urlCount     = Integer.parseInt(params[8]);
		
		data.cacheList.caches.add(cache);
	}
	
	private void updateDatastore() {

		Output out  = null;
		Entity item = null;
		
		try {
			item = new Entity("Itens");
			out  = new Output();
			
			kryo.writeObject(out, data);
			
			item.setProperty("value", out.getBuffer());
			datastore.put(item);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			out   = null;
			cache = null;
		}
	}
	
	//
	private void loadLastItem() {
		
		Query query = null;
		Input input = null;
		
		try {

			query                 = new Query("Itens");
			query.addSort("timeStamp", SortDirection.DESCENDING);
			PreparedQuery pq      = datastore.prepare(query);
			List<Entity> entities = pq.asList(FetchOptions.Builder.withLimit(1));
			
			//
			if(entities.size()>0) {
				kryo      = new Kryo();
				input     = new Input(1024 * 1024);
				Blob blob = (Blob) entities.get(0).getProperty("value");
				input.setBuffer(blob.getBytes());
				
				data = kryo.readObject(input, DataHolder.class);
			} else {
				data           = new DataHolder();
				data.cacheList = new CacheList();
				data.hostList  = new HostList();
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		} finally {
			query     = null;
			input     = null;
		}
	}
	
	// get ip of host
	private long getIpOfUrl( String url ) {
		return 0;
	}
=======
  
  private CacheList caches;
  private HostList hosts;
  DatastoreService ds;
  
  public UpdateProcessor() {
    
    //
    
  }
  
  public final void doGet(HttpServletRequest rq, HttpServletResponse rs) {
    
    Queue queue = null;
    List<TaskHandle> tasks = null;
    int hosts_processed = 0;
    int urls_processed = 0;
    String payload = null;
    
    try {
    
      // host|timeStamp|ip|port|clientNick|clientVersion|hostUptime|totalLeaves|maxLeaves
      // --------------------------------------------------------------------------------------------------------
      // host|191919191|129090909|6346|RAZA|2.5.8.0|121999292|200|300
      
      // url|url (address)|cacheName|cacheVersion|clientNick|clientVersion|rank|timeStamp|urlCount|ipCount|g1|g2
      // --------------------------------------------------------------------------------------------------------
      // url|http://cache.leite.us/|GUAR|0.3|RAZA|2.5.5.3|10|123232323|40|80|false|true
      
      ds = DatastoreServiceFactory.getDatastoreService();
      
      queue = QueueFactory.getQueue("update");
      
      while(!((tasks = queue.leaseTasks(30, TimeUnit.SECONDS, 5)).isEmpty())){
        
        for (TaskHandle leasedTask : tasks) {
          
          payload = new String(leasedTask.getPayload());
          
          if(payload.indexOf("host|")==0) {
            
          } else {
            
          }
          
          queue.deleteTask(leasedTask.getName());
        }
      }
      
      new logger.LogManager().logInfo( hosts_processed + " host(s), " + urls_processed + " url(s) processed" );
      
      return;
      
    } catch ( Exception ex ) {
      
      new logger.LogManager().logExc(ex);
    } finally {
      
      payload = null;
      queue = null;
    }
    
  }
  
  /*
  MyObject o = new MyObject();
  Kryo kryo = new Kryo();
  kryo.register(MyObject.class);

  ObjectBuffer ob = new ObjectBuffer(kryo);
  byte[] myByteArray = ob.writeObject(o);
  */
  
  private void createCache() {

    Output out        = null;
    Kryo kryo        = null;
    Entity cache      = null;
    
    try {
      cache = new Entity("Cache");
      
      CacheList cacheList = new CacheList();
      Cache _cache = new Cache();
      Cache _cache2 = new Cache();
      _cache.cacheName = "Joe";
      _cache.cacheVersion = "2.0";
      _cache.g1 = true;
      _cache.firstSeen = 0121212121L;
      
      _cache2.cacheName = "Maria";
      _cache2.cacheVersion = "3.0";
      _cache2.g1 = false;
      _cache2.firstSeen = 121212121L;
      
      cacheList.caches.add(_cache);
      cacheList.caches.add(_cache2);
      
      kryo = new Kryo();
      out  = new Output();
      kryo.register(String.class);
      kryo.register(Cache.class);
      kryo.register(List.class);
      kryo.register(CacheList.class);
      kryo.writeObject(out, cacheList);
      
      cache.setProperty("value", out.getBuffer());
      ds.put(cache);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      out   = null;
      kryo  = null;
      cache = null;
    }
  }
  
  
  // 
  private void loadLastItem( String model ) {
    Query query        = null;
    List<Entity> entities  = null;
    PreparedQuery prepared = null;
    Kryo kryo         = null;
    Input input         = null;
    //byte[] buffer          = new byte[BUFFER_SIZE];
    try {
      query = new Query(model);
      query.addSort("timeStamp", SortDirection.DESCENDING);
      prepared = ds.prepare(query);
      entities = prepared.asList(FetchOptions.Builder.withLimit(1));
      if(entities.size()>0) {
        kryo  = new Kryo();
        input = new Input();
        input.setBuffer((byte[]) entities.get(0).getProperty("value"));
        if(model=="Cache") {
          kryo.register(Cache.class);
          kryo.register(List.class);
          kryo.register(CacheList.class);
          caches = kryo.readObject(input, CacheList.class);
        } else {
          kryo.register(Host.class);
          kryo.register(List.class);
          kryo.register(HostList.class);
          hosts = kryo.readObject(input, HostList.class);
        }
      } else {
        //
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
  
  // get ip of host
  private long getIpOfUrl( String url ) {
    return 0;
  }
>>>>>>> ca5711c767bce5541307bc15931c54741c8c1e9a

}
