// ---- Features handling ----
let features = new Set();

// Initialize existing features on load
document.addEventListener('DOMContentLoaded', () => {
    const existing = document.querySelectorAll('#featuresContainer .feature-tag span:first-child');
    existing.forEach(f => features.add(f.textContent.trim()));
    updateFeaturesDisplay();
});

// Add a feature
function addFeature() {
    const input = document.getElementById('newFeature');
    const feature = input.value.trim();
    if (feature && features.size < 10 && !features.has(feature)) {
        features.add(feature);
        updateFeaturesDisplay();
        input.value = '';
    }
}

// Remove feature from event
function removeFeatureFromEvent(el) {
    const feature = el.getAttribute('data-feature');
    features.delete(feature);
    updateFeaturesDisplay();
}

// Update DOM + hidden field
function updateFeaturesDisplay() {
    const container = document.getElementById('featuresContainer');
    const limitMsg = document.getElementById('featuresLimit');
    const hiddenField = document.getElementById('featuresData');

    container.innerHTML = '';
    features.forEach(f => {
        const tag = document.createElement('span');
        tag.className = 'feature-tag';
        tag.innerHTML = `
            <span>${f}</span>
            <span class="feature-remove" data-feature="${f}" onclick="removeFeatureFromEvent(this)">×</span>
        `;
        container.appendChild(tag);
    });

    hiddenField.value = Array.from(features).join(',');
    limitMsg.style.display = features.size >= 10 ? 'block' : 'none';
}

// Handle Enter key to add feature
document.getElementById('newFeature')?.addEventListener('keypress', e => {
    if (e.key === 'Enter') {
        e.preventDefault();
        addFeature();
    }
});

// ---- Image upload preview ----
document.getElementById('images')?.addEventListener('change', e => {
    const count = e.target.files.length;
    const status = document.getElementById('imageUploadStatus');
    const uploadedCount = document.getElementById('uploadedCount');
    if (count > 0) {
        uploadedCount.textContent = count;
        status.style.display = 'block';
    } else {
        status.style.display = 'none';
    }
});

// ---- Image delete (for edit mode) ----
function removeImageFromEvent(element) {
    const propertyId = element.getAttribute('data-property-id');
    const imageUrl = element.getAttribute('data-image-url');
    const csrfToken = document.getElementById('csrfToken')?.value;

    if (confirm('Remove this image?')) {
        fetch(`/api/admin/real-estates/${propertyId}/images`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ imageUrl })
        })
        .then(r => {
            if (r.ok) {
                element.closest('.image-preview').remove();
                showAlert('Image removed successfully', 'success');
            } else {
                showAlert('Failed to remove image', 'error');
            }
        })
        .catch(() => showAlert('Error removing image', 'error'));
    }
}

// ---- Simple alert helper ----
function showAlert(message, type = 'success') {
    const alertClass = type === 'success' ? 'alert-success' : 'alert-danger';
    const icon = type === 'success' ? 'fa-check' : 'fa-exclamation-triangle';

    const html = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert" style="margin:10px 0;">
            <i class="fas ${icon} mr-2"></i>${message}
            <button type="button" class="close" data-dismiss="alert">&times;</button>
        </div>
    `;
    document.querySelector('.content-header')?.insertAdjacentHTML('afterend', html);

    setTimeout(() => document.querySelector('.alert')?.remove(), 4000);
}
