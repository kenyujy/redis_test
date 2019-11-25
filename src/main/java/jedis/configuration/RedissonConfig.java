package jedis.configuration;

//import org.redisson.Redisson;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//import org.redisson.config.TransportMode;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Component
@Configuration
public class RedissonConfig {

    /*
    @Bean("redissonClient")
    RedissonClient redissonConf(){
        Config config = new Config();
        config.setTransportMode(TransportMode.NIO);
        config.useSingleServer()
                // use "rediss://" for SSL connection
                .setAddress("redis://192.168.56.10:6379");

        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }

     */
}
