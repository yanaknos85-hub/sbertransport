-- src/test/resources/sql/test_data_expired_drafts.sql

-- Вставка 5 просроченных черновиков (expires_at в прошлом)

INSERT INTO exchange_request.request (
    id,
    humanreadable_id,
    internal_id,
    owner_id,
    status,
    use_etrn,
    view_type,
    payment_form,
    payment_terms,
    request_created,
    created_at,
    expires_at,
    updated_at,
    published_at,
    completed_at,
    cost_request,
    vat_include,
    organization_id
) VALUES
    -- Черновик 1
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380006', 'ОП-202602-0000006', 'INT-REQ-006', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'DRAFT', true, 'FIXED', 'NON_CASH', 'PREPAYMENT', '2026-01-01', '2026-01-01T10:00:00', '2026-01-03T10:00:00', NULL, NULL, NULL, 999000.0, TRUE, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'),

    -- Черновик 2
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380007', 'ОП-202602-0000007', 'INT-REQ-007', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380002', 'DRAFT', false, 'FIXED', 'CASH', 'ON_DELIVERY', '2026-01-02', '2026-01-02T11:00:00', '2026-01-04T11:00:00', NULL, NULL, NULL, 1000.0, FALSE, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'),

    -- Черновик 3
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380008', 'ОП-202602-0000008', 'INT-REQ-008', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003', 'DRAFT', true, 'FIXED', 'NON_CASH', 'DEFERRED_PAYMENT', '2026-01-03', '2126-01-03T09:00:00', '2126-01-05T09:00:00', NULL, NULL, NULL, 9000.0, TRUE, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'),

    -- Черновик 4
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380009', 'ОП-202602-0000009', 'INT-REQ-009', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001', 'DRAFT', false, 'FIXED', 'NON_CASH', 'PREPAYMENT', '2026-01-04', '2126-01-04T14:00:00', '2126-01-06T14:00:00', NULL, NULL, NULL, 22000.0, TRUE, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004'),

    -- Черновик 5
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380010', 'ОП-202602-0000010', 'INT-REQ-010', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004', 'PUBLISHED', true, 'FIXED', 'CASH', 'ON_DELIVERY', '2026-01-05', '2026-01-05T16:00:00', '2026-01-07T16:00:00', NULL, NULL, NULL, 400.0, FALSE, 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380004');

