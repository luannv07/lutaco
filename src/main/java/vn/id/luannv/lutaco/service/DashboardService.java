package vn.id.luannv.lutaco.service;

import jakarta.servlet.http.HttpServletResponse;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.enumerate.PeriodRange;

public interface DashboardService {
    DashboardResponse handleSummary(PeriodRange periodRange);
    void exportBasic(HttpServletResponse response, PeriodRange period);
    void exportAdvanced(HttpServletResponse response, PeriodRange period);
}
