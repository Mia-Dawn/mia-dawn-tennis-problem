package com.tenniscourts.reservations;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class CreateReservationRequestDTO {

    @ApiModelProperty(required = true)
    @NotNull
    private Long guestId;

    @ApiModelProperty(required = true)
    @NotNull
    private Long scheduleId;

}
