import React from "react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { api } from "./api.js";

const initialStats = {
  students: 0, courses: 0, enrollments: 0, exams: 0, rooms: 0,
  timeSlots: 0, invigilators: 0, assignments: 0, assigned: 0, unassigned: 0
};

function Stat({ label, value, note }) {
  return <article className="stat"><span>{label}</span><strong>{Number(value || 0).toLocaleString()}</strong>{note && <small>{note}</small>}</article>;
}

function dateTime(value) {
  if (!value) return "—";
  return value.replace("T", " ");
}

export default function App() {
  const [stats, setStats] = useState(initialStats);
  const [schedule, setSchedule] = useState([]);
  const [validation, setValidation] = useState(null);
  const [profile, setProfile] = useState("small");
  const [busy, setBusy] = useState("");
  const [notice, setNotice] = useState({ type: "", text: "" });
  const [filter, setFilter] = useState("ALL");

  const refresh = useCallback(async () => {
    try {
      const [summary, rows] = await Promise.all([api.dashboard(), api.schedule()]);
      setStats(summary);
      setSchedule(Array.isArray(rows) ? rows : []);
    } catch (error) {
      setNotice({ type: "error", text: `${error.message}. Check that the Spring Boot backend is running on port 8080.` });
    }
  }, []);

  useEffect(() => { refresh(); }, [refresh]);

  async function runAction(label, task) {
    setBusy(label);
    setNotice({ type: "", text: "" });
    try {
      const result = await task();
      if (label === "validate") {
        setValidation(result);
        setNotice({
          type: result.valid ? "success" : "error",
          text: result.valid
            ? `Dataset passed validation. ${result.errorCount || 0} errors found.`
            : `Dataset validation found ${result.errorCount || result.errors?.length || 0} error(s). Review the report below.`
        });
      } else if (label === "seed") {
        setValidation(null);
        setNotice({ type: "success", text: `Generated the ${profile} dataset with seed ${result.seed}. Now validate it before scheduling.` });
      } else if (label === "schedule") {
        setNotice({ type: "success", text: `Scheduling completed. ${result.filter(x => x.status === "ASSIGNED").length} assigned; ${result.filter(x => x.status === "UNASSIGNED").length} unassigned.` });
      } else {
        setNotice({ type: "success", text: "Action completed successfully." });
      }
      await refresh();
    } catch (error) {
      setNotice({ type: "error", text: error.message });
    } finally {
      setBusy("");
    }
  }

  const filtered = useMemo(() => schedule.filter(row =>
    filter === "ALL" || row.status === filter
  ), [schedule, filter]);

  function exportInputs() {
    return runAction("export", async () => {
      const result = await api.exportInputs();
      return result;
    });
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><div className="brand-mark">E</div><div><b>ExamFlow</b><small>Scheduling console</small></div></div>
        <div className="nav-label">WORKSPACE</div>
        <a className="nav-item active" href="#overview">▦ <span>Overview</span></a>
        <a className="nav-item" href="#schedule">◷ <span>Schedule results</span></a>
        <a className="nav-item" href="#validation">✓ <span>Validation report</span></a>
        <div className="sidebar-bottom"><div className="status-dot"></div><span>API workspace</span><small>v1.0</small></div>
      </aside>

      <main className="main">
        <header className="topbar">
          <div><p className="eyebrow">EXAM OPERATIONS / DASHBOARD</p><h1>Scheduling overview</h1><p className="subtitle">Generate, validate, and review a conflict-aware exam schedule.</p></div>
          <button className="button secondary" onClick={() => runAction("refresh", refresh)} disabled={!!busy}>↻ Refresh data</button>
        </header>

        {notice.text && <div className={`notice ${notice.type}`} role="status"><span>{notice.type === "error" ? "!" : "✓"}</span><div>{notice.text}</div><button aria-label="Dismiss notification" onClick={() => setNotice({type:"",text:""})}>×</button></div>}

        <section id="overview" className="stats-grid">
          <Stat label="Students" value={stats.students} note="Registered records" />
          <Stat label="Exams" value={stats.exams} note="Exam sessions to place" />
          <Stat label="Rooms" value={stats.rooms} note="Available room records" />
          <Stat label="Invigilators" value={stats.invigilators} note="Staff records" />
        </section>

        <section className="work-grid">
          <article className="panel workflow">
            <div className="panel-heading"><div><p className="eyebrow">WORKFLOW</p><h2>Run scheduling pipeline</h2></div><span className="step-count">4 steps</span></div>
            <div className="pipeline">
              <div className="pipeline-step"><div className="step-icon">01</div><div className="step-copy"><b>Prepare dataset</b><p>Generate deterministic sample data.</p></div><div className="step-action"><select value={profile} onChange={e => setProfile(e.target.value)} disabled={!!busy}><option value="small">Small · demo</option><option value="full">Full · 5,000 students</option></select><button className="button secondary" disabled={!!busy} onClick={() => runAction("seed", () => api.seed(profile))}>{busy === "seed" ? "Generating…" : "Generate data"}</button></div></div>
              <div className="pipeline-step"><div className="step-icon">02</div><div className="step-copy"><b>Validate constraints</b><p>Check required records, references, and structural consistency.</p></div><button className="button secondary" disabled={!!busy} onClick={() => runAction("validate", api.validate)}>{busy === "validate" ? "Validating…" : "Validate data"}</button></div>
              <div className="pipeline-step"><div className="step-icon">03</div><div className="step-copy"><b>Generate schedule</b><p>Assign exams to feasible slots, rooms, and invigilators.</p></div><button className="button primary" disabled={!!busy || stats.exams === 0 || stats.assignments > 0 || validation?.valid === false} onClick={() => runAction("schedule", api.generateSchedule)}>{busy === "schedule" ? "Scheduling…" : "Generate schedule"}</button></div>
              <div className="pipeline-step"><div className="step-icon">04</div><div className="step-copy"><b>Export results</b><p>Download schedule assignments as CSV.</p></div><button className="button secondary" disabled={!!busy || stats.assignments === 0} onClick={() => runAction("export-schedule", api.downloadSchedule)}>{busy === "export-schedule" ? "Exporting…" : "Export schedule"}</button></div>
            </div>
            <p className="hint"><b>Important:</b> The current backend intentionally prevents generating a second schedule over existing assignments. Restart the backend to reset the in-memory H2 database.</p>
          </article>

          <article className="panel">
            <div className="panel-heading"><div><p className="eyebrow">CURRENT DATASET</p><h2>Dataset health</h2></div><span className={`pill ${validation?.valid ? "good" : "neutral"}`}>{validation ? (validation.valid ? "Validated" : "Needs review") : "Not checked"}</span></div>
            <div className="health-list">
              <div><span>Courses</span><b>{stats.courses.toLocaleString()}</b></div>
              <div><span>Enrollments</span><b>{stats.enrollments.toLocaleString()}</b></div>
              <div><span>Time slots</span><b>{stats.timeSlots.toLocaleString()}</b></div>
              <div><span>Assignments created</span><b>{stats.assignments.toLocaleString()}</b></div>
            </div>
            <div className="result-summary"><div><span className="legend assigned"></span><span>Assigned</span><b>{stats.assigned}</b></div><div><span className="legend unassigned"></span><span>Unassigned</span><b>{stats.unassigned}</b></div></div>
            <div className="progress-track"><div style={{width: `${stats.assignments ? (stats.assigned / stats.assignments) * 100 : 0}%`}} /></div>
            <small className="muted">Assignment rate {stats.assignments ? Math.round(stats.assigned / stats.assignments * 100) : 0}%</small>
          </article>
        </section>

        {validation && <section id="validation" className="panel validation-panel">
          <div className="panel-heading"><div><p className="eyebrow">DATA QUALITY</p><h2>Validation report</h2></div><span className={`pill ${validation.valid ? "good" : "bad"}`}>{validation.errorCount || 0} errors</span></div>
          <div className="validation-counts">{Object.entries(validation.counts || {}).map(([key,value]) => <div key={key}><span>{key.replace(/([A-Z])/g," $1")}</span><b>{Number(value).toLocaleString()}</b></div>)}</div>
          {validation.errors?.length > 0 && <div className="message-list"><b>Errors</b>{validation.errors.map((e,i)=><p key={i}>• {e}</p>)}</div>}
          {validation.warnings?.length > 0 && <div className="message-list"><b>Warnings</b>{validation.warnings.map((e,i)=><p key={i}>• {e}</p>)}</div>}
          {validation.valid && <p className="success-line">No structural validation errors were found.</p>}
        </section>}

        <section id="schedule" className="panel schedule-panel">
          <div className="panel-heading schedule-heading"><div><p className="eyebrow">RESULTS</p><h2>Generated schedule</h2><p className="subtitle">Inspect assignment status and reason codes for exams that could not be placed.</p></div><select value={filter} onChange={e=>setFilter(e.target.value)}><option value="ALL">All statuses</option><option value="ASSIGNED">Assigned</option><option value="UNASSIGNED">Unassigned</option></select></div>
          <div className="table-wrap"><table><thead><tr><th>Exam</th><th>Course</th><th>Room</th><th>Time slot</th><th>Invigilators</th><th>Status</th><th>Reason</th></tr></thead>
            <tbody>{filtered.slice(0,250).map(row => <tr key={row.id}><td><b>{row.exam?.id || row.examId || "—"}</b><small>{row.id}</small></td><td>{row.exam?.course?.id || "—"}</td><td>{row.room?.name || row.room?.id || "—"}</td><td>{row.timeSlot ? <><b>{row.timeSlot.id}</b><small>{dateTime(row.timeSlot.startTime)}</small></> : "—"}</td><td>{row.invigilators?.map(i=>i.name || i.id).join(", ") || "—"}</td><td><span className={`pill ${row.status === "ASSIGNED" ? "good" : "bad"}`}>{row.status}</span></td><td className="reason">{row.reasonCode || "—"}</td></tr>)}
              {filtered.length === 0 && <tr><td colSpan="7" className="empty">No schedule rows yet. Generate data, validate it, then run the scheduler.</td></tr>}
            </tbody></table></div>
          {filtered.length > 250 && <p className="muted table-foot">Showing first 250 of {filtered.length.toLocaleString()} rows. Export the CSV for the full result set.</p>}
        </section>
        <footer>ExamFlow · Deterministic scheduling workspace <span>Built with Spring Boot + React</span></footer>
      </main>
    </div>
  );
}
