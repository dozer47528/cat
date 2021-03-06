package com.dianping.cat.system.config;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.rule.entity.MetricItem;
import com.dianping.cat.home.rule.entity.MonitorRules;
import com.dianping.cat.home.rule.entity.Rule;
import com.dianping.cat.home.rule.transform.DefaultSaxParser;

public class NetworkRuleConfigManager extends BaseRuleConfigManager implements Initializable {

	private static final String CATEGORY_NAME = "network";

	private static final String CONFIG_NAME = "networkRuleConfig";

	@Override
	protected String getCategoryName() {
		return CATEGORY_NAME;
	}
	
	@Override
   protected String getConfigName() {
	   return CONFIG_NAME;
   }

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_config = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-network-metric-rule-config-unit.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new MonitorRules();
		}
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);
			setSumTrueWhenAllFalse(m_config);

			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private void setSumTrueWhenAllFalse(MonitorRules config) {
		for (Rule rule : m_config.getRules().values()) {
			for (MetricItem item : rule.getMetricItems()) {
				if (!item.isMonitorAvg() && !item.isMonitorCount() && !item.isMonitorSum()) {
					item.setMonitorSum(true);
				}
			}
		}
	}

}
