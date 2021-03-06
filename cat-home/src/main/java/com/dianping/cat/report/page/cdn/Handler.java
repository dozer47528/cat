package com.dianping.cat.report.page.cdn;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.cdn.graph.CdnGraphCreator;
import com.dianping.cat.report.page.userMonitor.CityManager;

import org.unidal.lookup.annotation.Inject;
import org.unidal.web.mvc.PageHandler;
import org.unidal.web.mvc.annotation.InboundActionMeta;
import org.unidal.web.mvc.annotation.OutboundActionMeta;
import org.unidal.web.mvc.annotation.PayloadMeta;

public class Handler implements PageHandler<Context> {
	@Inject
	private JspViewer m_jspViewer;
	
	@Inject
	private CityManager m_cityManager;
	
	@Inject
	private CdnGraphCreator m_cdnGraphCreator;
	
	private static final String ALL = "ALL";
	
	@Override
	@PayloadMeta(Payload.class)
	@InboundActionMeta(name = "cdn")
	public void handleInbound(Context ctx) throws ServletException, IOException {
		// display only, no action here
	}

	@Override
	@OutboundActionMeta(name = "cdn")
	public void handleOutbound(Context ctx) throws ServletException, IOException {
		Model model = new Model(ctx);
		Payload payload = ctx.getPayload();

		long start = payload.getHistoryStartDate().getTime();
		long end = payload.getHistoryEndDate().getTime();

		start = start - start % TimeUtil.ONE_HOUR;
		end = end - end % TimeUtil.ONE_HOUR;

		Date startDate = new Date(start);
		Date endDate = new Date(end);
		String province = payload.getProvince();
		String city = payload.getCity();
		String cdn = payload.getCdn();
		if (province == ALL) {
			city = ALL;
		}
		if (cdn == ALL) {
			province = ALL;
			city = ALL;
		}
		
		model.setLineCharts(m_cdnGraphCreator.queryBaseInfo(startDate, endDate, cdn, province, city));
		model.setStart(startDate);
		model.setEnd(endDate);
		model.setAction(Action.VIEW);
		model.setPage(ReportPage.CDN);
		model.setCityInfo(m_cityManager.getCityInfo());

		if (!ctx.isProcessStopped()) {
		   m_jspViewer.view(ctx, model);
		}
	}
	
}
