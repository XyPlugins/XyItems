package org.xyplugin.xyitems.api;

import org.xyplugin.xyitems.XyItemsPlugin;

/** Static access point for the public XyItems API. */
public final class XyItems {
    private XyItems() {
    }

    public static XyItemsApi get() {
        XyItemsPlugin plugin = XyItemsPlugin.getInstance();
        if (plugin == null || !plugin.isEnabled()) {
            throw new IllegalStateException("XyItems is not enabled");
        }
        return plugin.getApi();
    }
}
