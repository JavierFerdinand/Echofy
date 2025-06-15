function filterSidebar(type) {
  const playlists = document.querySelectorAll('.playlist-type');
  const artists = document.querySelectorAll('.artist-type');
  const buttons = document.querySelectorAll('.pill-btn');

  // Update tombol aktif
  buttons.forEach(btn => {
    btn.classList.remove('active');
    const label = btn.textContent.trim().toLowerCase();
    if ((type === 'playlist' && label === 'playlists') ||
        (type === 'artist' && label === 'artists') ||
        (type === 'all' && label === 'all')) {
      btn.classList.add('active');
    }
  });

  // Filter tampilan berdasarkan tipe
  if (type === 'playlist') {
    playlists.forEach(el => el.style.display = 'flex');
    artists.forEach(el => el.style.display = 'none');
  } else if (type === 'artist') {
    playlists.forEach(el => el.style.display = 'none');
    artists.forEach(el => el.style.display = 'flex');
  } else {
    playlists.forEach(el => el.style.display = 'flex');
    artists.forEach(el => el.style.display = 'flex');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  filterSidebar('all');
});
