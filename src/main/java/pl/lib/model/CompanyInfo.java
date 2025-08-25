package pl.lib.model;

public class CompanyInfo {
    private String name;
    private String address;
    private String postalCode;
    private String city;
    private String taxId;
    private String phone;
    private String email;
    private String website;
    private String logoPath;

    public CompanyInfo() {}

    public CompanyInfo(String name) {
        this.name = name;
    }

    public CompanyInfo withName(String name) {
        this.name = name;
        return this;
    }

    public CompanyInfo withAddress(String address) {
        this.address = address;
        return this;
    }

    public CompanyInfo withLocation(String postalCode, String city) {
        this.postalCode = postalCode;
        this.city = city;
        return this;
    }

    public CompanyInfo withTaxId(String taxId) {
        this.taxId = taxId;
        return this;
    }

    public CompanyInfo withContact(String phone, String email) {
        this.phone = phone;
        this.email = email;
        return this;
    }

    public CompanyInfo withWebsite(String website) {
        this.website = website;
        return this;
    }

    public CompanyInfo withLogo(String logoPath) {
        this.logoPath = logoPath;
        return this;
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



}