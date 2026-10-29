package com.example.demo.controller;

import com.example.demo.model.ScheduleAssignment;
import com.example.demo.repository.ScheduleAssignmentRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/schedules")
public class ScheduleExportController {
    private final ScheduleAssignmentRepository assignments;

    public ScheduleExportController(ScheduleAssignmentRepository assignments) {
        this.assignments = assignments;
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> exportCsv() {
        StringBuilder csv = new StringBuilder("assignment_id,exam_id,course_id,status,room_id,slot_id,start_time,end_time,invigilators,reason_code\n");
        List<ScheduleAssignment> rows = assignments.findAll();
        rows.sort((a, b) -> {
            String x = a.getTimeSlot() == null ? "" : a.getTimeSlot().getStartTime().toString();
            String y = b.getTimeSlot() == null ? "" : b.getTimeSlot().getStartTime().toString();
            int compare = x.compareTo(y);
            return compare != 0 ? compare : a.getExam().getId().compareTo(b.getExam().getId());
        });
        for (ScheduleAssignment a : rows) {
            csv.append(cell(a.getId())).append(',')
                .append(cell(a.getExam() == null ? "" : a.getExam().getId())).append(',')
                .append(cell(a.getExam() == null || a.getExam().getCourse() == null ? "" : a.getExam().getCourse().getId())).append(',')
                .append(cell(a.getStatus() == null ? "" : a.getStatus().name())).append(',')
                .append(cell(a.getRoom() == null ? "" : a.getRoom().getId())).append(',')
                .append(cell(a.getTimeSlot() == null ? "" : a.getTimeSlot().getId())).append(',')
                .append(cell(a.getTimeSlot() == null ? "" : a.getTimeSlot().getStartTime().toString())).append(',')
                .append(cell(a.getTimeSlot() == null ? "" : a.getTimeSlot().getEndTime().toString())).append(',')
                .append(cell(a.getInvigilators().stream().map(i -> i.getId()).sorted().reduce((x,y) -> x + ";" + y).orElse(""))).append(',')
                .append(cell(a.getReasonCode())).append("\n");
        }
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=examflow-schedule.csv")
            .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
            .body(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private String cell(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
