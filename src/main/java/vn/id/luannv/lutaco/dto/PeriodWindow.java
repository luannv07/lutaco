package vn.id.luannv.lutaco.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PeriodWindow {
    private LocalDateTime from;
    private LocalDateTime to;

    private LocalDateTime previousFrom;
    private LocalDateTime previousTo;
}
