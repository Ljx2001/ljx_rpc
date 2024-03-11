package ljx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author LiuJixing
 * @Date 10/3/2024
 */
@SpringBootApplication
@RestController
public class ApplicationConsumer {
    public static void main(String[] args) {
        SpringApplication.run(ApplicationConsumer.class,args);
    }
    @GetMapping("test")
    public String Hello(){
        return "hello consumer";
    }
}
