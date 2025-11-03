// ---- Features handling ----
let features = new Set();

// Initialize existing features on load
document.addEventListener('DOMContentLoaded', () => {
    // Initialize features if on form page
    const existing = document.querySelectorAll('#featuresContainer .feature-tag span:first-child');
    existing.forEach(f => features.add(f.textContent.trim()));
    updateFeaturesDisplay();
    
    // Initialize delete buttons if on data table page
    initializeDeleteButtons();
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

    if (container) {
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

        if (hiddenField) {
            hiddenField.value = Array.from(features).join(',');
        }
        if (limitMsg) {
            limitMsg.style.display = features.size >= 10 ? 'block' : 'none';
        }
    }
}

// Handle Enter key to add feature
document.addEventListener('DOMContentLoaded', () => {
    const newFeatureInput = document.getElementById('newFeature');
    if (newFeatureInput) {
        newFeatureInput.addEventListener('keypress', e => {
            if (e.key === 'Enter') {
                e.preventDefault();
                addFeature();
            }
        });
    }
});

// ---- Image upload preview ----
document.addEventListener('DOMContentLoaded', () => {
    const imagesInput = document.getElementById('images');
    if (imagesInput) {
        imagesInput.addEventListener('change', e => {
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
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert" style="position: fixed; top: 80px; right: 20px; z-index: 9999; min-width: 300px;">
            <i class="fas ${icon} mr-2"></i>${message}
            <button type="button" class="close" data-dismiss="alert">&times;</button>
        </div>
    `;
    document.body.insertAdjacentHTML('beforeend', html);

    setTimeout(() => {
        const alert = document.querySelector('.alert');
        if (alert) alert.remove();
    }, 4000);
}

// ---- Delete real estate functionality ----
let currentPropertyId = null;

function initializeDeleteButtons() {
    const deleteButtons = document.querySelectorAll('.delete-btn');
    
    deleteButtons.forEach(button => {
        button.addEventListener('click', function() {
            currentPropertyId = this.getAttribute('data-property-id');
            const propertyTitle = this.getAttribute('data-property-title');
            const propertyType = this.getAttribute('data-property-type');
            const propertyPrice = this.getAttribute('data-property-price');
            const propertyAddress = this.getAttribute('data-property-address');
            
            // Check if modal exists (enhanced version)
            const deleteModal = document.getElementById('deleteConfirmationModal');
            if (deleteModal) {
                // Use enhanced modal version
                showDeleteModal(propertyTitle, propertyType, propertyPrice, propertyAddress);
            } else {
                // Use simple confirm version
                if (confirm(`Are you sure you want to delete "${propertyTitle}" (${propertyType}) at ${propertyAddress}?`)) {
                    deleteRealEstate(currentPropertyId);
                }
            }
        });
    });
}

// Enhanced modal version
function showDeleteModal(title, type, price, address) {
    // Populate modal with property data
    document.getElementById('propertyTitle').textContent = title;
    document.getElementById('propertyType').textContent = type;
    document.getElementById('propertyPrice').textContent = new Intl.NumberFormat().format(price);
    document.getElementById('propertyAddress').textContent = address;
    
    // Reset confirmation input
    document.getElementById('confirmationInput').value = '';
    document.getElementById('confirmDeleteBtn').disabled = true;
    
    // Show modal
    $('#deleteConfirmationModal').modal('show');
}

// Handle confirmation input
document.addEventListener('DOMContentLoaded', () => {
    const confirmationInput = document.getElementById('confirmationInput');
    if (confirmationInput) {
        confirmationInput.addEventListener('input', function() {
            const confirmBtn = document.getElementById('confirmDeleteBtn');
            if (confirmBtn) {
                confirmBtn.disabled = this.value.toUpperCase() !== 'DELETE';
            }
        });
        
        confirmationInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && this.value.toUpperCase() === 'DELETE') {
                document.getElementById('confirmDeleteBtn')?.click();
            }
        });
    }
    
    // Handle delete confirmation button
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener('click', function() {
            if (currentPropertyId) {
                deleteRealEstate(currentPropertyId);
                $('#deleteConfirmationModal').modal('hide');
            }
        });
    }
});

function deleteRealEstate(propertyId) {
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content');
    
    if (!csrfToken || !csrfHeader) {
        showAlert('Security token missing. Please refresh the page.', 'error');
        return;
    }
    
    // Show loading state on the specific delete button
    const button = document.querySelector(`.delete-btn[data-property-id="${propertyId}"]`);
    if (button) {
        const originalContent = button.innerHTML;
        button.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
        button.classList.add('delete-loading');
        
        fetch(`/admin/real-estates/${propertyId}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        })
        .then(response => {
            if (response.ok) {
                showAlert('Real estate deleted successfully!', 'success');
                removeRealEstateRow(propertyId);
            } else {
                return response.text().then(text => {
                    throw new Error(text || 'Failed to delete real estate');
                });
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showAlert('Failed to delete real estate: ' + error.message, 'error');
            // Restore button state
            button.innerHTML = originalContent;
            button.classList.remove('delete-loading');
        });
    }
}

function removeRealEstateRow(propertyId) {
    const row = document.querySelector(`tr[data-property-id="${propertyId}"]`);
    
    if (row) {
        // Add fade out animation
        row.classList.add('fade-out');
        
        setTimeout(() => {
            row.remove();
            // If no rows left, show empty state
            const tableBody = document.querySelector('table tbody');
            if (tableBody && tableBody.children.length === 0) {
                showEmptyState();
            }
        }, 300);
    } else {
        // If we can't find the specific row, reload the page after a delay
        setTimeout(() => {
            location.reload();
        }, 1000);
    }
}

function showEmptyState() {
    const tableBody = document.querySelector('table tbody');
    if (tableBody && tableBody.children.length === 0) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="100%" class="text-center text-muted py-4">
                    <i class="fas fa-inbox fa-2x mb-2"></i>
                    <p>No real estates found</p>
                </td>
            </tr>
        `;
    }
}