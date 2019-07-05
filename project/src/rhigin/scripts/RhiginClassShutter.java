package rhigin.scripts;

import java.util.HashMap;
import java.util.Map;

import org.mozilla.javascript.ClassShutter;

final class RhiginClassShutter implements ClassShutter {
    private static Map<String, Boolean> protectedClasses;
    private static RhiginClassShutter theInstance;

    private RhiginClassShutter() {
    }

    static synchronized ClassShutter getInstance() {
        if (theInstance == null) {
            theInstance = new RhiginClassShutter();
            protectedClasses = new HashMap<String, Boolean>();
            protectedClasses.put("java.security.AccessController", Boolean.TRUE);
        }
        return theInstance;
    }

    public boolean visibleToScripts(String fullClassName) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            int i = fullClassName.lastIndexOf(".");
            if (i != -1) {
                try {
                    sm.checkPackageAccess(fullClassName.substring(0, i));
                } catch (SecurityException se) {
                    return false;
                }
            }
        }
        return protectedClasses.get(fullClassName) == null;
    }
}