// Admin panel functionality

async function blockUser(userId) {
    if (!confirm('Are you sure you want to block this user?')) return;

    try {
        await apiCall(`/api/admin/users/${userId}/block`, 'POST');
        showNotification('User blocked successfully', 'success');
        location.reload();
    } catch (error) {
        showNotification('Failed to block user', 'error');
    }
}

async function unblockUser(userId) {
    if (!confirm('Are you sure you want to unblock this user?')) return;

    try {
        await apiCall(`/api/admin/users/${userId}/unblock`, 'POST');
        showNotification('User unblocked successfully', 'success');
        location.reload();
    } catch (error) {
        showNotification('Failed to unblock user', 'error');
    }
}

async function grantRole(userId, role) {
    try {
        await apiCall(`/api/admin/users/${userId}/roles/${role}`, 'POST');
        showNotification(`Role ${role} granted successfully`, 'success');
        location.reload();
    } catch (error) {
        showNotification('Failed to grant role', 'error');
    }
}

async function revokeRole(userId, role) {
    if (!confirm(`Are you sure you want to revoke ${role} from this user?`)) return;

    try {
        await apiCall(`/api/admin/users/${userId}/roles/${role}`, 'DELETE');
        showNotification(`Role ${role} revoked successfully`, 'success');
        location.reload();
    } catch (error) {
        showNotification('Failed to revoke role', 'error');
    }
}




