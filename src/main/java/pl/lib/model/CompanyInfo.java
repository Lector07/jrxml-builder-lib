package pl.lib.model;

import java.util.Objects;

/**
 * Represents company information used in reports.
 *
 * <p>This class stores all company data needed to generate
 * report headers, including contact, address and identification data.</p>
 *
 * <p>Uses Builder pattern for convenient creation of instances with optional fields.</p>
 *
 * <h3>Usage example:</h3>
 * <pre>{@code
 * CompanyInfo company = CompanyInfo.builder("ACME Corp")
 *     .address("123 Main Street")
 *     .location("00-001", "Warsaw")
 *     .contact("555-123-456", "contact@acme.com")
 *     .taxId("1234567890")
 *     .website("www.acme.com")
 *     .logo("/path/to/logo.png")
 *     .build();
 * }</pre>
 *
 * @author SOFTRES
 * @since 1.0
 */
public final class CompanyInfo {
    private String name;
    private String address;
    private  String postalCode;
    private  String city;
    private  String taxId;
    private  String phone;
    private String email;
    private String website;
    private String logoPath;

    /**
     * Default constructor for JSON deserialization.
     */
    public CompanyInfo() {
    }

    /**
     * Private constructor used by Builder.
     *
     * @param builder builder instance with set values
     */
    private CompanyInfo(Builder builder) {
        this.name = builder.name;
        this.address = builder.address;
        this.postalCode = builder.postalCode;
        this.city = builder.city;
        this.taxId = builder.taxId;
        this.phone = builder.phone;
        this.email = builder.email;
        this.website = builder.website;
        this.logoPath = builder.logoPath;
    }

    /**
     * Returns the company name.
     *
     * @return company name
     */
    public String getName() { return name; }

    /**
     * Returns the company address.
     *
     * @return company address
     */
    public String getAddress() { return address; }

    /**
     * Returns the company postal code.
     *
     * @return postal code
     */
    public String getPostalCode() { return postalCode; }

    /**
     * Returns the company city.
     *
     * @return city
     */
    public String getCity() { return city; }

    /**
     * Returns the tax identification number.
     *
     * @return tax ID number
     */
    public String getTaxId() { return taxId; }

    /**
     * Returns the company phone number.
     *
     * @return phone number
     */
    public String getPhone() { return phone; }

    /**
     * Returns the company email address.
     *
     * @return email address
     */
    public String getEmail() { return email; }

    /**
     * Returns the company website address.
     *
     * @return website URL
     */
    public String getWebsite() { return website; }

    /**
     * Returns the path to company logo file.
     *
     * @return logo file path
     */
    public String getLogoPath() { return logoPath; }

    /**
     * Creates a new Builder for building CompanyInfo instance.
     *
     * @param name company name (required)
     * @return new Builder
     * @throws NullPointerException if name is null
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }

    /**
     * Builder for CompanyInfo class implementing Builder pattern.
     *
     * <p>Enables step-by-step building of CompanyInfo object with optional fields.</p>
     */
    public static class Builder {
        private final String name;
        private String address;
        private String postalCode;
        private String city;
        private String taxId;
        private String phone;
        private String email;
        private String website;
        private String logoPath;

        /**
         * Builder constructor with required company name.
         *
         * @param name company name (cannot be null)
         * @throws NullPointerException if name is null
         */
        public Builder(String name) {
            this.name = Objects.requireNonNull(name, "Company name cannot be null");
        }

        /**
         * Sets the company address.
         *
         * @param address company address
         * @return this Builder (for method chaining)
         */
        public Builder address(String address) {
            this.address = address;
            return this;
        }

        /**
         * Sets the company postal code and city.
         *
         * @param postalCode postal code
         * @param city city
         * @return this Builder (for method chaining)
         */
        public Builder location(String postalCode, String city) {
            this.postalCode = postalCode;
            this.city = city;
            return this;
        }

        /**
         * Sets the tax identification number.
         *
         * @param taxId tax ID number
         * @return this Builder (for method chaining)
         */
        public Builder taxId(String taxId) {
            this.taxId = taxId;
            return this;
        }

        /**
         * Sets the company contact information.
         *
         * @param phone phone number
         * @param email email address
         * @return this Builder (for method chaining)
         */
        public Builder contact(String phone, String email) {
            this.phone = phone;
            this.email = email;
            return this;
        }

        /**
         * Sets the company website address.
         *
         * @param website website URL
         * @return this Builder (for method chaining)
         */
        public Builder website(String website) {
            this.website = website;
            return this;
        }

        /**
         * Sets the path to company logo file.
         *
         * @param logoPath logo file path
         * @return this Builder (for method chaining)
         */
        public Builder logo(String logoPath) {
            this.logoPath = logoPath;
            return this;
        }

        /**
         * Builds the final CompanyInfo instance.
         *
         * @return new CompanyInfo instance with set values
         */
        public CompanyInfo build() {
            return new CompanyInfo(this);
        }
    }
}