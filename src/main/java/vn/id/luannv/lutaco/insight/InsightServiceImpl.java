package vn.id.luannv.lutaco.insight;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.NumberUtils;
import vn.id.luannv.lutaco.config.InsightThresholdConfig;
import vn.id.luannv.lutaco.dto.InsightDto;
import vn.id.luannv.lutaco.dto.request.LoginRequest;
import vn.id.luannv.lutaco.dto.request.UserCreateRequest;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.dto.response.CategoryExpenseResponse;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.Role;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.UserGender;
import vn.id.luannv.lutaco.enumerate.UserPlan;
import vn.id.luannv.lutaco.enumerate.UserStatus;
import vn.id.luannv.lutaco.enumerate.UserType;
import vn.id.luannv.lutaco.event.entity.UserRegisteredEvent;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.mapper.UserMapper;
import vn.id.luannv.lutaco.repository.RoleRepository;
import vn.id.luannv.lutaco.repository.UserRepository;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.InvalidatedTokenService;
import vn.id.luannv.lutaco.service.RefreshTokenService;
import vn.id.luannv.lutaco.util.CustomizeNumberUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class InsightServiceImpl implements InsightService {
    InsightThresholdConfig insightConfig;
    @Override
    public List<InsightDto> generate(InsightContext ctx) {
        List<InsightDto> insights = new ArrayList<>();

        expenseInsight(ctx.getExpenseThisMonth(), ctx.getExpenseLastMonth()).ifPresent(insights::add);
        incomeInsight(ctx.getIncomeThisMonth(), ctx.getIncomeLastMonth()).ifPresent(insights::add);
        balanceInsight(ctx.getBalance()).ifPresent(insights::add);
        categoryInsight(ctx.getCategories()).ifPresent(insights::add);

        return insights;
    }
    private InsightDto buildInsight(
            InsightDto.InsightLevel level,
            InsightDto.InsightCode code,
            Double value,
            String unit
    ) {
        return InsightDto.builder()
                .level(level)
                .code(code)
                .value(CustomizeNumberUtils.formatDecimal(value, 2).doubleValue() * 100.0)
                .unit(unit)
                .colorTone(level.getColorTone())
                .defaultColor(level.getColor())
                .build();
    }
    private Optional<InsightDto> expenseInsight(Long thisMonth, Long lastMonth) {
        if (lastMonth == null || lastMonth == 0) return Optional.empty();

        double rate = (double) (thisMonth - lastMonth) / lastMonth;

        if (rate >= insightConfig.getExpense().getDangerRate()) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.DANGER,
                    InsightDto.InsightCode.EXPENSE_INCREASE,
                    rate,
                    "%"
            ));
        }

        if (rate >= insightConfig.getExpense().getWarnRate()) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.WARN,
                    InsightDto.InsightCode.EXPENSE_INCREASE,
                    rate,
                    "%"
            ));
        }

        if (rate < 0) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.SUCCESS,
                    InsightDto.InsightCode.EXPENSE_DECREASE,
                    Math.abs(rate),
                    "%"
            ));
        }

        return Optional.empty();
    }
    private Optional<InsightDto> incomeInsight(Long thisMonth, Long lastMonth) {
        if (lastMonth == null || lastMonth == 0) return Optional.empty();

        double rate = (double) (thisMonth - lastMonth) / lastMonth;

        if (rate >= insightConfig.getIncome().getSuccessRate()) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.SUCCESS,
                    InsightDto.InsightCode.INCOME_INCREASE,
                    rate,
                    "%"
            ));
        }

        if (rate < 0) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.WARN,
                    InsightDto.InsightCode.INCOME_DECREASE,
                    Math.abs(rate),
                    "%"
            ));
        }

        return Optional.empty();
    }
    private Optional<InsightDto> balanceInsight(Long balance) {
        if (balance < insightConfig.getBalance().getNegative()) {
            return Optional.ofNullable(buildInsight(
                    InsightDto.InsightLevel.DANGER,
                    InsightDto.InsightCode.NEGATIVE_BALANCE,
                    balance.doubleValue(),
                    "VND"
            ));
        }
        return Optional.empty();
    }
    private Optional<InsightDto> categoryInsight(List<CategoryExpenseResponse> categories) {
        return categories.stream()
                .max(Comparator.comparing(CategoryExpenseResponse::getRatioNormalized))
                .filter(c -> c.getRatioNormalized() >= insightConfig.getCategory().getWarnRatio())
                .map(c -> buildInsight(
                        c.getRatioNormalized() >= insightConfig.getCategory().getDangerRatio()
                                ? InsightDto.InsightLevel.DANGER
                                : InsightDto.InsightLevel.WARN,
                        InsightDto.InsightCode.CATEGORY_DOMINANT,
                        c.getRatioNormalized(),
                        "%"
                ));
    }
}
