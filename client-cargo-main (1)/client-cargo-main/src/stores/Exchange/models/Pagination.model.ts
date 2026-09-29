interface IPaginationConfig {
  size: number;
  totalElements: number;
  totalPages: number;
  number: number;
}

export class PaginationModel implements IPaginationConfig {
  size: number;

  totalElements: number;

  totalPages: number;

  number: number;

  constructor(config: IPaginationConfig) {
    this.size = config.size;
    this.totalElements = config.totalElements;
    this.totalPages = config.totalPages;
    this.number = config.totalPages;
  }
}
