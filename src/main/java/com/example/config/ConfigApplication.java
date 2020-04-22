package com.example.config;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

@Log4j2
@SpringBootApplication
@EnableConfigurationProperties(MessageProperties.class)
public class ConfigApplication {

	@Bean
	ApplicationRunner config(
		@Value("${VAULT_TOKEN}") String vaultToken,
		Environment environment,
		@Value("${program-argument:NO PROGRAM ARGUMENT PROVIDED!!}") String programArgument,
		@Value("${HOME}") String userHome,
		@Value("${username}") String vaultMessage,
		@Value("${message-from-property-source}") String propertySource,
		@Value("${message-from-value}") String value,
		MessageProperties messageProperties) {
		return args -> {
			log.info("secure vault message: " + vaultMessage);
			log.info("$VAULT_TOKEN: " + vaultToken);
			log.info("message from a PropertySource : " + propertySource);
			log.info("from the environment: " + environment.getProperty("message-from-environment"));
			log.info("program-argument: " + programArgument);
			log.info("$HOME=" + userHome);
			log.info("message properties: " + messageProperties.getConfigProperties());
			log.info("message from @Value: " + value);
		};
	}



	@Autowired
	void contributePropertySource(ConfigurableEnvironment configurableEnvironment) {
		configurableEnvironment.getPropertySources().addFirst(this.bootifulPropertySource());
	}

	@Bean
	PropertySource<String> bootifulPropertySource() {
		return new PropertySource<>("bootiful-spring-tips") {
			@Override
			public Object getProperty(String s) {
				if (s.equalsIgnoreCase("message-from-property-source")) {
					return "Hello, PropertySource!";
				}
				return null;
			}
		};
	}

	public static void main(String[] args) {
		SpringApplication.run(ConfigApplication.class, args);
	}

}

@Data
@ConstructorBinding
@ConfigurationProperties("cp")
@RequiredArgsConstructor
class MessageProperties {
	private String configProperties;
}