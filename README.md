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
        custom = hashMapOf(
            "custom-property" to "hello custom",
            "blow-up" to true
        )
        tokens = arrayListOf("asdfsd", "sadfsdf")
    } push {
        println(it)
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
        custom = hashMapOf(
            "custom-property" to "hello custom",
            "blow-up" to true
        )
        tokens = arrayListOf("asdfsd", "sadfsdf")
    } 

PushIt.with(payload)
```