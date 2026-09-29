import { FurnitureItemType } from 'stores/Cargo/Cargo.interface';

export const calculateCargo = (cargo: FurnitureItemType): FurnitureItemType => {
  return {
    ...cargo,
    width: cargo.width / 10,
    length: cargo.length / 10,
    height: cargo.height / 10,
    // Объём приходит в мм3
    // Необходимо делить на 1000, чтобы отправлять данные объёма на бэк в сантиметрах
    volume: cargo.volume / 1000,
  };
};
