package com.tenniscourts.schedules;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tenniscourts.tenniscourts.TennisCourtDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScheduleDTO {

    private Long id;

    private TennisCourtDTO tennisCourt;

    @ApiModelProperty(required = true)
    @NotNull
    private Long tennisCourtId;

    @ApiModelProperty(required = true, example = "2021-09-06T13:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm")
    @NotNull
    private LocalDateTime startDateTime;

    @ApiModelProperty(required = true, example = "2021-09-06T13:00")
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endDateTime;

}
