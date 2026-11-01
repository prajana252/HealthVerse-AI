const API = (window.HV_API_BASE || '').replace(/\/$/, '');
const DRAFT = 'hv_draft', HIST = 'hv_history';
const STEPS = ['Profile', 'Lifestyle', 'Body metrics', 'Symptoms', 'Analysis', 'Insights', 'Plan', 'Report'];
const SYMPTOMS = [['fatigue', 'Persistent fatigue', '🔋'], ['headaches', 'Frequent headaches', '🤕'], ['breathlessness', 'Breathlessness', '🌬️'],
  ['chest_pain', 'Chest discomfort', '💢'], ['dizziness', 'Dizziness', '😵'], ['frequent_thirst', 'Frequent thirst', '💧'],
  ['frequent_urination', 'Frequent urination', '🚻'], ['sleep_disturbance', 'Sleep disturbances', '🌙'], ['cough', 'Persistent cough', '😮‍💨'],
  ['weight_change', 'Unexplained weight change', '⚖️'], ['digestive', 'Digestive discomfort', '🍽️'], ['joint_pain', 'Joint pain', '🦴'],
  ['fainting', 'Fainting', '⚠️'], ['severe_breathlessness', 'Severe breathing difficulty', '🚨']];
const EMERGENCY = ['chest_pain', 'fainting', 'severe_breathlessness'];
const DEMO = {name: 'Demo User', age: 52, sex: 'male', heightCm: 170, weightKg: 82, waistCm: 102, systolic: 138, diastolic: 88,
  conditions: ['hypertension'], diabetesFamily: 'ONE_PARENT', heartFamily: true, activityLevel: 'SEDENTARY', exerciseDays: 1, sleepHours: 5.5,
  sleepQuality: 2, diet: 'HIGH_PROCESSED', waterLitres: 1.25, tobacco: 'FORMER', alcohol: 'OCCASIONAL', stress: 7, screenHours: 9,
  symptoms: ['fatigue', 'frequent_thirst']};
const params = new URLSearchParams(location.search);
const demo = params.has('demo'), reportId = params.get('report');
let S = Object.assign({conditions: [], symptoms: [], activityLevel: '', exerciseDays: 3, sleepHours: 7, sleepQuality: 3, diet: 'BALANCED',
  waterLitres: 2, tobacco: 'NEVER', alcohol: 'NONE', stress: 4, screenHours: 4, diabetesFamily: '', heartFamily: false},
  demo ? DEMO : JSON.parse(localStorage.getItem(DRAFT) || '{}'));
