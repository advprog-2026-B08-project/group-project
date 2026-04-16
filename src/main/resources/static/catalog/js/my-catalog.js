function deleteCatalog(id) {
    if (confirm('Are you sure you want to delete this product?')) {
        fetch('/api/catalogs/' + id, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json'
            }
        })
            .then(response => {
                if (response.ok) {
                    alert('Product deleted successfully!');
                    location.reload();
                } else {
                    alert('Failed to delete product');
                }
            })
            .catch(error => {
                console.error('Error:', error);
                alert('An error occurred');
            });
    }
}