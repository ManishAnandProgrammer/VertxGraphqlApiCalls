package com.example.dto;

public class State {
  private Integer id;
  private String name;

  public State(final Integer id, final String name) {
    this.id = id;
    this.name = name;
  }

  public Integer getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}
