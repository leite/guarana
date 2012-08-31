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
		Kryo kryo	      = null;
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
		Query query 		   = null;
		List<Entity> entities  = null;
		PreparedQuery prepared = null;
		Kryo kryo			   = null;
		Input input			   = null;
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

}
