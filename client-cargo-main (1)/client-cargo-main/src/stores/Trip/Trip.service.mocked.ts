import { injectable } from 'inversify';

import { TripPaginationModel } from './models/TripPagination.model';
import {
  ITripService,
  ITripTariff,
  SavedFileInfo,
  TCoopTripsSettings,
  TExternalPrices,
  TripPurpose,
  TripRequest,
  TTariffPersonal,
  TTariffPublic,
  TTariffTaxi,
  TTripCoop
} from './Trip.interface';

@injectable()
export class TripServiceMocked implements ITripService {
  getTripRequestList = async (): Promise<TripRequest[]> => (await import('mock/stores/Trip/getTripRequestList.json')).default as TripRequest[];

  getTripRequestListTerminal = async (): Promise<TripPaginationModel & { content: TripRequest[] }> => (await import('mock/stores/Trip/getTripRequestListWithPagination.json')).default as TripPaginationModel & {
    content: TripRequest[];
  };

  getTripRequestListNonTerminal = async (): Promise<TripPaginationModel & { content: TripRequest[] }> => (await import('mock/stores/Trip/getTripRequestListWithPagination.json')).default as TripPaginationModel & {
    content: TripRequest[];
  };

  getTripRequest = async (): Promise<TripRequest> => (await import('mock/stores/Trip/getTripRequest.json')).default as TripRequest;

  getAllPurposesByEmployee = async (): Promise<TripPurpose[]> => (await import('mock/stores/Trip/getAllPurposes.json')).default as TripPurpose[];

  getAllTariffsTaxi = async (): Promise<TTariffTaxi[]> => (await import('mock/stores/Trip/getAllTariffsTaxi.json')).default as TTariffTaxi[];

  getAllTariffsPersonal = async (): Promise<TTariffPersonal[]> => (await import('mock/stores/Trip/getAllTariffsPersonal.json')).default as TTariffPersonal[];

  getAllTariffsPublic = async (): Promise<TTariffPublic> => ({} as any);

  getTariffById = async (): Promise<TTariffPublic> => (await import('mock/stores/Trip/getTariffById.json')).default as TTariffPublic;

  getAllCoopTrips = async (): Promise<TTripCoop[]> => (await import('mock/stores/Trip/getAllSuitableCooperativeTrips.json')).default as TTripCoop[];

  calculateExternalPrices = async (): Promise<TExternalPrices[]> => (await import('mock/stores/Trip/getExternalPrices.json')).default as TExternalPrices[];

  deleteRequest = async (): Promise<number> => Promise.resolve(200);

  finishTripRequest = async (): Promise<number> => Promise.resolve(200);

  joinCoopTrip = async (): Promise<TripRequest> => (await import('mock/stores/Trip/acceptSuitableCooperativeTrip.json')).default as TripRequest;

  saveFile = async (): Promise<SavedFileInfo> => (await {
    id: '9f7c1bfd-679b-4db8-8d34-ff5c3fbce8a4',
    folder: '3fa85f64-5717-4562-b3fc-2c963f66afa6',
    fileName: '2021-03-02 05-55-27_920 images.png',
    fileFormat: 'PNG',
    fileSize: 1992,
  }) as SavedFileInfo;

  saveConfirmSuburbTripFile = async (): Promise<number> => 200;

  saveConfirmCardTripFile = async (): Promise<number> => 200;

  saveTravelCardRequest = async (): Promise<number> => 200;

  saveSuburbCompensation = async (): Promise<number> => 200;

  saveCityTripCompensation = async (): Promise<number> => 200;

  savePublicTripRequest = async (): Promise<number> => 200;

  loadCoopTripsSettings = async (): Promise<TCoopTripsSettings[]> => Promise.resolve([]);

  calculateTripCost = async (): Promise<ITripTariff[]> => (await import('mock/stores/Trip/calculateTripCost.json')).default as ITripTariff[];

  calculateCostForPersonalCarTrip = async (): Promise<ITripTariff[]> => (await import('mock/stores/Trip/calculateTripCost.json')).default as ITripTariff[];

  saveTripRequest = async (): Promise<TripRequest> => (await import('mock/stores/Trip/saveTripRequest.json')).default as TripRequest;

  editTripRequest = (): Promise<number> => Promise.resolve(200);

  rateTripRequest = (): Promise<number> => Promise.resolve(200);
}
