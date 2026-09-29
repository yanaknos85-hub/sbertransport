import { TransportCompensation } from 'stores/PublicRegistry/models/PublicRegistry.interface';
import { TransportCompensations } from 'stores/TransportTypes/TransportTypes.interface';

export class TripRequestModel {
  compensationType?: TransportCompensations;

  transportCompensation?: TransportCompensation[];

  constructor(trip: { compensationType: TransportCompensations; transportCompensation: TransportCompensation[] }) {
    this.compensationType = trip.compensationType;
    this.transportCompensation = trip.transportCompensation;
  }
}
