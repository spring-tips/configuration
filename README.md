# Configuration 

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

```