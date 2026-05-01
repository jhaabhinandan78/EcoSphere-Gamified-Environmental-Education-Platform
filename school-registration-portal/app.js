// School Code Generator (same logic as Android app)
class SchoolCodeGenerator {
    static generateCode(schoolName, city, year = new Date().getFullYear()) {
        // Extract first 8 characters from school name (alphanumeric only)
        const cleanName = schoolName.replace(/[^a-zA-Z0-9]/g, '');
        // IMPORTANT: Take EXACTLY 8 characters, pad if less
        const namePrefix = cleanName.substring(0, 8).padEnd(8, 'X');
        
        // Generate 2-character random suffix
        const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
        const randomSuffix = chars.charAt(Math.floor(Math.random() * chars.length)) +
                           chars.charAt(Math.floor(Math.random() * chars.length));
        
        // Format: NAME(8) + YEAR(4) + RANDOM(2) = 14 characters total
        // Example: KHALSAMO + 2026 + MK = KHALSAMO2026MK (14 chars)
        // IMPORTANT: Return UPPERCASE for Android app compatibility
        const code = `${namePrefix}${year}${randomSuffix}`.toUpperCase();
        
        // Verify length is exactly 14
        if (code.length !== 14) {
            console.error(`❌ Generated code has wrong length: ${code.length} (expected 14)`);
            console.error(`   Code: ${code}`);
            console.error(`   namePrefix: ${namePrefix} (${namePrefix.length})`);
            console.error(`   year: ${year} (${year.toString().length})`);
            console.error(`   randomSuffix: ${randomSuffix} (${randomSuffix.length})`);
        }
        
        return code;
    }

    static generateSchoolId(schoolName, year = new Date().getFullYear()) {
        const cleanName = schoolName.toLowerCase()
            .replace(/[^a-z0-9]/g, '_')
            .replace(/_+/g, '_')
            .replace(/^_|_$/g, '');
        return `${cleanName}_${year}`;
    }
}

