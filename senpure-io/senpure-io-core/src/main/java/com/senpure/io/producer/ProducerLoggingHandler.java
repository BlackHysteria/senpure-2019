package com.senpure.io.producer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import java.util.Arrays;

/**
 * ConsumerLoggingHandler
 *
 * @author senpure
 * @time 2019-08-06 14:59:59
 */
public class ProducerLoggingHandler extends LoggingHandler {

    private boolean outFormat;

    private boolean inFormat;

    public ProducerLoggingHandler(LogLevel level, boolean inFormat, boolean outFormat) {
        super(level);
        this.inFormat = inFormat;
        this.outFormat = outFormat;
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {

        if (this.logger.isEnabled(this.internalLevel)) {
            if (msg instanceof Producer2GatewayMessage) {
                if (outFormat) {
                    Producer2GatewayMessage message = (Producer2GatewayMessage) msg;
                    this.logger.log(this.internalLevel, "{} token:{} userIds:{}{}{}",
                            "WRITE", message.getToken(), Arrays.toString(message.getUserIds()), "\n", message.getMessage().toString(null));
                    //this.logger.log(this.internalLevel, this.format(ctx, ChannelAttributeUtil.getChannelPlayerStr(ctx.channel())+" WRITE", "\n"+((Message) msg).toString(null)));
                } else {
                    this.logger.log(this.internalLevel, "{} {}",
                            "WRITE: ", msg);
                    //  this.logger.log(this.internalLevel, this.format(ctx, ChannelAttributeUtil.getChannelPlayerStr(ctx.channel())+" WRITE", msg));

                }

            } else {
                this.logger.log(this.internalLevel, "{} {}",
                        "WRITE: ", msg);
            }
        }

        ctx.write(msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (this.logger.isEnabled(this.internalLevel)) {
            if (msg instanceof Gateway2ProducerMessage) {
                if (inFormat) {
                    Gateway2ProducerMessage message = (Gateway2ProducerMessage) msg;
                    this.logger.log(this.internalLevel, "{} token:{} userId:{}{}{}",
                            "RECEIVED", message.getToken(), message.getUserId(), "\n", message.getMessage().toString(null));
                    // this.logger.log(this.internalLevel, this.format(ctx, ChannelAttributeUtil.getChannelPlayerStr(ctx.channel()) + " RECEIVED", "\n" + ((Message) msg).toString(null)));

                } else {
                    this.logger.log(this.internalLevel, "{} {}",
                            "RECEIVED: ", msg);
                }
            } else {
                this.logger.log(this.internalLevel, "{} {}",
                        "RECEIVED: ", msg);
            }
        }
        ctx.fireChannelRead(msg);

    }
}
