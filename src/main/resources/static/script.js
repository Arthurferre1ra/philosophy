const philosophers = [
  { name: 'Sócrates', initials: 'S', period: 'Antiguidade', years: '470 — 399 a.C.', school: 'Filosofia clássica', quote: 'Só sei que nada sei.', color: '' },
  { name: 'Platão', initials: 'P', period: 'Antiguidade', years: '428 — 348 a.C.', school: 'Idealismo', quote: 'A direção em que a educação inicia um homem determinará o seu futuro.', color: 'blue' },
  { name: 'Aristóteles', initials: 'A', period: 'Antiguidade', years: '384 — 322 a.C.', school: 'Aristotelismo', quote: 'A excelência não é um ato, mas um hábito.', color: 'green' },
  { name: 'René Descartes', initials: 'R', period: 'Modernidade', years: '1596 — 1650', school: 'Racionalismo', quote: 'Penso, logo existo.', color: 'rose' },
  { name: 'Immanuel Kant', initials: 'K', period: 'Modernidade', years: '1724 — 1804', school: 'Iluminismo', quote: 'Ouse saber! Tenha coragem de usar seu próprio entendimento.', color: 'lilac' },
  { name: 'Simone de Beauvoir', initials: 'S', period: 'Modernidade', years: '1908 — 1986', school: 'Existencialismo', quote: 'Mudar a vida: eis o que todos querem, mas não sabem como.', color: 'terracotta' }
];

const grid = document.querySelector('#philosopher-grid');
const searchInput = document.querySelector('#search-input');
const emptyState = document.querySelector('#empty-state');
const count = document.querySelector('#philosopher-count');
let selectedPeriod = 'Todos';

function renderCards() {
  const search = searchInput.value.trim().toLowerCase();
  const visible = philosophers.filter((philosopher) => {
    const matchesPeriod = selectedPeriod === 'Todos' || philosopher.period === selectedPeriod;
    const matchesSearch = [philosopher.name, philosopher.school, philosopher.quote].some((value) => value.toLowerCase().includes(search));
    return matchesPeriod && matchesSearch;
  });

  grid.innerHTML = visible.map((philosopher, index) => `
    <article class="philosopher-card">
      <div class="card-top"><div class="portrait ${philosopher.color}">${philosopher.initials}</div><span class="period-tag">${philosopher.period}</span></div>
      <h3>${philosopher.name}</h3><span class="school">${philosopher.school}</span>
      <p class="quote">“${philosopher.quote}”</p>
      <button class="detail-link" data-index="${philosophers.indexOf(philosopher)}">Ver perfil completo <span>→</span></button>
    </article>`).join('');

  emptyState.classList.toggle('hidden', visible.length > 0);
  count.textContent = String(philosophers.length).padStart(2, '0');
}

const modalBackdrop = document.querySelector('#modal-backdrop');
const detailBackdrop = document.querySelector('#detail-backdrop');
const form = document.querySelector('#philosopher-form');

function toggleModal(modal, show) { modal.classList.toggle('hidden', !show); if (show) modal.querySelector('input, button').focus(); }

document.querySelector('#open-modal').addEventListener('click', () => toggleModal(modalBackdrop, true));
document.querySelector('#close-modal').addEventListener('click', () => toggleModal(modalBackdrop, false));
document.querySelector('#cancel-modal').addEventListener('click', () => toggleModal(modalBackdrop, false));
document.querySelector('#close-detail').addEventListener('click', () => toggleModal(detailBackdrop, false));

form.addEventListener('submit', (event) => {
  event.preventDefault();
  const data = new FormData(form);
  const name = data.get('name').trim();
  philosophers.unshift({ name, initials: name.charAt(0).toUpperCase(), period: data.get('period'), years: 'Dados a completar', school: data.get('school').trim(), quote: data.get('quote').trim(), color: 'blue' });
  form.reset(); toggleModal(modalBackdrop, false); selectedPeriod = 'Todos'; document.querySelectorAll('.filter-pill').forEach((pill) => pill.classList.toggle('selected', pill.dataset.period === 'Todos')); renderCards();
});

grid.addEventListener('click', (event) => {
  const button = event.target.closest('.detail-link');
  if (!button) return;
  const philosopher = philosophers[button.dataset.index];
  document.querySelector('#detail-content').innerHTML = `<p class="eyebrow">PERFIL DO FILÓSOFO</p><h2>${philosopher.name}</h2><p class="detail-quote">“${philosopher.quote}”</p><div class="detail-meta"><div>Período<strong>${philosopher.period}</strong></div><div>Vida<strong>${philosopher.years}</strong></div><div>Escola<strong>${philosopher.school}</strong></div></div>`;
  toggleModal(detailBackdrop, true);
});

searchInput.addEventListener('input', renderCards);
document.querySelectorAll('.filter-pill').forEach((pill) => pill.addEventListener('click', () => { selectedPeriod = pill.dataset.period; document.querySelectorAll('.filter-pill').forEach((item) => item.classList.remove('selected')); pill.classList.add('selected'); renderCards(); }));
document.addEventListener('keydown', (event) => { if (event.key === 'Escape') { toggleModal(modalBackdrop, false); toggleModal(detailBackdrop, false); } });
renderCards();
