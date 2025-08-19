package com.mm.book.domain;

import lombok.Data;


/**
 * 手机验证码的简单实体
 */
@Data
public class SMSCode {
    private String telephone;
    private String code;
}
