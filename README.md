# Configuration 
<!-- 
* property values from the Environment 
* property values from `application.properties`
* property values from profiles (`application-default.properties`, `application-dev.properties`)
* default property values `${foo:default}`
* property values from the environment variables (`$HOME`) (12 factor config)
* property values from `--`, adjacent `application.properties`, JNDI, etc
* property source from a custom `PropertySource` by customizing the `SpringApplicationBuilder()`
* `@ConfigurationProperties` && `@ConstructorBinding`
* config server 
* vault 

```bash

export VAULT_ADDR="https://localhost:8200"
export VAULT_SKIP_VERIFY=true
export VAULT_TOKEN=00000000-0000-0000-0000-000000000000
vault server --dev --dev-root-token-id="00000000-0000-0000-0000-000000000000"

export VAULT_ADDR="http://localhost:8200"
export VAULT_SKIP_VERIFY=true
export VAULT_TOKEN=00000000-0000-0000-0000-000000000000
vault kv put secret/bootiful message-from-vault-server="Hello Vault"

``` -->

Hi, spring fans! welcome to another installment of Spring tips! in this installment were going to tlook at something thats rather foundationa, sna something that i wish id adressed earlier: configuratino. And no i dont mean functional configuration or java configuration or anyting like th, im taling about the string values that inform how your code eecutes. the stuff that yo put in application.properites. _that_ configuration. 

All confuguration in Spring emininats fromt he Spring `Environment` abstractin. the environment is sort of like a dictionary  a map with keys an values. Environment is just an interface through which we can ask questions about, you know, the environnet. the abstraction lives in Spring Framework, and was introduced in Spring 3, more than a decade ago. up intil that point there was a focued mechanism to allowe integratino of configueation caled property placeholder resolition. tzhi snvironment mecanism and the constellation of classes around that interface more than supercede that old support. if you find a blog still using thos types, may i suggest u move on to newer and greener pastures? :) 

Lets get started. go to the sprin ginitialir and geenrate a new project and make sure to choose `Spring Cloud Vault` and choose `Spring Cloud Config Client`. I named my project `configuration`. Go ahead and  click `Generate` the application. Open the project in your favorit IDE. I fyou want to flloow aloong, be sure to disable the Sprin CLou dVault and Spring Clod Config Lcieny dependecies. We dont need them right now.

The first steps mofr most Spring Boot developers is ot use application.properties. The Spring Initializr even puts am empty application.properties in the `src/main/resources/application.properties` folde whn you generate  anew project there! Super conveninent. You _do_ generate your projects on the Spring iNitializr, don't ya?  You could use appication.properties or applicatin.yml. I don't particualrly love yaml files, but you can use it if thats your taste. 

Spring Boot automatically loads applicatino.proeprties whenever it starts up. You can dereference value sfrom the property file in your java code through the environment. Put a propert in   `application.properties` file, like this.

```properties
message-from-application-properties=Hello from application.properties
```

Now, let's edit the code to read in that value. 

```java
package com.example.configuration;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigurationApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(Environment environment) {
		return args -> {
			log.info("message from application.properties " + environment.getProperty("message-from-application-properties"));
		};
	}
}
```

Run this and youll see the value form the configueation property file in the output of the log. If you want to change which file SPring Boot rads by defaut, you can do that too. it's a chicken and egg prpblem tho - you need to specify a property that Spring Boot wil luse to figue out where to load all the properties. So you need to speciffy this outside of the application.properties file. You can use a program argument or an envirtonment vatiable to specify the `spring.config.name` property. 

```shell 
export SPRING_CONFIG_NAME=foo
```

Re-run the application now with that environment variable in scope  and it'll fail becaue itll try to load `foo.properties`, not `application.properties`.

Spring Boot is aware of PSring profiles, as wel. Profiles are a mechanism that let you tag objects and property files so that they can be seleteively activate or deactivated at runtime. Thi sis great if you want ot have environment specificc configuhratio. Yo ucan tag a spring bean or a configuration file as belonging to a parituclar profile and SPring iwl automatically load it for you when that profile is activated.

Profile names are, basically, arbitrary. there are some profiles that are magic - that Spring honors in a parituclar way. But generally, the names areup tp you. i find it very useful to map my profiles to differen tenvironemtns, dev, qa, staging, prod, etc.


Let's say that theres a profile called `dev`. Spring Boot will automatically load `application-dev.properties`. It'll laod that in addition to applicatin.properties. fi there are any conflcits between values in the the two files, then more specific file - the one with the profile - wins. you could have a default value that applies absent a partiular profike, and then provide specifcs in the config for a prfile. 


