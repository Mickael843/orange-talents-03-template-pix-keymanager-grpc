package com.mikkaeru.pix.model

import com.mikkaeru.pix.shared.exception.InvalidArgumentException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.StringContains.containsStringIgnoringCase
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

internal class KeyTypeTest {

    @Nested
    inner class RANDOM {

        @Test
        fun `deve dar uma exception se a chave nao for vazia ou nula`() {
            val exception = assertThrows<InvalidArgumentException> {
                KeyType.RANDOM.validate("Não sou nulo")
            }

            with(exception) {
                assertNotNull(this)
                assertThat(exception.message, containsStringIgnoringCase("Não é necessário informar a chave para geração randômica"))
            }
        }

        @Test
        fun `nao deve dar uma exception se a chave for vazia ou nula`() {
            assertDoesNotThrow {
                KeyType.RANDOM.validate("")
            }
        }
    }

    @Nested
    inner class PHONE {

        @Test
        fun `deve dar uma exception se a chave for nula ou vazia`() {
            val exception = assertThrows<InvalidArgumentException> {
                KeyType.PHONE.validate("")
            }

            with(exception) {
                assertNotNull(this)
                assertThat(exception.message, containsStringIgnoringCase("A chave deve ser informada"))
            }
        }

        @Test
        fun `deve dar uma exception se o telefone for invalido`() {
            val exception = assertThrows<InvalidArgumentException> {
                KeyType.PHONE.validate("123321")
            }

            with(exception) {
                assertNotNull(this)
                assertThat(exception.message, containsStringIgnoringCase("Numero de telefone invalido!"))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve dar uma exception se a chave for nula ou vazia`() {
            val exception = assertThrows<InvalidArgumentException> {
                KeyType.EMAIL.validate("")
            }

            with(exception) {
                assertNotNull(this)
                assertThat(exception.message, containsStringIgnoringCase("A chave deve ser informada"))
            }
        }

        @Test
        fun `deve dar uma exception se o email for invalido`() {
            val exception = assertThrows<InvalidArgumentException> {
                KeyType.EMAIL.validate("EMAIL.INVALIDO.COM")
            }

            with(exception) {
                assertNotNull(this)
                assertThat(exception.message, containsStringIgnoringCase("Email invalido"))
            }
        }
    }

    @Nested
    inner class CPF {

        @Test
        fun `deve dar uma exception se a chave for nula ou vazia`() {
            val exception = assertThrows<InvalidArgumentException> {
                KeyType.CPF.validate("")
            }

            with(exception) {
                assertNotNull(this)
                assertThat(exception.message, containsStringIgnoringCase("A chave deve ser informada"))
            }
        }

        @Test
        fun `deve dar uma exception se o email for invalido`() {
            val exception = assertThrows<InvalidArgumentException> {
                KeyType.CPF.validate("1233210")
            }

            with(exception) {
                assertNotNull(this)
                assertThat(exception.message, containsStringIgnoringCase("CPF invalido"))
            }
        }
    }
}