package vn.id.luannv.lutaco.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.MasterDictionaryFilterRequest;
import vn.id.luannv.lutaco.dto.request.MasterDictionaryRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.dto.response.MasterDictionaryResponse;
import vn.id.luannv.lutaco.service.MasterDictionaryService;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/v1/master-dictionaries")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('SYS_ADMIN') and @securityPermission.isActive()")
public class MasterDictionaryController {

    MasterDictionaryService masterDictionaryService;

    @GetMapping
    public ResponseEntity<BaseResponse<Page<MasterDictionaryResponse>>> search(
            @Valid @ModelAttribute MasterDictionaryFilterRequest request) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        masterDictionaryService.search(request, request.getPage(), request.getSize()),
                        "Lấy danh sách từ điển thành công."));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<MasterDictionaryResponse>> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        masterDictionaryService.getDetail(id),
                        "Lấy chi tiết từ điển thành công."));
    }

    @GetMapping("/group/{dictGroup}")
    public ResponseEntity<BaseResponse<List<MasterDictionaryResponse>>> getByGroup(
            @PathVariable @NotBlank(message = "{validation.required}") String dictGroup,
            @RequestParam(required = false) Boolean activeFlg) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        masterDictionaryService.getByGroup(dictGroup, activeFlg),
                        "Lấy danh sách từ điển theo nhóm thành công."));
    }

    @PostMapping
    public ResponseEntity<BaseResponse<MasterDictionaryResponse>> create(
            @Valid @RequestBody MasterDictionaryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                BaseResponse.success(
                        masterDictionaryService.create(request),
                        "Tạo từ điển thành công."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<MasterDictionaryResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody MasterDictionaryRequest request) {
        return ResponseEntity.ok(
                BaseResponse.success(
                        masterDictionaryService.update(id, request),
                        "Cập nhật từ điển thành công."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id) {
        masterDictionaryService.deleteById(id);
        return ResponseEntity.ok(
                BaseResponse.success("Xoá từ điển thành công."));
    }
}
