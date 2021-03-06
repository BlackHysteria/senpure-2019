package com.senpure.io.support.configure;

import com.senpure.base.util.RandomUtil;
import com.senpure.io.ServerProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;

import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * PropertiesCompletion
 *
 * @author senpure
 * @time 2019-07-30 21:08:59
 */
public class PropertiesCompletionConfiguration implements SpringApplicationRunListener {

  //  Logger logger = LoggerFactory.getLogger(getClass());

    public PropertiesCompletionConfiguration() {
    }

    public PropertiesCompletionConfiguration(SpringApplication springApplication, String[] args) {

        // this.springApplication = springApplication;
    }

    @Override
    public void starting() {

    }

    @Override
    public void environmentPrepared(ConfigurableEnvironment environment) {
        boolean current = false;
        for (PropertySource<?> propertySource : environment.getPropertySources()) {
            // logger.debug(propertySource.getClass().toString());
            if (propertySource.getName().startsWith("applicationConfig")) {
                current = true;
                break;
            }
        }
        if (current) {
            Map<String, Object> ioMap = new HashMap<>();
            ServerProperties.Gateway gateway = new ServerProperties.Gateway();
            Integer tempCsPort = environment.getProperty("server.io.gateway.cs-port", Integer.class);
            if (tempCsPort == null) {
                tempCsPort = environment.getProperty("server.io.gateway.csPort", Integer.class);
            }
            Integer csPort;
            //tempCsPort == null ||非网关服务就不用计算了
            if (tempCsPort != null && tempCsPort == 0) {
                csPort = getPort(gateway.getCsPort());
                ioMap.put("server.io.gateway.cs-port", csPort);
                ioMap.put("eureka.instance.metadataMap[csPort]", csPort);

            }
            Integer tempScPort = environment.getProperty("server.io.gateway.sc-port", Integer.class);
            if (tempScPort == null) {
                tempScPort = environment.getProperty("server.io.gateway.scPort", Integer.class);
            }
            Integer scPort;
            if (tempScPort != null && tempScPort == 0) {
                scPort = getPort(gateway.getScPort());
                ioMap.put("server.io.gateway.scPort", scPort);
                ioMap.put("eureka.instance.metadataMap[scPort]", scPort);
            }

            if (ioMap.size() > 0) {
                PropertySource propertySource = new MapPropertySource("senpure-io-proofing", ioMap);
                environment.getPropertySources().addFirst(propertySource);
            }
        }

    }

    private int getPort(int prior) {

        if (isPortUsing("127.0.0.1", prior)) {
            return getPort();
        }
        return prior;
    }

    private int getPort() {
        int port;
        do {
            //ServerSocket serverSocket =  new ServerSocket(0);
            //serverSocket.getLocalPort();
            port = RandomUtil.random(1, 65536);
        } while (isPortUsing("127.0.0.1", port));

        return port;

    }

    public static boolean isPortUsing(String host, int port) {
        boolean flag = false;

        try {
            InetAddress Address = InetAddress.getByName(host);
            new Socket(Address, port).close();  //建立一个Socket连接
            flag = true;
        } catch (Exception ignored) {
        }
        return flag;
    }

    @Override
    public void contextPrepared(ConfigurableApplicationContext context) {

    }

    @Override
    public void contextLoaded(ConfigurableApplicationContext context) {

    }

    @Override
    public void started(ConfigurableApplicationContext context) {

    }

    @Override
    public void running(ConfigurableApplicationContext context) {

    }

    @Override
    public void failed(ConfigurableApplicationContext context, Throwable exception) {

    }


}
