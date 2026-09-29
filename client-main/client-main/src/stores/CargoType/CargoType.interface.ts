import type { CargoCreateType, CargoType } from 'types/Cargo';

export interface ICargoTypeStore {
  cargoType: CargoType | null;
  cargoTypes: Record<string, CargoType>;
  cargoTypeAutocompleteList: CargoType[];
  clearState(): void;
  onCargoTypeSelect(name: string, dontClear?: boolean): void;
  searchCargoType(text: string): void;
  postCargoType(data: CargoCreateType): Promise<void>;
}

export interface ICargoTypeService {
  searchCargoType(text: string): Promise<CargoType[]>;
  postCargoType(data: CargoCreateType): Promise<unknown>;
}
