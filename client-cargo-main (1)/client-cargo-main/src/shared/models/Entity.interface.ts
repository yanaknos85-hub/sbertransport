export interface IEntityBase {
  id: string;
  isExisting: boolean;
}

export interface IEntityStatused {
  status?: string;
  isMatched(statuses: string[]): boolean;
}

export interface IEntityRated {
  isRated: boolean;
}
