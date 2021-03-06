package com.dianping.cat.report.task.alert.exception;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.BaseVisitor;
import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.dependency.exception.entity.ExceptionExclude;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.system.config.ExceptionConfigManager;

public class TopReportVisitor extends BaseVisitor {

	private ExceptionConfigManager m_exceptionConfigManager;

	private AlertReport m_report;

	private String m_currentDomain;

	private long m_totalSegmentException;

	private static final String TOTAL_EXCEPTION_ALERT = "TotalExceptionAlert";

	public TopReportVisitor setExceptionConfigManager(ExceptionConfigManager exceptionConfigManager) {
		m_exceptionConfigManager = exceptionConfigManager;
		return this;
	}

	public TopReportVisitor setReport(AlertReport report) {
		m_report = report;
		return this;
	}

	@Override
	public void visitDomain(Domain domain) {
		m_currentDomain = domain.getName();
		super.visitDomain(domain);
	}

	@Override
	public void visitError(Error error) {

		ExceptionExclude exceptionExclude = m_exceptionConfigManager.queryDomainExceptionExclude(m_currentDomain,
		      error.getId());

		if (exceptionExclude != null) {
			return;
		}

		int warnLimit = -1;
		int errorLimit = -1;
		int count = error.getCount();
		ExceptionLimit exceptionLimit = m_exceptionConfigManager
		      .queryDomainExceptionLimit(m_currentDomain, error.getId());

		m_totalSegmentException += count;

		if (exceptionLimit != null) {
			warnLimit = exceptionLimit.getWarning();
			errorLimit = exceptionLimit.getError();
		}

		if (errorLimit > 0 & warnLimit > 0 & count >= Math.min(warnLimit, errorLimit)) {

			com.dianping.cat.home.alert.report.entity.Domain domain = m_report.findOrCreateDomain(m_currentDomain);
			domain.setName(m_currentDomain);

			com.dianping.cat.home.alert.report.entity.Exception exception = domain.findOrCreateException(error.getId());

			if (errorLimit > 0 && count >= errorLimit) {
				exception.incErrorNumber();
				domain.incErrorNumber();
			} else if (warnLimit > 0 && count >= warnLimit) {
				exception.incWarnNumber();
				domain.incWarnNumber();
			}
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		m_totalSegmentException = 0;

		super.visitSegment(segment);

		ExceptionLimit exceptionLimit = m_exceptionConfigManager.queryDomainTotalLimit(m_currentDomain);
		int warnLimit = -1;
		int errorLimit = -1;

		if (exceptionLimit != null) {
			warnLimit = exceptionLimit.getWarning();
			errorLimit = exceptionLimit.getError();
		}

		if (errorLimit > 0 & warnLimit > 0 & m_totalSegmentException >= Math.min(warnLimit, errorLimit)) {
			com.dianping.cat.home.alert.report.entity.Domain domain = m_report.findOrCreateDomain(m_currentDomain);
			com.dianping.cat.home.alert.report.entity.Exception exception = domain
			      .findOrCreateException(TOTAL_EXCEPTION_ALERT);

			if (m_totalSegmentException >= errorLimit) {
				domain.incErrorNumber();
				exception.incErrorNumber();
			} else if (m_totalSegmentException >= warnLimit) {
				domain.incWarnNumber();
				exception.incWarnNumber();
			}
		}
	}

	@Override
	public void visitTopReport(TopReport topReport) {
		super.visitTopReport(topReport);
	}
}
