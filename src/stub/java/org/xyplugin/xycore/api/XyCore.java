package org.xyplugin.xycore.api;

/** Compile-only XyCore API stub. The real class is supplied by XyCore at runtime. */
public final class XyCore {
    private XyCore() {
    }

    public static XyCoreApi get() {
        throw new IllegalStateException("XyCore API stub is only for compilation.");
    }
}
