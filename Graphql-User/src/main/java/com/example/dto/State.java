package com.example.dto;

public class State {
  private Integer id;
  private String name;
  private Country country;

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Country getCountry() {
    return country;
  }

  public void setCountry(Country country) {
    this.country = country;
  }

  @Override
  public String toString() {
    return "State{" +
      "id=" + id +
      ", name='" + name + '\'' +
      ", country=" + country +
      '}';
  }
}
