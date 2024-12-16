package ru.webdevpet.server.dto;

public class UnicastMessage implements Message{
    public int idFrom;
    public String nameFrom;
    public int idTo;
    public String nameTo;
    public String message;

    public UnicastMessage(){

    }
}
