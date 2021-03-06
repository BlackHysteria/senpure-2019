package com.senpure.io.support;

import com.senpure.executor.DefaultTaskLoopGroup;
import com.senpure.executor.TaskLoopGroup;
import com.senpure.io.ServerProperties;
import com.senpure.io.consumer.ConsumerMessageExecutor;
import com.senpure.io.consumer.ConsumerServer;
import com.senpure.io.consumer.RemoteServerChannelManager;
import com.senpure.io.consumer.RemoteServerManager;
import com.senpure.io.event.EventHelper;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * GatewayServerStarter
 *
 * @author senpure
 * @time 2019-03-01 15:17:33
 */
public class ConsumerServerStarter implements ApplicationRunner {

    private Logger logger = LoggerFactory.getLogger(getClass());
    @Resource
    private DiscoveryClient discoveryClient;
    @Resource
    private ServerProperties properties;
    @Resource
    private RemoteServerManager remoteServerManager;

    @Resource
    private ConsumerMessageExecutor messageExecutor;

    private ServerProperties.Gateway gateway = new ServerProperties.Gateway();
    private List<ConsumerServer> servers = new ArrayList<>();
    private long lastFailTime = 0;
    private long lastLogTime = 0;
    private long failTimes = 0;
    private String lastFailServerKey;

    @PostConstruct
    public void init() {
        check();
        messageExecutor();
    }

    private void check() {
        if (StringUtils.isEmpty(properties.getName())) {
            properties.setName("consumerServer");
        }
        ServerProperties.Consumer consumer = properties.getConsumer();
        if (!consumer.isSetReadableName()) {
            consumer.setReadableName(properties.getName());
        }
        //io *2 logic *1 综合1.5
        double size = Runtime.getRuntime().availableProcessors() * 1.5;
        int ioSize = (int) (size * 0.6);
        ioSize = Math.max(ioSize, 1);
        int logicSize = (int) (size * 0.4);
        logicSize = Math.max(logicSize, 1);
        if (consumer.getIoWorkThreadPoolSize() < 1) {
            consumer.setIoWorkThreadPoolSize(ioSize);
        }
        if (consumer.getExecutorThreadPoolSize() < 1) {
            consumer.setExecutorThreadPoolSize(logicSize);
        }
    }


    private void messageExecutor() {
        ServerProperties.Consumer consumer = properties.getConsumer();
        TaskLoopGroup service =new DefaultTaskLoopGroup(consumer.getExecutorThreadPoolSize(),
                new DefaultThreadFactory(properties.getName() + "-executor"));
        messageExecutor.setService(service);
        EventHelper.setService(service);
    }

    @PreDestroy
    public void destroy() {
        for (ConsumerServer server : servers) {
            server.destroy();
        }
        if (messageExecutor != null) {
            messageExecutor.shutdownService();
        }

    }

    public void setProperties(ServerProperties properties) {
        this.properties = properties;
    }


    private boolean canLog() {
        long now = System.currentTimeMillis();
        if (now - lastLogTime >= 60000) {
            lastLogTime = now;
            return true;
        }
        return false;
    }

    @Override

    public void run(ApplicationArguments args) throws Exception {
        if (properties.getConsumer().isAutoConnect()) {
            messageExecutor.getService().scheduleWithFixedDelay(() -> {
                try {
                    boolean canLog = canLog();
                    if (remoteServerManager.getDefaultChannelManager() == null) {

                        List<ServiceInstance> serviceInstances = discoveryClient.getInstances(properties.getConsumer().getRemoteName());
                        if (serviceInstances.size() == 0) {
                            if (canLog) {
                                logger.warn("没有服务可用{}", properties.getConsumer().getRemoteName());
                            }
                            return;
                        }
                        ServiceInstance instance;
                        if (lastFailServerKey == null) {
                            instance = serviceInstances.get(0);
                        } else {
                            Random random = new Random();
                            instance = serviceInstances.get(random.nextInt(serviceInstances.size()));
                        }

                        String portStr = instance.getMetadata().get("csPort");
                        int port;
                        if (portStr == null) {
                            port = gateway.getCsPort();
                        } else {
                            port = Integer.parseInt(portStr);
                        }
                        String serverKey = remoteServerManager.getRemoteServerKey(instance.getHost(), port);
                        RemoteServerChannelManager remoteServerChannelManager = remoteServerManager.
                                getRemoteServerChannelManager(serverKey);
                        remoteServerChannelManager.setHost(instance.getHost());
                        remoteServerChannelManager.setPort(port);
                        remoteServerChannelManager.setDefaultMessageRetryTimeLimit(properties.getConsumer().getMessageRetryTimeLimit());
                        remoteServerManager.setDefaultChannelManager(remoteServerChannelManager);

                    } else {


                        RemoteServerChannelManager remoteServerChannelManager = remoteServerManager.getDefaultChannelManager();

                        if (remoteServerChannelManager.isConnecting()) {
                            return;
                        }
                        long now = System.currentTimeMillis();
                        if (remoteServerChannelManager.getChannelSize() < properties.getConsumer().getRemoteChannel()) {
                            boolean start = false;
                            if (lastFailTime == 0) {
                                start = true;
                            } else {
                                if (now - lastFailTime >= properties.getConsumer().getConnectFailInterval()) {
                                    start = true;
                                }
                            }
                            if (start) {
                                remoteServerChannelManager.setConnecting(true);
                                ConsumerServer consumerServer = new ConsumerServer();
                                consumerServer.setMessageExecutor(messageExecutor);
                                consumerServer.setRemoteServerManager(remoteServerManager);
                                consumerServer.setProperties(properties.getConsumer());
                                if (consumerServer.start(remoteServerChannelManager.getHost(), remoteServerChannelManager.getPort())) {
                                    servers.add(consumerServer);
                                    //验证
                                    remoteServerChannelManager.addChannel(consumerServer.getChannel());
                                    failTimes = 0;
                                } else {
                                    lastFailTime = now;
                                    lastFailServerKey = remoteServerChannelManager.getServerKey();
                                    failTimes++;
                                    if (failTimes >= 10 && remoteServerChannelManager.getChannelSize() == 0) {
                                        remoteServerManager.setDefaultChannelManager(null);
                                        failTimes = 0;
                                    }
                                }
                                remoteServerChannelManager.setConnecting(false);
                            }

                        }

                    }
                } catch (Exception e) {

                    logger.error("error", e);
                }

            }, 500, 50, TimeUnit.MILLISECONDS);
        }
    }
}
