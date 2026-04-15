package vn.id.luannv.lutaco.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.service.GeminiService;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/chat")
    public ResponseEntity<BaseResponse<Void>> chat(@RequestParam String message) {
        return ResponseEntity.ok(
                BaseResponse.success(geminiService.askGemini(message))
        );
    }
}