# push-it

### Usage

```ruby
payload {
    notification {
        aps {
          alert {
            body = "Hello"
            subtitle = "Subtitle"
           }
        }
    }
    headers = hashMapOf(
            "apns-topic" to "org.machinehead.app"
        )
    custom = hashMapOf(
           "custom-property" to "hello custom",
           "blow-up" to true
        )
    stage = DEVELOPMENT
    tokens = arrayListOf("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55")
} push { errorAndResponses ->
          logger.info { "the errors: ${errorAndResponses.clientErrors}" }
          logger.info { "the request errors: ${errorAndResponses.requestErrors}" }
          logger.info { "THE RESPONSES COUNT: ${errorAndResponses.responses.size}" }
          logger.info { "the platform responses: ${errorAndResponses.responses}" }
 }
```

```ruby
val payload = 
payload {
    notification {
        aps {
          alert {
            body = "Hello"
            subtitle = "Subtitle"
           }
        }
    }
    headers = hashMapOf(
            "apns-topic" to "org.machinehead.app"
        )
    custom = hashMapOf(
           "custom-property" to "hello custom",
           "blow-up" to true
        )
    stage = DEVELOPMENT
    tokens = arrayListOf("3c2e55b1939ac0c8afbad36fc6724ab42463edbedb6abf7abdc7836487a81a55")
}

PushIt.with(payload)
{ errorAndResponses ->
        logger.info { "the errors: ${errorAndResponses.clientErrors}" }
        logger.info { "the request errors: ${errorAndResponses.requestErrors}" }
        logger.info { "THE RESPONSES COUNT: ${errorAndResponses.responses.size}" }
        logger.info { "the platform responses: ${errorAndResponses.responses}" }
}
```
#### Testing
The Push It client is tested with the help of ```MockWebServer```.
To achieve this, the property ```localhost.url``` is setting the ```MockWebServer``` url 
to intercept the `OkHttpClient` calls. 
For the tests to run, you need a valid p12 Certificate in your environment variables 
and to make sure that the ```MockWebServer``` is using the *localhost*.
On a MacBook there is a problem with the ``MockWebServer`` cause the local host url
incorporates the user name into the local host.

If that is so, you have to add this to your hosts file as follow:
- ``sudo vi /private/etc/hosts``
-  right after `127.0.0.1	localhost` add a new line
-  ``127.0.0.1	username-macbook-pro.local`` or what ever the test is telling your local host is
##### Create self signed P12
To be able to build and test, a valid certificate is needed. For test purposes, a self signed P12 Certificate can be created and used.
```bash
#!/usr/bin/env bash
PASSWORD=password
KEYSTORE_ENTRY=machinehead
keytool -genkeypair -keystore myKeystore.p12 -storetype PKCS12 -storepass $PASSWORD -alias $KEYSTORE_ENTRY -keyalg RSA -keysize 2048 -validity 99999 -dname "CN=Test SSL, OU=Test Team, O=Machine Head, L=Test City, ST=Test State, C=TE" -ext san=dns:machinehead.com,dns:localhost,ip:127.0.0.1
```

```java

class P12Converter {
    @Throws(IOException::class)
    fun encodeToBase64(fileName: String): String? {
        val file = File(fileName)
        val bytes = Files.readAllBytes(file.toPath())
        return Base64.getEncoder().encodeToString(bytes)
    }
}

fun main() {
    val certBase64 =
        P12Converter().encodeToBase64("your-cert-file.p12")
    println(certBase64)
}
```
- Add the Base 64 Encoded file into the Build Environment assigned to the ``CERTIFICATE`` variable.
- Add the Certificate password into the Build Environment assigned to the ```PASSWORD``` variable.