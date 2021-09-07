package com.tenniscourts.reservations;

import com.tenniscourts.schedules.ScheduleDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Data
public class ReservationDTO {

    private Long id;

    private ScheduleDTO schedule;

    private String reservationStatus;

    private ReservationDTO previousReservation;

    private BigDecimal refundValue;

    private BigDecimal value;

    @ApiModelProperty(required = true)
    @NotNull
    private Long scheduledId;

    @ApiModelProperty(required = true)
    @NotNull
    private Long guestId;
}
