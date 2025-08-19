package com.mm.book.utils;

import lombok.Data;

@Data
public class Result {
    private boolean flag;
    private Object data;

    public Result (){

    }
    public Result (boolean flag){
        this.flag = flag;
    }

    public Result (boolean flag, Object data){
        this.flag = flag;
        this.data = data;
    }
}
