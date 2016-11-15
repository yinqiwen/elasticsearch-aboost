/**
 * 
 */
package org.yinqiwen.elasticsearch.plugin.aboost.cfg;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.io.PathUtils;
import org.elasticsearch.common.logging.ESLoggerFactory;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.yinqiwen.elasticsearch.plugin.aboost.ABoostPlugin;

/**
 * @author wangqiying
 *
 */
public class Config {
	private static final Logger logger = ESLoggerFactory.getLogger(Config.class.getName());
	private Environment environment;
	private Settings settings;
	private Map<String, Double> factorValueRef;

	@Inject
	public Config(Environment env) throws IOException {
		Path path = env.pluginsFile().resolve("aboost/config/aboost.json");
		settings = Settings.builder().loadFromPath(path).build();
		this.environment = env;;
		

//		this.useSmart = settings.get("use_smart", "false").equals("true");
//		this.enableLowercase = settings.get("enable_lowercase", "true").equals("true");
//		this.enableRemoteDict = settings.get("enable_remote_dict", "true").equals("true");

		//Dictionary.initial(this);
		logger.info("####" + settings.get("dicts", "false"));

	}

	public Path getConfigInPluginDir() {
		return PathUtils
				.get(new File(ABoostPlugin.class.getProtectionDomain().getCodeSource().getLocation().getPath())
						.getParent(), "config")
				.toAbsolutePath();
	}



	public Environment getEnvironment() {
		return environment;
	}

	public Settings getSettings() {
		return settings;
	}

}
