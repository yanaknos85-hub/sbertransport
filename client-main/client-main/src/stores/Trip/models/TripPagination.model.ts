interface ITripPaginationConfig {
  size: number;
  totalElements: number;
  totalPages: number;
  number: number;
}

export class TripPaginationModel implements ITripPaginationConfig {
  size: number;

  totalElements: number;

  totalPages: number;

  number: number;

  constructor(config: ITripPaginationConfig) {
    this.size = config.size;
    this.totalElements = config.totalElements;
    this.totalPages = config.totalPages;
    this.number = config.totalPages;
  }
}
