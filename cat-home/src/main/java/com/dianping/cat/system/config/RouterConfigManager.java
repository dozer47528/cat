package com.dianping.cat.system.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.home.router.transform.DefaultSaxParser;

public class RouterConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private RouterConfig m_routerConfig;

	private Logger m_logger;

	private static final String CONFIG_NAME = "routerConfig";

	private Map<String, Set<String>> m_configs = new HashMap<String, Set<String>>();

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public RouterConfig getRouterConfig() {
		return m_routerConfig;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_configId = config.getId();
			m_routerConfig = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-router-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_routerConfig = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_routerConfig == null) {
			m_routerConfig = new RouterConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			m_routerConfig = DefaultSaxParser.parse(xml);
			boolean result = storeConfig();
			m_configs.clear();
			return result;
		} catch (Exception e) {
			Cat.logError(e);
			m_logger.error(e.getMessage(), e);
			return false;
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_routerConfig.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}