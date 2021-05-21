package com.luo.ibatis.scripting;

import java.util.HashMap;
import java.util.Map;

public class LanguageDriverRegistry {
    private final Map<Class<? extends ArcherLanguageDriver>, ArcherLanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

    private Class<? extends ArcherLanguageDriver> defaultDriverClass;

    public void register(Class<? extends ArcherLanguageDriver> cls) {
        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
            try {
                LANGUAGE_DRIVER_MAP.put(cls, cls.newInstance());
            } catch (Exception ex) {
                throw new ScriptingException("Failed to load language driver for " + cls.getName(), ex);
            }
        }
    }

    public void register(ArcherLanguageDriver instance) {
        if (instance == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        Class<? extends ArcherLanguageDriver> cls = instance.getClass();
        if (!LANGUAGE_DRIVER_MAP.containsKey(cls)) {
            LANGUAGE_DRIVER_MAP.put(cls, instance);
        }
    }

    public ArcherLanguageDriver getDriver(Class<? extends ArcherLanguageDriver> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public ArcherLanguageDriver getDefaultDriver() {
        return getDriver(getDefaultDriverClass());
    }

    public Class<? extends ArcherLanguageDriver> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    public void setDefaultDriverClass(Class<? extends ArcherLanguageDriver> defaultDriverClass) {
        register(defaultDriverClass);
        this.defaultDriverClass = defaultDriverClass;
    }
}
