package pl.lib.automation.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderFormatterTest {

    @Test
    void shouldFormatSimpleSnakeCaseField() {
        String result = HeaderFormatter.formatHeaderName("user_name");
        assertThat(result).isEqualTo("Name");
    }

    @Test
    void shouldFormatCamelCaseField() {
        String result = HeaderFormatter.formatHeaderName("userName");
        assertThat(result).isEqualTo("User Name");
    }

    @Test
    void shouldFormatFieldWithMultipleUnderscores() {
        String result = HeaderFormatter.formatHeaderName("total_order_amount");
        assertThat(result).isEqualTo("Amount");
    }

    @Test
    void shouldFormatMixedCaseWithUnderscores() {
        String result = HeaderFormatter.formatHeaderName("customer_firstName");
        assertThat(result).isEqualTo("First Name");
    }

    @Test
    void shouldHandleNullInput() {
        String result = HeaderFormatter.formatHeaderName(null);
        assertThat(result).isNull();
    }

    @Test
    void shouldHandleEmptyString() {
        String result = HeaderFormatter.formatHeaderName("");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldFormatSingleWord() {
        String result = HeaderFormatter.formatHeaderName("name");
        assertThat(result).isEqualTo("Name");
    }

    @Test
    void shouldFormatUpperCaseInput() {
        String result = HeaderFormatter.formatHeaderName("USER_NAME");
        assertThat(result).isEqualTo("Name");
    }

    @Test
    void shouldFormatComplexNestedField() {
        String result = HeaderFormatter.formatHeaderName("company_address_postalCode");
        assertThat(result).isEqualTo("Postal Code");
    }

    @Test
    void shouldHandleMultipleSpaces() {
        String result = HeaderFormatter.formatHeaderName("first   name");
        assertThat(result).isEqualTo("First Name");
    }

    @Test
    void shouldFormatFieldWithNumbers() {
        String result = HeaderFormatter.formatHeaderName("address_line1");
        assertThat(result).isEqualTo("Line1");
    }

    @Test
    void shouldFormatAcronyms() {
        String result = HeaderFormatter.formatHeaderName("company_taxID");
        assertThat(result).isEqualTo("Tax Id");
    }

    @Test
    void shouldFormatAllUpperCaseCamelCase() {
        String result = HeaderFormatter.formatHeaderName("HTTPSConnection");
        assertThat(result).isEqualTo("Httpsconnection");
    }

    @Test
    void shouldFormatMixedUnderscoresAndCamelCase() {
        String result = HeaderFormatter.formatHeaderName("order_totalAmount");
        assertThat(result).isEqualTo("Total Amount");
    }

    @Test
    void shouldFormatSingleLetter() {
        String result = HeaderFormatter.formatHeaderName("x");
        assertThat(result).isEqualTo("X");
    }

    @Test
    void shouldFormatNestedFieldWithCamelCase() {
        String result = HeaderFormatter.formatHeaderName("user_profile_contactEmail");
        assertThat(result).isEqualTo("Contact Email");
    }

    @Test
    void shouldHandleTrailingUnderscore() {
        String result = HeaderFormatter.formatHeaderName("field_name_");
        assertThat(result).isEmpty();
    }

    @Test
    void shouldHandleLeadingSpace() {
        String result = HeaderFormatter.formatHeaderName(" firstName");
        assertThat(result).isEqualTo("First Name");
    }

    @Test
    void shouldFormatTypicalJsonFields() {
        assertThat(HeaderFormatter.formatHeaderName("id")).isEqualTo("Id");
        assertThat(HeaderFormatter.formatHeaderName("created_at")).isEqualTo("At");
        assertThat(HeaderFormatter.formatHeaderName("updated_at")).isEqualTo("At");
        assertThat(HeaderFormatter.formatHeaderName("first_name")).isEqualTo("Name");
        assertThat(HeaderFormatter.formatHeaderName("last_name")).isEqualTo("Name");
    }

    @Test
    void shouldFormatPolishFieldNames() {
        assertThat(HeaderFormatter.formatHeaderName("nazwa_firmy")).isEqualTo("Firmy");
        assertThat(HeaderFormatter.formatHeaderName("data_utworzenia")).isEqualTo("Utworzenia");
        assertThat(HeaderFormatter.formatHeaderName("kwota_brutto")).isEqualTo("Brutto");
    }
}

