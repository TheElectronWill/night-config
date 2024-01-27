package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;

/** A filter that is applied when a {@link FileConfig} is loaded and reloaded.
 * It can accept or reject the (re)load, based on the new version of the config.
 * If the (re)load is rejected, the config will not change.
 */
@FunctionalInterface
public interface ConfigLoadFilter {
    /**
     * Filters the new version of a config, before it has been applied on the config itself.
     * 
     * @param newConfig the new configuration
     * @return {@code true} to accept the new version, {@code false} to reject it
     */
    public boolean acceptNewVersion(CommentedConfig newConfig);
}
