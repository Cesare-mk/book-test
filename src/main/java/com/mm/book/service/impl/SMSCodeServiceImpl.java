package com.mm.book.service.impl;

import com.mm.book.domain.SMSCode;
import com.mm.book.service.SMSCodeService;
import com.mm.book.utils.SMSCodeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * 验证码接口实现
 */
@Service
public class SMSCodeServiceImpl implements SMSCodeService {
    @Autowired
    private SMSCodeUtils smsCodeUtils;
    @Override
    //@Cacheable(value = "SMSCode", key="#telephone")
    @CachePut(value = "SMSCode",key = "#telephone")
    public String resolveCode(String telephone) {

        return smsCodeUtils.generator(telephone);
    }

    @Override
    public boolean checkCode(SMSCode smsCode) {
        /**
         * 从缓存里取出验证码和当前验证码比对
         */
        String code = smsCode.getCode();
        String cacheCode = smsCodeUtils.getCacheCode(smsCode.getTelephone());
        return code.equals(cacheCode);
    }
}
