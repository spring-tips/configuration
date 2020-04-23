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

Lets get started. go to the sprin ginitialir and geenrate a new project and make sure to choose `Spring Cloud Vault`, `Lombok`, and  `Spring Cloud Config Client`. I named my project `configuration`. Go ahead and  click `Generate` the application. Open the project in your favorit IDE. I fyou want to flloow aloong, be sure to disable the Sprin CLou dVault and Spring Clod Config Lcieny dependecies. We dont need them right now.

The first steps mofr most Spring Boot developers is ot use application.properties. The Spring Initializr even puts am empty application.properties in the `src/main/resources/application.properties` folde whn you generate  anew project there! Super conveninent. You _do_ generate your projects on the Spring iNitializr, don't ya?  You could use appication.properties or applicatin.yml. I don't particualrly love yaml files, but you can use it if thats your taste. 

Spring Boot automatically loads applicatino.proeprties whenever it starts up. You can dereference value sfrom the property file in your java code through the environment. Put a propert in `application.properties` file, like this.

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

Incidentally, you could also run the application with configurationy tht live s_outside_ the application, adjacent to the jar, like this. If you run the application liek this, the values in the external `applicatin.properties` will override the values inside the `.jar`.

```shell
.
├── application.properties
└── configuration-0.0.1-SNAPSHOT.jar

0 directories, 2 files

```

Spring Boot is aware of Spring profiles, as well. Profiles are a mechanism that let you tag objects and property files so that they can be seleteively activate or deactivated at runtime. Thi sis great if you want ot have environment specificc configuhratio. Yo ucan tag a spring bean or a configuration file as belonging to a parituclar profile and SPring iwl automatically load it for you when that profile is activated.

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

The `@Data` and `@RequiredArgsConstructor` annotations on the `BootifulProperties` object come from Lombok. `@Data` syntheizes getters for final fields, and etters and setters for non-final fields. `@RequiredArgsConstructor` synthesizes a constructor for all the final fields int he class. The result is an object that's immutable once constructed through constructor initialization. Spring boot's ConfigurationProperties mechanism doesn't know about immutable objects by default, you need to use the `@ConstructorBinding` annotation, a fairly new addition to Spring Boot, to make it do the righ thing hre. this is even more useful in other programing languates like Kotlin (`data class ...`) and Scala (`case class ...`) which have syntax sugar fro creating immutable objects.


We've seen that Spring can load cofguration adjvacent to the application `.jar`, and that it can load the configuration from environment ariables and program arguments. its not hard o get information ito a Spring Boot aplication, but its sort of piecemail. It's hard to version control environment variables or to secure program arguments. 

