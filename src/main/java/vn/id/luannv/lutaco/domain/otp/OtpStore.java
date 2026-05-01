package vn.id.luannv.lutaco.domain.otp;

import vn.id.luannv.lutaco.enumerate.OtpType;

public interface OtpStore {
    void save(OtpType type, String identifier, OtpInfo otp);

    OtpInfo get(OtpType type, String identifier);

    void delete(OtpType type, String identifier);
}
