package com.senpure.io.support;

import com.senpure.base.util.NameThreadFactory;
import com.senpure.io.ServerProperties;
import com.senpure.io.direct.DirectMessageExecutor;
import com.senpure.io.direct.DirectServer;
import com.senpure.io.event.EventHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * DirectServerStarter
 *
 * @author senpure
 * @time 2019-09-18 10:01:46
 */
public class DirectServerStarter implements ApplicationRunner {
    @Autowired
    private ServerProperties properties;

    @Autowired
    private DirectMessageExecutor messageExecutor;

    private DirectServer directServer;
    private ScheduledExecutorService service;

    @PostConstruct
    public void init() {
        check();
        messageExecutor();
    }

    public void check() {
        if (StringUtils.isEmpty(properties.getName())) {
            properties.setName("direct");
        }
        ServerProperties.Direct direct = properties.getDirect();
        if (!direct.isSetReadableName()) {
            direct.setReadableName(properties.getName());
        }

        //io *2 logic *1 综合1.5
        double size = Runtime.getRuntime().availableProcessors() * 1.5;
        int ioSize = (int) (size * 0.6);
        ioSize = ioSize < 1 ? 1 : ioSize;
        int logicSize = (int) (size * 0.4);
        logicSize = logicSize < 1 ? 1 : logicSize;
        if (direct.getExecutorThreadPoolSize() < 1) {
            direct.setExecutorThreadPoolSize(logicSize);
        }
        if (direct.getIoWorkThreadPoolSize() < 1) {
            direct.setIoWorkThreadPoolSize(ioSize);
        }
    }

    public void messageExecutor() {
        ScheduledExecutorService service = Executors.newScheduledThreadPool(properties.getDirect().getExecutorThreadPoolSize(),
                new NameThreadFactory(properties.getName() + "-executor"));
        messageExecutor.setService(service);
        this.service = service;
        EventHelper.setService(service);
        DirectServer directServer = new DirectServer();
        directServer.setMessageExecutor(messageExecutor);
        directServer.setProperties(properties.getDirect());
        directServer.setReadableName(properties.getDirect().getReadableName());
        directServer.start();
        this.directServer = directServer;

    }

    @PreDestroy
    public void destroy() {
        if (directServer != null) {
            directServer.destroy();
        }
        if (messageExecutor != null) {
            messageExecutor.shutdownService();
        }

    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

    }
}