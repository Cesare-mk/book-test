package com.mm.book.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.mm.book.domain.Book;


/**
 * @author 马宇
 */
public interface IBookService extends IService<Book> {
    boolean modify(Book book);
    boolean save(Book book);
    IPage<Book> getPage(Integer currentPage, Integer pageSize, Book book);

    /**
    * 通过页码和页大小，以及条件查询的实体做参数，进行查询
     */

    IPage<Book> getPage(Integer currentPage, Integer pageSize);
}
