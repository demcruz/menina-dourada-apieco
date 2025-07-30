package br.com.ecommerce.meninadourada.model;

import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Objects;

public class ShippingAddress {

    @Field("zipCode")
    private String zipCode;
    @Field("streetName")
    private String streetName;
    @Field("streetNumber")
    private String streetNumber;
    @Field("complement")
    private String complement; // Ex: apto, bloco
    @Field("neighborhood")
    private String neighborhood; // Bairro
    @Field("cityName")
    private String cityName;
    @Field("stateName")
    private String stateName;
    @Field("countryName")
    private String countryName;

    // Default constructor
    public ShippingAddress() {
    }

    // Constructor with all arguments
    public ShippingAddress(String zipCode, String streetName, String streetNumber, String complement, String neighborhood, String cityName, String stateName, String countryName) {
        this.zipCode = zipCode;
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.complement = complement;
        this.neighborhood = neighborhood;
        this.cityName = cityName;
        this.stateName = stateName;
        this.countryName = countryName;
    }

    // Getters and Setters
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getStreetName() { return streetName; }
    public void setStreetName(String streetName) { this.streetName = streetName; }
    public String getStreetNumber() { return streetNumber; }
    public void setStreetNumber(String streetNumber) { this.streetNumber = streetNumber; }
    public String getComplement() { return complement; }
    public void setComplement(String complement) { this.complement = complement; }
    public String getNeighborhood() { return neighborhood; }
    public void setNeighborhood(String neighborhood) { this.neighborhood = neighborhood; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getStateName() { return stateName; }
    public void setStateName(String stateName) { this.stateName = stateName; }
    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    @Override
    public String toString() {
        return "ShippingAddress{" +
                "zipCode='" + zipCode + '\'' +
                ", streetName='" + streetName + '\'' +
                ", streetNumber='" + streetNumber + '\'' +
                ", complement='" + complement + '\'' +
                ", neighborhood='" + neighborhood + '\'' +
                ", cityName='" + cityName + '\'' +
                ", stateName='" + stateName + '\'' +
                ", countryName='" + countryName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShippingAddress that = (ShippingAddress) o;
        return Objects.equals(zipCode, that.zipCode) && Objects.equals(streetName, that.streetName) && Objects.equals(streetNumber, that.streetNumber) && Objects.equals(complement, that.complement) && Objects.equals(neighborhood, that.neighborhood) && Objects.equals(cityName, that.cityName) && Objects.equals(stateName, that.stateName) && Objects.equals(countryName, that.countryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(zipCode, streetName, streetNumber, complement, neighborhood, cityName, stateName, countryName);
    }
}
