package com.github.quorum.component.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class APIRequest {

    private String user;

    private List<String> privateFor;
}
