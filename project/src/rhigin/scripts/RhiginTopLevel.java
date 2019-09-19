package rhigin.scripts;

import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.AccessController;
import java.security.AllPermission;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.JavaAdapter;

final class RhiginTopLevel extends ImporterTopLevel {
	private static final long serialVersionUID = 2594663122275499379L;
	private AccessControlContext accCtxt;

	RhiginTopLevel(Context ctx) {
		super(ctx, System.getSecurityManager() != null);
		JavaAdapter.init(ctx, this, false);

		if (System.getSecurityManager() != null) {
			try {
				AccessController.checkPermission(new AllPermission());
			} catch (AccessControlException ace) {
				accCtxt = AccessController.getContext();
			}
		}
	}

	AccessControlContext getAccessContext() {
		return accCtxt;
	}
}