package pl.lib.model;

import java.util.Objects;

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

    public CompanyInfo() {
    }

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

    // Gettery
    public String getName() { return name; }
    public String getAddress() { return address; }
    public String getPostalCode() { return postalCode; }
    public String getCity() { return city; }
    public String getTaxId() { return taxId; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getWebsite() { return website; }
    public String getLogoPath() { return logoPath; }

    public static Builder builder(String name) {
        return new Builder(name);
    }

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

        public Builder(String name) {
            this.name = Objects.requireNonNull(name, "Company name cannot be null");
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder location(String postalCode, String city) {
            this.postalCode = postalCode;
            this.city = city;
            return this;
        }

        public Builder taxId(String taxId) {
            this.taxId = taxId;
            return this;
        }

        public Builder contact(String phone, String email) {
            this.phone = phone;
            this.email = email;
            return this;
        }

        public Builder website(String website) {
            this.website = website;
            return this;
        }

        public Builder logo(String logoPath) {
            this.logoPath = logoPath;
            return this;
        }

        public CompanyInfo build() {
            return new CompanyInfo(this);
        }
    }
}