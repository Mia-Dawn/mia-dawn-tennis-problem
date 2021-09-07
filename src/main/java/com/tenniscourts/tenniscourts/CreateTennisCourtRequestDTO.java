package com.tenniscourts.tenniscourts;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTennisCourtRequestDTO {

    @ApiModelProperty(required = true)
    @NotEmpty
    private String name;
}
