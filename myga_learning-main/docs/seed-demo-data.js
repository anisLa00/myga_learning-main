/*
 * Demo dataset for MYGA Learning — creates the data shown in the README screenshots.
 *
 * It only calls the public REST API, exactly as a real client would, so it also
 * doubles as an end-to-end smoke test of the whole stack.
 *
 *   1. start the backend   (cd backend && ./mvnw spring-boot:run)
 *   2. node docs/seed-demo-data.js
 *
 * The backend must be freshly started: it seeds only the default administrator,
 * and this script assumes an otherwise empty database. Demo passwords below are
 * for local development only.
 */

const API = 'http://localhost:8080/api';

async function login(email, password) {
  const r = await fetch(`${API}/auth/login`, {
    method: 'POST', headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ email, password }),
  });
  if (!r.ok) throw new Error(`login ${email} -> ${r.status} ${await r.text()}`);
  return (await r.json()).accessToken;
}

function client(token) {
  return async (method, path, body) => {
    const r = await fetch(`${API}${path}`, {
      method,
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
    const text = await r.text();
    if (!r.ok) throw new Error(`${method} ${path} -> ${r.status} ${text}`);
    return text ? JSON.parse(text) : null;
  };
}

const d = (offset) => {
  const t = new Date(); t.setDate(t.getDate() - offset);
  return t.toISOString().slice(0, 10);
};

(async () => {
  const admin = client(await login('admin@myga.local', 'admin123'));

  // --- academic calendar ---
  const year = await admin('POST', '/academic-years',
    { label: '2026-2027', startDate: '2026-09-01', endDate: '2027-06-30', current: true });
  const sem = await admin('POST', '/semesters',
    { label: 'Semester 1', academicYearId: year.id, startDate: '2026-09-01', endDate: '2027-01-31' });
  console.log('year', year.id, 'semester', sem.id);

  // --- structure ---
  const c101 = await admin('POST', '/classes', { salle: 101 });
  const c102 = await admin('POST', '/classes', { salle: 102 });
  const maths = await admin('POST', '/subjects', { nom: 'Mathematics', code: 'MATH' });
  const physics = await admin('POST', '/subjects', { nom: 'Physics', code: 'PHYS' });
  const history = await admin('POST', '/subjects', { nom: 'History', code: 'HIST' });

  // --- teachers ---
  const turing = await admin('POST', '/teachers',
    { nom: 'Turing', prenom: 'Alan', email: 'turing@myga.local', password: 'teacher123' });
  const curie = await admin('POST', '/teachers',
    { nom: 'Curie', prenom: 'Marie', email: 'curie@myga.local', password: 'teacher123' });
  await admin('POST', `/teachers/${turing.id}/subjects/${maths.id}`);
  await admin('POST', `/teachers/${turing.id}/subjects/${physics.id}`);
  await admin('POST', `/teachers/${turing.id}/classes/${c101.id}`);
  await admin('POST', `/teachers/${curie.id}/subjects/${history.id}`);
  await admin('POST', `/teachers/${curie.id}/classes/${c102.id}`);

  // --- students ---
  const mk = (nom, prenom, age, classeId) => admin('POST', '/students', { nom, prenom, age, classeId });
  const ada = await mk('Lovelace', 'Ada', 16, c101.id);
  const leo = await mk('Martin', 'Leo', 16, c101.id);
  const sofia = await mk('Rossi', 'Sofia', 15, c101.id);
  const noah = await mk('Bernard', 'Noah', 16, c101.id);
  const emma = await mk('Dubois', 'Emma', 15, c102.id);
  console.log('students', [ada, leo, sofia, noah, emma].map((s) => `${s.id}:${s.prenom}`).join(' '));

  // --- parents (accounts created by the administration) ---
  const pierre = await admin('POST', '/parents',
    { nom: 'Lovelace', prenom: 'Pierre', email: 'pierre@myga.local', password: 'parent123' });
  const claire = await admin('POST', '/parents',
    { nom: 'Martin', prenom: 'Claire', email: 'claire@myga.local', password: 'parent123' });
  await admin('POST', `/parents/${pierre.phone}/students/${ada.id}`);
  await admin('POST', `/parents/${pierre.phone}/students/${sofia.id}`);
  await admin('POST', `/parents/${claire.phone}/students/${leo.id}`);

  // --- teacher work: assessments, grades, attendance, observations ---
  const t = client(await login('turing@myga.local', 'teacher123'));
  const asm = (title, subjectId, type, maxGrade, date) =>
    t('POST', '/assessments', { title, subjectId, classeId: c101.id, type, maxGrade, date, semesterId: sem.id });
  const a1 = await asm('Algebra test', maths.id, 'EXAM', 20, d(7));
  const a2 = await asm('Geometry quiz', maths.id, 'QUIZ', 20, d(5));
  const a3 = await asm('Functions exam', maths.id, 'EXAM', 20, d(1));
  const a4 = await asm('Mechanics lab report', physics.id, 'PROJECT', 20, d(3));
  await asm('Trigonometry exam', maths.id, 'EXAM', 20, d(-7)); // upcoming

  const grade = (studentId, assessmentId, value, comment, date) =>
    t('POST', '/grades', { studentId, assessmentId, value, comment, date });

  // Ada: a clearly improving term
  await grade(ada.id, a1.id, 11.5, 'Struggles with factorisation.', d(7));
  await grade(ada.id, a2.id, 13, 'Better method, watch the units.', d(5));
  await grade(ada.id, a4.id, 16, 'Very well structured report.', d(3));
  await grade(ada.id, a3.id, 17.5, 'Excellent progress this term.', d(1));
  // Classmates
  await grade(leo.id, a1.id, 14, 'Solid work.', d(7));
  await grade(leo.id, a2.id, 12.5, null, d(5));
  await grade(leo.id, a3.id, 15, 'Good improvement.', d(1));
  await grade(sofia.id, a1.id, 16.5, 'Very good.', d(7));
  await grade(sofia.id, a3.id, 18, 'Top of the class.', d(1));
  await grade(noah.id, a1.id, 9, 'Needs regular revision.', d(7));
  await grade(noah.id, a3.id, 12, 'Real effort, keep going.', d(1));

  const roll = (date, statuses) => t('POST', '/attendance/bulk', {
    classeId: c101.id, subjectId: maths.id, date,
    entries: [ada, leo, sofia, noah].map((s, i) => ({
      studentId: s.id, status: statuses[i], note: statuses[i] === 'ABSENT' ? 'No excuse received' : null,
    })),
  });
  await roll(d(4), ['PRESENT', 'PRESENT', 'PRESENT', 'ABSENT']);
  await roll(d(3), ['LATE', 'PRESENT', 'PRESENT', 'PRESENT']);
  await roll(d(2), ['PRESENT', 'PRESENT', 'EXCUSED', 'ABSENT']);
  await roll(d(1), ['EXCUSED', 'PRESENT', 'PRESENT', 'PRESENT']);

  await t('POST', '/observations', { studentId: ada.id, subjectId: maths.id, type: 'POSITIVE_FEEDBACK',
    message: 'Excellent participation this term — asks precise questions.', date: d(1), visibleToParents: true });
  await t('POST', '/observations', { studentId: ada.id, subjectId: maths.id, type: 'HOMEWORK',
    message: 'Homework handed in on time all term.', date: d(4), visibleToParents: true });
  await t('POST', '/observations', { studentId: noah.id, type: 'CONCERN',
    message: 'Internal note: repeated absences, to discuss with the head teacher.', date: d(3), visibleToParents: false });

  // --- announcements ---
  await admin('POST', '/announcements', { title: 'Parent–teacher meetings',
    message: 'Parent–teacher meetings take place on Friday from 16:00 to 19:00 in the main hall. '
           + 'Slots can be booked with the class teacher.', target: 'PARENTS' });
  await admin('POST', '/announcements', { title: 'Staff meeting',
    message: 'Teaching staff meeting on Wednesday at 17:00, room 101.', target: 'TEACHERS' });

  console.log('seed complete');
})().catch((e) => { console.error('SEED FAILED:', e.message); process.exit(1); });
