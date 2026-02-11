package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.TransactionFilterRequest;
import vn.id.luannv.lutaco.dto.request.TransactionRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.TransactionResponse;
import vn.id.luannv.lutaco.service.TransactionService;
import vn.id.luannv.lutaco.util.LocalizationUtils;
import vn.id.luannv.lutaco.util.SecurityUtils;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Tag(
        name = "Transaction API",
        description = "API quản lý giao dịch tài chính của người dùng"
)
@PreAuthorize("isAuthenticated() and @securityPermission.isActive()")
public class TransactionController {

    TransactionService transactionService;
    LocalizationUtils localizationUtils;

    @GetMapping
    @Operation(
            summary = "Lấy danh sách giao dịch",
            description = "Lấy danh sách giao dịch của người dùng hiện tại, hỗ trợ lọc theo nhiều tiêu chí và phân trang"
    )
    public ResponseEntity<BaseResponse<Page<TransactionResponse>>> search(
            @Parameter(description = "Điều kiện lọc và phân trang giao dịch")
            @ModelAttribute TransactionFilterRequest request
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        transactionService.search(
                                request,
                                request.getPage(),
                                request.getSize()
                        ),
                        localizationUtils.getLocalizedMessage(MessageKeyConst.Success.SENT)
                )
        );
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Lấy chi tiết giao dịch",
            description = "Lấy thông tin chi tiết của một giao dịch theo id"
    )
    public ResponseEntity<BaseResponse<TransactionResponse>> getDetail(
            @Parameter(
                    description = "ID giao dịch",
                    example = "TXN_123456",
                    required = true
            )
            @PathVariable String id
    ) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        transactionService.getDetail(id),
                        localizationUtils.getLocalizedMessage(MessageKeyConst.Success.SENT)
                )
        );
    }

    @PostMapping
    @Operation(
            summary = "Tạo giao dịch mới",
            description = "Tạo mới một giao dịch cho người dùng hiện tại"
    )
    public ResponseEntity<BaseResponse<TransactionResponse>> create(
            @Valid
            @Parameter(description = "Thông tin giao dịch cần tạo")
            @RequestBody TransactionRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        transactionService.customCreate(request, SecurityUtils.getCurrentId()),
                        localizationUtils.getLocalizedMessage(MessageKeyConst.Success.CREATED)
                ));
    }

    @PostMapping("/bulk")
    @Operation(
            summary = "Tạo nhiều giao dịch cùng lúc",
            description = "Tạo nhiều giao dịch cho người dùng hiện tại trong một lần gọi API"
    )
    public ResponseEntity<BaseResponse<List<TransactionResponse>>> createBulk(
            @Valid
            @Parameter(description = "Danh sách thông tin giao dịch cần tạo")
            @RequestBody List<TransactionRequest> requests
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.success(
                        transactionService.createBulk(requests, SecurityUtils.getCurrentId()),
                        localizationUtils.getLocalizedMessage(MessageKeyConst.Success.CREATED)
                ));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Cập nhật giao dịch",
            description = "Cập nhật thông tin giao dịch theo id"
    )
    public ResponseEntity<BaseResponse<Void>> update(
            @Parameter(
                    description = "ID giao dịch",
                    example = "TXN_123456",
                    required = true
            )
            @PathVariable String id,
            @Valid
            @Parameter(description = "Thông tin giao dịch cần cập nhật")
            @RequestBody TransactionRequest request
    ) {
        transactionService.update(id, request);
        return ResponseEntity.ok(
                BaseResponse.success(localizationUtils.getLocalizedMessage(MessageKeyConst.Success.UPDATED))
        );
    }

    @PatchMapping("/{id}/{walletId}/disable")
    @Operation(
            summary = "Xoá giao dịch (soft delete)",
            description = "Đánh dấu giao dịch là không còn hiệu lực, không xoá vật lý khỏi hệ thống"
    )
    public ResponseEntity<BaseResponse<Void>> delete(
            @Parameter(
                    description = "ID giao dịch",
                    example = "05675404-6a6e-4478-b8fc-05ff6c8b7da2",
                    required = true
            )
            @PathVariable String id,
            @Parameter(
                    description = "ID ví",
                    example = "ac4a7a82-ccc1-459b-8fc5-1b7836713f52",
                    required = true
            )
            @PathVariable String walletId
    ) {
        transactionService.deleteByIdAndWalletId(id, walletId);
        return ResponseEntity.ok(
                BaseResponse.success(localizationUtils.getLocalizedMessage(MessageKeyConst.Success.UPDATED))
        );
    }

    @PatchMapping("/{id}/{walletId}/enable")
    @Operation(
            summary = "Phục hồi giao dịch (undo delete)",
            description = "Phục hồi giao dịch, dành cho premium user"
    )
    @PreAuthorize("@securityPermission.isPremiumUser()")
    public ResponseEntity<BaseResponse<Void>> unDelete(
            @Parameter(
                    description = "ID giao dịch",
                    example = "05675404-6a6e-4478-b8fc-05ff6c8b7da2",
                    required = true
            )
            @PathVariable String id,
            @Parameter(
                    description = "ID ví",
                    example = "ac4a7a82-ccc1-459b-8fc5-1b7836713f52",
                    required = true
            )
            @PathVariable String walletId
    ) {
        transactionService.restoreTransaction(id, walletId);
        return ResponseEntity.ok(
                BaseResponse.success(localizationUtils.getLocalizedMessage(MessageKeyConst.Success.UPDATED))
        );
    }
}
