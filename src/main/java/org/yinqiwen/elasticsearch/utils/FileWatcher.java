/**
 * 
 */
package org.yinqiwen.elasticsearch.utils;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.SpecialPermission;
import org.elasticsearch.common.logging.ESLoggerFactory;

/**
 * @author qiyingwang
 *
 */
public class FileWatcher implements Runnable {

	public interface ReloadFile {
		public void onReload(String file);
	}

	private static class WatchFile {
		String file;
		long ts;
		ReloadFile cb;
	}

	private static final Logger logger = ESLoggerFactory.getLogger(FileWatcher.class.getName());
	private static FileWatcher instance = new FileWatcher();
	private Map<String, WatchFile> watchFiles = new ConcurrentHashMap<>();

	public static FileWatcher getInstance() {
		return instance;
	}

	private FileWatcher() {
		new Thread(this).start();
	}

	public void watch(String file, ReloadFile cb) {
		WatchFile f = new WatchFile();
		f.cb = cb;
		f.file = file;
		f.ts = 0;
		watchFiles.put(file, f);

	}

	@Override
	public void run() {
		SecurityManager sm = System.getSecurityManager();
		if (sm != null) {
			// unprivileged code such as scripts do not have SpecialPermission
			sm.checkPermission(new SpecialPermission());
		}
		while (true) {
			try {
				for (Entry<String, WatchFile> entry : watchFiles.entrySet()) {
					Long ts = AccessController.doPrivileged(new PrivilegedExceptionAction<Long>(){
						@Override
						public Long run() throws Exception {
							// TODO Auto-generated method stub
							File f = new File(entry.getValue().file);
							if(f.exists()){
								return f.lastModified();
							}
							return 0L;
						}	
					});
					if (ts > 0 && ts > entry.getValue().ts) {
						if (entry.getValue().cb != null) {
							entry.getValue().cb.onReload(entry.getValue().file);
							entry.getValue().ts = ts;
						}
					}
				}
				Thread.sleep(5000);
			} catch (Throwable e) {
				logger.error("FileWatcher routine error:", e);
			}
		}

	}

}
