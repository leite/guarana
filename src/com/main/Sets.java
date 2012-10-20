package com.main;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletConfig;
//import javax.servlet.ServletContext;

public class Sets {
  
  // debug inspect etc ...
  public boolean debug = true;
  
  // useful variables
  public int maxIpsToStore = 100;
  public int maxIpsToShow = 80;
  public int maxUrlsToStore = 100;
  public int maxUrlsToShow = 60;
  public int hostExpirationTime = 21600;
  public int urlExpirationTime = 86400;
  public int accessWait = 1800;
  public Set<String> supportedNetworks = new HashSet<String>(Arrays.asList( new String[] {"gnutella2"} ));
  public Set<String> urlBlacklist = new HashSet<String>(Arrays.asList( new String[] { "nyud.net", "nyucd.net", "noneexiste.net", "spearforensics.com", "divergentlogic.net", "gofoxy.net" } ));
  public String cacheName = "Guarana";
  public String cacheVendor = "GUAR";
  public String cacheVersion = "0.3";
  public String appUrl = "http://127.0.0.1:8080";
  
  // quick list of vendors
  public Map<String, String> vendors = null;
  
  // public initializer
  public Sets( ServletConfig context ) {
    
    // hold fields
    String[] fields;
    
    try {
      
      // create local
      Map<String, String> _vendors = new HashMap<String, String>();
      
      // populate
      _vendors.put("ACQL", "Acqlite");
      _vendors.put("ACQX", "Acquisition");
      _vendors.put("AGIO", "Adagio");
      _vendors.put("BAZK", "Bazooka");
      _vendors.put("BCON", "Beacon Cache");
      _vendors.put("BCON", "Beacon Cache II");
      _vendors.put("BCON", "Beacon Cache II (Chico mod)");
      _vendors.put("BEAR", "BearShare");
      _vendors.put("CABO", "Cabos/LimeWire");
      _vendors.put("CANN", "Cannon");
      _vendors.put("CHTC", "Cheater Cache");
      _vendors.put("COCO", "CocoGnut");
      _vendors.put("DNET", "Deepnet Explorer");
      _vendors.put("Dianlei", "Dianlei");
      _vendors.put("dianlei", "Dianlei");
      _vendors.put("FLOX", "Flox");
      _vendors.put("FOXY", "Foxy P2P");
      _vendors.put("FTWC", "FTWebCache");
      _vendors.put("GCII", "PHPGnuCacheII");
      _vendors.put("GDNA", "GnucDNA");
      _vendors.put("GIFT", "giFT");
      _vendors.put("GNZL", "Gnoozle");
      _vendors.put("GNUC", "Gnucleus");
      _vendors.put("GNUT", "Gnut");
      _vendors.put("GOLD", "Ares Gold");
      _vendors.put("GTKG", "GTK Gnutella");
      _vendors.put("GUAR", "Guarana");
      _vendors.put("GWCC", "Cheater Cache");
      _vendors.put("iswip", "iSwipe");
      _vendors.put("JGWC", "Jums Web Cache");
      _vendors.put("JTEL", "JTella");
      _vendors.put("KIWI", "Kiwi Alpha");
      _vendors.put("KUPE", "Kupe");
      _vendors.put("LIME", "LimeWire");
      _vendors.put("MESH", "iMesh");
      _vendors.put("MLDK", "MLDonkey");
      _vendors.put("MMMM", "Morpheus");
      _vendors.put("MNAP", "MyNapster");
      _vendors.put("MRPH", "Morpheus");
      _vendors.put("MTLL", "Mutella");
      _vendors.put("mutekomm", "Kommute");
      _vendors.put("MUTEkomm", "Kommute");
      _vendors.put("MUTE", "MUTE");
      _vendors.put("NOVA", "Nova P2P");
      _vendors.put("PEER", "Peer Project");
      _vendors.put("PGII", "Pocket G2");
      _vendors.put("PHEX", "Phex");
      _vendors.put("PNTH", "Panthera Project");
      _vendors.put("QTEL", "Qtella");
      _vendors.put("RAZA", "Shareaza");
      _vendors.put("RAZB", "Shareaza Beta");
      _vendors.put("RAZL", "ShareazaLite");
      _vendors.put("RZCA", "ShareazaPlus (Alpha)");
      _vendors.put("RZCB", "ShareazaPlus (Beta)");
      _vendors.put("RZCC", "ShareazaPlus");
      _vendors.put("Self-Add", "Cache Retry System");
      _vendors.put("Sharetastic", "Sharetastic");
      _vendors.put("SHLN-DEV", "Sharelin");
      _vendors.put("SHLN", "Sharelin");
      _vendors.put("SKLL", "Skulls");
      _vendors.put("SNOW", "Frostwire");
      _vendors.put("SWAP", "Swapper");
      _vendors.put("TESTBazooka", "Bazooka GWC");
      _vendors.put("TESTBCII", "Beacon Cache II");
      _vendors.put("TESTBCON", "Beacon Cache");
      _vendors.put("TESTCachechu", "Cachechu");
      _vendors.put("TESTCrab-", "GhostWhiteCrab");
      _vendors.put("TESTGWCSCANNER", "Multi-Network GWC Scan");
      _vendors.put("TESTPGDBScan", "Jon Atkins GWC Scan");
      _vendors.put("TESTSKLL", "Skulls");
      _vendors.put("TFLS", "TrustyFiles");
      _vendors.put("URL-ADD", "URL-ADD");
      _vendors.put("WURM", "Wurm GWC Scanner");
      
      // pass values to public and destroy local
      vendors = Collections.unmodifiableMap(_vendors);
      _vendors = null;
      
      // set fields
      debug              = Boolean.parseBoolean(context.getInitParameter("debug"));
      maxIpsToStore      = Integer.parseInt(context.getInitParameter("maxIpsToStore"));
      maxIpsToShow       = Integer.parseInt(context.getInitParameter("maxIpsToShow"));
      maxUrlsToStore     = Integer.parseInt(context.getInitParameter("maxUrlsToStore"));
      maxUrlsToShow      = Integer.parseInt(context.getInitParameter("maxUrlsToShow"));
      hostExpirationTime = Integer.parseInt(context.getInitParameter("hostExpirationTime"));
      urlExpirationTime  = Integer.parseInt(context.getInitParameter("urlExpirationTime"));
      accessWait         = Integer.parseInt(context.getInitParameter("accessWait"));
      cacheName          = context.getInitParameter("cacheName");
      cacheVendor        = context.getInitParameter("cacheVendor");
      cacheVersion       = context.getInitParameter("cacheVersion");
      appUrl             = context.getInitParameter("appUrl");
      
      fields             = context.getInitParameter("supportedNetworks").split(";");
      supportedNetworks  = new HashSet<String>(Arrays.asList( fields ));
      
      fields             = context.getInitParameter("urlBlacklist").split(";");
      urlBlacklist       = new HashSet<String>(Arrays.asList( fields ));
      
    } catch ( Exception ex ) {
      // log message
    }  
  }
  
}
