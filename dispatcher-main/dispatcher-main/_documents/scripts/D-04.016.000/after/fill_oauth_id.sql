update trips.dispatcher set oauth_id = d.oauth_id
from dispatcher.dispatcher as d where d.id = trips.dispatcher.id;

update trips_cargo.dispatcher set oauth_id = d.oauth_id
from dispatcher.dispatcher as d where d.id = trips_cargo.dispatcher.id;