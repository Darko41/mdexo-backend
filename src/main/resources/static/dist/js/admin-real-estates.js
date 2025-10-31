$(function () {
    $('#realEstatesTable').DataTable({
        "paging": true,
        "lengthChange": true,
        "searching": true,
        "ordering": true,
        "info": true,
        "autoWidth": false,
        "responsive": true,
        "order": [[0, "desc"]],
        "columnDefs": [
            { "orderable": false, "targets": [15] },
            { "width": "80px", "targets": [0] },
            { "width": "120px", "targets": [3, 4, 5, 6, 7, 8, 9, 10, 14] },
            { "width": "150px", "targets": [15] }
        ]
    });
    
    $('.clickable-row').on('click', function(e) {
        if (!$(e.target).is('button') && !$(e.target).is('a') && !$(e.target).closest('button').length && !$(e.target).closest('a').length) {
            const viewLink = $(this).find('a[href*="/view"]');
            if (viewLink.length) {
                window.location.href = viewLink.attr('href');
            }
        }
    });
});