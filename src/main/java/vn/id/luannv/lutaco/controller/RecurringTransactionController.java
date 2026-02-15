package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.RecurringTransactionRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.RecurringTransactionResponse;
import vn.id.luannv.lutaco.service.RecurringTransactionService;

@RestController
@RequestMapping("/api/v1/recurring-transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Recurring Transaction",
        description = "API quản lý giao dịch định kỳ của người dùng"
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class RecurringTransactionController {

    RecurringTransactionService recurringTransactionService;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách giao dịch định kỳ",
            description = "Lấy danh sách giao dịch định kỳ của người dùng hiện tại, hỗ trợ lọc theo nhiều tiêu chí và phân trang"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách giao dịch định kỳ thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<BaseResponse<Page<RecurringTransactionResponse>>> search(
            @Parameter(description = "Điều kiện lọc và phân trang giao dịch định kỳ")
            @Valid  @ModelAttribute RecurringTransactionFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        recurringTransactionService.search(
                                request,
                                request.getPage(),
                                request.getSize()
                        ),
                        "Lấy danh sách giao dịch định kỳ thành công."
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Tạo giao dịch định kỳ mới",
            description = "Tạo mới một giao dịch định kỳ cho người dùng hiện tại"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tạo giao dịch định kỳ thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập")
    })
    public ResponseEntity<BaseResponse<RecurringTransactionResponse>> create(
            @Valid
            @Parameter(description = "Thông tin giao dịch định kỳ cần tạo")
            @RequestBody RecurringTransactionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        recurringTransactionService.create(request),
                        "Tạo giao dịch định kỳ thành công."
                ));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết giao dịch định kỳ",
            description = "Lấy thông tin chi tiết của một giao dịch định kỳ theo id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy chi tiết giao dịch định kỳ thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy giao dịch định kỳ")
    })
    public ResponseEntity<BaseResponse<RecurringTransactionResponse>> getDetail(
            @Parameter(
                    description = "ID giao dịch định kỳ",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        recurringTransactionService.getDetail(id),
                        "Lấy chi tiết giao dịch định kỳ thành công."
                )
        );
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Cập nhật giao dịch định kỳ",
            description = "Cập nhật thông tin giao dịch định kỳ theo id"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật giao dịch định kỳ thành công"),
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy giao dịch định kỳ")
    })
    public ResponseEntity<BaseResponse<Void>> update(
            @Parameter(
                    description = "ID giao dịch định kỳ",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,
            @Valid
            @Parameter(description = "Thông tin giao dịch định kỳ cần cập nhật")
            @RequestBody RecurringTransactionRequest request
    ) {
        recurringTransactionService.update(id, request);
        return ResponseEntity.ok(
                BaseResponse.success("Cập nhật giao dịch định kỳ thành công.")
        );
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Xoá giao dịch định kỳ",
            description = "Xoá giao dịch định kỳ khỏi hệ thống"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Xóa giao dịch định kỳ thành công"),
            @ApiResponse(responseCode = "401", description = "Chưa đăng nhập"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy giao dịch định kỳ")
    })
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(
                    description = "ID giao dịch định kỳ",
                    example = "1",
                    required = true
            )
            @PathVariable Long id
    ) {
        recurringTransactionService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success("Xóa giao dịch định kỳ thành công.")
        );
    }
}
