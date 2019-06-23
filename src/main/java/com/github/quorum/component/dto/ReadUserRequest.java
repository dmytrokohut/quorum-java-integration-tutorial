package com.github.quorum.component.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReadUserRequest {

    private String endpoint;

    private String contractAddress;
}
