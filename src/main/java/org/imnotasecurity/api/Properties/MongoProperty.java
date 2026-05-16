package org.imnotasecurity.api.Properties;

public class MongoProperty extends AbstractProperty {
    private String mongoKey;
    public String getKey() {
        return mongoKey;
    }
    public void setKey(String key) {
        this.mongoKey = key;
    }

    public MongoProperty(String key) {
        this.mongoKey = key;
    }
}
