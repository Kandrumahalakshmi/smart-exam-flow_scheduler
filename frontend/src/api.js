const BASE = (import.meta.env.VITE_API_BASE_URL || "http://localhost:8080").replace(/\/$/, "");

async function request(path, options = {}) {
  const response = await fetch(`${BASE}${path}`, {
    ...options,
    headers: { "Content-Type": "application/json", ...(options.headers || {}) }
  });
  const contentType = response.headers.get("content-type") || "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : await response.text();
  if (!response.ok) {
    const message = typeof body === "object"
      ? body.message || body.detail || body.error || JSON.stringify(body)
      : body;
    throw new Error(message || `Request failed (${response.status})`);
  }
  return body;
}

export const api = {
  dashboard: () => request("/api/dashboard"),
  validate: async () => {
    const response = await fetch(`${BASE}/api/admin/seed/validate`);
    const body = await response.json();
    if (!response.ok && response.status !== 422) throw new Error(body.message || JSON.stringify(body));
    return body;
  },
  seed: (profile) => request(`/api/admin/seed?profile=${encodeURIComponent(profile)}`, { method: "POST" }),
  generateSchedule: () => request("/api/schedules/generate", { method: "POST" }),
  schedule: () => request("/api/schedules"),
  exportInputs: () => request("/api/admin/seed/export/csv", { method: "POST" }),
  async downloadSchedule() {
    const response = await fetch(`${BASE}/api/schedules/export.csv`);
    if (!response.ok) throw new Error(await response.text());
    const blob = await response.blob();
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "examflow-schedule.csv";
    a.click();
    URL.revokeObjectURL(url);
  }
};
