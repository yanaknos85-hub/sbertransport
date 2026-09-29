update authentication.account t
    set email = s.email,
    phone = s.mobile_phone
    from corporate.employee s
    where t.id = s.id;