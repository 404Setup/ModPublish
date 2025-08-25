package one.pkg.modpublish.data.local;

public class SupportedInfo {
    public SupportTarget client;
    public SupportTarget server;
    
    public SupportedInfo() {}
    
    public SupportedInfo(SupportTarget client, SupportTarget server) {
        this.client = client;
        this.server = server;
    }
    
    public SupportTarget getClient() {
        return client;
    }
    
    public SupportTarget getServer() {
        return server;
    }
    
    public static class SupportTarget {
        public int cfid;
        public boolean enabled = false;
        
        public SupportTarget() {}
        
        public SupportTarget(int cfid) {
            this.cfid = cfid;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getCfid() {
            return cfid;
        }
    }
}