package com.senpure.io.generator.habit;

import com.senpure.io.generator.executor.LanguageExecutor;

/**
 * LanguageConfig
 *
 * @author senpure
 * @time 2019-09-23 11:17:56
 */
public interface LanguageConfig {

    LanguageExecutor languageExecutor();

    boolean hasExtraOverwrite();

    void notAllowExtraOverwrite();

    void initValue();

    void checkSelf();
}