package com.camplus.login.mappers;

import com.camplus.login.entity.VerificationCode;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

public interface VerificationCodeMapper {

    int insertCode(VerificationCode code);

    VerificationCode selectByTargetAndType(@Param("target") String target, @Param("type") String type);

    int updateCode(@Param("id") Long id, @Param("code") String code, @Param("expireTime") java.time.LocalDateTime expireTime);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int updateErrorCount(@Param("id") Long id, @Param("errorCount") Integer errorCount);

    int deleteExpiredCodes(@Param("now") java.time.LocalDateTime now);
}