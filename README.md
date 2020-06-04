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
} push { resultEither ->
        resultEither
            .fold({ clientError ->
                println(clientError.message)
            }, { platformResponse ->
                println("$platformResponse.status, $platformResponse.message")
            })
    }
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
{ resultEither ->
        resultEither
            .fold({ clientError ->
                println(clientError.message)
            }, { platformResponse ->
                println("$platformResponse.status, $platformResponse.message")
            })
    }
}
```