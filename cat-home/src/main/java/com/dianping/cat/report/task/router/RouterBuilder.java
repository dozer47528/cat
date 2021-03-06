package com.dianping.cat.report.task.router;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.helper.MapUtils;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.report.task.spi.ReportTaskBuilder;

public class RouterBuilder implements ReportTaskBuilder {

	@Inject
	private ReportService m_reportService;
	
	@Override
	public boolean buildDailyTask(String name, String domain, Date period) {
		Date start = period;
		Date end = new Date(start.getTime() + TimeUtil.ONE_DAY);
		StateReport report = m_reportService.queryStateReport(Constants.CAT, start, end);
		StateReportVisitor visitor = new StateReportVisitor();

		visitor.visitStateReport(report);
		
		Map<String, Long> numbers = visitor.getNumbers();
		Comparator<Entry<String, Long>> compator = new Comparator<Map.Entry<String, Long>>() {

			@Override
			public int compare(Entry<String, Long> o1, Entry<String, Long> o2) {
				return (int) (o1.getValue() - o2.getValue());
			}
		};
		numbers = MapUtils.sortMap(numbers, compator);
		return false;
	}

	public static class StateReportVisitor extends BaseVisitor {

		private Map<String, Long> m_numbers = new HashMap<String, Long>();

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			String domain = processDomain.getName();
			long total = processDomain.getTotal();
			Long count = m_numbers.get(domain);

			if (count == null) {
				m_numbers.put(domain, total);
			} else {
				m_numbers.put(domain, total + count);
			}
		}

		public Map<String, Long> getNumbers() {
			return m_numbers;
		}

	}

	@Override
	public boolean buildHourlyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support hourly task");
	}

	@Override
	public boolean buildMonthlyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support monthly task");
	}

	@Override
	public boolean buildWeeklyTask(String name, String domain, Date period) {
		throw new RuntimeException("router builder don't support weekly task");
	}

}