You can activate a given profile a number o didferent ways but the easiest is to just speicfy it on the command line. or you could turn it on in your IDE's run configurations dialog box. IntelliJ and Spring Tool Suite bothprovide a palce to specify the profile to sue when running the application. You can also set an env var, `SPRING_PROFILES_ACTIVE`, or specify an argument on the command line `--spring.profiles.active`. Either one acepts a comma delimited list of profiles - you  can activate more than on eprofle at a time. 


Le'ts try that out. 

Creat sa file called application-dev.properiotes. Put the following value in to it.


```properties
message-from-application-properties=Hello from dev application.properties
```

Thi shas the same keuy as the one in application.properties. The java code here is identical to what we had before. Jut be sure to specificy the prfile before you start the Spring aplication. You can use the envronment variable, properties, etc. You can even speicfy it programmatically when building the `SpringApplication` in the `main()` method. 

```java
package com.example.configuration.profiles;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

	public static void main(String[] args) {
		// this works
		// export SPRING_PROFILES_ACTIVE=dev  
		// System.setProperty("spring.profiles.active", "dev"); // so does this
		new SpringApplicationBuilder()
			.profiles("dev") // and so does this
			.sources(ConfigurationApplication.class)
			.run(args);
	}

	@Bean
	ApplicationRunner applicationRunner(Environment environment) {
		return args -> {
			log.info("message from application.properties " + environment.getProperty("message-from-application-properties"));
		};
	}
}
```

Run the applicatino and youll see the speciflized mesage reflected in the output. 

So far weve been using the ENvironment to inject the configuratio. you can also use `@Value` annotation to inject the value as a parameter. You probabl already knw tha. but di you know that youc an also specify default valeus to be returned if the re are no other values that match? There are a lot of reason why you might want to do this. yo could use it prive fallback values and to mak eit clearer when somebody fat fingers the spelling of a property. Its also useful becaue you are given a vlae tahat might be sueful if somebody doesnt know thay they need to ativate a pofil or somethign,.


```java
package com.example.configuration.value;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigurationApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(
		@Value("${message-from-application-properties:OOPS!}") String valueDoesExist,
		@Value("${mesage-from-application-properties:OOPS!}") String valueDoesNotExist) {
		return args -> {
			log.info("message from application.properties " + valueDoesExist);
			log.info("missing message from application.properties " + valueDoesNotExist);
		};
	}
}
```



Convenient, eh? Also, note that the default String that you providee can in turn interpolate some other property. So yuo could do somethinng like this, assumign a key like `default-error-message` does exist somewehre in your applicatin configration: 

`${message-from-application-properties:${default-error-message:YIKES!}}`

That will evaluate the first peoprty if it exists, then the second and then the String `YIKES!`, fianlly. 

Earlier, we looked at how to speicfy a profile using an environment atiable or program argument. this mechanism - configuraing SPring bOot with envrionent variables or program aguments - is actually general purppose. You can use it for any arbitrary key. Sprig Boot will normalize hte configuration for you. Ay key that you would out in application.properties can be speificed externally in this way. Let's see some eamples. Let's sppose you want to specify the URL for a datasource connectin. You _cold_ harcode that  vale in the application.properties, ut thats not very secure. might be miuch better to sintead create an env variable that only exists in production. that way the develiopers dont have caccess to the keys to the production database and so on. 

Let's try it out. Heres the java code fo the example. 

```java

package com.example.configuration.envvars;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

	public static void main(String[] args) {
		// simulate program arguments
		String[] actualArgs = new String[]{"spring.datasource.url=jdbc:postgres://localhost/some-prod-db"};
		SpringApplication.run(ConfigurationApplication.class, actualArgs);
	}

	@Bean
	ApplicationRunner applicationRunner(Environment environment) {
		return args -> {
			log.info("our database URL connection will be " + environment.getProperty("spring.datasource.url"));
		};
	}
}

```

Befoer eou run it be sure to either export an environment variable in the shell that you use to run  your application  or to specify a program argument.  I simulate the latter - the program arguments - by intercepting the `public static void main(String [] args)` that we pass into the Spring Boot application here. You can also specufy an env variable like this: 

```shell
export SPRING_DATASOURCE_URL=some-arbitrary-value
mvn -DskipTests=true spring-boot:run 
```

