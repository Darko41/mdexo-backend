// CSRF token for Spring Security with fallback
function getCsrfToken() {
    const csrfMeta = document.querySelector('meta[name="_csrf"]');
    return csrfMeta ? csrfMeta.getAttribute('content') : '';
}

function getCsrfHeader() {
    const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
    return csrfHeaderMeta ? csrfHeaderMeta.getAttribute('content') : 'X-CSRF-TOKEN';
}

// Global variable to store the current property ID to delete
let currentPropertyIdToDelete = null;

function setupDeleteHandlers() {
    // Event delegation for delete buttons
    document.addEventListener('click', function(e) {
        if (e.target.closest('.delete-btn')) {
            const button = e.target.closest('.delete-btn');
            const propertyId = button.getAttribute('data-property-id');
            const propertyTitle = button.getAttribute('data-property-title');
            const propertyType = button.getAttribute('data-property-type');
            const propertyPrice = button.getAttribute('data-property-price');
            const propertyAddress = button.getAttribute('data-property-address');
            
            deleteRealEstate(propertyId, propertyTitle, propertyType, propertyPrice, propertyAddress);
        }
    });
}

// Update the deleteRealEstate function to accept parameters
function deleteRealEstate(propertyId, propertyTitle, propertyType, propertyPrice, propertyAddress) {
    currentPropertyIdToDelete = propertyId;
    
    // Populate property details in modal if elements exist
    const propertyTitleEl = document.getElementById('propertyTitle');
    const propertyTypeEl = document.getElementById('propertyType');
    const propertyPriceEl = document.getElementById('propertyPrice');
    const propertyAddressEl = document.getElementById('propertyAddress');
    
    if (propertyTitleEl) propertyTitleEl.textContent = propertyTitle || 'Unknown Property';
    if (propertyTypeEl) propertyTypeEl.textContent = propertyType || 'N/A';
    if (propertyPriceEl) propertyPriceEl.textContent = propertyPrice ? '$' + propertyPrice : 'N/A';
    if (propertyAddressEl) propertyAddressEl.textContent = propertyAddress || 'No address';
    
    // Reset the confirmation input and button
    document.getElementById('confirmationInput').value = '';
    document.getElementById('confirmDeleteBtn').disabled = true;
    
    // Show the modal
    $('#deleteConfirmationModal').modal('show');
}

// Initialize event handlers when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    setupDeleteHandlers();
    
    // Confirmation input validation
    const confirmationInput = document.getElementById('confirmationInput');
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    
    if (confirmationInput && confirmDeleteBtn) {
        confirmationInput.addEventListener('input', function() {
            confirmDeleteBtn.disabled = this.value.toUpperCase() !== 'DELETE';
        });
        
        // Enter key support
        confirmationInput.addEventListener('keypress', function(e) {
            if (e.key === 'Enter' && !confirmDeleteBtn.disabled) {
                confirmDeleteBtn.click();
            }
        });
        
        // Confirm delete button click
        confirmDeleteBtn.addEventListener('click', performDelete);
        
        // Clear input when modal is hidden (added this for better UX)
        $('#deleteConfirmationModal').on('hidden.bs.modal', function() {
            confirmationInput.value = '';
            confirmDeleteBtn.disabled = true;
            currentPropertyIdToDelete = null;
        });
    }
});

// Perform the actual deletion
async function performDelete() {
    if (!currentPropertyIdToDelete) return;
    
    const confirmDeleteBtn = document.getElementById('confirmDeleteBtn');
    const originalText = confirmDeleteBtn.innerHTML;
    
    // Show loading state
    confirmDeleteBtn.disabled = true;
    confirmDeleteBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-1"></i> Deleting...';
    
    try {
        const headers = {
            'Content-Type': 'application/json'
        };
        
        // Only add CSRF token if available
        const csrfToken = getCsrfToken();
        const csrfHeader = getCsrfHeader();
        if (csrfToken) {
            headers[csrfHeader] = csrfToken;
        }
        
        const response = await fetch(`/api/admin/real-estates/${currentPropertyIdToDelete}`, {
            method: 'DELETE',
            headers: headers
        });
        
        if (response.ok) {
            // Close modal first
            $('#deleteConfirmationModal').modal('hide');
            
            // Show success message
            showAlert('Real estate deleted successfully!', 'success');
            
            // Reload the page after a short delay
            setTimeout(() => {
                window.location.reload();
            }, 1500);
            
        } else if (response.status === 403) {
            throw new Error('Permission denied. Please check if you are logged in.');
        } else {
            const errorData = await response.json();
            throw new Error(errorData.message || 'Failed to delete real estate');
        }
    } catch (error) {
        // Close modal on error
        $('#deleteConfirmationModal').modal('hide');
        showAlert('Error deleting real estate: ' + error.message, 'error');
    } finally {
        // Reset button state
        confirmDeleteBtn.disabled = false;
        confirmDeleteBtn.innerHTML = originalText;
        currentPropertyIdToDelete = null;
    }
}

// Alert function for success/error messages
function showAlert(message, type) {
    // Remove existing alerts
    const existingAlert = document.querySelector('.alert-dismissible:not(.modal .alert)');
    if (existingAlert) {
        existingAlert.remove();
    }
    
    const alertClass = type === 'success' ? 'alert-success' : 'alert-danger';
    const iconClass = type === 'success' ? 'fa-check' : 'fa-exclamation-triangle';
    const title = type === 'success' ? 'Success!' : 'Error!';
    
    const alertHtml = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert" style="margin: 20px;">
            <button type="button" class="close" data-dismiss="alert" aria-hidden="true">&times;</button>
            <h5><i class="icon fas ${iconClass}"></i> ${title}</h5>
            ${message}
        </div>
    `;
    
    const contentHeader = document.querySelector('.content-header');
    if (contentHeader) {
        contentHeader.insertAdjacentHTML('afterend', alertHtml);
    }
    
    // Auto remove alert after 5 seconds
    setTimeout(() => {
        const alert = document.querySelector('.alert-dismissible:not(.modal .alert)');
        if (alert) {
            alert.remove();
        }
    }, 5000);
}

// Optional: Add keyboard shortcuts
document.addEventListener('keydown', function(e) {
    // ESC key to close modal
    if (e.key === 'Escape' && $('#deleteConfirmationModal').is(':visible')) {
        $('#deleteConfirmationModal').modal('hide');
    }
});