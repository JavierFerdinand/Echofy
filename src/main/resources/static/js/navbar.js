document.addEventListener('DOMContentLoaded', function() {
    // Search input functionality
    const searchInput = document.querySelector('.search-input');
    const searchIcon = document.querySelector('.search-icon');
    const closeSearch = document.querySelector('.close-search');
    
    if (searchInput && searchIcon && closeSearch) {
        searchInput.addEventListener('focus', function() {
            searchIcon.style.display = 'none';
            closeSearch.style.display = 'block';
        });
        
        searchInput.addEventListener('blur', function() {
            if (!this.value) {
                searchIcon.style.display = 'block';
                closeSearch.style.display = 'none';
            }
        });
        
        closeSearch.addEventListener('click', function() {
            searchInput.value = '';
            searchInput.focus();
        });
    }
    
    // Upgrade button animation
    const upgradeButton = document.querySelector('.upgrade-btn');
    if (upgradeButton) {
        upgradeButton.addEventListener('mouseenter', function() {
            this.style.transform = 'scale(1.04)';
        });
        
        upgradeButton.addEventListener('mouseleave', function() {
            this.style.transform = 'scale(1)';
        });
    }
});