Run the program multple times, trying out the differnet approaches, adn you ll sethe rrules in the output. Rheres no autoconfiguration in the applicatino that will connect to a database, so were using this propety as an example. the url doesnt have to be a valid url (at least not unitl you add Spring's JDBC support and a JDBC driver to the classpath).

What shoul dbe implied in all of this is that spring Boot is very flexible in its sourcing of the values. it doesnt care if you do `SPRING_DATASOURCE_URL`, `spring.datasource.url`, etc. Spring Boot calls this _relaxed binding_. It allows you to do things in a way that's most natural for idfferent environments, while still working for SPring Boot.

This idea - of extnelaizing cofnigutation for an application from the envrionemtn - is not a nw idea. its well understood and escribed in the 12 factor manifesto. the 12 factor manifesto says that enviroennt specifi cocnfi should live in tat enironment, not in the code itself. this is because we want one build for all the environemtns. things that change should be external. So fr weve seen that Spring Boot can pull in confguration from the comand line arguments (program arguments) , and from  environment vatiables. ti can also be made to read in confguration foming from JOpt or even from a JNDI contert if you happen to be running in an application server with one of those around! 

Spring Boots abilityt o pull in any environment variable is very helful here. It's also more secure than using progteam arguments because the program arguments will show p in the output of tools like `ps axu`. Environment variables are a better fit.

So far weve seent hat Spring Boot can pull in confiureatino from a lot ofdifferent places. It knows about profiles, it knows about `.yml` and `.properties`.  It's pretyt flexivile! But what if it doesnt now how to do what yo uwant it to do? You an easily reach it new tricks using a custom `PropertySource<T>`. Yo might want to do something like thisif you want, for example to integrate your application with cnfiguatino youre storing in an external database or a directory or someth other thing tht Spting Boot doesnt automatically know about. 

```java

package com.example.configuration.propertysource;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.PropertySource;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder()
			.sources(ConfigurationApplication.class)
			.initializers(context -> context
				.getEnvironment()
				.getPropertySources()
				.addLast(new BootifulPropertySource())
			)
			.run(args);
	}

	@Bean
	ApplicationRunner applicationRunner(@Value("${bootiful-message}") String bootifulMessage) {
		return args -> {
			log.info("message from custom PropertySource: " + bootifulMessage);
		};
	}
}

class BootifulPropertySource extends PropertySource<String> {

	BootifulPropertySource() {
		super("bootiful");
	}

	@Override
	public Object getProperty(String name) {

		if (name.equalsIgnoreCase("bootiful-message")) {
			return "Hello from " + BootifulPropertySource.class.getSimpleName() + "!";
		}

		return null;
	}
}


```

The xample above is the safest way to register a `PropertySource` early enough on that everything that needs it will be able to find it. You can also do it at runtime, when Spring has started wiring objects together and you have acccess to configured objects, but i woulnt be sure that this will work in every situation. Heres how that might look . 


```java
package com.example.configuration.propertysource;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

@Log4j2
@SpringBootApplication
public class ConfigurationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigurationApplication.class, args);
	}

	@Bean
	ApplicationRunner applicationRunner(@Value("${bootiful-message}") String bootifulMessage) {
		return args -> {
			log.info("message from custom PropertySource: " + bootifulMessage);
		};
	}

	@Autowired
	void contributeToTheEnvironment(ConfigurableEnvironment environment) {
		environment.getPropertySources().addLast(new BootifulPropertySource());
	}
}

class BootifulPropertySource extends PropertySource<String> {

	BootifulPropertySource() {
		super("bootiful");
	}

	@Override
	public Object getProperty(String name) {

		if (name.equalsIgnoreCase("bootiful-message")) {
			return "Hello from " + BootifulPropertySource.class.getSimpleName() + "!";
		}

		return null;
	}
}
```

Thus far weve looked almost entirely at how to souce property values from elsewhere, but we havent really talked about what becomes of th Strings once theyre in our working memory and available for us in th eaplicatin. Most of the time theyre just strings and we can ise them asis. Sometimes, however its useful to trn them into other type sof vaues - ints, Dates, doubles, etc. this work - turning strings into things - could be the topc of a whole other Spring Tips vieo and perhaps one ill do soon. Sufficie it to say that there are a lot of interellated pieces there - the `ConversionService`, `Converter<T>`s, Spring Boot's `Binder`s and so much more. For the comon times, this will just work. You can for example specify a property `server.port = 8080` and then inject it into your application as an int:

```java
@Value("${server.port}") int port
```

I tmight be nice to hve these values bound to an ojbect automatically. thi sis exactly what Spring bOots `ConfigutationProeprties` do for you. Let's see this ina ction. 

Ley's say that ou ave an application.properties file wih the follwoing property:

```property
bootiful.message = Hello from a @ConfiguratinoProperties 
```

Then you can run the application and see that the configuration value has been bound to the object for us: 

```java
package com.example.configuration.cp;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@Log4j2
@SpringBootApplication
@EnableConfigurationProperties(BootifulProperties.class)
public class ConfigurationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigurationApplication.class, args);
	}
	
	@Bean
	ApplicationRunner applicationRunner(BootifulProperties bootifulProperties) {
		return args -> {
			log.info("message from @ConfigurationProperties " + bootifulProperties.getMessage());
		};
	}
 
}

@Data
@RequiredArgsConstructor
@ConstructorBinding
@ConfigurationProperties("bootiful")
class BootifulProperties {
	private final String message;
}

```

