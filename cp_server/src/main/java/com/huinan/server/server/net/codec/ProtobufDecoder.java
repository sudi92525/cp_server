package com.huinan.server.server.net.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import java.io.IOException;

import com.google.protobuf.ExtensionRegistry;
import com.google.protobuf.MessageLite;

public class ProtobufDecoder {

    private final MessageLite prototype;
    private final ExtensionRegistry extensionRegistry;

    private MessageLite messageData;

    public ProtobufDecoder(MessageLite prototype) {
        this(prototype, null);
    }

    public ProtobufDecoder(MessageLite prototype,
            ExtensionRegistry extensionRegistry) {
        if (prototype == null) {
            throw new NullPointerException("ProtobufDecoder: prototype");
        }
        this.prototype = prototype.getDefaultInstanceForType();
        this.extensionRegistry = extensionRegistry;
    }

    public MessageLite decode(ByteBuf buf) throws IOException {
        if (buf.hasArray()) {
            final int offset = buf.readerIndex();
            if (extensionRegistry == null) {
                messageData = prototype
                        .newBuilderForType()
                        .mergeFrom(buf.array(), buf.arrayOffset() + offset,
                                buf.readableBytes()).build();
            } else {
                messageData = prototype
                        .newBuilderForType()
                        .mergeFrom(buf.array(), buf.arrayOffset() + offset,
                                buf.readableBytes(), extensionRegistry).build();
            }
        } else {
            if (extensionRegistry == null) {
                messageData = prototype.newBuilderForType()
                        .mergeFrom(new ByteBufInputStream(buf)).build();
            } else {
                messageData = prototype
                        .newBuilderForType()
                        .mergeFrom(new ByteBufInputStream(buf),
                                extensionRegistry).build();;
            }
        }

        return messageData;
    }

}
