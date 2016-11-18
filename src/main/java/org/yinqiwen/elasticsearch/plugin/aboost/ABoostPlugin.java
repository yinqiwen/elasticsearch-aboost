package org.yinqiwen.elasticsearch.plugin.aboost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.elasticsearch.common.inject.AbstractModule;
import org.elasticsearch.common.inject.Module;
import org.elasticsearch.common.inject.multibindings.Multibinder;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.elasticsearch.search.fetch.FetchSubPhase;
import org.yinqiwen.elasticsearch.plugin.aboost.cfg.Config;
import org.yinqiwen.elasticsearch.plugin.aboost.functions.FieldMatchFactorFunctionBuilder;
import org.yinqiwen.elasticsearch.plugin.aboost.functions.FieldRefValueFactorFunctionBuilder;

public class ABoostPlugin extends Plugin implements SearchPlugin {
	public static String PLUGIN_NAME = "aboost";

	@Override
	public Collection<Module> createGuiceModules() {
		return Collections.<Module> singletonList(new ConfiguredModule());
	}
	
	@Override
	public List<FetchSubPhase> getFetchSubPhases(FetchPhaseConstructionContext context) {
		// TODO Auto-generated method stub
		return SearchPlugin.super.getFetchSubPhases(context);
	}

	@Override
	public List<ScoreFunctionSpec<?>> getScoreFunctions() {
		// TODO Auto-generated method stub
		List<ScoreFunctionSpec<?>> funcs = new ArrayList<>();
		ScoreFunctionSpec<FieldRefValueFactorFunctionBuilder> s1 = new ScoreFunctionSpec<FieldRefValueFactorFunctionBuilder>(
				FieldRefValueFactorFunctionBuilder.NAME, FieldRefValueFactorFunctionBuilder::new,
				FieldRefValueFactorFunctionBuilder::fromXContent);
		ScoreFunctionSpec<FieldMatchFactorFunctionBuilder> s2 = new ScoreFunctionSpec<FieldMatchFactorFunctionBuilder>(
				FieldMatchFactorFunctionBuilder.NAME, FieldMatchFactorFunctionBuilder::new,
				FieldMatchFactorFunctionBuilder::fromXContent);
		funcs.add(s1);
		funcs.add(s2);
		return funcs;
	}

	public static class ConfiguredModule extends AbstractModule {
		@Override
		protected void configure() {
			bind(Config.class).asEagerSingleton();
//			Multibinder<Config> multibinder = Multibinder.newSetBinder(binder(), Config.class);
//			multibinder.addBinding().to
//			multibinder.addBinding().toInstance(new Twix());
//			multibinder.addBinding().toProvider(SnickersProvider.class);
//			multibinder.addBinding().to(Skittles.class);
		}
	}
}
