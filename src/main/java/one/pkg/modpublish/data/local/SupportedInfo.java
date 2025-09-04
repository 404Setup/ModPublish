package one.pkg.modpublish.data.local;

import lombok.Getter;
import lombok.Setter;

@Getter
@SuppressWarnings("unused")
public class SupportedInfo {
    public SupportTarget client;
    public SupportTarget server;

    public SupportedInfo() {
    }

    public SupportedInfo(SupportTarget client, SupportTarget server) {
        this.client = client;
        this.server = server;
    }

    @Getter
    @Setter
    public static class SupportTarget {
        public int cfid;
        public boolean enabled = false;

        public SupportTarget() {
        }

        public SupportTarget(int cfid) {
            this.cfid = cfid;
        }

    }
}