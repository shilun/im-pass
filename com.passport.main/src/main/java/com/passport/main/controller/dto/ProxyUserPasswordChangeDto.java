package com.passport.main.controller.dto;

import lombok.Data;

@Data
public class ProxyUserPasswordChangeDto  {

    private String id;

    private String password;
    private String vpassword;
}
