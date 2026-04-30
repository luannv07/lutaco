package vn.id.luannv.lutaco.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.id.luannv.lutaco.dto.response.DashboardResponse;
import vn.id.luannv.lutaco.enumerate.PeriodRange;
import vn.id.luannv.lutaco.service.DashboardService;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DashboardServiceImpl implements DashboardService {

    @Override
    @Transactional
    public DashboardResponse handleSummary(PeriodRange range) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    public void exportBasic(HttpServletResponse response, PeriodRange period) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }

    @Override
    @Transactional
    public void exportAdvanced(HttpServletResponse response, PeriodRange period) {
        throw new UnsupportedOperationException("This service is temporarily disabled.");
    }
}
