DO
$do$
    DECLARE
        rec record;
    BEGIN
    --update reports.request
        	FOR rec IN select id, "request_status" from request.request_for_personal
            	LOOP
					update
						reports.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
        	FOR rec IN select id, "request_status" from request.request_for_taxi
            	LOOP
					update
						reports.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
        	FOR rec IN select id, "request_status" from request.request_for_group_transfer
            	LOOP
					update
						reports.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
        	FOR rec IN select id, "request_status" from request.request_for_carsharing
            	LOOP
					update
						reports.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
        	FOR rec IN select id, "request_status" from request.request_for_public
            	LOOP
					update
						reports.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;

    --update oto.request
        	FOR rec IN select id, "request_status" from request.request_for_personal
            	LOOP
					update
						oto.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
        	FOR rec IN select id, "request_status" from request.request_for_taxi
            	LOOP
					update
						oto.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
        	FOR rec IN select id, "request_status" from request.request_for_group_transfer
            	LOOP
					update
						oto.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
        	FOR rec IN select id, "request_status" from request.request_for_carsharing
            	LOOP
					update
						oto.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
        	FOR rec IN select id, "request_status" from request.request_for_public
            	LOOP
					update
						oto.request r
					set
						"request_status" = rec."request_status"
					where r.id = rec.id;
            	END LOOP;
    END
$do$;