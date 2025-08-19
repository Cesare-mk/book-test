package com.mm.book.service;

import com.mm.book.domain.SMSCode;

public interface SMSCodeService {
    public String resolveCode(String telephone);
    public boolean checkCode(SMSCode smsCode);
}
