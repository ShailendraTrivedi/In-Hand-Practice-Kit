package com.codingshuttle.youtube.hospitalManagement.dto.reponse;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    String jwt;
    Long userId;

    @Data
    public static class AppointmentResponseDto {
        private Long id;
        private LocalDateTime appointmentTime;
        private String reason;
        private DoctorResponseDto doctor;
    //    private PatientResponseDto patient;
    }
}
