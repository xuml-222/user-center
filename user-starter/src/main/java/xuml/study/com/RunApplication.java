package xuml.study.com;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.freemarker.FreeMarkerAutoConfiguration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@SpringBootApplication(exclude = { FreeMarkerAutoConfiguration.class },scanBasePackages = {"xuml.study.com.*"})
@EnableAsync
@MapperScans(@MapperScan({"xuml.study.com.**.mapper*"}))
public class RunApplication {

    public static void main(String[] args) throws UnknownHostException {
        Environment env = SpringApplication.run(RunApplication.class, args).getEnvironment();
        String ip = InetAddress.getLocalHost().getHostAddress();
        String port = env.getProperty("server.port");
        log.info("\n----------------------------------------------------------\n\t" +
                "Application is running! Access URLs:\n\t" +
                "Local: \t\thttp://localhost:" + port  + "/\n\t" +
                "External: \thttp://" + ip + ":" + port  + "/\n\t" +
                "----------------------------------------------------------");
    }
}