let step = 1, analysis = null;
const $ = (s, r = document) => r.querySelector(s), $$ = (s, r = document) => [...r.querySelectorAll(s)];
const persist = () => { if (!demo && !reportId) localStorage.setItem(DRAFT, JSON.stringify(S)); };
const toast = m => { const t = $('#toast'); t.textContent = m; t.style.display = 'block'; setTimeout(() => t.style.display = 'none', 3500); };
const esc = s => String(s).replace(/[&<>"]/g, c => ({'&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;'}[c]));
const num = v => (v === '' || v == null || isNaN(v)) ? null : Number(v);

function payload() {
  return {age: num(S.age), sex: S.sex || null, heightCm: num(S.heightCm), weightKg: num(S.weightKg), waistCm: num(S.waistCm),
    systolic: num(S.systolic), diastolic: num(S.diastolic), conditions: S.conditions, diabetesFamily: S.diabetesFamily || null,
    heartFamily: !!S.heartFamily, activityLevel: S.activityLevel || null, sleepHours: num(S.sleepHours), sleepQuality: num(S.sleepQuality),
    diet: S.diet || null, waterLitres: num(S.waterLitres), tobacco: S.tobacco || null, alcohol: S.alcohol || null,
    stress: num(S.stress), screenHours: num(S.screenHours), symptoms: S.symptoms};
}

/* ---------- rendering ---------- */
function fill() {
  $$('[data-k]').forEach(el => {
    if (el.matches('.opt')) el.classList.toggle('on', S[el.dataset.k] === el.dataset.v);
    else if (el.type === 'checkbox') el.checked = !!S[el.dataset.k];
    else el.value = S[el.dataset.k] ?? '';
  });
  $$('[data-g]').forEach(el => el.classList.toggle('on', (S[el.dataset.g] || []).includes(el.dataset.v)));
  $$('[data-o]').forEach(o => o.textContent = S[o.dataset.o] + (o.dataset.unit || ''));
  $('#emerg').hidden = !S.symptoms.some(s => EMERGENCY.includes(s));
}
function renderStepper() {
  $('#stepper').innerHTML = STEPS.map((n, i) => `<div class="st ${i + 1 < step ? 'done' : i + 1 === step ? 'now' : ''}">${i + 1}. ${n}</div>`).join('');
  $('#prog').style.width = (step / STEPS.length * 100) + '%';
}
function show(n) {
  step = n;
  $$('.slide').forEach((s, i) => s.classList.toggle('on', i + 1 === n));
  $('#prevBtn').hidden = n === 1;
  $('#nextBtn').hidden = n === 5 || n >= 8;
  $('#nextBtn').textContent = n === 4 ? 'Run analysis' : 'Continue';
  renderStepper(); fill(); window.scrollTo({top: 0, behavior: 'smooth'});
  if (n === 3) bmi();
  if (n === 5) enter5();
  if (n === 6) renderDashboard();
  if (n === 7) renderPlan();
  if (n === 8) renderReport();
}

/* ---------- validation ---------- */
function showErr(e) { $$('[data-err]').forEach(el => el.textContent = e[el.dataset.err] || ''); return !Object.keys(e).length; }
function valid(n) {
  const e = {};
  if (n === 1) {
    if (!(S.age >= 18 && S.age <= 120)) e.age = 'Enter an age between 18 and 120.';
    if (!S.sex) e.sex = 'Choose an option.';
    if (!(S.heightCm >= 50 && S.heightCm <= 272)) e.heightCm = 'Enter a height between 50 and 272 cm.';
    if (!(S.weightKg >= 10 && S.weightKg <= 500)) e.weightKg = 'Enter a weight between 10 and 500 kg.';
  }
  if (n === 3) {
    if (S.waistCm && !(S.waistCm >= 40 && S.waistCm <= 200)) e.waistCm = 'Waist must be 40 to 200 cm.';
    if (S.systolic && !(S.systolic >= 70 && S.systolic <= 250)) e.systolic = 'Enter 70 to 250.';
    if (S.diastolic && !(S.diastolic >= 40 && S.diastolic <= 150)) e.diastolic = 'Enter 40 to 150.';
    if (!!S.systolic !== !!S.diastolic) e.diastolic = 'Enter both blood pressure numbers, or neither.';
  }
  const ok = showErr(e);
  if (!ok) toast('Please fix the highlighted fields.');
  return ok;
}

/* ---------- events ---------- */
document.addEventListener('input', e => {
  const t = e.target, k = t.dataset.k; if (!k || t.matches('.opt')) return;
  S[k] = t.type === 'checkbox' ? t.checked : t.value;
  $$(`[data-k="${k}"]`).forEach(o => { if (o !== t && o.type !== 'checkbox' && !o.matches('.opt')) o.value = t.value; });
  $$(`[data-o="${k}"]`).forEach(o => o.textContent = S[k] + (o.dataset.unit || ''));
  persist();
  if (k === 'heightCm' || k === 'weightKg') { clearTimeout(bmi.t); bmi.t = setTimeout(bmi, 250); }
});
document.addEventListener('click', e => {
  const el = e.target.closest('button'); if (!el) return;
  if (el.matches('.opt')) { S[el.dataset.k] = el.dataset.v; persist(); fill(); }
  else if (el.dataset.g) {
    const list = S[el.dataset.g] = S[el.dataset.g] || [], v = el.dataset.v, i = list.indexOf(v);
    i >= 0 ? list.splice(i, 1) : list.push(v); persist(); fill();
  } else if (el.dataset.nav === 'next') { if (valid(step)) show(step + 1); }
  else if (el.dataset.nav === 'prev') show(step - 1);
  else if (el.dataset.nav === 'exit') { persist(); location.href = 'index.html'; }
});
$('#symSearch').addEventListener('input', e => {
  const q = e.target.value.toLowerCase();
  $$('#symGrid .card').forEach(c => c.hidden = !c.textContent.toLowerCase().includes(q));
});
$('#symGrid').innerHTML = SYMPTOMS.map(([k, n, i]) => `<button type="button" class="card" data-g="symptoms" data-v="${k}"><span>${i}</span>${n}</button>`).join('');

/* ---------- BMI gauge ---------- */
const pt = b => { const t = Math.PI * (1 - (Math.min(40, Math.max(10, b)) - 10) / 30); return [100 + 90 * Math.cos(t), 100 - 90 * Math.sin(t)]; };
const seg = (a, b, c) => { const [x1, y1] = pt(a), [x2, y2] = pt(b); return `<path d="M${x1} ${y1} A90 90 0 0 1 ${x2} ${y2}" stroke="${c}" stroke-width="14" fill="none"/>`; };
const gaugeSvg = id => seg(10, 18.5, '#38bdf8') + seg(18.5, 25, '#34d399') + seg(25, 30, '#fbbf24') + seg(30, 40, '#fb7185') +
  `<g class="needle" id="${id}" style="transform:rotate(-90deg)"><line x1="100" y1="100" x2="100" y2="26" stroke="#fff" stroke-width="3" stroke-linecap="round"/><circle cx="100" cy="100" r="6" fill="#fff"/></g>`;
$('#gauge').innerHTML = gaugeSvg('needle'); $('#gauge2').innerHTML = gaugeSvg('needle2');
async function bmi() {
  if (!(S.heightCm >= 50 && S.weightKg >= 10)) { $('#bmiVal').textContent = '--'; $('#bmiCat').textContent = 'Enter height and weight'; return; }
  try {
    const r = await fetch(API + '/api/bmi', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({heightCm: +S.heightCm, weightKg: +S.weightKg})});
    const d = await r.json(); if (!r.ok) throw new Error();
    $('#bmiVal').textContent = d.bmi; $('#bmiCat').textContent = d.category; $('#bmiTxt').textContent = d.explanation;
    $('#bmiRange').innerHTML = `Healthy BMI ${d.healthyBmiMin}–${d.healthyBmiMax} ≈ <b>${d.healthyWeightMinKg}–${d.healthyWeightMaxKg} kg</b> for your height`;
    $('#needle').style.transform = `rotate(${(Math.min(40, Math.max(10, d.bmi)) - 10) / 30 * 180 - 90}deg)`;
  } catch (e) { $('#bmiCat').textContent = 'Could not reach the Java server. Is it running?'; }
}

/* ---------- analysis ---------- */
const sleep = ms => new Promise(r => setTimeout(r, ms));
const STAGES = ['Checking your answers', 'Calculating BMI', 'Cardiovascular indicators', 'Diabetes screening', 'Respiratory and sleep indicators',
  'Stress, lifestyle and nutrition', 'Combining the results'];
let plan = null, analysisKey = '';
function enter5() {
  if (analysis && analysisKey === JSON.stringify(payload())) { $('#runBox').hidden = true; $('#doneBox').hidden = false; renderDone(); }
  else runAnalysis();
}
const post = (path, body) => fetch(API + path, {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify(body)})
  .then(async r => ({ok: r.ok, data: await r.json()})).catch(() => ({ok: false, data: null}));
async function runAnalysis() {
  $('#runBox').hidden = false; $('#doneBox').hidden = true; analysis = null; plan = null;
  $('#runList').innerHTML = STAGES.map(s => `<li>${s}</li>`).join(''); $('#runBar').style.width = '0';
  const body = payload();
  const call = Promise.all([post('/api/assessments/analyze', body), post('/api/recommendations', body)]);
  const items = $$('#runList li');
  for (let i = 0; i < items.length; i++) {
    items[i].className = 'act'; await sleep(550); items[i].className = 'done';
    $('#runBar').style.width = ((i + 1) / items.length * 100) + '%';
  }
  const [res, rec] = await call;
  $('#runBox').hidden = true; $('#doneBox').hidden = false;
  if (!res.ok || !rec.ok) {
    const bad = !res.ok ? res : rec;
    const f = bad.data && bad.data.fields ? Object.values(bad.data.fields).join(' ') : 'The Java server could not be reached. Start the backend and try again.';
    $('#doneBox').innerHTML = `<div class="alert">${esc(f)}</div><button class="btn" id="retry">Try again</button>`;
    $('#retry').onclick = runAnalysis; return;
  }
  analysis = res.data; plan = rec.data; analysisKey = JSON.stringify(body); renderDone();
}
function renderDone() {
  const a = analysis;
  $('#doneBox').innerHTML = (a.emergencyWarning ? `<div class="alert"><b>Urgent:</b> ${esc(a.emergencyWarning)}</div>` : '') +
    `<h3>Analysis complete</h3><p>Overall attention level: <span class="badge ${a.overallIndicator}">${a.overallIndicator}</span> · ${a.riskFactorCount} contributing factors found</p>` +
    a.risks.map(r => `<div class="row"><div><b>${esc(r.category)}</b><div class="small muted">${esc(r.method)}</div></div><span class="badge ${r.level}">${r.level.replace('_', ' ')}${r.score != null ? ' · ' + r.score : ''}</span></div>`).join('') +
    (a.missingInputs.length ? `<p class="small muted">Not provided, so some checks were limited: ${a.missingInputs.map(esc).join(', ')}.</p>` : '') +
    `<p class="small muted">${esc(a.disclaimer)}</p>
     <button class="btn primary" data-nav="next">View results dashboard</button> <button class="btn saveBtn">Save this assessment</button> <button class="btn" data-nav="prev">Back to symptoms</button>
     <div class="saveOut"></div>`;
  if (reportId) $$('.saveBtn').forEach(b => b.hidden = true);
}
async function save() {
  try {
    const r = await fetch(API + '/api/assessments', {method: 'POST', headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({assessment: payload(), label: demo ? 'Demo assessment' : 'Assessment ' + new Date().toLocaleDateString(), demo})});
    const d = await r.json(); if (!r.ok) throw new Error();
    const h = JSON.parse(localStorage.getItem(HIST) || '[]'); h.push({id: d.id, accessCode: d.accessCode});
    localStorage.setItem(HIST, JSON.stringify(h));
    $$('.saveBtn').forEach(b => b.disabled = true);
    const msg = `<p>Saved. Your private access code is <b>${esc(d.accessCode)}</b>. It is also stored in this browser so you can find the assessment on the home page. Keep it safe: anyone with the code can open this record.</p><a class="btn" href="index.html#history">View saved assessments</a>`;
    $$('.saveOut').forEach(o => o.innerHTML = msg);
  } catch (e) { toast('Could not save. Is the Java server running?'); }
}

/* ---------- dashboard ---------- */
const COL = {LOW: '#34d399', MODERATE: '#fbbf24', HIGH: '#fb7185', OTHER: '#64748b'};
const LVL = l => COL[l] ? l : 'OTHER';
let charts = [];
function insights(a) {
  const out = [], add = (tag, text) => out.push({tag, text});
  a.risks.filter(r => r.level === 'MODERATE' || r.level === 'HIGH').forEach(r =>
    add('Screening indicator', `${r.category}: rated ${r.level.toLowerCase()} based on ${r.factors.length} factor(s) you reported. This is not a diagnosis.`));
  if (S.sleepHours && S.sleepHours < 7) add('Observed lifestyle factor', `Your sleep duration (${S.sleepHours} h) is below the recommended 7-9 hour range.`);
  if (S.activityLevel === 'SEDENTARY') add('Observed lifestyle factor', 'Your current activity level may benefit from improvement.');
  if (S.waterLitres && S.waterLitres < 1.5) add('Observed lifestyle factor', `Your water intake (${S.waterLitres} L) is lower than the roughly 2 L many adults aim for.`);
  if (S.screenHours >= 8) add('Observed lifestyle factor', `${S.screenHours} hours of screen and sitting time a day is high; regular breaks help.`);
  if (S.tobacco === 'CURRENT') add('Observed lifestyle factor', 'Current tobacco use is one of the most important health factors you can change.');
  if (S.heartFamily || ['ONE_PARENT', 'BOTH_PARENTS'].includes(S.diabetesFamily))
    add('Self-reported (not confirmed)', 'Your family history makes it important to discuss appropriate screening with a healthcare professional.');
  if (S.conditions.length) add('Self-reported (not confirmed)', `You reported: ${S.conditions.map(c => c.replace('_', ' ')).join(', ')}. This tool has not confirmed any condition.`);
  if (a.bmi.category !== 'Normal weight') add('Screening indicator', `Your BMI of ${a.bmi.bmi} falls in the "${a.bmi.category}" range. BMI is a screening measure only.`);
  if (!out.length) add('Observed lifestyle factor', 'No notable concerns were found in the information you provided. Keep up your habits and have routine check-ups.');
  return out;
}
function habitScores() {
  const dist = S.sleepHours >= 7 && S.sleepHours <= 9 ? 0 : S.sleepHours < 7 ? 7 - S.sleepHours : S.sleepHours - 9;
  return [['Activity', {SEDENTARY: 20, MODERATE: 60, VIGOROUS: 100}[S.activityLevel] ?? null],
    ['Exercise days', S.exerciseDays == null ? null : Math.min(100, S.exerciseDays / 5 * 100)], ['Sleep', Math.max(0, 100 - dist * 25)],
    ['Hydration', Math.min(100, S.waterLitres / 2.5 * 100)], ['Low stress', (10 - S.stress) / 9 * 100],
    ['Low screen time', Math.max(0, 100 - S.screenHours / 12 * 100)], ['Diet', S.diet === 'BALANCED' ? 90 : 40]];
}
function renderDashboard() {
  const a = analysis;
  if (!a) { $('#snap').innerHTML = '<p class="muted">Run the analysis first (slide 5).</p>'; return; }
  const cnt = {LOW: 0, MODERATE: 0, HIGH: 0, OTHER: 0}; a.risks.forEach(r => cnt[LVL(r.level)]++);
  $('#snap').innerHTML = (a.emergencyWarning ? `<div class="alert"><b>Urgent:</b> ${esc(a.emergencyWarning)}</div>` : '') +
    `<div class="grid3"><div class="glass"><div class="muted small">Overall attention level</div><div class="big ${a.overallIndicator}">${a.overallIndicator}</div></div>
     <div class="glass"><div class="muted small">Risk factors identified</div><div class="big">${a.riskFactorCount}</div></div>
     <div class="glass"><div class="muted small">Categories</div><div><span class="LOW">${cnt.LOW} low</span> · <span class="MODERATE">${cnt.MODERATE} moderate</span> · <span class="HIGH">${cnt.HIGH} high</span>${cnt.OTHER ? ` · ${cnt.OTHER} not rated` : ''}</div></div></div><br>`;
  $('#riskCards').innerHTML = a.risks.map(r => `<div class="glass rc"><h3>${esc(r.category)}</h3>
    <span class="badge ${r.level}">${r.level.replace('_', ' ')}</span>${r.score != null ? ` <span class="small muted">score ${r.score}</span>` : ''}
    <p class="small muted">${esc(r.method)}</p>
    ${r.factors.length ? '<div class="lbl">Contributing factors</div><ul>' + r.factors.map(f => `<li>${esc(f)}</li>`).join('') + '</ul>' : '<p class="small">No contributing factors found.</p>'}
    <p class="small">${esc(r.explanation)}</p><div class="lbl">Suggested next step</div><ul>${r.nextSteps.map(s => `<li>${esc(s)}</li>`).join('')}</ul>
    ${r.missingInputs.length ? `<p class="small muted">Missing: ${r.missingInputs.map(esc).join(', ')}</p>` : ''}</div>`).join('');
  $('#bmi2').textContent = a.bmi.bmi; $('#bmi2c').textContent = a.bmi.category;
  setTimeout(() => $('#needle2').style.transform = `rotate(${(Math.min(40, Math.max(10, a.bmi.bmi)) - 10) / 30 * 180 - 90}deg)`, 150);
  $('#insights').innerHTML = insights(a).map(i => `<div class="insight"><span class="tag ${i.tag.startsWith('Obs') ? 'life' : i.tag.startsWith('Self') ? 'self' : ''}">${i.tag}</span>${esc(i.text)}</div>`).join('') +
    '<p class="small muted">Screening indicator: a rule-based result. Observed lifestyle factor: something you told us. Self-reported: not confirmed by any test.</p>';
  charts.forEach(c => c.destroy()); charts = [];
  if (typeof Chart === 'undefined') { $$('.chartbox').forEach(b => b.innerHTML = '<p class="muted">Charts need an internet connection to load Chart.js.</p>'); return; }
  Chart.defaults.color = '#9fb0e0'; Chart.defaults.borderColor = 'rgba(120,150,255,.18)';
  const base = {responsive: true, maintainAspectRatio: false};
  const mk = (id, cfg) => charts.push(new Chart($(id), cfg));
  mk('#cDonut', {type: 'doughnut', data: {labels: ['Low', 'Moderate', 'High', 'Not rated'], datasets: [{data: [cnt.LOW, cnt.MODERATE, cnt.HIGH, cnt.OTHER], backgroundColor: [COL.LOW, COL.MODERATE, COL.HIGH, COL.OTHER], borderWidth: 0}]}, options: base});
  mk('#cBar', {type: 'bar', data: {labels: a.risks.map(r => r.category), datasets: [{label: 'Risk level', data: a.risks.map(r => ({LOW: 1, MODERATE: 2, HIGH: 3}[r.level] || 0)), backgroundColor: a.risks.map(r => COL[LVL(r.level)])}]},
    options: {...base, indexAxis: 'y', plugins: {legend: {display: false}}, scales: {x: {min: 0, max: 3, ticks: {stepSize: 1, callback: v => ['Not rated', 'Low', 'Moderate', 'High'][v]}}}}});
  const hs = habitScores();
  mk('#cRadar', {type: 'radar', data: {labels: hs.map(h => h[0]), datasets: [{label: 'Your habits', data: hs.map(h => h[1]), backgroundColor: 'rgba(168,85,247,.25)', borderColor: '#a855f7', pointBackgroundColor: '#22d3ee'}]},
    options: {...base, scales: {r: {min: 0, max: 100, ticks: {display: false}, grid: {color: 'rgba(120,150,255,.2)'}, angleLines: {color: 'rgba(120,150,255,.2)'}}}}});
  mk('#cFact', {type: 'bar', data: {labels: a.risks.map(r => r.category), datasets: [{label: 'Factors', data: a.risks.map(r => r.factors.length), backgroundColor: '#3b82f6'}]},
    options: {...base, indexAxis: 'y', plugins: {legend: {display: false}}, scales: {x: {beginAtZero: true, ticks: {precision: 0}}}}});
}

/* ---------- action plan ---------- */
const ymd = d => d.getFullYear() + '-' + String(d.getMonth() + 1).padStart(2, '0') + '-' + String(d.getDate()).padStart(2, '0');
function weekStart() { const d = new Date(); d.setDate(d.getDate() - (d.getDay() + 6) % 7); return ymd(d); }
function renderPlan() {
  if (!plan) { $('#goals').innerHTML = '<p class="muted">Run the analysis first (slide 5).</p>'; return; }
  const checks = JSON.parse(localStorage.getItem('hv_checks') || '{}');
  const mk = (list, pfx) => list.map(g => { const key = pfx + ':' + g;
    return `<label class="goal"><input type="checkbox" class="ck" data-ck="${esc(key)}" ${checks[key] ? 'checked' : ''}><span>${esc(g)}</span></label>`; }).join('');
  $('#goals').innerHTML = `<div class="grid2"><div class="glass"><h3>Daily goals</h3><p class="small muted">Resets every day</p>${mk(plan.dailyGoals, 'd' + ymd(new Date()))}</div>
    <div class="glass"><h3>Weekly goals</h3><p class="small muted">Resets every Monday</p>${mk(plan.weeklyGoals, 'w' + weekStart())}</div></div>`;
  $('#recs').innerHTML = plan.recommendations.length ? plan.recommendations.map(r => `<div class="glass rc" style="margin-bottom:14px"><h3>${esc(r.category)} <span class="badge ${r.level}">${r.level}</span></h3>
    <p>${esc(r.meaning)}</p>${r.contributingFactors.length ? '<div class="lbl">Factors from your answers</div><ul>' + r.contributingFactors.map(f => `<li>${esc(f)}</li>`).join('') + '</ul>' : ''}
    <div class="lbl">Lifestyle</div><p>${esc(r.lifestyle)}</p><div class="lbl">Diet</div><p>${esc(r.diet)}</p><div class="lbl">Physical activity</div><p>${esc(r.activity)}</p>
    <div class="lbl">Sleep</div><p>${esc(r.sleep)}</p><div class="lbl">Stress</div><p>${esc(r.stress)}</p><div class="lbl">Professional advice</div><p>${esc(r.consultation)}</p></div>`).join('')
    : '<div class="glass"><p>No category was rated moderate or high. The goals above help you keep it that way.</p></div>';
  updateProgress();
}
function updateProgress() {
  const all = $$('.ck'), done = all.filter(c => c.checked).length, pct = all.length ? Math.round(done / all.length * 100) : 0;
  $('#planProg').innerHTML = `<p>${done} of ${all.length} goals done (${pct}%)</p><div class="bar"><i style="width:${pct}%"></i></div><br>`;
}
document.addEventListener('change', e => {
  if (!e.target.classList.contains('ck')) return;
  const c = JSON.parse(localStorage.getItem('hv_checks') || '{}'); c[e.target.dataset.ck] = e.target.checked;
  localStorage.setItem('hv_checks', JSON.stringify(c)); updateProgress();
});
let savedAt = null, savedDemo = false;
document.addEventListener('click', e => {
  if (e.target.closest('.saveBtn')) save();
  if (e.target.closest('#pdfBtn')) downloadPdf();
});

/* ---------- report ---------- */
const nice = v => v ? String(v).toLowerCase().replace(/_/g, ' ') : 'not answered';
async function renderReport() {
  if (!analysis) { $('#reportBody').innerHTML = '<p class="muted">Run the analysis first (slide 5).</p>'; return; }
  const a = analysis;
  const date = (savedAt ? new Date(savedAt) : new Date()).toLocaleDateString(undefined, {day: 'numeric', month: 'long', year: 'numeric'});
  $('#reportMeta').textContent = 'Assessment date: ' + date + (reportId ? ' (saved report)' : '');
  if (reportId) $$('.saveBtn').forEach(b => b.hidden = true);
  const kv = (k, v) => v == null || v === '' ? '' : `<div class="row"><span class="muted">${esc(k)}</span><span style="text-align:right">${esc(v)}</span></div>`;
  const ul = l => '<ul>' + l.map(x => `<li>${esc(x)}</li>`).join('') + '</ul>';
  const symNames = S.symptoms.map(k => (SYMPTOMS.find(s => s[0] === k) || [k, k])[1]);
  const factors = [...new Set(a.risks.flatMap(r => r.factors))];
  const concerns = a.risks.filter(r => r.level === 'MODERATE' || r.level === 'HIGH');
  const steps = [...new Set(concerns.flatMap(r => r.nextSteps))];
  $('#reportBody').innerHTML = (a.emergencyWarning ? `<div class="alert"><b>Urgent:</b> ${esc(a.emergencyWarning)}</div>` : '') +
    `<h3>Personal profile</h3>${kv('Name', S.name)}${kv('Age', S.age + ' years')}${kv('Biological sex', nice(S.sex))}${kv('Height / weight', S.heightCm + ' cm / ' + S.weightKg + ' kg')}
     ${kv('Waist', S.waistCm ? S.waistCm + ' cm' : null)}${kv('Blood pressure', S.systolic && S.diastolic ? S.systolic + '/' + S.diastolic : null)}${kv('Blood group', S.bloodGroup)}
     ${kv('Reported conditions', S.conditions.length ? S.conditions.join(', ').replace(/_/g, ' ') : 'None reported')}${kv('Medications', S.medications)}${kv('Allergies', S.allergies)}
     <h3 style="margin-top:18px">BMI</h3><p><b>${a.bmi.bmi}</b> (${esc(a.bmi.category)}). Healthy range about ${a.bmi.healthyWeightMinKg}-${a.bmi.healthyWeightMaxKg} kg for your height. <span class="small muted">${esc(a.bmi.disclaimer)}</span></p>
     <h3>Overall assessment</h3><p>Overall attention level: <span class="badge ${a.overallIndicator}">${a.overallIndicator}</span>, ${a.riskFactorCount} contributing factors.</p>
     <h3>Lifestyle analysis</h3>${kv('Activity', nice(S.activityLevel))}${kv('Exercise days per week', S.exerciseDays)}${kv('Sleep', S.sleepHours + ' h, quality ' + S.sleepQuality + '/5')}${kv('Diet', nice(S.diet))}
     ${kv('Water', S.waterLitres + ' L/day')}${kv('Tobacco', nice(S.tobacco))}${kv('Alcohol', nice(S.alcohol))}${kv('Stress', S.stress + '/10')}${kv('Screen and sitting time', S.screenHours + ' h/day')}
     <h3 style="margin-top:18px">Symptom summary</h3>${kv('Symptoms', symNames.length ? symNames.join(', ') : 'None selected')}${kv('Duration', S.duration)}${kv('Severity', S.severity)}${kv('Frequency', S.frequency)}${kv('Recent changes', S.recentChanges)}
     <h3 style="margin-top:18px">Individual risk assessments</h3>` +
    a.risks.map(r => `<div class="row"><span>${esc(r.category)}</span><span class="badge ${r.level}">${r.level.replace('_', ' ')}${r.score != null ? ' · ' + r.score : ''}</span></div>`).join('') +
    `<h3 style="margin-top:18px">Main contributing factors</h3>${factors.length ? ul(factors.slice(0, 12)) : '<p class="muted">None identified.</p>'}
     <h3>Recommendations</h3>${plan.recommendations.length ? ul(plan.recommendations.map(r => r.category + ': ' + r.lifestyle + ' ' + r.consultation)) : '<p class="muted">No Moderate or High categories. Keep up your habits.</p>'}
     <h3>Suggested next steps</h3>${steps.length ? ul(steps) : '<p>Continue routine check-ups and your current healthy habits.</p>'}
     <div class="glass" style="margin-top:18px"><h2>HOW YOUR RESULTS WERE GENERATED</h2><div id="methBox"><p class="muted">Loading…</p></div></div>`;
  const m = await post('/api/reports/methodology', payload());
  $('#methBox').innerHTML = m.ok ? `<div class="rc"><div class="lbl">Information considered</div>${ul(m.data.consideredInputs)}
    <div class="lbl">Information missing</div>${ul(m.data.notProvided)}<div class="lbl">Assessment rules applied</div>${ul(m.data.rulesApplied)}
    <div class="lbl">How risk categories were determined</div>${ul(m.data.categoryDetermination)}<div class="lbl">What this system cannot determine</div>${ul(m.data.cannotDetermine)}
    <div class="lbl">Why this is not a medical diagnosis</div>${ul(m.data.notADiagnosis)}</div>` : '<p class="muted">Could not load this section. Is the Java server running?</p>';
}
async function downloadPdf() {
  const btn = $('#pdfBtn'); btn.disabled = true; btn.textContent = 'Preparing PDF…';
  const day = savedAt ? savedAt.slice(0, 10) : ymd(new Date());
  try {
    const r = await fetch(API + '/api/reports/pdf', {method: 'POST', headers: {'Content-Type': 'application/json'}, body: JSON.stringify({
      assessment: payload(), name: S.name || null, bloodGroup: S.bloodGroup || null, medications: S.medications || null, allergies: S.allergies || null,
      duration: S.duration || null, severity: S.severity || null, frequency: S.frequency || null, recentChanges: S.recentChanges || null,
      assessmentDate: day, demo: demo || savedDemo})});
    if (!r.ok) throw new Error();
    const a = document.createElement('a'); a.href = URL.createObjectURL(await r.blob()); a.download = 'healthverse-report-' + day + '.pdf';
    document.body.appendChild(a); a.click(); a.remove(); URL.revokeObjectURL(a.href);
  } catch (e) { toast('Could not create the PDF. Is the Java server running?'); }
  finally { btn.disabled = false; btn.textContent = 'Download PDF report'; }
}
async function loadReport() {
  const item = JSON.parse(localStorage.getItem(HIST) || '[]').find(s => s.id === reportId);
  if (!item) { toast('This saved report is not stored in this browser.'); show(1); return; }
  try {
    const r = await fetch(API + '/api/assessments/' + reportId, {headers: {'X-Access-Code': item.accessCode}});
    const d = await r.json(); if (!r.ok) throw new Error();
    S = Object.assign({conditions: [], symptoms: []}, d.request); S.exerciseDays = null;
    analysis = d.result; plan = d.plan; analysisKey = JSON.stringify(payload()); savedAt = d.createdAt; savedDemo = d.demo;
    $('#demoBanner').hidden = !d.demo; show(8);
  } catch (e) { toast('Could not open the saved report. Is the Java server running?'); show(1); }
}

if (demo) $('#demoBanner').hidden = false;
reportId ? loadReport() : show(1);
