// CSRF token for Spring Security
function getCsrfToken() {
    return document.querySelector('meta[name="_csrf"]').getAttribute('content');
}

function getCsrfHeader() {
    return document.querySelector('meta[name="_csrf_header"]').getAttribute('content');
}

// Form submission handler
document.getElementById('realEstateForm').addEventListener('submit', async function(e) {
    e.preventDefault();
    
    const form = this;
    const isEdit = form.getAttribute('th:method') === 'PUT';
    const submitButton = form.querySelector('button[type="submit"]');
    const originalText = submitButton.innerHTML;
    
    // Show loading state
    submitButton.disabled = true;
    submitButton.innerHTML = '<i class="fas fa-spinner fa-spin"></i> ' + (isEdit ? 'Updating...' : 'Creating...');
    
    try {
        let response;
        
        if (isEdit) {
            // For edit, use JSON (no file uploads)
            const formData = {
                title: document.getElementById('title').value,
                description: document.getElementById('description').value,
                propertyType: document.getElementById('propertyType').value,
                price: parseFloat(document.getElementById('price').value),
                address: document.getElementById('address').value,
                city: document.getElementById('city').value,
                state: document.getElementById('state').value,
                zipCode: document.getElementById('zipCode').value,
                sizeInSqMt: document.getElementById('sizeInSqMt').value ? parseFloat(document.getElementById('sizeInSqMt').value) : null,
                listingType: document.getElementById('listingType').value,
                features: document.getElementById('featuresData').value ? document.getElementById('featuresData').value.split(',') : []
            };
            
            response = await fetch(form.action, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    'X-CSRF-TOKEN': document.getElementById('csrfToken').value
                },
                body: JSON.stringify(formData)
            });
        } else {
            // For create, use FormData (for file uploads)
            const formData = new FormData(form);
            
            // Add features to FormData
            const featuresValue = document.getElementById('featuresData').value;
            if (featuresValue) {
                formData.append('features', featuresValue);
            }
            
            response = await fetch(form.action, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': document.getElementById('csrfToken').value
                },
                body: formData
            });
        }
        
        if (response.ok) {
            showAlert(`Real estate ${isEdit ? 'updated' : 'created'} successfully!`, 'success');
            setTimeout(() => {
                window.location.href = '/admin/real-estates';
            }, 1500);
        } else {
            const errorData = await response.json();
            throw new Error(errorData.message || `Failed to ${isEdit ? 'update' : 'create'} real estate`);
        }
    } catch (error) {
        showAlert('Error: ' + error.message, 'error');
    } finally {
        // Restore button state
        submitButton.disabled = false;
        submitButton.innerHTML = originalText;
    }
});

// Alert function
function showAlert(message, type) {
    const alertClass = type === 'success' ? 'alert-success' : 'alert-danger';
    const alertHtml = `
        <div class="alert ${alertClass} alert-dismissible fade show" role="alert">
            ${message}
            <button type="button" class="close" data-dismiss="alert">
                <span>&times;</span>
            </button>
        </div>
    `;
    document.querySelector('.content-header').insertAdjacentHTML('afterend', alertHtml);
    
    // Auto remove alert after 5 seconds
    setTimeout(() => {
        const alert = document.querySelector('.alert');
        if (alert) {
            alert.remove();
        }
    }, 5000);
}