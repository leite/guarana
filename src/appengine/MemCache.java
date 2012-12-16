package appengine;

/*
 * file:  MemCache.java
 * 
 * author:   @xxleite
 * 
 * date:   09-23-2011 -03 GMT
 * 
 * detais:  memcache facilities for appengine
 * 
 *  ----------------------------------------------------------------------------
 *   "THE BEER-WARE LICENSE" (Revision 42.1):
 *   <xxleite@gmail.com> wrote this file. As long as you retain this notice you
 *   can do whatever you want with this stuff. If we meet some day, and you think
 *   this stuff is worth it, you can buy me a beer or more in return 
 *  ----------------------------------------------------------------------------
 */

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

import com.google.appengine.api.memcache.stdimpl.GCacheFactory;

public class MemCache {
  
  private Cache cache;
  
  public MemCache() {
    
    CacheFactory cFactory = null;
    
    try {
      
      cFactory = CacheManager.getInstance().getCacheFactory();
      cache = cFactory.createCache(Collections.emptyMap());
    } catch ( Exception ex ) {
      cache = null;
      new logger.LogManager().logExc(ex); 
    } finally {
      cFactory = null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public long increaseKey( Object key, int seconds ) {
  
    Map<Object,Integer> mp = null;
    CacheFactory cFactory = null;
    Cache _cache = null;
    Long value = 0L;
    
    try{
      
      mp=new HashMap<Object, Integer>();
      mp.put(GCacheFactory.EXPIRATION_DELTA, seconds);
      cFactory= CacheManager.getInstance().getCacheFactory();
      _cache= cFactory.createCache(mp);
      
      value = (Long) _cache.get(key);
      
      if ( value==null ) { value = 0L; }
      
      _cache.put(key, ++value);
      
      return value;
    }catch(CacheException e){
      
      new logger.LogManager().logExc(e);
      return value;
    }finally{
      mp = null;
      cFactory = null;
      _cache = null;
    }
    
  }
  
  public String get( String key ){
    
    if( cache==null )
    	return null;
    return (String) cache.get(key); 
  }
  
  public byte[] getBinary( String key ){
    
    if( cache==null )  
    	return null;
    return (byte[]) cache.get(key);
  }
  
  @SuppressWarnings("unchecked")
  public Boolean set( String key, String value, Integer seconds ){
    
    Map<Object,Integer> mp = null;
    CacheFactory cFactory = null;
    Cache _cache = null;
    
    try{
      
      mp=new HashMap<Object, Integer>();
      mp.put(GCacheFactory.EXPIRATION_DELTA, seconds);
      cFactory= CacheManager.getInstance().getCacheFactory();
      _cache= cFactory.createCache(mp);
      _cache.put(key, value);
      return true;
    }catch(CacheException e){
      
      new logger.LogManager().logExc(e);
      return false;
    }finally{
      mp = null;
      cFactory = null;
      _cache = null;
    }
  }
  
  @SuppressWarnings("unchecked")
  public Boolean setBinary( String key, byte[] value, Integer seconds ){
    
    Map<Object,Integer> mp = null;
    CacheFactory cFactory = null;
    Cache _cache = null;
    
    try{
      
      mp=new HashMap<Object, Integer>();
      mp.put(GCacheFactory.EXPIRATION_DELTA, seconds);
      cFactory= CacheManager.getInstance().getCacheFactory();
      _cache= cFactory.createCache(mp);
      _cache.put(key, value);
      return true;
    }catch(CacheException e){
      
      new logger.LogManager().logExc(e);
      return false;
    }finally{
      mp = null;
      cFactory = null;
      _cache = null;
    }
  }
  
  public Boolean clear( String key ){
    
    if ( cache==null ) { return null; }
    
    cache.remove(key);
      
    return true;
  }
  
  public Boolean clearAllCache(){
    
    if ( cache==null ) { return null; }
    
    cache.clear();
      
    return true;  
  }
  
}

