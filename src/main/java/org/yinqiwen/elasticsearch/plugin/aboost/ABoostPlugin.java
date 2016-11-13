package org.yinqiwen.elasticsearch.plugin.aboost;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Hello world!
 *
 */

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.yinqiwen.elasticsearch.plugin.aboost.functions.FieldMatchFactorFunctionBuilder;
import org.yinqiwen.elasticsearch.plugin.aboost.functions.FieldRefValueFactorFunctionBuilder;
public class ABoostPlugin extends Plugin  implements SearchPlugin
{
	public static String PLUGIN_NAME = "aboost";
	
	@Override
	public  List<ScoreFunctionSpec<?>> getScoreFunctions() {
		// TODO Auto-generated method stub
		List<ScoreFunctionSpec<?>> funcs = new ArrayList<>();
		ScoreFunctionSpec<FieldRefValueFactorFunctionBuilder> s1 = new ScoreFunctionSpec<FieldRefValueFactorFunctionBuilder>(FieldRefValueFactorFunctionBuilder.NAME, FieldRefValueFactorFunctionBuilder::new, FieldRefValueFactorFunctionBuilder::fromXContent);
		ScoreFunctionSpec<FieldMatchFactorFunctionBuilder> s2 = new ScoreFunctionSpec<FieldMatchFactorFunctionBuilder>(FieldMatchFactorFunctionBuilder.NAME, FieldMatchFactorFunctionBuilder::new, FieldMatchFactorFunctionBuilder::fromXContent);
		funcs.add(s1);
		funcs.add(s2);
		return funcs;
	}
	

}
