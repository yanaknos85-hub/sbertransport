INSERT INTO tariff.transport_type (id, name, formula)
VALUES ('7f18ce71-99a7-47b5-b285-335058c6715c',
        'taxi',
        '(tripData.distance > tariff.priceDetails.minDistance ? tripData.distance - tariff.priceDetails.minDistance ' ||
        ': 0) * tariff.pricePerMile +' ||
        ' (tripData.time > tariff.priceDetails.minTime ? tripData.time - tariff.priceDetails.minTime : 0) * tariff' ||
        '.priceDetails.minutePrice +' ||
        ' tariff.priceDetails.submission +' ||
        ' (tripData.waitingTime > tariff.priceDetails.freeWaitingTime ? tripData.waitingTime - tariff.priceDetails' ||
        '.freeWaitingTime : 0) * tariff.priceDetails.waitingPrice');

INSERT INTO tariff.transport_type (id, name, formula)
VALUES ('1a33601d-4db4-4720-8d09-95f015770fe0',
        'personal',
        'tripData.distance * ' ||
        'tariff.pricePerMile * ' ||
        '(new Date("tariff.priceDetails.season.start") <= new Date("tripData.tripDate") ' ||
        '&& new Date("tripData.tripDate") <= ' ||
        'new Date("tariff.priceDetails.season.end") ' ||
        '? tariff.priceDetails.season.coefficient : 1)');