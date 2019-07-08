package com.senpure.io.generator.util;


import com.senpure.base.AppEvn;
import com.senpure.io.generator.Constant;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;


public class TemplateUtil {
    private static Logger logger
            = LoggerFactory.getLogger(TemplateUtil.class);
    private static File templateDir;

    public static File templateDir() {
        if (templateDir != null) {
            return templateDir;
        }
        String root = AppEvn.getClassRootPath();

        if (AppEvn.classInJar(AppEvn.getStartClass())) {
            templateDir = new File(root, "template");
        } else {
            templateDir = new File(root, "template");
        }
        logger.debug("模板文件路径{}",templateDir.getAbsolutePath());
        return templateDir;
    }

    public static void share(Configuration cfg) {
        cfg.setSharedVariable("rightPad", new RightPad());
        cfg.setSharedVariable("var32Size", new Var32Size());
        cfg.setSharedVariable("luaNameStyle", new LuaNameStyle());
        try {
            cfg.setSharedVariable("luaImplPrefix", Constant.LUA_IMPL_SC_PREFIX);
        } catch (TemplateModelException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {


    }
}
