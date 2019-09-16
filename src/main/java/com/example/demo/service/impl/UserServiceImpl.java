package com.example.demo.service.impl;

import com.example.demo.common.PasswordUtils;
import com.example.demo.common.entity.User;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@PropertySource("classpath:application.properties")
public class UserServiceImpl implements UserService {

    @Autowired
    private RedisTemplate<String, User> redisTemplate;

    @Value("${server.port}")
    private String port;

    @Override
    public void addUser(User user) {
        user.setPassword(PasswordUtils.saltAndMd5(user.getUsername(),user.getPassword()));  // 加密
        redisTemplate.boundHashOps("users").put(user.getUsername(), user);
    }

    @Override
    public User login(User user) {
        user.setPassword(PasswordUtils.saltAndMd5(user.getUsername(),user.getPassword()));  // 加密
        User u = (User) redisTemplate.boundHashOps("users").get(user.getUsername());
        if (u == null || !check(user, u)){
            return null;
        }
        return u;
    }

    @Override
    public List<User> getUsers() {
        List<Object> list = redisTemplate.boundHashOps("users").values();
        List<User> users = new ArrayList<>();
        list.forEach(u->{
            User u1 = (User) u;
            u1.setShow(port);
            users.add(u1);
        });
        return users;
    }

    private boolean check(User a, User b){
        if (a.getUsername().equals(b.getUsername()) && a.getPassword().equals(b.getPassword())){
            return true;
        }
        return false;
    }
}
