package com.senpure.io.generator.reader;

import com.senpure.base.AppEvn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * IoReader
 *
 * @author senpure
 * @time 2019-06-10 10:25:45
 */
public class IoReader {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static class Inner {
        private static IoReader ioReader = new IoReader();

    }

    public static IoReader getInstance() {
        return Inner.ioReader;
    }

    private Map<String, IoProtocolReader> ioProtocolReaderMap = new HashMap<>();

    public IoProtocolReader read(File file) {
        String key = file.getAbsolutePath();
        if (key.endsWith(".io")) {
            IoProtocolReader ioProtocolReader = ioProtocolReaderMap.get(key);
            if (ioProtocolReader == null) {
                ioProtocolReader = new IoProtocolReader();

                ioProtocolReader.read(file, ioProtocolReaderMap);
                ioProtocolReaderMap.putIfAbsent(key, ioProtocolReader);
                return ioProtocolReader;
            }

        } else {
            logger.debug("{} 文件后缀格式不对", key);
        }

        return null;
    }

    public Map<String, IoProtocolReader> getIoProtocolReaderMap() {
        return ioProtocolReaderMap;
    }


    public static void main(String[] args) {
        AppEvn.markClassRootPath();
        AppEvn.installAnsiConsole();
        IoReader.getInstance().read(new File(AppEvn.getClassRootPath(), "hello.io"));
    }

}