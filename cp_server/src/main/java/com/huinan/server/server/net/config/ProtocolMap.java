package com.huinan.server.server.net.config;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration.Node;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;

import com.google.protobuf.MessageLite;

/**
 * Configured C/S protocol here
 * 
 * @author ashley
 *
 */
public class ProtocolMap {

    private List<List<MessageLite>> prototypeArray = new ArrayList<>();
    private String fileName;

    public ProtocolMap(String fileName) {
        this.fileName = fileName;
        init();
    }

    public void init() throws IllegalArgumentException {
        XMLConfiguration config = null;
        try {
            config = new XMLConfiguration(ProtocolMap.class.getResource("/"
                    + fileName));
        } catch (ConfigurationException e) {
            // String msg = "[proto.xml] is not found!"
            throw new IllegalArgumentException(e);
        }

        try {
            Node protoMap = config.getRoot();
            for (ConfigurationNode system : protoMap.getChildren()) {
                ArrayList<MessageLite> temp = new ArrayList<>();
                for (ConfigurationNode proto : system.getChildren()) {
                    String key = (String) proto.getValue();
                    Class<?> clazz = Class.forName(key);
                    Method m = clazz.getDeclaredMethod("getDefaultInstance");
                    MessageLite obj = (MessageLite) m.invoke(null,
                            new Object[]{});
                    temp.add(obj);
                }
                prototypeArray.add(temp);

            }
        } catch (ClassNotFoundException e) {
            // String msg = "class is not found!"
            throw new IllegalArgumentException(e);
        } catch (NoSuchMethodException | SecurityException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new IllegalArgumentException(e);
        } finally {
            config.clear();
        }
    }

    public List<List<MessageLite>> getProtocolConfig() {
        return prototypeArray;
    }

}
