package com.example.dto;

public class Country {
  private Integer id;
  private String name;

  public Country() {}

  public Country(final Integer id, final String name) {
    this.id = id;
    this.name = name;
  }

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
}
