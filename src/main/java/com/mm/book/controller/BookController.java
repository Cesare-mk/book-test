package com.mm.book.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.mm.book.domain.Book;
import com.mm.book.domain.SMSCode;
import com.mm.book.service.IBookService;
import com.mm.book.service.SMSCodeService;
import com.mm.book.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

/**
 * @author
 */
@Slf4j
@RestController
@RequestMapping("/books")
public class BookController {
    @Autowired
    private IBookService iBookService;
    @Autowired
    private SMSCodeService smsCodeService;

    @GetMapping
    public Result getAll() {
        return new Result(true, iBookService.list());
    }

    @PostMapping
    public Result save(@RequestBody Book book) {

        return new Result(iBookService.save(book));
    }

    @PutMapping()
    public Result update(@RequestBody Book book) {

        return new Result(iBookService.modify(book));
    }

    @DeleteMapping("{id}")
    public Result removeUsers(@PathVariable Integer id) {

        return new Result(iBookService.removeById(id));
    }
    @Cacheable(value = "booksList",key = "#id")
    @GetMapping("{id}")
    public Result getUser(@PathVariable Integer id) {

        return new Result(true, iBookService.getById(id));
    }

  /*  @GetMapping("{currentPage}/{pageSize}")
    public Result getPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize) {
        IPage<Book> page = iBookService.getPage(currentPage, pageSize);
        //判断当前页是否大于总页数,大于就取最大页作为当前页
        if (currentPage > page.getPages()) {
            page = iBookService.getPage((int) page.getPages(), pageSize);
        }
        return new Result(true, page);
    }*/
  @GetMapping("{currentPage}/{pageSize}")
  public Result getPage(@PathVariable Integer currentPage, @PathVariable Integer pageSize, Book book) {
      IPage<Book> page = iBookService.getPage(currentPage, pageSize,book);
      //判断当前页是否大于总页数,大于就取最大页作为当前页
      if (currentPage > page.getPages()) {
          page = iBookService.getPage((int) page.getPages(), pageSize,book);
      }
      return new Result(true, page);
  }

}
