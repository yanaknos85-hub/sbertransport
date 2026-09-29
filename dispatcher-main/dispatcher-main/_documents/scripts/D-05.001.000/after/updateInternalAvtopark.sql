update dispatcher.contractor dc
set is_internal = true
from contractors.contractor cc
where dc.id = cc.external_id;