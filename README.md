# push-it
### General
This project is first and foremost my playground to learn the Kotlin language.
It is fully functional project that can be used to push iOS notifications to Apple devices using Kotlin.
It may or may not be usable for Java, I have never tried to use it from within Java project.
The project is tested on live iOS devices registered on APNS Sandbox and never with the APNS Production stage.
Since pushing on APNS Production is only changing the URL for the service, this should as well work for Production.
Artifact on Maven or jCentral may come with the time, for now include the built jar into your local Artifactory. 
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
- client errors are reported when something went wrong when the Ok Client and the Request were in creation. It also reports wrong validated payloads.
- request errors are mapped to a device token and contains the network error that might have occurred when APNS communication failed.
- APNS responses are also mapped to a device token and can be errors that APNS is communicating downstream. The APNS errors 
  are in this case JSON string with single field called ``reason`` that contains one of the values listed on the APNS developer site.
### Custom Credential manager
Authentication on APNS is achieved with the help of P12 certificates. The default certificate manager expects the certificate in a ```CERTIFICATE```
environment variable and it's content encoded in base 64 format. How to do that, see below. The password for the certificate is expected to be given in the
```PASSWORD``` environment variable.
This comes from the idea to store the certificate and password contents in a OpenShift Secrets. If you don't use OpenShift or you need another way 
to provide the certificate, you can provide your own certificate manager provided you implement the following interface
```ruby
interface CredentialsManager {
    fun credentials(): Either<ClientError, Pair<SSLSocketFactory, X509TrustManager>>
}
```
To use this custom credentials manager, you first need to instantiate the PushIt Class
```ruby
val pushIt = PushIt()
pushIt.credentialsManager = myCustomCredentialsManager
pushIt.with(myPayload)
```
Future versions may provide more declarative way to do this and maintain the declarative use of the library as shown in the example above.
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

### Plans for next versions
- Use Kotlin coroutines
- Use Retrofit (optional)
- Android push notifications
- Build chain
- Maven and jCentral artifact