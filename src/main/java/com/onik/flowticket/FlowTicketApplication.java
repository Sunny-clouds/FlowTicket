package com.onik.flowticket;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.onik.flowticket.mapper")
@SpringBootApplication
public class FlowTicketApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlowTicketApplication.class, args);
    }

}
