
    function logoutSpotify() {
      const newTab = window.open("https://accounts.spotify.com/logout", "_blank");
      // Tunggu logout selesai, lalu tutup tab baru
      setTimeout(() => {
        if (newTab) newTab.close(); // hanya bisa kalau browser mengizinkan
        window.location.href = "/"; // redirect tab saat ini ke home
      }, 1000); // delay 1 detik
    }

    // Add hover effect for profile dropdown
    document.addEventListener('DOMContentLoaded', function () {
      const profileContainer = document.querySelector('.profile-dropdown-container');

      profileContainer.addEventListener('mouseenter', function () {
        this.querySelector('.profile-dropdown-content').style.display = 'block';
      });

      profileContainer.addEventListener('mouseleave', function () {
        this.querySelector('.profile-dropdown-content').style.display = 'none';
      });
    });