To solve some of these problems the Spring Coud tam built the spring CLou COnfigu Server. The Spring Cloud Config Server is an HTTP API that fronts a backend storage engine. The storage s pluggable, with the most common being a Git repository thoguht there is suport for others as well. These include SUbversion, a local file system, and even [MongDB](https://github.com/spring-cloud-incubator/spring-cloud-config-server-mongodb). 

We're goin to setup a nw Spring Cloud Cpnfig Server. Go to the Spring Initializr and choose `Config Server` and then click `Generate`. Open it in you favorite IDE.

We're going to need to do two things to make it work: first we must use an annotation and then provide ac ofniguration value to point it to the Git repository with out configuration file.  Here'sw aht your application.properties should look like.

```properties
spring.cloud.config.server.git.uri=https://github.com/joshlong/greetings-config-repository.git
server.port=8888
```

And here's what yor main class should look like.

```java
package com.example.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerApplication.class, args);
	}
}
```

Run the application - `mvn spring-boot:run` or just run the application in your favorite IDe. It's now available. It'll act as a proxy to the Git configuration in teh Github repository. Other clients can then use the Spring clou dCOnfi client to pull their configuration in from the Spring Clloud Config Server which will in turn pull it in from the Gi repository. Note: im making this as insecure as possible for ease of the demo, ut you can and should secur eboth link sin the chain - from the config client ot th eocnfig server, and from the config server to the git repository. Spring Cloud Config Server, the Spring Cloud COnfiug Clientt and Gihub all work well together, and securely. 

Now, go bck to the build for our configuration app and makes rue to uncommment the PSrin fCLoud Config Client dpeendnecy. In order fo rht elcient server, itll need to have some - you guessed it! - confgiuration. A classic chicken and egg problem. This configuration needs to be evaluated earlier, before the rest of the configuration. You can pput this configuration in a file called `bootstrap.properties`. 

You'll need to identify yor application, to give it a name, so that when it connects to th Spring Xloud cnfig Server, it will know hich configuration to give us. The name we specify her will be matches to a property file in the Git repository. Here's what you should put in the file.

```
spring.cloud.config.uri=http://localhost:8888
spring.application.name=bootiful
```

now we can read any value we want in the git repository in the `bootiful.properties` file whose contents are:

```
message-from-config-server = Hello, Spring Cloud Config Server
```


We can pull that confguration file in luke this: 

```java
package com.example.configuration.configclient;

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
	ApplicationRunner applicationRunner(@Value("${message-from-config-server}") String configServer) {
		return args -> {
			log.info("message from the Spring Cloud Config Server: " + configServer);
		};
	}
}
```

You should see the value in the output. Not bad! The Spring Cloud Config Serer does a lot of cool stuff for us. It can encrupt values for us. It can help version out properties. One of my favorite things is that you can change the configuration independant of the change sto the codebase. You can use that in conjunction with the Spring Cloud `@RefreshScope` to dynamcially reconfigure an appkication adter its started running. (I should really do a video on the refresh scope and its myriad many uses...) The Spring Cloud Config Server is among the most popular Sprtin Cloud modules for a rason   - it can be used with monoliths and microservices alike. 

The Sprin fCloud Config Server can encrypt values in the property files, if you configure it appripriately. it works. A lot of folks also use Hashicorp's excellent Vault product which is a much more fully featured offering for security. Vault can secure, store and tightly control access to tokens, passwords, certificates, encryption keys  for protecting secrets and other sensitive data using a UI, CLI, or HTTP API. You can also use this easily as a property source using the Spring Cloud Vault project. Uncomment the Sring Cloud Vault depenecy fro teh build and lets look at setting up Hashicorp Vault. 

Downloa the latest version and then run he following commands. I'm assuming a Linix or Unix-like environment. It should be fairly straightforward to translate to Windows, though. I wont try to explain everything about Vault, Id rfer you to the excellent Getting Statted guides for [Hashicorp Vault](https://learn.hashicorp.com/vault/getting-started/install), instead. Here's the least-secure, but quickest, way I know to get this all setup and working. First, run the Vault server. I'm providing a root token here, but you would normally us the token rprovided by Vault on startup. 

```shell
export VAULT_ADDR="https://localhost:8200"
export VAULT_SKIP_VERIFY=true
export VAULT_TOKEN=00000000-0000-0000-0000-000000000000
vault server --dev --dev-root-token-id="00000000-0000-0000-0000-000000000000"
```

Once that's up, in another cshell, install some values into the Vault server, like this.

```shell
export VAULT_ADDR="http://localhost:8200"
export VAULT_SKIP_VERIFY=true
export VAULT_TOKEN=00000000-0000-0000-0000-000000000000
vault kv put secret/bootiful message-from-vault-server="Hello Spring Cloud Vault"
```

That puts the key `message-from-vault-server` with a value `Hello Spring Cloud Vault` into the Vault service. Now, let's change our application to connect to that Vault instance to read the secure values. Well need a bootstrap.properties, just as with the Sprin Cloud Confg Client. 

```properties
spring.application.name=bootiful
spring.cloud.vault.token=${VAULT_TOKEN}
spring.cloud.vault.scheme=http
```

Thn, you can use the property just like any other ocnfuguration values. 

```java
package com.example.configuration.vault;

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
	ApplicationRunner applicationRunner(@Value("${message-from-vault-server:}") String valueFromVaultServer) {
		return args -> {
			log.info("message from the Spring Cloud Vault Server : " + valueFromVaultServer);
		};
	}
}
```

Now, before you run this, make sure to also have the same three envitronment variables we used in the tow interactions with the `vault` CLI configured: `VAULT_TOKEN`, `VAULT_SKIP_VERIFY`, and `VAULT_ADDR`. Then run it and you should see reflected on the console the value that you write to Hashicorp Vault. 

## Next Steps

Hopefully you've learned something about the colorful and compelling world of configuration in SPring. With this information under your belt, youre now better prpeared to use the other projects that suport property resolution. Armed as you are with a knowledge of how this works and some of the possible appications, you're ready to look at the Spring Cloud Netflux  Archaius integration, or how to use Configmaps with Spring Cloud Kubernetes, or the    Google Runtime Configuration API with Spring Cloud GCP, or the Microsoft Azure Key Vault with Spring Cloud Azure and the hsoted Azure Spring Cloud runtime, etc. I've only mentioned a few offerings here, but it doesn't matter if the list is exhautive, their use will be basically the same if the inegration is done correctly: the cloud's the limit! 