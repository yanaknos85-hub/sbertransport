interface ICompensationPaginationConfig {
  size: number;
  totalElements: number;
  totalPages: number;
  number: number;
}

export class CompensationPaginationModel implements ICompensationPaginationConfig {
  size: number;

  totalElements: number;

  totalPages: number;

  number: number;

  constructor(config: ICompensationPaginationConfig) {
    this.size = config.size;
    this.totalElements = config.totalElements;
    this.totalPages = config.totalPages;
    this.number = config.totalPages;
  }
}
