/**
 * 
 */
package org.yinqiwen.elasticsearch.utils;

import java.io.File;
import java.security.PrivilegedAction;
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
		while (true) {
			try {
				for (Entry<String, WatchFile> entry : watchFiles.entrySet()) {
					File f = new File(entry.getValue().file);
					if(f.exists() && f.lastModified() > entry.getValue().ts){
						if(entry.getValue().cb != null){
							entry.getValue().cb.onReload(entry.getValue().file);
							entry.getValue().ts = f.lastModified();
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
