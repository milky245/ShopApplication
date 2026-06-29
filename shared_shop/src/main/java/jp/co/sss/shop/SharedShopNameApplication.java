package jp.co.sss.shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SharedShopNameApplication {

	public static void main(String[] args) {
		SpringApplication.run(SharedShopNameApplication.class, args);
	}

}
