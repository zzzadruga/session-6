package ru.sbt.jschool.session6;

import java.util.Properties;

public class ApplicationProperties extends Properties {
    public ApplicationProperties() {
    }

    public ApplicationProperties(Properties defaults) {
        super(defaults);
    }

    public int getIntProperty(String key){
        return Integer.parseInt(super.getProperty(key));
    }

    public int getIntProperty(String key,  String defaultValue){
        return Integer.parseInt(super.getProperty(key, defaultValue));
    }
}
