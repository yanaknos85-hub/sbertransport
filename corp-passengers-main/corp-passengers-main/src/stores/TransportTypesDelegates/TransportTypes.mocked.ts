import { injectable } from 'inversify';

import { ITransportType, ITransportTypesService } from './TransportTypes.interface';

@injectable()
export class TransportTypesServiceMocked implements ITransportTypesService {
  // eslint-disable-next-line class-methods-use-this
  getAvailableTransportTypes(): Promise<ITransportType[]> {
    // TODO: add mock
    throw new Error('Method not implemented.');
  }

  getAvailableTransportTypesByService(): Promise<ITransportType[]> {
    // TODO: add mock
    throw new Error('Method not implemented.');
  }

  getTransportTypes = async (): Promise<ITransportType[]> => (await import('mock/stores/TransportTypes/getTransportTypes.json')).default as ITransportType[];
}
