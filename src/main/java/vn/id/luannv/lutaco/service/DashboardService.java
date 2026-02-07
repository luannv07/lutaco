package vn.id.luannv.lutaco.service;

import vn.id.luannv.lutaco.dto.response.DashboardResponse;

public interface DashboardService {
    DashboardResponse handleSummary();
    void exportBasic();
    void exportAdvanced();
}
