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
            "apns-topic" to "ch.sbb.ios.pushnext"
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
            "apns-topic" to "ch.sbb.ios.pushnext"
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

