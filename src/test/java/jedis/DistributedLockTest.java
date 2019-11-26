package jedis;

import org.junit.Test;
import org.junit.runner.RunWith;
//import org.redisson.api.RLock;
//import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/*
 * 各自查询各自的锁 lock+ productid
 * 查各自的数据之前先加上锁
 * 自旋的次数
 * redisson了解
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class DistributedLockTest {

    @Autowired
    RedisTemplate redisTemplate;

    Lock lock=new ReentrantLock();

    /*
    @Autowired
    RedissonClient redissonClient;

    @Test
    public void test1(){
        RLock lock= redissonClient.getLock("lock");

        try{
            lock.lock(6, TimeUnit.SECONDS); //默认阻塞，拿不到lock会阻塞
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            lock.unlock();
        }
        redissonClient.shutdown();
    }
    */
    /*
    * 使用Spring容器项目启动的时候，容器一直在运行中，它的stringRedisTemplate实例一直存在，
    * 不管异步还是同步执行，stringRedisTemplate bean的资源都不会被回收
    * 而使用@SpringBootTest在跑test的时候，虽然同样也是使用Spring管理bean，
    * 但是执行下面这段测试会发现，Spring容器的执行时间是不一样的。主线程跑完后测试结束，
    * Spring容器中的bean被回收，但是子线程还没有结束，这样在执行到stringRedisTemplate操作后拿不到相关的连接
    */
    @Test
    public void test2() throws InterruptedException {

        Thread[] ths= new Thread[1000];
        for(int i=0; i<1000; i++) {
            ths[i]= new Thread(() -> {
                try {
                    for(int j=0; j<100; j++)
                    incrementBy1();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        Arrays.asList(ths).forEach(th->th.start());
        Arrays.asList(ths).forEach(th-> {
            try {
                th.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Test
    public void test6() throws InterruptedException {
        //线程池比开1000个线程跑要快
        ExecutorService service= Executors.newFixedThreadPool(16);
        for(int i=0; i<1000; i++) {
            service.execute(() -> {
                try {
                    for(int j=0; j<100; j++)
                        incrementBy1();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        service.shutdown();
        service.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
    }

    @Test
    public void test3() throws InterruptedException {

        Thread th= new Thread(()->{
            try {
                incrementBy1();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        th.start();
        th.join();
    }

    @Test
    public synchronized void incrementBy() throws InterruptedException { //synchronized 比 ReentrantLock 快
        //lock.lock();
        String uuid = UUID.randomUUID().toString();
        Integer k = (Integer) redisTemplate.opsForValue().get("num");
        redisTemplate.opsForValue().set("num", k + 1);
        //TimeUnit.MILLISECONDS.sleep(20); //睡了一下反而提高性能?
        //lock.unlock();
    }

    @Test
    public void incrementBy1() throws InterruptedException {
        boolean lock=true;
        while (lock) {  //拿不到锁就自旋, 这种方式正确
            String uuid = UUID.randomUUID().toString();
            lock = redisTemplate.opsForValue().setIfAbsent("lock1", "newlock" + uuid, 3, TimeUnit.SECONDS);
            if (lock) {
                Integer k = (Integer) redisTemplate.opsForValue().get("num");
                redisTemplate.opsForValue().set("num", k + 1);
                lock=false;
            }else {
                TimeUnit.MILLISECONDS.sleep(20); //睡了一下反而提高性能?
                lock=true;  //拿不到锁就继续拿
            }
        }
        //String script="if (redis.call('GET', KEYS[1]) == ARGV[1]) then return redis.call('DEL',KEYS[1]) else return 0 end";
        //RedisScript<String> rscript= new DefaultRedisScript<String>(script, String.class);
        //Object o= redisTemplate.execute(rscript, Collections.singletonList("lock1"), "newlock");
        redisTemplate.delete("lock1"); //删除锁
    }

    @Test
    public void getResult(){
        //redisTemplate.opsForValue().set("num",1);
        Integer b= (Integer) redisTemplate.opsForValue().get("num");
        System.out.println(b);
    }

    @Test
    public void printUUID(){
        String uuid= UUID.randomUUID().toString(); //UUID 在线程中取不到值？
        System.out.println(uuid);
    }

}

