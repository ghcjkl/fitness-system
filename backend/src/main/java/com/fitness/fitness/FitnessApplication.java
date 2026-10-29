package com.fitness.fitness;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 个性化健康饮食运动管理系统 启动入口
 */
@SpringBootApplication
// 扫描所有mapper接口，避免每个mapper加@Mapper注解
@MapperScan("com.fitness.fitness.mapper")
public class FitnessApplication {
	public static void main(String[] args) {
		SpringApplication.run(FitnessApplication.class, args);
	}
}