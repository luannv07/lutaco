package vn.id.luannv.lutaco.enumerate;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RequiredArgsConstructor
public enum UserGender {
    MALE("config.enum.user.gender.male"),
    FEMALE("config.enum.user.gender.female"),
    OTHER("config.enum.user.gender.other")
    ;
    String display;
}
