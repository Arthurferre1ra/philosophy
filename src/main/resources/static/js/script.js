const grid = document.querySelector('#philosopher-grid');
const searchInput = document.querySelector('#search-input');
const emptyState = document.querySelector('#empty-state');
const filterPills = document.querySelectorAll('.filter-pill');

let selectedPeriod = 'Todos';

function updateVisibleCards() {
  if (!grid) return;

  const search = searchInput ? searchInput.value.trim().toLowerCase() : '';
  const cards = Array.from(grid.querySelectorAll('.philosopher-card'));
  let visibleCount = 0;

  cards.forEach((card) => {
    const matchesPeriod = selectedPeriod === 'Todos' || card.dataset.period === selectedPeriod;
    const matchesSearch = !search || card.dataset.search.includes(search);
    const isVisible = matchesPeriod && matchesSearch;

    card.classList.toggle('hidden', !isVisible);
    if (isVisible) visibleCount += 1;
  });

  if (emptyState) {
    emptyState.classList.toggle('hidden', visibleCount > 0);
  }
}

if (searchInput) {
  searchInput.addEventListener('input', updateVisibleCards);
}

filterPills.forEach((pill) => {
  pill.addEventListener('click', () => {
    selectedPeriod = pill.dataset.period;
    filterPills.forEach((item) => item.classList.remove('selected'));
    pill.classList.add('selected');
    updateVisibleCards();
  });
});

updateVisibleCards();
