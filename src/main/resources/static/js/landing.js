const API = (window.HV_API_BASE || '').replace(/\/$/, '');
const HIST = 'hv_history';
if (localStorage.getItem('hv_draft')) document.getElementById('resumeBtn').hidden = false;

function render(list) {
  const box = document.getElementById('histBox');
  if (!list.length) {
    box.innerHTML = '<div style="text-align:center;padding:20px"><svg width="64" height="64" viewBox="0 0 64 64" fill="none" stroke="#8d9bc7" stroke-width="2"><rect x="12" y="8" width="40" height="48" rx="6"/><path d="M22 24h20M22 34h20M22 44h12"/></svg><p class="muted">No saved assessments in this browser yet.<br><a href="assessment.html">Start your first assessment</a></p></div>';
    return;
  }
  box.innerHTML = list.map(a => `<div class="row"><div><b>${esc(a.label || 'Assessment')}</b>${a.demo ? ' <span class="small" style="color:#fbbf24">(demo)</span>' : ''}
    <div class="small muted">${new Date(a.createdAt).toLocaleString()} · BMI ${a.bmi} · ${a.riskFactorCount} factors</div></div>
    <div><span class="badge ${a.overallIndicator}">${a.overallIndicator}</span> <a class="btn" href="assessment.html?report=${a.id}">View report</a> <button class="btn" data-del="${a.id}">Delete</button></div></div>`).join('');
}
const esc = s => String(s).replace(/[&<>"]/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'}[c]));

async function load() {
  const stored = JSON.parse(localStorage.getItem(HIST) || '[]');
  if (!stored.length) return render([]);
  try {
    const r = await fetch(API + '/api/assessments/history', {method: 'POST', headers: {'Content-Type': 'application/json'},
      body: JSON.stringify(stored.map(s => ({id: s.id, accessCode: s.accessCode})))});
    render(await r.json());
  } catch (e) {
    document.getElementById('histBox').innerHTML = '<p class="muted">The Java server is not reachable, so saved assessments cannot be listed. Start the backend and refresh.</p>';
  }
}
document.getElementById('histBox').addEventListener('click', async e => {
  const id = e.target.dataset.del; if (!id) return;
  const stored = JSON.parse(localStorage.getItem(HIST) || '[]');
  const item = stored.find(s => s.id === id); if (!item) return;
  await fetch(API + '/api/assessments/' + id, {method: 'DELETE', headers: {'X-Access-Code': item.accessCode}});
  localStorage.setItem(HIST, JSON.stringify(stored.filter(s => s.id !== id)));
  load();
});
load();
