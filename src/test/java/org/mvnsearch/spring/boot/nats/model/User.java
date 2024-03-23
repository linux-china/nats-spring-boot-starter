package org.mvnsearch.spring.boot.nats.model;

public class User {
  private Integer id;
  private String nick;

  public User() {
  }

  public User(Integer id, String nick) {
    this.id = id;
    this.nick = nick;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }
}
