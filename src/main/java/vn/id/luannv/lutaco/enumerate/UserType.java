package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum UserType {
    SYS_ADMIN("config.enum.user.type.sys_admin"),
    ADMIN("config.enum.user.type.admin"),
    USER("config.enum.user.type.user")
    ;
    String display;
}
