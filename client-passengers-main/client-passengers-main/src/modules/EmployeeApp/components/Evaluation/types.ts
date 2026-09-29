import { UUID } from 'utils/io-ts';
import { FC } from 'react';
import { IRequestRating, ITripStore, TripRequest } from 'stores/Trip/Trip.interface';
import Process from './Constants/Process';

export interface IIcons {
  advantages: Record<string, FC>;
  drawbacks: Record<string, FC>;
  title?: FC;
}

export interface IProduct extends IRequestRating {
  process: Process;
  icons?: IIcons;
  iconTitlesMap?: Dictionary<string>;
  isPositive: boolean;

  advantageTitles?: Dictionary<string>;
  drawbackTitles?: Dictionary<string>;

  isRated?: boolean;
  isReadOnly?: boolean;

  setIsPositive(isPositive: boolean): void;
  setRating(rating: number): void;
  setAdvantages(tabNumber: number): void;
  setDrawbacks(tabNumber: number): void;
  setRatingComment(comment: string): void;

  request?: TripRequest;

  clearAdvantages(): void;
  clearDrawbacks(): void;
  setIcons(icons?: IIcons): void;
  setIconTitlesMap(map: Dictionary<string>): void;
  setAdvantageTitles(dic: Dictionary<string>): void;
  setDrawbackTitles(dic: Dictionary<string>): void;
  onOpen(): void;
  onClose(): void;
  onSuccess(data: IRequestRating, reqId: UUID): Promise<number>;
  onFailure(data: IRequestRating, reqId: UUID): Promise<number>;
}

export interface ICreator {
  factoryMethod(props: ITripStore): IProduct;
}

export interface IMood {
  reasons: IIcons['advantages'] | IIcons['drawbacks'];
  reasonKeys: string[];
  reasonTitles: Record<string, string>;
  isPositive: boolean;
}
