update contractors.employee empl
set consent = (select consent from corporate.employee ce where ce.id = empl.id);