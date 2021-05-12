package com.mikkaeru.pix.model

import io.grpc.Status
import java.util.*
import java.util.regex.Pattern

enum class KeyType {

    CPF {
        override fun validate(key: String?): Optional<RuntimeException> {
            if (key!!.matches("^[0-9]{11}$".toRegex())) {
                return Optional.empty()
            }

            return Optional.of(Status.INVALID_ARGUMENT
                .withDescription("CPF invalido!")
                .asRuntimeException())
        }

    },

    EMAIL {
        override fun validate(key: String?): Optional<RuntimeException> {
            val isValid = Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
            ).matcher(key!!).matches()

            if (isValid) {
                return Optional.empty()
            }

            return Optional.of(Status.INVALID_ARGUMENT
                .withDescription("Email invalido!")
                .asRuntimeException())
        }

    },

    PHONE {
        override fun validate(key: String?): Optional<RuntimeException> {
            if (key!!.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                return Optional.empty()
            }

            return Optional.of(Status.INVALID_ARGUMENT
                .withDescription("Numero de telefone invalido!")
                .asRuntimeException())
        }

    },

    RANDOM_KEY {
        override fun validate(key: String?): Optional<RuntimeException> {
            if (key.isNullOrBlank()) {
                return Optional.empty()
            }

            return Optional.of(Status.INVALID_ARGUMENT
                .withDescription("Não é necessário informar a chave para geração randômica")
                .asRuntimeException())
        }

    };

    abstract fun validate(key: String?): Optional<RuntimeException>
}