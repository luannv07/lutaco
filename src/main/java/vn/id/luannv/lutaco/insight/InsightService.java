package vn.id.luannv.lutaco.insight;

import vn.id.luannv.lutaco.dto.InsightDto;

import java.util.List;

public interface InsightService {
    List<InsightDto> generate(InsightContext context);
}
