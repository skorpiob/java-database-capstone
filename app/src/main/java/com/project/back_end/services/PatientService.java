package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final TokenService tokenService;

    public PatientService(PatientRepository patientRepository,
                          AppointmentRepository appointmentRepository,
                          TokenService tokenService) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.tokenService = tokenService;
    }

    @Transactional
    public int createPatient(Patient patient) {
        try {
            patientRepository.save(patient);
            return 1;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> getPatientAppointment(Long id, String token) {
        try {

            String email = tokenService.extractIdentifier(token);

            Patient patient = patientRepository.findByEmail(email);
            if (patient == null) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Unauthorized"));
            }

            if (!patient.getId().equals(id)) {
                return ResponseEntity.status(401)
                        .body(Map.of("error", "Unauthorized access"));
            }

            List<Appointment> appointments = appointmentRepository.findByPatientId(id);

            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Internal server error"));
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByCondition(Long patientId, String condition) {
        try {

            int status;

            if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else {
                return ResponseEntity.badRequest().build();
            }

            List<Appointment> appointments =
                    appointmentRepository.findByPatient_IdAndStatusOrderByAppointmentTimeAsc(patientId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctor(Long patientId, String doctorName) {
        try {

            List<Appointment> appointments =
                    appointmentRepository.filterByDoctorNameAndPatientId(doctorName, patientId);

            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> filterByDoctorAndCondition(Long patientId, String doctorName, String condition) {
        try {

            int status;

            if ("future".equalsIgnoreCase(condition)) {
                status = 0;
            } else if ("past".equalsIgnoreCase(condition)) {
                status = 1;
            } else {
                return ResponseEntity.badRequest().build();
            }

            List<Appointment> appointments =
                    appointmentRepository.filterByDoctorNameAndPatientIdAndStatus(doctorName, patientId, status);

            Map<String, Object> response = new HashMap<>();
            response.put("appointments", appointments);

        return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Patient> getPatientDetails(String token) {
        try {

            String email = tokenService.extractIdentifier(token);
            Patient patient = patientRepository.findByEmail(email);

            if (patient == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            return ResponseEntity.ok(patient);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}