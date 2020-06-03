package machinehead.model.yaml

import machinehead.parse.ParseErrors
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should not be null`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

class YAMLSpec : Spek({
    describe("parsing error messages are loaded from yaml file") {

        val errors = From the YAMLFile("payload-errors.yml", ParseErrors::class)

        it("contains all the yaml values in the specified fields") {
            errors.`should not be null`()
            errors.noAlert `should be equal to` "valid notification must include at least the alert dictionary with body set"
            errors.noTopic `should be equal to` "you have to provide the \"apns-topic\" in the headers of the payload. It is usually the bundle id of your App"
            errors.noTokens `should be equal to` "payload must include the device tokens"
            errors.credentialsError `should be equal to` "could not create the ssl factory and trust manager with the provided credentials or credentials manager"
            errors.noCredentialsManager `should be equal to` "no credential manager instantiated"
        }
    }
})
