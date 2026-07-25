package org.xyplugin.xycore.api;

import org.xyplugin.xycore.api.item.ItemLibraryService;
import org.xyplugin.xycore.api.item.ItemTagService;

/** Compile-only subset of the public XyCore API used by XyItems. */
public interface XyCoreApi {
    ItemLibraryService getItems();

    ItemTagService getItemTags();

    String getVersion();
}
