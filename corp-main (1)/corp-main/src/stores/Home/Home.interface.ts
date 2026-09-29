import { IMetricsMain, IMetricsMainData } from 'modules/RedesignHome/types/Home.types';

interface Settings {
  nameSetting: string;
  valueSetting: string;
  sort: number;
}

interface Controls {
  typeControl: string;
  value: string;
  settings: Settings[];
}

export interface IHomeStore {
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  saveSettings: any;
  metricsData: IMetricsMain;
  setMetricsData(data: IMetricsMain);
}

export interface IUiPreferences {
  userID: string;
  nameForm: string;
  statusCode: number;
  statusDescription: string;
  controls: Controls[];
}

export interface SelectedRowData {
  requestId: string;
  payed: boolean;
}

export interface INotification {
  clientName: string;
  phoneNumber: string;
  email: string;
  receiver: string;
  territory: string;
  comment: string;
  service: string;
  serviceTypes: string[];
}

export interface IHomeService {
  getUiPreferences(userID: string | undefined, nameForm: string): Promise<IUiPreferences>;
  setUiPreferences(userID: string | undefined, data: IUiPreferences): Promise<IUiPreferences>;
  getMetrics(data: IMetricsMainData): Promise<IMetricsMain>;
  saveNotification(data: INotification): Promise<INotification>;
}
