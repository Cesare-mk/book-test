package com.mm.book.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mm.book.domain.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookDao extends BaseMapper<Book> {
}
