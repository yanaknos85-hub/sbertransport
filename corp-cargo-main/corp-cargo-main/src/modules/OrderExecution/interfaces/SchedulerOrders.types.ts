export interface SchedulerOrder {
  humanReadableId: string;
  status: string;
  tariffType: string;
  nextRequestDate: string;
  lastRequestDate: string;
  period?: string;
  author: string;
  authorMobilePhone: string;
  carrier: string;
  senderAddress: string;
  sender: string;
  senderPhone: string;
  senderOrganization: string;
  recipientAddress: string;
  recipient: string;
  recipientPhone: string;
  recipientOrganization: string;
  plannedRange: number;
  plannedPrice: number;
  cargoType: string;
  loaders: number;
  weight: number;
  volume: number
  creationTime: string;
  comment: string;
  countRequests?: number;
  countRequestsInRoute?: number;
}

export interface SchedulerSearchResponse {
  totalPages: number;
  totalElements: number;
  size: number;
  content: SchedulerOrder[];
  number: number;
  sort: {
    empty: boolean;
    sorted: boolean;
    unsorted: boolean;
  };
  numberOfElements: number;
  pageable: {
    offset: number;
    sort: {
      empty: boolean;
      sorted: boolean;
      unsorted: boolean;
    };
    pageNumber: number;
    pageSize: number;
    paged: boolean;
    unpaged: boolean;
  };
  first: boolean;
  last: boolean;
  empty: boolean;
}