package jedis;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test1 {

    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void test2(){

        boolean b= redisTemplate.opsForValue().setIfAbsent("enn","val",10, TimeUnit.SECONDS);
        System.out.println(b);
        String s= (String) redisTemplate.opsForValue().get("enn");
        System.out.println(s);
    }

    @Test
    public void test3(){
        Person p1= new Person("ky", 10000L);
        boolean b= redisTemplate.opsForHash().putIfAbsent("1","list", p1);
        redisTemplate.expire("1",20,TimeUnit.SECONDS);
        System.out.println(b);
        Map<String, Person> map= redisTemplate.opsForHash().entries("1");
        System.out.println(map.get("list"));
    }

    @Test
    public void test5(){
        Map<String, List> map= new HashMap<String, List>();
        map.put("list", Arrays.asList("hello","World"));
        redisTemplate.opsForHash().putAll("key",map);
        redisTemplate.expire("key",20,TimeUnit.SECONDS);
        System.out.println(redisTemplate.opsForHash().entries("key"));
    }

    @Test
    public void test6(){
        Person p1= new Person("ky", 10000L);
        redisTemplate.opsForValue().setIfAbsent("p1",p1,30,TimeUnit.SECONDS);
        System.out.println(redisTemplate.opsForValue().get("p1"));
    }
}

class Person implements Serializable {
    private String name;
    private Long money;

    @Override
    public String toString() {
        return "Person{" +
                "name='" + name + '\'' +
                ", money=" + money +
                '}';
    }

    public Person(String name, Long money) {
        this.name = name;
        this.money = money;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getMoney() {
        return money;
    }

    public void setMoney(Long money) {
        this.money = money;
    }
}