package one.pkg.modpublish.data.internel;

import lombok.Getter;

@Getter
public enum ReleaseChannel {
    Release("release"), Beta("beta"), Alpha("alpha");

    private final String type;

    ReleaseChannel(String type) {
        this.type = type;
    }

}