// Form handling
document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('schoolRegistrationForm');
    const submitBtn = document.getElementById('submitBtn');
    const successModal = document.getElementById('successModal');
    const errorModal = document.getElementById('errorModal');
    
    // Form submission
    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Disable submit button
        submitBtn.disabled = true;
        submitBtn.querySelector('.btn-text').style.display = 'none';
        submitBtn.querySelector('.btn-loader').style.display = 'inline-block';
        
        try {
            // Get form data
            const formData = {
                schoolName: document.getElementById('schoolName').value.trim(),
                schoolType: document.getElementById('schoolType').value,
                city: document.getElementById('city').value.trim(),
                state: document.getElementById('state').value.trim(),
                country: document.getElementById('country').value.trim(),
                address: document.getElementById('address').value.trim(),
                principalName: document.getElementById('principalName').value.trim(),
                contactEmail: document.getElementById('contactEmail').value.trim(),
                contactPhone: document.getElementById('contactPhone').value.trim(),
                alternateEmail: document.getElementById('alternateEmail').value.trim(),
                studentCount: parseInt(document.getElementById('studentCount').value),
                teacherCount: parseInt(document.getElementById('teacherCount').value),
                website: document.getElementById('website').value.trim(),
                notes: document.getElementById('notes').value.trim()
            };
            
            // Generate school code and ID
            const schoolCode = SchoolCodeGenerator.generateCode(
                formData.schoolName,
                formData.city
            );
            const schoolId = SchoolCodeGenerator.generateSchoolId(formData.schoolName);
            
            // Check if school already exists
            const existingSchool = await db.collection('Schools')
                .where('schoolCode', '==', schoolCode)
                .get();
            
            if (!existingSchool.empty) {
                throw new Error('A school with similar details already exists. Please contact support.');
            }
            
            // Create school document
            const schoolData = {
                schoolId: schoolId,
                schoolName: formData.schoolName,
                schoolCode: schoolCode,
                schoolType: formData.schoolType,
                city: formData.city,
                state: formData.state,
                country: formData.country,
                address: formData.address,
                principalName: formData.principalName,
                contactEmail: formData.contactEmail,
                contactPhone: formData.contactPhone,
                alternateEmail: formData.alternateEmail || null,
                studentCount: formData.studentCount,
                teacherCount: formData.teacherCount,
                website: formData.website || null,
                notes: formData.notes || null,
                active: true,
                createdAt: firebase.firestore.FieldValue.serverTimestamp(),
                registrationSource: 'web_portal'
            };
            
            // Save to Firestore
            await db.collection('Schools').doc(schoolId).set(schoolData);
            
            console.log('✅ School registered successfully:', schoolCode);
            
            // Show success modal
            document.getElementById('generatedCode').textContent = schoolCode;
            document.getElementById('confirmEmail').textContent = formData.contactEmail;
            showModal(successModal);
            
            // Store data for download
            window.registrationData = {
                ...schoolData,
                schoolCode: schoolCode
            };
            
            // Reset form
            form.reset();
            
            // Send email (you'll need to implement this with a backend service)
            // For now, we'll just log it
            console.log('📧 Email should be sent to:', formData.contactEmail);
            console.log('School Code:', schoolCode);
            
        } catch (error) {
            console.error('❌ Registration error:', error);
            document.getElementById('errorMessage').textContent = 
                error.message || 'An error occurred during registration. Please try again.';
            showModal(errorModal);
        } finally {
            // Re-enable submit button
            submitBtn.disabled = false;
            submitBtn.querySelector('.btn-text').style.display = 'inline-block';
            submitBtn.querySelector('.btn-loader').style.display = 'none';
        }
    });
    
    // Copy code to clipboard
    document.getElementById('copyCodeBtn').addEventListener('click', function() {
        const codeText = document.getElementById('generatedCode').textContent;
        navigator.clipboard.writeText(codeText).then(() => {
            const btn = this;
            const originalText = btn.textContent;
            btn.textContent = '✅ Copied!';
            btn.style.background = '#28a745';
            setTimeout(() => {
                btn.textContent = originalText;
                btn.style.background = '';
            }, 2000);
        }).catch(err => {
            console.error('Failed to copy:', err);
            alert('Failed to copy code. Please copy manually.');
        });
    });
    
    // Download details as text file
    document.getElementById('downloadBtn').addEventListener('click', function() {
        if (!window.registrationData) return;
        
        const data = window.registrationData;
        const content = `
EcoLearn Platform - School Registration Details
================================================

School Information:
------------------
School Name: ${data.schoolName}
School Code: ${data.schoolCode}
School Type: ${data.schoolType}
School ID: ${data.schoolId}

Location:
---------
City: ${data.city}
State: ${data.state}
Country: ${data.country}
Address: ${data.address}

Contact Information:
-------------------
Principal/Head: ${data.principalName}
Email: ${data.contactEmail}
Phone: ${data.contactPhone}
${data.alternateEmail ? 'Alternate Email: ' + data.alternateEmail : ''}
${data.website ? 'Website: ' + data.website : ''}

School Details:
--------------
Student Count: ${data.studentCount}
Teacher Count: ${data.teacherCount}

Important Instructions:
----------------------
1. Keep this school code secure and confidential
2. Share this code only with authorized teachers
3. The first teacher to register will become the Lead Teacher
4. Lead Teacher can approve additional teachers
5. Teachers need this code to register in the mobile app

Registration Date: ${new Date().toLocaleString()}

For support, contact: support@ecolearn.edu
        `.trim();
        
        const blob = new Blob([content], { type: 'text/plain' });
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = `${data.schoolCode}_registration_details.txt`;
        document.body.appendChild(a);
        a.click();
        document.body.removeChild(a);
        URL.revokeObjectURL(url);
    });
    
    // Close modals
    document.getElementById('closeModalBtn').addEventListener('click', () => {
        hideModal(successModal);
    });
    
    document.getElementById('closeErrorBtn').addEventListener('click', () => {
        hideModal(errorModal);
    });
    
    // Close modal on background click
    [successModal, errorModal].forEach(modal => {
        modal.addEventListener('click', function(e) {
            if (e.target === modal) {
                hideModal(modal);
            }
        });
    });
});

// Modal helpers
function showModal(modal) {
    modal.classList.add('active');
    document.body.style.overflow = 'hidden';
}

function hideModal(modal) {
    modal.classList.remove('active');
    document.body.style.overflow = '';
}

// Form validation helpers
document.getElementById('contactEmail').addEventListener('blur', function() {
    const email = this.value.trim();
    if (email && !isValidEmail(email)) {
        this.setCustomValidity('Please enter a valid email address');
    } else {
        this.setCustomValidity('');
    }
});

document.getElementById('contactPhone').addEventListener('blur', function() {
    const phone = this.value.trim();
    if (phone && phone.length < 10) {
        this.setCustomValidity('Please enter a valid phone number');
    } else {
        this.setCustomValidity('');
    }
});

function isValidEmail(email) {
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

// Auto-format phone number
document.getElementById('contactPhone').addEventListener('input', function(e) {
    let value = e.target.value.replace(/\D/g, '');
    if (value.length > 0 && !value.startsWith('+')) {
        // Add country code if not present
        if (value.length === 10) {
            value = '+91' + value; // Default to India, adjust as needed
        }
    }
});

console.log('✅ Application initialized');
