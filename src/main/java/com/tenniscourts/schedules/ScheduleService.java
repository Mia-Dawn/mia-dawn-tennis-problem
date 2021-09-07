package com.tenniscourts.schedules;

import com.tenniscourts.exceptions.EntityNotFoundException;
import com.tenniscourts.tenniscourts.TennisCourt;
import com.tenniscourts.tenniscourts.TennisCourtRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TennisCourtRepository tennisCourtRepository;

    private final ScheduleMapper scheduleMapper;

    public ScheduleDTO addSchedule(Long tennisCourtId, CreateScheduleRequestDTO createScheduleRequestDTO) {
        LocalDateTime endDateTime = createScheduleRequestDTO.getStartDateTime().plusHours(1);

        validateScheduleOverlap(tennisCourtId, createScheduleRequestDTO.getStartDateTime(), endDateTime);

        /* Is this better off using TennisCourtService? We'd have to make the service return a non-DTO,
           include a mapper, or rework the Schedule object to only use ids...
         */
        TennisCourt tennisCourt = tennisCourtRepository.findById(tennisCourtId).orElseThrow(() -> {
            throw new EntityNotFoundException("Tennis court not found.");
        });

        Schedule schedule = Schedule.builder()
                .tennisCourt(tennisCourt)
                .startDateTime(createScheduleRequestDTO.getStartDateTime())
                .endDateTime(createScheduleRequestDTO.getStartDateTime().plusHours(1))
                .build();

        return scheduleMapper.map(scheduleRepository.saveAndFlush(schedule));
    }

    private void validateScheduleOverlap(Long tennisCourtId, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if(startDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Schedules must be made in the future.");
        }

        List<ScheduleDTO> scheduleDTOS = findSchedulesByTennisCourtId(tennisCourtId);
        if(scheduleDTOS.stream().anyMatch(scheduleDTO ->
                scheduleDTO.getStartDateTime().isBefore(endDateTime)
                        && scheduleDTO.getEndDateTime().isAfter(startDateTime))) {
            throw new IllegalArgumentException("Schedule overlaps an existing schedule; please schedule " +
                    "at a different time or on a different court.");
        }
    }

    public List<ScheduleDTO> findSchedulesByDates(LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleMapper.map(
                scheduleRepository.findByStartDateTimeGreaterThanEqualAndEndDateTimeLessThanEqual(startDate, endDate));
    }

    public ScheduleDTO findSchedule(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).map(scheduleMapper::map).orElseThrow(() -> {
            throw new EntityNotFoundException("Schedule not found.");
        });
    }

    public List<ScheduleDTO> findSchedulesByTennisCourtId(Long tennisCourtId) {
        return scheduleMapper.map(scheduleRepository.findByTennisCourt_IdOrderByStartDateTime(tennisCourtId));
    }
}
