package vn.id.luannv.lutaco.enumerate;

import lombok.extern.slf4j.Slf4j;
import vn.id.luannv.lutaco.exception.BusinessException;
import vn.id.luannv.lutaco.exception.ErrorCode;

import java.util.Map;

@Slf4j
public enum OtpType {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD
}
