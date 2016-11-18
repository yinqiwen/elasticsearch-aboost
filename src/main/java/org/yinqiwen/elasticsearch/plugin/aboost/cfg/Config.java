/**
 * 
 */
package org.yinqiwen.elasticsearch.plugin.aboost.cfg;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.yinqiwen.elasticsearch.plugin.aboost.ABoostPlugin;
import org.yinqiwen.elasticsearch.utils.FileWatcher;
import org.yinqiwen.elasticsearch.utils.FileWatcher.ReloadFile;

/**
 * @author wangqiying
 *
 */
public class Config implements ReloadFile {
	private static final Logger logger = ESLoggerFactory.getLogger(Config.class.getName());
	private Environment environment;
	private Settings settings;
	private static Map<String, Double> cpWeights = new ConcurrentHashMap<>();

	public static Double getCPWeight(String cp, Double default_val) {
		if (cpWeights.containsKey(cp)) {
			return cpWeights.get(cp);
		}
		return default_val;
	}

	@Inject
	public Config(Environment env) throws IOException {
		Path pluginConf = getConfigInPluginDir();
		Path path = pluginConf.resolve("aboost.json");
		settings = Settings.builder().loadFromPath(path).build();
		this.environment = env;

		// this.useSmart = settings.get("use_smart", "false").equals("true");
		// this.enableLowercase = settings.get("enable_lowercase",
		// "true").equals("true");
		// this.enableRemoteDict = settings.get("enable_remote_dict",
		// "true").equals("true");
		String cp_weights_conf = settings.get("cp_weight_conf", "aboost.json");
		if(!cp_weights_conf.startsWith("/")){
			cp_weights_conf = pluginConf.resolve(cp_weights_conf).toString();
		}
		FileWatcher.getInstance().watch(cp_weights_conf, this);

	}

	public Path getConfigInPluginDir() {
		return PathUtils.get(
				new File(ABoostPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent(),
				"config").toAbsolutePath();
	}

	public Environment getEnvironment() {
		return environment;
	}

	public Settings getSettings() {
		return settings;
	}

	@Override
	public void onReload(String file) {
		logger.info("Reload aboost conf:" + file);
		cpWeights.clear();
		BufferedReader br = null;
		try {
			String line;
			br = AccessController.doPrivileged(new PrivilegedExceptionAction<BufferedReader>(){
				@Override
				public BufferedReader run() throws Exception {
					return new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
				}	
			});
			while ((line = br.readLine()) != null) {
				String[] ss = line.trim().split(",");
				logger.info("###" + line);
				if (ss.length == 2) {
					cpWeights.put(ss[0], Double.parseDouble(ss[1]));
				} else {
					logger.error("###Invalid aboost conf line:" + line);
				}
			}
		} catch (Exception e) {
			logger.error("Reload conf file:" + file, e);
		} finally {
			if (null != br) {
				try {
					br.close();
				} catch (IOException e) {
					//
				}
			}
		}
	}
}
