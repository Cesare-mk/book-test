package com.mm.book.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book implements Cloneable {
    private Integer id;
    private String type;
    @TableField("name")
    private String name;
    private String author;
    private String description;
    private Double price;
    @TableLogic
    private Integer deleted;
    //@Version
    private Integer version;

}
