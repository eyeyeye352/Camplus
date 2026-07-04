import { bindEvents, initCurrentUser } from './event.js';
import { initElements } from './state.js';

function initApp() {
    initElements();
    initCurrentUser();
    bindEvents();
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', initApp);
} else {
    initApp();
}
