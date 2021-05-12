package com.mikkaeru.pix.model

import com.mikkaeru.pix.shared.exception.InvalidArgumentException
import java.util.regex.Pattern

enum class KeyType {

    CPF {
        override fun validate(key: String?) {
            if (key.isNullOrBlank()) {
                throw InvalidArgumentException("A chave deve ser informada")
            }

            if (!key.matches("^[0-9]{11}$".toRegex())) {
                throw InvalidArgumentException("CPF invalido")
            }
        }
    },

    EMAIL {
        override fun validate(key: String?) {
            if (key.isNullOrBlank()) {
                throw InvalidArgumentException("A chave deve ser informada")
            }

            val isValid = Pattern.compile(
                "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                        + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                        + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                        + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                        + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
            ).matcher(key).matches()

            if (!isValid) {
                throw InvalidArgumentException("Email invalido")
            }
        }
    },

    PHONE {
        override fun validate(key: String?) {
            if (key.isNullOrBlank()) {
                throw InvalidArgumentException("A chave deve ser informada")
            }

            if (!key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())) {
                throw InvalidArgumentException("Numero de telefone invalido!")
            }
        }
    },

    RANDOM_KEY {
        override fun validate(key: String?) {
            if (!key.isNullOrBlank()) {
                throw InvalidArgumentException("Não é necessário informar a chave para geração randômica")
            }
        }

    };

    abstract fun validate(key: String?)
}