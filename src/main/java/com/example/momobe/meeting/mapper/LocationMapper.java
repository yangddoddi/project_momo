package com.example.momobe.meeting.mapper;

import com.example.momobe.meeting.domain.Location;
import com.example.momobe.meeting.dto.MeetingRequestDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import static org.mapstruct.ReportingPolicy.IGNORE;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = IGNORE)
public interface LocationMapper {
    @Mapping(target = "address.address1", source = "address1")
    @Mapping(target = "address.address2", source = "address2")
    Location toLocation(MeetingRequestDto.LocationDto locationDto);
}
