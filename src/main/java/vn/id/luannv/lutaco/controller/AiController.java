package vn.id.luannv.lutaco.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import vn.id.luannv.lutaco.dto.request.*;
import vn.id.luannv.lutaco.dto.response.AuthenticateResponse;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.enumerate.OtpType;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.service.AuthService;
import vn.id.luannv.lutaco.service.GeminiService;
import vn.id.luannv.lutaco.service.OtpService;
import vn.id.luannv.lutaco.util.EnumUtils;
import vn.id.luannv.lutaco.util.JwtUtils;

import java.util.Date;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final GeminiService geminiService;

    public AiController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return geminiService.askGemini(message);
    }
}