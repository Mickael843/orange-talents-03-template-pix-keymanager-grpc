package com.mikkaeru.pix.model

import org.hibernate.validator.internal.constraintvalidators.hv.EmailValidator
import org.hibernate.validator.internal.constraintvalidators.hv.br.CPFValidator

enum class KeyType {

    CPF {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrEmpty()) {
                return false
            }

            if (!key.matches("[0-9]+".toRegex())) {
                return false
            }

            return CPFValidator().run {
                initialize(null)
                isValid(key, null)
            }
        }
    },
    EMAIL {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrEmpty()) {
                return false
            }

            return EmailValidator().run {
                initialize(null)
                isValid(key, null)
            }
        }
    },
    PHONE {
        override fun validate(key: String?): Boolean {
            if (key.isNullOrEmpty()) {
                return false
            }

            return key.matches("^\\+[1-9][0-9]\\d{1,14}\$".toRegex())
        }
    },
    RANDOM {
        override fun validate(key: String?): Boolean {
            return key.isNullOrEmpty()
        }
    };

    abstract fun validate(key: String?): Boolean
}