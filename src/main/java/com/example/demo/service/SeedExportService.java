
package com.example.demo.service;

import com.example.demo.model.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeedExportService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ExamRepository examRepository;
    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final InvigilatorRepository invigilatorRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final InvigilatorAvailabilityRepository invigilatorAvailabilityRepository;
    private final AccommodationRepository accommodationRepository;
    private final RoomEquipmentRepository roomEquipmentRepository;
    private final ExamRequirementRepository examRequirementRepository;
    private final SeedValidationService validationService;

    public SeedExportService(
            StudentRepository studentRepository,
            CourseRepository courseRepository,
            EnrollmentRepository enrollmentRepository,
            ExamRepository examRepository,
            RoomRepository roomRepository,
            TimeSlotRepository timeSlotRepository,
            InvigilatorRepository invigilatorRepository,
            RoomAvailabilityRepository roomAvailabilityRepository,
            InvigilatorAvailabilityRepository invigilatorAvailabilityRepository,
            AccommodationRepository accommodationRepository,
            RoomEquipmentRepository roomEquipmentRepository,
            ExamRequirementRepository examRequirementRepository,
            SeedValidationService validationService) {

        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.examRepository = examRepository;
        this.roomRepository = roomRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.invigilatorRepository = invigilatorRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.invigilatorAvailabilityRepository = invigilatorAvailabilityRepository;
        this.accommodationRepository = accommodationRepository;
        this.roomEquipmentRepository = roomEquipmentRepository;
        this.examRequirementRepository = examRequirementRepository;
        this.validationService = validationService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> exportCsv() {
        Map<String, Object> validation = validationService.validate();

        if (!Boolean.TRUE.equals(validation.get("valid"))) {
            throw new IllegalStateException(
                    "Export blocked: dataset validation failed. Validate the dataset first.");
        }

        Path outputDirectory = Path.of("exports", "examflow")
                .toAbsolutePath()
                .normalize();

        try {
            Files.createDirectories(outputDirectory);

            Map<String, Integer> exportedRows = new LinkedHashMap<>();

            exportedRows.put("students.csv", writeCsv(
                    outputDirectory.resolve("students.csv"),
                    List.of("id", "name", "active"),
                    studentRepository.findAll().stream()
                            .map(s -> row(s.getId(), s.getName(), s.isActive()))
                            .toList()));

            exportedRows.put("courses.csv", writeCsv(
                    outputDirectory.resolve("courses.csv"),
                    List.of("id", "name", "active"),
                    courseRepository.findAll().stream()
                            .map(c -> row(c.getId(), c.getName(), c.isActive()))
                            .toList()));

            exportedRows.put("enrollments.csv", writeCsv(
                    outputDirectory.resolve("enrollments.csv"),
                    List.of("id", "student_id", "course_id", "enrolled_on", "status"),
                    enrollmentRepository.findAll().stream()
                            .map(e -> row(
                                    e.getId(),
                                    e.getStudent() == null ? null : e.getStudent().getId(),
                                    e.getCourse() == null ? null : e.getCourse().getId(),
                                    e.getEnrolledOn(),
                                    e.getStatus()))
                            .toList()));

            exportedRows.put("exams.csv", writeCsv(
                    outputDirectory.resolve("exams.csv"),
                    List.of("id", "course_id", "duration_minutes",
                            "required_capacity", "required_invigilators", "status"),
                    examRepository.findAll().stream()
                            .map(e -> row(
                                    e.getId(),
                                    e.getCourse() == null ? null : e.getCourse().getId(),
                                    e.getDurationMinutes(),
                                    e.getRequiredCapacity(),
                                    e.getRequiredInvigilators(),
                                    e.getStatus()))
                            .toList()));

            exportedRows.put("rooms.csv", writeCsv(
                    outputDirectory.resolve("rooms.csv"),
                    List.of("id", "name", "capacity", "active"),
                    roomRepository.findAll().stream()
                            .map(r -> row(r.getId(), r.getName(),
                                    r.getCapacity(), r.isActive()))
                            .toList()));

            exportedRows.put("time_slots.csv", writeCsv(
                    outputDirectory.resolve("time_slots.csv"),
                    List.of("id", "start_time", "end_time", "active"),
                    timeSlotRepository.findAll().stream()
                            .map(t -> row(t.getId(), t.getStartTime(),
                                    t.getEndTime(), t.isActive()))
                            .toList()));

            exportedRows.put("invigilators.csv", writeCsv(
                    outputDirectory.resolve("invigilators.csv"),
                    List.of("id", "name", "active"),
                    invigilatorRepository.findAll().stream()
                            .map(i -> row(i.getId(), i.getName(), i.isActive()))
                            .toList()));

            exportedRows.put("room_availability.csv", writeCsv(
                    outputDirectory.resolve("room_availability.csv"),
                    List.of("id", "room_id", "time_slot_id", "available"),
                    roomAvailabilityRepository.findAll().stream()
                            .map(a -> row(
                                    a.getId(),
                                    a.getRoom() == null ? null : a.getRoom().getId(),
                                    a.getTimeSlot() == null ? null : a.getTimeSlot().getId(),
                                    a.isAvailable()))
                            .toList()));

            exportedRows.put("invigilator_availability.csv", writeCsv(
                    outputDirectory.resolve("invigilator_availability.csv"),
                    List.of("id", "invigilator_id", "time_slot_id", "available"),
                    invigilatorAvailabilityRepository.findAll().stream()
                            .map(a -> row(
                                    a.getId(),
                                    a.getInvigilator() == null
                                            ? null : a.getInvigilator().getId(),
                                    a.getTimeSlot() == null
                                            ? null : a.getTimeSlot().getId(),
                                    a.isAvailable()))
                            .toList()));

            exportedRows.put("accommodations.csv", writeCsv(
                    outputDirectory.resolve("accommodations.csv"),
                    List.of("id", "student_id", "type",
                            "extra_time_minutes", "details", "active"),
                    accommodationRepository.findAll().stream()
                            .map(a -> row(
                                    a.getId(),
                                    a.getStudent() == null ? null : a.getStudent().getId(),
                                    a.getType(),
                                    a.getExtraTimeMinutes(),
                                    a.getDetails(),
                                    a.isActive()))
                            .toList()));

            exportedRows.put("room_equipment.csv", writeCsv(
                    outputDirectory.resolve("room_equipment.csv"),
                    List.of("id", "room_id", "equipment_type", "quantity"),
                    roomEquipmentRepository.findAll().stream()
                            .map(e -> row(
                                    e.getId(),
                                    e.getRoom() == null ? null : e.getRoom().getId(),
                                    e.getEquipmentType(),
                                    e.getQuantity()))
                            .toList()));

            exportedRows.put("exam_requirements.csv", writeCsv(
                    outputDirectory.resolve("exam_requirements.csv"),
                    List.of("id", "exam_id", "requirement_type",
                            "requirement_value", "description"),
                    examRequirementRepository.findAll().stream()
                            .map(r -> row(
                                    r.getId(),
                                    r.getExam() == null ? null : r.getExam().getId(),
                                    r.getRequirementType(),
                                    r.getRequirementValue(),
                                    r.getDescription()))
                            .toList()));

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("format", "CSV");
            response.put("directory", outputDirectory.toString());
            response.put("filesExported", exportedRows.size());
            response.put("rowsPerFile", exportedRows);

            return response;

        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Could not write CSV exports: " + ex.getMessage(), ex);
        }
    }

    private int writeCsv(
            Path file,
            List<String> headers,
            List<List<String>> rows) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(
                file, StandardCharsets.UTF_8)) {

            writer.write(headers.stream()
                    .map(this::escapeCsv)
                    .reduce((a, b) -> a + "," + b)
                    .orElse(""));
            writer.newLine();

            for (List<String> values : rows) {
                writer.write(values.stream()
                        .map(this::escapeCsv)
                        .reduce((a, b) -> a + "," + b)
                        .orElse(""));
                writer.newLine();
            }
        }

        return rows.size();
    }

    private List<String> row(Object... values) {
        List<String> result = new ArrayList<>();
        for (Object value : values) {
            result.add(value == null ? "" : value.toString());
        }
        return result;
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"")
                || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }
}
