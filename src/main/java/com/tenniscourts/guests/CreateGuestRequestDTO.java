package com.tenniscourts.guests;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class CreateGuestRequestDTO {

    @ApiModelProperty(required = true)
    @NotEmpty
    private String name;
}
