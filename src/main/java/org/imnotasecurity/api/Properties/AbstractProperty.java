package org.imnotasecurity.api.Properties;

import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import org.imnotasecurity.api.ImNotLanguage;

import java.util.function.Consumer;

public abstract class AbstractProperty {
    private boolean authCracked = true;
    public boolean getDoAuthLoginForCracked() {
        return authCracked;
    }
    public void setDoAuthLoginForCracked(boolean state) {
        this.authCracked = state;
    }

    private Consumer<AsyncPlayerConfigurationEvent> loadCallback;
    public void setLoadCallback(Consumer<AsyncPlayerConfigurationEvent> callback) {
        this.loadCallback = callback;
    }
    public Consumer<AsyncPlayerConfigurationEvent> getLoadCallback() {return loadCallback;}

    private ImNotLanguage language = ImNotLanguage.VIETNAMESE;
    public ImNotLanguage getLanguage() {
        return language;
    }
    public void setLanguage(ImNotLanguage language) {
        this.language = language;
    }

    private long loginLimitTicks = 10*20;
    public long getLoginLimitTicks() {return loginLimitTicks;}
    public void setLoginLimitTicks(long loginLimitTicks) {
        this.loginLimitTicks = loginLimitTicks;
    }

    private String website = "serverhere.com";
    public String getWebsite() {
        return website;
    }
    public void setWebsite(String website) {
        this.website = website;
    }

    public AbstractProperty() {

    }
}
