update reports.request
set vehicle = jsonb_build_object(
        'id', v.id,
        'brand', v.brand,
        'model', v.model,
        'stateNumber', v.state_number,
        'color', v.color,
        'autoparkId', v.autopark_id
    )
from reports.vehicle v where request.vehicle_id = v.id;