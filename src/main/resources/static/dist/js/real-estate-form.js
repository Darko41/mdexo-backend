// === Feature Management ===
let features = new Set();

function updateFeaturesDisplay() {
    const container = document.getElementById('featuresContainer');
    const limitMessage = document.getElementById('featuresLimit');
    container.innerHTML = '';

    features.forEach(feature => {
        const featureTag = document.createElement('span');
        featureTag.className = 'feature-tag';
        featureTag.innerHTML = `
            <span>${feature}</span>
            <span class="feature-remove" data-feature="${feature}" onclick="removeFeatureFromEvent(this)">×</span>
        `;
        container.appendChild(featureTag);
    });

    document.getElementById('featuresData').value = Array.from(features).join(',');
    limitMessage.style.display = features.size >= 10 ? 'block' : 'none';
}

function addFeature() {
    const input = document.getElementById('newFeature');
    const feature = input.value.trim();

    if (feature && features.size < 10) {
        features.add(feature);
        updateFeaturesDisplay();
        input.value = '';
    }
}

function removeFeature(feature) {
    features.delete(feature);
    updateFeaturesDisplay();
}

function removeFeatureFromEvent(element) {
    const feature = element.getAttribute('data-feature');
    removeFeature(feature);
}

document.getElementById('newFeature')?.addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        e.preventDefault();
        addFeature();
    }
});

document.addEventListener('DOMContentLoaded', function() {
    updateFeaturesDisplay();
});
