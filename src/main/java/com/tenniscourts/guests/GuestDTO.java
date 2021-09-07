package com.tenniscourts.guests;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class GuestDTO {

    @ApiModelProperty(required = true)
    @NotNull
    private Long id;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String name;
}
