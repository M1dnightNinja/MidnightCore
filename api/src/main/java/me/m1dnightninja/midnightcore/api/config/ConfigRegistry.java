package me.m1dnightninja.midnightcore.api.config;

import me.m1dnightninja.midnightcore.api.MidnightCoreAPI;

import java.io.File;
import java.util.HashMap;

public class ConfigRegistry {

    private final HashMap<Class<?>, ConfigSerializer<?>> serializers = new HashMap<>();
    private final HashMap<Class<?>, InlineSerializer<?>> inlineSerializers = new HashMap<>();
    private final HashMap<String, ConfigProvider> providers = new HashMap<>();

    public <T> void registerSerializer(Class<T> clazz, ConfigSerializer<T> serializer) {
        this.serializers.put(clazz, serializer);
    }

    public <T> void registerInlineSerializer(Class<T> clazz, InlineSerializer<T> serializer) {
        this.inlineSerializers.put(clazz, serializer);
    }

    @SuppressWarnings("unchecked")
    public <T> ConfigSerializer<T> getSerializer(Class<T> clazz) {
        for(Class<?> ser : serializers.keySet()) {
            if(ser == clazz || ser.isAssignableFrom(clazz)) return (ConfigSerializer<T>) serializers.get(ser);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> InlineSerializer<T> getInlineSerializer(Class<T> clazz) {
        for(Class<?> ser : inlineSerializers.keySet()) {
            if(ser == clazz || ser.isAssignableFrom(clazz)) return (InlineSerializer<T>) inlineSerializers.get(ser);
        }

        return null;
    }

    public boolean canSerialize(Class<?> clazz) {

        for(Class<?> ser : serializers.keySet()) {
            if(ser == clazz || ser.isAssignableFrom(clazz)) return true;
        }

        return false;
    }

    public boolean canSerializeInline(Class<?> clazz) {
        for(Class<?> ser : inlineSerializers.keySet()) {
            if(ser == clazz || ser.isAssignableFrom(clazz)) return true;
        }

        return false;
    }

    public void registerProvider(ConfigProvider prov) {
        if(providers.containsKey(prov.getFileExtension())) return;

        this.providers.put(prov.getFileExtension(), prov);
    }

    public ConfigProvider getProviderForFileType(String extension) {
        return providers.get(extension);
    }

    public ConfigProvider getProviderForFile(File f) {

        String name = f.getName();
        if(name.contains(".")) {
            return getProviderForFileType(name.substring(name.lastIndexOf(".")));

        } else {
            return MidnightCoreAPI.getInstance().getDefaultConfigProvider();
        }

    }

}

