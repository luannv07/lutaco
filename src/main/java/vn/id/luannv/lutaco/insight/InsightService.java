package vn.id.luannv.lutaco.insight;

import vn.id.luannv.lutaco.dto.response.DashboardInsightResponse;

import java.util.List;

public interface InsightService {
    List<DashboardInsightResponse> generate(InsightContext context);
}
