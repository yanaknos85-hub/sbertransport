import { AnalyticTypes } from '../constants/charts.constants';

export interface IValuesForm {
  corpClient: string[];
  year: number;
  monthList: number[];
  services: string[];
  selectedDepartments?: string[];
  contractor?: string;
}

export interface IDataTotal {
  code: string;
  name: string;
  value: number;
  typeValue: string;
}

export interface IDataForPieCharts extends IDataTotal {
  valueForPie: number;
}

export interface ITotals {
  code: string;
  name: string;
  value: number;
  typeValue: string;
}

export interface IDataPerPeriod {
  dateFrom: string;
  dateTo: string;
  metrics: ITotals[];
}

export interface IResponseTotal {
  type: AnalyticTypes;
  data: IDataTotal[];
  totals: ITotals[];
  dataPerPeriod: IDataPerPeriod[] | null;
}

export interface ILimitDataTotal {
  code: string;
  name: string;
  value: number;
  typeValue: string;
  numOfEmployees: number;
  totalBudget: number;
  totalBudgetPerEmployee: number;
  spentBudget: number;
  spentBudgetPerEmployee: number;
  spentBudgetPercent: number;
}

export interface IDataPerTransportType {
  code: string;
  name: string;
  transportType: string;
  typeValue: string;
  totalBudget: number;
  totalBudgetPerEmployee: number;
  spentBudget: number;
  spentBudgetPerEmployee: number;
}

export interface ILimitsResponseTotal {
  type: AnalyticTypes;
  data: ILimitDataTotal[];
  dataPerTransportType: IDataPerTransportType[];
}

export interface ILegendContent {
  totalTitle: string;
  totalValue: number;
  totalSuccessTitle: string;
  totalSuccessValue: number;
  totalFailedTitle: string;
  totalFailedValue: number;
  totalHelperTitle: string;
  totalHelperValue: string;
}

export interface IMetricObject {
  XAxis: string;
  YAxis: number;
  firstValue: number;
  secondValue: number;
  thirdValue?: number;
  label: string;
  totalDateTitle: string;
  totalDateValue: string;
  totalTitle: string;
  totalValue: string | number;
  totalSuccessTitle: string;
  totalSuccessValue: string | number;
  totalFailedTitle: string;
  totalFailedValue: string | number;
  totalCancelledTitle?: string;
  totalCancelledValue?: string | number;
  totalHelperTitle: string;
  totalHelperValue: string | number;
}

export interface ICardContent {
  title: string;
  description: string;
  helper?: string;
}
