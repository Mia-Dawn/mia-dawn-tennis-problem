package com.tenniscourts.tenniscourts;

import com.tenniscourts.schedules.ScheduleDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TennisCourtDTO {

    private Long id;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String name;

    private List<ScheduleDTO> tennisCourtSchedules;

}
