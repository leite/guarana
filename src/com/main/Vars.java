package com.main;

import java.util.Date;
import java.util.Map;

public final class Vars {
  
  // request variables
  public boolean dump        = false;
  public boolean truncate    = false;
  public int timeStamp       = 0;
  public boolean ping        = false;
  public boolean update      = false;
  public boolean get         = false;
  public boolean bFile       = false;
  public boolean urlFile     = false;
  public boolean gwcs        = false;
  public boolean hostFile    = false;
  public boolean showHosts   = false;
  public String net          = null;
  public String client       = null;
  public String version      = null;
  public String ip           = null;
  public String url          = null;
  public long uptime         = 0;
  public long cluster        = 0;
  public long xLeaves        = 0;
  public long xMax           = 0;
  public boolean getNetworks = false;
  public boolean getVendors  = false;
  public boolean getClusters = false;
  public boolean getUptime   = false;
  public boolean getLeaves   = false;
  
  // public initializer
  public Vars( Map<String,String[]> param ){
    
    // basic debug
    this.dump        = param.containsKey("dump");
    this.truncate    = param.containsKey("truncate");
    
    // rules
    this.ping        = param.containsKey("ping"); 
    this.update      = param.containsKey("update"); 
    this.get         = param.containsKey("get"); 
    this.bFile       = param.containsKey("bfile"); 
    this.urlFile     = param.containsKey("urlfile"); 
    this.gwcs        = param.containsKey("gwcs"); 
    this.hostFile    = param.containsKey("hostfile"); 
    this.showHosts   = param.containsKey("showhosts"); 
    
    // data to process
    this.net         = param.containsKey("net")? param.get("net")[0] : null; 
    this.client      = param.containsKey("client")? param.get("client")[0] : null; 
    this.version     = param.containsKey("version")? param.get("version")[0] : null; 
    this.ip          = param.containsKey("ip")? param.get("ip")[0] : null; 
    this.url         = param.containsKey("url")? param.get("url")[0] : null; 
    this.uptime      = param.containsKey("uptime")? Long.parseLong(param.get("uptime")[0]) : 0; 
    this.cluster     = param.containsKey("cluster")? Long.parseLong(param.get("cluster")[0]) : 0; 
    this.xLeaves     = param.containsKey("x_leaves")? Long.parseLong(param.get("x_leaves")[0]) : (param.containsKey("x.leaves")? Long.parseLong(param.get("x.leaves")[0]) : 0 );
    this.xMax        = param.containsKey("x_max")? Long.parseLong(param.get("x_max")[0]) : (param.containsKey("x.max")? Long.parseLong(param.get("x.max")[0]) :0 ); 
    this.getNetworks = param.containsKey("getnetworks"); 
    this.getVendors  = param.containsKey("getvendors");
    this.getClusters = param.containsKey("getclusters"); 
    this.getUptime   = param.containsKey("getuptime");
    this.getLeaves   = param.containsKey("getleaves");
    this.timeStamp   = Math.round((new Date()).getTime() / 1000);

  }
  
}
