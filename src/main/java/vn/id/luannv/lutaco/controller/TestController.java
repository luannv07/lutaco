package vn.id.luannv.lutaco.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.id.luannv.lutaco.constant.MessageKeyConst;
import vn.id.luannv.lutaco.dto.request.TokenRequest;
import vn.id.luannv.lutaco.dto.response.BaseResponse;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.jwt.JwtService;
import vn.id.luannv.lutaco.mapper.UserMapper;
import vn.id.luannv.lutaco.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    JwtService jwtService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserMapper userMapper;

    @GetMapping("/ip")
    public String testIp() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        return request.getRemoteAddr();
    }
    @PostMapping("/token")
    public ResponseEntity<BaseResponse<Map<String, Object>>> testAuth(@RequestBody TokenRequest token) {
        log.debug("testAuth {}", token);

        String username = jwtService.getUsernameFromToken(token.getToken());
        User user = userRepository.findByUsername(username).get();
        Map<String, Object> data = new HashMap<>();

        data.put("user", userMapper.toResponse(user));
        data.put("jwt", jwtService.getClaimsFromToken(token.getToken()));

        return ResponseEntity.status(HttpStatus.OK)
                .body(BaseResponse.success(data, MessageKeyConst.Success.SENT));
    }
}
