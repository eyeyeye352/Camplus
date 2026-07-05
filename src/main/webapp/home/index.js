const fixedTitle = document.querySelector('.fixed-title');
const activationZone = 200;

document.addEventListener('mousemove', (e) => {
    const mouseX = e.clientX;
    const mouseY = e.clientY;
    const windowWidth = window.innerWidth;
    const windowHeight = window.innerHeight;

    if (mouseX <= activationZone && mouseY >= windowHeight - activationZone) {
        fixedTitle.classList.add('active');
    } else {
        fixedTitle.classList.remove('active');
    }
});

function initHomePage() {
    const menuButton = document.getElementById('menuButton');
    const sideMenu = document.getElementById('sideMenu');

    if (!menuButton || !sideMenu) {
        setTimeout(initHomePage, 100);
        return;
    }

    let heroGuideHidden = false;

    menuButton.addEventListener('click', () => {
        const heroGuide = document.querySelector('.hero-guide');
        if (heroGuide && !heroGuideHidden && sideMenu.classList.contains('active')) {
            heroGuide.style.opacity = '0';
            heroGuideHidden = true;
        }
    });
}

initHomePage();