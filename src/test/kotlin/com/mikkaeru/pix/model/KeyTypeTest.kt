package com.mikkaeru.pix.model

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class KeyTypeTest {

    @Nested
    inner class RANDOM {

        @Test
        fun `deve ser true se a chave for vazia ou nula`() {
            val validate = KeyType.RANDOM.validate("")
            assertTrue(validate)
        }

        @Test
        fun `deve ser false se a chave nao for vazia ou nula`() {
            val validate = KeyType.RANDOM.validate("Não sou nulo")
            assertFalse(validate)
        }
    }

    @Nested
    inner class PHONE {

        @Test
        fun `deve retornar true se a chave for valida`() {
            val validate = KeyType.PHONE.validate("+5538999309941")
            assertTrue(validate)
        }

        @Test
        fun `deve retornar false se a chave for nula ou vazia`() {
            val validate = KeyType.PHONE.validate("")
            assertFalse(validate)
        }

        @Test
        fun `deve retornar false se o telefone for invalido`() {
            val validate = KeyType.PHONE.validate("123321")
            assertFalse(validate)
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve retornar true se a chave for valida`() {
            val validate = KeyType.EMAIL.validate("teste@gmail.com")
            assertTrue(validate)
        }

        @Test
        fun `deve retornar false se a chave for nula ou vazia`() {
            val validate = KeyType.EMAIL.validate("")
            assertFalse(validate)
        }

        @Test
        fun `deve retornar false se o telefone for invalido`() {
            val validate = KeyType.EMAIL.validate("teste.gmail.com")
            assertFalse(validate)
        }
    }

    @Nested
    inner class CPF {

        @Test
        fun `deve retornar true se a chave for valida`() {
            val validate = KeyType.CPF.validate("76361450066")
            assertTrue(validate)
        }

        @Test
        fun `deve retornar false se a chave for nula ou vazia`() {
            val validate = KeyType.CPF.validate("")
            assertFalse(validate)
        }

        @Test
        fun `deve retornar false se o telefone for invalido`() {
            val validate = KeyType.CPF.validate("32039284672")
            assertFalse(validate)
        }
    }
}