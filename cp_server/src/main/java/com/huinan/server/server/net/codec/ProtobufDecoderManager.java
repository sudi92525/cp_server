package com.huinan.server.server.net.codec;


import java.util.ArrayList;
import java.util.List;

import com.google.protobuf.MessageLite;
import com.huinan.server.server.net.config.ProtocolMap;

public final class ProtobufDecoderManager {
    private List<List<ProtobufDecoder>> protoDecoders = new ArrayList<>();
    private String defaultProtoFileName = "proto.xml";

    public ProtobufDecoderManager(String fileName) {
        load(fileName);
    }
    public ProtobufDecoderManager() {
        load(defaultProtoFileName);
    }

    private void load(String fileName){
        List<List<MessageLite>> lists = new ProtocolMap(fileName).getProtocolConfig();
        for (List<MessageLite> arr : lists) {
           List<ProtobufDecoder> temp = new ArrayList<>();
            for (MessageLite item : arr) {
                temp.add(new ProtobufDecoder(item));
            }
            protoDecoders.add(temp);
        }
    }
    
    public ProtobufDecoder getProtoDecoder(int systemId, int protoId) {
        return protoDecoders.get(systemId - 1).get(protoId - 1);
    }
}
