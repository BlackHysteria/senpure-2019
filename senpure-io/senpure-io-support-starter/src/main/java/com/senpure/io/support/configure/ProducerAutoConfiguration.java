package com.senpure.io.support.configure;

import com.senpure.base.util.Assert;
import com.senpure.io.message.CSBreakUserGatewayMessage;
import com.senpure.io.producer.GatewayManager;
import com.senpure.io.producer.ProducerMessageHandlerUtil;
import com.senpure.io.producer.ProducerServer;
import com.senpure.io.producer.handler.CSAskHandleMessageHandler;
import com.senpure.io.producer.handler.CSBreakUserGatewayMessageHandler;
import com.senpure.io.producer.handler.CSRelationUserGatewayMessageHandler;
import com.senpure.io.producer.handler.ProducerMessageHandler;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;

/**
 * ProducerAutoConfiguration
 *
 * @author senpure
 * @time 2019-03-01 11:46:50
 */

public class ProducerAutoConfiguration {
   // private Logger logger = LoggerFactory.getLogger(getClass());

   // @Autowired
    //private DiscoveryClient discoveryClient;

    @Bean
    public CSRelationUserGatewayMessageHandler csRelationUserGatewayMessageHandler() {
        return new CSRelationUserGatewayMessageHandler();
    }

    /**
     * @return
     * @see BreakUserSelector
     */
    public CSBreakUserGatewayMessageHandler csBreakUserGatewayMessageHandler() {
        return new CSBreakUserGatewayMessageHandler();
    }

    @Bean
    public CSAskHandleMessageHandler csAskHandleMessageHandler() {
        return new CSAskHandleMessageHandler();
    }


    @Bean
    public GatewayManager gatewayManager() {
        GatewayManager gatewayManager = new GatewayManager();
        return gatewayManager;
    }

    @Bean
    public ProducerServer producerServer(){
        return new ProducerServer();
    }

    @Bean
    public HandlerChecker handlerChecker() {
        return new HandlerChecker();
    }

    class HandlerChecker implements ApplicationRunner {
        @Override
        public void run(ApplicationArguments args) throws Exception {
            ProducerMessageHandler handler = ProducerMessageHandlerUtil.getHandler(CSBreakUserGatewayMessage.MESSAGE_ID);
            if (handler == null) {
                Assert.error("缺少[CSBreakUserGatewayMessage]处理器");
            }
        }
    }

}