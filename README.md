# GoOutTogether Backend Project

## Related Command

### Build jar and get OpenTelemetry Agent
```shell
    ./gradlew clean build
```

### Generate RSA Keypair
```shell
openssl genrsa -out private_key.pem 4096
openssl rsa -pubout -in private_key.pem -out public_key.pem
openssl pkcs8 -topk8 -in private_key.pem -inform pem -out private_key_pkcs8.pem -outform pem -nocrypt
```