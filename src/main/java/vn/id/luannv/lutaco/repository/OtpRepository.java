package vn.id.luannv.lutaco.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.id.luannv.lutaco.entity.Otp;
import vn.id.luannv.lutaco.entity.RefreshToken;
import vn.id.luannv.lutaco.entity.User;
import vn.id.luannv.lutaco.enumerate.OtpType;

import java.util.Optional;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Integer> {
    Otp getOtpsByUserAndOtpType(User user, OtpType otpType);
}
