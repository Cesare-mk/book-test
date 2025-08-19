package com.mm.shoppingcat;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mm.book.ShoppingCatApplication;
import com.mm.book.domain.Book;
import com.mm.book.domain.Users;
import com.mm.book.service.IBookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

@SpringBootTest(classes = ShoppingCatApplication.class)
class ShoppingCatApplicationTests {
    //private static final ObjectMapper mapper = new ObjectMapper();
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void setTestString() throws JsonProcessingException {

        //创建对象
        Users user = new Users("fastjson", 58, "female");
        //序列化
        String userToJson = JSON.toJSONString(user);
        //String userToJson = mapper.writeValueAsString(user);
        //写入数据
        stringRedisTemplate.opsForValue().set("com:user:7", userToJson);
        //获取数据
        String jsonToUser = stringRedisTemplate.opsForValue().get("com:user:7");
        //序列化
        Users reUser = JSON.parseObject(jsonToUser, Users.class);
        //Users reUser = mapper.readValue(jsonToUser, Users.class);
        System.out.println(reUser);


    }


    /*//使用自定义的序列化器。package com.mm.book.config.RedisConfiguration;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    public void setValue() {
        redisTemplate.opsForValue().set("com:user:5", new Users("stan", 33, "male"));
        Users users = (Users)redisTemplate.opsForValue().get("com:user:5");
        System.out.println("user: " + users);
    }
*/
    @Autowired
    private IBookService iBookService;

    @Test
    void updateTest() {
//        IPage<Book> page = iBookService.getPage(3,3);
        Page<Book> page1 = new Page<>(2, 3);
        iBookService.page(page1);
        System.out.println("当前页码" + page1.getCurrent());//当前页码
        System.out.println("当前页大小" + page1.getSize());//当前页大小
        System.out.println("总页数" + page1.getPages());//总页数
        System.out.println("总记录数" + page1.getTotal());//总记录数
        System.out.println("具体数据" + page1.getRecords());//具体数据
        System.out.println("当前page用的那个类" + page1.getClass());
    }

    /*
        @Autowired
        private RedisTemplate redisTemplate;
        @Test
        void set(){
            ValueOperation ops = redisTemplate.opsForValue();
            ops.set("test","springboot-redis");
        }
        @Test
        void get(){
            ValueOperation ops = redisTemplate.opsForValue();
            Object obj = ops.get("test");
            System.out.println(obj);

        }*/
}
